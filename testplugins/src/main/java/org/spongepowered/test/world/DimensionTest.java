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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.WorldTypeEffects;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeProvider;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.Structure;
import org.spongepowered.api.world.generation.Structures;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.SamplingConfig;
import org.spongepowered.api.world.generation.config.noise.SlideConfig;
import org.spongepowered.api.world.generation.config.structure.SeparatedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.SpacedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.StructureGenerationConfig;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Plugin("dimensiontest")
public final class DimensionTest {

    private final PluginContainer plugin;

    @Inject
    public DimensionTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Command.builder().setExecutor(this::createRandomWorld).build(), "createrandomworld");
    }

    private CommandResult createRandomWorld(CommandContext context) {
        final WorldManager wm = Sponge.getServer().getWorldManager();
        final ServerPlayer player = (ServerPlayer) context.getCause().root();
        final String owner = player.getName();
        final Scheduler scheduler = Sponge.getServer().getScheduler();
        final Random random = player.getWorld().getRandom();

        final List<RegistryReference<Biome>> allBiomes = Sponge.getServer().registries().registry(RegistryTypes.BIOME)
                .streamEntries().map(RegistryEntry::key)
                .map(location -> RegistryKey.of(RegistryTypes.BIOME, location).asReference())
                .collect(Collectors.toList());
        final List<RegistryReference<Biome>> biomes = IntStream.range(0, random.nextInt(8) + 2)
                .mapToObj(i -> allBiomes.get(random.nextInt(allBiomes.size())))
                .collect(Collectors.toList());

        final List<Structure> allStructures = Sponge.getGame().registries().registry(RegistryTypes.STRUCTURE).stream().collect(Collectors.toList());

        final Map<Structure, SeparatedStructureConfig> abundantStructures = IntStream.range(0, random.nextInt(2) + 1)
                .mapToObj(i -> allStructures.get(random.nextInt(allStructures.size())))
                .distinct()
                .collect(Collectors.toMap(s -> s, s -> SeparatedStructureConfig.of(random.nextInt(3) + 2, 1, random.nextInt(10))));
        final Map<Structure, SeparatedStructureConfig> rareStructures = IntStream.range(0, random.nextInt(8) + 2)
                .mapToObj(i -> allStructures.get(random.nextInt(allStructures.size())))
                .distinct()
                .collect(Collectors.toMap(s -> s, s -> SeparatedStructureConfig.of(random.nextInt(10) + 6, 5, random.nextInt(10))));

        final StructureGenerationConfig structureConfig = StructureGenerationConfig.builder()
                .addStructures(abundantStructures)
                .addStructures(rareStructures)
                .build();

        final NoiseConfig noiseConfig = NoiseConfig.builder().height(256).build();

        final NoiseGeneratorConfig noiseGenConfig = NoiseGeneratorConfig.builder()
                .structureConfig(structureConfig)
                .noiseConfig(noiseConfig)
                .seaLevel(random.nextInt(60) + 30)
                .build();

        final ResourceKey worldKey = ResourceKey.of(this.plugin, owner.toLowerCase());
        final WorldTemplate customTemplate = WorldTemplate.builder().from(WorldTemplate.overworld())
                .key(worldKey)
                .worldType(WorldTypes.OVERWORLD)
                .generateSpawnOnLoad(false) // TODO generating spawn on load stalls the server forever after almost fully generating :(
                .displayName(Component.text("Custom world by " + owner))
                .generator(ChunkGenerator.noise(BiomeProvider.checkboard(biomes, random.nextInt(3)), noiseGenConfig))
                .difficulty(Difficulties.HARD)
                .build();

        if (player.getWorld().getKey().equals(worldKey)) {
            final ServerWorld defaultWorld = wm.getWorld(ResourceKey.minecraft("overworld")).get();
            player.setLocation(defaultWorld.getLocation(defaultWorld.getProperties().spawnPosition()));
        }
        context.sendMessage(Identity.nil(), Component.text("Generating your world..."));
        CompletableFuture<Boolean> worldDeletedFuture = CompletableFuture.completedFuture(true);
        if (wm.getWorld(worldKey).isPresent()) {
            worldDeletedFuture = wm.unloadWorld(worldKey).thenCompose(b -> wm.deleteWorld(worldKey));
        }
        worldDeletedFuture.thenCompose(b -> {
            wm.saveTemplate(customTemplate);
            return wm.loadWorld(customTemplate);
        }).thenAccept(w -> {
            scheduler.submit(Task.builder().plugin(plugin).execute(() -> this.transportToWorld(player, w)).build());
        }).exceptionally(e -> {
            context.sendMessage(Identity.nil(), Component.text("OH NO! " + e.getMessage(), NamedTextColor.DARK_RED));
            e.printStackTrace();
            return null;
        });


        return CommandResult.success();
    }

    private void transportToWorld(ServerPlayer player, ServerWorld world) {
        final Vector3i spawnChunk = Sponge.getServer().getChunkLayout().forceToChunk(world.getProperties().spawnPosition());
        world.loadChunk(spawnChunk, true);
        player.sendMessage(Identity.nil(), Component.text("Teleporting..."));
        final ServerLocation spawn = world.getLocation(world.getProperties().spawnPosition());
        player.setLocation(Sponge.getServer().getTeleportHelper().getSafeLocation(spawn).orElse(spawn));
        player.showTitle(Title.title(Component.text("Welcome to your world"), Component.text(player.getName())));
    }

    @Listener
    public void onRegisterWorldTypeTemplates(final RegisterDataPackValueEvent<WorldTypeTemplate> event) {
        event
            .register(WorldTypeTemplate
                .builder()
                    .from(WorldTypeTemplate.theNether())
                    .key(ResourceKey.of(this.plugin, "test_one"))
                    .effect(WorldTypeEffects.END)
                    .createDragonFight(true)
                    .build()
            )
        ;
    }

    @Listener
    public void onRegisterWorldTemplates(final RegisterDataPackValueEvent<WorldTemplate> event) {
        event
                .register(WorldTemplate
                        .builder()
                        .from(WorldTemplate.overworld())
                        .key(ResourceKey.of(this.plugin, "world_1"))
                        .worldType(WorldTypes.THE_NETHER)
                        .displayName(Component.text("Mean World", NamedTextColor.RED))
                        .generator(ChunkGenerator.noise(
                                BiomeProvider.checkboard(Lists.newArrayList(Biomes.DARK_FOREST, Biomes.CRIMSON_FOREST, Biomes.BIRCH_FOREST), 2),
                                NoiseGeneratorConfig.builder()
                                        .structureConfig(StructureGenerationConfig.builder()
                                                .stronghold(SpacedStructureConfig.of(10, 10, 1))
                                                .addStructure(Structures.IGLOO.get(), SeparatedStructureConfig.of(10, 5, 10))
                                                .build()
                                        )
                                        .noiseConfig(NoiseConfig.builder()
                                                .sampling(SamplingConfig.of(1, 120, 1, 180))
                                                .top(SlideConfig.of(-5, 5, 0))
                                                .bottom(SlideConfig.of(-15, 0, 0))
                                                .build()
                                        )
                                        .seaLevel(64)
                                        .build()
                                )
                        )
                        .difficulty(Difficulties.HARD)
                        .build()
                )
                .register(WorldTemplate
                        .builder()
                        .from(WorldTemplate.overworld())
                        .key(ResourceKey.of(this.plugin, "world_2"))
                        .build()
                )
        ;
    }
}
