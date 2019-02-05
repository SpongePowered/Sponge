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

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableGenerationData;
import org.spongepowered.api.data.manipulator.mutable.GenerationData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGenerationData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.SpongeImmutableValue;

import java.util.Optional;

public final class GenerationDataProcessor
        extends AbstractItemSingleDataProcessor<Integer, GenerationData, ImmutableGenerationData> {

    public GenerationDataProcessor() {
        super(stack -> stack.getItem().equals(Items.WRITTEN_BOOK), Keys.GENERATION);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value.Mutable<Integer> constructMutableValue(Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.GENERATION)
                .value(actualValue)
                .defaultValue(0)
                .minimum(0)
                .maximum(DataConstants.MAXIMUM_GENERATION)
                .build();
    }

    @Override
    protected boolean set(ItemStack stack, Integer value) {
        NbtDataUtil.getOrCreateCompound(stack).setInteger(NbtDataUtil.ITEM_BOOK_GENERATION, value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(ItemStack stack) {
        return Optional.of(NbtDataUtil.getItemCompound(stack).map(tag -> tag.getInteger(NbtDataUtil.ITEM_BOOK_GENERATION)).orElse(0));
    }

    @Override
    protected Value.Immutable<Integer> constructImmutableValue(Integer value) {
        return SpongeImmutableValue.cachedOf(Keys.GENERATION, value);
    }

    @Override
    protected GenerationData createManipulator() {
        return new SpongeGenerationData();
    }

}
