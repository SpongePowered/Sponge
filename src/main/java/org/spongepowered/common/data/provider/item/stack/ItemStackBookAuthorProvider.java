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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemStackBookAuthorProvider extends ItemStackDataProvider<Text> {

    public ItemStackBookAuthorProvider() {
        super(Keys.BOOK_AUTHOR);
    }

    @Override
    protected boolean supports(Item item) {
        return item == Items.WRITTEN_BOOK;
    }

    @Override
    protected Optional<Text> getFrom(final ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag == null) {
            return Optional.empty();
        }
        final String legacy = tag.getString(Constants.Item.Book.ITEM_BOOK_AUTHOR);
        return Optional.of(SpongeTexts.fromLegacy(legacy));
    }

    @Override
    protected boolean set(final ItemStack dataHolder, final Text value) {
        dataHolder.setTagInfo(Constants.Item.Book.ITEM_BOOK_AUTHOR, new StringNBT(SpongeTexts.toLegacy(value)));
        return true;
    }
}
