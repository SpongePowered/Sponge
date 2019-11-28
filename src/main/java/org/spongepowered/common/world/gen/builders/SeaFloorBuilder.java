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

import net.minecraft.init.Blocks;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.SeaFloor;
import org.spongepowered.api.world.gen.populator.SeaFloor.Builder;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.gen.populators.SeaFloorPopulator;

import java.util.function.Predicate;

public class SeaFloorBuilder implements SeaFloor.Builder {

    private BlockState block;
    private VariableAmount count;
    private VariableAmount radius;
    private VariableAmount depth;
    private Predicate<BlockState> check;

    public SeaFloorBuilder() {
        reset();
    }

    @Override
    public Builder block(BlockState block) {
        this.block = checkNotNull(block, "block");
        return this;
    }

    @Override
    public Builder perChunk(VariableAmount count) {
        this.count = checkNotNull(count, "count");
        return this;
    }

    @Override
    public Builder radius(VariableAmount radius) {
        this.radius = checkNotNull(radius, "radius");
        return this;
    }

    @Override
    public Builder depth(VariableAmount depth) {
        this.depth = checkNotNull(depth, "depth");
        return this;
    }

    @Override
    public Builder replace(Predicate<BlockState> check) {
        this.check = checkNotNull(check, "check");
        return this;
    }

    @Override
    public Builder from(SeaFloor value) {
        this.block(value.getBlock())
            .perChunk(value.getDiscsPerChunk())
            .radius(value.getRadius())
            .depth(value.getDepth())
            .replace(value.getValidBlocksToReplace());
        return this;
    }

    @Override
    public Builder reset() {
        this.check = WorldGenConstants.DIRT_OR_GRASS;
        this.block = (BlockState) Blocks.field_150354_m.func_176223_P();
        this.radius = VariableAmount.fixed(7);
        this.count = VariableAmount.fixed(3);
        this.depth = VariableAmount.fixed(2);
        return this;
    }

    @Override
    public SeaFloor build() throws IllegalStateException {
        SeaFloor pop = new SeaFloorPopulator(this.block, this.radius, this.count, this.depth, this.check);
        return pop;
    }

}
