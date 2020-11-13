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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.common.block.BlockStateSerializerDeserializer;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.MutableBimapPalette;
import org.spongepowered.common.world.schematic.SpongePaletteType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class PaletteTypeStreamGenerator {

    private PaletteTypeStreamGenerator() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Stream<PaletteType> stream() {
        final List<PaletteType> paletteTypes = new ArrayList<>();
        paletteTypes.add(new SpongePaletteType<BlockState>(
            ResourceKey.sponge("global_block_palette"),
            GlobalPalette::getBlockPalette,
            BlockStateSerializerDeserializer::serialize,
            BlockStateSerializerDeserializer::deserialize
        ));
        paletteTypes.add(new SpongePaletteType<>(
            ResourceKey.sponge("block_state_palette"),
            () -> new MutableBimapPalette<>(PaletteTypes.BLOCK_STATE_PALETTE.get()),
            BlockStateSerializerDeserializer::serialize,
            BlockStateSerializerDeserializer::deserialize
        ));
        paletteTypes.add(new SpongePaletteType<BiomeType>(
            ResourceKey.sponge("global_biome_palette"),
            GlobalPalette::getBiomePalette,
            (type) -> {
                final ResourceLocation key = Registry.BIOME.getKey((Biome) (
                    type instanceof VirtualBiomeType
                        ? ((VirtualBiomeType) type).getPersistedType()
                        : type
                ));
                if (key == null) {
                    return "minecraft:plains";
                }
                return key.toString();
            },
            (id) -> (Optional<BiomeType>) (Optional) Registry.BIOME.getValue(ResourceLocation.tryCreate(id))
        ));
        paletteTypes.add(new SpongePaletteType<>(
            ResourceKey.sponge("biome_palette"),
            () -> new MutableBimapPalette<>(PaletteTypes.BIOME_PALETTE.get()),
            (type) -> {
                final ResourceLocation key = Registry.BIOME.getKey((Biome) (
                    type instanceof VirtualBiomeType
                        ? ((VirtualBiomeType) type).getPersistedType()
                        : type
                ));
                if (key == null) {
                    return "minecraft:plains";
                }
                return key.toString();
            },
            (id) -> (Optional<BiomeType>) (Optional) Registry.BIOME.getValue(ResourceLocation.tryCreate(id))
        ));

        return paletteTypes.stream();
    }


}
