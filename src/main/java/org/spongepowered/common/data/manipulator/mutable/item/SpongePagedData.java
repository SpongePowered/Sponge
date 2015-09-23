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
package org.spongepowered.common.data.manipulator.mutable.item;

import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongePagedData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

import java.util.ArrayList;
import java.util.List;

public class SpongePagedData extends AbstractData<PagedData, ImmutablePagedData> implements PagedData {

    private List<Text> pages;

    public SpongePagedData() {
        this(new ArrayList<Text>());
    }

    public SpongePagedData(List<Text> pages) {
        super(PagedData.class);
        this.pages = pages;
        registerGettersAndSetters();
    }

    @Override
    public ListValue<Text> pages() {
        return new SpongeListValue<Text>(Keys.BOOK_PAGES, pages);
    }

    @Override
    public PagedData copy() {
        return new SpongePagedData(pages);
    }

    @Override
    public ImmutablePagedData asImmutable() {
        return new ImmutableSpongePagedData(pages);
    }

    @Override
    public int compareTo(PagedData o) {
        return Booleans.compare(o.pages().containsAll(this.pages),
                this.pages.containsAll(o.pages().get()));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.BOOK_PAGES.getQuery(), this.asJson(this.pages));
    }

    private List<String> asJson(List<Text> pages) {
        List<String> jsonLines = Lists.newArrayList();
        for (Text line : pages) {
            jsonLines.add(Texts.json().to(line));
        }
        return jsonLines;
    }

    public List<Text> getPages() {
        return this.pages;
    }

    public void setPages(List<Text> pages) {
        this.pages = pages;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.BOOK_PAGES, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getPages();
            }
        });
        registerFieldSetter(Keys.BOOK_PAGES, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setPages((List<Text>) value);
            }
        });
        registerKeyValue(Keys.BOOK_PAGES, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return pages();
            }
        });
    }

}
