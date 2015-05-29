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
package org.spongepowered.common.data.component.entity;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.FoodComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

public class SpongeFoodComponent extends SpongeAbstractComponent<FoodComponent> implements FoodComponent {

    private double exhaustion;
    private double saturation;
    private double foodLevel;

    public SpongeFoodComponent() {
        super(FoodComponent.class);
    }

    @Override
    public double getExhaustion() {
        return this.exhaustion;
    }

    @Override
    public FoodComponent setExhaustion(double exhaustion) {
        checkArgument(exhaustion >= 0 && exhaustion <= 20);
        this.exhaustion = exhaustion;
        return this;
    }

    @Override
    public double getSaturation() {
        return this.saturation;
    }

    @Override
    public FoodComponent setSaturation(double saturation) {
        this.saturation = saturation;
        return this;
    }

    @Override
    public double getFoodLevel() {
        return this.foodLevel;
    }

    @Override
    public FoodComponent setFoodLevel(double foodLevel) {
        this.foodLevel = foodLevel;
        return this;
    }

    @Override
    public FoodComponent copy() {
        return new SpongeFoodComponent()
                .setExhaustion(this.exhaustion)
                .setFoodLevel(this.foodLevel)
                .setSaturation(this.saturation);
    }

    @Override
    public FoodComponent reset() {
        return setExhaustion(0)
                .setFoodLevel(20)
                .setSaturation(20);
    }

    @Override
    public int compareTo(FoodComponent o) {
        return (int) Math.floor((o.getFoodLevel() - this.foodLevel) - (o.getExhaustion() - this.exhaustion) - (o.getSaturation() - this.saturation));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.FOOD_LEVEL.getQuery(), this.foodLevel)
                .set(Tokens.SATURATION.getQuery(), this.saturation)
                .set(Tokens.EXHAUSTION.getQuery(), this.exhaustion);
    }
}
