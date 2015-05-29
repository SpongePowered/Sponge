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
package org.spongepowered.common.data.processor;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.base.WetComponent;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.base.SpongeWetComponent;
import org.spongepowered.common.interfaces.entity.IMixinWetHolder;

public class SpongeWetDataProcessor implements SpongeDataProcessor<WetComponent>, SpongeBlockProcessor<WetComponent> {

    @Override
    public Optional<WetComponent> getFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof IMixinWetHolder)) {
            return Optional.absent();
        }
        return Optional.absent();
    }

    @Override
    public Optional<WetComponent> fillData(DataHolder dataHolder, WetComponent manipulator, DataPriority priority) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, WetComponent manipulator, DataPriority priority) {
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<WetComponent> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public WetComponent create() {
        return new SpongeWetComponent();
    }

    @Override
    public Optional<WetComponent> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof Entity)) {
            return Optional.absent();
        }
        return Optional.of(create());
    }

    @Override
    public Optional<WetComponent> fromBlockPos(World world, BlockPos blockPos) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (!(blockState.getBlock() instanceof IMixinWetHolder)) {
            return Optional.absent();
        }
        return ((IMixinWetHolder) blockState.getBlock()).isWet() ? Optional.of(create()) : Optional.<WetComponent>absent();
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, WetComponent manipulator, DataPriority priority) {

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
    public Optional<WetComponent> createFrom(IBlockState blockState) {
        return Optional.absent();
    }
}
