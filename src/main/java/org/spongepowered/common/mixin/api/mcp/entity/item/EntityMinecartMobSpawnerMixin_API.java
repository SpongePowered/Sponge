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

import net.minecraft.entity.item.EntityMinecartMobSpawner;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.entity.vehicle.minecart.MobSpawnerMinecart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.SpongeMobSpawnerData;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.mixin.core.tileentity.MobSpawnerBaseLogicAccessor;

import java.util.Collection;

@Mixin(EntityMinecartMobSpawner.class)
public abstract class EntityMinecartMobSpawnerMixin_API extends EntityMinecartMixin_API implements MobSpawnerMinecart {

    @Shadow @Final private MobSpawnerBaseLogic mobSpawnerLogic;

    @Override
    public MobSpawnerData getSpawnerData() {
        final MobSpawnerBaseLogicAccessor accessor = (MobSpawnerBaseLogicAccessor) this.mobSpawnerLogic;
        return new SpongeMobSpawnerData(
            (short) accessor.accessor$getSpawnDelay(),
            (short) accessor.accessor$getMinSpawnDelay(),
            (short) accessor.accessor$getMaxSpawnDelay(),
            (short) accessor.accessor$getSpawnCount(),
            (short) accessor.accessor$getMaxNearbyEntities(),
            (short) accessor.accessor$getActivatingRangeFromPlayer(),
            (short) accessor.accessor$getSpawnRange(),
            SpawnerUtils.getNextEntity(accessor),
            SpawnerUtils.getEntities(this.mobSpawnerLogic));
    }

    @Override
    protected void spongeApi$supplyVanillaManipulators(final Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(this.getSpawnerData());
    }
}
