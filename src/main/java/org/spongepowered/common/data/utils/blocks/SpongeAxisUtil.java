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
package org.spongepowered.common.data.utils.blocks;

import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.blocks.AxisData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeBlockUtil;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.manipulators.blocks.SpongeAxisData;

public class SpongeAxisUtil implements SpongeDataUtil<AxisData>, SpongeBlockUtil<AxisData> {

    @Override
    public Optional<AxisData> fillData(DataHolder holder, AxisData manipulator, DataPriority priority) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, AxisData manipulator, DataPriority priority) {
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<AxisData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public AxisData create() {
        return new SpongeAxisData();
    }

    @Override
    public Optional<AxisData> createFrom(DataHolder dataHolder) {
        return Optional.absent();
    }

    @Override
    public Optional<AxisData> fromBlockPos(World world, BlockPos blockPos) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, AxisData manipulator, DataPriority priority) {
        return fail(manipulator);
    }

    @Override
    public Optional<BlockState> removeFrom(IBlockState blockState) {
        return Optional.absent();
    }

    @Override
    public boolean remove(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public Optional<AxisData> createFrom(IBlockState blockState) {

        return Optional.absent();
    }
}
