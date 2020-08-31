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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.spawner.AbstractSpawnerBridge;

@Mixin(AbstractSpawner.class)
public abstract class AbstractSpawnerMixin implements AbstractSpawnerBridge {

    @Shadow private int spawnDelay;
    @Shadow private int minSpawnDelay;
    @Shadow private int maxSpawnDelay;
    @Shadow private int spawnCount;
    @Shadow private int maxNearbyEntities;
    @Shadow private int activatingRangeFromPlayer;
    @Shadow private int spawnRange;

    @Override
    public int bridge$getSpawnDelay() {
        return this.spawnDelay;
    }

    @Override
    public void bridge$setSpawnDelay(final int spawnDelay) {
        this.spawnDelay = spawnDelay;
    }

    @Override
    public int bridge$getMinSpawnDelay() {
        return this.minSpawnDelay;
    }

    @Override
    public int bridge$getMaxSpawnDelay() {
        return this.maxSpawnDelay;
    }

    @Override
    public int bridge$getSpawnCount() {
        return this.spawnCount;
    }

    @Override
    public int bridge$getMaxNearbyEntities() {
        return this.maxNearbyEntities;
    }

    @Override
    public void bridge$setMaxNearbyEntities(final int maxNearbyEntities) {
        this.maxNearbyEntities = maxNearbyEntities;
    }

    @Override
    public int bridge$getActivatingRangeFromPlayer() {
        return this.activatingRangeFromPlayer;
    }

    @Override
    public int bridge$getSpawnRange() {
        return this.spawnRange;
    }

    @Redirect(method = "isActivated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isPlayerWithin(DDDD)Z"))
    public boolean onIsPlayerWithin(World world, double x, double y, double z, double distance) {
        // Like vanilla but filter out players with !bridge$affectsSpawning
        for(PlayerEntity playerentity : world.getPlayers()) {
            if (EntityPredicates.NOT_SPECTATING.test(playerentity)
                  && EntityPredicates.IS_LIVING_ALIVE.test(playerentity)
                  && ((PlayerEntityBridge) playerentity).bridge$affectsSpawning()) {
                double d0 = playerentity.getDistanceSq(x, y, z);
                if (distance < 0.0D || d0 < distance * distance) {
                    return true;
                }
            }
        }

        return false;
    }
}
