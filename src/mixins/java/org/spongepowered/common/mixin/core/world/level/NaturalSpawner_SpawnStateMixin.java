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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.NaturalSpawnerAccessor;
import org.spongepowered.common.bridge.world.level.NaturalSpawner_SpawnStateBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.SpawnerCategory;

@Mixin(NaturalSpawner.SpawnState.class)
public abstract class NaturalSpawner_SpawnStateMixin implements NaturalSpawner_SpawnStateBridge {

    // @formatter:off
    @Shadow @Final private int spawnableChunkCount;
    @Shadow @Final private Object2IntOpenHashMap<MobCategory> mobCategoryCounts;
    // @formatter:on

    @Override
    public boolean bridge$canSpawnForCategoryInWorld(final MobCategory classification, final ServerLevel world) {
        final SpawnerCategory.SpawnLimitsSubCategory spawnLimits = SpongeGameConfigs.getForWorld(world).get().spawner.spawnLimits;
        final int maxInstancesPerChunk;
        switch (classification) {
            case MONSTER:
                maxInstancesPerChunk = spawnLimits.monster;
                break;
            case CREATURE:
                maxInstancesPerChunk = spawnLimits.creature;
                break;
            case AMBIENT:
                maxInstancesPerChunk = spawnLimits.ambient;
                break;
            case UNDERGROUND_WATER_CREATURE:
                maxInstancesPerChunk = spawnLimits.undergroundAquaticCreature;
                break;
            case WATER_CREATURE:
                maxInstancesPerChunk = spawnLimits.aquaticCreature;
                break;
            case WATER_AMBIENT:
                maxInstancesPerChunk = spawnLimits.aquaticAmbient;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + classification);
        }
        final int i = maxInstancesPerChunk * this.spawnableChunkCount / NaturalSpawnerAccessor.accessor$MAGIC_NUMBER();
        return this.mobCategoryCounts.getInt(classification) < i;
    }
}
