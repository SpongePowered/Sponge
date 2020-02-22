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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.registry.SimpleRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

public final class MappedRegistry<T, U> extends SimpleRegistry<T> {

    private final Map<T, U> mappings;
    private final Map<U, T> reverseMappings;

    MappedRegistry() {
        this.mappings = new Object2ObjectOpenHashMap<>();
        this.reverseMappings = new Object2ObjectOpenHashMap<>();
    }

    void registerMapping(T value, U mapping) {
        this.mappings.put(value, mapping);
        this.reverseMappings.put(mapping, value);
    }

    public @Nullable U getMapping(T value) {
        return this.mappings.get(value);
    }

    public U requireMapping(T value) {
        final U mapping = this.mappings.get(value);
        if (mapping == null) {
            throw new IllegalArgumentException("Failed to get mapping for: " + value);
        }
        return mapping;
    }

    public @Nullable T getReverseMapping(U value) {
        return this.reverseMappings.get(value);
    }

    public T requireReverseMapping(U value) {
        final T mapping = this.reverseMappings.get(value);
        if (mapping == null) {
            throw new IllegalArgumentException("Failed to get reverse mapping for: " + value);
        }
        return mapping;
    }

}
