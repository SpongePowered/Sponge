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
package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;

import java.util.Locale;

public class SpongeKeyBuilder<E, V extends BaseValue<E>> implements Key.Builder<E, V> {

    TypeToken<V> valueToken;
    String id;
    String name;
    DataQuery query;

    @SuppressWarnings("unchecked")
    @Override
    public <T, B extends BaseValue<T>> Key.Builder<T, B> type(TypeToken<B> token) {
        this.valueToken = (TypeToken<V>) checkNotNull(token, "Value Token cannot be null!");
        return (Key.Builder<T, B>) this;
    }

    @Override
    public Key.Builder<E, V> id(String id) {
        checkState(this.valueToken != null, "Value Token must be set first!");
        checkArgument(!checkNotNull(id, "ID cannot be null!").contains(" "), "Id cannot contain spaces!");
        this.id = id.toLowerCase(Locale.ENGLISH);
        return this;
    }

    @Override
    public Key.Builder<E, V> name(String name) {
        checkState(this.valueToken != null, "Value Token must be set first!");
        checkArgument(!checkNotNull(name).isEmpty(), "Name cannot be empty!");
        this.name = name;
        return this;
    }

    @Override
    public Key.Builder<E, V> query(DataQuery query) {
        checkState(this.valueToken != null, "Value Token must be set first!");
        checkArgument(!query.getParts().isEmpty(), "DataQuery cannot be null!");
        this.query = query;
        return this;
    }

    @Override
    public Key<V> build() {
        checkState(this.valueToken != null, "Value Token must be set!");
        checkState(this.id != null, "Key id must be set!");
        checkState(this.query != null, "DataQuery not set!");
        checkState(this.name != null, "Name must be set");
        return new SpongeKey<>(this);
    }

    @Override
    public Key.Builder<E, V> reset() {
        this.valueToken = null;
        this.id = null;
        this.name = null;
        this.query = null;
        return this;
    }
}
