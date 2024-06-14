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
package org.spongepowered.common.data.provider.block.entity;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.level.BaseSpawnerAccessor;
import org.spongepowered.common.accessor.world.level.block.entity.SpawnerBlockEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.SpongeTicks;

public final class MobSpawnerData {

    private MobSpawnerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(SpawnerBlockEntityAccessor.class)
                    .create(Keys.MAX_NEARBY_ENTITIES)
                        .get(h -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$maxNearbyEntities())
                        .set((h, v) -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$maxNearbyEntities(v))
                    .create(Keys.MAX_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((BaseSpawnerAccessor) h.accessor$spawner()).accessor$maxSpawnDelay()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$maxSpawnDelay(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.MIN_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((BaseSpawnerAccessor) h.accessor$spawner()).accessor$minSpawnDelay()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$minSpawnDelay(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.NEXT_ENTITY_TO_SPAWN)
                        .get(h -> EntityUtil.toArchetype(((BaseSpawnerAccessor) h.accessor$spawner()).accessor$nextSpawnData()))
                        .set((h, v) -> ((BaseSpawnerAccessor)h.accessor$spawner()).accessor$nextSpawnData(EntityUtil.toSpawnData(v)))
                    .create(Keys.REMAINING_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnDelay()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnDelay(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.REQUIRED_PLAYER_RANGE)
                        .get(h -> (double) ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$requiredPlayerRange())
                        .set((h, v) -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$requiredPlayerRange(v.intValue()))
                    .create(Keys.SPAWN_COUNT)
                        .get(h -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnCount())
                        .set((h, v) -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnCount(v))
                    .create(Keys.SPAWN_RANGE)
                        .get(h -> (double) ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnRange())
                        .set((h, v) -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnRange(v.intValue()))
                    .create(Keys.SPAWNABLE_ENTITIES)
                        .get(h -> EntityUtil.toWeightedArchetypes(((BaseSpawnerAccessor)h.accessor$spawner()).accessor$spawnPotentials()))
                        .set((h, v) -> {
                            final BaseSpawnerAccessor logic = (BaseSpawnerAccessor) h.accessor$spawner();
                            logic.accessor$spawnPotentials(EntityUtil.toSpawnPotentials(v));
                            logic.accessor$nextSpawnData(null); // Reset next entity spawn
                        });
    }
    // @formatter:on

}
