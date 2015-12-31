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

import net.minecraft.block.Block;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.util.persistence.InvalidDataException;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.builder.AbstractDataBuilder;
import org.spongepowered.common.data.util.DataQueries;

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

    @SuppressWarnings("rawtypes")
    @Override
    public BlockState.Builder add(DataManipulator<?, ?> manipulator) {
        return add((ImmutableDataManipulator) manipulator.asImmutable());
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
    public BlockState.Builder from(BlockState holder) {
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
        if (!container.contains(DataQueries.BLOCK_TYPE, DataQueries.BLOCK_STATE_UNSAFE_META)) {
            return Optional.empty();
        }
        checkDataExists(container, DataQueries.BLOCK_TYPE);
        checkDataExists(container, DataQueries.BLOCK_STATE_UNSAFE_META);
        /* todo write the deserializers for immutable data....
        final ImmutableList<ImmutableDataManipulator<?, ?>> list;
        if (container.contains(DataQueries.DATA_MANIPULATORS)) {
            list = DataUtil.deserializeImmutableManipulatorList(container.getViewList(DataQueries.DATA_MANIPULATORS).get());
        } else {
            list = ImmutableList.of();
        }
        */
        final String blockid = container.getString(DataQueries.BLOCK_TYPE).get();
        final BlockType blockType = SpongeImpl.getGame().getRegistry().getType(BlockType.class, blockid).get();
        final int meta = container.getInt(DataQueries.BLOCK_STATE_UNSAFE_META).get();
        BlockState blockState = (BlockState) ((Block) blockType).getStateFromMeta(meta);
        try {
            /*
            if (!list.isEmpty() && false) {
                for (ImmutableDataManipulator<?, ?> manipulator : list) {
                    blockState = blockState.with(manipulator).get();
                }
            }
            */
            return Optional.of(blockState);
        } catch (Exception e) {
            throw new InvalidDataException("Could not retrieve a blockstate!", e);
        }
    }
}
