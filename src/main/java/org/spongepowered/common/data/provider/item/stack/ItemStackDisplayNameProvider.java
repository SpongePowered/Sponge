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
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemStackDisplayNameProvider extends GenericMutableDataProvider<ItemStack, Text> {

    public ItemStackDisplayNameProvider() {
        super(Keys.DISPLAY_NAME);
    }

    @Override
    protected Optional<Text> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem() == Items.WRITTEN_BOOK) {
            @Nullable final CompoundNBT tag = dataHolder.getTag();
            if (tag != null) {
                final String title = tag.getString(Constants.Item.Book.ITEM_BOOK_TITLE);
                return Optional.of(SpongeTexts.fromLegacy(title));
            }
        }

        /*
        @Nullable final CompoundNBT display = dataHolder.getChildTag(Constants.Item.ITEM_DISPLAY);
        if (display == null || !display.contains(Constants.Item.ITEM_DISPLAY_NAME, Constants.NBT.TAG_STRING)) {
            return Optional.empty();
        }
        */

        return Optional.of(SpongeTexts.toText(dataHolder.getDisplayName()));
    }

    @Override
    protected boolean set(ItemStack dataHolder, Text value) {
        if (dataHolder.getItem() == Items.WRITTEN_BOOK) {
            final String legacy = SpongeTexts.toLegacy(value);
            dataHolder.setTagInfo(Constants.Item.Book.ITEM_BOOK_TITLE, new StringNBT(legacy));
        } else {
            dataHolder.setDisplayName(SpongeTexts.toComponent(value));
        }
        return true;
    }

    @Override
    protected boolean removeFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT display = dataHolder.getChildTag(Constants.Item.ITEM_DISPLAY);
        if (display != null) {
            display.remove(Constants.Item.ITEM_DISPLAY_NAME);
        }
        return true;
    }
}
