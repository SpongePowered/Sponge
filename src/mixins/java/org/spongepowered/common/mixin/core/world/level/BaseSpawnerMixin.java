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

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.bridge.world.level.BaseSpawnerBridge;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin implements BaseSpawnerBridge {

    // @formatter:off
    @Shadow private int spawnDelay;
    @Shadow private int minSpawnDelay;
    @Shadow private int maxSpawnDelay;
    @Shadow private int spawnCount;
    @Shadow private int maxNearbyEntities;
    @Shadow private int requiredPlayerRange;
    @Shadow private int spawnRange;
    // @formatter:on

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
        return this.requiredPlayerRange;
    }

    @Override
    public int bridge$getSpawnRange() {
        return this.spawnRange;
    }

    @Redirect(method = "isNearPlayer",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;hasNearbyAlivePlayer(DDDD)Z"))
    public boolean impl$checkPlayerSpawningStateForActivation(final Level world, final double x, final double y, final double z, final double distance) {
        // Like vanilla but filter out players with !bridge$affectsSpawning
        for (final Player playerentity : world.players()) {
            if (EntitySelector.NO_SPECTATORS.test(playerentity)
                  && EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(playerentity)
                  && ((PlayerBridge) playerentity).bridge$affectsSpawning()) {
                final double d0 = playerentity.distanceToSqr(x, y, z);
                if (distance < 0.0D || d0 < distance * distance) {
                    return true;
                }
            }
        }

        return false;
    }
}
