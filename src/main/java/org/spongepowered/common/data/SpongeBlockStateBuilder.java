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
package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockStateBuilder;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.service.persistence.InvalidDataException;

import java.util.List;

public class SpongeBlockStateBuilder implements BlockStateBuilder {

    private BlockType blockType;
    private List<ImmutableDataManipulator<?, ?>> manipulators;

    @Override
    public BlockStateBuilder blockType(BlockType blockType) {
        this.blockType = checkNotNull(blockType);
        return this;
    }

    @Override
    public <M extends DataManipulator<M, ?>> BlockStateBuilder add(M manipulator) {
        this.manipulators.add(manipulator.asImmutable());
        return this;
    }

    @Override
    public <I extends ImmutableDataManipulator<I, ?>> BlockStateBuilder add(I manipulator) {
        this.manipulators.add(manipulator);
        return this;
    }

    @Override
    public BlockStateBuilder from(BlockState holder) {
        this.blockType = holder.getType();
        this.manipulators = Lists.newArrayList();
        this.manipulators.addAll(holder.getManipulators());
        return this;
    }

    @Override
    public BlockStateBuilder reset() {
        this.blockType = BlockTypes.STONE;
        this.manipulators = Lists.newArrayList();
        return this;
    }

    @Override
    public BlockState build() {
        IBlockState blockState = ((IBlockState) this.blockType.getDefaultState());
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            blockState = (IBlockState) ((BlockState) blockState).with( manipulator);
        }
        return (BlockState) blockState;
    }

    @Override
    public Optional<BlockState> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }
}
