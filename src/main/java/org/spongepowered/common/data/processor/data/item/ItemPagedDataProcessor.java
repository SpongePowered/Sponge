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

import static org.spongepowered.common.item.ItemsHelper.getTagCompound;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class ItemPagedDataProcessor extends AbstractItemSingleDataProcessor<List<Text>, ListValue<Text>, PagedData, ImmutablePagedData>  {

    public ItemPagedDataProcessor() {
        super(new Predicate<ItemStack>(){

            @Override
            public boolean apply(ItemStack input) {
                return input.getItem() == Items.writable_book || input.getItem() == Items.written_book;
            }

        }, Keys.BOOK_PAGES);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<PagedData> fill(DataContainer container, PagedData pagedData) {
        final List<String> json = DataUtil.getData(container, Keys.BOOK_PAGES, List.class);
        return Optional.of(pagedData.set(Keys.BOOK_PAGES, SpongeTexts.fromJson(json)));
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<PagedData> data = from(dataHolder);
            if (data.isPresent()) {
                try {
                    NbtDataUtil.removePagesFromNBT((ItemStack) dataHolder);
                    return builder.replace(data.get().getValues()).result(Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue removing the pages from an itemstack!", e);
                    return builder.result(Type.ERROR).build();
                }
            } else {
                return builder.result(Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
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
        if (!itemStack.hasTagCompound() || !itemStack.getTagCompound().hasKey(NbtDataUtil.ITEM_BOOK_PAGES)) {
            return Optional.empty();
        }
        return Optional.of(NbtDataUtil.getPagesFromNBT(getTagCompound(itemStack)));
    }

    @Override
    protected ImmutableValue<List<Text>> constructImmutableValue(List<Text> value) {
        return new ImmutableSpongeListValue<Text>(Keys.BOOK_PAGES, ImmutableList.copyOf(value));
    }

}
