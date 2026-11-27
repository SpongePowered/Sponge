/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.world.server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles migration of old Sponge world data (UUID, key, player table, map UUIDs) from the pre-26.1 format
 * to the new per-dimension {@link SpongeRegistryData} and server-level {@link SpongeMapUUIDData} SavedData formats.
 *
 * <p>The migration flow is:</p>
 * <ol>
 *   <li>{@code FileFixerUpperMixin} extracts the old {@code SpongeData} of level.dat and the per-dimension
 *       level.dat files BEFORE vanilla's file fixer rewrites them.</li>
 *   <li>They are cached via {@link #cacheSpongeData(Map)}.</li>
 *   <li>{@code MinecraftServerMixin.loadLevel()} calls {@link #writeCachedRegistryData(Path)}
 *       AFTER the file fix completes but BEFORE any ServerLevel is created.</li>
 * </ol>
 */
public final class SpongeLevelMigration {

    // Static fields (not ThreadLocal) because fix() runs on "main" thread
    // but loadLevel() runs on "Server thread".
    private static volatile @Nullable Map<ResourceKey<Level>, CompoundTag> cachedSpongeData;

    private SpongeLevelMigration() {
    }

    public static void cacheSpongeData(final Map<ResourceKey<Level>, CompoundTag> spongeData) {
        cachedSpongeData = spongeData;
    }

    /** Gets the players of the player table in a world's api-18 {@code SpongeData}, by the index api-18 gave them */
    public static Map<Integer, UUID> legacyPlayerUniqueIds(final CompoundTag spongeData) {
        return SpongeLevelMigration.indexPlayers(
            spongeData.read(Constants.Sponge.SPONGE_PLAYER_UUID_TABLE, UUIDUtil.CODEC.listOf()).orElse(List.of()));
    }

    // Indexes the table the same way api-18 read it
    static Map<Integer, UUID> indexPlayers(final List<UUID> table) {
        final BiMap<Integer, UUID> players = HashBiMap.create();
        table.forEach(uuid -> players.inverse().putIfAbsent(uuid, players.size()));
        return players;
    }

    /**
     * Writes the cached worlds' ids and player tables to per-dimension {@code data/sponge/registry.dat} files, and the
     * default world's map UUIDs to the server's {@code data/sponge/map_uuids.dat}, where none exist yet.
     * Must be called AFTER FileFixerUpper.fix() completes (directory structure is final)
     * and BEFORE any ServerLevel is created (so SavedDataStorage finds files on disk).
     */
    public static void writeCachedRegistryData(final Path worldDir) {
        final @Nullable Map<ResourceKey<Level>, CompoundTag> spongeData = cachedSpongeData;
        cachedSpongeData = null;
        if (spongeData == null) {
            return;
        }

        for (final var entry : spongeData.entrySet()) {
            final ResourceKey<Level> dimKey = entry.getKey();
            final @Nullable UUID uuid = entry.getValue().read(Constants.Sponge.World.UNIQUE_ID, UUIDUtil.CODEC).orElse(null);
            if (uuid == null) {
                continue;
            }
            final Path registryFile = DimensionType.getStorageFolder(dimKey, worldDir).resolve("data").resolve("sponge").resolve("registry.dat");
            final SpongeRegistryData registryData = new SpongeRegistryData(uuid, Optional.of(dimKey),
                SpongeLevelMigration.legacyPlayerUniqueIds(entry.getValue()));
            if (SpongeLevelMigration.write(registryFile, SpongeRegistryData.CODEC, registryData)) {
                SpongeCommon.logger().info("[Sponge]   Wrote migrated {} UUID {} -> {}", dimKey.identifier(), uuid, registryFile);
            }
        }

        // Only the default world's map UUIDs were ever used, so we only migrate those
        final Map<Integer, UUID> mapUUIDs = Optional.ofNullable(spongeData.get(Level.OVERWORLD))
            .flatMap(defaultWorld -> defaultWorld.read(Constants.Map.MAP_UUID_INDEX, SpongeMapUUIDData.MAP_INDEX_CODEC))
            .orElse(Map.of());
        final Path mapUUIDFile = worldDir.resolve("data").resolve("sponge").resolve("map_uuids.dat");
        if (!mapUUIDs.isEmpty() && SpongeLevelMigration.write(mapUUIDFile, SpongeMapUUIDData.CODEC, new SpongeMapUUIDData(mapUUIDs, Map.of(), List.of()))) {
            SpongeCommon.logger().info("[Sponge]   Wrote migrated map UUIDs -> {}", mapUUIDFile);
        }
    }

    // Writes the data the same way SavedDataStorage does, unless the file already exists
    private static <T> boolean write(final Path file, final Codec<T> codec, final T data) {
        if (Files.exists(file)) {
            return false;
        }
        try {
            final CompoundTag savedDataTag = new CompoundTag();
            savedDataTag.put("data", codec.encodeStart(NbtOps.INSTANCE, data).getOrThrow());
            NbtUtils.addCurrentDataVersion(savedDataTag);
            Files.createDirectories(file.getParent());
            NbtIo.writeCompressed(savedDataTag, file);
            return true;
        } catch (final IOException | IllegalStateException e) {
            SpongeCommon.logger().warn("[Sponge]   Failed to write {}", file, e);
            return false;
        }
    }
}
