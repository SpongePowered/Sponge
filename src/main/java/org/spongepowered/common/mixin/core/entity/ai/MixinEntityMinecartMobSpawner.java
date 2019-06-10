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
package org.spongepowered.common.mixin.core.entity.ai;

import net.minecraft.entity.item.EntityMinecartMobSpawner;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.entity.vehicle.minecart.MobSpawnerMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.mutable.SpongeMobSpawnerData;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.interfaces.IMixinMobSpawner;
import org.spongepowered.common.mixin.core.entity.item.MixinEntityMinecart_Impl;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(EntityMinecartMobSpawner.class)
public abstract class MixinEntityMinecartMobSpawner extends MixinEntityMinecart_Impl implements MobSpawnerMinecart, IMixinMobSpawner {

    @Shadow private MobSpawnerBaseLogic mobSpawnerLogic;

    @Override
    public MobSpawnerBaseLogic getLogic() {
        return this.mobSpawnerLogic;
    }

    @Override
    public MobSpawnerData getSpawnerData() {
        return new SpongeMobSpawnerData(
                (short) getLogic().spawnDelay,
                (short) getLogic().minSpawnDelay,
                (short) getLogic().maxSpawnDelay,
                (short) getLogic().spawnCount,
                (short) getLogic().maxNearbyEntities,
                (short) getLogic().activatingRangeFromPlayer,
                (short) getLogic().spawnRange,
                SpawnerUtils.getNextEntity(getLogic()),
                SpawnerUtils.getEntities(getLogic()));
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getSpawnerData());
    }
}
