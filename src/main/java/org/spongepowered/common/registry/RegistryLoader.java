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
import org.spongepowered.api.registry.RegistryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RegistryLoader<T> {

    private final Map<ResourceKey, T> values = new HashMap<>();

    public static <T> RegistryLoader<T> of() {
        return new RegistryLoader<>();
    }

    public RegistryLoader<T> add(final RegistryKey<T> key, final Function<ResourceKey, ? super T> function) {
        this.values.put(key.location(), function.apply(key.location()));
        return this;
    }

    public RegistryLoader<T> addAll(final Function<ResourceKey, ? super T> function, final RegistryKey<T>... keys) {
        for (final RegistryKey<T> key : keys) {
            this.values.put(key.location(), function.apply(key.location()));
        }

        return this;
    }

    public Map<ResourceKey, T> values() {
        return this.values;
    }
}
