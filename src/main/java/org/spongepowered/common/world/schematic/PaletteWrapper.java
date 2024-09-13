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

import net.minecraft.core.IdMapper;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteReference;
import org.spongepowered.api.world.schematic.PaletteType;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PaletteWrapper<NT, T, R> implements Palette.Immutable<T, R> {

    public static <NT, T, R> PaletteWrapper<NT, T, R> of(PaletteType<T, R> type, IdMapper<NT> proxy, Registry<R> registry) {
        return new PaletteWrapper<>(type, proxy, registry);
    }

    private final PaletteType<T, R> type;
    private final IdMapper<NT> proxy;
    private final Registry<R> registry;

    private PaletteWrapper(PaletteType<T, R> type, IdMapper<NT> proxy, Registry<R> registry) {
        this.type = type;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public PaletteType<T, R> type() {
        return this.type;
    }

    @Override
    public int highestId() {
        return this.proxy.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<PaletteReference<T, R>> get(int id) {
        final var n = this.proxy.byId(id);
        if (n == null) {
            return Optional.empty();
        }
        final var apply = this.type.stringifier().apply(this.registry, (T) n);
        return Optional.of(PaletteReference.byString(this.registry.type(), apply));
    }

    @SuppressWarnings("unchecked")
    @Override
    public OptionalInt get(T type) {
        final var id = this.proxy.getId((NT) type);
        if (id >= 0) {
            return OptionalInt.of(id);
        }
        return OptionalInt.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(this.proxy::spliterator,
                Spliterator.SIZED | Spliterator.ORDERED | Spliterator.IMMUTABLE,
                false)
            .map(n -> (T) n);
    }

    @Override
    public Stream<Map.Entry<T, Integer>> streamWithIds() {
        return Stream.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mutable<T, R> asMutable(RegistryHolder registry) {
        final var mutable = new MutableBimapPalette<T, R>(this.type, (Registry<R>) this.registry);
        for (NT nt : this.proxy) {
            mutable.orAssign((T) nt);
        }
        return mutable;
    }

    @Override
    public Immutable<T, R> asImmutable() {
        return this;
    }
}
