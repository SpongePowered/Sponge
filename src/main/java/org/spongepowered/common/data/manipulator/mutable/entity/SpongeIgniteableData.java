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
import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeIgniteableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;

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
        this(1, 20);
    }

    @Override
    public MutableBoundedValue<Integer> fireTicks() {
        return new SpongeBoundedValue<>(Keys.FIRE_TICKS, 10, intComparator(), 1, Integer.MAX_VALUE, this.fireTicks);
    }

    @Override
    public MutableBoundedValue<Integer> fireDelay() {
        return new SpongeBoundedValue<>(Keys.FIRE_DAMAGE_DELAY, 20, intComparator(), 0, Integer.MAX_VALUE, this.fireDelay);
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
    public int compareTo(IgniteableData o) {
        return ComparisonChain.start()
                .compare(o.fireTicks().get().intValue(), this.fireTicks)
                .compare(o.fireDelay().get().intValue(), this.fireDelay)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
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
