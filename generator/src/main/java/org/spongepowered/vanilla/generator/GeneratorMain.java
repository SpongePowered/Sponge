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
package org.spongepowered.vanilla.generator;

import com.github.javaparser.utils.Log;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A generator that will output source code containing constants used in <em>Minecraft: Java Edition</em>.
 */
public final class GeneratorMain {

    private GeneratorMain() {
    }

    /**
     * The entry point.
     *
     * @param args arguments, expected to be {@code <output directory> }
     */
    public static void main(final String[] args) {
        Logger.info("Begining bootstrap");
        Log.setAdapter(new JavaparserLog());
        Bootstrap.bootStrap();
        Bootstrap.validate();

        // Create a generator context based on arguments
        if(args.length != 2) {
            Logger.error("Invalid arguments. Usage: generator <outputDir> <licenseHeader>");
            System.exit(1);
        }
        final var outputDir = Path.of(args[0]);
        final String licenseHeader;
        try(var reader = Files.newBufferedReader(Path.of(args[1]), StandardCharsets.UTF_8)) {
            licenseHeader = reader.lines().map(line -> (" * " + line).stripTrailing()).collect(Collectors.joining("\n", "\n", "\n "));
        } catch (final IOException ex) {
            Logger.error("Failed to read license header file!", ex);
            System.exit(1);
            return;
        }

        final var context = new Context(outputDir, RegistryAccess.builtin(), licenseHeader);
        Logger.info("Generating data for Minecraft version {}", context.gameVersion());

        // Execute every generator
        boolean failed = false;
        for (final Generator generator : GeneratorMain.generators(context)) {
            try {
                generator.generate(context);
            } catch (final Exception ex) {
                Logger.error(ex, "An unexpected error occurred while generating {} data", generator.name());
                failed = true;
            }
        }
        if (failed) {
            Logger.error("A failure occurred earlier in generating data. See log for details.");
            System.exit(1);
        }

        // Write modified files to disk
        context.complete();
        // Success!
        Logger.info("Successfully generated data!");
    }

    private static List<Generator> generators(final Context context) {
        // Prepare a set of generators
        // We are starting out by just generating Vanilla registry-backed catalogs
        // Enum-backed (automatically-named) catalogs can be added later as necessary
        return List.of(
            new RegistryEntriesGenerator<>(
                "data.type",
                "ArtTypes",
                "ART_TYPE",
                context.relativeClass("data.type", "ArtType"),
                Registry.MOTIVE_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "entity.attribute.type",
                "AttributeTypes",
                "ATTRIBUTE_TYPE",
                context.relativeClass("entity.attribute.type", "RangedAttributeType"),
                Registry.ATTRIBUTE_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "world.biome",
                "Biomes",
                "BIOME",
                context.relativeClass("world.biome", "Biome"),
                Registry.BIOME_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "block",
                "BlockTypes",
                "BLOCK_TYPE",
                context.relativeClass("block", "BlockType"),
                Registry.BLOCK_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "block.entity",
                "BlockEntityTypes",
                "BLOCK_ENTITY_TYPE",
                context.relativeClass("block.entity", "BlockEntityType"),
                Registry.BLOCK_ENTITY_TYPE_REGISTRY
            ),
            /*new RegistryEntriesValidator<>( // TODO: Chunk statuses are not yet updated
                "world.chunk",
                "ChunkStates",
                Registry.CHUNK_STATUS_REGISTRY
            ),*/
            /*new RegistryEntriesValidator<>( // todo: has special ordering
                "item.inventory",
                "ContainerTypes",
                Registry.MENU_REGISTRY
            ),*/
            new RegistryEntriesValidator<>(
                "item.enchantment",
                "EnchantmentTypes",
                Registry.ENCHANTMENT_REGISTRY
            ),
            new RegistryEntriesValidator<>(
                "entity",
                "EntityTypes",
                Registry.ENTITY_TYPE_REGISTRY,
                $ -> true,
                Set.of(new ResourceLocation("sponge", "human")) // Sponge's Human type is an extra addition
            ),
            new RegistryEntriesGenerator<>(
                "fluid",
                "FluidTypes",
                "FLUID_TYPE",
                context.relativeClass("fluid", "FluidType"),
                Registry.FLUID_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "item",
                "ItemTypes",
                "ITEM_TYPE",
                context.relativeClass("item", "ItemType"),
                Registry.ITEM_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "effect.particle",
                "ParticleTypes",
                "PARTICLE_TYPE",
                context.relativeClass("effect.particle", "ParticleType"),
                Registry.PARTICLE_TYPE_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "item.potion",
                "PotionTypes",
                "POTION_TYPE",
                context.relativeClass("item.potion", "PotionType"),
                Registry.POTION_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "effect.potion",
                "PotionEffectTypes",
                "POTION_EFFECT_TYPE",
                context.relativeClass("effect.potion", "PotionEffectType"),
                Registry.MOB_EFFECT_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "ProfessionTypes",
                "PROFESSION_TYPE",
                context.relativeClass("data.type", "ProfessionType"),
                Registry.VILLAGER_PROFESSION_REGISTRY
            ),
            new RegistryEntriesValidator<>(
                "item.recipe",
                "RecipeTypes",
                Registry.RECIPE_TYPE_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "effect.sound",
                "SoundTypes",
                "SOUND_TYPE",
                context.relativeClass("effect.sound", "SoundType"),
                Registry.SOUND_EVENT_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "statistic",
                "Statistics",
                "STATISTIC",
                context.relativeClass("statistic", "Statistic"),
                Registry.CUSTOM_STAT_REGISTRY
            ),
            /*new RegistryEntriesValidator<>( // TODO: Needs to be updated
                "statistic",
                "StatisticCategories",
                Registry.STAT_TYPE_REGISTRY
            ), */
            new RegistryEntriesGenerator<>(
                "world.generation.structure",
                "Structures",
                "STRUCTURE",
                context.relativeClass("world.generation.structure", "Structure"),
                Registry.STRUCTURE_FEATURE_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "VillagerTypes",
                "VILLAGER_TYPE",
                context.relativeClass("data.type", "VillagerType"),
                Registry.VILLAGER_TYPE_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "world",
                "WorldTypes",
                "WORLD_TYPE",
                context.relativeClass("world", "WorldType"),
                Registry.DIMENSION_TYPE_REGISTRY,
                $ -> true,
                RegistryScope.SERVER
            ),
            new BlockStatePropertiesGenerator()
        );
    }
}
