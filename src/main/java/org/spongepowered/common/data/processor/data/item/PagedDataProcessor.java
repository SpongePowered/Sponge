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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.item.ItemsHelper.getTagCompound;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongePagedData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.List;

public class PagedDataProcessor extends AbstractSpongeDataProcessor<PagedData, ImmutablePagedData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            return ((ItemStack) dataHolder).getItem() == Items.writable_book || ((ItemStack) dataHolder).getItem() == Items.written_book;
        } else {
            return false;
        }
    }

    @Override
    public Optional<PagedData> from(DataHolder dataHolder) {
        if (this.supports(dataHolder)) {
            if (dataHolder.get(Keys.BOOK_PAGES).isPresent()) {
                return Optional.<PagedData>of(new SpongePagedData(NbtDataUtil.getPagesFromNBT(getTagCompound((ItemStack) dataHolder))));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<PagedData> createFrom(DataHolder dataHolder) {
        if (this.supports(dataHolder)) {
            return Optional.<PagedData>of(new SpongePagedData(NbtDataUtil.getPagesFromNBT(getTagCompound((ItemStack) dataHolder))));
        }
        return Optional.absent();
    }

    @Override
    public Optional<PagedData> fill(DataHolder dataHolder, PagedData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final PagedData data = from(dataHolder).orNull();
            final PagedData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final List<Text> pages = newData.get(Keys.BOOK_PAGES).get();
            return Optional.of(manipulator.set(Keys.BOOK_PAGES, pages));
        }
        return Optional.absent();
    }

    @Override
    public Optional<PagedData> fill(DataContainer container, PagedData pagedData) {
        final List<String> json = DataUtil.getData(container, Keys.BOOK_PAGES, List.class);
        final List<Text> pages = Lists.newArrayList();
        for (String str : json) {
            pages.add(Texts.json().fromUnchecked(str));
        }
        return Optional.of(pagedData.set(Keys.BOOK_PAGES, pages));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, PagedData manipulator, MergeFunction function) {
        if (!supports(dataHolder)) {
            return DataTransactionBuilder.failResult(manipulator.getValues());
        }
        try {
            Optional<PagedData> data = from(dataHolder);
            final PagedData newData = checkNotNull(function).merge(data.orNull(), manipulator);
            NbtDataUtil.setPagesToNBT((ItemStack) dataHolder, newData.pages().get());
            if (data.isPresent()) {
                return DataTransactionBuilder.successReplaceResult(newData.getValues(), data.get().getValues());
            } else {
                return DataTransactionBuilder.builder().success(newData.getValues()).build();
            }
        } catch (Exception e) {
            return DataTransactionBuilder.builder().reject(manipulator.getValues()).result(Type.ERROR).build();
        }
    }

    @Override
    public Optional<ImmutablePagedData> with(Key<? extends BaseValue<?>> key, Object value, ImmutablePagedData immutable) {
        if (key == Keys.BOOK_PAGES) {
            return Optional.<ImmutablePagedData>of(new ImmutableSpongePagedData(immutable.pages().get()));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<PagedData> data = from(dataHolder);
            if (data.isPresent()) {
                try {
                    NbtDataUtil.removePagesFromNBT((ItemStack) dataHolder);
                    return builder.replace(data.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue removing the pages from an itemstack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

}
