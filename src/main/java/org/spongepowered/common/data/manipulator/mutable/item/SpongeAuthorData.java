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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableAuthorData;
import org.spongepowered.api.data.manipulator.mutable.item.AuthorData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeAuthorData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeAuthorData extends AbstractSingleData<Text, AuthorData, ImmutableAuthorData> implements AuthorData {

    public SpongeAuthorData(Text value) {
        super(AuthorData.class, value, Keys.BOOK_AUTHOR);
    }

    public SpongeAuthorData() {
        this(DataConstants.EMPTY_TEXT);
    }

    @Override
    public AuthorData copy() {
        return new SpongeAuthorData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.BOOK_AUTHOR.getQuery(), Texts.json().to(this.getValue()));
    }

    @Override
    public Value<Text> author() {
        return new SpongeValue<>(Keys.BOOK_AUTHOR, DataConstants.EMPTY_TEXT, this.getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return author();
    }

    @Override
    public ImmutableAuthorData asImmutable() {
        return new ImmutableSpongeAuthorData(this.getValue());
    }

    @Override
    public int compareTo(AuthorData o) {
        return ComparisonChain.start()
                .compare(Texts.json().to(o.get(Keys.BOOK_AUTHOR).get()), Texts.json().to(this.getValue()))
                .result();
    }

}
