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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableAuthorData;
import org.spongepowered.api.data.manipulator.mutable.item.AuthorData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeAuthorData;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeAuthorData extends AbstractImmutableSingleData<Text, ImmutableAuthorData, AuthorData> implements ImmutableAuthorData {

    public ImmutableSpongeAuthorData(Text value) {
        super(ImmutableAuthorData.class, value, Keys.BOOK_AUTHOR);
    }

    @Override
    public ImmutableAuthorData copy() {
        return this;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.BOOK_AUTHOR.getQuery(), Texts.json().to(this.getValue()));
    }

    @Override
    public ImmutableValue<Text> author() {
        return new ImmutableSpongeValue<Text>(Keys.BOOK_AUTHOR, Texts.of(NbtDataUtil.INVALID_TITLE), this.getValue());
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return author();
    }

    @Override
    public AuthorData asMutable() {
        return new SpongeAuthorData(this.getValue());
    }

    @Override
    public int compareTo(ImmutableAuthorData o) {
        return ComparisonChain.start()
                .compare(Texts.json().to(o.get(Keys.BOOK_AUTHOR).get()), Texts.json().to(this.getValue()))
                .result();
    }

}
