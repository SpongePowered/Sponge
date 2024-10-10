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

import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteReference;
import org.spongepowered.api.world.schematic.PaletteType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public abstract class CachingPalette<T, R, D extends Palette<T, R>> implements Palette<T, R> {

    protected final D delegate;
    protected final Map<Integer, Optional<T>> cache;

    protected CachingPalette(final D delegate) {
        this.delegate = delegate;
        this.cache = new HashMap<>();
    }

    @Override
    public PaletteType<T, R> type() {
        return this.delegate.type();
    }

    @Override
    public int highestId() {
        return this.delegate.highestId();
    }

    @Override
    public Optional<PaletteReference<T, R>> get(final int id) {
        return this.delegate.get(id);
    }

    @Override
    public Optional<T> get(final int id, final RegistryHolder holder) {
        return this.cache.computeIfAbsent(id, $ -> this.delegate.get(id, holder));
    }

    @Override
    public OptionalInt get(final T type) {
        return this.delegate.get(type);
    }

    @Override
    public Stream<T> stream() {
        return this.delegate.stream();
    }

    @Override
    public Stream<Map.Entry<T, Integer>> streamWithIds() {
        return this.delegate.streamWithIds();
    }

    @Override
    public Mutable<T, R> asMutable(final RegistryHolder registry) {
        return new MutableImpl<>(this.delegate.asMutable(registry));
    }

    @Override
    public Immutable<T, R> asImmutable() {
        return new ImmutableImpl<>(this.delegate.asImmutable());
    }

    public static final class MutableImpl<T, R> extends CachingPalette<T, R, Palette.Mutable<T, R>> implements Palette.Mutable<T, R> {

        public MutableImpl(final Palette.Mutable<T, R> delegate) {
            super(delegate);
        }

        @Override
        public int orAssign(final T type) {
            int id = this.delegate.orAssign(type);
            this.cache.put(id, Optional.of(type));
            return id;
        }

        @Override
        public boolean remove(final T type) {
            this.delegate.get(type).ifPresent(this.cache::remove);
            return this.delegate.remove(type);
        }
    }

    public static final class ImmutableImpl<T, R> extends CachingPalette<T, R, Palette.Immutable<T, R>> implements Palette.Immutable<T, R> {

        public ImmutableImpl(final Palette.Immutable<T, R> delegate) {
            super(delegate);
        }
    }
}
