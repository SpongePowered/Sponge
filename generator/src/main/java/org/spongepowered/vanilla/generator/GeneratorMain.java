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
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.vanilla.generator.item.ItemRegistries;
import org.spongepowered.vanilla.generator.world.TagRegistries;
import org.spongepowered.vanilla.generator.world.WorldRegistries;
import org.spongepowered.vanilla.generator.world.entities.EntityRegistries;
import org.spongepowered.vanilla.generator.world.level.LevelDataRegistries;
import org.spongepowered.vanilla.generator.world.level.block.BlockRegistries;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        try (final var reader = Files.newBufferedReader(Path.of(args[1]), StandardCharsets.UTF_8)) {
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
        final var packRepository = ServerPacksSource.createVanillaTrustedRepository();
        MinecraftServer.configurePackRepository(packRepository, WorldDataConfiguration.DEFAULT, /* safeMode = */ false, true);
        final CloseableResourceManager rm = new MultiPackResourceManager(PackType.SERVER_DATA, packRepository.openAllSelected());

        // WorldLoader.load
        final LayeredRegistryAccess<RegistryLayer> staticRegistries = RegistryLayer.createRegistryAccess();
        List<Registry.PendingTags<?>> pendingTags = TagLoader.loadTagsForExistingRegistries(rm, staticRegistries.getLayer(RegistryLayer.STATIC));
        final var wga = staticRegistries.getAccessForLoading(RegistryLayer.WORLDGEN);
        List<HolderLookup.RegistryLookup<?>> tl = TagLoader.buildUpdatedLookups(wga, pendingTags);
        RegistryAccess.Frozen wgr = RegistryDataLoader.load(rm, tl, RegistryDataLoader.WORLDGEN_REGISTRIES);
        List<HolderLookup.RegistryLookup<?>> cl = Stream.concat(tl.stream(), wgr.listRegistries()).toList();
        final LayeredRegistryAccess<RegistryLayer> withWorldGen = staticRegistries.replaceFrom(RegistryLayer.WORLDGEN, wgr);
        RegistryAccess.Frozen da = RegistryDataLoader.load(rm, cl, RegistryDataLoader.DIMENSION_REGISTRIES);
        final LayeredRegistryAccess<RegistryLayer> withDimensions = withWorldGen.replaceFrom(RegistryLayer.DIMENSIONS, da);
        TagLoader.loadTagsForExistingRegistries(rm, withDimensions.getLayer(RegistryLayer.WORLDGEN));

        final RegistryAccess.Frozen compositeRegistries = withDimensions.getAccessForLoading(RegistryLayer.RELOADABLE);
        final var resourcesFuture = ReloadableServerResources.loadResources(
            rm,
            withDimensions,
            pendingTags,
            packRepository.getRequestedFeatureFlags(),
            CommandSelection.ALL,
            2, // functionPermissionLevel
            Util.backgroundExecutor(), // prepareExecutor
            Runnable::run // applyExecutor
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                rm.close();
            }
        }).thenApply(resources -> {
            resources.updateStaticRegistryTags();
            return resources;
        });

        Logger.info("Datapack load initiated");

        final ReloadableServerResources resources;
        try {
            resources = resourcesFuture.get();
        } catch (final InterruptedException | ExecutionException ex) {
            Logger.error(ex, "Failed to load registries/datapacks");
            System.exit(1);
            throw new RuntimeException();
        }

        Logger.info("Datapack load complete");

        return Pair.of(
            compositeRegistries,
            resources
        );
    }

    private static List<Generator> generators(final Context context) {
        // Prepare a set of generators
        // We are startinsg out by just generating Vanilla registry-backed catalogs
        // Enum-backed (automatically-named) catalogs can be added later as necessary
        return Stream.of(
                WorldRegistries.worldRegistries(context),
                LevelDataRegistries.levelDataRegistries(context),
                BlockRegistries.registries(context),
                BlockRegistries.enumRegistries(context),
                EntityRegistries.registryEntries(context),
                EntityRegistries.enumEntries(context),
                ItemRegistries.itemRegistries(context),
                TagRegistries.tagRegistries(context)
            )
            .flatMap(List::stream)
            .toList();
    }
}
