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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.NBTCollectors;

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
                            final CompoundTag tag = h.getTag();
                            if (tag == null) {
                                return null;
                            }
                            return LegacyComponentSerializer.legacySection().deserialize(tag.getString(Constants.Item.Book.ITEM_BOOK_AUTHOR));
                        })
                        .set((h, v) -> h.addTagElement(Constants.Item.Book.ITEM_BOOK_AUTHOR, StringTag.valueOf(LegacyComponentSerializer.legacySection().serialize(v))))
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.GENERATION)
                        .get(h -> {
                            final CompoundTag tag = h.getTag();
                            if (tag == null) {
                                return null;
                            }
                            return tag.getInt(Constants.Item.Book.ITEM_BOOK_GENERATION);
                        })
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.addTagElement(Constants.Item.Book.ITEM_BOOK_GENERATION, IntTag.valueOf(v));
                            return true;
                        })
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.PAGES)
                        .get(h -> BookItemStackData.get(h, iv -> GsonComponentSerializer.gson().deserialize(iv)))
                        .setAnd((h, v) -> BookItemStackData.set(h, v, ih -> GsonComponentSerializer.gson().serialize(ih)))
                        .deleteAnd(BookItemStackData::delete)
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.PLAIN_PAGES)
                        .get(h -> BookItemStackData.get(h, iv -> iv))
                        .setAnd((h, v) -> BookItemStackData.set(h, v, iv -> iv))
                        .deleteAnd(BookItemStackData::delete)
                        .supports(h -> h.getItem() == Items.WRITABLE_BOOK);
    }
    // @formatter:on

    private static <V> List<V> get(final ItemStack holder, final Function<String, V> predicate) {
        final CompoundTag tag = holder.getTag();
        if (tag == null || !tag.contains(Constants.Item.Book.ITEM_BOOK_PAGES)) {
            return null;
        }
        final ListTag list = tag.getList(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_STRING);
        return list.stream()
                .map(Tag::getAsString)
                .map(predicate)
                .collect(Collectors.toList());
    }

    private static <V> boolean set(final ItemStack holder, final List<V> value, final Function<V, String> predicate) {
        final ListTag list = value.stream()
                .map(predicate)
                .collect(NBTCollectors.toStringTagList());

        holder.addTagElement(Constants.Item.Book.ITEM_BOOK_PAGES, list);
        final CompoundTag compound = holder.getOrCreateTag();
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
        final CompoundTag tag = holder.getTag();
        if (tag != null && tag.contains(Constants.Item.Book.ITEM_BOOK_PAGES, Constants.NBT.TAG_LIST)) {
            tag.remove(Constants.Item.Book.ITEM_BOOK_PAGES);
            return true;
        }
        return false;
    }
}
