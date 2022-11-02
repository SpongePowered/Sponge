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
import com.mojang.datafixers.util.Pair;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.FrameType;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.DataPackConfig;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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
        if (args.length != 2) {
            Logger.error("Invalid arguments. Usage: generator <outputDir> <licenseHeader>");
            System.exit(1);
            return;
        }

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Bootstrap.validate();

        // Create a generator context based on arguments
        final var outputDir = Path.of(args[0]);
        final String licenseHeader;
        try(final var reader = Files.newBufferedReader(Path.of(args[1]), StandardCharsets.UTF_8)) {
            licenseHeader = reader.lines().map(line -> (" * " + line).stripTrailing()).collect(Collectors.joining("\n", "\n", "\n "));
        } catch (final IOException ex) {
            Logger.error("Failed to read license header file!", ex);
            System.exit(1);
            return;
        }

        final var dataPacks = GeneratorMain.loadVanillaDatapack();
        final var context = new Context(outputDir, dataPacks.getFirst(), dataPacks.getSecond(), licenseHeader);
        Logger.info("Generating data for Minecraft version {}", context.gameVersion());

        // Execute every generator
        boolean failed = false;
        for (final Generator generator : GeneratorMain.generators(context)) {
            try {
                Logger.info("Generating {}", generator.name());
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

    private static Pair<RegistryAccess.Frozen, ReloadableServerResources> loadVanillaDatapack() {
        // Load resource packs, see WorldStem.load
        // and call to WorldStem.load in net.minecraft.server.Main
        // We don't currently try to load any datapacks here
        final var packRepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource());
        MinecraftServer.configurePackRepository(packRepository, DataPackConfig.DEFAULT, /* safeMode = */ false);
        final CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, packRepository.openAllSelected());

        final RegistryAccess.Writable registriesBuilder = RegistryAccess.builtinCopy();
        // Load the registry contents -- we don't need the result here, since we don't have any level data to read
        RegistryOps.createAndLoad(NbtOps.INSTANCE, registriesBuilder, resourceManager);
        final RegistryAccess.Frozen registries = registriesBuilder.freeze();

        final var resourcesFuture = ReloadableServerResources.loadResources(
            resourceManager,
            registries,
            CommandSelection.ALL,
            2, // functionPermissionLevel
            Util.backgroundExecutor(), // prepareExecutor
            Runnable::run // applyExecutor
        );

        Logger.info("Datapack load initiated");

        final ReloadableServerResources resources;
        try {
            resources = resourcesFuture.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.error(ex, "Failed to load registries/datapacks");
            System.exit(1);
            throw new RuntimeException();
        }

        Logger.info("Datapack load complete");
        resources.updateRegistryTags(registries);

        return Pair.of(
            registries,
            resources
        );
    }

    private static List<Generator> generators(final Context context) {
        // Prepare a set of generators
        // We are starting out by just generating Vanilla registry-backed catalogs
        // Enum-backed (automatically-named) catalogs can be added later as necessary
        return List.of(
            new MapEntriesValidator<>(
                "advancement.criteria.trigger",
                "Triggers",
                CriteriaTriggers.class,
                "CRITERIA"
            ),
            new EnumEntriesValidator<>(
                 "item",
                 "FireworkShapes",
                  FireworkRocketItem.Shape.class,
                 "getName",
                 "sponge"
            ),
            new EnumEntriesValidator<>(
                 "entity",
                 "EntityCategories",
                 MobCategory.class,
                 "getName",
                 "sponge"
            ),
            new EnumEntriesValidator<>(
                 "item",
                 "ItemRarities",
                 Rarity.class,
                 "name",
                 "sponge"
            ),
            new EnumEntriesValidator<>(
                 "advancement",
                 "AdvancementTypes",
                 FrameType.class,
                 "getName",
                 "sponge"
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "ArtTypes",
                "ART_TYPE",
                context.relativeClass("data.type", "ArtType"),
                Registry.PAINTING_VARIANT_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "entity.attribute.type",
                "AttributeTypes",
                "ATTRIBUTE_TYPE",
                context.relativeClass("entity.attribute.type", "RangedAttributeType"),
                Registry.ATTRIBUTE_REGISTRY,
                a -> true, null,
                context.relativeClass("entity.attribute.type", "AttributeType")
            ),
            new RegistryEntriesGenerator<>(
                "world.biome",
                "Biomes",
                "BIOME",
                context.relativeClass("world.biome", "Biome"),
                Registry.BIOME_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.carver",
                "CarverTypes",
                "CARVER_TYPE",
                context.relativeClass("world.generation.carver", "CarverType"),
                Registry.CARVER_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.carver",
                "Carvers",
                "CARVER",
                context.relativeClass("world.generation.carver", "Carver"),
                Registry.CONFIGURED_CARVER_REGISTRY,
                a -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.config.noise",
                "NoiseGeneratorConfigs",
                "NOISE_GENERATOR_CONFIG",
                context.relativeClass("world.generation.config.noise", "NoiseGeneratorConfig"),
                Registry.NOISE_GENERATOR_SETTINGS_REGISTRY,
                a -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.config.flat",
                "FlatGeneratorConfigs",
                "FLAT_GENERATOR_CONFIG",
                context.relativeClass("world.generation.config.flat", "FlatGeneratorConfig"),
                Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY,
                a -> true, RegistryScope.GAME
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.config.noise",
                "Noises",
                "NOISE",
                context.relativeClass("world.generation.config.noise", "Noise"),
                Registry.NOISE_REGISTRY,
                a -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.config.noise",
                "DensityFunctions",
                "DENSITY_FUNCTION",
                context.relativeClass("world.generation.config.noise", "DensityFunction"),
                Registry.DENSITY_FUNCTION_REGISTRY,
                a -> true, RegistryScope.SERVER
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
            new RegistryEntriesValidator<>(
                "world.chunk",
                "ChunkStates",
                Registry.CHUNK_STATUS_REGISTRY
            ),
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
                Registry.STRUCTURE_REGISTRY,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure",
                "StructureTypes",
                "STRUCTURE_TYPE",
                context.relativeClass("world.generation.structure", "StructureType"),
                Registry.STRUCTURE_TYPE_REGISTRY
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure",
                "StructureSets",
                "STRUCTURE_SET",
                context.relativeClass("world.generation.structure", "StructureSet"),
                Registry.STRUCTURE_SET_REGISTRY,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure.jigsaw",
                "JigsawPools",
                "JIGSAW_POOL",
                context.relativeClass("world.generation.structure.jigsaw", "JigsawPool"),
                Registry.TEMPLATE_POOL_REGISTRY,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure.jigsaw",
                "ProcessorLists",
                "PROCESSOR_LIST",
                context.relativeClass("world.generation.structure.jigsaw", "ProcessorList"),
                Registry.PROCESSOR_LIST_REGISTRY,
                $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "world.generation.structure.jigsaw",
                "ProcessorTypes",
                "PROCESSOR_TYPE",
                context.relativeClass("world.generation.structure.jigsaw", "ProcessorType"),
                Registry.STRUCTURE_PROCESSOR_REGISTRY,
                $ -> true, RegistryScope.GAME
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
            new BlockStatePropertiesGenerator(),
            new RegistryEntriesGenerator<>(
                    "world.generation.feature",
                    "PlacedFeatures",
                    "PLACED_FEATURE",
                    context.relativeClass("world.generation.feature", "PlacedFeature"),
                    Registry.PLACED_FEATURE_REGISTRY,
                    $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                    "world.generation.feature",
                    "Features",
                    "FEATURE",
                    context.relativeClass("world.generation.feature", "Feature"),
                    Registry.CONFIGURED_FEATURE_REGISTRY,
                    $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                    "world.generation.feature",
                    "FeatureTypes",
                    "FEATURE_TYPE",
                    context.relativeClass("world.generation.feature", "FeatureType"),
                    Registry.FEATURE_REGISTRY,
                    $ -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                    "world.generation.feature",
                    "PlacementModifierTypes",
                    "PLACEMENT_MODIFIER",
                    context.relativeClass("world.generation.feature", "PlacementModifierType"),
                    Registry.PLACEMENT_MODIFIER_REGISTRY,
                    $ -> true, RegistryScope.SERVER
            ),
            new TagGenerator(
                    "BLOCK_TYPE",
                    Registry.BLOCK_REGISTRY,
                    context.relativeClass("block", "BlockType"),
                    "tag",
                    "BlockTypeTags"
            ),
            new TagGenerator(
                "BIOME",
                Registry.BIOME_REGISTRY,
                context.relativeClass("world.biome", "Biome"),
                "tag",
                "BiomeTags"
            ),
            new TagGenerator(
                    "ITEM_TYPE",
                    Registry.ITEM_REGISTRY,
                    context.relativeClass("item", "ItemType"),
                    "tag",
                    "ItemTypeTags"
            ),
            new TagGenerator(
                    "ENTITY_TYPE",
                    Registry.ENTITY_TYPE_REGISTRY,
                    ParameterizedTypeName.get(context.relativeClass("entity", "EntityType"), WildcardTypeName.subtypeOf(Object.class)),
                    "tag",
                    "EntityTypeTags"
            ),
            new TagGenerator(
                    "FLUID_TYPE",
                    Registry.FLUID_REGISTRY,
                    context.relativeClass("fluid", "FluidType"),
                    "tag",
                    "FluidTypeTags"
            )
        );
    }
}
