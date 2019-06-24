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

import static org.spongepowered.common.item.inventory.util.ItemStackUtil.getTagCompound;

import com.google.common.collect.ImmutableList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePlainPagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PlainPagedData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePlainPagedData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ItemPlainPagedDataProcessor extends AbstractItemSingleDataProcessor<List<String>, ListValue<String>, PlainPagedData,
        ImmutablePlainPagedData> {

    public ItemPlainPagedDataProcessor() {
        super(input -> input.getItem() == Items.WRITABLE_BOOK, Keys.PLAIN_BOOK_PAGES);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<PlainPagedData> fill(DataContainer container, PlainPagedData pagedData) {
        final List<String> pages = DataUtil.getData(container, Keys.PLAIN_BOOK_PAGES, List.class);
        return Optional.of(pagedData.set(Keys.PLAIN_BOOK_PAGES, pages));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (supports(container)) {
            ItemStack stack = (ItemStack) container;
            Optional<List<String>> old = getVal(stack);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            NbtDataUtil.removePagesFromNBT(stack);
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected PlainPagedData createManipulator() {
        return new SpongePlainPagedData();
    }

    @Override
    protected boolean set(ItemStack itemStack, List<String> value) {
        NbtDataUtil.setPlainPagesToNBT(itemStack, value);
        return true;
    }

    @Override
    protected Optional<List<String>> getVal(ItemStack itemStack) {
        if (!itemStack.hasTagCompound() || !itemStack.getTagCompound().hasKey(Constants.Item.Book.ITEM_BOOK_PAGES)) {
            return Optional.empty();
        }
        return Optional.of(NbtDataUtil.getPlainPagesFromNBT(getTagCompound(itemStack)));
    }

    @Override
    protected ListValue<String> constructValue(List<String> actualValue) {
        return new SpongeListValue<>(Keys.PLAIN_BOOK_PAGES, actualValue);
    }

    @Override
    protected ImmutableValue<List<String>> constructImmutableValue(List<String> value) {
        return new ImmutableSpongeListValue<>(Keys.PLAIN_BOOK_PAGES, ImmutableList.copyOf(value));
    }

}
