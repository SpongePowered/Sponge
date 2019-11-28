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
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.PaletteTypes;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

import javax.annotation.Nullable;

public class GlobalPalette<T extends CatalogType> implements Palette<T> {

    @Nullable
    private static Palette<BlockState> blockPalette;
    @Nullable
    private static GlobalPalette<BiomeType> biomePalette;

    private final Function<T, Integer> typeToInt;
    private final IntFunction<T> intToType;
    private final PaletteType<T> paletteType;
    private final Class<T> catalogType;
    private final int length;

    private GlobalPalette(PaletteType<T> paletteType, Function<T, Integer> map, IntFunction<T> identity, Class<T> catalogType) {
        int highest = 0;
        for (T type : Sponge.getRegistry().getAllOf(catalogType)) {
            int id = map.apply(type);
            if (id > highest) {
                highest = id;
            }
        }
        this.length = highest;
        this.typeToInt = map;
        this.intToType = identity;
        this.paletteType = paletteType;
        this.catalogType = catalogType;
    }

    @SuppressWarnings("deprecation")
    public static Palette<BlockState> getBlockPalette() {
        if (blockPalette == null) {
            blockPalette = new BlockPaletteWrapper(new GlobalPalette<>(PaletteTypes.GLOBAL_BLOCKS,
                (type) -> Block.BLOCK_STATE_IDS.get((net.minecraft.block.BlockState) type),
                (id) -> (BlockState) Block.BLOCK_STATE_IDS.getByValue(id),
                BlockState.class), org.spongepowered.api.world.schematic.BlockPaletteTypes.GLOBAL);
        }
        return blockPalette;
    }

    public static GlobalPalette<BiomeType> getBiomePalette() {
        if (biomePalette == null) {
            biomePalette = new GlobalPalette<>(PaletteTypes.GLOBAL_BIOMES,
                (type) -> Biome.func_185362_a((Biome) (type instanceof VirtualBiomeType ? ((VirtualBiomeType) type).getPersistedType() : type)),
                (id) -> (BiomeType) Biome.func_185357_a(id),
                BiomeType.class
                );

        }
        return biomePalette;
    }

    @Override
    public PaletteType<T> getType() {
        return this.paletteType;
    }

    @Override
    public int getHighestId() {
        return this.length;
    }

    @Override
    public Optional<Integer> get(T type) {
        return Optional.of(this.typeToInt.apply(type));
    }

    @Override
    public int getOrAssign(T state) {
        return this.typeToInt.apply(state);
    }

    @Override
    public Optional<T> get(int id) {
        return Optional.ofNullable(this.intToType.apply(id));
    }

    @Override
    public boolean remove(T state) {
        throw new UnsupportedOperationException("Cannot remove blockstates from the global palette");
    }

    @Override
    public Collection<T> getEntries() {
        return Sponge.getRegistry().getAllOf(this.catalogType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.paletteType, this.catalogType, this.length);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final GlobalPalette other = (GlobalPalette) obj;
        return Objects.equals(this.paletteType, other.paletteType)
               && Objects.equals(this.catalogType, other.catalogType)
               && Objects.equals(this.length, other.length);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("paletteType", this.paletteType)
            .add("catalogType", this.catalogType)
            .add("length", this.length)
            .toString();
    }
}
