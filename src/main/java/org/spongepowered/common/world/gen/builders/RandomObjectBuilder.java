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
package org.spongepowered.common.world.gen.builders;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.populator.RandomObject;
import org.spongepowered.api.world.gen.populator.RandomObject.Builder;
import org.spongepowered.common.world.gen.populators.RandomObjectPopulator;

public class RandomObjectBuilder implements RandomObject.Builder {

    private VariableAmount count;
    private VariableAmount height;
    private double chance;
    private PopulatorObject object;

    public RandomObjectBuilder() {
        reset();
    }

    @Override
    public Builder perChunk(VariableAmount count) {
        this.count = checkNotNull(count);
        return this;
    }

    @Override
    public Builder height(VariableAmount height) {
        this.height = checkNotNull(height);
        return this;
    }

    @Override
    public Builder object(PopulatorObject obj) {
        this.object = checkNotNull(obj);
        return this;
    }

    @Override
    public Builder spawnChance(double chance) {
        checkArgument(!Double.isNaN(chance), "Chance must be a number.");
        checkArgument(!Double.isInfinite(chance), "Chance cannot be infinite.");
        this.chance = chance;
        return this;
    }

    @Override
    public Builder from(RandomObject value) {
        return perChunk(value.getAttemptsPerChunk())
            .height(value.getHeightRange())
            .object(value.getObject())
            .spawnChance(value.getSpawnChance());
    }

    @Override
    public Builder reset() {
        this.count = null;
        this.height = VariableAmount.baseWithRandomAddition(0, 128);
        this.chance = 1;
        this.object = null;
        return this;
    }

    @Override
    public RandomObject build() throws IllegalStateException {
        RandomObject pop = new RandomObjectPopulator(this.object, this.count, this.height, this.chance);
        return pop;
    }

}
