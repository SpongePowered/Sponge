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

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BookItemStackData {

    private BookItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.AUTHOR)
                        .get(h -> {
                            final CompoundNBT tag = h.getTag();
                            if (tag == null) {
                                return null;
                            }
                            return SpongeAdventure.legacySection(tag.getString(Constants.Item.Book.ITEM_BOOK_AUTHOR));
                        })
                        .set((h, v) -> h.setTagInfo(Constants.Item.Book.ITEM_BOOK_AUTHOR, StringNBT.valueOf(SpongeAdventure.legacySection(v))))
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.GENERATION)
                        .get(h -> {
                            final CompoundNBT tag = h.getTag();
                            if (tag == null) {
                                return null;
                            }
                            return tag.getInt(Constants.Item.Book.ITEM_BOOK_GENERATION);
                        })
                        .set((h, v) -> h.setTagInfo(Constants.Item.Book.ITEM_BOOK_GENERATION, IntNBT.valueOf(v)))
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.PAGES)
                        .get(h -> get(h, iv -> GsonComponentSerializer.gson().deserialize(iv)))
                        .setAnd((h, v) -> set(h, v, ih -> GsonComponentSerializer.gson().serialize(ih)))
                        .deleteAnd(BookItemStackData::delete)
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.PLAIN_PAGES)
                        .get(h -> get(h, iv -> iv))
                        .setAnd((h, v) -> set(h, v, iv -> iv))
                        .deleteAnd(BookItemStackData::delete)
                        .supports(h -> h.getItem() == Items.WRITABLE_BOOK);
    }
    // @formatter:on

    private static <V> List<V> get(final ItemStack holder, final Function<String, V> predicate) {
        final CompoundNBT tag = holder.getTag();
        if (tag == null || !tag.contains(Constants.Item.Book.ITEM_BOOK_PAGES)) {
            return null;
        }
        final ListNBT list = tag.getList(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_STRING);
        return list.stream()
                .map(INBT::getString)
                .map(predicate)
                .collect(Collectors.toList());
    }

    private static <V> boolean set(final ItemStack holder, final List<V> value, final Function<V, String> predicate) {
        final ListNBT list = value.stream()
                .map(predicate)
                .collect(NbtCollectors.toStringTagList());

        holder.setTagInfo(Constants.Item.Book.ITEM_BOOK_PAGES, list);
        final CompoundNBT compound = holder.getOrCreateTag();
        if (!compound.contains(Constants.Item.Book.ITEM_BOOK_TITLE)) {
            compound.putString(Constants.Item.Book.ITEM_BOOK_TITLE, Constants.Item.Book.INVALID_TITLE);
        }
        if (!compound.contains(Constants.Item.Book.ITEM_BOOK_AUTHOR)) {
            compound.putString(Constants.Item.Book.ITEM_BOOK_AUTHOR, Constants.Item.Book.INVALID_TITLE);
        }
        compound.putBoolean(Constants.Item.Book.ITEM_BOOK_RESOLVED, true);
        return false;
    }

    private static boolean delete(final ItemStack holder) {
        final CompoundNBT tag = holder.getTag();
        if (tag != null && tag.contains(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_LIST)) {
            tag.remove(Constants.Item.Book.ITEM_BOOK_PAGES);
            return true;
        }
        return false;
    }
}
