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
package org.spongepowered.common.data.manipulator;


import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.util.CopyHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

@SuppressWarnings("unchecked")
abstract class SpongeDataManipulator implements DataManipulator {

    protected final Map<Key<?>, Object> values;

    SpongeDataManipulator(final Map<Key<?>, Object> values) {
        this.values = values;
    }

    Map<Key<?>, Object> copyMap() {
        final Map<Key<?>, Object> copy = new HashMap<>();
        for (final Map.Entry<Key<?>, Object> entry : this.values.entrySet()) {
            copy.put(entry.getKey(), CopyHelper.copy(entry.getValue()));
        }
        return copy;
    }

    @Override
    public <E> Optional<E> get(final Key<? extends Value<E>> key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable((E) CopyHelper.copy(this.values.get(key)));
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
        Objects.requireNonNull(key, "key");
        final E element = (E) CopyHelper.copy(this.values.get(key));
        return element == null ? Optional.empty() : Optional.of(Value.genericMutableOf(key, element));
    }

    @Override
    public boolean supports(final Key<?> key) {
        return true;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Set<Value.Immutable<?>> getValues() {
        return this.values.entrySet().stream()
                .map(entry -> (Value.Immutable<?>)Value.immutableOf((Key) entry.getKey(), CopyHelper.copy(entry.getValue())).asImmutable())
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public String toString() {
        final StringJoiner builder = new StringJoiner(", ", SpongeDataManipulator.class.getSimpleName() + "[", "]");
        for (final Map.Entry<Key<?>, Object> entry : this.values.entrySet()) {
            builder.add(entry.getKey().key() + "= " + entry.getValue());
        }
        return builder.toString();
    }
}
