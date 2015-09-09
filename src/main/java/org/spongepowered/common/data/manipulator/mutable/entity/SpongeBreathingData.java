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

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBreathingData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

public class SpongeBreathingData extends AbstractData<BreathingData, ImmutableBreathingData> implements BreathingData {

    private int maxAir;
    private int remainingAir;

    public SpongeBreathingData(int maxAir, int remainingAir) {
        super(BreathingData.class);
        this.maxAir = maxAir;
        this.remainingAir = remainingAir;
        registerGettersAndSetters();
    }

    public SpongeBreathingData() {
        this(300, 300);
    }

    @Override
    public BreathingData copy() {
        return new SpongeBreathingData(this.maxAir, this.remainingAir);
    }

    @Override
    public ImmutableBreathingData asImmutable() {
        return new ImmutableSpongeBreathingData(this.maxAir, this.remainingAir);
    }

    @Override
    public int compareTo(BreathingData o) {
        return ComparisonChain.start()
                .compare(o.maxAir().get().intValue(), this.maxAir)
                .compare(o.remainingAir().get().intValue(), this.remainingAir)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.MAX_AIR.getQuery(), this.maxAir)
                .set(Keys.REMAINING_AIR.getQuery(), this.remainingAir);
    }

    @Override
    public MutableBoundedValue<Integer> remainingAir() {
        return new SpongeBoundedValue<Integer>(Keys.MAX_AIR, 300, intComparator(), 0, Integer.MAX_VALUE, this.maxAir);
    }

    @Override
    public MutableBoundedValue<Integer> maxAir() {
        return new SpongeBoundedValue<Integer>(Keys.REMAINING_AIR, this.maxAir, intComparator(), -20, this.maxAir, this.remainingAir);
    }

    private int getMaxAir() {
        return this.maxAir;
    }

    private void setMaxAir(int maxAir) {
        this.maxAir = maxAir;
    }

    private int getRemainingAir() {
        return this.remainingAir;
    }

    private void setRemainingAir(int remainingAir) {
        this.remainingAir = remainingAir;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.MAX_AIR, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getMaxAir();
            }
        });
        registerFieldSetter(Keys.MAX_AIR, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setMaxAir(((Number) value).intValue());
            }
        });
        registerKeyValue(Keys.MAX_AIR, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return maxAir();
            }
        });

        registerFieldGetter(Keys.REMAINING_AIR, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getRemainingAir();
            }
        });
        registerFieldSetter(Keys.REMAINING_AIR, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setRemainingAir(((Number) value).intValue());
            }
        });
        registerKeyValue(Keys.REMAINING_AIR, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return remainingAir();
            }
        });
    }
}
