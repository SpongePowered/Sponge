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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.entity;

import org.spongepowered.api.block.entity.MobSpawner;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.BaseSpawnerBridge;

import java.util.Set;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

@Mixin(SpawnerBlockEntity.class)
public abstract class SpawnerBlockEntityMixin_API extends BlockEntityMixin_API implements MobSpawner {

    // @formatter:off
    @Shadow public abstract BaseSpawner shadow$getSpawner();
    // @formatter:on

    @Override
    public void spawnEntityBatchImmediately(final boolean force) {
        final BaseSpawnerBridge bridge = ((BaseSpawnerBridge) this.shadow$getSpawner());

        if (force) {
            final short oldMaxNearby = (short) bridge.bridge$getMaxNearbyEntities();
            bridge.bridge$setMaxNearbyEntities(Short.MAX_VALUE);

            bridge.bridge$setSpawnDelay(0);
            this.shadow$getSpawner().tick();

            bridge.bridge$setMaxNearbyEntities(oldMaxNearby);
        } else {
            bridge.bridge$setSpawnDelay(0);
        }
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // Spawner
        values.add(this.remainingDelay().asImmutable());
        values.add(this.minimumSpawnDelay().asImmutable());
        values.add(this.maximumSpawnDelay().asImmutable());
        values.add(this.spawnCount().asImmutable());
        values.add(this.maximumNearbyEntities().asImmutable());
        values.add(this.requiredPlayerRange().asImmutable());
        values.add(this.spawnRange().asImmutable());
        values.add(this.nextEntityToSpawn().asImmutable());
        values.add(this.possibleEntitiesToSpawn().asImmutable());

        return values;
    }

}
