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
package org.spongepowered.common.world.schematic;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.world.schematic.PaletteType;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class SpongePaletteTypeBuilder<T, R> implements PaletteType.Builder<T, R> {

    private @MonotonicNonNull BiFunction<String, Registry<R>, Optional<T>> resolver;
    private @MonotonicNonNull BiFunction<Registry<R>, T, String> stringifier;

    @SuppressWarnings("ConstantConditions")
    @Override
    public PaletteType.Builder<T, R> reset() {
        this.resolver = null;
        this.stringifier = null;
        return this;
    }

    @Override
    public PaletteType.Builder<T, R> resolver(final BiFunction<String, Registry<R>, Optional<T>> resolver) {
        this.resolver = Objects.requireNonNull(resolver, "Resolver cannot be null");
        return this;
    }

    @Override
    public PaletteType.Builder<T, R> stringifier(final BiFunction<Registry<R>, T, String> stringifier) {
        this.stringifier = Objects.requireNonNull(stringifier, "Stringifier cannot be null");
        return this;
    }

    @Override
    public PaletteType<T, R> build() throws IllegalStateException {
        Objects.requireNonNull(this.resolver, "Encoder cannot be null");
        Objects.requireNonNull(this.stringifier, "Decoder cannot be null");
        return new SpongePaletteType<>(
            this.resolver,
            this.stringifier
        );
    }

}
