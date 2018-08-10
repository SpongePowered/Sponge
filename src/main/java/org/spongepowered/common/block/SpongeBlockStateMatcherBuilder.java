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
package org.spongepowered.common.block;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.block.BlockStateMatcher;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class SpongeBlockStateMatcherBuilder implements BlockStateMatcher.Builder {

    @Nullable private BlockType type;
    private ArrayList<BlockTrait<?>> traits = new ArrayList<>();
    private ArrayList<Object> values = new ArrayList<>();

    @Override
    public SpongeBlockStateMatcherBuilder type(BlockType type) {
        this.type = checkNotNull(type, "BlockType cannot be null!");
        return this;
    }

    @Override
    public <T extends Comparable<T>> SpongeBlockStateMatcherBuilder trait(BlockTrait<T> trait, T value) throws IllegalArgumentException {
        checkState(this.type != null, "BlockType cannot be null! Must be set before using any traits!");
        checkArgument(this.type.getTraits().contains(trait), "BlockType does not contain the specified trait: %s", trait);
        checkArgument(trait.getPossibleValues().contains(value), "BlockTrait %s does not contain value %s", trait, value);
        checkArgument(!this.traits.contains(trait), "Already contains the trait %s! Cannot add multiple values!", trait);
        this.traits.add(trait);
        this.values.add(value);
        return this;
    }

    @Override
    public SpongeBlockStateMatcher build() throws IllegalStateException {
        checkState(this.type != null, "BlockType cannot be null!");
        final int size = this.traits.size() == 0 ? 0 : this.traits.size() - 1;
        return new SpongeBlockStateMatcher(this.type, this.traits.toArray(new BlockTrait<?>[size]), this.values.toArray());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SpongeBlockStateMatcherBuilder from(BlockStateMatcher value) {
        reset();
        final SpongeBlockStateMatcher other = (SpongeBlockStateMatcher) value;
        type(other.type);
        for (int i = 0; i < other.traits.length; i++) {
            trait((BlockTrait) other.traits[i], (Comparable) other.values[i]);
        }
        return this;
    }

    @Override
    public SpongeBlockStateMatcherBuilder reset() {
        this.type = null;
        this.traits.clear();
        this.values.clear();
        return this;
    }
}
