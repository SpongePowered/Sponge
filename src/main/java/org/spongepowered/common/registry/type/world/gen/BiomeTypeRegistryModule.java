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

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.common.bridge.world.biome.BiomeBridge;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.ArrayList;
import java.util.List;

@RegisterCatalog(BiomeTypes.class)
public final class BiomeTypeRegistryModule
    extends AbstractPrefixAlternateCatalogTypeRegistryModule<BiomeType>
    implements AdditionalCatalogRegistryModule<BiomeType> {

    private final List<BiomeType> biomeTypes = new ArrayList<>();

    public BiomeTypeRegistryModule() {
        super("minecraft",
            new String[] {"minecraft:"},
            string -> {
                final String alternateKey = MINECRAFT_TO_SPONGE_FIELD_NAMES.get(string);
                return alternateKey == null ? string : alternateKey;
            });
    }

    @Override
    public void registerDefaults() {
        for (Biome biome : Biome.field_185377_q) {
            if (biome != null) {
                String id = ((BiomeType) biome).getId();
                if (id == null) {
                    ResourceLocation reg_id = Biome.field_185377_q.getKey(biome);
                    ((BiomeBridge) biome).bridge$setModId(reg_id.getNamespace());
                    id = reg_id.toString();
                    ((BiomeBridge) biome).bridge$setId(id);
                }
                this.biomeTypes.add((BiomeType) biome);
                this.catalogTypeMap.put(id, (BiomeType) biome);
            }
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (Biome biome : Biome.field_185377_q) {
            if (biome != null && !this.biomeTypes.contains(biome)) {
                String id = ((BiomeType) biome).getId();
                if (id == null) {
                    ResourceLocation reg_id = Biome.field_185377_q.getKey(biome);
                    ((BiomeBridge) biome).bridge$setModId(reg_id.getNamespace());
                    id = reg_id.toString();
                    ((BiomeBridge) biome).bridge$setId(id);
                }
                this.biomeTypes.add((BiomeType) biome);
                this.catalogTypeMap.put(id, (BiomeType) biome);
            }
        }
        // Re-map fields in case mods have changed vanilla world types
        RegistryHelper.mapFields(BiomeTypes.class, provideCatalogMap());
    }

    public static final ImmutableMap<String, String> MINECRAFT_TO_SPONGE_FIELD_NAMES = ImmutableMap.<String, String>builder()
        .put("ice_flats", "ice_plains")
        .put("beaches", "beach")
        .put("smaller_extreme_hills", "extreme_hills_edge")
        .put("birch_forest_hills", "birch_forest_hills")
        .put("roofed_forest", "roofed_forest")
        .put("taiga_cold", "cold_taiga")
        .put("taiga_cold_hills", "cold_taiga_hills")
        .put("redwood_taiga", "mega_taiga")
        .put("redwood_taiga_hills", "mega_taiga_hills")
        .put("extreme_hills_with_trees", "extreme_hills_plus")
        .put("savanna_rock", "savanna_plateau")
        .put("mesa_rock", "mesa_plateau_forest")
        .put("mesa_clear_rock", "mesa_plateau")
        .put("mutated_plains", "sunflower_plains")
        .put("mutated_desert", "desert_mountains")
        .put("mutated_extreme_hills", "extreme_hills_mountains")
        .put("mutated_forest", "flower_forest")
        .put("mutated_taiga", "taiga_mountains")
        .put("mutated_swampland", "swampland_mountains")
        .put("mutated_ice_flats", "ice_plains_spikes")
        .put("mutated_jungle", "jungle_mountains")
        .put("mutated_jungle_edge", "jungle_edge_mountains")
        .put("mutated_birch_forest", "birch_forest_mountains")
        .put("mutated_birch_forest_hills", "birch_forest_hills_mountains")
        .put("mutated_roofed_forest", "roofed_forest_mountains")
        .put("mutated_taiga_cold", "cold_taiga_mountains")
        .put("mutated_redwood_taiga", "mega_spruce_taiga")
        .put("mutated_redwood_taiga_hills", "mega_spruce_taiga_hills")
        .put("mutated_extreme_hills_with_trees", "extreme_hills_plus_mountains")
        .put("mutated_savanna", "savanna_mountains")
        .put("mutated_savanna_rock", "savanna_plateau_mountains")
        .put("mutated_mesa", "mesa_bryce")
        .put("mutated_mesa_rock", "mesa_plateau_forest_mountains")
        .put("mutated_mesa_clear_rock", "mesa_plateau_mountains")
        .build();

    @Override
    public void registerAdditionalCatalog(BiomeType biome) {
        checkNotNull(biome);
        checkArgument(biome instanceof VirtualBiomeType, "Cannot register non-virtual biomes at this time.");
        checkArgument(!getById(biome.getId()).isPresent(), "Duplicate biome id");

        this.biomeTypes.add(biome);
    }

}
