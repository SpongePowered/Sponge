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
package org.spongepowered.common.data.manipulator.immutable.item;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Booleans;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.GetterFunction;

import java.util.List;

public class ImmutableSpongePagedData extends AbstractImmutableData<ImmutablePagedData, PagedData> implements ImmutablePagedData {

    private final ImmutableList<Text> pages;

    public ImmutableSpongePagedData() {
        this(ImmutableList.of(Texts.of()));
    }

    public ImmutableSpongePagedData(List<Text> pages) {
        super(ImmutablePagedData.class);
        this.pages = ImmutableList.copyOf(pages);
        registerGetters();
    }

    @Override
    public ImmutableListValue<Text> pages() {
        return new ImmutableSpongeListValue<>(Keys.BOOK_PAGES, this.pages);
    }

    @Override
    public PagedData asMutable() {
        return new SpongePagedData(this.pages);
    }

    @Override
    public int compareTo(ImmutablePagedData o) {
        return Booleans.compare(o.pages().containsAll(this.pages),
                this.pages.containsAll(o.pages().get()));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.BOOK_PAGES.getQuery(), SpongeTexts.asJson(this.pages));
    }

    public List<Text> getPages() {
        return this.pages;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.BOOK_PAGES, ImmutableSpongePagedData.this::getPages);
        registerKeyValue(Keys.BOOK_PAGES, ImmutableSpongePagedData.this::pages);
    }

}
