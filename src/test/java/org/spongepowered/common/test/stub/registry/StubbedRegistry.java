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
package org.spongepowered.common.test.stub.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.common.registry.SpongeRegistryEntry;
import org.spongepowered.common.test.stub.StubKey;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StubbedRegistry<T> implements Registry<T> {

    private final BiMap<ResourceKey, T> stubs = HashBiMap.create();
    private final BiMap<ResourceKey, RegistryEntry<T>> entries = HashBiMap.create();
    private final Supplier<RegistryType<T>> typeSupplier;
    private final Function<ResourceKey, T> generator;

    public StubbedRegistry(final Supplier<RegistryType<T>> typeSupplier, final Function<ResourceKey, T> generator) {
        this.typeSupplier = typeSupplier;
        this.generator = generator;
    }

    public T createEntry(final String namespace, final String desired) {
        final ResourceKey key = new StubKey(namespace, desired);
        return this.getOrCreate(key);
    }

    @Override
    public RegistryType<T> type() {
        return this.typeSupplier.get();
    }

    @Override
    public ResourceKey valueKey(final T value) {
        return this.findValueKey(value).orElseThrow(() -> new IllegalArgumentException("Could not find key for value: " + value));
    }

    @Override
    public Optional<ResourceKey> findValueKey(final T value) {
        final ResourceKey resourceKey = this.stubs.inverse().get(value);
        return Optional.ofNullable(resourceKey);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends T> Optional<RegistryEntry<V>> findEntry(
        final ResourceKey key
    ) {
        final RegistryEntry<T> entry = this.entries.computeIfAbsent(key, (k) -> {
            final T stub = this.getOrCreate(k);
            return new SpongeRegistryEntry<>(this.type(), k, stub);
        });
        return Optional.of((RegistryEntry<V>) entry);
    }

    private T getOrCreate(final ResourceKey key) {
        if (this.stubs.containsKey(key)) {
            return this.stubs.get(key);
        }
        // otherwise, set up the blocks
        final T type = this.generator.apply(key);
        this.stubs.put(key, type);
        this.entries.put(key, new SpongeRegistryEntry<>(this.type(), key, type));
        return type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends T> Optional<V> findValue(final ResourceKey key) {

        return Optional.of((V) this.getOrCreate(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends T> V value(final ResourceKey key) {
        return (V) this.getOrCreate(key);
    }

    @Override
    public Stream<RegistryEntry<T>> streamEntries() {
        return this.entries.values().stream();
    }

    @Override
    public Stream<T> stream() {
        return this.stubs.values().stream();
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends T> Optional<RegistryEntry<V>> register(final ResourceKey key, final V value) {
        final RegistryEntry<T> entry;
        if (!this.entries.containsKey(key)) {
            entry = new SpongeRegistryEntry<>(this.type(), key, value);
            this.entries.put(key, entry);
            this.stubs.put(key, value);
        } else {
            entry = this.entries.get(key);
        }
        return Optional.of((RegistryEntry<V>) entry);
    }
}
