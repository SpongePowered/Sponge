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

import org.spongepowered.api.data.value.Value;

import java.util.Objects;

@SuppressWarnings("unchecked")
final class CachedEnumValueConstructor<V extends Value<E>, E extends Enum<E>> implements ValueConstructor<V, E> {

    private final ValueConstructor<V, E> original;
    private final V[] immutableValues;

    public CachedEnumValueConstructor(final ValueConstructor<V, E> original, final Class<E> enumType) {
        this.original = original;
        final E[] constants = enumType.getEnumConstants();
        this.immutableValues = (V[]) new Value<?>[constants.length];
        for (int i = 0; i < constants.length; i++) {
            this.immutableValues[i] = this.original.getImmutable(constants[i]);
        }
    }

    @Override
    public V getMutable(final E element) {
        return this.original.getMutable(element);
    }

    @Override
    public V getImmutable(final E element) {
        Objects.requireNonNull(element, "element");
        return this.immutableValues[element.ordinal()];
    }

    @Override
    public V getRawImmutable(final E element) {
        return this.getImmutable(element);
    }
}
