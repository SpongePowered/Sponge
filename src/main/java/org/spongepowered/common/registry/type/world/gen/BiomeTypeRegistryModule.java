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
import java.util.Map;
import java.util.Optional;

public final class BiomeTypeRegistryModule implements CatalogRegistryModule<BiomeType> {

    @RegisterCatalog(BiomeTypes.class)
    private final Map<String, BiomeType> biomeTypeMappings = Maps.newHashMap();

    private final List<BiomeType> biomeTypes = new ArrayList<>();

    @Override
    public Optional<BiomeType> getById(String id) {
        return Optional.ofNullable(this.biomeTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<BiomeType> getAll() {
        return ImmutableList.copyOf(this.biomeTypes);
    }

    @Override
    public void registerDefaults() {
        BiomeGenBase[] biomeArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeArray) {
            if (biome != null) {
                this.biomeTypes.add((BiomeType) biome);
            }
        }
        this.biomeTypeMappings.put("ocean", (BiomeType) BiomeGenBase.ocean);
        this.biomeTypeMappings.put("plains", (BiomeType) BiomeGenBase.plains);
        this.biomeTypeMappings.put("desert", (BiomeType) BiomeGenBase.desert);
        this.biomeTypeMappings.put("extreme_hills", (BiomeType) BiomeGenBase.extremeHills);
        this.biomeTypeMappings.put("forest", (BiomeType) BiomeGenBase.forest);
        this.biomeTypeMappings.put("taiga", (BiomeType) BiomeGenBase.taiga);
        this.biomeTypeMappings.put("swampland", (BiomeType) BiomeGenBase.swampland);
        this.biomeTypeMappings.put("river", (BiomeType) BiomeGenBase.river);
        this.biomeTypeMappings.put("hell", (BiomeType) BiomeGenBase.hell);
        this.biomeTypeMappings.put("sky", (BiomeType) BiomeGenBase.sky);
        this.biomeTypeMappings.put("frozen_ocean", (BiomeType) BiomeGenBase.frozenOcean);
        this.biomeTypeMappings.put("frozen_river", (BiomeType) BiomeGenBase.frozenRiver);
        this.biomeTypeMappings.put("ice_plains", (BiomeType) BiomeGenBase.icePlains);
        this.biomeTypeMappings.put("ice_mountains", (BiomeType) BiomeGenBase.iceMountains);
        this.biomeTypeMappings.put("mushroom_island", (BiomeType) BiomeGenBase.mushroomIsland);
        this.biomeTypeMappings.put("mushroom_island_shore", (BiomeType) BiomeGenBase.mushroomIslandShore);
        this.biomeTypeMappings.put("beach", (BiomeType) BiomeGenBase.beach);
        this.biomeTypeMappings.put("desert_hills", (BiomeType) BiomeGenBase.desertHills);
        this.biomeTypeMappings.put("forest_hills", (BiomeType) BiomeGenBase.forestHills);
        this.biomeTypeMappings.put("taiga_hills", (BiomeType) BiomeGenBase.taigaHills);
        this.biomeTypeMappings.put("extreme_hills_edge", (BiomeType) BiomeGenBase.extremeHillsEdge);
        this.biomeTypeMappings.put("jungle", (BiomeType) BiomeGenBase.jungle);
        this.biomeTypeMappings.put("jungle_hills", (BiomeType) BiomeGenBase.jungleHills);
        this.biomeTypeMappings.put("jungle_edge", (BiomeType) BiomeGenBase.jungleEdge);
        this.biomeTypeMappings.put("deep_ocean", (BiomeType) BiomeGenBase.deepOcean);
        this.biomeTypeMappings.put("stone_beach", (BiomeType) BiomeGenBase.stoneBeach);
        this.biomeTypeMappings.put("cold_beach", (BiomeType) BiomeGenBase.coldBeach);
        this.biomeTypeMappings.put("birch_forest", (BiomeType) BiomeGenBase.birchForest);
        this.biomeTypeMappings.put("birch_forest_hills", (BiomeType) BiomeGenBase.birchForestHills);
        this.biomeTypeMappings.put("roofed_forest", (BiomeType) BiomeGenBase.roofedForest);
        this.biomeTypeMappings.put("cold_taiga", (BiomeType) BiomeGenBase.coldTaiga);
        this.biomeTypeMappings.put("cold_taiga_hills", (BiomeType) BiomeGenBase.coldTaigaHills);
        this.biomeTypeMappings.put("mega_taiga", (BiomeType) BiomeGenBase.megaTaiga);
        this.biomeTypeMappings.put("mega_taiga_hills", (BiomeType) BiomeGenBase.megaTaigaHills);
        this.biomeTypeMappings.put("extreme_hills_plus", (BiomeType) BiomeGenBase.extremeHillsPlus);
        this.biomeTypeMappings.put("savanna", (BiomeType) BiomeGenBase.savanna);
        this.biomeTypeMappings.put("savanna_plateau", (BiomeType) BiomeGenBase.savannaPlateau);
        this.biomeTypeMappings.put("mesa", (BiomeType) BiomeGenBase.mesa);
        this.biomeTypeMappings.put("mesa_plateau_forest", (BiomeType) BiomeGenBase.mesaPlateau_F);
        this.biomeTypeMappings.put("mesa_plateau", (BiomeType) BiomeGenBase.mesaPlateau);
        this.biomeTypeMappings.put("sunflower_plains", (BiomeType) biomeArray[BiomeGenBase.plains.biomeID + 128]);
        this.biomeTypeMappings.put("desert_mountains", (BiomeType) biomeArray[BiomeGenBase.desert.biomeID + 128]);
        this.biomeTypeMappings.put("flower_forest", (BiomeType) biomeArray[BiomeGenBase.forest.biomeID + 128]);
        this.biomeTypeMappings.put("taiga_mountains", (BiomeType) biomeArray[BiomeGenBase.taiga.biomeID + 128]);
        this.biomeTypeMappings.put("swampland_mountains", (BiomeType) biomeArray[BiomeGenBase.swampland.biomeID + 128]);
        this.biomeTypeMappings.put("ice_plains_spikes", (BiomeType) biomeArray[BiomeGenBase.icePlains.biomeID + 128]);
        this.biomeTypeMappings.put("jungle_mountains", (BiomeType) biomeArray[BiomeGenBase.jungle.biomeID + 128]);
        this.biomeTypeMappings.put("jungle_edge_mountains", (BiomeType) biomeArray[BiomeGenBase.jungleEdge.biomeID + 128]);
        this.biomeTypeMappings.put("cold_taiga_mountains", (BiomeType) biomeArray[BiomeGenBase.coldTaiga.biomeID + 128]);
        this.biomeTypeMappings.put("savanna_mountains", (BiomeType) biomeArray[BiomeGenBase.savanna.biomeID + 128]);
        this.biomeTypeMappings.put("savanna_plateau_mountains", (BiomeType) biomeArray[BiomeGenBase.savannaPlateau.biomeID + 128]);
        this.biomeTypeMappings.put("mesa_bryce", (BiomeType) biomeArray[BiomeGenBase.mesa.biomeID + 128]);
        this.biomeTypeMappings.put("mesa_plateau_forest_mountains", (BiomeType) biomeArray[BiomeGenBase.mesaPlateau_F.biomeID + 128]);
        this.biomeTypeMappings.put("mesa_plateau_mountains", (BiomeType) biomeArray[BiomeGenBase.mesaPlateau.biomeID + 128]);
        this.biomeTypeMappings.put("birch_forest_mountains", (BiomeType) biomeArray[BiomeGenBase.birchForest.biomeID + 128]);
        this.biomeTypeMappings.put("birch_forest_hills_mountains", (BiomeType) biomeArray[BiomeGenBase.birchForestHills.biomeID + 128]);
        this.biomeTypeMappings.put("roofed_forest_mountains", (BiomeType) biomeArray[BiomeGenBase.roofedForest.biomeID + 128]);
        this.biomeTypeMappings.put("mega_spruce_taiga", (BiomeType) biomeArray[BiomeGenBase.megaTaiga.biomeID + 128]);
        this.biomeTypeMappings.put("extreme_hills_mountains", (BiomeType) biomeArray[BiomeGenBase.extremeHills.biomeID + 128]);
        this.biomeTypeMappings.put("extreme_hills_plus_mountains", (BiomeType) biomeArray[BiomeGenBase.extremeHillsPlus.biomeID + 128]);
        this.biomeTypeMappings.put("mega_spruce_taiga_hills", (BiomeType) biomeArray[BiomeGenBase.megaTaigaHills.biomeID + 128]);
    }

    @AdditionalRegistration
    public void registerAdditional() {
        BiomeGenBase[] biomeArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeArray) {
            if (biome != null && !this.biomeTypes.contains(biome)) {
                this.biomeTypes.add((BiomeType) biome);
                this.biomeTypeMappings.put(biome.biomeName.toLowerCase(), (BiomeType) biome);
            }
        }
        // Re-map fields in case mods have changed vanilla world types
        RegistryHelper.mapFields(BiomeTypes.class, this.biomeTypeMappings);
    }
}
