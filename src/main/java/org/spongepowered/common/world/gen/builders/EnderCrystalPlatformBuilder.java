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

import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.EnderCrystalPlatform;
import org.spongepowered.api.world.gen.populator.EnderCrystalPlatform.Builder;


public class EnderCrystalPlatformBuilder implements EnderCrystalPlatform.Builder {
    
    private double chance;
    private VariableAmount height;
    private VariableAmount radius;
    
    public EnderCrystalPlatformBuilder() {
        reset();
    }

    @Override
    public Builder chance(double p) {
        checkArgument(!Double.isNaN(p), "The probability must be a number.");
        checkArgument(!Double.isInfinite(p), "The probability cannot be infinite.");
        this.chance = p;
        return this;
    }

    @Override
    public Builder height(VariableAmount height) {
        this.height = height;
        return this;
    }

    @Override
    public Builder radius(VariableAmount radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public Builder reset() {
        this.chance = 0.2;
        this.radius = VariableAmount.baseWithRandomAddition(1, 4);
        this.height = VariableAmount.baseWithRandomAddition(6, 32);
        return this;
    }

    @Override
    public EnderCrystalPlatform build() throws IllegalStateException {
        EnderCrystalPlatform populator = (EnderCrystalPlatform) new WorldGenSpikes(Blocks.end_stone);
        populator.setSpawnProbability(this.chance);
        populator.setHeight(this.height);
        populator.setRadius(this.radius);
        return populator;
    }

}
