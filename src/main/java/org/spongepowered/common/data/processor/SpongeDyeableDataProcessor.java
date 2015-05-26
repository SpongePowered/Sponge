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
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DyeableData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.SpongeDyeableData;
import org.spongepowered.common.interfaces.block.IMixinBlockDyeable;
import org.spongepowered.common.interfaces.item.IMixinItemDyeable;

public class SpongeDyeableDataProcessor implements SpongeDataProcessor<DyeableData>, SpongeBlockProcessor<DyeableData> {

    @Override
    public Optional<DyeableData> getFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntitySheep) {
            final EnumDyeColor color = ((EntitySheep) dataHolder).getFleeceColor();
            return Optional.of(create().setValue(((DyeColor) (Object) color)));
        }
        if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() instanceof IMixinItemDyeable) {
                return Optional.of(((IMixinItemDyeable) ((ItemStack) dataHolder).getItem()).getDyeableData(((ItemStack) dataHolder)));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<DyeableData> fillData(DataHolder dataHolder, DyeableData manipulator, DataPriority priority) {
        if (dataHolder instanceof EntitySheep) {
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                case PRE_MERGE:
                    return Optional.of(manipulator.setValue((DyeColor) (Object) ((EntitySheep) dataHolder).getFleeceColor()));
                default:
                    return Optional.of(manipulator);
            }
        }
        if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() instanceof IMixinItemDyeable) {
                switch (checkNotNull(priority)) {
                    case DATA_HOLDER:
                    case PRE_MERGE:
                        return Optional.of(manipulator.setValue(((IMixinItemDyeable) ((ItemStack) dataHolder).getItem()).getDyeColor(
                                ((ItemStack) dataHolder))));
                    default:
                        return Optional.of(manipulator);
                }
            }
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, DyeableData manipulator, DataPriority priority) {
        if (dataHolder instanceof EntitySheep) {
            switch (checkNotNull(priority)) {
                case DATA_MANIPULATOR:
                case POST_MERGE:
                    final DyeableData oldData = getFrom(dataHolder).get();
                    ((EntitySheep) dataHolder).setFleeceColor((EnumDyeColor) (Object) manipulator.getValue());
                    return successReplaceData(oldData);
                default:
                    return successNoData();
            }
        }
        if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() instanceof IMixinItemDyeable) {
                switch (checkNotNull(priority)) {
                    case DATA_MANIPULATOR:
                    case POST_MERGE:
                        final DyeableData oldData = getFrom(dataHolder).get();
                        ((IMixinItemDyeable) ((ItemStack) dataHolder).getItem()).setDyeableData(((ItemStack) dataHolder), manipulator.copy());
                        return successReplaceData(oldData);
                    default:
                        return successNoData();
                }
            }
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<DyeableData> build(DataView container) throws InvalidDataException {
        final String dyeString = getData(container, SpongeDyeableData.DYE_COLOR, String.class);
        final Optional<DyeColor> colorOptional = Sponge.getGame().getRegistry().getType(DyeColor.class, dyeString);
        if (colorOptional.isPresent()) {
            return Optional.of(create().setValue(colorOptional.get()));
        }
        return Optional.absent();
    }

    @Override
    public DyeableData create() {
        return new SpongeDyeableData();
    }

    @Override
    public Optional<DyeableData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntitySheep) {
            return getFrom(dataHolder);
        }
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() instanceof IMixinItemDyeable) {
            return getFrom(dataHolder);
        }
        return Optional.absent();
    }

    @Override
    public Optional<DyeableData> fromBlockPos(World world, BlockPos blockPos) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() instanceof IMixinBlockDyeable) {
            return Optional.of(((IMixinBlockDyeable) blockState.getBlock()).getDyeableData(blockState));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, DyeableData manipulator, DataPriority priority) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (!(blockState.getBlock() instanceof IMixinBlockDyeable)) {
            return fail(manipulator);
        }
        return ((IMixinBlockDyeable) blockState.getBlock()).setDyeableData(checkNotNull(manipulator), world, blockPos, priority);
    }

    @Override
    public boolean remove(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public Optional<BlockState> removeFrom(IBlockState blockState) { // we can't really remove dyeable data since all blockstates require it.
        return Optional.absent();
    }

    @Override
    public Optional<DyeableData> createFrom(IBlockState blockState) {
        if (blockState.getBlock() instanceof IMixinBlockDyeable) {
            return Optional.of(((IMixinBlockDyeable) blockState.getBlock()).getDyeableData(blockState));
        }
        return Optional.absent();
    }
}
