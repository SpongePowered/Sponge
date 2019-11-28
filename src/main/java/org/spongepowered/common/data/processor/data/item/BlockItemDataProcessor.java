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
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableBlockItemData;
import org.spongepowered.api.data.manipulator.mutable.item.BlockItemData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeBlockItemData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class BlockItemDataProcessor extends AbstractItemSingleDataProcessor<BlockState, Value<BlockState>, BlockItemData, ImmutableBlockItemData> {

    public BlockItemDataProcessor() {
        super(stack -> stack.getItem() instanceof BlockItem, Keys.ITEM_BLOCKSTATE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(ItemStack stack, BlockState value) {
        final net.minecraft.block.BlockState blockState = (net.minecraft.block.BlockState) value;
        final Block baseBlock = blockState.getBlock();
        if (Block.getBlockFromItem(stack.getItem()) != baseBlock) {
            // Invalid state for this stack.
            return false;
        }
        stack.func_77964_b(baseBlock.func_180651_a(blockState));
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Optional<BlockState> getVal(ItemStack stack) {
        final Block block = ((BlockItem) stack.getItem()).getBlock();
        final int blockMeta = stack.getItem().func_77647_b(stack.getDamage());
        return Optional.of((BlockState) block.func_176203_a(blockMeta));
    }

    @Override
    protected Value<BlockState> constructValue(BlockState actualValue) {
        return new SpongeValue<>(Keys.ITEM_BLOCKSTATE, Constants.Catalog.DEFAULT_BLOCK_STATE, actualValue);
    }

    @Override
    protected ImmutableValue<BlockState> constructImmutableValue(BlockState value) {
        return new ImmutableSpongeValue<>(Keys.ITEM_BLOCKSTATE, Constants.Catalog.DEFAULT_BLOCK_STATE, value);
    }

    @Override
    protected BlockItemData createManipulator() {
        return new SpongeBlockItemData();
    }

}
