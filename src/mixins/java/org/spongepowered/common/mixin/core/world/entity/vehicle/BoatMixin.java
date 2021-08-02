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
package org.spongepowered.common.mixin.core.world.entity.vehicle;

import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.api.data.Keys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.world.entity.vehicle.BoatBridge;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

@Mixin(Boat.class)
public abstract class BoatMixin extends EntityMixin implements BoatBridge {

    private float impl$maxSpeed = Constants.Entity.Boat.DEFAULT_MAX_SPEED;
    private boolean impl$moveOnLand = Constants.Entity.Boat.MOVE_ON_LAND;
    private double impl$occupiedDecelerationSpeed = Constants.Entity.Boat.OCCUPIED_DECELERATION_SPEED;
    private double impl$unoccupiedDecelerationSpeed = Constants.Entity.Boat.UNOCCUPIED_DECELERATION_SPEED;

    @ModifyConstant(method = "floatBoat", constant = @Constant(floatValue = Constants.Entity.Boat.DEFAULT_MAX_SPEED))
    private float impl$getMaximumWaterMotion(final float originalSpeed) {
        return this.impl$maxSpeed;
    }

    @Override
    public double bridge$getMaxSpeed() {
        return this.impl$maxSpeed;
    }

    @Override
    public void bridge$setMaxSpeed(double impl$maxSpeed) {
        this.impl$maxSpeed = (float) impl$maxSpeed;
    }

    @Override
    public boolean bridge$getMoveOnLand() {
        return this.impl$moveOnLand;
    }

    @Override
    public void bridge$setMoveOnLand(boolean impl$moveOnLand) {
        this.impl$moveOnLand = impl$moveOnLand;
        if (impl$moveOnLand) {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.CAN_MOVE_ON_LAND, true);
        } else {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.CAN_MOVE_ON_LAND);
        }
    }

    @Override
    public double bridge$getOccupiedDecelerationSpeed() {
        return this.impl$occupiedDecelerationSpeed;
    }

    @Override
    public void bridge$setOccupiedDecelerationSpeed(double impl$occupiedDecelerationSpeed) {
        this.impl$occupiedDecelerationSpeed = impl$occupiedDecelerationSpeed;
        if (impl$occupiedDecelerationSpeed == Constants.Entity.Boat.OCCUPIED_DECELERATION_SPEED) {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.OCCUPIED_DECELERATION);
        } else {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.OCCUPIED_DECELERATION, impl$occupiedDecelerationSpeed);
        }
    }

    @Override
    public double bridge$getUnoccupiedDecelerationSpeed() {
        return this.impl$unoccupiedDecelerationSpeed;
    }

    @Override
    public void bridge$setUnoccupiedDecelerationSpeed(double impl$unoccupiedDecelerationSpeed) {
        this.impl$unoccupiedDecelerationSpeed = impl$unoccupiedDecelerationSpeed;
        if (impl$unoccupiedDecelerationSpeed == Constants.Entity.Boat.UNOCCUPIED_DECELERATION_SPEED) {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.UNOCCUPIED_DECELERATION);
        } else {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.UNOCCUPIED_DECELERATION, impl$unoccupiedDecelerationSpeed);
        }
    }
}
