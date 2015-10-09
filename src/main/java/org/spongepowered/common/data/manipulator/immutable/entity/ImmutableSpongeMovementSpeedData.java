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
package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMovementSpeedData;
import org.spongepowered.api.data.manipulator.mutable.entity.MovementSpeedData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMovementSpeedData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.common.data.util.ComparatorUtil.doubleComparator;

public class ImmutableSpongeMovementSpeedData extends AbstractImmutableData<ImmutableMovementSpeedData, MovementSpeedData> implements ImmutableMovementSpeedData  {

    private final double walkSpeed;
    private final double flySpeed;

    public ImmutableSpongeMovementSpeedData(double walkSpeed, double flySpeed) {
        super(ImmutableMovementSpeedData.class);
        this.walkSpeed = walkSpeed;
        this.flySpeed = flySpeed;
        registerGetters();
    }

    public double getWalkSpeed() {
        return this.walkSpeed;
    }

    public double getFlySpeed() {
        return this.flySpeed;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.WALKING_SPEED, ImmutableSpongeMovementSpeedData.this::getWalkSpeed);
        registerKeyValue(Keys.WALKING_SPEED, ImmutableSpongeMovementSpeedData.this::walkSpeed);

        registerFieldGetter(Keys.FLYING_SPEED, ImmutableSpongeMovementSpeedData.this::getFlySpeed);
        registerKeyValue(Keys.FLYING_SPEED, ImmutableSpongeMovementSpeedData.this::flySpeed);
    }

    @Override
    public ImmutableBoundedValue<Double> walkSpeed() {
        return new ImmutableSpongeBoundedValue<>(Keys.WALKING_SPEED, this.walkSpeed, 0.1d, doubleComparator(), Double.MIN_VALUE, Double.MAX_VALUE);
    }

    @Override
    public ImmutableBoundedValue<Double> flySpeed() {
        return new ImmutableSpongeBoundedValue<>(Keys.FLYING_SPEED, this.flySpeed, 0.05d, doubleComparator(), Double.MIN_VALUE, Double.MAX_VALUE);
    }

    @Override
    public ImmutableMovementSpeedData copy() {
        return this;
    }

    @Override
    public MovementSpeedData asMutable() {
        return new SpongeMovementSpeedData(walkSpeed, flySpeed);
    }

    @Override
    public int compareTo(ImmutableMovementSpeedData o) {
        return ComparisonChain.start()
                .compare(o.walkSpeed().get().doubleValue(), this.walkSpeed)
                .compare(o.flySpeed().get().doubleValue(), this.flySpeed)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.WALKING_SPEED.getQuery(), this.walkSpeed).set(Keys.FLYING_SPEED.getQuery(), this.flySpeed);
    }
}
