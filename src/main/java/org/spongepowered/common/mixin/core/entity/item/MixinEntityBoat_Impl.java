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
package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.mixin.api.minecraft.entity.MixinEntity_API;
import org.spongepowered.common.mixin.core.entity.MixinEntity_Impl;

import java.util.List;

@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat_Impl extends MixinEntity_Impl {

    private double maxSpeed = 0.35D;
    private boolean moveOnLand = false;
    private double occupiedDecelerationSpeed = 0D;
    private double unoccupiedDecelerationSpeed = 0.8D;


    @Override
    public boolean isInWater() {
        return !this.onGround;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey(NbtDataUtil.BOAT_MAX_SPEED)) {
            this.maxSpeed = compound.getDouble(NbtDataUtil.BOAT_MAX_SPEED);
        }
        if (compound.hasKey(NbtDataUtil.BOAT_MOVE_ON_LAND)) {
            this.moveOnLand = compound.getBoolean(NbtDataUtil.BOAT_MOVE_ON_LAND);
        }
        if (compound.hasKey(NbtDataUtil.BOAT_OCCUPIED_DECELERATION_SPEED)) {
            this.occupiedDecelerationSpeed = compound.getDouble(NbtDataUtil.BOAT_OCCUPIED_DECELERATION_SPEED);
        }
        if (compound.hasKey(NbtDataUtil.BOAT_UNOCCUPIED_DECELERATION_SPEED)) {
            this.unoccupiedDecelerationSpeed = compound.getDouble(NbtDataUtil.BOAT_UNOCCUPIED_DECELERATION_SPEED);
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setDouble(NbtDataUtil.BOAT_MAX_SPEED, this.maxSpeed);
        compound.setBoolean(NbtDataUtil.BOAT_MOVE_ON_LAND, this.moveOnLand);
        compound.setDouble(NbtDataUtil.BOAT_OCCUPIED_DECELERATION_SPEED, this.occupiedDecelerationSpeed);
        compound.setDouble(NbtDataUtil.BOAT_UNOCCUPIED_DECELERATION_SPEED, this.unoccupiedDecelerationSpeed);
    }
}
