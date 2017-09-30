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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMovementSpeedData;
import org.spongepowered.api.data.manipulator.mutable.entity.MovementSpeedData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeMovementSpeedData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeMovementSpeedData extends AbstractData<MovementSpeedData, ImmutableMovementSpeedData> implements MovementSpeedData {

    private double walkSpeed;
    private double flySpeed;

    public SpongeMovementSpeedData(double walkSpeed, double flySpeed) {
        super(MovementSpeedData.class);
        this.walkSpeed = walkSpeed;
        this.flySpeed = flySpeed;
        registerGettersAndSetters();
    }

    public SpongeMovementSpeedData() {
        this(0.7D, 0.05D);
    }

    public double getWalkSpeed() {
        return this.walkSpeed;
    }


    public SpongeMovementSpeedData setWalkSpeed(double value) {
        this.walkSpeed = value;
        return this;
    }

    public double getFlySpeed() {
        return this.flySpeed;
    }

    public SpongeMovementSpeedData setFlySpeed(double value) {
        this.flySpeed = value;
        return this;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.WALKING_SPEED, SpongeMovementSpeedData.this::getWalkSpeed);
        registerFieldSetter(Keys.WALKING_SPEED, SpongeMovementSpeedData.this::setWalkSpeed);
        registerKeyValue(Keys.WALKING_SPEED, SpongeMovementSpeedData.this::walkSpeed);

        registerFieldGetter(Keys.FLYING_SPEED, SpongeMovementSpeedData.this::getFlySpeed);
        registerFieldSetter(Keys.FLYING_SPEED, SpongeMovementSpeedData.this::setFlySpeed);
        registerKeyValue(Keys.FLYING_SPEED, SpongeMovementSpeedData.this::flySpeed);
    }

    @Override
    public Value<Double> walkSpeed() {
        return new SpongeValue<>(Keys.WALKING_SPEED, 0.7D, this.walkSpeed);
    }

    @Override
    public Value<Double> flySpeed() {
        return new SpongeValue<>(Keys.FLYING_SPEED, 0.05D, this.flySpeed);
    }

    @Override
    public MovementSpeedData copy() {
        return new SpongeMovementSpeedData(this.walkSpeed, this.flySpeed);
    }

    @Override
    public ImmutableMovementSpeedData asImmutable() {
        return new ImmutableSpongeMovementSpeedData(this.walkSpeed, this.flySpeed);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.WALKING_SPEED, this.walkSpeed)
                .set(Keys.FLYING_SPEED, this.flySpeed);
    }
}
