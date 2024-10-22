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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.accessor.world.level.NaturalSpawner_SpawnStateAccessor;
import org.spongepowered.common.bridge.world.level.NaturalSpawner_SpawnStateBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.config.inheritable.SpawnerCategory;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @Redirect(method = "spawnForChunk", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private static boolean impl$canSpawnForCategoryLocal(final NaturalSpawner.SpawnState instance, final MobCategory $$0, final ChunkPos $$1, final ServerLevel level) {
        return NaturalSpawnerMixin.impl$canSpawnInLevel(instance, $$0, level, $$1);
    }

    private static boolean impl$canSpawnInLevel(final NaturalSpawner.SpawnState spawnState, final MobCategory classification, final ServerLevel level, final ChunkPos chunkPos) {
        final int tick = NaturalSpawnerMixin.impl$getSpawningTickRate(classification, level);
        // Unknown category/use default
        if (tick == -1) {
            return ((NaturalSpawner_SpawnStateAccessor) spawnState).invoker$canSpawnForCategoryLocal(classification, chunkPos);
        }
        // Turn off spawns
        if (tick == 0) {
            return false;
        }
        return level.getGameTime() % tick  == 0L && ((NaturalSpawner_SpawnStateBridge) spawnState).bridge$canSpawnForCategoryInWorld(classification, level);
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
            case UNDERGROUND_WATER_CREATURE:
                return tickRates.undergroundAquaticCreature;
            case WATER_CREATURE:
                return tickRates.aquaticCreature;
            case WATER_AMBIENT:
                return tickRates.aquaticAmbient;
            default:
                return -1;
        }
    }
}
