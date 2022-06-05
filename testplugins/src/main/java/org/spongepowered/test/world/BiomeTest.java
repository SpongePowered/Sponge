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
package org.spongepowered.test.world;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.EntityCategories;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.biome.spawner.NaturalSpawner;
import org.spongepowered.api.world.generation.biome.BiomeTemplate;
import org.spongepowered.api.world.generation.biome.DecorationSteps;
import org.spongepowered.api.world.generation.feature.Feature;
import org.spongepowered.api.world.generation.feature.FeatureConfig;
import org.spongepowered.api.world.generation.feature.PlacedFeatures;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Plugin("biometest")
public final class BiomeTest {

    public static final String CUSTOM_PLAINS = "custom_plains";
    private final PluginContainer plugin;

    @Inject
    public BiomeTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    private void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Command.builder()
                        .addChild(Command.builder().executor(this::listBiomes).build(), "list")
                        .addChild(Command.builder().executor(this::listFeatures).build(), "features")
                        .build(), "biometest")
        ;
    }

    @SuppressWarnings("unchecked")
    @Listener
    private void onRegisterDataPack(final RegisterDataPackValueEvent<BiomeTemplate> event) {
        final Biome defaultBiome = Biomes.PLAINS.get(Sponge.server()); // TODO server is not available yet - how to get the registry? - provide in event?
        final List<NaturalSpawner> naturalSpawners = defaultBiome.spawners().get(EntityCategories.MONSTER.get()).get(new Random());
        final WeightedTable<NaturalSpawner> spawner = new WeightedTable<>();
        naturalSpawners.forEach(s -> spawner.add(s, 1));

        final BiomeTemplate template = BiomeTemplate.builder().from(defaultBiome)
                .add(Keys.FEATURES, Map.of(DecorationSteps.LAKES.get(), List.of(PlacedFeatures.LAKE_LAVA_SURFACE.get())))
                .add(Keys.CARVERS, Map.of())
                .add(Keys.NATURAL_SPAWNERS, Map.of(EntityCategories.MONSTER.get(), spawner))
                .key(ResourceKey.of(this.plugin, CUSTOM_PLAINS)).build();
        event.register(template);
        try {
            System.out.println(DataFormats.JSON.get().write(template.toContainer()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private CommandResult listBiomes(CommandContext commandContext) {
        final Registry<Biome> registry = Biomes.registry(Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get());
        registry.streamEntries().filter(e -> !e.key().namespace().equals("minecraft")).forEach(biome ->
            commandContext.sendMessage(Identity.nil(), Component.text(biome.key().toString()))
        );
        if (registry.findValue(ResourceKey.of(this.plugin, CUSTOM_PLAINS)).isPresent()) {
            commandContext.sendMessage(Identity.nil(), Component.text("custom_plains found"));
        }
        return CommandResult.success();
    }

    private CommandResult listFeatures(CommandContext commandContext) {
        final Biome plainsBiome = Sponge.server().registry(RegistryTypes.BIOME).findValue(ResourceKey.of(this.plugin, CUSTOM_PLAINS)).orElse(Biomes.PLAINS.get(Sponge.server()));
        System.out.println("Plains Biome Features:");
        plainsBiome.features().forEach((step, list) -> {
            System.out.println("Step: " + step);
            list.forEach(placedFeature -> {
                final var configurableFeature = placedFeature.feature();
                final Feature<FeatureConfig> feature = configurableFeature.feature();
                System.out.println(" - " + feature.getClass().getSimpleName() + " /w " + configurableFeature.config().getClass().getSimpleName() + " @ " + placedFeature.placementModifiers().stream().map(mod -> mod.getClass().getSimpleName()).toList());
            });
        });
        System.out.println("Plains Biome Carvers:");
        plainsBiome.carvers().forEach((step, list) -> {
            System.out.println("Step: " + step);
            list.forEach(configuredCarver -> {
                System.out.println(" - " + configuredCarver.carver().getClass().getSimpleName() + " /w " + configuredCarver.config().getClass().getSimpleName());
            });
        });
        return CommandResult.success();
    }


}
