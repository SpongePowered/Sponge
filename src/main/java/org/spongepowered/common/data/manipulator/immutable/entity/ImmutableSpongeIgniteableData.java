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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeIgniteableData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class ImmutableSpongeIgniteableData extends AbstractImmutableData<ImmutableIgniteableData, IgniteableData> implements ImmutableIgniteableData {

    private final int fireTicks;
    private final int fireDelay;

    private final ImmutableBoundedValue<Integer> fireTicksValue;
    private final ImmutableBoundedValue<Integer> fireDelayValue;

    public ImmutableSpongeIgniteableData(int fireTicks, int fireDelay) {
        super(ImmutableIgniteableData.class);
        checkArgument(fireTicks > 0);
        this.fireTicks = fireTicks;
        this.fireDelay = fireDelay;

        this.fireTicksValue = SpongeValueFactory.boundedBuilder(Keys.FIRE_TICKS)
                .actualValue(this.fireTicks)
                .defaultValue(Constants.Entity.DEFAULT_FIRE_TICKS)
                .minimum(1)
                .maximum(Integer.MAX_VALUE)
                .build()
                .asImmutable();

        this.fireDelayValue = SpongeValueFactory.boundedBuilder(Keys.FIRE_DAMAGE_DELAY)
                .actualValue(this.fireDelay)
                .defaultValue(Constants.Entity.DEFAULT_FIRE_DAMAGE_DELAY)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .build()
                .asImmutable();

        registerGetters();
    }

    @Override
    public ImmutableBoundedValue<Integer> fireTicks() {
        return this.fireTicksValue;
    }

    @Override
    public ImmutableBoundedValue<Integer> fireDelay() {
        return this.fireDelayValue;
    }

    @Override
    public IgniteableData asMutable() {
        return new SpongeIgniteableData(this.fireTicks, this.fireDelay);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.FIRE_TICKS.getQuery(), this.fireTicks)
            .set(Keys.FIRE_DAMAGE_DELAY.getQuery(), this.fireDelay);
    }

    public int getFireTicks() {
        return this.fireTicks;
    }

    public int getFireDelay() {
        return this.fireDelay;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.FIRE_TICKS, ImmutableSpongeIgniteableData.this::getFireTicks);
        registerKeyValue(Keys.FIRE_TICKS, ImmutableSpongeIgniteableData.this::fireTicks);

        registerFieldGetter(Keys.FIRE_DAMAGE_DELAY, ImmutableSpongeIgniteableData.this::getFireDelay);
        registerKeyValue(Keys.FIRE_DAMAGE_DELAY, ImmutableSpongeIgniteableData.this::fireDelay);
    }

}
