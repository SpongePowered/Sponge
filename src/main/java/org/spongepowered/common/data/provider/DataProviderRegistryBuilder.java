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

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class DataProviderRegistryBuilder {

    private final DataProviderRegistry registry;

    public DataProviderRegistryBuilder(DataProviderRegistry registry) {
        this.registry = registry;
        this.register();
    }

    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key, Function<H, E> getter) {
        register(new GenericMutableDataProvider<H, E>(key, target) {

            @Override
            protected Optional<E> getFrom(H dataHolder) {
                return Optional.ofNullable(getter.apply(dataHolder));
            }

            @Override
            protected boolean set(H dataHolder, E value) {
                return false;
            }
        });
    }

    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key,
            Function<H, E> getter, BiConsumer<H, E> setter) {
        register(new GenericMutableDataProvider<H, E>(key, target) {

            @Override
            protected Optional<E> getFrom(H dataHolder) {
                return Optional.ofNullable(getter.apply(dataHolder));
            }

            @Override
            protected boolean set(H dataHolder, E value) {
                setter.accept(dataHolder, value);
                return true;
            }
        });
    }

    protected void register(DataProvider<?,?> provider) {
        this.registry.register(provider);
    }

    protected abstract void register();
}
