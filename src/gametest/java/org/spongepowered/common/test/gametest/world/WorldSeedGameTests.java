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

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.generation.config.flat.FlatGeneratorConfigs;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldArchetype;
import org.spongepowered.api.world.server.WorldArchetypeType;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.mixin.gametest.StructureCheckAccessor_GameTest;
import org.spongepowered.common.mixin.gametest.StructureManagerAccessor_GameTest;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.concurrent.CompletableFuture;

/**
 * A world created with a seed in its {@link WorldGenerationConfig} reports, generates and locates structures with that
 * seed rather than the default world's, and keeps it when loaded again. The server loads each of its worlds on startup
 * with no generation config, from the world's dimension alone, so the seed it then has can only be the one the world
 * saved.
 */
@GameTestGroup("world_seed")
public final class WorldSeedGameTests {

    // Loading and unloading a world finish on Sponge's async scheduler, which keeps wall-clock time while the game test
    // server ticks as fast as it can: a budget for a slow machine, not how long it takes
    private static final int MAX_TICKS = 20 * 60 * 5;
    // Not the default world's: the game test server's has seed 0
    private static final long SEED = 0x5EED_CAFEL;

    @GameTest(maxTicks = 1)
    void defaultWorldUsesServerSeed(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        WorldSeedGameTests.assertSeed(helper, level.getSeed(), level.getServer().getWorldGenSettings().options().seed(), "The default world's seed");
        helper.succeed();
    }

    @GameTest(maxTicks = WorldSeedGameTests.MAX_TICKS)
    void createdWorldUsesItsSeed(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_created_seed");
        WorldSeedGameTests.succeedWithSeed(helper, WorldSeedGameTests.load(key, WorldSeedGameTests.archetype(WorldSeedGameTests.seeded()))
            .thenCompose(generation -> WorldSeedGameTests.unload(key).thenApply($ -> generation)));
    }

    @GameTest(maxTicks = WorldSeedGameTests.MAX_TICKS)
    void reloadedWorldKeepsItsSeed(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_reloaded_seed");
        WorldSeedGameTests.succeedWithSeed(helper, WorldSeedGameTests.load(key, WorldSeedGameTests.archetype(WorldSeedGameTests.seeded()))
            .thenCompose(created -> WorldSeedGameTests.unload(key))
            .thenCompose($ -> WorldSeedGameTests.load(key, WorldSeedGameTests.archetype(null)))
            .thenCompose(generation -> WorldSeedGameTests.unload(key).thenApply($ -> generation)));
    }

    // Chunk generation asks the world's structure manager whether to place structures. The world asks for the opposite
    // of the default world, which on the game test server places none
    @GameTest(maxTicks = WorldSeedGameTests.MAX_TICKS)
    void createdWorldKeepsItsStructureOption(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_structure_option");
        final boolean structures = !helper.getLevel().getServer().getWorldGenSettings().options().generateStructures();
        final WorldGenerationConfig config = WorldGenerationConfig.builder()
            .seed(WorldSeedGameTests.SEED)
            .generateStructures(structures)
            .build();
        final CompletableFuture<Generation> loaded = WorldSeedGameTests.load(key, WorldSeedGameTests.archetype(config))
            .thenCompose(generation -> WorldSeedGameTests.unload(key).thenApply($ -> generation));
        helper.succeedWhen(() -> {
            final Generation generation = WorldSeedGameTests.finished(helper, loaded);
            GameTestAssertions.assertTrue(helper, generation.generatesStructures() == structures, structures
                ? "The world places no structures though created with them"
                : "The world places structures though created without them");
        });
    }

    private static WorldGenerationConfig seeded() {
        return WorldGenerationConfig.builder().seed(WorldSeedGameTests.SEED).build();
    }

    // A superflat, so creating the world generates next to nothing
    private static WorldArchetype archetype(final @Nullable WorldGenerationConfig config) {
        final WorldArchetypeType type = WorldArchetypeType.of(WorldTypes.OVERWORLD.get(), ChunkGenerator.flat(FlatGeneratorConfigs.CLASSIC_FLAT.get()));
        return config == null ? WorldArchetype.of(type) : WorldArchetype.of(type, config);
    }

    private static CompletableFuture<Generation> load(final ResourceKey key, final WorldArchetype archetype) {
        return Sponge.server().worldManager().loadWorld(key, ServerWorldProperties.LoadOptions.create(archetype))
            .thenApply(world -> world.map(Generation::of).orElseThrow(() -> new IllegalStateException(key + " was not loaded")));
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

    private static void succeedWithSeed(final GameTestHelper helper, final CompletableFuture<Generation> loaded) {
        helper.succeedWhen(() -> {
            final Generation generation = WorldSeedGameTests.finished(helper, loaded);
            WorldSeedGameTests.assertSeed(helper, generation.world(), WorldSeedGameTests.SEED, "The world's seed");
            WorldSeedGameTests.assertSeed(helper, generation.chunks(), WorldSeedGameTests.SEED, "The seed the world generates with");
            WorldSeedGameTests.assertSeed(helper, generation.structures(), WorldSeedGameTests.SEED, "The seed the world locates structures with");
        });
    }

    private static Generation finished(final GameTestHelper helper, final CompletableFuture<Generation> loaded) {
        GameTestAssertions.assertTrue(helper, loaded.isDone(), "The world has not finished loading and unloading");
        if (loaded.isCompletedExceptionally()) {
            GameTestAssertions.assertTrue(helper, false, "Loading or unloading the world failed: " + loaded.handle(($, error) -> error).join());
        }
        return loaded.join();
    }

    // Rather than assertValueEqual, whose message names the actual value as the expected one
    private static void assertSeed(final GameTestHelper helper, final long seed, final long expected, final String name) {
        GameTestAssertions.assertTrue(helper, seed == expected, name + " was " + seed + " rather than " + expected);
    }

    /**
     * How a world generates, as it was created: the seed it reports, the one its chunk generator took, which places
     * its structures and seeds its noise, the one its structure check took, which {@code /locate} and explorer maps
     * search with, and whether it places structures at all
     */
    private record Generation(long world, long chunks, long structures, boolean generatesStructures) {

        static Generation of(final ServerWorld world) {
            final ServerLevel level = (ServerLevel) world;
            final StructureCheck structureCheck = ((StructureManagerAccessor_GameTest) level.structureManager()).accessor$structureCheck();
            return new Generation(world.seed(), level.getChunkSource().getGeneratorState().getLevelSeed(),
                ((StructureCheckAccessor_GameTest) structureCheck).accessor$seed(), level.structureManager().shouldGenerateStructures());
        }
    }
}
