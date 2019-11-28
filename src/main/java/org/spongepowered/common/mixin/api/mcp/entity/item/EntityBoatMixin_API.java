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
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.mixin.api.mcp.entity.EntityMixin_API;

import java.util.Collection;
import net.minecraft.entity.item.BoatEntity;

// TODO 1.9: Refactor this for boat overhaul
@Mixin(BoatEntity.class)
@Implements(@Interface(iface = Boat.class, prefix = "apiBoat$"))
public abstract class EntityBoatMixin_API extends EntityMixin_API implements Boat {

    @Shadow public abstract BoatEntity.Type getBoatType();

    private double maxSpeed = 0.35D;
    private boolean moveOnLand = false;
    private double occupiedDecelerationSpeed = 0D;
    private double unoccupiedDecelerationSpeed = 0.8D;

    @Override
    public void spongeApi$supplyVanillaManipulators(Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        final BoatEntity.Type boatType = this.getBoatType();
        if (boatType == BoatEntity.Type.OAK) {
            manipulators.add(new SpongeTreeData(TreeTypes.OAK));
        } else if ( boatType == BoatEntity.Type.BIRCH) {
            manipulators.add(new SpongeTreeData(TreeTypes.BIRCH));
        } else if ( boatType == BoatEntity.Type.JUNGLE) {
            manipulators.add(new SpongeTreeData(TreeTypes.JUNGLE));
        } else if ( boatType == BoatEntity.Type.DARK_OAK) {
            manipulators.add(new SpongeTreeData(TreeTypes.DARK_OAK));
        } else if ( boatType == BoatEntity.Type.ACACIA) {
            manipulators.add(new SpongeTreeData(TreeTypes.ACACIA));
        } else if ( boatType == BoatEntity.Type.SPRUCE) {
            manipulators.add(new SpongeTreeData(TreeTypes.SPRUCE));
        }
    }

    @Intrinsic
    public boolean apiBoat$isInWater() {
        return !this.onGround;
    }

    @Override
    public double getMaxSpeed() {
        return this.maxSpeed;
    }

    @Override
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public boolean canMoveOnLand() {
        return this.moveOnLand;
    }

    @Override
    public void setMoveOnLand(boolean moveOnLand) {
        this.moveOnLand = moveOnLand;
    }

    @Override
    public double getOccupiedDeceleration() {
        return this.occupiedDecelerationSpeed;
    }

    @Override
    public void setOccupiedDeceleration(double occupiedDeceleration) {
        this.occupiedDecelerationSpeed = occupiedDeceleration;
    }

    @Override
    public double getUnoccupiedDeceleration() {
        return this.unoccupiedDecelerationSpeed;
    }

    @Override
    public void setUnoccupiedDeceleration(double unoccupiedDeceleration) {
        this.unoccupiedDecelerationSpeed = unoccupiedDeceleration;
    }

}
