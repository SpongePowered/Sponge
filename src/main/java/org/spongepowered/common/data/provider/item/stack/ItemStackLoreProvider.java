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
import net.minecraft.nbt.ListNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ItemStackLoreProvider extends ItemStackDataProvider<List<Text>> {

    public ItemStackLoreProvider() {
        super(Keys.ITEM_LORE);
    }

    @Override
    protected Optional<List<Text>> getFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag == null || tag.contains(Constants.Item.ITEM_DISPLAY)) {
            return Optional.empty();
        }
        final ListNBT list = tag.getList(Constants.Item.ITEM_LORE, Constants.NBT.TAG_STRING);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(SpongeTexts.fromJson(list.stream().collect(NbtCollectors.toStringList())));
    }

    @Override
    protected boolean set(ItemStack dataHolder, List<Text> value) {
        if (value.isEmpty()) {
            delete(dataHolder);
        } else {
            final ListNBT list = SpongeTexts.asJsonNBT(value);
            dataHolder.getOrCreateChildTag(Constants.Item.ITEM_DISPLAY).put(Constants.Item.ITEM_LORE, list);
        }
        return true;
    }

    @Override
    protected boolean delete(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag != null && tag.contains(Constants.Item.ITEM_DISPLAY)) {
            tag.getCompound(Constants.Item.ITEM_DISPLAY).remove(Constants.Item.ITEM_LORE);
        }
        return true;
    }
}
