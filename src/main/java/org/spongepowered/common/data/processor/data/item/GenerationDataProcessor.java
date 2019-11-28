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

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.IntNBT;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGenerationData;
import org.spongepowered.api.data.manipulator.mutable.item.GenerationData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGenerationData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public final class GenerationDataProcessor
        extends AbstractItemSingleDataProcessor<Integer, MutableBoundedValue<Integer>, GenerationData, ImmutableGenerationData> {

    public GenerationDataProcessor() {
        super(stack -> stack.func_77973_b().equals(Items.field_151164_bB), Keys.GENERATION);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(final Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.GENERATION)
                .actualValue(actualValue)
                .defaultValue(0)
                .minimum(0)
                .maximum(Constants.Item.Book.MAXIMUM_GENERATION)
                .build();
    }

    @Override
    protected boolean set(final ItemStack stack, final Integer value) {
        stack.func_77983_a(Constants.Item.Book.ITEM_BOOK_GENERATION, new IntNBT(value));
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final ItemStack stack) {
        if (!stack.func_77942_o()) {
            return Optional.empty();
        }
        return Optional.of(stack.func_77978_p().func_74762_e(Constants.Item.Book.ITEM_BOOK_GENERATION));
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return ImmutableSpongeValue.cachedOf(Keys.GENERATION, 0, value);
    }

    @Override
    protected GenerationData createManipulator() {
        return new SpongeGenerationData();
    }

}
