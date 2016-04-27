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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.common.registry.RegistryHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BiomeTypeRegistryModule implements CatalogRegistryModule<BiomeType> {

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
        for (BiomeGenBase biome : BiomeGenBase.biomeRegistry) {
            if (biome != null) {
                this.biomeTypes.add((BiomeType) biome);
            }
        }
        this.biomeTypeMappings.put("ocean", (BiomeType) Biomes.ocean);
        this.biomeTypeMappings.put("plains", (BiomeType) Biomes.plains);
        this.biomeTypeMappings.put("desert", (BiomeType) Biomes.desert);
        this.biomeTypeMappings.put("extreme_hills", (BiomeType) Biomes.extremeHills);
        this.biomeTypeMappings.put("forest", (BiomeType) Biomes.forest);
        this.biomeTypeMappings.put("taiga", (BiomeType) Biomes.taiga);
        this.biomeTypeMappings.put("swampland", (BiomeType) Biomes.swampland);
        this.biomeTypeMappings.put("river", (BiomeType) Biomes.river);
        this.biomeTypeMappings.put("hell", (BiomeType) Biomes.hell);
        this.biomeTypeMappings.put("sky", (BiomeType) Biomes.sky);
        this.biomeTypeMappings.put("frozen_ocean", (BiomeType) Biomes.frozenOcean);
        this.biomeTypeMappings.put("frozen_river", (BiomeType) Biomes.frozenRiver);
        this.biomeTypeMappings.put("ice_plains", (BiomeType) Biomes.icePlains);
        this.biomeTypeMappings.put("ice_mountains", (BiomeType) Biomes.iceMountains);
        this.biomeTypeMappings.put("mushroom_island", (BiomeType) Biomes.mushroomIsland);
        this.biomeTypeMappings.put("mushroom_island_shore", (BiomeType) Biomes.mushroomIslandShore);
        this.biomeTypeMappings.put("beach", (BiomeType) Biomes.beach);
        this.biomeTypeMappings.put("desert_hills", (BiomeType) Biomes.desertHills);
        this.biomeTypeMappings.put("forest_hills", (BiomeType) Biomes.forestHills);
        this.biomeTypeMappings.put("taiga_hills", (BiomeType) Biomes.taigaHills);
        this.biomeTypeMappings.put("extreme_hills_edge", (BiomeType) Biomes.extremeHillsEdge);
        this.biomeTypeMappings.put("jungle", (BiomeType) Biomes.jungle);
        this.biomeTypeMappings.put("jungle_hills", (BiomeType) Biomes.jungleHills);
        this.biomeTypeMappings.put("jungle_edge", (BiomeType) Biomes.jungleEdge);
        this.biomeTypeMappings.put("deep_ocean", (BiomeType) Biomes.deepOcean);
        this.biomeTypeMappings.put("stone_beach", (BiomeType) Biomes.stoneBeach);
        this.biomeTypeMappings.put("cold_beach", (BiomeType) Biomes.coldBeach);
        this.biomeTypeMappings.put("birch_forest", (BiomeType) Biomes.birchForest);
        this.biomeTypeMappings.put("birch_forest_hills", (BiomeType) Biomes.birchForestHills);
        this.biomeTypeMappings.put("roofed_forest", (BiomeType) Biomes.roofedForest);
        this.biomeTypeMappings.put("cold_taiga", (BiomeType) Biomes.coldTaiga);
        this.biomeTypeMappings.put("cold_taiga_hills", (BiomeType) Biomes.coldTaigaHills);
        this.biomeTypeMappings.put("mega_taiga", (BiomeType) Biomes.megaTaiga);
        this.biomeTypeMappings.put("mega_taiga_hills", (BiomeType) Biomes.megaTaigaHills);
        this.biomeTypeMappings.put("extreme_hills_plus", (BiomeType) Biomes.extremeHillsPlus);
        this.biomeTypeMappings.put("savanna", (BiomeType) Biomes.savanna);
        this.biomeTypeMappings.put("savanna_plateau", (BiomeType) Biomes.savannaPlateau);
        this.biomeTypeMappings.put("mesa", (BiomeType) Biomes.mesa);
        this.biomeTypeMappings.put("mesa_plateau_forest", (BiomeType) Biomes.mesaPlateau_F);
        this.biomeTypeMappings.put("mesa_plateau", (BiomeType) Biomes.mesaPlateau);
        this.biomeTypeMappings.put("sunflower_plains", (BiomeType) Biomes.mutated_plains);
        this.biomeTypeMappings.put("desert_mountains", (BiomeType) Biomes.mutated_desert);
        this.biomeTypeMappings.put("flower_forest", (BiomeType) Biomes.mutated_forest);
        this.biomeTypeMappings.put("taiga_mountains", (BiomeType) Biomes.mutated_taiga);
        this.biomeTypeMappings.put("swampland_mountains", (BiomeType) Biomes.mutated_swampland);
        this.biomeTypeMappings.put("ice_plains_spikes", (BiomeType) Biomes.mutated_ice_flats);
        this.biomeTypeMappings.put("jungle_mountains", (BiomeType) Biomes.mutated_jungle);
        this.biomeTypeMappings.put("jungle_edge_mountains", (BiomeType) Biomes.mutated_jungle_edge);
        this.biomeTypeMappings.put("cold_taiga_mountains", (BiomeType) Biomes.mutated_taiga_cold);
        this.biomeTypeMappings.put("savanna_mountains", (BiomeType) Biomes.mutated_savanna);
        this.biomeTypeMappings.put("savanna_plateau_mountains", (BiomeType) Biomes.mutated_savanna_rock);
        this.biomeTypeMappings.put("mesa_bryce", (BiomeType) Biomes.mutated_mesa);
        this.biomeTypeMappings.put("mesa_plateau_forest_mountains", (BiomeType) Biomes.mutated_mesa_rock);
        this.biomeTypeMappings.put("mesa_plateau_mountains", (BiomeType) Biomes.mutated_mesa_clear_rock);
        this.biomeTypeMappings.put("birch_forest_mountains", (BiomeType) Biomes.mutated_birch_forest);
        this.biomeTypeMappings.put("birch_forest_hills_mountains", (BiomeType) Biomes.mutated_birch_forest_hills);
        this.biomeTypeMappings.put("roofed_forest_mountains", (BiomeType) Biomes.mutated_roofed_forest);
        this.biomeTypeMappings.put("mega_spruce_taiga", (BiomeType) Biomes.mutated_redwood_taiga);
        this.biomeTypeMappings.put("extreme_hills_mountains", (BiomeType) Biomes.mutated_extreme_hills);
        this.biomeTypeMappings.put("extreme_hills_plus_mountains", (BiomeType) Biomes.mutated_extreme_hills_with_trees);
        this.biomeTypeMappings.put("mega_spruce_taiga_hills", (BiomeType) Biomes.mutated_redwood_taiga_hills);
        this.biomeTypeMappings.put("void", (BiomeType) Biomes.voidBiome);
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (BiomeGenBase biome : BiomeGenBase.biomeRegistry) {
            if (biome != null && !this.biomeTypes.contains(biome)) {
                this.biomeTypes.add((BiomeType) biome);
                this.biomeTypeMappings.put(biome.getBiomeName().toLowerCase(Locale.ENGLISH), (BiomeType) biome);
            }
        }
        // Re-map fields in case mods have changed vanilla world types
        RegistryHelper.mapFields(BiomeTypes.class, this.biomeTypeMappings);
    }
}
