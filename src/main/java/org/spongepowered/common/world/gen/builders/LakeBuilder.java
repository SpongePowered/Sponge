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

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.LakesFeature;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.api.world.gen.populator.Lake.Builder;

import java.util.Optional;

public class LakeBuilder implements Lake.Builder {

    private BlockState liquid;
    private double chance;
    private VariableAmount height;

    public LakeBuilder() {
        reset();
    }

    @Override
    public Builder liquidType(BlockState liquid) {
        this.liquid = checkNotNull(liquid, "liquid");
        Optional<MatterProperty> matter = liquid.getType().getProperty(MatterProperty.class);
        checkArgument(matter.isPresent(), "For some reason, the property is not returning correctly!");
        checkArgument(matter.get().getValue() == MatterProperty.Matter.LIQUID, "Must use a liquid property based BlockState!");
        return this;
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
        this.height = checkNotNull(height);
        return this;
    }

    @Override
    public Builder from(Lake value) {
        return liquidType(value.getLiquidType())
            .chance(value.getLakeProbability())
            .height(value.getHeight());
    }

    @Override
    public Builder reset() {
        this.liquid = BlockTypes.WATER.getDefaultState();
        this.chance = 1 / 20d;
        this.height = VariableAmount.baseWithRandomAddition(0, 256);
        return this;
    }

    @Override
    public Lake build() throws IllegalStateException {
        Lake pop = (Lake) new LakesFeature((Block) this.liquid.getType());
        pop.setLiquidType(this.liquid);
        pop.setLakeProbability(this.chance);
        pop.setHeight(this.height);
        return pop;
    }

}
