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
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldArchetype;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.Optional;
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
        // Reuse the already-loaded overworld's archetype type. A freshly built ChunkGenerator.flat(...)
        // uses entries from the global registry and fails to encode against the gametest server's
        // registry set on Forge. Spawn logic is disabled so the test does not generate a full spawn
        // area while it only needs the world to be created, saved, unloaded and loaded again.
        // api-19 writes no level.dat for a world other than the default one, so a load-only option finds nothing
        // to load: the reload passes the archetype too, and the id must come back from the world's saved data.
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
            .thenCompose(first -> manager.loadWorld(key, create)
                .thenApply(Optional::orElseThrow)
                .thenCompose(reloaded -> manager.unloadWorld(key)
                    .thenApply($ -> new UUID[]{first, reloaded.uniqueId()})));

        helper.succeedWhen(() -> {
            GameTestAssertions.assertTrue(helper, ids.isDone(), "The world has not finished loading and unloading");
            if (ids.isCompletedExceptionally()) {
                GameTestAssertions.assertTrue(helper, false, "Loading or unloading the world failed: " + ids.handle(($, error) -> error).join());
            }
            final UUID[] pair = ids.join();
            GameTestAssertions.assertTrue(helper, pair[0].equals(pair[1]),
                "The reloaded world's id was " + pair[1] + " rather than " + pair[0]);
        });
    }
}
