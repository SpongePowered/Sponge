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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractItemStackBookPagesProvider<T> extends ItemStackDataProvider<List<T>> {

    public AbstractItemStackBookPagesProvider(Supplier<? extends Key<? extends Value<List<T>>>> key) {
        super(key);
    }

    protected abstract String translateTo(T type);

    protected abstract T translateFrom(String page);

    @Override
    protected Optional<List<T>> getFrom(final ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag == null || !tag.contains(Constants.Item.Book.ITEM_BOOK_PAGES)) {
            return Optional.empty();
        }
        final ListNBT list = tag.getList(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_STRING);
        return Optional.of(list.stream()
                .map(INBT::getString)
                .map(this::translateFrom)
                .collect(Collectors.toList()));
    }

    @Override
    protected boolean set(final ItemStack dataHolder, final List<T> value) {
        final ListNBT list = value.stream()
                .map(this::translateTo)
                .collect(NbtCollectors.toStringTagList());

        dataHolder.setTagInfo(Constants.Item.Book.ITEM_BOOK_PAGES, list);
        final CompoundNBT compound = dataHolder.getOrCreateTag();
        if (!compound.contains(Constants.Item.Book.ITEM_BOOK_TITLE)) {
            compound.putString(Constants.Item.Book.ITEM_BOOK_TITLE, Constants.Item.Book.INVALID_TITLE);
        }
        if (!compound.contains(Constants.Item.Book.ITEM_BOOK_AUTHOR)) {
            compound.putString(Constants.Item.Book.ITEM_BOOK_AUTHOR, Constants.Item.Book.INVALID_TITLE);
        }
        compound.putBoolean(Constants.Item.Book.ITEM_BOOK_RESOLVED, true);
        return false;
    }

    @Override
    protected boolean delete(final ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag != null && tag.contains(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_LIST)) {
            tag.remove(Constants.Item.Book.ITEM_BOOK_PAGES);
            return true;
        }
        return false;
    }
}
