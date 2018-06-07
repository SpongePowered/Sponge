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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFoodData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeFoodData extends AbstractData<FoodData, ImmutableFoodData> implements FoodData {

    private int foodLevel;
    private double foodSaturationLevel;
    private double foodExhaustionLevel;

    public SpongeFoodData(int foodLevel, double foodSaturationLevel, double foodExhaustionLevel) {
        super(FoodData.class);
        this.foodLevel = foodLevel;
        this.foodSaturationLevel = foodSaturationLevel;
        this.foodExhaustionLevel = foodExhaustionLevel;
        registerGettersAndSetters();
    }

    public SpongeFoodData() {
        this(20, 5.0F, 0);
    }

    @Override
    public FoodData copy() {
        return new SpongeFoodData(this.foodLevel, this.foodSaturationLevel, this.foodExhaustionLevel);
    }

    @Override
    public ImmutableFoodData asImmutable() {
        return new ImmutableSpongeFoodData(this.foodLevel, this.foodSaturationLevel, this.foodExhaustionLevel);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.FOOD_LEVEL.getQuery(), this.foodLevel)
                .set(Keys.SATURATION.getQuery(), this.foodSaturationLevel)
                .set(Keys.EXHAUSTION.getQuery(), this.foodExhaustionLevel);
    }

    @Override
    public MutableBoundedValue<Integer> foodLevel() {
        return SpongeValueFactory.boundedBuilder(Keys.FOOD_LEVEL)
            .defaultValue(20)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .actualValue(this.foodLevel)
            .build();
    }

    @Override
    public MutableBoundedValue<Double> exhaustion() {
        return SpongeValueFactory.boundedBuilder(Keys.EXHAUSTION)
            .defaultValue(0D)
            .minimum(0D)
            .maximum(Double.MAX_VALUE)
            .actualValue(this.foodExhaustionLevel)
            .build();
    }

    @Override
    public MutableBoundedValue<Double> saturation() {
        return SpongeValueFactory.boundedBuilder(Keys.SATURATION)
            .defaultValue(20D)
            .minimum(0D)
            .maximum(Double.MAX_VALUE)
            .actualValue(this.foodSaturationLevel)
            .build();
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public double getFoodSaturation() {
        return this.foodSaturationLevel;
    }

    public void setFoodSaturation(double foodSaturationLevel) {
        this.foodSaturationLevel = (float) foodSaturationLevel;
    }

    public double getFoodExhaustion() {
        return this.foodExhaustionLevel;
    }

    public void setFoodExhaustion(double foodExhaustionLevel) {
        this.foodExhaustionLevel = (float) foodExhaustionLevel;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.FOOD_LEVEL, SpongeFoodData.this::getFoodLevel);
        registerFieldSetter(Keys.FOOD_LEVEL, SpongeFoodData.this::setFoodLevel);
        registerKeyValue(Keys.FOOD_LEVEL, SpongeFoodData.this::foodLevel);

        registerFieldGetter(Keys.SATURATION, SpongeFoodData.this::getFoodSaturation);
        registerFieldSetter(Keys.SATURATION, SpongeFoodData.this::setFoodSaturation);
        registerKeyValue(Keys.SATURATION, SpongeFoodData.this::saturation);

        registerFieldGetter(Keys.EXHAUSTION, SpongeFoodData.this::getFoodExhaustion);
        registerFieldSetter(Keys.EXHAUSTION, SpongeFoodData.this::setFoodExhaustion);
        registerKeyValue(Keys.EXHAUSTION, SpongeFoodData.this::exhaustion);
    }
}
