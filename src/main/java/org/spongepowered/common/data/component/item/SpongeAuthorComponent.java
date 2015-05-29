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
package org.spongepowered.common.data.component.item;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.item.AuthorComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.component.AbstractSingleValueComponent;

public class SpongeAuthorComponent extends AbstractSingleValueComponent<Text, AuthorComponent> implements AuthorComponent {

    public SpongeAuthorComponent() {
        super(AuthorComponent.class, Texts.of());
    }

    @Override
    public AuthorComponent copy() {
        return new SpongeAuthorComponent().setValue(this.getValue());
    }

    @Override
    public AuthorComponent reset() {
        return setValue(Texts.of());
    }

    @Override
    public int compareTo(AuthorComponent o) {
        return Texts.toPlain(getValue()).compareTo(Texts.toPlain(o.getValue()));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Tokens.BOOK_AUTHOR.getQuery(), getValue());
    }
}