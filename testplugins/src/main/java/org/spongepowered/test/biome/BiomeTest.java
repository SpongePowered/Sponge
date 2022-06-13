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
package org.spongepowered.test.biome;

import com.google.inject.Inject;
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
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.entity.EntityCategories;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.api.world.biome.spawner.NaturalSpawner;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.biome.BiomeTemplate;
import org.spongepowered.api.world.generation.carver.Carver;
import org.spongepowered.api.world.generation.carver.CarverTemplate;
import org.spongepowered.api.world.generation.carver.CarverTypes;
import org.spongepowered.api.world.generation.carver.Carvers;
import org.spongepowered.api.world.generation.feature.DecorationSteps;
import org.spongepowered.api.world.generation.feature.FeatureType;
import org.spongepowered.api.world.generation.feature.PlacedFeatures;
import org.spongepowered.api.world.server.DataPackManager;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.world.WorldTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Plugin("biometest")
public final class BiomeTest {

    public static final String CUSTOM_PLAINS = "custom_plains";
    public static final String CUSTOM_FOREST = "custom_forest";
    private final PluginContainer plugin;

    @Inject
    public BiomeTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    private void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final var biomeParam = Parameter.registryElement(TypeToken.get(Biome.class), RegistryTypes.BIOME, "minecraft").key("biome").build();
        final Parameter.Value<String> filter = Parameter.string().key("filter").optional().build();
        event.register(this.plugin, Command.builder()
                        .addChild(Command.builder().addParameter(filter).executor(ctx -> this.list(ctx, filter)).build(), "list")
                        .addChild(Command.builder().addParameter(biomeParam).executor(ctx -> this.info(ctx, biomeParam)).build(), "info")
                        .addChild(Command.builder().executor(this::register).build(), "register")
                        .addChild(Command.builder().executor(this::carvers).build(), "carvers")
                        .addChild(Command.builder().executor(this::world).build(), "world")
                        .build(), "biometest")
        ;
    }

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
        biome.carvers().forEach((step, list) -> {
            if (!list.isEmpty()) {
                ctx.sendMessage(Identity.nil(), Component.text("Carvers Step:", NamedTextColor.DARK_AQUA));
                list.forEach(configuredCarver -> {
                    ctx.sendMessage(Identity.nil(), Component.text(" - " + configuredCarver.type().getClass().getSimpleName(), NamedTextColor.GRAY));
                });
            }
        });
        return CommandResult.success();
    }

    private CommandResult register(CommandContext commandContext) {
        final Biome defaultBiome = Biomes.PLAINS.get(Sponge.server());
        final List<NaturalSpawner> naturalSpawners = defaultBiome.spawners().get(EntityCategories.MONSTER.get()).get(new Random());
        final WeightedTable<NaturalSpawner> spawner = new WeightedTable<>();
        naturalSpawners.forEach(s -> spawner.add(s, 1));

        final BiomeTemplate template = BiomeTemplate.builder().from(defaultBiome)
                .add(Keys.FEATURES, Map.of(DecorationSteps.LAKES.get(), List.of(PlacedFeatures.LAKE_LAVA_SURFACE.get())))
                .add(Keys.CARVERS, Map.of())
                .add(Keys.NATURAL_SPAWNERS, Map.of(EntityCategories.MONSTER.get(), spawner))
                .key(ResourceKey.of(this.plugin, CUSTOM_PLAINS)).build();
        Sponge.server().dataPackManager().save(template);

        naturalSpawners.forEach(s -> spawner.add(s, 1));
        final Biome defaultBiome2 = Biomes.FLOWER_FOREST.get(Sponge.server());
        final BiomeTemplate template2 = BiomeTemplate.builder().from(defaultBiome2)
                .add(Keys.NATURAL_SPAWNERS, Map.of(EntityCategories.MONSTER.get(), spawner))
                .key(ResourceKey.of(this.plugin, CUSTOM_FOREST)).build();
        Sponge.server().dataPackManager().save(template2);

        return CommandResult.success();
    }


    private CommandResult carvers(final CommandContext ctx) {
        ctx.sendMessage(Identity.nil(), Component.text("Carver Types", NamedTextColor.DARK_AQUA));
        CarverTypes.registry().streamEntries().forEach(e -> {
            ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY));
        });
        ctx.sendMessage(Identity.nil(), Component.text("Carver", NamedTextColor.DARK_AQUA));
        Carvers.registry().streamEntries().forEach(e -> {
            ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY));
        });

        final CarverTemplate template = CarverTemplate.builder().from(Carvers.CAVE.get()).key(ResourceKey.of(this.plugin, "custom_carver")).build();
        final DataContainer container = template.toContainer();
        try {
            final CarverTemplate rebuiltTemplate = CarverTemplate.builder().fromDataPack(container).key(ResourceKey.of(this.plugin, "custom_carver")).build();
            Sponge.server().dataPackManager().save(rebuiltTemplate);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        final Carver reconfigured = CarverTypes.CAVE.get().configure(container);

        return CommandResult.success();
    }


    private CommandResult world(CommandContext commandContext) {
        final WorldManager wm = Sponge.server().worldManager();
        final ResourceKey worldKey = ResourceKey.of(this.plugin, CUSTOM_PLAINS);
        final Optional<Biome> customBiome = this.registry().findValue(ResourceKey.of(this.plugin, CUSTOM_PLAINS));
        final Optional<Biome> customBiome2 = this.registry().findValue(ResourceKey.of(this.plugin, CUSTOM_FOREST));
        if (customBiome.isPresent()) {
            final RegistryReference<Biome> ref = RegistryReference.referenced(Sponge.server(), RegistryTypes.BIOME, customBiome.get());
            final RegistryReference<Biome> ref2 = RegistryReference.referenced(Sponge.server(), RegistryTypes.BIOME, customBiome2.get());
            final MultiNoiseBiomeConfig cfg = MultiNoiseBiomeConfig.builder()
                    .addBiome(AttributedBiome.of(ref, BiomeAttributes.of(0.1f, -0.6f, 0.1f, -0.5f, 0f, -1f, 0.0f)))
                    .addBiome(AttributedBiome.of(ref2, BiomeAttributes.of(0.4f, -1f, 0.5f, -0.2f, 0f, -0.95f, 0.0f)))
                    .build();
            final var chunkGen = ChunkGenerator.noise(BiomeProvider.multiNoise(cfg), ChunkGenerator.overworld().config());
            final WorldTemplate template = WorldTemplate.builder().add(Keys.CHUNK_GENERATOR, chunkGen).key(worldKey).build();
            final Optional<ServerPlayer> optPlayer = commandContext.cause().first(ServerPlayer.class);
            wm.loadWorld(template).thenAccept(w -> optPlayer.ifPresent(player -> WorldTest.transportToWorld(player, w)));
        }
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

        dpm.load(DataPacks.BIOME, ResourceKey.of(this.plugin, CUSTOM_PLAINS)).join().ifPresent(template -> {
            ctx.sendMessage(Identity.nil(), Component.text("BiomeTemplate loaded from disk is present " + template.key(), NamedTextColor.DARK_AQUA));
        });
        return CommandResult.success();
    }

    private Registry<Biome> registry() {
        return Sponge.server().registry(RegistryTypes.BIOME);
    }

}
