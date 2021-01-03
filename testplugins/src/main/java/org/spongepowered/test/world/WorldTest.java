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
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.biome.provider.CheckerboardBiomeConfig;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.Structure;
import org.spongepowered.api.world.generation.config.NoiseGeneratorConfig;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.structure.SeparatedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.StructureGenerationConfig;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Plugin("worldtest")
public final class WorldTest {

    private final PluginContainer plugin;

    @Inject
    public WorldTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerPlayer> playerParameter = Parameter.player().optional().setKey("player").build();
        final Parameter.Value<ServerWorld> optWorldParameter = Parameter.world().optional().setKey("world").build();
        final Parameter.Value<ServerLocation> locationParameter = Parameter.location().setKey("location").build();
        final Parameter.Value<Vector3d> optVector3Parameter = Parameter.vector3d().optional().setKey("position").build();
        final Parameter.Value<PortalType> portalTypeParameter =
                Parameter.registryElement(
                        TypeToken.get(PortalType.class),
                        RegistryTypes.PORTAL_TYPE,
                        "minecraft",
                        "sponge")
                    .setKey("portal_type").build();
        final Parameter.Value<WorldType> worldTypeParameter = Parameter.registryElement(
                TypeToken.get(WorldType.class),
                RegistryTypes.WORLD_TYPE,
                "minecraft",
                "sponge").setKey("world_type").build();
        final Parameter.Value<ResourceKey> worldKeyParameter = Parameter.resourceKey().setKey("world").build();
        final Parameter.Value<ResourceKey> copyWorldKeyParameter = Parameter.resourceKey().setKey("copy_world").build();
        final Parameter.Value<ResourceKey> moveWorldKeyParameter = Parameter.resourceKey().setKey("new_world_name").build();

        event.register(this.plugin, Command
                    .builder()
                    .parameters(locationParameter, portalTypeParameter)
                    .setPermission(this.plugin.getMetadata().getId() + ".command.portal.create")
                    .setExecutor(context -> {
                        final ServerLocation location = context.requireOne(locationParameter);
                        final PortalType portalType = context.requireOne(portalTypeParameter);
                        portalType.generatePortal(location, Axis.X);
                        return CommandResult.success();
                    })
                    .build()
                , "cp", "createportal"
        );

        event.register(this.plugin, Command
                    .builder()
                    .parameters(playerParameter, locationParameter, portalTypeParameter)
                    .setPermission(this.plugin.getMetadata().getId() + ".command.portal.use")
                    .setExecutor(context -> {
                        final ServerPlayer player = context.getOne(playerParameter).orElse(this.getSourcePlayer(context));
                        final ServerLocation location = context.requireOne(locationParameter);
                        final PortalType portalType = context.requireOne(portalTypeParameter);
                        return portalType.teleport(player, location, true) ? CommandResult.success() : CommandResult
                                .error(Component.text("Could not teleport!"));
                    })
                    .build()
                , "up", "useportal"
        );

        event.register(this.plugin, Command
                    .builder()
                    .parameters(playerParameter, worldTypeParameter)
                    .setPermission(this.plugin.getMetadata().getId() + ".command.environment.change")
                    .setExecutor(context -> {
                        final ServerPlayer player = context.getOne(playerParameter).orElse(this.getSourcePlayer(context));
                        final WorldType worldType = context.requireOne(worldTypeParameter);
                        player.sendWorldType(worldType);
                        return CommandResult.success();
                    })
                    .build()
                , "ce", "changeenvironment"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameters(CommonParameters.WORLD, worldTypeParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.worldtype.change")
                        .setExecutor(context -> {
                            final ServerWorld world = context.requireOne(CommonParameters.WORLD);
                            final WorldType worldType = context.requireOne(worldTypeParameter);
                            world.getProperties().setWorldType(worldType);
                            return CommandResult.success();
                        })
                        .build()
                , "cwt", "changeworldtype"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameters(playerParameter, optWorldParameter, optVector3Parameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.location.change")
                        .setExecutor(context -> {
                            final ServerPlayer player = context.getOne(playerParameter).orElse(this.getSourcePlayer(context));
                            final ServerWorld world = context.getOne(optWorldParameter).orElse(player.getWorld());
                            final Vector3d position =
                                    context.getOne(optVector3Parameter).orElse(world.getProperties().spawnPosition().toDouble());
                            return player.setLocation(ServerLocation.of(world, position)) ? CommandResult.success() :
                                    CommandResult.error(Component.text("Could not teleport!"));
                        })
                        .build()
                , "cl", "changelocation"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(worldKeyParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.world.load")
                        .setExecutor(context -> {
                            final ResourceKey key = context.requireOne(worldKeyParameter);
                            Sponge.getServer().getWorldManager().loadWorld(key).whenComplete(((serverWorld, throwable) -> {
                                if (throwable != null) {
                                    context.getCause().getAudience().sendMessage(Identity.nil(), Component.text(throwable.getMessage()));
                                }
                            }));
                            return CommandResult.success();
                        })
                        .build()
                , "lw", "loadworld"
        );

        // TODO Minecraft 1.16 - Some biomes for a provider? Maybe other template things?
        event.register(this.plugin, Command
                        .builder()
                        .parameters(worldKeyParameter, worldTypeParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.world.create")
                        .setExecutor(context -> {
                            final ResourceKey key = context.requireOne(worldKeyParameter);
                            final ResourceKey worldType = Sponge.getServer().registries().registry(RegistryTypes.WORLD_TYPE).valueKey(context.requireOne(worldTypeParameter));
                            final WorldTemplate template = WorldTemplate.builder()
                                    .key(key)
                                    .worldType(RegistryKey.of(RegistryTypes.WORLD_TYPE, worldType).asReference())
                                    .generateSpawnOnLoad(true)
                                    .build();
                            Sponge.getServer().getWorldManager().loadWorld(template);
                            return CommandResult.success();
                        })
                        .build()
                , "cw", "createworld"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(CommonParameters.WORLD)
                        .setExecutor(context -> {
                            final ServerWorld world = context.requireOne(CommonParameters.WORLD);
                            Sponge.getServer().getWorldManager().unloadWorld(world.getKey()).whenComplete((aBoolean, throwable) -> {
                                if (throwable != null) {
                                    context.getCause().getAudience().sendMessage(Identity.nil(), Component.text(throwable.getMessage()));
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "uw", "unloadworld"
        );

        event.register(this.plugin, Command
                    .builder()
                    .parameters(worldKeyParameter, copyWorldKeyParameter)
                    .setExecutor(context -> {
                        final ResourceKey worldKey = context.requireOne(worldKeyParameter);
                        final ResourceKey copyWorldKey = context.requireOne(copyWorldKeyParameter);

                        Sponge.getServer().getWorldManager().copyWorld(worldKey, copyWorldKey).whenComplete((aBoolean, throwable) -> {
                            if (throwable != null) {
                                context.getCause().getAudience().sendMessage(Identity.nil(), Component.text(throwable.getMessage()));
                            }
                        });

                        return CommandResult.success();
                    })
                    .build()
                , "cpw", "copyworld"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameters(worldKeyParameter, moveWorldKeyParameter)
                        .setExecutor(context -> {
                            final ResourceKey worldKey = context.requireOne(worldKeyParameter);
                            final ResourceKey moveKey = context.requireOne(moveWorldKeyParameter);

                            Sponge.getServer().getWorldManager().moveWorld(worldKey, moveKey).whenComplete((aBoolean, throwable) -> {
                                if (throwable != null) {
                                    context.getCause().getAudience().sendMessage(Identity.nil(), Component.text(throwable.getMessage()));
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "mw", "moveworld"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameters(worldKeyParameter)
                        .setExecutor(context -> {
                            final ResourceKey worldKey = context.requireOne(worldKeyParameter);

                            Sponge.getServer().getWorldManager().deleteWorld(worldKey).whenComplete((aBoolean, throwable) -> {
                                if (throwable != null) {
                                    context.getCause().getAudience().sendMessage(Identity.nil(), Component.text(throwable.getMessage()));
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "dw", "deleteworld"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(playerParameter)
                        .setExecutor(context -> {
                            final ServerPlayer player = context.getOne(playerParameter).orElse(this.getSourcePlayer(context));
                            player.sendMessage(Identity.nil(), Component.text("You are in World ").append(Component.text(player.getWorld().getKey().toString(),
                             NamedTextColor.AQUA)).append(Component.text(" at (" + player.getPosition().getFloorX() + ", " + player.getPosition().getFloorY() +
                                    ", " + player.getPosition().getFloorZ() + ")")));
                            return CommandResult.success();
                        })
                        .build()
                , "wai", "whereami"
        );
        event.register(this.plugin, Command.builder().setExecutor(this::createRandomWorld).build(), "createrandomworld", "crw");
    }

    private CommandResult createRandomWorld(final CommandContext context) {
        final WorldManager wm = Sponge.getServer().getWorldManager();
        final ServerPlayer player = (ServerPlayer) context.getCause().root();
        final String owner = player.getName();
        final Scheduler scheduler = Sponge.getServer().getScheduler();
        final Random random = player.getWorld().getRandom();

        final List<RegistryReference<Biome>> allBiomes = Sponge.getServer().registries().registry(RegistryTypes.BIOME)
                .streamEntries().map(RegistryEntry::key)
                .map(location -> RegistryKey.of(RegistryTypes.BIOME, location).asReference())
                .collect(Collectors.toList());
        final List<RegistryReference<Biome>> biomes = IntStream.range(0, random.nextInt(allBiomes.size()))
                .mapToObj(i -> allBiomes.get(random.nextInt(allBiomes.size())))
                .collect(Collectors.toList());

        final List<Structure> allStructures = Sponge.getGame().registries().registry(RegistryTypes.STRUCTURE).stream().collect(Collectors.toList());

        final Map<Structure, SeparatedStructureConfig> abundantStructures = IntStream.range(0, random.nextInt(allStructures.size()))
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
                .seaLevel(random.nextInt(60) + random.nextInt(30)) // 2 rolls
                .build();

        final ResourceKey worldKey = ResourceKey.of(this.plugin, owner.toLowerCase());
        final WorldTemplate customTemplate = WorldTemplate.builder()
                .from(WorldTemplate.overworld())
                .key(worldKey)
                .worldType(WorldTypes.OVERWORLD)
                .generateSpawnOnLoad(false) // TODO generating spawn on load stalls the server forever after almost fully generating :(
                .displayName(Component.text("Custom world by " + owner))
                .generator(ChunkGenerator
                        .noise(BiomeProvider.checkerboard(CheckerboardBiomeConfig.builder().biomes(biomes).scale(random.nextInt(5) + 1).build()), noiseGenConfig))
                .difficulty(Difficulties.HARD)
                .build();

        if (player.getWorld().getKey().equals(worldKey)) {
            player.setLocation(ServerLocation.of(wm.defaultWorld(), wm.defaultWorld().getProperties().spawnPosition()));
        }
        context.sendMessage(Identity.nil(), Component.text("Generating your world..."));
        CompletableFuture<Boolean> worldDeletedFuture = CompletableFuture.completedFuture(true);
        if (wm.world(worldKey).isPresent()) {
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

    private ServerPlayer getSourcePlayer(final CommandContext context) {
        if (context.getCause().root() instanceof ServerPlayer) {
            return (ServerPlayer) context.getCause().root();
        }
        throw new NoSuchElementException("Source is not a player");
    }

}
