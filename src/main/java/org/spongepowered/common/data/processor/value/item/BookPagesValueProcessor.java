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
package org.spongepowered.common.data.processor.value.item;

import static org.spongepowered.common.item.ItemsHelper.getTagCompound;

import com.google.common.collect.ImmutableList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Optional;

public class BookPagesValueProcessor extends AbstractSpongeValueProcessor<List<Text>, ListValue<Text>> {

    public BookPagesValueProcessor() {
        super(Keys.BOOK_PAGES);

    }

    @Override
    protected ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<Text>(Keys.BOOK_PAGES, defaultValue);
    }

    @Override
    public Optional<List<Text>> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            final ItemStack itemStack = (ItemStack) container;
            if (!itemStack.hasTagCompound() || !itemStack.getTagCompound().hasKey(NbtDataUtil.ITEM_BOOK_PAGES)) {
                return Optional.empty();
            }
            return Optional.of(NbtDataUtil.getPagesFromNBT(getTagCompound(itemStack)));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            return ((ItemStack) container).getItem() == Items.writable_book || ((ItemStack) container).getItem() == Items.written_book;
        } else {
            return false;
        }
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, List<Text> value) {
        final ImmutableListValue<Text> pages = new ImmutableSpongeListValue<Text>(Keys.BOOK_PAGES, ImmutableList.copyOf(value));
        if (this.supports(container)) {
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> oldPages =
                        new ImmutableSpongeListValue<Text>(Keys.BOOK_PAGES, ImmutableList.copyOf(oldData.get()));
                builder.replace(oldPages);
            }
            NbtDataUtil.setPagesToNBT((ItemStack) container, value);
            builder.success(pages);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionBuilder.failResult(pages);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> pages = new ImmutableSpongeListValue<Text>(Keys.BOOK_PAGES, ImmutableList.copyOf(oldData.get()));
                builder.replace(pages);
            }
            NbtDataUtil.removePagesFromNBT((ItemStack) container);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionBuilder.failNoData();
    }

}
