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
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePlaceableData;
import org.spongepowered.api.data.manipulator.mutable.item.PlaceableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePlaceableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.processor.common.BreakablePlaceableUtils;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;

import java.util.Optional;
import java.util.Set;

public class PlaceableDataProcessor
        extends AbstractItemSingleDataProcessor<Set<BlockType>, SetValue<BlockType>, PlaceableData, ImmutablePlaceableData> {

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
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<PlaceableData> old = from(dataHolder);
            try {
                if (set((ItemStack) dataHolder, ImmutableSet.<BlockType>of())) {
                    if (old.isPresent()) {
                        builder.replace(old.get().getValues());
                    }
                    return builder.result(DataTransactionResult.Type.SUCCESS).build();
                } else {
                    return builder.result(DataTransactionResult.Type.FAILURE).build();
                }
            } catch (Exception e) {
                SpongeImpl.getLogger().debug("An exception occurred when removing data: ", e);
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected ImmutableValue<Set<BlockType>> constructImmutableValue(Set<BlockType> value) {
        return new ImmutableSpongeSetValue<>(Keys.PLACEABLE_BLOCKS, value);
    }

}
