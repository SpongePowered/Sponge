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
import org.spongepowered.api.data.manipulator.immutable.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.mutable.PagedData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.SpongeImmutableListValue;
import org.spongepowered.common.data.value.SpongeMutableListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class ItemPagedDataProcessor extends AbstractItemSingleDataProcessor<List<Text>, PagedData, ImmutablePagedData> {

    public ItemPagedDataProcessor() {
        super(input -> input.getItem() == Items.WRITABLE_BOOK || input.getItem() == Items.WRITTEN_BOOK, Keys.BOOK_PAGES);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<PagedData> fill(DataContainer container, PagedData pagedData) {
        final List<String> json = DataUtil.getData(container, Keys.BOOK_PAGES, List.class);
        return Optional.of(pagedData.set(Keys.BOOK_PAGES, SpongeTexts.fromJson(json)));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (supports(container)) {
            ItemStack stack = (ItemStack) container;
            Optional<List<Text>> old = getVal(stack);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            NbtDataUtil.removePagesFromNBT(stack);
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected PagedData createManipulator() {
        return new SpongePagedData();
    }

    @Override
    protected boolean set(ItemStack itemStack, List<Text> value) {
        NbtDataUtil.setPagesToNBT(itemStack, value);
        return true;
    }

    @Override
    protected Optional<List<Text>> getVal(ItemStack itemStack) {
        if (!itemStack.hasTag() || !itemStack.getTag().contains(NbtDataUtil.ITEM_BOOK_PAGES)) {
            return Optional.empty();
        }
        return Optional.of(NbtDataUtil.getPagesFromNBT(getTagCompound(itemStack)));
    }

    @Override
    protected Value.Mutable<List<Text>> constructMutableValue(List<Text> actualValue) {
        return new SpongeMutableListValue<>(Keys.BOOK_PAGES, actualValue);
    }

    @Override
    protected Value.Immutable<List<Text>> constructImmutableValue(List<Text> value) {
        return new SpongeImmutableListValue<>(Keys.BOOK_PAGES, ImmutableList.copyOf(value));
    }

}
