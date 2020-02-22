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
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.copy.CopyHelper;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class DataProviderRegistryBuilder {

    protected final DataProviderRegistry registry;

    public DataProviderRegistryBuilder(DataProviderRegistry registry) {
        this.registry = registry;
    }

    /**
     * Builds and registers a new {@link DataProvider}.
     *
     * <p>No setter is provided which means that the provider is read-only.</p>
     *
     * @param target The target type which the provider is supported by
     * @param key The key the provider is bound to
     * @param getter The getter function
     * @param <E> The element type of the key
     * @param <H> The target data holder type
     */
    protected <E, H> void register(Class<H> target, Supplier<? extends Key<? extends Value<E>>> key, Function<H, E> getter) {
        this.register(target, key.get(), getter);
    }

    protected static <E, H> Function<H, Optional<E>> toOptionalGetter(Key<? extends Value<E>> key, Function<H, E> getter) {
        // Optimize boolean optionals
        if (key.getElementToken().getRawType() == Boolean.class) {
            //noinspection unchecked
            return dataHolder -> (Optional<E>) OptBool.of((Boolean) getter.apply(dataHolder));
        } else {
            return dataHolder -> Optional.ofNullable(getter.apply(dataHolder));
        }
    }

    /**
     * Builds and registers a new {@link DataProvider}.
     *
     * <p>No setter is provided which means that the provider is read-only.</p>
     *
     * @param target The target type which the provider is supported by
     * @param key The key the provider is bound to
     * @param getter The getter function
     * @param <E> The element type of the key
     * @param <H> The target data holder type
     */
    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key, Function<H, E> getter) {
        final Function<H, Optional<E>> optionalGetter = toOptionalGetter(key, getter);
        this.register(new GenericMutableDataProvider<H, E>(key, target) {

            @Override
            protected Optional<E> getFrom(H dataHolder) {
                return optionalGetter.apply(dataHolder);
            }
        });
    }

    /**
     * Builds and registers a new {@link DataProvider}.
     *
     * @param target The target type which the provider is supported by
     * @param key The key the provider is bound to
     * @param getter The getter function
     * @param setter The setter function
     * @param <E> The element type of the key
     * @param <H> The target data holder type
     */
    protected <E, H> void register(Class<H> target, Supplier<? extends Key<? extends Value<E>>> key,
            Function<H, E> getter, BiConsumer<H, E> setter) {
        this.register(target, key.get(), getter, setter);
    }

    /**
     * Builds and registers a new {@link DataProvider}.
     *
     * @param target The target type which the provider is supported by
     * @param key The key the provider is bound to
     * @param getter The getter function
     * @param setter The setter function
     * @param <E> The element type of the key
     * @param <H> The target data holder type
     */
    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key,
            Function<H, E> getter, BiConsumer<H, E> setter) {
        final Function<H, Optional<E>> optionalGetter = toOptionalGetter(key, getter);
        this.register(new GenericMutableDataProvider<H, E>(key, target) {

            @Override
            protected Optional<E> getFrom(H dataHolder) {
                return optionalGetter.apply(dataHolder);
            }

            @Override
            protected boolean set(H dataHolder, E value) {
                setter.accept(dataHolder, value);
                return true;
            }
        });
    }

    /**
     * Builds and registers a new {@link DataProvider}.
     *
     * @param target The target type which the provider is supported by
     * @param key The key the provider is bound to
     * @param defaultValue The default value, which will be used to
     *                     "remove" values and reset them to their original values.
     * @param getter The getter function
     * @param setter The setter function
     * @param <E> The element type of the key
     * @param <H> The target data holder type
     */
    protected <E, H> void register(Class<H> target, Supplier<? extends Key<? extends Value<E>>> key, E defaultValue,
            Function<H, E> getter, BiConsumer<H, E> setter) {
        this.register(target, key.get(), defaultValue, getter, setter);
    }

    /**
     * Builds and registers a new {@link DataProvider}.
     *
     * @param target The target type which the provider is supported by
     * @param key The key the provider is bound to
     * @param defaultValue The default value, which will be used to
     *                     "remove" values and reset them to their original values.
     * @param getter The getter function
     * @param setter The setter function
     * @param <E> The element type of the key
     * @param <H> The target data holder type
     */
    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key, E defaultValue,
            Function<H, E> getter, BiConsumer<H, E> setter) {
        this.register(target, key, CopyHelper.createSupplier(defaultValue), getter, setter);
    }

    /**
     * Builds and registers a new {@link DataProvider}.
     *
     * @param target The target type which the provider is supported by
     * @param key The key the provider is bound to
     * @param defaultSupplier The supplier for default values, which will be used to
     *                        "remove" values and reset them to their original values.
     * @param getter The getter function
     * @param setter The setter function
     * @param <E> The element type of the key
     * @param <H> The target data holder type
     */
    protected <E, H> void register(Class<H> target, Supplier<? extends Key<? extends Value<E>>> key, Supplier<E> defaultSupplier,
            Function<H, E> getter, BiConsumer<H, E> setter) {
        this.register(target, key.get(), defaultSupplier, getter, setter);
    }

    /**
     * Builds and registers a new {@link DataProvider}.
     *
     * @param target The target type which the provider is supported by
     * @param key The key the provider is bound to
     * @param defaultSupplier The supplier for default values, which will be used to
     *                        "remove" values and reset them to their original values.
     * @param getter The getter function
     * @param setter The setter function
     * @param <E> The element type of the key
     * @param <H> The target data holder type
     */
    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key, Supplier<E> defaultSupplier,
            Function<H, E> getter, BiConsumer<H, E> setter) {
        final Function<H, Optional<E>> optionalGetter = toOptionalGetter(key, getter);
        this.register(new GenericMutableDataProvider<H, E>(key, target) {

            @Override
            protected Optional<E> getFrom(H dataHolder) {
                return optionalGetter.apply(dataHolder);
            }

            @Override
            protected boolean set(H dataHolder, E value) {
                setter.accept(dataHolder, value);
                return true;
            }

            @Override
            protected boolean delete(H dataHolder) {
                return this.set(dataHolder, defaultSupplier.get());
            }
        });
    }

    /**
     * Registers the {@link DataProvider}.
     *
     * @param provider The data provider
     */
    protected void register(DataProvider<?,?> provider) {
        this.registry.register(provider);
    }

    public abstract void register();
}
