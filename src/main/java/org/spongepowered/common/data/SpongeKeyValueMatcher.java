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
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.util.Constants;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiPredicate;

public final class SpongeKeyValueMatcher<V> implements KeyValueMatcher<V> {

    private final Key<? extends Value<V>> key;
    private final Operator operator;
    private final @Nullable V value;

    public SpongeKeyValueMatcher(final Key<? extends Value<V>> key, final Operator operator, final @Nullable V value) {
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public Key<? extends Value<V>> key() {
        return this.key;
    }

    @Override
    public Operator operator() {
        return this.operator;
    }

    @Override
    public Optional<V> value() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public boolean matches(final @Nullable V value) {
        switch (this.operator) {
            case EQUAL:
                return this.compare(value) == 0;
            case NOT_EQUAL:
                return this.compare(value) != 0;
            case GREATER:
                return this.compare(value) > 0;
            case GREATER_OR_EQUAL:
                return this.compare(value) >= 0;
            case LESS:
                return this.compare(value) < 0;
            case LESS_OR_EQUAL:
                return this.compare(value) <= 0;
            case INCLUDES:
                return this.includes(value);
            case EXCLUDES:
                return !this.includes(value);
        }
        throw new IllegalStateException("Unknown operator: " + this.operator);
    }

    @SuppressWarnings("unchecked")
    private boolean includes(final @Nullable V value) {
        if (this.value == null || value == null) {
            return false;
        }
        final BiPredicate<V, V> predicate = (BiPredicate<V, V>) this.key.elementIncludesTester();
        return predicate.test(this.value, value);
    }

    @SuppressWarnings("unchecked")
    private int compare(final @Nullable V value) {
        if (this.value == null && value == null) {
            return 0;
        }
        if (this.value != null && value == null) {
            return 1;
        }
        if (this.value == null) {
            return -1;
        }
        final Comparator<V> comparator = (Comparator<V>) this.key.elementComparator();
        return -comparator.compare(this.value, value);
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Constants.KeyValueMatcher.KEY, this.key)
                .set(Constants.KeyValueMatcher.OPERATOR, this.operator.toString().toLowerCase());
        if (this.value != null) {
            container.set(Constants.KeyValueMatcher.VALUE, this.value);
        }
        return container;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeKeyValueMatcher<?> that = (SpongeKeyValueMatcher<?>) o;
        return this.key.equals(that.key) &&
                this.operator == that.operator &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.operator, this.value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeKeyValueMatcher.class.getSimpleName() + "[", "]")
                .add("key=" + this.key)
                .add("operator=" + this.operator)
                .add("value=" + this.value)
                .toString();
    }
}
