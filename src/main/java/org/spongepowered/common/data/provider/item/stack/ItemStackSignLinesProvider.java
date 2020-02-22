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

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ItemStackSignLinesProvider extends ItemStackDataProvider<List<Text>> {

    public ItemStackSignLinesProvider() {
        super(Keys.SIGN_LINES);
    }

    @Override
    protected Optional<List<Text>> getFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        if (tag == null) {
            return Optional.empty();
        }
        final String id = tag.getString(Constants.Item.BLOCK_ENTITY_ID);
        if (!id.equalsIgnoreCase(Constants.TileEntity.SIGN)) {
            return Optional.empty();
        }
        final List<Text> texts = Lists.newArrayListWithCapacity(4);
        for (int i = 0; i < 4; i++) {
            texts.add(SpongeTexts.fromLegacy(tag.getString("Text" + (i + 1))));
        }
        return Optional.of(texts);
    }

    @Override
    protected boolean set(ItemStack dataHolder, List<Text> value) {
        final CompoundNBT tag = dataHolder.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        tag.putString(Constants.Item.BLOCK_ENTITY_ID, Constants.TileEntity.SIGN);
        for (int i = 0; i < 4; i++) {
            final Text line = value.size() > i ? value.get(i) : Text.empty();
            if (line == null) {
                throw new IllegalArgumentException("A null line was given at index " + i);
            }
            tag.putString("Text" + (i + 1), TextSerializers.JSON.get().serialize(line));
        }
        return true;
    }

    @Override
    protected boolean delete(ItemStack dataHolder) {
        dataHolder.removeChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        return true;
    }
}
