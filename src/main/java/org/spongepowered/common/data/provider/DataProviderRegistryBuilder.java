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
package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class DataProviderRegistryBuilder {

    private final DataProviderRegistry registry;

    public DataProviderRegistryBuilder(DataProviderRegistry registry) {
        this.registry = registry;
        this.register();
    }

    protected <T, R> ObjectConverter<T, R> identity() {
        return new ObjectConverter<T, R>() {
            @Override
            public R to(T element) {
                return (R) element;
            }
            @Override
            public T from(R value) {
                return (T) value;
            }
        };
    }

    protected interface ObjectConverter<E, T> {

        T to(E element);

        E from(T value);
    }

    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key,
            Function<H, E> getter) {

    }

    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key,
            Function<H, E> getter, BiConsumer<H, E> setter) {

    }

    protected <E, H, T> void register(Class<H> target, Key<? extends Value<E>> key,
            Function<H, T> getter, BiConsumer<H, T> setter, ObjectConverter<E, T> converter) {

    }

    protected <E, G, S> void register(Class<G> getterTarget, Class<S> setterTarget,
            Key<? extends Value<E>> key, Function<G, E> getter, BiConsumer<S, E> setter) {

    }

    protected void register(DataProvider<?,?> provider) {

    }

    protected abstract void register();
}
