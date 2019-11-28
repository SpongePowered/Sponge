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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFoodData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class ImmutableSpongeFoodData extends AbstractImmutableData<ImmutableFoodData, FoodData> implements ImmutableFoodData {

    private final int foodLevel;
    private final double foodSaturationLevel;
    private final double foodExhaustionLevel;

    private final ImmutableBoundedValue<Integer> foodLevelValue;
    private final ImmutableBoundedValue<Double> saturationValue;
    private final ImmutableBoundedValue<Double> exhaustionValue;


    public ImmutableSpongeFoodData(int foodLevel, double foodSaturationLevel, double foodExhaustionLevel) {
        super(ImmutableFoodData.class);
        this.foodLevel = foodLevel;
        this.foodSaturationLevel = foodSaturationLevel;
        this.foodExhaustionLevel = foodExhaustionLevel;

        this.foodLevelValue = SpongeValueFactory.boundedBuilder(Keys.FOOD_LEVEL)
                .defaultValue(20)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .actualValue(this.foodLevel)
                .build()
                .asImmutable();

        this.exhaustionValue = SpongeValueFactory.boundedBuilder(Keys.EXHAUSTION)
                .actualValue(this.foodExhaustionLevel)
                .defaultValue(0D)
                .minimum(0D)
                .maximum(Double.MAX_VALUE)
                .build()
                .asImmutable();

        this.saturationValue = SpongeValueFactory.boundedBuilder(Keys.SATURATION)
                .actualValue(this.foodSaturationLevel)
                .defaultValue(20D)
                .minimum(0D)
                .maximum(Double.MAX_VALUE)
                .build()
                .asImmutable();

        registerGetters();
    }

    @Override
    public FoodData asMutable() {
        return new SpongeFoodData(this.foodLevel, this.foodSaturationLevel, this.foodExhaustionLevel);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.FOOD_LEVEL, this.foodLevel)
                .set(Keys.SATURATION, this.foodSaturationLevel)
                .set(Keys.EXHAUSTION, this.foodExhaustionLevel);
    }

    @Override
    public ImmutableBoundedValue<Integer> foodLevel() {
        return this.foodLevelValue;
    }

    @Override
    public ImmutableBoundedValue<Double> exhaustion() {
        return this.exhaustionValue;
    }

    @Override
    public ImmutableBoundedValue<Double> saturation() {
        return this.saturationValue;
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
        registerFieldGetter(Keys.FOOD_LEVEL, ImmutableSpongeFoodData.this::getFood);
        registerKeyValue(Keys.FOOD_LEVEL, ImmutableSpongeFoodData.this::foodLevel);

        registerFieldGetter(Keys.EXHAUSTION, ImmutableSpongeFoodData.this::getExhaustion);
        registerKeyValue(Keys.EXHAUSTION, ImmutableSpongeFoodData.this::exhaustion);

        registerFieldGetter(Keys.SATURATION, ImmutableSpongeFoodData.this::getSaturation);
        registerKeyValue(Keys.SATURATION, ImmutableSpongeFoodData.this::saturation);

    }
}
