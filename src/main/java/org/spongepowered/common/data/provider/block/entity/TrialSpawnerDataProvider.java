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

import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.level.block.entity.trialspawner.TrialSpawnerAccessor;
import org.spongepowered.common.accessor.world.level.block.entity.trialspawner.TrialSpawnerDataAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Optional;

public final class TrialSpawnerDataProvider {

    private TrialSpawnerDataProvider() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(TrialSpawnerBlockEntity.class)
                    .create(Keys.MAX_NEARBY_ENTITIES)
                        .get(h -> h.getTrialSpawner().getConfig().calculateTargetSimultaneousMobs(h.getTrialSpawner().getData().countAdditionalPlayers(h.getBlockPos())))
                    .create(Keys.NEXT_ENTITY_TO_SPAWN)
                        .get(h -> ((TrialSpawnerDataAccessor) h.getTrialSpawner().getData()).accessor$nextSpawnData().map(EntityUtil::toArchetype).orElse(null))
                        .set((h, v) -> ((TrialSpawnerDataAccessor) h.getTrialSpawner().getData()).accessor$nextSpawnData(Optional.of(EntityUtil.toSpawnData(v))))
                        .delete(h -> ((TrialSpawnerDataAccessor) h.getTrialSpawner().getData()).accessor$nextSpawnData(Optional.empty()))
                    .create(Keys.REMAINING_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((TrialSpawnerDataAccessor) h.getTrialSpawner().getData()).accessor$nextMobSpawnsAt() - h.getLevel().getGameTime()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            ((TrialSpawnerDataAccessor) h.getTrialSpawner().getData()).accessor$nextMobSpawnsAt(h.getLevel().getGameTime() + v.ticks());
                            return true;
                        })
                    .create(Keys.SPAWN_RANGE)
                        .get(h -> (double) h.getTrialSpawner().getConfig().spawnRange())
                        .set((h, v) -> {
                            final var normalConfig = h.getTrialSpawner().getNormalConfig();
                            final var ominousConfig = h.getTrialSpawner().getOminousConfig();
                            final var newNormalConfig = new TrialSpawnerConfig(v.intValue(), normalConfig.totalMobs(), normalConfig.simultaneousMobs(), normalConfig.totalMobsAddedPerPlayer(), normalConfig.simultaneousMobsAddedPerPlayer(), normalConfig.ticksBetweenSpawn(), normalConfig.spawnPotentialsDefinition(), normalConfig.lootTablesToEject(), normalConfig.itemsToDropWhenOminous());
                            final var newOminiousConfig = new TrialSpawnerConfig(v.intValue(), ominousConfig.totalMobs(), ominousConfig.simultaneousMobs(), ominousConfig.totalMobsAddedPerPlayer(), ominousConfig.simultaneousMobsAddedPerPlayer(), ominousConfig.ticksBetweenSpawn(), ominousConfig.spawnPotentialsDefinition(), ominousConfig.lootTablesToEject(), ominousConfig.itemsToDropWhenOminous());

                            final var accessor = (TrialSpawnerAccessor) (Object) h.getTrialSpawner();
                            accessor.accessor$normalConfig(newNormalConfig);
                            accessor.accessor$ominousConfig(newOminiousConfig);
                        })
                    // TODO totalMobs
                    // TODO simultaneousMobs
                    // TODO totalMobsAddedPerPlayer
                    // TODO simultaneousMobsAddedPerPlayer
                    .create(Keys.MAX_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(h.getTrialSpawner().getConfig().ticksBetweenSpawn()))
                    // TODO set ticksBetweenSpawn
                    .create(Keys.MIN_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(h.getTrialSpawner().getConfig().ticksBetweenSpawn()))
                    .create(Keys.SPAWNABLE_ENTITIES)
                        .get(h -> EntityUtil.toWeightedArchetypes(h.getTrialSpawner().getConfig().spawnPotentialsDefinition()))
                        .set((h, v) -> {
                            final var spawnPotentials = EntityUtil.toSpawnPotentials(v);

                            final var normalConfig = h.getTrialSpawner().getNormalConfig();
                            final var ominousConfig = h.getTrialSpawner().getOminousConfig();
                            final var newNormalConfig = new TrialSpawnerConfig(normalConfig.spawnRange(), normalConfig.totalMobs(), normalConfig.simultaneousMobs(), normalConfig.totalMobsAddedPerPlayer(), normalConfig.simultaneousMobsAddedPerPlayer(), normalConfig.ticksBetweenSpawn(), spawnPotentials, normalConfig.lootTablesToEject(), normalConfig.itemsToDropWhenOminous());
                            final var newOminiousConfig = new TrialSpawnerConfig(ominousConfig.spawnRange(), ominousConfig.totalMobs(), ominousConfig.simultaneousMobs(), ominousConfig.totalMobsAddedPerPlayer(), ominousConfig.simultaneousMobsAddedPerPlayer(), ominousConfig.ticksBetweenSpawn(), spawnPotentials, ominousConfig.lootTablesToEject(), ominousConfig.itemsToDropWhenOminous());

                            final var accessor = (TrialSpawnerAccessor) (Object) h.getTrialSpawner();
                            accessor.accessor$normalConfig(newNormalConfig);
                            accessor.accessor$ominousConfig(newOminiousConfig);
                        });
                    // TODO lootTablesToEject
                    // TODO itemsToDropWhenOminous
    }
    // @formatter:on

}
