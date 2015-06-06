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
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.block.FlowerData;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.block.SpongeFlowerData;

import java.util.List;

public class SpongePlantProcessor implements SpongeDataProcessor<FlowerData>, SpongeBlockProcessor<FlowerData> {

    @Override
    public Optional<FlowerData> build(DataView container) throws InvalidDataException {
        final String plantId = getData(container, SpongeFlowerData.PLANT_TYPE, String.class);
        final Optional<PlantType> plantTypeOptional = Sponge.getSpongeRegistry().getType(PlantType.class, plantId);
        if (plantTypeOptional.isPresent()) {
            return Optional.of(create().setValue(plantTypeOptional.get()));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public FlowerData create() {
        return new SpongeFlowerData();
    }

    @Override
    public Optional<FlowerData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack) || !(((ItemStack) dataHolder).getItem() instanceof ItemMultiTexture)) {
            return Optional.absent();
        }
        final ItemMultiTexture item = (ItemMultiTexture) ((ItemStack) dataHolder).getItem();
        if (item.getBlock() != Blocks.red_flower) {
            return Optional.absent();
        }
        final List<PlantType> plantTypes = (List<PlantType>) Sponge.getSpongeRegistry().getAllOf(PlantType.class);
        final int mod = plantTypes.size() - 1;
        final PlantType plantType = plantTypes.get(((ItemStack) dataHolder).getItemDamage() % mod);
        return Optional.of(create().setValue(plantType));
    }

    @Override
    public Optional<FlowerData> fromBlockPos(World world, BlockPos blockPos) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() != Blocks.red_flower && blockState.getBlock() != Blocks.yellow_flower) {
            return Optional.absent();
        } else if (blockState.getBlock() == Blocks.red_flower) {
            final BlockFlower.EnumFlowerType flowerType = (BlockFlower.EnumFlowerType) blockState.getValue(Blocks.red_flower.getTypeProperty());
            final PlantType plantType = (PlantType) (Object) flowerType;
            return Optional.of(create().setValue(plantType));
        } else {
            return Optional.of(create().setValue(PlantTypes.DANDELION));
        }
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, FlowerData manipulator, DataPriority priority) {
        final IBlockState blockState = world.getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() != Blocks.yellow_flower && blockState.getBlock() != Blocks.red_flower) {
            return fail(manipulator);
        }
        if (blockState.getBlock() == Blocks.red_flower) {
            switch (checkNotNull(priority)) {
                case DATA_MANIPULATOR:
                case POST_MERGE:
                    final BlockFlower.EnumFlowerType flowerType = (BlockFlower.EnumFlowerType) (Object) manipulator.getValue();
                    final FlowerData old = fromBlockPos(world, blockPos).get();
                    blockState.withProperty(Blocks.red_flower.getTypeProperty(), flowerType);
                    return successReplaceData(old);
                default:
                    return successNoData();
            }
        } else {
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
    public Optional<FlowerData> createFrom(IBlockState blockState) {
        final Block block = blockState.getBlock();
        if (block == Blocks.red_flower) {
            final BlockFlower.EnumFlowerType flowerType = (BlockFlower.EnumFlowerType) blockState.getValue(Blocks.red_flower.getTypeProperty());
            return Optional.of(create().setValue((PlantType) (Object) flowerType));
        } else if (block == Blocks.yellow_flower) {
            return Optional.of(create().setValue(PlantTypes.DANDELION));
        }
        return Optional.absent();
    }

    @Override
    public Optional<FlowerData> getFrom(DataHolder dataHolder) {
        return Optional.absent();
    }

    @Override
    public Optional<FlowerData> fillData(DataHolder dataHolder, FlowerData manipulator, DataPriority priority) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, FlowerData manipulator, DataPriority priority) {
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }
}
