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
import org.spongepowered.api.Game;
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
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.biome.AttributedBiome;
import org.spongepowered.api.world.biome.BiomeAttributes;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.biome.provider.BiomeProvider;
import org.spongepowered.api.world.biome.provider.CheckerboardBiomeConfig;
import org.spongepowered.api.world.biome.provider.MultiNoiseBiomeConfig;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.generation.config.noise.Shaper;
import org.spongepowered.api.world.generation.structure.Structure;
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
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Plugin("worldtest")
public final class WorldTest {

    private final PluginContainer plugin;
    private final Game game;

    @Inject
    public WorldTest(final PluginContainer plugin, final Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Listener
    private void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerPlayer> optPlayerParameter = Parameter.player().optional().key("player").build();
        final Parameter.Value<ResourceKey> worldKeyParameter = Parameter.resourceKey().key("world").build();
        final Parameter.Value<ServerWorld> optWorldParameter = Parameter.world().optional().key("world").build();
        final Parameter.Value<Vector3d> optPositionParameter = Parameter.vector3d().optional().key("position").build();
        final Parameter.Value<PortalType> portalTypeParameter = Parameter.registryElement(TypeToken.get(PortalType.class), RegistryTypes.PORTAL_TYPE, "minecraft", "sponge").key("portal_type").build();
        final Parameter.Value<WorldType> worldTypeParameter = Parameter.registryElement(TypeToken.get(WorldType.class), RegistryTypes.WORLD_TYPE, "minecraft", "sponge").key("world_type").build();
        final Parameter.Value<ResourceKey> copyWorldKeyParameter = Parameter.resourceKey().key("copy_world").build();
        final Parameter.Value<ResourceKey> moveWorldKeyParameter = Parameter.resourceKey().key("move_world").build();

        event
                .register(this.plugin, Command
                        .builder()
                        .addParameters(CommonParameters.LOCATION_ONLINE_ONLY, portalTypeParameter)
                        .permission(this.plugin.metadata().id() + ".command.portal.create")
                        .executor(context -> {
                            final ServerLocation location = context.requireOne(CommonParameters.LOCATION_ONLINE_ONLY);
                            final PortalType portalType = context.requireOne(portalTypeParameter);
                            portalType.generatePortal(location, Axis.X);
                            return CommandResult.success();
                        })
                        .build()
                , "cp", "createportal")
                .register(this.plugin, Command
                        .builder()
                        .addParameters(optPlayerParameter, CommonParameters.LOCATION_ONLINE_ONLY, portalTypeParameter)
                        .permission(this.plugin.metadata().id() + ".command.portal.use")
                        .executor(context -> {
                            final ServerPlayer player = context.one(optPlayerParameter).orElse(this.getSourcePlayer(context));
                            final ServerLocation location = context.requireOne(CommonParameters.LOCATION_ONLINE_ONLY);
                            final PortalType portalType = context.requireOne(portalTypeParameter);
                            return portalType.teleport(player, location, true) ? CommandResult.success() : CommandResult.error(Component.text("Could not teleport!"));
                        })
                        .build()
                , "up", "useportal")
                .register(this.plugin, Command
                        .builder()
                        .addParameters(optPlayerParameter, worldTypeParameter)
                        .permission(this.plugin.metadata().id() + ".command.environment.change")
                        .executor(context -> {
                            final ServerPlayer player = context.one(optPlayerParameter).orElse(this.getSourcePlayer(context));
                            final WorldType worldType = context.requireOne(worldTypeParameter);
                            player.sendWorldType(worldType);
                            return CommandResult.success();
                        })
                        .build()
                , "ce", "changeenvironment")
                .register(this.plugin, Command
                        .builder()
                        .addParameters(CommonParameters.WORLD, worldTypeParameter)
                        .permission(this.plugin.metadata().id() + ".command.worldtype.change")
                        .executor(context -> {
                            final ServerWorld world = context.requireOne(CommonParameters.WORLD);
                            final WorldType worldType = context.requireOne(worldTypeParameter);
                            world.properties().setWorldType(worldType);
                            return CommandResult.success();
                        })
                        .build()
                , "cwt", "changeworldtype")
                .register(this.plugin, Command
                        .builder()
                        .addParameters(optPlayerParameter, optWorldParameter, optPositionParameter)
                        .permission(this.plugin.metadata().id() + ".command.location.change")
                        .executor(context -> {
                            final ServerPlayer player = context.one(optPlayerParameter).orElse(this.getSourcePlayer(context));
                            final ServerWorld world = context.one(optWorldParameter).orElse(player.world());
                            final Vector3d position = context.one(optPositionParameter).orElse(world.properties().spawnPosition().toDouble());
                            return player.setLocation(ServerLocation.of(world, position)) ? CommandResult.success() : CommandResult.error(Component.text("Could not teleport!"));
                        })
                        .build()
                , "cl", "changelocation")
                .register(this.plugin, Command
                        .builder()
                        .addParameter(worldKeyParameter)
                        .permission(this.plugin.metadata().id() + ".command.world.load")
                        .executor(context -> {
                            final ResourceKey key = context.requireOne(worldKeyParameter);

                            this.game.server().worldManager().loadWorld(key).whenComplete((r, t) -> {
                                if (t != null) {
                                    context.cause().audience().sendMessage(Identity.nil(), Component.text(t.getMessage()));
                                } else {
                                    if (r != null) {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World loaded successfully!"));
                                    } else {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World failed to load!"));
                                    }
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "lw", "loadworld")
                .register(this.plugin, Command
                        .builder()
                        .addParameters(worldKeyParameter, worldTypeParameter)
                        .permission(this.plugin.metadata().id() + ".command.world.create")
                        .executor(context -> {
                            final ResourceKey key = context.requireOne(worldKeyParameter);
                            final ResourceKey worldType = RegistryTypes.WORLD_TYPE.get().valueKey(context.requireOne(worldTypeParameter));
                            final WorldTemplate template = WorldTemplate.builder()
                                    .from(WorldTemplate.overworld())
                                    .key(key)
                                    .worldType(RegistryKey.of(RegistryTypes.WORLD_TYPE, worldType).asReference())
                                    .performsSpawnLogic(true)
                                    .build();

                            this.game.server().worldManager().loadWorld(template).whenComplete((r, t) -> {
                                if (t != null) {
                                    context.cause().audience().sendMessage(Identity.nil(), Component.text(t.getMessage()));
                                } else {
                                    if (r != null) {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World created successfully!"));
                                    } else {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World failed to create!"));
                                    }
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "cw", "createworld")
                .register(this.plugin, Command
                        .builder()
                        .addParameter(CommonParameters.WORLD)
                        .executor(context -> {
                            final ServerWorld world = context.requireOne(CommonParameters.WORLD);

                            this.game.server().worldManager().unloadWorld(world).whenComplete((r, t) -> {
                                if (t != null) {
                                    context.cause().audience().sendMessage(Identity.nil(), Component.text(t.getMessage()));
                                } else {
                                    if (r) {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World unloaded successfully!"));
                                    } else {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World failed to unload!"));
                                    }
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "uw", "unloadworld")
                .register(this.plugin, Command
                        .builder()
                        .addParameters(worldKeyParameter, copyWorldKeyParameter)
                        .executor(context -> {
                            final ResourceKey key = context.requireOne(worldKeyParameter);
                            final ResourceKey copyWorldKey = context.requireOne(copyWorldKeyParameter);

                            this.game.server().worldManager().copyWorld(key, copyWorldKey).whenComplete((r, t) -> {
                                if (t != null) {
                                    context.cause().audience().sendMessage(Identity.nil(), Component.text(t.getMessage()));
                                } else {
                                    if (r) {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World copied successfully!"));
                                    } else {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World failed to copy!"));
                                    }
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "cpw", "copyworld")
                .register(this.plugin, Command
                        .builder()
                        .addParameters(worldKeyParameter, moveWorldKeyParameter)
                        .executor(context -> {
                            final ResourceKey key = context.requireOne(worldKeyParameter);
                            final ResourceKey moveWorldKey = context.requireOne(moveWorldKeyParameter);

                            this.game.server().worldManager().moveWorld(key, moveWorldKey).whenComplete((r, t) -> {
                                if (t != null) {
                                    context.cause().audience().sendMessage(Identity.nil(), Component.text(t.getMessage()));
                                } else {
                                    if (r) {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World moved successfully!"));
                                    } else {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World failed to move!"));
                                    }
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "mw", "moveworld")
                .register(this.plugin, Command
                        .builder()
                        .addParameters(worldKeyParameter)
                        .executor(context -> {
                            final ResourceKey key = context.requireOne(worldKeyParameter);

                            this.game.server().worldManager().deleteWorld(key).whenComplete((r, t) -> {
                                if (t != null) {
                                    context.cause().audience().sendMessage(Identity.nil(), Component.text(t.getMessage()));
                                } else {
                                    if (r) {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World deleted successfully!"));
                                    } else {
                                        context.cause().audience().sendMessage(Identity.nil(), Component.text("World failed to delete!"));
                                    }
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "dw", "deleteworld")
                .register(this.plugin, Command
                        .builder()
                        .addParameter(optPlayerParameter)
                        .executor(context -> {
                            final ServerPlayer player = context.one(optPlayerParameter).orElse(this.getSourcePlayer(context));

                            player.sendMessage(Identity.nil(),
                                    Component.text("You are in World ").append(player.world().properties().displayName().orElseGet(() -> Component.text(player.world().key().toString(),
                                            NamedTextColor.AQUA)))
                                            .append(Component.text(" at (" + player.position().floorX() + ", " + player.position().floorY() +
                                                    ", " + player.position().floorZ() + ")")));
                            return CommandResult.success();
                        })
                        .build()
                , "wai", "whereami")
                .register(this.plugin, Command
                        .builder()
                        .executor(this::createWorld)
                        .build()
                , "createrandomworld", "crw"
        );
    }

    private CommandResult createWorld(final CommandContext context) {
        final WorldManager wm = Sponge.server().worldManager();
        final ServerPlayer player = (ServerPlayer) context.cause().root();
        final String owner = player.name();
        final Random random = player.world().random();

        final List<RegistryReference<Biome>> allBiomes = Sponge.server().registry(RegistryTypes.BIOME)
                .streamEntries()
                .map(RegistryEntry::asReference)
                .collect(Collectors.toList());
        final List<RegistryReference<Biome>> biomes = IntStream.range(0, random.nextInt(allBiomes.size()))
                .mapToObj(i -> allBiomes.get(random.nextInt(allBiomes.size())))
                .collect(Collectors.toList());
        if (biomes.isEmpty()) {
            biomes.add(Biomes.PLAINS);
        }

        final List<Structure> allStructures = RegistryTypes.STRUCTURE.get().stream().collect(Collectors.toList());

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

        Shaper[] shapers = {Shaper.overworld(), Shaper.amplified(), Shaper.caves(), Shaper.floatingIslands(), Shaper.nether(), Shaper.end()};
        final NoiseConfig noiseConfig = NoiseConfig.builder().minY(random.nextInt(128/16)*16-64).height(256)
                .terrainShaper(shapers[random.nextInt(shapers.length)])
                .build();

        final NoiseGeneratorConfig noiseGenConfig = NoiseGeneratorConfig.builder()
                .structureConfig(structureConfig)
                .noiseConfig(noiseConfig)
                .seaLevel(random.nextInt(61 - 1) + 1 + random.nextInt(30)) // 2 rolls
                .build();

        final ResourceKey worldKey = ResourceKey.of(this.plugin, owner.toLowerCase());
        final List<AttributedBiome> attributedBiomes = biomes.stream().map(biomeRef -> {
                    Biome biome = biomeRef.get(Sponge.server());
                    BiomeAttributes attr = BiomeAttributes.of((float) biome.temperature(),
                            (float) biome.humidity(),
                            random.nextFloat() * 4 - 2,
                            random.nextFloat() * 4 - 2,
                            random.nextFloat() * 4 - 2,
                            random.nextFloat() / 5,
                            0f);
                    return AttributedBiome.of(biomeRef, attr);
                }
                ).collect(Collectors.toList());
        final MultiNoiseBiomeConfig biomeCfg = MultiNoiseBiomeConfig.builder().addBiomes(attributedBiomes).build();
        final WorldTemplate customTemplate = WorldTemplate.builder()
                .from(WorldTemplate.overworld())
                .key(worldKey)
                .worldType(WorldTypes.OVERWORLD)
                .serializationBehavior(SerializationBehavior.NONE)
                .loadOnStartup(false)
                .performsSpawnLogic(true)
                .displayName(Component.text("Custom world by " + owner))
                .generator(ChunkGenerator.noise(BiomeProvider.multiNoise(biomeCfg), noiseGenConfig))
                .build();

        if (player.world().key().equals(worldKey)) {
            player.setLocation(ServerLocation.of(wm.defaultWorld(), wm.defaultWorld().properties().spawnPosition()));
        }
        context.sendMessage(Identity.nil(), Component.text("Generating your world..."));
        wm.deleteWorld(worldKey).thenCompose(b -> wm.loadWorld(customTemplate)).thenAccept(w -> this.transportToWorld(player, w)).exceptionally(e -> {
                    context.sendMessage(Identity.nil(), Component.text("Failed to teleport!", NamedTextColor.DARK_RED));
                    e.printStackTrace();
                    return null;
                }
        );

        return CommandResult.success();
    }

    private void transportToWorld(final ServerPlayer player, final ServerWorld world) {
        player.sendMessage(Identity.nil(), Component.text("Teleporting..."));
        final ServerLocation spawn = world.location(world.properties().spawnPosition());
        player.setLocation(Sponge.server().teleportHelper().findSafeLocation(spawn).orElse(spawn));
        player.showTitle(Title.title(Component.text("Welcome to your world"), Component.text(player.name())));
    }

    private ServerPlayer getSourcePlayer(final CommandContext context) {
        if (context.cause().root() instanceof ServerPlayer) {
            return (ServerPlayer) context.cause().root();
        }
        throw new NoSuchElementException("Source is not a player");
    }

}
