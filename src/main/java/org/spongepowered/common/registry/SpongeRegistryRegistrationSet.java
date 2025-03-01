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
package org.spongepowered.common.registry;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryRegistrationSet;
import org.spongepowered.api.registry.RegistryType;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public record SpongeRegistryRegistrationSet<T>(RegistryType<T> registryType, Map<ResourceKey, Function<RegistryHolder, T>> values) implements RegistryRegistrationSet<T> {

    public static final class BuilderImpl<T> implements RegistryRegistrationSet.Builder<T> {

        private final RegistryType<T> registryType;
        private final Supplier<RegistryHolder> defaultHolder;

        private ImmutableMap.Builder<ResourceKey, Function<RegistryHolder, T>> builder = ImmutableMap.builder();

        BuilderImpl(final RegistryType<T> registryType, final Supplier<RegistryHolder> defaultHolder) {
            this.registryType = registryType;
            this.defaultHolder = defaultHolder;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V extends T> DefaultedRegistryReference<V> register(final ResourceKey key, final Function<RegistryHolder, V> value) {
            this.builder.put(key, (Function<RegistryHolder, T>) value);
            return new SpongeDefaultedRegistryReference<>(new SpongeRegistryKey<>((RegistryType<V>) this.registryType, key), this.defaultHolder);
        }

        @Override
        public Builder<T> reset() {
            this.builder = ImmutableMap.builder();
            return this;
        }

        @NotNull
        @Override
        public RegistryRegistrationSet<T> build() {
            return new SpongeRegistryRegistrationSet<>(this.registryType, this.builder.build());
        }
    }

    public static final class FactoryImpl implements RegistryRegistrationSet.Factory {

        @Override
        public <T> Builder<T> builder(final RegistryType<T> registryType, final Supplier<RegistryHolder> defaultHolder) {
            return new BuilderImpl<>(registryType, defaultHolder);
        }
    }
}
