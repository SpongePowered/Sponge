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
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePlainPagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PlainPagedData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableListData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePlainPagedData;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;

public class ImmutableSpongePlainPagedData extends AbstractImmutableListData<String, ImmutablePlainPagedData, PlainPagedData>
        implements ImmutablePlainPagedData {

    public ImmutableSpongePlainPagedData() {
        this(ImmutableList.of(""));
    }

    public ImmutableSpongePlainPagedData(List<String> pages) {
        super(ImmutablePlainPagedData.class, pages, Keys.PLAIN_BOOK_PAGES, SpongePlainPagedData.class);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(Keys.PLAIN_BOOK_PAGES.getQuery(), this.getValue());
    }
}
