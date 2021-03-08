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

import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;

abstract class InitialRegistryData<T> {
    abstract Map<ResourceKey, T> keyToValue();

    abstract @Nullable Map<ResourceKey, Integer> keyToId();

    void forEach(final LoaderConsumer<T> consumer) {
        final Map<ResourceKey, T> kv = this.keyToValue();
        final Map<ResourceKey, Integer> ki = this.keyToId();

        for (final Map.Entry<ResourceKey, T> entry : kv.entrySet()) {
            final OptionalInt id;
            if (ki != null) {
                final Integer ni = ki.get(entry.getKey());
                id = ni != null ? OptionalInt.of(ni) : OptionalInt.empty();
            } else {
                id = OptionalInt.empty();
            }
            consumer.accept(
                entry.getKey(),
                id,
                entry.getValue()
            );
        }
    }

    static <T> @Nullable InitialRegistryData<T> noIds(final @Nullable Supplier<Map<ResourceKey, T>> values) {
        if (values == null) {
            return null;
        }
        return new InitialRegistryData<T>() {
            @Override
            Map<ResourceKey, T> keyToValue() {
                return values.get();
            }

            @Override
            @Nullable Map<ResourceKey, Integer> keyToId() {
                return null;
            }
        };
    }

    interface LoaderConsumer<T> {
        void accept(final ResourceKey key, final OptionalInt id, final T value);
    }
}
