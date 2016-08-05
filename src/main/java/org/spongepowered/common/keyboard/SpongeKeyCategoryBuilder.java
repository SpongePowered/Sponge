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
package org.spongepowered.common.keyboard;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.keyboard.KeyCategory;
import org.spongepowered.api.text.Text;

public class SpongeKeyCategoryBuilder implements KeyCategory.Builder {

    private Text title;
    private String id;

    @Override
    public KeyCategory.Builder id(String id) {
        checkNotNull(id, "id");
        checkArgument(!id.isEmpty(), "The id may not be empty");
        checkArgument(id.indexOf(' ') == -1, "The id may not contain any spaces.");
        this.id = id;
        return this;
    }

    @Override
    public KeyCategory.Builder title(Text title) {
        this.title = checkNotNull(title, "title");
        return this;
    }

    @Override
    public KeyCategory build() {
        checkState(this.title != null, "The title must be set");
        checkState(this.id != null, "The id must be set");
        return new SpongeKeyCategory(this.id, this.title);
    }

    @Override
    public KeyCategory.Builder from(KeyCategory value) {
        this.id = value.getId();
        this.title = value.getTitle();
        return this;
    }

    @Override
    public KeyCategory.Builder reset() {
        this.id = null;
        this.title = null;
        return this;
    }
}
