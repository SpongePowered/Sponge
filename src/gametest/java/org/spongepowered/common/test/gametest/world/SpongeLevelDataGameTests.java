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

import com.mojang.serialization.Lifecycle;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.worldupdate.UpgradeProgress;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import org.apache.commons.io.file.PathUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.generation.config.flat.FlatGeneratorConfigs;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldArchetype;
import org.spongepowered.api.world.server.WorldArchetypeType;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeDataRegistration;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.server.SpongeLevelMigration;
import org.spongepowered.common.world.server.SpongeRegistryData;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Tests for what each world keeps in its own saved data. A world other than the default one keeps its level data and
 * custom data there, while the default world only keeps its custom data there. Every world also keeps the players its
 * chunks track. The tests change these, save, and load them back. They only read the files for what a load can't
 * show: the default world's data, the root {@code level.dat} that an unloaded world's save must not touch, and which
 * table a player is in.
 */
@GameTestGroup("level_data")
public final class SpongeLevelDataGameTests {

    // Loading and unloading a world finish on Sponge's async scheduler, which runs on wall-clock time while the game
    // test server ticks as fast as it can. This is a budget for a slow machine, not how long the tests take
    private static final int MAX_TICKS = 20 * 60 * 5;
    private static final long SEED = 0x5EED_CAFEL;
    private static final Vector3i SPAWN = new Vector3i(37, 70, -21);
    private static final long GAME_TIME = 123_456L;
    // These match the values in the fixtures
    private static final UUID LEGACY_ID = UUID.fromString("7a1e9c3b-18a2-4d5e-9f00-00000a181121");
    private static final long LEGACY_SEED = 0x0A18_5EED_1121L;
    private static final long LEGACY_SERVER_SEED = 0x0A18_0DEF_A017L;
    private static final double LEGACY_BORDER = 1234.0;

    @GameTest(maxTicks = SpongeLevelDataGameTests.MAX_TICKS)
    void reloadedWorldKeepsItsId(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_reloaded_id");
        final WorldManager manager = Sponge.server().worldManager();
        // Reuse the already-loaded overworld's archetype type. A freshly built ChunkGenerator.flat(...)
        // uses entries from the global registry and fails to encode against the gametest server's
        // registry set on Forge. Spawn logic is disabled so the test does not generate a full spawn
        // area while it only needs the world to be created, saved, unloaded and loaded again.
        final ServerWorld overworld = manager.world(ResourceKey.minecraft("overworld"))
            .orElseThrow(() -> new IllegalStateException("The overworld is not loaded"));
        final ServerWorldProperties.LoadOptions create = ServerWorldProperties.LoadOptions.builder()
            .load()
            .create(WorldArchetype.of(overworld.properties().worldArchetypeType()))
            .createCallback(properties -> properties.setPerformsSpawnLogic(false))
            .build();

        final CompletableFuture<UUID[]> ids = manager.loadWorld(key, create)
            .thenApply(Optional::orElseThrow)
            .thenCompose(created -> manager.unloadWorld(key).thenApply($ -> created.uniqueId()))
            .thenCompose(first -> manager.loadWorld(key)
                .thenApply(Optional::orElseThrow)
                .thenCompose(reloaded -> manager.unloadWorld(key)
                    .thenApply($ -> new UUID[]{first, reloaded.uniqueId()})));

        helper.succeedWhen(() -> {
            final UUID[] pair = SpongeLevelDataGameTests.finished(helper, ids);
            GameTestAssertions.assertTrue(helper, pair[0].equals(pair[1]),
                "The reloaded world's id was " + pair[1] + " rather than " + pair[0]);
        });
    }

    // A world loaded by key alone keeps what it was unloaded with. It isn't new either, so its spawn isn't chosen again
    @GameTest(maxTicks = SpongeLevelDataGameTests.MAX_TICKS)
    void reloadedWorldKeepsItsProperties(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_reloaded_properties");
        final MinecraftServer server = helper.getLevel().getServer();
        final WorldManager manager = Sponge.server().worldManager();
        final List<Loaded> loads = SpongeLevelDataGameTests.loads(key);

        final CompletableFuture<Properties> saved = SpongeLevelDataGameTests.create(key)
            .thenApplyAsync(created -> {
                SpongeLevelDataGameTests.change(created.properties());
                return created;
            }, server)
            .thenCompose(created -> SpongeLevelDataGameTests.unload(key)
                .thenApply($ -> Properties.of(created.properties())))
            .thenCompose(properties -> SpongeLevelDataGameTests.load(key).thenApply($ -> properties))
            .thenCompose(properties -> SpongeLevelDataGameTests.unload(key)
                .thenCompose($ -> manager.deleteWorld(key))
                .thenApply($ -> properties));

        helper.succeedWhen(() -> SpongeLevelDataGameTests.assertLoadedAgain(
            helper,
            loads,
            SpongeLevelDataGameTests.finished(helper, saved)
        ));
    }

    // Saving the properties of an unloaded world writes them to that world, not over the default world's level.dat
    @GameTest(maxTicks = SpongeLevelDataGameTests.MAX_TICKS)
    void unloadedWorldSavesItsProperties(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_unloaded_properties");
        final MinecraftServer server = helper.getLevel().getServer();
        final WorldManager manager = Sponge.server().worldManager();
        final Path levelDat = server.getWorldPath(LevelResource.LEVEL_DATA_FILE);
        final List<Loaded> loads = SpongeLevelDataGameTests.loads(key);
        final CompoundTag[] defaultWorld = new CompoundTag[2];
        final GameMode[] gameModes = new GameMode[2];

        final CompletableFuture<Properties> saved = SpongeLevelDataGameTests.create(key)
            .thenCompose($ -> SpongeLevelDataGameTests.unload(key))
            .thenCompose($ -> SpongeLevelDataGameTests.loadProperties(key))
            .thenComposeAsync(properties -> {
                // The root level.dat is only there once the server has saved
                server.saveAllChunks(true, true, true);
                defaultWorld[0] = SpongeLevelDataGameTests.defaultWorld(levelDat);
                SpongeLevelDataGameTests.change(properties);
                gameModes[0] = properties.gameMode().equals(GameModes.ADVENTURE.get())
                    ? GameModes.SURVIVAL.get()
                    : GameModes.ADVENTURE.get();
                properties.setGameMode(gameModes[0]);
                return manager.saveProperties(properties).thenApply(ok -> {
                    if (!ok) {
                        throw new IllegalStateException("The properties of " + key + " were not saved");
                    }
                    defaultWorld[1] = SpongeLevelDataGameTests.defaultWorld(levelDat);
                    return Properties.of(properties);
                });
            }, server)
            .thenCompose(properties -> SpongeLevelDataGameTests.loadProperties(key).thenApply(again -> {
                gameModes[1] = again.gameMode();
                return properties;
            }))
            .thenCompose(properties -> SpongeLevelDataGameTests.load(key).thenApply($ -> properties))
            .thenCompose(properties -> SpongeLevelDataGameTests.unload(key)
                .thenCompose($ -> manager.deleteWorld(key))
                .thenApply($ -> properties));

        helper.succeedWhen(() -> {
            final Properties properties = SpongeLevelDataGameTests.finished(helper, saved);
            GameTestAssertions.assertTrue(
                helper,
                defaultWorld[1].equals(defaultWorld[0]),
                "Saving the unloaded world's properties changed the default world's level.dat to " + defaultWorld[1]
                    + " from " + defaultWorld[0]
            );
            // Check the properties only, since vanilla gives every world it builds the server's game mode
            GameTestAssertions.assertTrue(
                helper,
                gameModes[1].equals(gameModes[0]),
                "The saved properties have game mode " + gameModes[1] + " rather than " + gameModes[0]
            );
            SpongeLevelDataGameTests.assertLoadedAgain(helper, loads, properties);
        });
    }

    // Only a restart would load the default world's custom data back, so check its registry.dat instead
    @GameTest(maxTicks = 1)
    void defaultWorldSavesItsCustomData(final GameTestHelper helper) {
        final MinecraftServer server = helper.getLevel().getServer();
        final ServerWorldProperties properties = (ServerWorldProperties) server.overworld().getLevelData();
        final String value = UUID.randomUUID().toString();
        properties.offer(CustomData.KEY, value);
        try {
            server.saveAllChunks(true, true, true);
            final Path registry = DimensionType.getStorageFolder(
                Level.OVERWORLD,
                server.getWorldPath(LevelResource.ROOT)
            ).resolve("data").resolve("sponge").resolve("registry.dat");
            final CompoundTag customData = SpongeLevelDataGameTests.read(registry)
                .getCompoundOrEmpty("data")
                .getCompoundOrEmpty("custom_data");
            GameTestAssertions.assertTrue(
                helper,
                customData.toString().contains(value),
                "The default world's registry.dat has custom data " + customData + " without " + value
            );
        } finally {
            properties.remove(CustomData.KEY);
        }
        helper.succeed();
    }

    // Each world has its own player table, as on api-18, and loading it by key reads the table back
    @GameTest(maxTicks = SpongeLevelDataGameTests.MAX_TICKS)
    void reloadedWorldKeepsItsPlayers(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_reloaded_players");
        final MinecraftServer server = helper.getLevel().getServer();
        final Path registry = Sponge.server().worldManager().worldDirectory(key)
            .resolve("data").resolve("sponge").resolve("registry.dat");
        final Path defaultRegistry = DimensionType.getStorageFolder(
            Level.OVERWORLD,
            server.getWorldPath(LevelResource.ROOT)
        ).resolve("data").resolve("sponge").resolve("registry.dat");
        final BlockPos defaultPos = helper.absolutePos(BlockPos.ZERO);
        final ServerLocation defaultLocation = ((ServerWorld) server.overworld()).location(
            defaultPos.getX(),
            defaultPos.getY(),
            defaultPos.getZ()
        );
        final UUID creator = UUID.randomUUID();
        final UUID notifier = UUID.randomUUID();
        final UUID defaultCreator = UUID.randomUUID();
        final Vector3i pos = new Vector3i(1, 64, 1);
        final List<Set<UUID>> tables = new CopyOnWriteArrayList<>();

        final CompletableFuture<List<Optional<UUID>>> tracked = SpongeLevelDataGameTests.create(key)
            .thenApplyAsync(world -> {
                world.location(pos).offer(Keys.CREATOR, creator);
                world.location(pos).offer(Keys.NOTIFIER, notifier);
                defaultLocation.offer(Keys.CREATOR, defaultCreator);
                return world;
            }, server)
            .thenCompose($ -> SpongeLevelDataGameTests.unload(key))
            .thenApplyAsync($ -> {
                // Save only the default world, so the root level.dat that other tests read is left alone
                server.overworld().save(null, true, false);
                defaultLocation.remove(Keys.CREATOR);
                tables.add(SpongeLevelDataGameTests.players(registry));
                tables.add(SpongeLevelDataGameTests.players(defaultRegistry));
                return true;
            }, server)
            .thenCompose($ -> SpongeLevelDataGameTests.load(key))
            .thenApplyAsync(
                world -> List.of(world.location(pos).get(Keys.CREATOR), world.location(pos).get(Keys.NOTIFIER)),
                server
            )
            .thenCompose(players -> SpongeLevelDataGameTests.unload(key)
                .thenCompose($ -> Sponge.server().worldManager().deleteWorld(key))
                .thenApply($ -> players));

        helper.succeedWhen(() -> {
            final List<Optional<UUID>> players = SpongeLevelDataGameTests.finished(helper, tracked);
            GameTestAssertions.assertTrue(
                helper,
                tables.get(0).contains(creator)
                && tables.get(0).contains(notifier)
                && !tables.get(0).contains(defaultCreator),
                "The world's registry.dat has players " + tables.get(0) + " rather than its own " + creator
                    + " and " + notifier
            );
            GameTestAssertions.assertTrue(
                helper,
                tables.get(1).contains(defaultCreator)
                && !tables.get(1).contains(creator)
                && !tables.get(1).contains(notifier),
                "The default world's registry.dat has players " + tables.get(1)
                    + " rather than its own " + defaultCreator
            );
            GameTestAssertions.assertTrue(
                helper,
                players.equals(List.of(Optional.of(creator), Optional.of(notifier))),
                "The reloaded world's block has creator and notifier " + players + " rather than " + creator
                    + " and " + notifier
            );
        });
    }

    // A pre-26.1 world with no registry.dat yet takes its id and players from its level.dat's SpongeData
    @GameTest(maxTicks = SpongeLevelDataGameTests.MAX_TICKS)
    void legacyWorldKeepsItsIdAndPlayers(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_legacy_players");
        final MinecraftServer server = helper.getLevel().getServer();
        final Path directory = Sponge.server().worldManager().worldDirectory(key);
        final UUID uniqueId = UUID.randomUUID();
        final UUID player = UUID.randomUUID();

        final CompletableFuture<List<Optional<UUID>>> loaded = SpongeLevelDataGameTests.create(key)
            .thenCompose($ -> SpongeLevelDataGameTests.unload(key))
            .thenApplyAsync($ -> {
                // Set it up as api-18 left it, with the level data in the world's own level.dat beside its SpongeData
                final Path registry = directory.resolve("data").resolve("sponge").resolve("registry.dat");
                final CompoundTag spongeData = new CompoundTag();
                spongeData.store(Constants.Sponge.World.UNIQUE_ID, UUIDUtil.CODEC, uniqueId);
                spongeData.store(
                    Constants.Sponge.SPONGE_PLAYER_UUID_TABLE,
                    UUIDUtil.CODEC.listOf(),
                    List.of(UUID.randomUUID(), player)
                );
                final CompoundTag levelDat = new CompoundTag();
                levelDat.put(
                    "Data",
                    SpongeLevelDataGameTests.read(registry).getCompoundOrEmpty("data").getCompoundOrEmpty("level_data")
                );
                levelDat.put(Constants.Sponge.Data.V2.SPONGE_DATA, spongeData);
                try {
                    Files.delete(registry);
                    NbtIo.writeCompressed(levelDat, directory.resolve("level.dat"));
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
                return true;
            }, server)
            .thenCompose($ -> SpongeLevelDataGameTests.load(key))
            .thenApplyAsync(
                world -> List.of(
                    Optional.of(world.uniqueId()),
                    ((PrimaryLevelDataBridge) world.properties()).bridge$getUniqueIdForIndex(1)
                ),
                server
            )
            .thenCompose(ids -> SpongeLevelDataGameTests.unload(key)
                .thenCompose($ -> Sponge.server().worldManager().deleteWorld(key))
                .thenApply($ -> ids));

        helper.succeedWhen(() -> {
            final List<Optional<UUID>> ids = SpongeLevelDataGameTests.finished(helper, loaded);
            GameTestAssertions.assertTrue(
                helper,
                ids.get(0).equals(Optional.of(uniqueId)),
                "The legacy world loaded with id " + ids.get(0) + " rather than its own " + uniqueId
            );
            GameTestAssertions.assertTrue(
                helper,
                ids.get(1).equals(Optional.of(player)),
                "The legacy world's player 1 is " + ids.get(1) + " rather than " + player
            );
        });
    }

    // An upgraded api-18 world loads its seed, game rules, weather and border from its old level.dat. Once it is saved,
    // it keeps them in its own saved data along with its own changes, and no longer reads the old file
    @GameTest(maxTicks = SpongeLevelDataGameTests.MAX_TICKS)
    void legacyWorldKeepsItsSavedData(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_legacy_upgrade");
        final MinecraftServer server = helper.getLevel().getServer();
        final WorldManager manager = Sponge.server().worldManager();
        final Key<Value<String>> customData = CustomData.KEY;
        final List<Legacy> loads = new CopyOnWriteArrayList<>();

        final CompletableFuture<Boolean> loaded = manager.deleteWorld(key)
            .thenApply($ -> {
                SpongeLevelDataGameTests.copyFixture(
                    "dimensions/sponge/gametest_legacy_upgrade/level.dat",
                    manager.worldDirectory(key).resolve("level.dat")
                );
                return true;
            })
            .thenCompose($ -> SpongeLevelDataGameTests.load(key))
            .thenApplyAsync(world -> {
                loads.add(Legacy.of(world));
                // If the legacy level.dat were read again, it would undo these changes
                world.properties().offer(customData, "26.1");
                ((ServerLevel) world).getGameRules().set(GameRules.KEEP_INVENTORY, false, null);
                return true;
            }, server)
            .thenCompose($ -> SpongeLevelDataGameTests.unload(key))
            .thenCompose($ -> SpongeLevelDataGameTests.load(key))
            .thenApplyAsync(world -> loads.add(Legacy.of(world)), server)
            .thenCompose($ -> SpongeLevelDataGameTests.unload(key).thenCompose($$ -> manager.deleteWorld(key)));

        helper.succeedWhen(() -> {
            SpongeLevelDataGameTests.finished(helper, loaded);
            final Legacy legacy = new Legacy(
                SpongeLevelDataGameTests.LEGACY_ID,
                SpongeLevelDataGameTests.LEGACY_SEED,
                true,
                true,
                SpongeLevelDataGameTests.LEGACY_BORDER,
                Optional.of("api-18")
            );
            GameTestAssertions.assertTrue(
                helper,
                loads.get(0).equals(legacy),
                "The legacy world loaded with " + loads.get(0) + " rather than its own " + legacy
            );
            final Legacy changed = new Legacy(
                legacy.uniqueId(),
                legacy.seed(),
                false,
                legacy.raining(),
                legacy.border(),
                Optional.of("26.1")
            );
            GameTestAssertions.assertTrue(
                helper,
                loads.get(1).equals(changed),
                "The legacy world loaded again with " + loads.get(1) + " rather than what it was saved with, " + changed
            );
        });
    }

    // The api-18 upgrade keeps the custom data from the root of the default world's level.dat. It also keeps the
    // nether's level data, whose saved data is moved out when the nether first loads from its new folder
    @GameTest(maxTicks = 1)
    void upgradeKeepsTheLevelDats(final GameTestHelper helper) throws IOException {
        final MinecraftServer server = helper.getLevel().getServer();
        final Key<Value<String>> customData = CustomData.KEY;
        final Path saves = Files.createTempDirectory("sponge-gametest-upgrade");
        try {
            final Path worldDir = saves.resolve("world");
            SpongeLevelDataGameTests.copyFixture("level.dat", worldDir.resolve("level.dat"));
            SpongeLevelDataGameTests.copyFixture("DIM-1/level.dat", worldDir.resolve("DIM-1").resolve("level.dat"));
            // Do what the server does on start: fix the api-18 world, then write what the fix kept before levels load
            try (final LevelStorageSource.LevelStorageAccess access = LevelStorageSource.createDefault(saves)
                .createAccess("world")) {
                DataFixers.getFileFixer().fix(access, access.getUnfixedDataTag(false), new UpgradeProgress());
            }
            SpongeLevelMigration.writeCachedRegistryData(worldDir);

            final CompoundTag registry = SpongeLevelDataGameTests.read(
                DimensionType.getStorageFolder(Level.OVERWORLD, worldDir)
                    .resolve("data").resolve("sponge").resolve("registry.dat")
            ).getCompoundOrEmpty("data");
            final PrimaryLevelData properties = new PrimaryLevelData(
                ((PrimaryLevelData) server.getWorldData()).getLevelSettings(),
                PrimaryLevelData.SpecialWorldProperty.NONE,
                Lifecycle.stable()
            );
            SpongeRegistryData.applyCustomData(properties, registry.getCompoundOrEmpty("custom_data"));
            final Optional<String> defaultCustomData = ((ServerWorldProperties) properties).get(customData);
            GameTestAssertions.assertTrue(
                helper,
                defaultCustomData.equals(Optional.of("api-18 default world")),
                "The default world's registry.dat has custom data " + defaultCustomData
                    + " rather than its api-18 level.dat's"
            );

            final Path nether = DimensionType.getStorageFolder(Level.NETHER, worldDir);
            GameTestAssertions.assertTrue(
                helper,
                Files.notExists(worldDir.resolve("DIM-1")),
                "The fix left DIM-1 behind"
            );
            // Save newer game rules, as an api-19 run would have. The legacy level.dat's rules must not replace them
            try (final SavedDataStorage storage = new SavedDataStorage(
                nether.resolve("data"),
                server.getFixerUpper(),
                server.registryAccess()
            )) {
                final GameRuleMap savedSince = GameRuleMap.of();
                new GameRules(server.getWorldData().enabledFeatures(), savedSince);
                storage.set(GameRuleMap.TYPE, savedSince);
            }
            final @Nullable CompoundTag legacy = SpongeLevelMigration.readLegacyLevelDat(nether);
            GameTestAssertions.assertTrue(helper, legacy != null, "The fix left the nether no level.dat in " + nether);
            final Optional<BlockPos> spawn = legacy.getCompoundOrEmpty("Data")
                .read("spawn", LevelData.RespawnData.CODEC)
                .map(LevelData.RespawnData::pos);
            GameTestAssertions.assertTrue(
                helper,
                spawn.equals(Optional.of(new BlockPos(8, 70, -8))),
                "The nether's level data has spawn " + spawn + " rather than its api-18 level.dat's"
            );
            try (final SavedDataStorage storage = new SavedDataStorage(
                nether.resolve("data"),
                server.getFixerUpper(),
                server.registryAccess()
            )) {
                final @Nullable WorldGenSettings worldGenSettings = storage.get(WorldGenSettings.TYPE);
                final @Nullable GameRuleMap gameRules = storage.get(GameRuleMap.TYPE);
                final @Nullable WeatherData weather = storage.get(WeatherData.TYPE);
                GameTestAssertions.assertTrue(
                    helper,
                    worldGenSettings != null
                    && worldGenSettings.options().seed() == SpongeLevelDataGameTests.LEGACY_SERVER_SEED,
                    "The nether's saved world gen settings are " + worldGenSettings
                        + " rather than its api-18 level.dat's"
                );
                GameTestAssertions.assertTrue(
                    helper,
                    gameRules != null
                    && !new GameRules(server.getWorldData().enabledFeatures(), gameRules).get(GameRules.KEEP_INVENTORY),
                    "The nether's saved game rules are " + gameRules
                        + " rather than those saved since its api-18 level.dat"
                );
                GameTestAssertions.assertTrue(
                    helper,
                    weather != null && weather.isRaining(),
                    "The nether's saved weather is not its api-18 level.dat's"
                );
            }
        } finally {
            PathUtils.deleteDirectory(saves);
        }
        helper.succeed();
    }

    // Uses a superflat, so creating the world generates next to nothing. Any world an earlier run left is deleted first
    private static CompletableFuture<ServerWorld> create(final ResourceKey key) {
        final WorldArchetypeType type = WorldArchetypeType.of(
            WorldTypes.OVERWORLD.get(),
            ChunkGenerator.flat(FlatGeneratorConfigs.CLASSIC_FLAT.get())
        );
        final WorldArchetype archetype = WorldArchetype.of(
            type,
            WorldGenerationConfig.builder().seed(SpongeLevelDataGameTests.SEED).build()
        );
        final WorldManager manager = Sponge.server().worldManager();
        return manager.deleteWorld(key)
            .thenCompose($ -> manager.loadWorld(
                key,
                ServerWorldProperties.LoadOptions.create(archetype, p -> p.setPerformsSpawnLogic(false))
            ))
            .thenApply(world -> world.orElseThrow(() -> new IllegalStateException(key + " was not created")));
    }

    private static CompletableFuture<ServerWorld> load(final ResourceKey key) {
        return Sponge.server().worldManager().loadWorld(key)
            .thenApply(world -> world.orElseThrow(() -> new IllegalStateException(key + " was not loaded by its key")));
    }

    private static CompletableFuture<ServerWorldProperties> loadProperties(final ResourceKey key) {
        return Sponge.server().worldManager().loadProperties(key)
            .thenApply(properties -> properties.orElseThrow(
                () -> new IllegalStateException("The properties of " + key + " were not loaded")
            ));
    }

    private static CompletableFuture<Boolean> unload(final ResourceKey key) {
        return Sponge.server().worldManager().unloadWorld(key)
            .thenApply(unloaded -> {
                if (!unloaded) {
                    throw new IllegalStateException(key + " was not unloaded");
                }
                return true;
            });
    }

    // Records the properties at each load of the world, once it is prepared and before it ticks
    private static List<Loaded> loads(final ResourceKey key) {
        final List<Loaded> loads = new CopyOnWriteArrayList<>();
        SpongeGameTests.listen(LoadWorldEvent.class, event -> {
            if (event.world().key().equals(key)) {
                loads.add(new Loaded(event.hasLoadedBefore(), Properties.of(event.world().properties())));
            }
        });
        return loads;
    }

    // Changes each property to something the created world doesn't have, since it copies them from the default world
    private static void change(final ServerWorldProperties properties) {
        properties.setSpawnPosition(SpongeLevelDataGameTests.SPAWN);
        ((ServerLevelData) properties).setGameTime(SpongeLevelDataGameTests.GAME_TIME);
        properties.setDifficulty(
            properties.difficulty().equals(Difficulties.HARD.get()) ? Difficulties.EASY.get() : Difficulties.HARD.get()
        );
        properties.setCommands(!properties.commands());
        properties.setPvp(!properties.pvp());
        properties.setDisplayName(Component.text("gametest"));
        properties.offer(CustomData.KEY, "gametest");
    }

    // Asserts the world was created, then loaded again with what it was saved with
    private static void assertLoadedAgain(final GameTestHelper helper, final List<Loaded> loads,
                                          final Properties saved
    ) {
        GameTestAssertions.assertTrue(
            helper,
            saved.spawn().equals(SpongeLevelDataGameTests.SPAWN)
            && saved.gameTime() >= SpongeLevelDataGameTests.GAME_TIME
            && saved.customData().equals(Optional.of("gametest"))
            && saved.seed() == SpongeLevelDataGameTests.SEED,
            "The world was saved with " + saved + " rather than the changed properties"
        );
        GameTestAssertions.assertTrue(
            helper,
            loads.size() == 2 && !loads.get(0).before() && loads.get(1).before(),
            "The world was loaded " + loads + " rather than as a new world, then as one loaded before"
        );
        GameTestAssertions.assertTrue(
            helper,
            loads.get(1).properties().equals(saved),
            "The world was loaded again with " + loads.get(1).properties() + " rather than " + saved
        );
    }

    private static CompoundTag defaultWorld(final Path levelDat) {
        final CompoundTag data = SpongeLevelDataGameTests.read(levelDat).getCompoundOrEmpty("Data");
        final CompoundTag identity = new CompoundTag();
        identity.putString("LevelName", data.getStringOr("LevelName", ""));
        data.getCompound("spawn").ifPresent(spawn -> identity.put("spawn", spawn));
        return identity;
    }

    // Reads the player table of a registry.dat
    private static Set<UUID> players(final Path registry) {
        final CompoundTag players = SpongeLevelDataGameTests.read(registry)
            .getCompoundOrEmpty("data")
            .getCompoundOrEmpty("player_uuids");
        return players.keySet().stream()
            .flatMap(index -> players.read(index, UUIDUtil.CODEC).stream())
            .collect(Collectors.toSet());
    }

    private static CompoundTag read(final Path file) {
        try {
            return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Copies a file of the api-18 world fixture, which was made from what an api-18 1.21.11 game test run wrote
    private static void copyFixture(final String path, final Path target) {
        try (final InputStream fixture = SpongeLevelDataGameTests.class.getResourceAsStream(
            "/level_data/api-18/world/" + path
        )) {
            Files.createDirectories(target.getParent());
            Files.copy(Objects.requireNonNull(fixture, path), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> T finished(final GameTestHelper helper, final CompletableFuture<T> future) {
        GameTestAssertions.assertTrue(helper, future.isDone(), "The worlds have not finished loading and unloading");
        if (future.isCompletedExceptionally()) {
            GameTestAssertions.assertTrue(
                helper,
                false,
                "Loading or unloading a world failed: " + future.handle(($, error) -> error).join()
            );
        }
        return future.join();
    }

    private record Loaded(boolean before, Properties properties) {
    }

    private record Legacy(UUID uniqueId, long seed, boolean keepInventory, boolean raining, double border,
                          Optional<String> customData) {

        static Legacy of(final ServerWorld world) {
            final ServerLevel level = (ServerLevel) world;
            return new Legacy(
                world.uniqueId(),
                world.properties().worldGenerationConfig().seed(),
                level.getGameRules().get(GameRules.KEEP_INVENTORY),
                level.getWeatherData().isRaining(),
                level.getWorldBorder().getSize(),
                world.properties().get(CustomData.KEY)
            );
        }
    }

    private record Properties(UUID uniqueId, long seed, Vector3i spawn, long gameTime, Difficulty difficulty,
                              boolean commands, boolean pvp,
                              Optional<Component> displayName, Optional<String> customData) {

        static Properties of(final ServerWorldProperties properties) {
            return new Properties(
                properties.uniqueId(),
                properties.worldGenerationConfig().seed(),
                properties.spawnPosition(),
                ((LevelData) properties).getGameTime(),
                properties.difficulty(),
                properties.commands(),
                properties.pvp(),
                properties.displayName(),
                properties.get(CustomData.KEY)
            );
        }
    }

    // Plugins register custom data during RegisterDataEvent, which has passed by test time, so register it on first use
    private static final class CustomData {

        static final Key<Value<String>> KEY = Key.from(ResourceKey.sponge("gametest_level_data"), String.class);

        static {
            SpongeDataManager.INSTANCE.registerCustomDataRegistration(
                (SpongeDataRegistration) DataRegistration.of(CustomData.KEY, ServerWorldProperties.class)
            );
        }
    }
}
