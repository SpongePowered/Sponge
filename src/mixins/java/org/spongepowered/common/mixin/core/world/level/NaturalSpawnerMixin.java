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
package org.spongepowered.common.mixin.core.world.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.NaturalSpawner_SpawnStateAccessor;
import org.spongepowered.common.bridge.world.level.NaturalSpawner_SpawnStateBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.config.inheritable.SpawnerCategory;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    // @formatter:off
    @Shadow @Final private static MobCategory[] SPAWNING_CATEGORIES;
    @Shadow static public void spawnCategoryForChunk(MobCategory p_234967_0_, ServerLevel p_234967_1_, LevelChunk p_234967_2_,
            NaturalSpawner.SpawnPredicate p_234967_3_, NaturalSpawner.AfterSpawnCallback p_234967_4_) {
    }
    // @formatter:on

    /**
     * @author morph - January 3rd, 2021 - Minecraft 1.16.4
     * @reason Use world configured spawn limits
     */
    @Overwrite
    public static void spawnForChunk(ServerLevel world, LevelChunk chunk, NaturalSpawner.SpawnState manager, boolean spawnFriendlies, boolean spawnEnemies, boolean doMobSpawning) {
        world.getProfiler().push("spawner");

        for (final MobCategory entityclassification : SPAWNING_CATEGORIES) {
            if ((spawnFriendlies || !entityclassification.isFriendly()) && (spawnEnemies || entityclassification.isFriendly()) && (doMobSpawning || !entityclassification.isPersistent()) && NaturalSpawnerMixin.impl$usePerWorldSpawnRules(manager, entityclassification, world)) {
                NaturalSpawnerMixin.spawnCategoryForChunk(entityclassification, world, chunk,
                        (p_234969_1_, p_234969_2_, p_234969_3_) -> ((NaturalSpawner_SpawnStateAccessor) manager).invoker$canSpawn(p_234969_1_, p_234969_2_, p_234969_3_),
                        (p_234970_1_, p_234970_2_) -> ((NaturalSpawner_SpawnStateAccessor) manager).invoker$afterSpawn(p_234970_1_, p_234970_2_)
                );
            }
        }

        world.getProfiler().pop();
    }

    private static boolean impl$usePerWorldSpawnRules(final NaturalSpawner.SpawnState manager, final MobCategory classification, final ServerLevel world) {
        final int tick = NaturalSpawnerMixin.impl$getSpawningTickRate(classification, world);
        if (tick == 0) {
            return false;
        }
        return world.getGameTime() % tick  == 0L && ((NaturalSpawner_SpawnStateBridge) manager).bridge$canSpawnForCategoryInWorld(classification, world);
    }

    private static int impl$getSpawningTickRate(final MobCategory classification, final ServerLevel world) {
        final SpawnerCategory.TickRatesSubCategory tickRates = ((PrimaryLevelDataBridge) world.getLevelData()).bridge$configAdapter().get().spawner.tickRates;
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
