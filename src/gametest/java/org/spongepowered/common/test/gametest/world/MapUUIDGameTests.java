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
package org.spongepowered.common.test.gametest.world;

import net.minecraft.core.UUIDUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.map.MapStorage;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.server.SpongeLevelMigration;
import org.spongepowered.common.world.server.SpongeMapUUIDData;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tests for Sponge's map UUIDs, which live in the server's {@code data/sponge/map_uuids.dat} beside vanilla's maps.
 * They are saved as they are handed out, and can be read back from what api-19 and the api-18 migration wrote.
 */
@GameTestGroup("map_uuids")
public final class MapUUIDGameTests {

    @GameTest(maxTicks = 1)
    void createdMapUuidIsSaved(final GameTestHelper helper) {
        final MinecraftServer server = helper.getLevel().getServer();
        final MapStorage maps = Sponge.server().mapStorage();
        // Have the map UUIDs' saved data made and saved first, so the next save only writes it because of the new UUID
        maps.mapInfo(UUID.randomUUID());
        server.saveAllChunks(true, true, true);

        final MapInfo map = maps.createNewMapInfo().orElseThrow(() -> new IllegalStateException("No map was created"));
        server.saveAllChunks(true, true, true);

        final Path file = server.getWorldPath(LevelResource.DATA).resolve("sponge").resolve("map_uuids.dat");
        GameTestAssertions.assertTrue(helper, Files.exists(file), "The save wrote no " + file);
        final CompoundTag uuids = MapUUIDGameTests.read(file)
            .getCompoundOrEmpty("data")
            .getCompoundOrEmpty("map_uuids");
        GameTestAssertions.assertTrue(
            helper,
            uuids.keySet().stream().anyMatch(id -> uuids.read(id, UUIDUtil.CODEC).equals(Optional.of(map.uniqueId()))),
            "map_uuids.dat has " + uuids + " without the new map's " + map.uniqueId()
        );
        helper.succeed();
    }

    // api-19's migration wrote the api-18 index as legacy_map_uuids instead of map_uuids
    @GameTest(maxTicks = 1)
    void legacyMapUuidsAreRead(final GameTestHelper helper) {
        final MinecraftServer server = helper.getLevel().getServer();
        final Path dataFolder = server.getWorldPath(LevelResource.ROOT)
            .resolve("gametest_legacy_map_uuids")
            .resolve("data");
        final UUID uuid = UUID.randomUUID();
        final CompoundTag legacy = new CompoundTag();
        legacy.store("7", UUIDUtil.CODEC, uuid);
        final CompoundTag data = new CompoundTag();
        data.put("legacy_map_uuids", legacy);
        final CompoundTag file = new CompoundTag();
        file.put("data", data);
        NbtUtils.addCurrentDataVersion(file);
        try {
            Files.createDirectories(dataFolder.resolve("sponge"));
            NbtIo.writeCompressed(file, dataFolder.resolve("sponge").resolve("map_uuids.dat"));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        final @Nullable UUID read = MapUUIDGameTests.mapUuid(server, dataFolder, 7);
        GameTestAssertions.assertTrue(
            helper,
            uuid.equals(read),
            "Map 7 has UUID " + read + " rather than the legacy " + uuid
        );
        helper.succeed();
    }

    // What the migration from api-18 writes can be read back: the map UUIDs, and the default world's id and players
    @GameTest(maxTicks = 1)
    void migratedSpongeDataIsRead(final GameTestHelper helper) {
        final MinecraftServer server = helper.getLevel().getServer();
        final Path worldDir = server.getWorldPath(LevelResource.ROOT).resolve("gametest_migration");
        final UUID uniqueId = UUID.randomUUID();
        final UUID mapUuid = UUID.randomUUID();
        final UUID player = UUID.randomUUID();
        // Set these up as api-18 kept them in the root level.dat
        final CompoundTag mapUUIDs = new CompoundTag();
        mapUUIDs.store("3", UUIDUtil.CODEC, mapUuid);
        final CompoundTag spongeData = new CompoundTag();
        spongeData.store(Constants.Sponge.World.UNIQUE_ID, UUIDUtil.CODEC, uniqueId);
        spongeData.put(Constants.Map.MAP_UUID_INDEX, mapUUIDs);
        spongeData.store(
            Constants.Sponge.SPONGE_PLAYER_UUID_TABLE,
            UUIDUtil.CODEC.listOf(),
            List.of(UUID.randomUUID(), player)
        );

        SpongeLevelMigration.cacheSpongeData(Map.of(Level.OVERWORLD, spongeData));
        SpongeLevelMigration.writeCachedRegistryData(worldDir);

        final @Nullable UUID read = MapUUIDGameTests.mapUuid(server, worldDir.resolve("data"), 3);
        GameTestAssertions.assertTrue(
            helper,
            mapUuid.equals(read),
            "Map 3 has UUID " + read + " rather than the migrated " + mapUuid
        );
        final CompoundTag registry = MapUUIDGameTests.read(
            DimensionType.getStorageFolder(Level.OVERWORLD, worldDir)
                .resolve("data").resolve("sponge").resolve("registry.dat")
        ).getCompoundOrEmpty("data");
        GameTestAssertions.assertTrue(
            helper,
            registry.read("uuid", UUIDUtil.CODEC).equals(Optional.of(uniqueId)),
            "The default world's registry.dat has id " + registry.read("uuid", UUIDUtil.CODEC)
                + " rather than " + uniqueId
        );
        final Optional<UUID> migratedPlayer = registry.getCompoundOrEmpty("player_uuids").read("1", UUIDUtil.CODEC);
        GameTestAssertions.assertTrue(
            helper,
            migratedPlayer.equals(Optional.of(player)),
            "The default world's player 1 is " + migratedPlayer + " rather than " + player
        );
        helper.succeed();
    }

    // Reads the map UUIDs the same way the server's saved data does
    private static @Nullable UUID mapUuid(final MinecraftServer server, final Path dataFolder, final int id) {
        try (final SavedDataStorage storage = new SavedDataStorage(
            dataFolder,
            server.getFixerUpper(),
            server.registryAccess()
        )) {
            final @Nullable SpongeMapUUIDData data = storage.get(SpongeMapUUIDData.TYPE);
            return data == null ? null : data.mapUUIDIndex().get(id);
        }
    }

    private static CompoundTag read(final Path file) {
        try {
            return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
