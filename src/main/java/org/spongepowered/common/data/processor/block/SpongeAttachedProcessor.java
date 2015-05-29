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
package org.spongepowered.common.data.processor.block;

import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.block.AttachedComponent;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.block.SpongeAttachedComponent;
import org.spongepowered.common.interfaces.block.IMixinBlockAttachable;

public class SpongeAttachedProcessor implements SpongeDataProcessor<AttachedComponent>, SpongeBlockProcessor<AttachedComponent> {

    public static final DataQuery ATTACHED = of("Attached");

    @Override
    public Optional<AttachedComponent> build(DataView container) throws InvalidDataException {
        checkDataExists(container, ATTACHED);
        final boolean attached = container.getBoolean(ATTACHED).get();
        if (attached) {
            return Optional.of(create());
        }
        return Optional.absent();
    }

    @Override
    public AttachedComponent create() {
        return new SpongeAttachedComponent();
    }

    @Override
    public Optional<AttachedComponent> createFrom(DataHolder dataHolder) {
        return Optional.absent();
    }

    @Override
    public Optional<AttachedComponent> fromBlockPos(World world, BlockPos blockPos) {
        final IBlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof IMixinBlockAttachable) {
            ((IMixinBlockAttachable) blockState.getBlock()).getAttachedData(blockState);
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, AttachedComponent manipulator, DataPriority priority) {
        final IBlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof IMixinBlockAttachable) {
            ((IMixinBlockAttachable) blockState.getBlock()).setAttachedData(manipulator, world, blockPos);
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public Optional<BlockState> removeFrom(IBlockState blockState) {
        return Optional.absent();
    }

    @Override
    public Optional<AttachedComponent> createFrom(IBlockState blockState) {
        return Optional.absent();
    }

    @Override
    public Optional<AttachedComponent> getFrom(DataHolder dataHolder) {
        return Optional.absent();
    }

    @Override
    public Optional<AttachedComponent> fillData(DataHolder dataHolder, AttachedComponent manipulator, DataPriority priority) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, AttachedComponent manipulator, DataPriority priority) {
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }
}
