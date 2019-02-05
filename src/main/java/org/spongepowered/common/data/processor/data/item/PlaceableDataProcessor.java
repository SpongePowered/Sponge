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

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePlaceableData;
import org.spongepowered.api.data.manipulator.mutable.PlaceableData;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePlaceableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.processor.common.BreakablePlaceableUtils;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.SpongeImmutableSetValue;
import org.spongepowered.common.data.value.SpongeMutableSetValue;

import java.util.Optional;
import java.util.Set;

public class PlaceableDataProcessor
        extends AbstractItemSingleDataProcessor<Set<BlockType>, PlaceableData, ImmutablePlaceableData> {

    public PlaceableDataProcessor() {
        super(stack -> true, Keys.PLACEABLE_BLOCKS);
    }

    @Override
    protected PlaceableData createManipulator() {
        return new SpongePlaceableData();
    }

    @Override
    protected Optional<Set<BlockType>> getVal(ItemStack itemStack) {
        return BreakablePlaceableUtils.get(itemStack, NbtDataUtil.ITEM_PLACEABLE_BLOCKS);
    }

    @Override
    protected boolean set(ItemStack itemStack, Set<BlockType> value) {
        return BreakablePlaceableUtils.set(itemStack, NbtDataUtil.ITEM_PLACEABLE_BLOCKS, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (supports(container)) {
            ItemStack stack = (ItemStack) container;
            Optional<Set<BlockType>> old = getVal(stack);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            if (set(stack, ImmutableSet.<BlockType>of())) {
                return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
            }
            return DataTransactionResult.builder().result(DataTransactionResult.Type.ERROR).build();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value.Mutable<Set<BlockType>> constructMutableValue(Set<BlockType> actualValue) {
        return new SpongeMutableSetValue<>(Keys.PLACEABLE_BLOCKS, actualValue);
    }

    @Override
    protected SetValue.Immutable<BlockType> constructImmutableValue(Set<BlockType> value) {
        return new SpongeImmutableSetValue<>(Keys.PLACEABLE_BLOCKS, value);
    }

}
