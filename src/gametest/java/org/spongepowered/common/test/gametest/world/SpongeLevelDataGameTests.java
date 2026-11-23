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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.config.flat.FlatGeneratorConfigs;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A world keeps its {@link org.spongepowered.api.world.server.ServerWorld#uniqueId() unique id} when it is saved and
 * loaded again. The id is stored in the world's {@code level.dat}, next to its vanilla level data, so saving and then
 * loading the world exercises both the write and the read of Sponge's level data.
 */
@GameTestGroup("level_data")
public final class SpongeLevelDataGameTests {

    // Loading and unloading finish on Sponge's async scheduler, which keeps wall-clock time while the game test server
    // ticks as fast as it can: a budget for a slow machine, not how long it takes
    private static final int MAX_TICKS = 20 * 60 * 5;

    @GameTest(maxTicks = SpongeLevelDataGameTests.MAX_TICKS)
    void worldKeepsItsIdAfterReload(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_level_data");
        final WorldManager worldManager = Sponge.server().worldManager();
        // A superflat world, so creating and saving it does next to nothing
        final WorldTemplate template = WorldTemplate.builder()
            .key(key)
            .add(Keys.WORLD_TYPE, WorldTypes.OVERWORLD.get())
            .add(Keys.CHUNK_GENERATOR, ChunkGenerator.flat(FlatGeneratorConfigs.CLASSIC_FLAT.get()))
            .add(Keys.SERIALIZATION_BEHAVIOR, SerializationBehavior.AUTOMATIC)
            .add(Keys.IS_LOAD_ON_STARTUP, false)
            .build();

        final CompletableFuture<UUID[]> ids = worldManager.loadWorld(template)
            .<UUID>thenCompose(world -> worldManager.unloadWorld(key).thenApply($ -> world.uniqueId()))
            .thenCompose(first -> worldManager.loadWorld(key)
                .<UUID>thenCompose(world -> worldManager.unloadWorld(key).thenApply($ -> world.uniqueId()))
                .thenApply(second -> new UUID[]{first, second}));

        helper.succeedWhen(() -> {
            helper.assertTrue(ids.isDone(), "The world has not finished loading and unloading");
            if (ids.isCompletedExceptionally()) {
                helper.assertTrue(false, "Loading or unloading the world failed: " + ids.handle(($, error) -> error).join());
            }
            final UUID[] result = ids.join();
            helper.assertTrue(result[0].equals(result[1]),
                "The world's unique id changed from " + result[0] + " to " + result[1] + " when it was loaded again");
        });
    }
}
