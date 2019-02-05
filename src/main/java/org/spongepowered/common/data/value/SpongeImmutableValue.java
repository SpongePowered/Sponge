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
package org.spongepowered.common.data.value;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.Value;

import java.util.function.Function;

public class SpongeImmutableValue<E> extends SpongeValue<E> implements Value.Immutable<E> {

    public SpongeImmutableValue(Key<? extends Value<E>> key, E value) {
        super(key, value);
    }

    @Override
    public E get() {
        return CopyHelper.copy(super.get());
    }

    @Override
    public SpongeValue.Immutable<E> with(E value) {
        checkNotNull(value, "value");
        return new SpongeImmutableValue<>(this.key, CopyHelper.copy(value));
    }

    @Override
    public SpongeValue.Immutable<E> transform(Function<E, E> function) {
        return with(checkNotNull(function, "function").apply(get()));
    }

    @Override
    public Mutable<E> asMutable() {
        return new SpongeMutableValue<>(this.key, CopyHelper.copy(this.value));
    }

    public static <E> SpongeImmutableValue<E> cachedOf(Key<? extends Value<E>> key, E value) {
        return new SpongeImmutableValue<>(key, value);
    }
}
