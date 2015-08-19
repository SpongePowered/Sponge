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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFoodData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.util.GetterFunction;

public class ImmutableSpongeFoodData extends AbstractImmutableData<ImmutableFoodData, FoodData> implements ImmutableFoodData {

    private int foodLevel;
    private float foodSaturationLevel;
    private float foodExhaustionLevel;

    public ImmutableSpongeFoodData(int foodLevel, float foodSaturationLevel, float foodExhaustionLevel) {
        super(ImmutableFoodData.class);
        this.foodLevel = foodLevel;
        this.foodSaturationLevel = foodSaturationLevel;
        this.foodExhaustionLevel = foodExhaustionLevel;
        registerGetters();
    }

    @Override
    public ImmutableFoodData copy() {
        return new ImmutableSpongeFoodData(this.foodLevel, this.foodSaturationLevel, this.foodExhaustionLevel);
    }

    @Override
    public FoodData asMutable() {
        return new SpongeFoodData(this.foodLevel, this.foodSaturationLevel, this.foodExhaustionLevel);
    }

    @Override
    public int compareTo(ImmutableFoodData o) {
        return ComparisonChain.start()
                .compare(o.foodLevel().get().intValue(), this.foodLevel)
                .compare(o.saturation().get().floatValue(), this.foodSaturationLevel)
                .compare(o.exhaustion().get().floatValue(), this.foodExhaustionLevel)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.FOOD_LEVEL.getQuery(), this.foodLevel)
                .set(Keys.SATURATION.getQuery(), this.foodSaturationLevel)
                .set(Keys.EXHAUSTION.getQuery(), this.foodExhaustionLevel);
    }

    @Override
    public ImmutableBoundedValue<Integer> foodLevel() {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeBoundedValue.class, Keys.FOOD_LEVEL, this.foodLevel, this.foodLevel);
    }

    @Override
    public ImmutableBoundedValue<Double> exhaustion() {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeBoundedValue.class, Keys.EXHAUSTION, (double) this.foodExhaustionLevel,
                (double) this.foodExhaustionLevel);
    }

    @Override
    public ImmutableBoundedValue<Double> saturation() {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeBoundedValue.class, Keys.SATURATION, (double) this.foodSaturationLevel,
                                                 (double) this.foodSaturationLevel);
    }

    public int getFood() {
        return this.foodLevel;
    }

    public double getExhaustion() {
        return this.foodExhaustionLevel;
    }

    public double getSaturation() {
        return this.foodSaturationLevel;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.FOOD_LEVEL, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getFood();
            }
        });
        registerKeyValue(Keys.FOOD_LEVEL, new GetterFunction<ImmutableValue<?>>() {
            @Override
            public ImmutableValue<?> get() {
                return foodLevel();
            }
        });

        registerFieldGetter(Keys.EXHAUSTION, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getExhaustion();
            }
        });
        registerKeyValue(Keys.EXHAUSTION, new GetterFunction<ImmutableValue<?>>() {
            @Override
            public ImmutableValue<?> get() {
                return exhaustion();
            }
        });

        registerFieldGetter(Keys.SATURATION, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getSaturation();
            }
        });
        registerKeyValue(Keys.SATURATION, new GetterFunction<ImmutableValue<?>>() {
            @Override
            public ImmutableValue<?> get() {
                return saturation();
            }
        });

    }
}
