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
package org.spongepowered.common.data.utils.items;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.item.ItemsHelper.getTagCompound;

import com.google.common.base.Optional;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.item.PagedData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulators.items.SpongePagedData;
import org.spongepowered.common.interfaces.text.IMixinText;

import java.util.List;
import java.util.Locale;

public class SpongePagesProcessor implements SpongeDataProcessor<PagedData> {

    private static final DataQuery PAGES = of("Pages");


    @Override
    public Optional<PagedData> fillData(DataHolder holder, PagedData manipulator, DataPriority priority) {
        if (holder instanceof ItemStack) {
            if (((ItemStack) holder).getItem() != Items.writable_book || ((ItemStack) holder).getItem() != Items.written_book) {
                return Optional.absent();
            }
            final NBTTagList pageList = ((ItemStack) holder).getTagCompound().getTagList("pages", 8);
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                    final int count = manipulator.getAll().size();
                    for (int i = 0; i < count; i++) {
                        manipulator.remove(i);
                    }
                    for (int i = 0; i < pageList.tagCount(); i++) {
                        manipulator.add(Texts.parseJson(pageList.getStringTagAt(i)));
                    }
                    return Optional.of(manipulator);
                case PRE_MERGE:
                    for (int i = 0; i < pageList.tagCount(); i++) {
                        manipulator.add(Texts.parseJson(pageList.getStringTagAt(i)));
                    }
                    return Optional.of(manipulator);
                case POST_MERGE:
                    for (int i = 0; i < pageList.tagCount(); i++) {
                        manipulator.add(0, Texts.parseJson(pageList.getStringTagAt(i)));
                    }
                    return Optional.of(manipulator);
                default:
                    return Optional.of(manipulator);
            }
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, PagedData manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack && (((ItemStack) dataHolder).getItem() == Items.writable_book
                || ((ItemStack) dataHolder).getItem() == Items.written_book)) {
            final NBTTagList loreList = new NBTTagList();
            for (Text text : manipulator.getAll()) {
                loreList.appendTag(new NBTTagString(((IMixinText) text).toLegacy('\247', Locale.ENGLISH)));
            }
            final NBTTagCompound compound = getTagCompound((ItemStack) dataHolder);
            compound.setTag("pages", loreList);
            if (!compound.hasKey("title")) {
                compound.setString("title", "invalid");
            }
            if (!compound.hasKey("author")) {
                compound.setString("author", "invalid");
            }
            compound.setBoolean("resolved", true);
            return successNoData();
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack)) {
            return false;
        } else {
            final ItemStack itemStack = ((ItemStack) dataHolder);
            final Item item = itemStack.getItem();
            if (item != Items.writable_book || item != Items.written_book) {
                return false;
            } else {
                final NBTTagList list = new NBTTagList();
                itemStack.getTagCompound().setTag("pages", list);
                return true;
            }
        }
    }

    @Override
    public Optional<PagedData> build(DataView container) throws InvalidDataException {
        if (!checkNotNull(container).contains(PAGES)) {
            throw new InvalidDataException("Missing pages to construct a PagedData.");
        }
        final List<String> pages = container.getStringList(PAGES).get();
        final PagedData data = create();
        for (String page : pages) {
            data.add(Texts.parseJson(page));
        }
        return Optional.of(data);
    }

    @Override
    public PagedData create() {
        return new SpongePagedData();
    }

    @Override
    public Optional<PagedData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack)) {
            return Optional.absent();
        }
        if ((((ItemStack) dataHolder).getItem() != Items.writable_book) || ((ItemStack) dataHolder).getItem() != Items.written_book) {
            return Optional.absent();
        }
        if (!((ItemStack) dataHolder).hasTagCompound()) {
            return Optional.absent();
        }
        final NBTTagList pageList = ((ItemStack) dataHolder).getTagCompound().getTagList("pages", 8);
        final PagedData data = create();
        for (int i = 0; i < pageList.tagCount(); i++) {
            data.add(Texts.parseJson(pageList.getStringTagAt(i)));
        }
        return Optional.of(data);
    }

    @Override
    public Optional<PagedData> getFrom(DataHolder holder) {
        return Optional.absent();
    }
}
