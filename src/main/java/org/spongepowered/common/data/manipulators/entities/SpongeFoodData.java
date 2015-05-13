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
package org.spongepowered.common.data.manipulators.entities;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.FoodData;
import org.spongepowered.common.data.manipulators.SpongeAbstractData;

public class SpongeFoodData extends SpongeAbstractData<FoodData> implements FoodData {

    private double exhaustion;
    private double saturation;
    private double foodLevel;

    public SpongeFoodData() {
        super(FoodData.class);
    }

    @Override
    public double getExhaustion() {
        return this.exhaustion;
    }

    @Override
    public FoodData setExhaustion(double exhaustion) {
        checkArgument(exhaustion >= 0 && exhaustion <= 20);
        this.exhaustion = exhaustion;
        return this;
    }

    @Override
    public double getSaturation() {
        return this.saturation;
    }

    @Override
    public FoodData setSaturation(double saturation) {
        this.saturation = saturation;
        return this;
    }

    @Override
    public double getFoodLevel() {
        return this.foodLevel;
    }

    @Override
    public FoodData setFoodLevel(double foodLevel) {
        this.foodLevel = foodLevel;
        return this;
    }

    @Override
    public FoodData copy() {
        return new SpongeFoodData()
                .setExhaustion(this.exhaustion)
                .setFoodLevel(this.foodLevel)
                .setSaturation(this.saturation);
    }

    @Override
    public int compareTo(FoodData o) {
        return (int) Math.floor((o.getFoodLevel() - this.foodLevel) - (o.getExhaustion() - this.exhaustion) - (o.getSaturation() - this.saturation));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(of("FoodLevel"), this.foodLevel)
                .set(of("Saturation"), this.saturation)
                .set(of("Exhaustion"), this.exhaustion);
    }
}
