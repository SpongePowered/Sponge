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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.MinableFeature;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.Ore.Builder;
import org.spongepowered.common.world.gen.WorldGenConstants;

import java.util.function.Predicate;

public class OreBuilder implements Ore.Builder {

    private BlockState block;
    private VariableAmount size;
    private VariableAmount count;
    private VariableAmount height;
    private Predicate<BlockState> conditions;

    public OreBuilder() {
        reset();
    }

    @Override
    public Builder ore(BlockState block) {
        this.block = checkNotNull(block, "block");
        return this;
    }

    @Override
    public Builder size(VariableAmount size) {
        this.size = checkNotNull(size, "size");
        return this;
    }

    @Override
    public Builder perChunk(VariableAmount count) {
        this.count = checkNotNull(count, "count");
        return this;
    }

    @Override
    public Builder height(VariableAmount height) {
        this.height = checkNotNull(height, "height");
        return this;
    }

    @Override
    public Builder placementCondition(Predicate<BlockState> condition) {
        this.conditions = checkNotNull(condition, "conditions");
        return this;
    }

    @Override
    public Builder from(Ore value) {
        return ore(value.getOreBlock())
            .size(value.getDepositSize())
            .perChunk(value.getDepositsPerChunk())
            .height(value.getHeight())
            .placementCondition(value.getPlacementCondition());
    }

    @Override
    public Builder reset() {
        this.block = (BlockState) Blocks.field_150366_p.func_176223_P();
        this.size = VariableAmount.fixed(9);
        this.count = VariableAmount.fixed(20);
        this.height = VariableAmount.baseWithRandomAddition(0, 64);
        this.conditions = WorldGenConstants.STONE;
        return this;
    }

    @Override
    public Ore build() throws IllegalStateException {
        Ore pop = (Ore) new MinableFeature((IBlockState) this.block, 10);
        pop.setDepositSize(this.size);
        pop.setDepositsPerChunk(this.count);
        pop.setHeight(this.height);
        pop.setOreBlock(this.block);
        pop.setPlacementCondition(this.conditions);
        return pop;
    }

}
