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

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoublePlant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.block.DoublePlantData;
import org.spongepowered.api.data.type.DoubleSizePlantType;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.block.SpongeDoublePlantData;

import java.util.List;

public class SpongeDoublePlantProcessor implements SpongeBlockProcessor<DoublePlantData>, SpongeDataProcessor<DoublePlantData> {

    @Override
    public Optional<DoublePlantData> getFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack) || !(((ItemStack) dataHolder).getItem() instanceof ItemDoublePlant)) {
            return Optional.absent();
        }
        final ItemStack itemStack = ((ItemStack) dataHolder);
        final BlockDoublePlant.EnumPlantType plantType = BlockDoublePlant.EnumPlantType.byMetadata(itemStack.getMetadata());
        return Optional.of(create().setValue((DoubleSizePlantType) (Object) plantType));
    }

    @Override
    public Optional<DoublePlantData> fillData(DataHolder dataHolder, DoublePlantData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof ItemStack) || !(((ItemStack) dataHolder).getItem() instanceof ItemDoublePlant)) {
            return Optional.absent();
        }
        final Block block = ((ItemBlock) ((ItemStack) dataHolder).getItem()).getBlock();
        if (block != Blocks.double_plant) {
            return Optional.absent();
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                final List<DoubleSizePlantType> doublePlantTypes = ((List<DoubleSizePlantType>) Sponge.getSpongeRegistry().getAllOf(DoubleSizePlantType.class));
                final int mod = doublePlantTypes.size() - 1;
                try {
                    final DoubleSizePlantType doublePlantType = doublePlantTypes.get(((ItemStack) dataHolder).getItemDamage() % mod);
                    return Optional.of(manipulator.setValue(doublePlantType));
                } catch (Exception e) {
                    e.printStackTrace();
                    return Optional.of(manipulator);
                }
            default:
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, DoublePlantData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof ItemStack) || !(((ItemStack) dataHolder).getItem() instanceof ItemBlock)) {
            return fail(manipulator);
        }
        final Block block = ((ItemBlock) ((ItemStack) dataHolder).getItem()).getBlock();
        if (block != Blocks.double_plant) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                final List<DoubleSizePlantType> doublePlantTypes = ((List<DoubleSizePlantType>) Sponge.getSpongeRegistry().getAllOf(DoubleSizePlantType.class));
                final int mod = doublePlantTypes.size() - 1;
                try {
                    final DoublePlantData oldData = getFrom(dataHolder).get();
                    final DoubleSizePlantType plantType = doublePlantTypes.get(((ItemStack) dataHolder).getItemDamage() % mod);
                    final BlockDoublePlant.EnumPlantType plantEnum = (BlockDoublePlant.EnumPlantType) (Object) plantType;
                    ((ItemStack) dataHolder).setItemDamage(plantEnum.getMeta());
                    return successReplaceData(oldData);
                } catch (Exception e) {
                    e.printStackTrace();
                    return errorData(manipulator);
                }
            default:
                return successNoData();
        }    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<DoublePlantData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public DoublePlantData create() {
        return new SpongeDoublePlantData();
    }

    @Override
    public Optional<DoublePlantData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack) || !(((ItemStack) dataHolder).getItem() instanceof ItemDoublePlant)) {
            return Optional.absent();
        }
        final ItemStack itemStack = ((ItemStack) dataHolder);
        final Item item = itemStack.getItem();
        final Block block = ((ItemDoublePlant) item).getBlock();
        if (block != Blocks.double_plant) {
            return Optional.absent();
        }
        final List<DoubleSizePlantType> doublePlantTypes = ((List<DoubleSizePlantType>) Sponge.getSpongeRegistry().getAllOf(DoubleSizePlantType.class));
        final int mod = doublePlantTypes.size() - 1;
        try {
            final DoubleSizePlantType doublePlantType = doublePlantTypes.get(itemStack.getItemDamage() % mod);
            return Optional.of(create().setValue(doublePlantType));

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.absent();
        }
    }

    @Override
    public Optional<DoublePlantData> fromBlockPos(World world, BlockPos blockPos) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        final Block block = blockState.getBlock();
        if (block != Blocks.double_plant) {
            return Optional.absent();
        }
        final DoubleSizePlantType grassType = (DoubleSizePlantType) blockState.getValue(BlockDoublePlant.VARIANT);
        return Optional.of(create().setValue(grassType));
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, DoublePlantData manipulator, DataPriority priority) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() != Blocks.double_plant) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                final DoublePlantData oldData = createFrom(blockState).get();
                final BlockDoublePlant.EnumPlantType plant = (BlockDoublePlant.EnumPlantType) (Object) checkNotNull(manipulator).getValue();
                blockState.withProperty(BlockDoublePlant.VARIANT, plant);
                return successReplaceData(oldData);
            default:
                return successNoData();
        }
    }

    @Override
    public Optional<BlockState> withData(IBlockState blockState, DoublePlantData manipulator) {
        if (blockState.getBlock() == Blocks.double_plant) {
            final BlockDoublePlant.EnumPlantType plant = (BlockDoublePlant.EnumPlantType) (Object) checkNotNull(manipulator).getValue();
            return Optional.of((BlockState) blockState.withProperty(BlockDoublePlant.VARIANT, plant));
        }
        return Optional.absent();
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
    public Optional<DoublePlantData> createFrom(IBlockState blockState) {
        final Block block = blockState.getBlock();
        if (block != Blocks.double_plant) {
            return Optional.absent();
        }
        final DoubleSizePlantType plantType = (DoubleSizePlantType) blockState.getValue(BlockDoublePlant.VARIANT);
        return Optional.of(create().setValue(plantType));
    }
}
