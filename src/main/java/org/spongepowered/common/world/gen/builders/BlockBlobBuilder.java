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

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.BlockBlobFeature;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.api.world.gen.populator.BlockBlob.Builder;

public class BlockBlobBuilder implements BlockBlob.Builder {

    private BlockState block;
    private VariableAmount radius;
    private VariableAmount count;

    public BlockBlobBuilder() {
        reset();
    }

    @Override
    public Builder block(BlockState block) {
        checkNotNull(block, "block");
        this.block = block;
        return this;
    }

    @Override
    public Builder radius(VariableAmount radius) {
        checkNotNull(radius, "radius");
        this.radius = radius;
        return this;
    }

    @Override
    public Builder blobCount(VariableAmount count) {
        checkNotNull(count, "count");
        this.count = count;
        return this;
    }

    @Override
    public Builder from(BlockBlob value) {
        checkNotNull(value, "BlockBlob cannot be null!");
        this.block = checkNotNull(value.getBlock(), "BlockState cannot be null!");
        this.radius = checkNotNull(value.getRadius(), "Radius cannot be null!");
        this.count = checkNotNull(value.getCount(), "Count cannot be null!");
        return this;
    }

    @Override
    public Builder reset() {
        this.radius = VariableAmount.baseWithVariance(0, 2);
        this.count = VariableAmount.baseWithVariance(0, 3);
        this.block = null;
        return this;
    }

    @Override
    public BlockBlob build() throws IllegalStateException {
        if (this.block == null) {
            throw new IllegalStateException("Builder is missing required BlockState argument.");
        }
        BlockBlob populator = (BlockBlob) new BlockBlobFeature((Block) this.block.getType(), 2);
        populator.setBlock(this.block);
        populator.setRadius(this.radius);
        populator.setCount(this.count);
        return populator;
    }

}
