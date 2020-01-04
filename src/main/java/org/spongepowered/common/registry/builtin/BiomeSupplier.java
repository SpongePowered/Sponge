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
package org.spongepowered.common.registry.builtin;

import net.minecraft.world.biome.Biomes;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class BiomeSupplier {

    private BiomeSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(BiomeType.class, "ocean", () -> (BiomeType) Biomes.OCEAN)
            .registerSupplier(BiomeType.class, "default", () -> (BiomeType) Biomes.DEFAULT)
            .registerSupplier(BiomeType.class, "plains", () -> (BiomeType) Biomes.PLAINS)
            .registerSupplier(BiomeType.class, "desert", () -> (BiomeType) Biomes.DESERT)
            .registerSupplier(BiomeType.class, "mountains", () -> (BiomeType) Biomes.MOUNTAINS)
            .registerSupplier(BiomeType.class, "forest", () -> (BiomeType) Biomes.FOREST)
            .registerSupplier(BiomeType.class, "taiga", () -> (BiomeType) Biomes.TAIGA)
            .registerSupplier(BiomeType.class, "swamp", () -> (BiomeType) Biomes.SWAMP)
            .registerSupplier(BiomeType.class, "river", () -> (BiomeType) Biomes.RIVER)
            .registerSupplier(BiomeType.class, "nether", () -> (BiomeType) Biomes.NETHER)
            .registerSupplier(BiomeType.class, "the_end", () -> (BiomeType) Biomes.THE_END)
            .registerSupplier(BiomeType.class, "frozen_ocean", () -> (BiomeType) Biomes.FROZEN_OCEAN)
            .registerSupplier(BiomeType.class, "frozen_river", () -> (BiomeType) Biomes.FROZEN_RIVER)
            .registerSupplier(BiomeType.class, "snowy_tundra", () -> (BiomeType) Biomes.SNOWY_TUNDRA)
            .registerSupplier(BiomeType.class, "snowy_mountains", () -> (BiomeType) Biomes.SNOWY_MOUNTAINS)
            .registerSupplier(BiomeType.class, "mushroom_fields", () -> (BiomeType) Biomes.MUSHROOM_FIELDS)
            .registerSupplier(BiomeType.class, "mushroom_field_shore", () -> (BiomeType) Biomes.MUSHROOM_FIELD_SHORE)
            .registerSupplier(BiomeType.class, "beach", () -> (BiomeType) Biomes.BEACH)
            .registerSupplier(BiomeType.class, "desert_hills", () -> (BiomeType) Biomes.DESERT_HILLS)
            .registerSupplier(BiomeType.class, "wooded_hills", () -> (BiomeType) Biomes.WOODED_HILLS)
            .registerSupplier(BiomeType.class, "taiga_hills", () -> (BiomeType) Biomes.TAIGA_HILLS)
            .registerSupplier(BiomeType.class, "mountain_edge", () -> (BiomeType) Biomes.MOUNTAIN_EDGE)
            .registerSupplier(BiomeType.class, "jungle", () -> (BiomeType) Biomes.JUNGLE)
            .registerSupplier(BiomeType.class, "jungle_hills", () -> (BiomeType) Biomes.JUNGLE_HILLS)
            .registerSupplier(BiomeType.class, "jungle_edge", () -> (BiomeType) Biomes.JUNGLE_EDGE)
            .registerSupplier(BiomeType.class, "deep_ocean", () -> (BiomeType) Biomes.DEEP_OCEAN)
            .registerSupplier(BiomeType.class, "stone_shore", () -> (BiomeType) Biomes.STONE_SHORE)
            .registerSupplier(BiomeType.class, "snowy_beach", () -> (BiomeType) Biomes.SNOWY_BEACH)
            .registerSupplier(BiomeType.class, "birch_forest", () -> (BiomeType) Biomes.BIRCH_FOREST)
            .registerSupplier(BiomeType.class, "birch_forest_hills", () -> (BiomeType) Biomes.BIRCH_FOREST_HILLS)
            .registerSupplier(BiomeType.class, "dark_forest", () -> (BiomeType) Biomes.DARK_FOREST)
            .registerSupplier(BiomeType.class, "snowy_taiga", () -> (BiomeType) Biomes.SNOWY_TAIGA)
            .registerSupplier(BiomeType.class, "snowy_taiga_hills", () -> (BiomeType) Biomes.SNOWY_TAIGA_HILLS)
            .registerSupplier(BiomeType.class, "giant_tree_taiga", () -> (BiomeType) Biomes.GIANT_TREE_TAIGA)
            .registerSupplier(BiomeType.class, "giant_tree_taiga_hills", () -> (BiomeType) Biomes.GIANT_TREE_TAIGA_HILLS)
            .registerSupplier(BiomeType.class, "wooded_mountains", () -> (BiomeType) Biomes.WOODED_MOUNTAINS)
            .registerSupplier(BiomeType.class, "savanna", () -> (BiomeType) Biomes.SAVANNA)
            .registerSupplier(BiomeType.class, "savanna_plateau", () -> (BiomeType) Biomes.SAVANNA_PLATEAU)
            .registerSupplier(BiomeType.class, "badlands", () -> (BiomeType) Biomes.BADLANDS)
            .registerSupplier(BiomeType.class, "wooded_badlands_plateau", () -> (BiomeType) Biomes.WOODED_BADLANDS_PLATEAU)
            .registerSupplier(BiomeType.class, "badlands_plateau", () -> (BiomeType) Biomes.BADLANDS_PLATEAU)
            .registerSupplier(BiomeType.class, "small_end_islands", () -> (BiomeType) Biomes.SMALL_END_ISLANDS)
            .registerSupplier(BiomeType.class, "end_midlands", () -> (BiomeType) Biomes.END_MIDLANDS)
            .registerSupplier(BiomeType.class, "end_highlands", () -> (BiomeType) Biomes.END_HIGHLANDS)
            .registerSupplier(BiomeType.class, "end_barrens", () -> (BiomeType) Biomes.END_BARRENS)
            .registerSupplier(BiomeType.class, "warm_ocean", () -> (BiomeType) Biomes.WARM_OCEAN)
            .registerSupplier(BiomeType.class, "lukewarm_ocean", () -> (BiomeType) Biomes.LUKEWARM_OCEAN)
            .registerSupplier(BiomeType.class, "cold_ocean", () -> (BiomeType) Biomes.COLD_OCEAN)
            .registerSupplier(BiomeType.class, "deep_warm_ocean", () -> (BiomeType) Biomes.DEEP_WARM_OCEAN)
            .registerSupplier(BiomeType.class, "deep_lukewarm_ocean", () -> (BiomeType) Biomes.DEEP_LUKEWARM_OCEAN)
            .registerSupplier(BiomeType.class, "deep_cold_ocean", () -> (BiomeType) Biomes.DEEP_COLD_OCEAN)
            .registerSupplier(BiomeType.class, "deep_frozen_ocean", () -> (BiomeType) Biomes.DEEP_FROZEN_OCEAN)
            .registerSupplier(BiomeType.class, "the_void", () -> (BiomeType) Biomes.THE_VOID)
            .registerSupplier(BiomeType.class, "sunflower_plains", () -> (BiomeType) Biomes.SUNFLOWER_PLAINS)
            .registerSupplier(BiomeType.class, "desert_lakes", () -> (BiomeType) Biomes.DESERT_LAKES)
            .registerSupplier(BiomeType.class, "gravelly_mountains", () -> (BiomeType) Biomes.GRAVELLY_MOUNTAINS)
            .registerSupplier(BiomeType.class, "flower_forest", () -> (BiomeType) Biomes.FLOWER_FOREST)
            .registerSupplier(BiomeType.class, "taiga_mountains", () -> (BiomeType) Biomes.TAIGA_MOUNTAINS)
            .registerSupplier(BiomeType.class, "swamp_hills", () -> (BiomeType) Biomes.SWAMP_HILLS)
            .registerSupplier(BiomeType.class, "ice_spikes", () -> (BiomeType) Biomes.ICE_SPIKES)
            .registerSupplier(BiomeType.class, "modified_jungle", () -> (BiomeType) Biomes.MODIFIED_JUNGLE)
            .registerSupplier(BiomeType.class, "modified_jungle_edge", () -> (BiomeType) Biomes.MODIFIED_JUNGLE_EDGE)
            .registerSupplier(BiomeType.class, "tall_birch_forest", () -> (BiomeType) Biomes.TALL_BIRCH_FOREST)
            .registerSupplier(BiomeType.class, "tall_birch_hills", () -> (BiomeType) Biomes.TALL_BIRCH_HILLS)
            .registerSupplier(BiomeType.class, "dark_forest_hills", () -> (BiomeType) Biomes.DARK_FOREST_HILLS)
            .registerSupplier(BiomeType.class, "snowy_taiga_mountains", () -> (BiomeType) Biomes.SNOWY_TAIGA_MOUNTAINS)
            .registerSupplier(BiomeType.class, "giant_spruce_taiga", () -> (BiomeType) Biomes.GIANT_SPRUCE_TAIGA)
            .registerSupplier(BiomeType.class, "giant_spruce_taiga_hills", () -> (BiomeType) Biomes.GIANT_SPRUCE_TAIGA_HILLS)
            .registerSupplier(BiomeType.class, "modified_gravelly_mountains", () -> (BiomeType) Biomes.MODIFIED_GRAVELLY_MOUNTAINS)
            .registerSupplier(BiomeType.class, "shattered_savanna", () -> (BiomeType) Biomes.SHATTERED_SAVANNA)
            .registerSupplier(BiomeType.class, "shattered_savanna_plateau", () -> (BiomeType) Biomes.SHATTERED_SAVANNA_PLATEAU)
            .registerSupplier(BiomeType.class, "eroded_badlands", () -> (BiomeType) Biomes.ERODED_BADLANDS)
            .registerSupplier(BiomeType.class, "modified_wooded_badlands_plateau", () -> (BiomeType) Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU)
            .registerSupplier(BiomeType.class, "modified_badlands_plateau", () -> (BiomeType) Biomes.MODIFIED_BADLANDS_PLATEAU)
            .registerSupplier(BiomeType.class, "bamboo_jungle", () -> (BiomeType) Biomes.BAMBOO_JUNGLE)
            .registerSupplier(BiomeType.class, "bamboo_jungle_hills", () -> (BiomeType) Biomes.BAMBOO_JUNGLE_HILLS)
        ;
    }
}
