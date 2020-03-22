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

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends EntityMixin {

    private float impl$maxSpeed = Constants.Entity.Boat.DEFAULT_MAX_SPEED;
    private boolean impl$moveOnLand = Constants.Entity.Boat.MOVE_ON_LAND;
    private double impl$occupiedDecelerationSpeed = Constants.Entity.Boat.OCCUPIED_DECELERATION_SPEED;
    private double impl$unoccupiedDecelerationSpeed = Constants.Entity.Boat.UNOCCUPIED_DECELERATION_SPEED;

    @ModifyConstant(method = "updateMotion", constant = @Constant(floatValue = 0.9f))
    private float impl$getMaximumWaterMotion() {
        return this.impl$maxSpeed;
    }

    @Override
    public void impl$readFromSpongeCompound(final CompoundNBT compound) {
        super.impl$readFromSpongeCompound(compound);
        if (compound.contains(Constants.Entity.Boat.BOAT_MAX_SPEED)) {
            this.impl$maxSpeed = compound.getFloat(Constants.Entity.Boat.BOAT_MAX_SPEED);
        }
        if (compound.contains(Constants.Entity.Boat.BOAT_MOVE_ON_LAND)) {
            this.impl$moveOnLand = compound.getBoolean(Constants.Entity.Boat.BOAT_MOVE_ON_LAND);
        }
        if (compound.contains(Constants.Entity.Boat.BOAT_OCCUPIED_DECELERATION_SPEED)) {
            this.impl$occupiedDecelerationSpeed = compound.getDouble(Constants.Entity.Boat.BOAT_OCCUPIED_DECELERATION_SPEED);
        }
        if (compound.contains(Constants.Entity.Boat.BOAT_UNOCCUPIED_DECELERATION_SPEED)) {
            this.impl$unoccupiedDecelerationSpeed = compound.getDouble(Constants.Entity.Boat.BOAT_UNOCCUPIED_DECELERATION_SPEED);
        }
    }

    @Redirect(method = "getBoatGlide", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"))
    private float impl$getBlockSlipperinessIfBoatIsNotOverridingMovingOnLand(final Block block) {
        return this.impl$moveOnLand ? Blocks.ICE.getSlipperiness() : block.getSlipperiness();
    }

    @Override
    public void impl$writeToSpongeCompound(final CompoundNBT compound) {
        super.impl$writeToSpongeCompound(compound);
        compound.putFloat(Constants.Entity.Boat.BOAT_MAX_SPEED, this.impl$maxSpeed);
        compound.putBoolean(Constants.Entity.Boat.BOAT_MOVE_ON_LAND, this.impl$moveOnLand);
        compound.putDouble(Constants.Entity.Boat.BOAT_OCCUPIED_DECELERATION_SPEED, this.impl$occupiedDecelerationSpeed);
        compound.putDouble(Constants.Entity.Boat.BOAT_UNOCCUPIED_DECELERATION_SPEED, this.impl$unoccupiedDecelerationSpeed);
    }
}
