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
package org.spongepowered.common.data.builder.manipulator.mutable.item;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.List;

public class PagedDataBuilder implements DataManipulatorBuilder<PagedData, ImmutablePagedData> {

    @Override
    public PagedData create() {
        return new SpongePagedData();
    }

    @Override
    public Optional<PagedData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() != Items.writable_book || ((ItemStack) dataHolder).getItem() != Items.written_book) {
                return Optional.absent();
            }
            final NBTTagList pageList = ((ItemStack) dataHolder).getTagCompound().getTagList(NbtDataUtil.PAGES, NbtDataUtil.TAG_STRING);
            final List<Text> pages = Lists.newArrayList();
            for (int i = 0; i < pageList.tagCount(); i++) {
                pages.add(Texts.legacy().fromUnchecked(pageList.getStringTagAt(i)));
            }
            return Optional.<PagedData>of(new SpongePagedData(pages));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<PagedData> build(DataView container) throws InvalidDataException {
        DataUtil.checkDataExists(container, Keys.ITEM_LORE.getQuery());
        final List<String> lines = container.getStringList(Keys.BOOK_PAGES.getQuery()).get();
        final List<Text> textLines = Lists.newArrayList();
        try {
            for (int i = 0; i < lines.size(); i++) {
                textLines.set(i, Texts.json().fromUnchecked(lines.get(i)));
            }
        } catch (Exception e) {
            throw new InvalidDataException("Could not deserialize text json lines", e);
        }
        return Optional.<PagedData>of(new SpongePagedData(textLines));
    }

}
