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

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class AbstractItemBookPagesProcessor<T, M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> extends
    AbstractItemSingleDataProcessor<List<T>, ListValue<T>, M, I> {


    AbstractItemBookPagesProcessor(final Predicate<ItemStack> predicate, final Key<ListValue<T>> key) {
        super(predicate, key);
    }

    abstract NBTTagString translateTo(T type);
    abstract T translateFrom(String page);

    @SuppressWarnings("unchecked")
    @Override
    public Optional<M> fill(final DataContainer container, final M pagedData) {
        final List<T> pages = DataUtil.getData(container, this.key, (Class<List<String>>) (Class<?>) List.class).stream()
            .map(this::translateFrom)
            .collect(Collectors.toList());
        return Optional.of(pagedData.set(this.key, pages));
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (supports(container)) {
            final ItemStack stack = (ItemStack) container;
            final Optional<List<T>> old = getVal(stack);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            final NBTTagCompound tag = stack.func_77978_p();
            if (tag != null) {
                tag.func_74782_a(Constants.Item.Book.ITEM_BOOK_PAGES, new NBTTagList());
            }
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }


    @Override
    protected boolean set(final ItemStack itemStack, final List<T> value) {
        final NBTTagList list = new NBTTagList();
        for (final T page : value) {
            list.func_74742_a(this.translateTo(page));
        }
        itemStack.func_77983_a(Constants.Item.Book.ITEM_BOOK_PAGES, list);
        final NBTTagCompound compound = itemStack.func_77978_p();
        if (!compound.func_74764_b(Constants.Item.Book.ITEM_BOOK_TITLE)) {
            compound.func_74778_a(Constants.Item.Book.ITEM_BOOK_TITLE, Constants.Item.Book.INVALID_TITLE);
        }
        if (!compound.func_74764_b(Constants.Item.Book.ITEM_BOOK_AUTHOR)) {
            compound.func_74778_a(Constants.Item.Book.ITEM_BOOK_AUTHOR, Constants.Item.Book.INVALID_TITLE);
        }
        compound.func_74757_a(Constants.Item.Book.ITEM_BOOK_RESOLVED, true);
        return true;
    }

    @Override
    protected Optional<List<T>> getVal(final ItemStack itemStack) {
        final NBTTagCompound tagCompound = itemStack.func_77978_p();
        if (tagCompound == null || !tagCompound.func_74764_b(Constants.Item.Book.ITEM_BOOK_PAGES)) {
            return Optional.empty();
        }
        final NBTTagList list = tagCompound.func_150295_c(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_STRING);
        final List<T> stringList = new ArrayList<>();
        if (!list.func_82582_d()) {
            for (int i = 0; i < list.func_74745_c(); i++) {
                stringList.add(this.translateFrom(list.func_150307_f(i)));
            }
        }
        return Optional.of(stringList);
    }

    @Override
    protected ListValue<T> constructValue(final List<T> actualValue) {
        return new SpongeListValue<>(this.key, actualValue);
    }

    @Override
    protected ImmutableValue<List<T>> constructImmutableValue(final List<T> value) {
        return new ImmutableSpongeListValue<>(this.key, ImmutableList.copyOf(value));
    }
}
