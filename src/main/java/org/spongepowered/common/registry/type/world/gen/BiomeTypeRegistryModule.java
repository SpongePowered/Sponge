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
package org.spongepowered.common.registry.type.world.gen;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.common.registry.RegistryHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BiomeTypeRegistryModule implements AdditionalCatalogRegistryModule<BiomeType> {

    @RegisterCatalog(BiomeTypes.class)
    private final Map<String, BiomeType> biomeTypeMappings = Maps.newHashMap();

    private final List<BiomeType> biomeTypes = new ArrayList<>();

    @Override
    public Optional<BiomeType> getById(String id) {
        return Optional.ofNullable(this.biomeTypeMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<BiomeType> getAll() {
        return ImmutableList.copyOf(this.biomeTypes);
    }

    @Override
    public void registerDefaults() {
        for (Biome biome : Biome.REGISTRY) {
            if (biome != null) {
                this.biomeTypes.add((BiomeType) biome);
            }
        }
        this.biomeTypeMappings.put("ocean", (BiomeType) Biomes.OCEAN);
        this.biomeTypeMappings.put("plains", (BiomeType) Biomes.PLAINS);
        this.biomeTypeMappings.put("desert", (BiomeType) Biomes.DESERT);
        this.biomeTypeMappings.put("extreme_hills", (BiomeType) Biomes.EXTREME_HILLS);
        this.biomeTypeMappings.put("forest", (BiomeType) Biomes.FOREST);
        this.biomeTypeMappings.put("taiga", (BiomeType) Biomes.TAIGA);
        this.biomeTypeMappings.put("swampland", (BiomeType) Biomes.SWAMPLAND);
        this.biomeTypeMappings.put("river", (BiomeType) Biomes.RIVER);
        this.biomeTypeMappings.put("hell", (BiomeType) Biomes.HELL);
        this.biomeTypeMappings.put("sky", (BiomeType) Biomes.SKY);
        this.biomeTypeMappings.put("frozen_ocean", (BiomeType) Biomes.FROZEN_OCEAN);
        this.biomeTypeMappings.put("frozen_river", (BiomeType) Biomes.FROZEN_RIVER);
        this.biomeTypeMappings.put("ice_plains", (BiomeType) Biomes.ICE_PLAINS);
        this.biomeTypeMappings.put("ice_mountains", (BiomeType) Biomes.ICE_MOUNTAINS);
        this.biomeTypeMappings.put("mushroom_island", (BiomeType) Biomes.MUSHROOM_ISLAND);
        this.biomeTypeMappings.put("mushroom_island_shore", (BiomeType) Biomes.MUSHROOM_ISLAND_SHORE);
        this.biomeTypeMappings.put("beach", (BiomeType) Biomes.BEACH);
        this.biomeTypeMappings.put("desert_hills", (BiomeType) Biomes.DESERT_HILLS);
        this.biomeTypeMappings.put("forest_hills", (BiomeType) Biomes.FOREST_HILLS);
        this.biomeTypeMappings.put("taiga_hills", (BiomeType) Biomes.TAIGA_HILLS);
        this.biomeTypeMappings.put("extreme_hills_edge", (BiomeType) Biomes.EXTREME_HILLS_EDGE);
        this.biomeTypeMappings.put("jungle", (BiomeType) Biomes.JUNGLE);
        this.biomeTypeMappings.put("jungle_hills", (BiomeType) Biomes.JUNGLE_HILLS);
        this.biomeTypeMappings.put("jungle_edge", (BiomeType) Biomes.JUNGLE_EDGE);
        this.biomeTypeMappings.put("deep_ocean", (BiomeType) Biomes.DEEP_OCEAN);
        this.biomeTypeMappings.put("stone_beach", (BiomeType) Biomes.STONE_BEACH);
        this.biomeTypeMappings.put("cold_beach", (BiomeType) Biomes.COLD_BEACH);
        this.biomeTypeMappings.put("birch_forest", (BiomeType) Biomes.BIRCH_FOREST);
        this.biomeTypeMappings.put("birch_forest_hills", (BiomeType) Biomes.BIRCH_FOREST_HILLS);
        this.biomeTypeMappings.put("roofed_forest", (BiomeType) Biomes.ROOFED_FOREST);
        this.biomeTypeMappings.put("cold_taiga", (BiomeType) Biomes.COLD_TAIGA);
        this.biomeTypeMappings.put("cold_taiga_hills", (BiomeType) Biomes.COLD_TAIGA_HILLS);
        this.biomeTypeMappings.put("mega_taiga", (BiomeType) Biomes.REDWOOD_TAIGA);
        this.biomeTypeMappings.put("mega_taiga_hills", (BiomeType) Biomes.REDWOOD_TAIGA_HILLS);
        this.biomeTypeMappings.put("extreme_hills_plus", (BiomeType) Biomes.EXTREME_HILLS_WITH_TREES);
        this.biomeTypeMappings.put("savanna", (BiomeType) Biomes.SAVANNA);
        this.biomeTypeMappings.put("savanna_plateau", (BiomeType) Biomes.SAVANNA_PLATEAU);
        this.biomeTypeMappings.put("mesa", (BiomeType) Biomes.MESA);
        this.biomeTypeMappings.put("mesa_plateau_forest", (BiomeType) Biomes.MESA_ROCK);
        this.biomeTypeMappings.put("mesa_plateau", (BiomeType) Biomes.MESA_CLEAR_ROCK);
        this.biomeTypeMappings.put("sunflower_plains", (BiomeType) Biomes.MUTATED_PLAINS);
        this.biomeTypeMappings.put("desert_mountains", (BiomeType) Biomes.MUTATED_DESERT);
        this.biomeTypeMappings.put("flower_forest", (BiomeType) Biomes.MUTATED_FOREST);
        this.biomeTypeMappings.put("taiga_mountains", (BiomeType) Biomes.MUTATED_TAIGA);
        this.biomeTypeMappings.put("swampland_mountains", (BiomeType) Biomes.MUTATED_SWAMPLAND);
        this.biomeTypeMappings.put("ice_plains_spikes", (BiomeType) Biomes.MUTATED_ICE_FLATS);
        this.biomeTypeMappings.put("jungle_mountains", (BiomeType) Biomes.MUTATED_JUNGLE);
        this.biomeTypeMappings.put("jungle_edge_mountains", (BiomeType) Biomes.MUTATED_JUNGLE_EDGE);
        this.biomeTypeMappings.put("cold_taiga_mountains", (BiomeType) Biomes.MUTATED_TAIGA_COLD);
        this.biomeTypeMappings.put("savanna_mountains", (BiomeType) Biomes.MUTATED_SAVANNA);
        this.biomeTypeMappings.put("savanna_plateau_mountains", (BiomeType) Biomes.MUTATED_SAVANNA_ROCK);
        this.biomeTypeMappings.put("mesa_bryce", (BiomeType) Biomes.MUTATED_MESA);
        this.biomeTypeMappings.put("mesa_plateau_forest_mountains", (BiomeType) Biomes.MUTATED_MESA_ROCK);
        this.biomeTypeMappings.put("mesa_plateau_mountains", (BiomeType) Biomes.MUTATED_MESA_CLEAR_ROCK);
        this.biomeTypeMappings.put("birch_forest_mountains", (BiomeType) Biomes.MUTATED_BIRCH_FOREST);
        this.biomeTypeMappings.put("birch_forest_hills_mountains", (BiomeType) Biomes.MUTATED_BIRCH_FOREST_HILLS);
        this.biomeTypeMappings.put("roofed_forest_mountains", (BiomeType) Biomes.MUTATED_ROOFED_FOREST);
        this.biomeTypeMappings.put("mega_spruce_taiga", (BiomeType) Biomes.MUTATED_REDWOOD_TAIGA);
        this.biomeTypeMappings.put("extreme_hills_mountains", (BiomeType) Biomes.MUTATED_EXTREME_HILLS);
        this.biomeTypeMappings.put("extreme_hills_plus_mountains", (BiomeType) Biomes.MUTATED_EXTREME_HILLS_WITH_TREES);
        this.biomeTypeMappings.put("mega_spruce_taiga_hills", (BiomeType) Biomes.MUTATED_REDWOOD_TAIGA_HILLS);
        this.biomeTypeMappings.put("void", (BiomeType) Biomes.VOID);
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (Biome biome : Biome.REGISTRY) {
            if (biome != null && !this.biomeTypes.contains(biome)) {
                this.biomeTypes.add((BiomeType) biome);
                this.biomeTypeMappings.put(biome.getBiomeName().toLowerCase(Locale.ENGLISH), (BiomeType) biome);
            }
        }
        // Re-map fields in case mods have changed vanilla world types
        RegistryHelper.mapFields(BiomeTypes.class, this.biomeTypeMappings);
    }

    @Override
    public void registerAdditionalCatalog(BiomeType biome) {
        checkNotNull(biome);
        checkArgument(biome instanceof VirtualBiomeType, "Cannot register non-virtual biomes at this time.");
        checkArgument(!getById(biome.getId()).isPresent(), "Duplicate biome id");

        this.biomeTypes.add(biome);
        this.biomeTypeMappings.put(biome.getId().toLowerCase(Locale.ENGLISH), biome);
    }
}
