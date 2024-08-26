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
package org.spongepowered.test.worldgen;

import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityCategories;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeTemplate;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.biome.ambient.ParticleConfig;
import org.spongepowered.api.world.biome.ambient.SoundConfig;
import org.spongepowered.api.world.biome.spawner.NaturalSpawner;
import org.spongepowered.api.world.generation.feature.DecorationSteps;
import org.spongepowered.api.world.generation.feature.FeatureType;
import org.spongepowered.api.world.generation.feature.PlacedFeatures;
import org.spongepowered.api.world.server.DataPackManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BiomeTest {

    public static final String CUSTOM_PLAINS = "custom_plains";
    public static final String CUSTOM_FOREST = "custom_forest";
    public static final String NAMESPACE = "biometest";

    private CommandResult info(final CommandContext ctx, final Parameter.Value<Biome> biomeParam) {
        final Biome biome = ctx.requireOne(biomeParam);
        ctx.sendMessage(Identity.nil(), Component.text("Biome Info:", NamedTextColor.GOLD));
        biome.features().forEach((step, list) -> {
            if (!list.isEmpty()) {
                ctx.sendMessage(Identity.nil(), Component.text("Feature Step:", NamedTextColor.DARK_AQUA));
                list.forEach(placedFeature -> {
                    final var configurableFeature = placedFeature.feature();
                    final FeatureType feature = configurableFeature.type();
                    ctx.sendMessage(Identity.nil(), Component.text(" - " + feature.getClass().getSimpleName() +
                            " @ " + placedFeature.placementModifiers().stream().map(mod -> mod.getClass().getSimpleName()).toList(), NamedTextColor.GRAY));
                });
            }
        });
        if (!biome.carvers().isEmpty()) {
            ctx.sendMessage(Identity.nil(), Component.text("Carvers Step:", NamedTextColor.DARK_AQUA));
            biome.carvers().forEach(configuredCarver -> {
                ctx.sendMessage(Identity.nil(), Component.text(" - " + configuredCarver.type().getClass().getSimpleName(), NamedTextColor.GRAY));
            });
        }

        biome.ambientMood().ifPresent(mood -> {
            ctx.sendMessage(Identity.nil(), Component.text("Mood: " + mood, NamedTextColor.DARK_AQUA));
        });
        biome.additionalAmbientSound().ifPresent(additional -> {
            ctx.sendMessage(Identity.nil(), Component.text("AdditionalSound: " + additional, NamedTextColor.DARK_AQUA));
        });
        biome.backgroundMusic().ifPresent(bgm -> {
            ctx.sendMessage(Identity.nil(), Component.text("BGM: " + bgm, NamedTextColor.DARK_AQUA));
        });
        biome.ambientParticle().ifPresent(particle -> {
            ctx.sendMessage(Identity.nil(), Component.text("Particle: " + particle, NamedTextColor.DARK_AQUA));
        });

        return CommandResult.success();
    }

    private CommandResult register(CommandContext commandContext) {
        final Biome defaultBiome = Biomes.PLAINS.get(Sponge.server());
        final List<NaturalSpawner> naturalSpawners = defaultBiome.spawners().get(EntityCategories.MONSTER.get());
        Collections.shuffle(naturalSpawners);
        final List<NaturalSpawner> spawner = Arrays.asList(naturalSpawners.get(0));

        final BiomeTemplate template = BiomeTemplate.builder().fromValue(defaultBiome)
                .add(Keys.FEATURES, Map.of(DecorationSteps.LAKES.get(), List.of(PlacedFeatures.LAKE_LAVA_SURFACE.get())))
                .add(Keys.CARVERS, List.of())
                .add(Keys.NATURAL_SPAWNERS, Map.of(EntityCategories.MONSTER.get(), spawner))
                .add(Keys.AMBIENT_ADDITIONAL_SOUND, SoundConfig.factory().ofAdditional(SoundTypes.ENTITY_CREEPER_PRIMED.get(), 0.001))
                .add(Keys.AMBIENT_PARTICLE, ParticleConfig.of(ParticleTypes.BUBBLE.get(), 0.01f))
                .key(ResourceKey.of(NAMESPACE, CUSTOM_PLAINS)).build();
        Sponge.server().dataPackManager().save(template);

        final Biome defaultBiome2 = Biomes.FLOWER_FOREST.get(Sponge.server());
        final BiomeTemplate template2 = BiomeTemplate.builder().fromValue(defaultBiome2)
                .add(Keys.NATURAL_SPAWNERS, Map.of(EntityCategories.MONSTER.get(), spawner))
                .key(ResourceKey.of(NAMESPACE, CUSTOM_FOREST)).build();
        Sponge.server().dataPackManager().save(template2);

        return CommandResult.success();
    }

    private CommandResult list(CommandContext ctx, final Parameter.Value<String> filterParam) {
        final Optional<String> rawFilter = ctx.one(filterParam);
        boolean invert = rawFilter.isPresent();
        final String filter = rawFilter.orElse("minecraft:").toUpperCase();
        final Registry<Biome> registry = this.registry();
        registry.streamEntries().filter(e -> invert == e.key().toString().toUpperCase().contains(filter))
                .forEach(e -> ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY)));

        final DataPackManager dpm = Sponge.server().dataPackManager();
        dpm.find(DataPackTypes.BIOME).forEach((pack, keys) -> {
            ctx.sendMessage(Identity.nil(), Component.text(pack.name() + ": " + pack.description() , NamedTextColor.DARK_AQUA));
            keys.forEach(key -> ctx.sendMessage(Identity.nil(), Component.text(" - " + key, NamedTextColor.GRAY)));
        });

        dpm.load(DataPacks.BIOME, ResourceKey.of(NAMESPACE, CUSTOM_PLAINS)).join().ifPresent(template -> {
            ctx.sendMessage(Identity.nil(), Component.text("BiomeTemplate loaded from disk is present " + template.key(), NamedTextColor.DARK_AQUA));
        });
        return CommandResult.success();
    }

    private Registry<Biome> registry() {
        return Sponge.server().registry(RegistryTypes.BIOME);
    }

    public Command.Parameterized biomeCmd() {

        final var biomeParam = Parameter.registryElement(TypeToken.get(Biome.class), RegistryTypes.BIOME, "minecraft").key("biome").build();
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        return Command.builder()
                .addChild(Command.builder().addParameter(filter).executor(ctx -> this.list(ctx, filter)).build(), "list")
                .addChild(Command.builder().addParameter(biomeParam).executor(ctx -> this.info(ctx, biomeParam)).build(), "info")
                .addChild(Command.builder().executor(this::register).build(), "register")
                .build();
    }
}
