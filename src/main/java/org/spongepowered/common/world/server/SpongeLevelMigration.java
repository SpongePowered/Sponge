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
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.filefix.fixes.LevelDatToSavedDataFileFix;
import net.minecraft.util.worldupdate.UpgradeProgress;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.io.file.PathUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles migration of old Sponge world data (UUID, key, player table, map UUIDs, custom data) from the pre-26.1 format
 * to the new per-dimension {@link SpongeRegistryData} and server-level {@link SpongeMapUUIDData} SavedData formats.
 *
 * <p>The migration flow is:</p>
 * <ol>
 *   <li>{@code FileFixerUpperMixin} keeps the old root {@code level.dat}, along with the nether's and end's, BEFORE
 *       vanilla's file fixer rewrites the root one and deletes the other two. It also moves the nether's and end's
 *       to their new folders.</li>
 *   <li>They are cached via {@link #cacheLegacyLevelDats(Map)}.</li>
 *   <li>{@code MinecraftServerMixin.loadLevel()} calls {@link #writeCachedRegistryData(Path)}
 *       AFTER the file fix completes but BEFORE any ServerLevel is created.</li>
 *   <li>When a world other than the default one is first loaded, {@link #readLegacyLevelDat(Path)} reads the level
 *       data that the world kept in its own {@code level.dat}.</li>
 * </ol>
 */
public final class SpongeLevelMigration {

    // The data version at which DataFixers adds vanilla's LevelDatToSavedDataFileFix
    private static final int LEVEL_DAT_TO_SAVED_DATA_VERSION = 4772;

    /**
     * The saved data that {@link LevelDatToSavedDataFileFix} moves out of a {@code level.dat} and that a dimension
     * keeps for itself in 26.1, mapped from where the fix puts the default world's to where Sponge reads it in the
     * dimension's data folder. The border comes from the overworld's file because the fix does not scale that one, and
     * api-18 kept each world's border in that world's own coordinates.
     *
     * <p>We leave out what 26.1 keeps for the server alone, since the default world's copy of it is kept: the world
     * clocks with the day time, the scheduled events and the wandering trader. We also leave out the dragon fight,
     * since api-18 only kept it in the default world's {@code level.dat}.</p>
     */
    private static final Map<String, String> DIMENSION_SAVED_DATA = Map.of(
        "data/minecraft/world_gen_settings.dat", "minecraft/world_gen_settings.dat",
        "data/minecraft/game_rules.dat", "minecraft/game_rules.dat",
        "data/minecraft/weather.dat", "minecraft/weather.dat",
        "data/minecraft/custom_boss_events.dat", "minecraft/custom_boss_events.dat",
        "dimensions/minecraft/overworld/data/minecraft/world_border.dat", "minecraft/world_border.dat"
    );

    // Static fields (not ThreadLocal) because fix() runs on "main" thread
    // but loadLevel() runs on "Server thread".
    private static volatile @Nullable Map<ResourceKey<Level>, CompoundTag> cachedLevelDats;

    private SpongeLevelMigration() {
    }

    /** Caches the whole api-18 {@code level.dat} of the default world, the nether and the end until they are written */
    public static void cacheLegacyLevelDats(final Map<ResourceKey<Level>, CompoundTag> levelDats) {
        cachedLevelDats = levelDats;
    }

    /**
     * Gets the custom data that api-18 merged into the root of a {@code level.dat}, in the form that
     * {@link SpongeRegistryData#applyCustomData} reads it.
     */
    public static Optional<CompoundTag> legacyCustomData(final CompoundTag levelDat) {
        return levelDat.getCompound(Constants.Sponge.Data.V3.SPONGE_TAG_KEY).map(data -> {
            final CompoundTag customData = new CompoundTag();
            customData.put(Constants.Sponge.Data.V3.SPONGE_TAG_KEY, data.copy());
            return customData;
        });
    }

    /**
     * Reads the {@code level.dat} that a dimension kept for itself before 26.1. We first run vanilla's file fix on a
     * copy of it, so the saved data that the fix moves out of the default world's {@code level.dat} is moved into the
     * dimension's own saved data. A file is only moved where the dimension has none yet, because an api-19 world will
     * have saved its own since. The returned tag holds the level data that remains, as the fix leaves it for the
     * default world, or is null if the dimension has none or it cannot be read.
     *
     * @throws UncheckedIOException if what it kept could not be moved, since the world should not load without it
     */
    public static @Nullable CompoundTag readLegacyLevelDat(final Path dimension) {
        final Path levelDat = dimension.resolve("level.dat");
        if (!Files.exists(levelDat)) {
            return null;
        }
        final CompoundTag legacy;
        try {
            legacy = NbtIo.readCompressed(levelDat, NbtAccounter.defaultQuota());
        } catch (final IOException e) {
            SpongeCommon.logger().warn("Failed to load level data from {}", levelDat, e);
            return null;
        }
        final CompoundTag levelData = legacy.getCompoundOrEmpty("Data");
        // This was written after that fix, so there is nothing left in it to move
        if (NbtUtils.getDataVersion(levelData) >= SpongeLevelMigration.LEVEL_DAT_TO_SAVED_DATA_VERSION) {
            return legacy;
        }
        try {
            legacy.put("Data", SpongeLevelMigration.moveToSavedData(levelData, dimension.resolve("data")));
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to move the saved data out of " + levelDat, e);
        }
        return legacy;
    }

    private static CompoundTag moveToSavedData(final CompoundTag levelData, final Path dataFolder) throws IOException {
        // Fix the copy beside the data folder, so what the fix moves out can be moved into it atomically
        final Path fixed = Files.createTempDirectory(dataFolder.getParent(), "sponge-filefix");
        try {
            final CompoundTag levelDat = new CompoundTag();
            levelDat.put("Data", levelData);
            NbtIo.writeCompressed(levelDat, fixed.resolve("level.dat"));
            new LevelDatToSavedDataFileFix(DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SpongeLevelMigration.LEVEL_DAT_TO_SAVED_DATA_VERSION)))
                .runFixOperations(fixed, new UpgradeProgress.Noop());
            for (final Map.Entry<String, String> savedData : SpongeLevelMigration.DIMENSION_SAVED_DATA.entrySet()) {
                final Path moved = fixed.resolve(savedData.getKey());
                final Path target = dataFolder.resolve(savedData.getValue());
                if (Files.exists(moved) && !Files.exists(target)) {
                    // Move it atomically, since a partly moved file would be taken for the dimension's own
                    Files.createDirectories(target.getParent());
                    Files.move(moved, target, StandardCopyOption.ATOMIC_MOVE);
                }
            }
            return NbtIo.readCompressed(fixed.resolve("level.dat"), NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("Data");
        } finally {
            PathUtils.deleteDirectory(fixed);
        }
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
     * Writes the cached worlds' ids, player tables and custom data to per-dimension {@code data/sponge/registry.dat}
     * files, and the default world's map UUIDs to the server's {@code data/sponge/map_uuids.dat}. A file that already
     * exists is left as it is.
     * Must be called AFTER FileFixerUpper.fix() completes (directory structure is final)
     * and BEFORE any ServerLevel is created (so SavedDataStorage finds files on disk).
     */
    public static void writeCachedRegistryData(final Path worldDir) {
        final @Nullable Map<ResourceKey<Level>, CompoundTag> levelDats = cachedLevelDats;
        cachedLevelDats = null;
        if (levelDats == null) {
            return;
        }

        for (final var entry : levelDats.entrySet()) {
            final ResourceKey<Level> dimKey = entry.getKey();
            final CompoundTag spongeData = entry.getValue().getCompoundOrEmpty(Constants.Sponge.Data.V2.SPONGE_DATA);
            final @Nullable UUID uuid = spongeData.read(Constants.Sponge.World.UNIQUE_ID, UUIDUtil.CODEC).orElse(null);
            if (uuid == null) {
                continue;
            }
            final Path registryFile = DimensionType.getStorageFolder(dimKey, worldDir).resolve("data").resolve("sponge").resolve("registry.dat");
            // The nether's and end's level data is left out, since they read it from their own level.dat, which the
            // file fix moved
            final SpongeRegistryData registryData = new SpongeRegistryData(uuid, Optional.of(dimKey), Optional.empty(),
                SpongeLevelMigration.legacyCustomData(entry.getValue()), SpongeLevelMigration.legacyPlayerUniqueIds(spongeData));
            if (SpongeLevelMigration.write(registryFile, SpongeRegistryData.CODEC, registryData)) {
                SpongeCommon.logger().info("[Sponge]   Wrote migrated {} UUID {} -> {}", dimKey.identifier(), uuid, registryFile);
            }
        }

        // Only the default world's map UUIDs were ever used, so we only migrate those
        final Map<Integer, UUID> mapUUIDs = Optional.ofNullable(levelDats.get(Level.OVERWORLD))
            .flatMap(defaultWorld -> defaultWorld.getCompoundOrEmpty(Constants.Sponge.Data.V2.SPONGE_DATA)
                .read(Constants.Map.MAP_UUID_INDEX, SpongeMapUUIDData.MAP_INDEX_CODEC))
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
