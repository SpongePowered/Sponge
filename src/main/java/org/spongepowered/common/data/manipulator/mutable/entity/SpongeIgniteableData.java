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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeIgniteableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeIgniteableData extends AbstractData<IgniteableData, ImmutableIgniteableData> implements IgniteableData {

    private int fireTicks;
    private int fireDelay;

    public SpongeIgniteableData(int fireTicks, int fireDelay) {
        super(IgniteableData.class);
        checkArgument(fireTicks > 0, "");
        this.fireTicks = fireTicks;
        this.fireDelay = fireDelay;
        registerGettersAndSetters();
    }

    public SpongeIgniteableData() {
        this(Constants.Entity.DEFAULT_FIRE_TICKS, Constants.Entity.DEFAULT_FIRE_DAMAGE_DELAY);
    }

    @Override
    public MutableBoundedValue<Integer> fireTicks() {
        return SpongeValueFactory.boundedBuilder(Keys.FIRE_TICKS)
            .defaultValue(Constants.Entity.DEFAULT_FIRE_TICKS)
            .minimum(1)
            .maximum(Integer.MAX_VALUE)
            .actualValue(this.fireTicks)
            .build();
    }

    @Override
    public MutableBoundedValue<Integer> fireDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.FIRE_DAMAGE_DELAY)
            .defaultValue(Constants.Entity.DEFAULT_FIRE_DAMAGE_DELAY)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .actualValue(this.fireDelay)
            .build();
    }

    @Override
    public IgniteableData copy() {
        return new SpongeIgniteableData(this.fireTicks, this.fireDelay);
    }

    @Override
    public ImmutableIgniteableData asImmutable() {
        return new ImmutableSpongeIgniteableData(this.fireTicks, this.fireDelay);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.FIRE_TICKS, this.fireTicks)
            .set(Keys.FIRE_DAMAGE_DELAY, this.fireDelay);
    }

    public int getFireTicks() {
        return this.fireTicks;
    }

    public SpongeIgniteableData setFireTicks(int value) {
        checkArgument(value > 0);
        this.fireTicks = value;
        return this;
    }

    public int getFireDelay() {
        return this.fireDelay;
    }

    public SpongeIgniteableData setFireDelay(int value) {
        this.fireDelay = value;
        return this;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.FIRE_TICKS, SpongeIgniteableData.this::getFireTicks);
        registerFieldSetter(Keys.FIRE_TICKS, SpongeIgniteableData.this::setFireTicks);
        registerKeyValue(Keys.FIRE_TICKS, SpongeIgniteableData.this::fireTicks);

        registerFieldGetter(Keys.FIRE_DAMAGE_DELAY, SpongeIgniteableData.this::getFireDelay);
        registerFieldSetter(Keys.FIRE_DAMAGE_DELAY, SpongeIgniteableData.this::setFireDelay);
        registerKeyValue(Keys.FIRE_DAMAGE_DELAY, SpongeIgniteableData.this::fireDelay);
    }

}
