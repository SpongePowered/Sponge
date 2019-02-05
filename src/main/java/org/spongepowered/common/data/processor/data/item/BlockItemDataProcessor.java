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
package org.spongepowered.common.data.processor.data.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableBlockItemData;
import org.spongepowered.api.data.manipulator.mutable.BlockItemData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeBlockItemData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;

import java.util.Optional;

public class BlockItemDataProcessor extends AbstractItemSingleDataProcessor<BlockState, BlockItemData, ImmutableBlockItemData> {

    public BlockItemDataProcessor() {
        super(stack -> stack.getItem() instanceof ItemBlock, Keys.ITEM_BLOCKSTATE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(ItemStack stack, BlockState value) {
        final IBlockState blockState = (IBlockState) value;
        final Block baseBlock = blockState.getBlock();
        if (Block.getBlockFromItem(stack.getItem()) != baseBlock) {
            // Invalid state for this stack.
            return false;
        }
        stack.setItemDamage(baseBlock.damageDropped(blockState));
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Optional<BlockState> getVal(ItemStack stack) {
        final Block block = ((ItemBlock) stack.getItem()).getBlock();
        final int blockMeta = stack.getItem().getMetadata(stack.getItemDamage());
        return Optional.of((BlockState) block.getStateFromMeta(blockMeta));
    }

    @Override
    protected Value.Mutable<BlockState> constructMutableValue(BlockState actualValue) {
        return new SpongeMutableValue<>(Keys.ITEM_BLOCKSTATE, actualValue);
    }

    @Override
    protected Value.Immutable<BlockState> constructImmutableValue(BlockState value) {
        return new SpongeImmutableValue<>(Keys.ITEM_BLOCKSTATE, value);
    }

    @Override
    protected BlockItemData createManipulator() {
        return new SpongeBlockItemData();
    }

}
