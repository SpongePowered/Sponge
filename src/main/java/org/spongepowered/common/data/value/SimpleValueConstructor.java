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

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.function.BiFunction;

final class SimpleValueConstructor<V extends Value<E>, E> implements ValueConstructor<V, E> {

    private final Key<V> key;
    private final BiFunction<Key<V>, E, V> mutableConstructor;
    private final BiFunction<Key<V>, E, V> immutableConstructor;

    public SimpleValueConstructor(Key<V> key,
            BiFunction<Key<V>, E, V> mutableConstructor,
            BiFunction<Key<V>, E, V> immutableConstructor) {
        this.key = key;
        this.mutableConstructor = mutableConstructor;
        this.immutableConstructor = immutableConstructor;
    }

    @Override
    public V getMutable(E element) {
        return this.mutableConstructor.apply(this.key, element);
    }

    @Override
    public V getImmutable(E element) {
        return this.immutableConstructor.apply(this.key, element);
    }
}
