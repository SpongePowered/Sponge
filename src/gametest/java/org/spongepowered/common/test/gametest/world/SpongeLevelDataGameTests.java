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
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.config.flat.FlatGeneratorConfigs;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A world's {@link org.spongepowered.api.world.server.ServerWorld#uniqueId() unique id} is stored in its level data on
 * save. Create a fresh world, remember its id, unload it, load it again, and compare the ids: this exercises the write
 * and read paths together with the on-disk layout they depend on, rather than inspecting {@code level.dat} directly.
 */
@GameTestGroup("level_data")
public final class SpongeLevelDataGameTests {

    @GameTest(maxTicks = 20 * 60 * 5)
    void reloadedWorldKeepsItsId(final GameTestHelper helper) {
        final ResourceKey key = ResourceKey.sponge("gametest_reloaded_id");
        final WorldManager manager = Sponge.server().worldManager();
        final WorldTemplate template = WorldTemplate.overworld().asBuilder()
            .key(key)
            .add(Keys.CHUNK_GENERATOR, ChunkGenerator.flat(FlatGeneratorConfigs.CLASSIC_FLAT.get()))
            .build();

        final CompletableFuture<UUID[]> ids = manager.loadWorld(template)
            .thenCompose(created -> manager.unloadWorld(key).thenApply($ -> created.uniqueId()))
            .thenCompose(first -> manager.loadWorld(key)
                .thenCompose(reloaded -> manager.unloadWorld(key)
                    .thenApply($ -> new UUID[]{first, reloaded.uniqueId()})));

        helper.succeedWhen(() -> {
            helper.assertTrue(ids.isDone(), "The world has not finished loading and unloading");
            final UUID[] pair = ids.join();
            helper.assertTrue(pair[0].equals(pair[1]),
                "The reloaded world's id was " + pair[1] + " rather than " + pair[0]);
        });
    }
}
