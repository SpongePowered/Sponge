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
package org.spongepowered.common.mixin.api.mcp.entity.item;

import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.entity.vehicle.minecart.MobSpawnerMinecart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.tileentity.MobSpawnerBaseLogicBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeMobSpawnerData;
import org.spongepowered.common.data.processor.common.SpawnerUtils;

import java.util.Collection;
import net.minecraft.entity.item.minecart.SpawnerMinecartEntity;
import net.minecraft.world.spawner.AbstractSpawner;

@Mixin(SpawnerMinecartEntity.class)
public abstract class EntityMinecartMobSpawnerMixin_API extends EntityMinecartMixin_API implements MobSpawnerMinecart {

    @Shadow @Final private AbstractSpawner mobSpawnerLogic;

    @Override
    public MobSpawnerData getSpawnerData() {
        // TODO - Update once Mixin 0.8 for accessors
//        final MobSpawnerBaseLogicAccessor accessor = (MobSpawnerBaseLogicAccessor) this.mobSpawnerLogic;
//        return new SpongeMobSpawnerData(
//                (short) accessor.accessor$getSpawnDelay(),
//                (short) accessor.accessor$getMinSpawnDelay(),
//                (short) accessor.accessor$getMaxSpawnDelay(),
//                (short) accessor.accessor$getSpawnCount(),
//                (short) accessor.accessor$getMaxNearbyEntities(),
//                (short) accessor.accessor$getActivatingRangeFromPlayer(),
//                (short) accessor.accessor$getSpawnRange(),
//                SpawnerUtils.getNextEntity(accessor),
//                SpawnerUtils.getEntities(this.mobSpawnerLogic));
        final MobSpawnerBaseLogicBridge accessor = (MobSpawnerBaseLogicBridge) this.mobSpawnerLogic;
        return new SpongeMobSpawnerData(
            (short) accessor.bridge$getSpawnDelay(),
            (short) accessor.bridge$getMinSpawnDelay(),
            (short) accessor.bridge$getMaxSpawnDelay(),
            (short) accessor.bridge$getSpawnCount(),
            (short) accessor.bridge$getMaxNearbyEntities(),
            (short) accessor.bridge$getActivatingRangeFromPlayer(),
            (short) accessor.bridge$getSpawnRange(),
            SpawnerUtils.getNextEntity(accessor),
            SpawnerUtils.getEntities(this.mobSpawnerLogic));
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(final Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(getSpawnerData());
    }
}
