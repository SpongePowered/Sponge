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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteReference;
import org.spongepowered.api.world.schematic.PaletteType;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public class ImmutableBimapPalette<T, R> implements Palette.Immutable<T, R> {

    private final ImmutableBiMap<Integer, PaletteReference<T, R>> ids;
    private final ImmutableBiMap<PaletteReference<T, R>, Integer> idsr;
    private final PaletteType<T, R> paletteType;
    private final int maxId;
    private final Registry<R> registry;
    private final RegistryType<R> registryType;

    public ImmutableBimapPalette(
        final PaletteType<T, R> paletteType,
        final Registry<R> registry,
        final RegistryType<R> registryType,
        final BiMap<Integer, PaletteReference<T, R>> reference
    ) {
        final ImmutableBiMap.Builder<Integer, PaletteReference<T, R>> builder = ImmutableBiMap.builder();
        reference.forEach(builder::put);
        this.ids = builder.build();
        this.idsr = this.ids.inverse();
        this.paletteType = paletteType;
        this.registry = registry;
        this.registryType = registryType;
        int maxId = 0;
        for (final Integer id : this.ids.keySet()) {
            if (maxId < id) {
                maxId = id;
            }
        }
        this.maxId = maxId;
    }

    @Override
    public PaletteType<T, R> getType() {
        return this.paletteType;
    }

    @Override
    public int getHighestId() {
        return this.maxId;
    }

    @Override
    public OptionalInt get(final T state) {
        final PaletteReference<T, R> ref = MutableBimapPalette.createPaletteReference(state, this.paletteType, this.registry);
        final Integer value = this.idsr.get(ref);
        if (value == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(value);
    }

    @Override
    public Optional<PaletteReference<T, R>> get(final int id) {
        return Optional.ofNullable(this.ids.get(id));
    }

    @Override
    public Stream<T> stream() {
        return this.idsr.keySet().stream()
            .map(ref -> this.paletteType.getResolver().apply(ref.value(), this.registry))
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    @Override
    public Stream<Map.Entry<T, Integer>> streamWithIds() {
        return this.ids.entrySet().stream()
            .map(entry -> {
                final Optional<T> apply = this.paletteType.getResolver().apply(entry.getValue()
                    .value(), this.registry);
                return apply.map(value -> new AbstractMap.SimpleEntry<>(value, entry.getKey()));
            })
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    @Override
    public Mutable<T, R> asMutable(final RegistryHolder holder) {
        return new MutableBimapPalette<>(this.paletteType, holder.registry(this.registryType), this.registryType, this.idsr);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ImmutableBimapPalette<?, ?> that = (ImmutableBimapPalette<?, ?>) o;
        return this.maxId == that.maxId &&
               this.ids.equals(that.ids) &&
               this.paletteType.equals(that.paletteType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ids, this.paletteType, this.maxId);
    }
}
