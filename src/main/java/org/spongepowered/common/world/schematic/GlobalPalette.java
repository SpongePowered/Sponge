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

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.common.util.MemoizedSupplier;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class GlobalPalette<T> implements Palette.Immutable<T> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    static final Supplier<GlobalPalette<BlockState>> GLOBAL_BLOCK_STATE_PALETTE = MemoizedSupplier.memoize(
        () -> new GlobalPalette<>(
            PaletteTypes.GLOBAL_BLOCK_PALETTE.get(),
            () -> (Stream<BlockState>) (Stream) Registry.BLOCK.stream()
                .flatMap(block -> block.getStateContainer().getValidStates().stream()),
            (type) -> Block.BLOCK_STATE_REGISTRY.getId((net.minecraft.block.BlockState) type),
            (id) -> (BlockState) Block.BLOCK_STATE_REGISTRY.byId(id),
            BlockState.class
        )
    );

    @SuppressWarnings({"unchecked", "rawtypes"})
    static final Supplier<GlobalPalette<BiomeType>> GLOBAL_BIOME_PALETTE = MemoizedSupplier.memoize(() -> new GlobalPalette<>(
        PaletteTypes.GLOBAL_BIOME_PALETTE.get(),
        () -> (Stream<BiomeType>) (Stream) Registry.BIOME.stream(),
        (type) -> Registry.BIOME.getId((Biome) (type instanceof VirtualBiomeType ? ((VirtualBiomeType) type).getPersistedType() : type)),
        (id) -> (BiomeType) Registry.BIOME.getByValue(id),
        BiomeType.class
    ));


    private final ToIntFunction<T> typeToInt;
    private final IntFunction<T> intToType;
    private final PaletteType<T> paletteType;
    private final Class<T> catalogType;
    private final IntSupplier length;
    private final Supplier<Stream<T>> catalogSupplier;

    private GlobalPalette(
        final PaletteType<T> paletteType,
        final Supplier<Stream<T>> catalogSupplier,
        final ToIntFunction<T> map,
        final IntFunction<T> identity,
        final Class<T> catalogType
    ) {

        this.length = () -> catalogSupplier.get().mapToInt(map).max().orElse(0);
        this.typeToInt = map;
        this.intToType = identity;
        this.paletteType = paletteType;
        this.catalogType = catalogType;
        this.catalogSupplier = catalogSupplier;
    }

    public static Palette<BlockState> getBlockPalette() {
        return GlobalPalette.GLOBAL_BLOCK_STATE_PALETTE.get();
    }

    public static GlobalPalette<BiomeType> getBiomePalette() {
        return GlobalPalette.GLOBAL_BIOME_PALETTE.get();
    }

    @Override
    public PaletteType<T> getType() {
        return this.paletteType;
    }

    @Override
    public int getHighestId() {
        return this.length.getAsInt();
    }

    @Override
    public OptionalInt get(final T type) {
        return OptionalInt.of(this.typeToInt.applyAsInt(type));
    }

    @Override
    public Optional<T> get(final int id) {
        return Optional.ofNullable(this.intToType.apply(id));
    }

    @Override
    public Stream<T> stream() {
        return this.catalogSupplier.get();
    }

    @Override
    public Mutable<T> asMutable() {
        final HashBiMap<T, Integer> copy = HashBiMap.create(this.length.getAsInt());
        this.catalogSupplier.get()
            .map(it -> Tuple.of(it, this.typeToInt.applyAsInt(it)))
            .forEach(tuple -> copy.put(tuple.getFirst(), tuple.getSecond()));
        return new MutableBimapPalette<T>(this.paletteType, copy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.paletteType, this.catalogType, this.length);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final GlobalPalette other = (GlobalPalette) obj;
        return Objects.equals(this.paletteType, other.paletteType)
            && Objects.equals(this.catalogType, other.catalogType)
            && Objects.equals(this.length.getAsInt(), other.length.getAsInt());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("paletteType", this.paletteType)
            .add("catalogType", this.catalogType)
            .add("length", this.length.getAsInt())
            .toString();
    }
}
