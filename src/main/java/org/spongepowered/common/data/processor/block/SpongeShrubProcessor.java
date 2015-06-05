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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.errorData;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.block.ShrubData;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.block.SpongeShrubData;
import org.spongepowered.common.mixin.core.data.types.MixinEnumShrubType;

import java.util.List;

public class SpongeShrubProcessor implements SpongeBlockProcessor<ShrubData>, SpongeDataProcessor<ShrubData> {

    @Override
    public Optional<ShrubData> getFrom(DataHolder dataHolder) {
        return createFrom(dataHolder); // since the itemstack can always have it iif and only if the itemstack is of type tall grass
    }

    @Override
    public Optional<ShrubData> fillData(DataHolder dataHolder, ShrubData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof ItemStack) || !(((ItemStack) dataHolder).getItem() instanceof ItemBlock)) {
            return Optional.absent();
        }
        final Block block = ((ItemBlock) ((ItemStack) dataHolder).getItem()).getBlock();
        if (block != Blocks.tallgrass) {
            return Optional.absent();
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                final List<ShrubType> shrubTypes = ((List<ShrubType>) Sponge.getSpongeRegistry().getAllOf(ShrubType.class));
                final int mod = shrubTypes.size() - 1;
                try {
                    final ShrubType shrubType = shrubTypes.get(((ItemStack) dataHolder).getItemDamage() % mod);
                    return Optional.of(manipulator.setValue(shrubType));
                } catch (Exception e) {
                    e.printStackTrace();
                    return Optional.of(manipulator);
                }
            default:
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, ShrubData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof ItemStack) || !(((ItemStack) dataHolder).getItem() instanceof ItemBlock)) {
            return fail(manipulator);
        }
        final Block block = ((ItemBlock) ((ItemStack) dataHolder).getItem()).getBlock();
        if (block != Blocks.tallgrass) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                final List<ShrubType> shrubTypes = ((List<ShrubType>) Sponge.getSpongeRegistry().getAllOf(ShrubType.class));
                final int mod = shrubTypes.size() - 1;
                try {
                    final ShrubData oldData = getFrom(dataHolder).get();
                    final ShrubType shrubType = shrubTypes.get(((ItemStack) dataHolder).getItemDamage() % mod);
                    final BlockTallGrass.EnumType shrubEnum = (BlockTallGrass.EnumType) (Object) shrubType;
                    ((ItemStack) dataHolder).setItemDamage(shrubEnum.getMeta());
                    return successReplaceData(oldData);
                } catch (Exception e) {
                    e.printStackTrace();
                    return errorData(manipulator);
                }
            default:
                return successNoData();
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<ShrubData> build(DataView container) throws InvalidDataException {
        final String shrubId = getData(container, SpongeShrubData.SHRUB_TYPE, String.class);
        final Optional<ShrubType> shrubTypeOptional = Sponge.getSpongeRegistry().getType(ShrubType.class, shrubId);
        return shrubTypeOptional.isPresent() ? Optional.of(create().setValue(shrubTypeOptional.get())) : Optional.<ShrubData>absent();
    }

    @Override
    public ShrubData create() {
        return new SpongeShrubData();
    }

    @Override
    public Optional<ShrubData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack) || !(((ItemStack) dataHolder).getItem() instanceof ItemBlock)) {
            return Optional.absent();
        }
        final ItemStack itemStack = ((ItemStack) dataHolder);
        final Item item = itemStack.getItem();
        final Block block = ((ItemBlock) item).getBlock();
        if (block != Blocks.tallgrass) {
            return Optional.absent();
        }
        final List<ShrubType> shrubTypes = ((List<ShrubType>) Sponge.getSpongeRegistry().getAllOf(ShrubType.class));
        final int mod = shrubTypes.size() - 1;
        try {
            final ShrubType shrubType = shrubTypes.get(itemStack.getItemDamage() % mod);
            return Optional.of(create().setValue(shrubType));

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.absent();
        }
    }

    @Override
    public Optional<ShrubData> fromBlockPos(World world, BlockPos blockPos) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        final Block block = blockState.getBlock();
        if (block != Blocks.tallgrass) {
            return Optional.absent();
        }
        final ShrubType grassType = (ShrubType) blockState.getValue(BlockTallGrass.TYPE);
        return Optional.of(create().setValue(grassType));
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, ShrubData manipulator, DataPriority priority) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() != Blocks.tallgrass) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                final ShrubData oldData = createFrom(blockState).get();
                final BlockTallGrass.EnumType shrub = (BlockTallGrass.EnumType) (Object) checkNotNull(manipulator).getValue();
                blockState.withProperty(BlockTallGrass.TYPE, shrub);
                return successReplaceData(oldData);
            default:
                return successNoData();
        }
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
    public Optional<ShrubData> createFrom(IBlockState blockState) {
        final Block block = blockState.getBlock();
        if (block != Blocks.tallgrass) {
            return Optional.absent();
        }
        final ShrubType shrubType = (ShrubType) blockState.getValue(BlockTallGrass.TYPE);
        return Optional.absent();
    }
}
