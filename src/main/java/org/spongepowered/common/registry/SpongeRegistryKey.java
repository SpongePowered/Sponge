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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryReference;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;

public class SpongeRegistryKey<T> implements RegistryKey<T> {

    private final RegistryType<T> registry;
    private final ResourceKey location;

    public SpongeRegistryKey(final RegistryType<T> registry, final ResourceKey location) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.location = Objects.requireNonNull(location, "location");
    }

    @Override
    public final RegistryType<T> registry() {
        return this.registry;
    }

    @Override
    public final ResourceKey location() {
        return this.location;
    }

    @Override
    public final RegistryReference<T> asReference() {
        return new SpongeRegistryReference<>(this);
    }

    @Override
    public final <V extends T> DefaultedRegistryReference<V> asDefaultedReference(final Supplier<RegistryHolder> defaultHolder) {
        return new SpongeDefaultedRegistryReference<>((RegistryKey<V>) this, defaultHolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.registry, this.location);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final SpongeRegistryKey other = (SpongeRegistryKey) o;
        return this.registry.equals(other.registry) && this.location.equals(other.location);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("registry='" + this.registry + "'")
                .add("location='" + this.location + "'")
                .toString();
    }

    public static final class FactoryImpl implements RegistryKey.Factory {

        @Override
        public <T> RegistryKey<T> of(final RegistryType<T> registry, final ResourceKey location) {
            return new SpongeRegistryKey<>(Objects.requireNonNull(registry, "registry"), Objects.requireNonNull(location, "location"));
        }

        @Override
        public <T> RegistryKey<T> referenced(final RegistryHolder holder, RegistryType<T> registry, T value) {
            return new SpongeRegistryKey<>(Objects.requireNonNull(registry, "registry"), Objects.requireNonNull(holder, "holder").registry(registry).valueKey(Objects.requireNonNull(value, "value")));
        }
    }
}
