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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreathingData;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.util.GetterFunction;

public class ImmutableSpongeBreathingData extends AbstractImmutableData<ImmutableBreathingData, BreathingData> implements ImmutableBreathingData {

    private int maxAir;
    private int remainingAir;

    public ImmutableSpongeBreathingData(int maxAir, int remainingAir) {
        super(ImmutableBreathingData.class);
        this.maxAir = maxAir;
        this.remainingAir = remainingAir;
        registerGetters();
    }

    @Override
    public ImmutableBreathingData copy() {
        return new ImmutableSpongeBreathingData(this.maxAir, this.remainingAir);
    }

    @Override
    public BreathingData asMutable() {
        return new SpongeBreathingData(this.maxAir, this.remainingAir);
    }

    @Override
    public int compareTo(ImmutableBreathingData o) {
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
    public ImmutableBoundedValue<Integer> remainingAir() {
        return new ImmutableSpongeBoundedValue<Integer>(Keys.REMAINING_AIR, this.remainingAir, this.maxAir, ComparatorUtil.intComparator(), -20, this.maxAir);
    }

    @Override
    public ImmutableBoundedValue<Integer> maxAir() {
        return new ImmutableSpongeBoundedValue<Integer>(Keys.MAX_AIR, this.maxAir, 300, ComparatorUtil.intComparator(), 0, Integer.MAX_VALUE);
    }

    public int getMaxAir() {
        return this.maxAir;
    }

    public int getRemainingAir() {
        return this.remainingAir;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.MAX_AIR, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getMaxAir();
            }
        });
        registerKeyValue(Keys.MAX_AIR, new GetterFunction<ImmutableValue<?>>() {

            @Override
            public ImmutableValue<?> get() {
                return maxAir();
            }
        });

        registerFieldGetter(Keys.REMAINING_AIR, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getRemainingAir();
            }
        });
        registerKeyValue(Keys.REMAINING_AIR, new GetterFunction<ImmutableValue<?>>() {

            @Override
            public ImmutableValue<?> get() {
                return remainingAir();
            }
        });
    }
}
