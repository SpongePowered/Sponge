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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

public class StubRegistryHolder implements RegistryHolder {

    private final Collection<Registry<?>> registries = new ConcurrentLinkedDeque<>();

    public <T> void register(final StubbedRegistry<T> registry) {
        this.registries.add(registry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Registry<T> registry(final RegistryType<T> type) {
        return this.registries.stream().filter(r -> r.type() == type).findFirst()
            .map(r -> (Registry<T>) r)
            .orElseThrow(() -> new IllegalStateException("Registry for type not available:" + type.toString()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<Registry<T>> findRegistry(
        final RegistryType<T> type
    ) {
        return this.registries.stream().filter(r -> r.type() == type).findFirst()
            .map(r -> (Registry<T>) r);
    }

    @Override
    public Stream<Registry<?>> stream(final ResourceKey root) {
        return Stream.of();
    }
}
