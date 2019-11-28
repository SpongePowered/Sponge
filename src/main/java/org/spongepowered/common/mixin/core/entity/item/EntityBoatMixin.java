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

import net.minecraft.entity.item.BoatEntity;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

@Mixin(BoatEntity.class)
public abstract class EntityBoatMixin extends EntityMixin {

    private double maxSpeed = 0.35D;
    private boolean moveOnLand = false;
    private double occupiedDecelerationSpeed = 0D;
    private double unoccupiedDecelerationSpeed = 0.8D;

    @Override
    public void spongeImpl$readFromSpongeCompound(CompoundNBT compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.func_74764_b(Constants.Entity.Boat.BOAT_MAX_SPEED)) {
            this.maxSpeed = compound.func_74769_h(Constants.Entity.Boat.BOAT_MAX_SPEED);
        }
        if (compound.func_74764_b(Constants.Entity.Boat.BOAT_MOVE_ON_LAND)) {
            this.moveOnLand = compound.func_74767_n(Constants.Entity.Boat.BOAT_MOVE_ON_LAND);
        }
        if (compound.func_74764_b(Constants.Entity.Boat.BOAT_OCCUPIED_DECELERATION_SPEED)) {
            this.occupiedDecelerationSpeed = compound.func_74769_h(Constants.Entity.Boat.BOAT_OCCUPIED_DECELERATION_SPEED);
        }
        if (compound.func_74764_b(Constants.Entity.Boat.BOAT_UNOCCUPIED_DECELERATION_SPEED)) {
            this.unoccupiedDecelerationSpeed = compound.func_74769_h(Constants.Entity.Boat.BOAT_UNOCCUPIED_DECELERATION_SPEED);
        }
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(CompoundNBT compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        compound.func_74780_a(Constants.Entity.Boat.BOAT_MAX_SPEED, this.maxSpeed);
        compound.func_74757_a(Constants.Entity.Boat.BOAT_MOVE_ON_LAND, this.moveOnLand);
        compound.func_74780_a(Constants.Entity.Boat.BOAT_OCCUPIED_DECELERATION_SPEED, this.occupiedDecelerationSpeed);
        compound.func_74780_a(Constants.Entity.Boat.BOAT_UNOCCUPIED_DECELERATION_SPEED, this.unoccupiedDecelerationSpeed);
    }
}
