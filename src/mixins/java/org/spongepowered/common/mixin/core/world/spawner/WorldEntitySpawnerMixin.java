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
package org.spongepowered.common.mixin.core.world.spawner;

import net.minecraft.entity.EntityClassification;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.spawner.WorldEntitySpawner_EntityDensityManagerAccessor;
import org.spongepowered.common.bridge.world.spawner.WorldEntitySpawner_EntityDensityManagerBridge;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;
import org.spongepowered.common.config.inheritable.SpawnerCategory;

@Mixin(WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin {

    // @formatter:off
    @Shadow @Final private static EntityClassification[] SPAWNING_CATEGORIES;
    @Shadow static public void spawnCategoryForChunk(EntityClassification p_234967_0_, ServerWorld p_234967_1_, Chunk p_234967_2_,
            WorldEntitySpawner.IDensityCheck p_234967_3_, WorldEntitySpawner.IOnSpawnDensityAdder p_234967_4_) {
    }
    // @formatter:on

    /**
     * @author morph - January 3rd, 2021 - Minecraft 1.16.4
     * @reason Use world configured spawn limits
     */
    @Overwrite
    public static void spawnForChunk(ServerWorld world, Chunk chunk, WorldEntitySpawner.EntityDensityManager manager, boolean spawnFriendlies, boolean spawnEnemies, boolean doMobSpawning) {
        world.getProfiler().push("spawner");

        for (final EntityClassification entityclassification : SPAWNING_CATEGORIES) {
            if ((spawnFriendlies || !entityclassification.isFriendly()) && (spawnEnemies || entityclassification.isFriendly()) && (doMobSpawning || !entityclassification.isPersistent()) && WorldEntitySpawnerMixin.impl$usePerWorldSpawnRules(manager, entityclassification, world)) {
                WorldEntitySpawnerMixin.spawnCategoryForChunk(entityclassification, world, chunk,
                        (p_234969_1_, p_234969_2_, p_234969_3_) -> ((WorldEntitySpawner_EntityDensityManagerAccessor) manager).invoker$canSpawn(p_234969_1_, p_234969_2_, p_234969_3_),
                        (p_234970_1_, p_234970_2_) -> ((WorldEntitySpawner_EntityDensityManagerAccessor) manager).invoker$afterSpawn(p_234970_1_, p_234970_2_)
                );
            }
        }

        world.getProfiler().pop();
    }

    private static boolean impl$usePerWorldSpawnRules(final WorldEntitySpawner.EntityDensityManager manager, final EntityClassification classification, final ServerWorld world) {
        final int tick = WorldEntitySpawnerMixin.impl$getSpawningTickRate(classification, world);
        if (tick == 0) {
            return false;
        }
        return world.getGameTime() % tick  == 0L && ((WorldEntitySpawner_EntityDensityManagerBridge) manager).bridge$canSpawnForCategoryInWorld(classification, world);
    }

    private static int impl$getSpawningTickRate(final EntityClassification classification, final ServerWorld world) {
        final SpawnerCategory.TickRatesSubCategory tickRates = ((ServerWorldInfoBridge) world.getLevelData()).bridge$configAdapter().get().spawner.tickRates;
        switch (classification) {
            case MONSTER:
                return tickRates.monster;
            case CREATURE:
                return tickRates.creature;
            case AMBIENT:
                return tickRates.ambient;
            case WATER_CREATURE:
                return tickRates.aquaticCreature;
            case WATER_AMBIENT:
                return tickRates.aquaticAmbient;
            default:
                throw new IllegalStateException("Unexpected value: " + classification);
        }
    }
}
