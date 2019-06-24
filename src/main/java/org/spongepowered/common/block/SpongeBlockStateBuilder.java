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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class SpongeBlockStateBuilder extends AbstractDataBuilder<BlockState> implements BlockState.Builder {

    private BlockState blockState;

    public SpongeBlockStateBuilder() {
        super(BlockState.class, 1);
    }

    @Override
    public BlockState.Builder blockType(BlockType blockType) {
        this.blockState = checkNotNull(blockType).getDefaultState();
        return this;
    }

    @Override
    public BlockState.Builder add(DataManipulator<?, ?> manipulator) {
        return add(manipulator.asImmutable());
    }

    @Override
    public BlockState.Builder add(ImmutableDataManipulator<?, ?> manipulator) {
        final Optional<BlockState> optional = this.blockState.with(manipulator);
        if (optional.isPresent()) {
            this.blockState = optional.get();
        }
        return this;
    }

    @Override
    public <V> SpongeBlockStateBuilder add(Key<? extends BaseValue<V>> key, V value) {
        checkNotNull(key, "key");
        this.blockState = this.blockState.with(key, value).orElse(this.blockState);
        return this;
    }

    @Override
    public SpongeBlockStateBuilder from(BlockState holder) {
        this.blockState = holder;
        return this;
    }

    @Override
    public SpongeBlockStateBuilder reset() {
        this.blockState = BlockTypes.STONE.getDefaultState();
        return this;
    }

    @Override
    public BlockState build() {
        return this.blockState;
    }

    @Override
    protected Optional<BlockState> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Block.BLOCK_STATE)) {
            return Optional.empty();
        }
        checkDataExists(container, Constants.Block.BLOCK_STATE);
        try {
            return container.getCatalogType(Constants.Block.BLOCK_STATE, BlockState.class);
        } catch (Exception e) {
            throw new InvalidDataException("Could not retrieve a blockstate!", e);
        }
    }
}
