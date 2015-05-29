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
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.base.RotationalComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.base.SpongeRotationalComponent;
import org.spongepowered.common.interfaces.block.IMixinBlockRotational;

public class SpongeRotationalDataProcessor implements SpongeDataProcessor<RotationalComponent>, SpongeBlockProcessor<RotationalComponent> {

    @Override
    public Optional<RotationalComponent> getFrom(DataHolder dataHolder) {
        return Optional.absent();
    }

    @Override
    public Optional<RotationalComponent> fillData(DataHolder dataHolder, RotationalComponent manipulator, DataPriority priority) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, RotationalComponent manipulator, DataPriority priority) {
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<RotationalComponent> build(DataView container) throws InvalidDataException {
        final String rotationId = getData(container, Tokens.ROTATION.getQuery(), String.class);
        final Optional<Rotation> rotation = Sponge.getGame().getRegistry().getType(Rotation.class, rotationId);
        if (rotation.isPresent()) {
            return Optional.of(create().setValue(rotation.get()));
        }
        return Optional.absent();
    }

    @Override
    public RotationalComponent create() {
        return new SpongeRotationalComponent();
    }

    @Override
    public Optional<RotationalComponent> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntitySkull) {
            final int skullRotationMeta = ((TileEntitySkull) dataHolder).getSkullRotation();
            // todo figure out the magic number of rotation

        }
        return Optional.absent();
    }

    @Override
    public Optional<RotationalComponent> fromBlockPos(World world, BlockPos blockPos) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() instanceof IMixinBlockRotational) {
            return Optional.of(((IMixinBlockRotational) blockState.getBlock()).getRotationalData(blockState));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, RotationalComponent manipulator, DataPriority priority) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() instanceof IMixinBlockRotational) {
            return ((IMixinBlockRotational) blockState.getBlock()).setRotationalData(manipulator, world, blockPos, priority);
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(World world, BlockPos blockPos) {
        return removeFrom(checkNotNull(world).getBlockState(checkNotNull(blockPos))).isPresent();
    }

    @Override
    public Optional<BlockState> removeFrom(IBlockState blockState) {
        if (blockState.getBlock() instanceof IMixinBlockRotational) {
            return Optional.of(((IMixinBlockRotational) blockState.getBlock()).resetRotationalData(blockState));
        }
        return Optional.absent();
    }

    @Override
    public Optional<RotationalComponent> createFrom(IBlockState blockState) {
        if (blockState.getBlock() instanceof IMixinBlockRotational) {
            return Optional.of(((IMixinBlockRotational) blockState.getBlock()).getRotationalData(blockState));
        }
        return Optional.absent();
    }
}
