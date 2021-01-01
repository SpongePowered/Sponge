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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.spawner.EntityDensityManagerBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.SpawnerCategory;

@Mixin(value = WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin {

    @Redirect(method = "spawnForChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/spawner/WorldEntitySpawner$EntityDensityManager;access$100(Lnet/minecraft/world/spawner/WorldEntitySpawner$EntityDensityManager;Lnet/minecraft/entity/EntityClassification;)Z"))
    private static boolean impl$usePerWorldSpawnRules(final WorldEntitySpawner.EntityDensityManager manager, final EntityClassification p_234991_1_,
                                                      final ServerWorld p_234979_0_, final Chunk p_234979_1_, final WorldEntitySpawner.EntityDensityManager p_234979_2_,
                                                      final boolean p_234979_3_, final boolean p_234979_4_, final boolean p_234979_5_) {
        final int tick = impl$getSpawningTickRate(p_234991_1_, p_234979_0_);
        if (tick == 0) {
            return false;
        }
        return p_234979_0_.getGameTime() % tick  == 0L && ((EntityDensityManagerBridge) manager).bridge$canSpawnForCategoryInWorld(p_234991_1_, p_234979_0_);
    }

    private static int impl$getSpawningTickRate(final EntityClassification p_234991_1_, final ServerWorld world) {
        final SpawnerCategory.TickRatesSubCategory tickRates = SpongeGameConfigs.getForWorld(world).get().spawner.tickRates;
        switch (p_234991_1_) {
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
                throw new IllegalStateException("Unexpected value: " + p_234991_1_);
        }
    }
}
