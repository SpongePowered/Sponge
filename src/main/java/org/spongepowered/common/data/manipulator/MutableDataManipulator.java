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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.copy.CopyHelper;
import org.spongepowered.common.data.util.MergeHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
final class MutableDataManipulator extends SpongeDataManipulator implements DataManipulator.Mutable {

    MutableDataManipulator() {
        super(new HashMap<>());
    }

    MutableDataManipulator(final Map<Key<?>, Object> values) {
        super(values);
    }

    @Override
    public Mutable asMutableCopy() {
        return this.copy();
    }

    @Override
    public Immutable asImmutable() {
        return new ImmutableDataManipulator(Collections.unmodifiableMap(this.copyMap()));
    }

    @Override
    public Mutable copyFrom(final ValueContainer valueContainer, final MergeFunction overlap,
        final Predicate<Key<?>> predicate) {
        checkNotNull(valueContainer, "valueContainer");
        checkNotNull(predicate, "predicate");
        checkNotNull(overlap, "overlap");
        if (valueContainer instanceof SpongeDataManipulator) {
            // Do this to prevent unnecessary object allocations
            final SpongeDataManipulator manipulator = (SpongeDataManipulator) valueContainer;
            for (final Map.Entry<Key<?>, Object> entry : manipulator.values.entrySet()) {
                if (!predicate.test(entry.getKey())) {
                    continue;
                }
                if (overlap == MergeFunction.REPLACEMENT_PREFERRED) {
                    this.values.put(entry.getKey(), CopyHelper.copy(entry.getValue()));
                } else {
                    @Nullable final Object original = this.values.get(entry.getKey());
                    if (overlap == MergeFunction.ORIGINAL_PREFERRED && original != null) {
                        // Prefer the original
                        continue;
                    }
                    final Object merged = MergeHelper.merge(overlap, (Key) entry.getKey(), original, entry.getValue());
                    this.values.put(entry.getKey(), CopyHelper.copy(merged));
                }
            }
        } else {
            for (final Key<?> key : valueContainer.getKeys()) {
                if (!predicate.test(key)) {
                    continue;
                }
                if (overlap == MergeFunction.REPLACEMENT_PREFERRED) {
                    this.values.put(key, CopyHelper.copy(valueContainer.require((Key) key)));
                } else {
                    @Nullable final Object original = this.values.get(key);
                    if (overlap == MergeFunction.ORIGINAL_PREFERRED && original != null) {
                        // Prefer the original
                        continue;
                    }
                    final Object merged = MergeHelper.merge(overlap, (Key) key, original,
                        valueContainer.require((Key) key));
                    this.values.put(key, CopyHelper.copy(merged));
                }
            }
        }
        return this;
    }

    @Override
    public Mutable copyFrom(final ValueContainer valueContainer, final MergeFunction overlap,
        final Iterable<Key<?>> keys) {
        checkNotNull(valueContainer, "valueContainer");
        checkNotNull(overlap, "overlap");
        checkNotNull(keys, "keys");
        if (valueContainer instanceof SpongeDataManipulator) {
            // Do this to prevent unnecessary object allocations
            final SpongeDataManipulator manipulator = (SpongeDataManipulator) valueContainer;
            copyFrom(this.values, overlap, keys, manipulator.values::get);
        } else {
            copyFrom(this.values, overlap, keys, key -> valueContainer.get((Key) key).orElse(null));
        }
        return this;
    }

    @Override
    public Mutable copyFrom(final ValueContainer valueContainer, final MergeFunction overlap) {
        checkNotNull(valueContainer, "valueContainer");
        checkNotNull(overlap, "overlap");
        copyFrom(this.values, valueContainer, overlap);
        return this;
    }

    public static void copyFrom(
        final Map<Key<?>, Object> values, final ValueContainer valueContainer, final MergeFunction overlap) {
        if (valueContainer instanceof SpongeDataManipulator) {
            // Do this to prevent unnecessary object allocations
            final SpongeDataManipulator manipulator = (SpongeDataManipulator) valueContainer;
            copyFrom(values, overlap, manipulator.values.keySet(), manipulator.values::get);
        } else {
            copyFrom(values, overlap, valueContainer.getKeys(), key -> valueContainer.get((Key) key).orElse(null));
        }
    }

    private static void copyFrom(final Map<Key<?>, Object> values, final MergeFunction overlap,
        final Iterable<Key<?>> keys,
        final Function<Key<?>, @Nullable Object> function) {
        for (final Key<?> key : keys) {
            @Nullable final Object replacement = function.apply(key);
            if (overlap == MergeFunction.REPLACEMENT_PREFERRED && replacement != null) {
                values.put(key, CopyHelper.copy(replacement));
            } else {
                @Nullable final Object original = values.get(key);
                if (overlap == MergeFunction.ORIGINAL_PREFERRED && original != null) {
                    // Prefer the original
                    continue;
                }
                final Object merged = MergeHelper.merge(overlap, (Key) key, original, replacement);
                values.put(key, CopyHelper.copy(merged));
            }
        }
    }

    @Override
    public <E> Mutable set(final Key<? extends Value<E>> key, final E value) {
        checkNotNull(key, "key");
        checkNotNull(value, "value");
        this.values.put(key, CopyHelper.copy(value));
        return this;
    }

    @Override
    public Mutable remove(final Key<?> key) {
        checkNotNull(key, "key");
        this.values.remove(key);
        return this;
    }

    @Override
    public Mutable copy() {
        return new MutableDataManipulator(this.copyMap());
    }

    @Override
    public Set<Key<?>> getKeys() {
        return ImmutableSet.copyOf(this.values.keySet());
    }
}
