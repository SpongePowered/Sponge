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
package org.spongepowered.vanilla.generator.world.level;

import com.google.common.base.CaseFormat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import org.spongepowered.vanilla.generator.Context;
import org.spongepowered.vanilla.generator.Generator;
import org.spongepowered.vanilla.generator.MapEntriesValidator;
import org.spongepowered.vanilla.generator.RegistryEntriesGenerator;
import org.spongepowered.vanilla.generator.RegistryEntriesValidator;
import org.spongepowered.vanilla.generator.RegistryScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class LevelDataRegistries {

    public static List<Generator> levelDataRegistries(final Context context) {
        return List.<Generator>of(
            new MapEntriesValidator<>(
                "world.gamerule",
                "GameRules",
                GameRules.class,
                "GAME_RULE_TYPES",
                map -> {
                    final Map<ResourceLocation, Object> out = new HashMap<>(map.size());
                    map.forEach((BiConsumer<Object, Object>) (k, v) -> {
                        var key = (GameRules.Key<?>) k;
                        out.put(ResourceLocation.fromNamespaceAndPath("sponge", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key.getId())), v);
                    });
                    return out;
                }
            ),
            new RegistryEntriesGenerator<>(
                "map.decoration",
                "MapDecorationTypes",
                "MAP_DECORATION_TYPE",
                context.relativeClass("map.decoration", "MapDecorationType"),
                Registries.MAP_DECORATION_TYPE
            ),
            new RegistryEntriesGenerator<>(
                "world.biome",
                "Biomes",
                "BIOME",
                context.relativeClass("world.biome", "Biome"),
                Registries.BIOME,
                a -> true,
                RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.carver",
                "CarverTypes",
                "CARVER_TYPE",
                context.relativeClass("world.generation.carver", "CarverType"),
                Registries.CARVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.carver",
                "Carvers",
                "CARVER",
                context.relativeClass("world.generation.carver", "Carver"),
                Registries.CONFIGURED_CARVER,
                a -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.config.noise",
                "NoiseGeneratorConfigs",
                "NOISE_GENERATOR_CONFIG",
                context.relativeClass("world.generation.config.noise", "NoiseGeneratorConfig"),
                Registries.NOISE_SETTINGS,
                a -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.config.flat",
                "FlatGeneratorConfigs",
                "FLAT_GENERATOR_CONFIG",
                context.relativeClass("world.generation.config.flat", "FlatGeneratorConfig"),
                Registries.FLAT_LEVEL_GENERATOR_PRESET,
                a -> true, RegistryScope.GAME
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.config.noise",
                "Noises",
                "NOISE",
                context.relativeClass("world.generation.config.noise", "Noise"),
                Registries.NOISE,
                a -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.config.noise",
                "DensityFunctions",
                "DENSITY_FUNCTION",
                context.relativeClass("world.generation.config.noise", "DensityFunction"),
                Registries.DENSITY_FUNCTION,
                a -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesValidator<>(
                "world.chunk",
                "ChunkStates",
                Registries.CHUNK_STATUS
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure",
                "StructureTypes",
                "STRUCTURE_TYPE",
                context.relativeClass("world.generation.structure", "StructureType"),
                Registries.STRUCTURE_TYPE
            ),

            new RegistryEntriesGenerator<>(
                "world.generation.structure",
                "StructureSets",
                "STRUCTURE_SET",
                context.relativeClass("world.generation.structure", "StructureSet"),
                Registries.STRUCTURE_SET,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure.jigsaw",
                "JigsawPools",
                "JIGSAW_POOL",
                context.relativeClass("world.generation.structure.jigsaw", "JigsawPool"),
                Registries.TEMPLATE_POOL,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure.jigsaw",
                "ProcessorLists",
                "PROCESSOR_LIST",
                context.relativeClass("world.generation.structure.jigsaw", "ProcessorList"),
                Registries.PROCESSOR_LIST,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure.jigsaw",
                "ProcessorTypes",
                "PROCESSOR_TYPE",
                context.relativeClass("world.generation.structure.jigsaw", "ProcessorType"),
                Registries.STRUCTURE_PROCESSOR,
                $ -> true, RegistryScope.GAME
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.feature",
                "PlacedFeatures",
                "PLACED_FEATURE",
                context.relativeClass("world.generation.feature", "PlacedFeature"),
                Registries.PLACED_FEATURE,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.feature",
                "Features",
                "FEATURE",
                context.relativeClass("world.generation.feature", "Feature"),
                Registries.CONFIGURED_FEATURE,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.feature",
                "FeatureTypes",
                "FEATURE_TYPE",
                context.relativeClass("world.generation.feature", "FeatureType"),
                Registries.FEATURE,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.feature",
                "PlacementModifierTypes",
                "PLACEMENT_MODIFIER",
                context.relativeClass("world.generation.feature", "PlacementModifierType"),
                Registries.PLACEMENT_MODIFIER_TYPE,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world",
                "WorldTypes",
                "WORLD_TYPE",
                context.relativeClass("world", "WorldType"),
                Registries.DIMENSION_TYPE,
                $ -> true,
                RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure",
                "Structures",
                "STRUCTURE",
                context.relativeClass("world.generation.structure", "Structure"),
                Registries.STRUCTURE,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "fluid",
                "FluidTypes",
                "FLUID_TYPE",
                context.relativeClass("fluid", "FluidType"),
                Registries.FLUID
            ),
            new RegistryEntriesGenerator<>(
                "world.server",
                "WorldArchetypeTypes",
                "WORLD_ARCHETYPE_TYPE",
                context.relativeClass("world.server", "WorldArchetypeType"),
                Registries.LEVEL_STEM,
                $ -> true,
                RegistryScope.SERVER
            )
        );
    }
}
