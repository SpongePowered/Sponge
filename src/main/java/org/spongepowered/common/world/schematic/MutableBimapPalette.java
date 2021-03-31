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
import com.google.common.collect.HashBiMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteReference;
import org.spongepowered.api.world.schematic.PaletteType;

import java.util.AbstractMap;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public class MutableBimapPalette<T, R> implements Palette.Mutable<T, R> {

    private static final int DEFAULT_ALLOCATION_SIZE = 64;

    private final BiMap<Integer, PaletteReference<T, R>> ids;
    private final BiMap<PaletteReference<T, R>, Integer> idsr;
    private final BitSet allocation = new BitSet(MutableBimapPalette.DEFAULT_ALLOCATION_SIZE);
    private final PaletteType<T, R> paletteType;
    private final Registry<R> registry;
    private final RegistryType<R> registryType;
    private int maxId = 0;

    public MutableBimapPalette(final PaletteType<T, R> paletteType, final Registry<R> registry, final RegistryType<R> registryType) {
        this.ids = HashBiMap.create();
        this.idsr = this.ids.inverse();
        this.paletteType = paletteType;
        this.registry = registry;
        this.registryType = registryType;
    }

    public MutableBimapPalette(final PaletteType<T, R> paletteType, final Registry<R> registry, final RegistryType<R> registryType,
        final BiMap<PaletteReference<T, R>, Integer> reference
    ) {
        this.ids = HashBiMap.create(reference.size());
        this.idsr = this.ids.inverse();
        this.paletteType = paletteType;
        this.registry = registry;
        this.registryType = registryType;
        reference.forEach((key, id) -> this.getOrAssignInternal(key));
    }

    public MutableBimapPalette(final PaletteType<T, R> paletteType, final Registry<R> registry, final RegistryType<R> registryType,
        final int expectedSize
    ) {
        this.ids = HashBiMap.create(expectedSize);
        this.idsr = this.ids.inverse();
        this.paletteType = paletteType;
        this.registry = registry;
        this.registryType = registryType;
    }

    @Override
    public PaletteType<T, R> type() {
        return this.paletteType;
    }

    @Override
    public int highestId() {
        return this.maxId;
    }

    @Override
    public OptionalInt get(final T state) {
        final PaletteReference<T, R> ref = MutableBimapPalette.createPaletteReference(
            state,
            this.paletteType,
            this.registry
        );
        final Integer value = this.idsr.get(ref);
        if (value == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(value);
    }

    private int getOrAssignInternal(final PaletteReference<T, R> ref) {
        final Integer id = this.idsr.get(ref);
        if (id == null) {
            final int next = this.allocation.nextClearBit(0);
            if (this.maxId < next) {
                this.maxId = next;
            }
            this.allocation.set(next);
            this.ids.put(next, ref);
            return next;
        }
        return id;
    }

    @Override
    public int orAssign(final T state) {
        final PaletteReference<T, R> ref = MutableBimapPalette.createPaletteReference(
            state,
            this.paletteType,
            this.registry
        );
        return this.getOrAssignInternal(ref);
    }

    @Override
    public Optional<PaletteReference<T, R>> get(final int id) {
        return Optional.ofNullable(this.ids.get(id));
    }

    @Override
    public Optional<T> get(final int id, final RegistryHolder holder) {
        return this.get(id)
            .flatMap(ref -> {
                final Optional<T> byRegistry = this.paletteType.resolver().apply(ref.value(), this.registry);
                if (!byRegistry.isPresent()) {
                    return Objects.requireNonNull(holder,"RegistryHolder cannot be null")
                        .findRegistry(ref.registry())
                        .flatMap(reg -> this.type().resolver().apply(ref.value(), reg));
                }
                return byRegistry;
            });
    }

    public int assign(final T state, final int id) {
        if (this.maxId < id) {
            this.maxId = id;
        }
        this.allocation.set(id);
        final PaletteReference<T, R> ref = MutableBimapPalette.createPaletteReference(
            state,
            this.paletteType,
            this.registry
        );
        this.ids.put(id, ref);
        return id;
    }

    @NonNull
    static <T, R> PaletteReference<T, R> createPaletteReference(
        final T state,
        final PaletteType<T, R> paletteType,
        final Registry<R> registry
    ) {
        final String string = paletteType.stringifier().apply(registry, state);
        return PaletteReference.byString(registry.type(), string);
    }

    @Override
    public boolean remove(final T state) {
        final Integer id = this.idsr.get(state);
        if (id == null) {
            return false;
        }
        this.allocation.clear(id);
        if (id == this.maxId) {
            this.maxId = this.allocation.previousSetBit(this.maxId);
        }
        this.ids.remove(id);
        return true;
    }

    @Override
    public Stream<T> stream() {
        final HashBiMap<PaletteReference<T, R>, Integer> copy = HashBiMap.create(this.idsr);
        return copy.keySet().stream()
            .map(ref -> this.paletteType.resolver().apply(ref.value(), this.registry))
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    @Override
    public Stream<Map.Entry<T, Integer>> streamWithIds() {
        final HashBiMap<Integer, PaletteReference<T, R>> copy = HashBiMap.create(this.ids);
        return copy.entrySet().stream()
            .map(entry -> {
                final Optional<T> apply = this.paletteType.resolver().apply(entry.getValue()
                    .value(), this.registry);
                return apply.map(value -> new AbstractMap.SimpleEntry<>(value, entry.getKey()));
            })
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    @Override
    public Immutable<T, R> asImmutable() {
        return new ImmutableBimapPalette<>(this.paletteType, this.registry, this.registryType, this.ids);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MutableBimapPalette<?, ?> that = (MutableBimapPalette<?, ?>) o;
        return this.maxId == that.maxId &&
            this.ids.equals(that.ids) &&
            this.allocation.equals(that.allocation) &&
            this.paletteType.equals(that.paletteType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ids, this.allocation, this.paletteType, this.maxId);
    }
}
