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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.Optional;

public final class SpongeKeyValueMatcherBuilder<V> implements KeyValueMatcher.Builder<V> {

    private KeyValueMatcher.Operator operator = KeyValueMatcher.Operator.EQUAL;
    private @Nullable Key<? extends Value<V>> key;
    private @Nullable V value;

    @Override
    public <NV> KeyValueMatcher.Builder<NV> key(final Key<? extends Value<NV>> key) {
        Objects.requireNonNull(key, "key");
        this.key = (Key<? extends Value<V>>) key;
        return (KeyValueMatcher.Builder<NV>) this;
    }

    @Override
    public KeyValueMatcher.Builder<V> operator(final KeyValueMatcher.Operator operator) {
        Objects.requireNonNull(operator, "operator");
        this.operator = operator;
        return this;
    }

    @Override
    public KeyValueMatcher.Builder<V> value(final @Nullable V value) {
        this.value = value;
        return this;
    }

    @Override
    public KeyValueMatcher.Builder<V> value(final  @Nullable Value<? extends V> value) {
        this.value = value == null ? null : value.get();
        return this;
    }

    @Override
    public KeyValueMatcher.Builder<V> from(final KeyValueMatcher<V> value) {
        this.key = value.getKey();
        this.value = value.getValue().orElse(null);
        this.operator = value.getOperator();
        return this;
    }

    @Override
    public KeyValueMatcher.Builder<V> reset() {
        this.key = null;
        this.operator = KeyValueMatcher.Operator.EQUAL;
        this.value = null;
        return this;
    }

    @Override
    public KeyValueMatcher<V> build() {
        Objects.requireNonNull(this.key, "The key must be set");
        return new SpongeKeyValueMatcher<>(this.key, this.operator, this.value);
    }

    @Override
    public Optional<KeyValueMatcher<V>> build(final DataView container) throws InvalidDataException {
        Objects.requireNonNull(container, "container");
        final Optional<Key> key = container.getCatalogType(Constants.KeyValueMatcher.KEY, Key.class);
        if (!key.isPresent()) {
            return Optional.empty();
        }
        final Optional<KeyValueMatcher.Operator> operator = container.getString(Constants.KeyValueMatcher.OPERATOR)
                .map(s -> KeyValueMatcher.Operator.valueOf(s.toUpperCase()));
        if (!operator.isPresent()) {
            return Optional.empty();
        }
        final Optional<Object> value = container.getObject(
                Constants.KeyValueMatcher.VALUE, key.get().getElementToken().getRawType());
        if (!value.isPresent()) {
            return Optional.empty();
        }
        final KeyValueMatcher<V> keyValueMatcher = new SpongeKeyValueMatcher<>(
                key.get(), operator.get(), value.orElse(null));
        return Optional.of(keyValueMatcher);
    }
}
