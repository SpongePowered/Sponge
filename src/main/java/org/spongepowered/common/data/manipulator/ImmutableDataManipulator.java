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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.Map;
import java.util.Set;

final class ImmutableDataManipulator extends SpongeDataManipulator implements DataManipulator.Immutable {

    @Nullable private Set<Value.Immutable<?>> cachedValues;

    ImmutableDataManipulator(final Map<Key<?>, Object> values) {
        super(values);
    }

    @Override
    public Mutable asMutableCopy() {
        return new MutableDataManipulator(this.copyMap());
    }

    @Override
    public Immutable without(final Key<?> key) {
        checkNotNull(key, "key");
        if (!this.values.containsKey(key)) {
            return this;
        }
        return DataManipulator.Immutable.super.without(key);
    }

    @Override
    public Mutable asMutable() {
        return this.asMutableCopy();
    }

    @Override
    public Set<Key<?>> getKeys() {
        return this.values.keySet();
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        if (this.cachedValues == null) {
            this.cachedValues = super.getValues();
        }
        return this.cachedValues;
    }
}
