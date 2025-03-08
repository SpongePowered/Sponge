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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.world.level.NaturalSpawner_SpawnStateBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.SpawnerCategory;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @WrapOperation(method = "spawnForChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"))
    private static boolean impl$spawnTickRate(final NaturalSpawner.SpawnState state, final MobCategory category, final ChunkPos pos,
                                              final Operation<Boolean> original, @Local(argsOnly = true) final ServerLevel level) {
        final int tickRate = NaturalSpawnerMixin.impl$getSpawningTickRate(category, level);
        // Unknown category/use default
        if (tickRate == -1) {
            return original.call(state, category, pos);
        }
        // Turn off spawns
        if (tickRate == 0) {
            return false;
        }
        return level.getGameTime() % tickRate == 0L && ((NaturalSpawner_SpawnStateBridge) state).bridge$canSpawnForCategoryInWorld(category, level);
    }

    private static int impl$getSpawningTickRate(final MobCategory category, final ServerLevel level) {
        final SpawnerCategory.TickRatesSubCategory tickRates = SpongeGameConfigs.getForWorld(level).get().spawner.tickRates;
        return switch (category) {
            case MONSTER -> tickRates.monster;
            case CREATURE -> tickRates.creature;
            case AMBIENT -> tickRates.ambient;
            case UNDERGROUND_WATER_CREATURE -> tickRates.undergroundAquaticCreature;
            case WATER_CREATURE -> tickRates.aquaticCreature;
            case WATER_AMBIENT -> tickRates.aquaticAmbient;
            default -> -1;
        };
    }
}
