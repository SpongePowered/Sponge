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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.api.world.server.ServerWorldProperties;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
        final Parameter.Value<ServerWorldProperties> worldParameter = Parameter.worldProperties().setKey("world").build();
        final Parameter.Value<ServerWorldProperties> optWorldParameter = Parameter.worldProperties().optional().setKey("world").build();
        final Parameter.Value<ServerLocation> locationParameter = Parameter.location().setKey("location").build();
        final Parameter.Value<Vector3d> optVector3Parameter = Parameter.vector3d().optional().setKey("position").build();
        final Parameter.Value<PortalType> portalTypeParameter =
                Parameter.registryElement(
                        TypeToken.get(PortalType.class),
                        RegistryTypes.PORTAL_TYPE,
                        "minecraft",
                        "sponge")
                    .setKey("portal_type").build();
        final Parameter.Value<DimensionType> dimensionTypeParameter = Parameter.registryElement(
                TypeToken.get(DimensionType.class),
                RegistryTypes.DIMENSION_TYPE,
                "minecraft",
                "sponge").setKey("dimension_type").build();
        final Parameter.Value<ResourceKey> worldKeyParameter = Parameter.resourceKey().setKey("world").build();
        final Parameter.Value<ResourceKey> copyWorldKeyParameter = Parameter.resourceKey().setKey("copy_world").build();
        final Parameter.Value<ResourceKey> renameWorldKeyParameter = Parameter.resourceKey().setKey("new_world_name").build();
        final Parameter.Value<BiomeType> biomeListTypeParameter =
                Parameter.registryElement(
                        TypeToken.get(BiomeType.class),
                        RegistryTypes.BIOME_TYPE,
                        "minecraft",
                        "sponge"
                ).setKey("biome_types").consumeAllRemaining().optional().build();

        final Parameter.Value<ResourceKey> unloadedWorldKeyParameter = Parameter.resourceKey()
                .setSuggestions((context, currentInput) -> Sponge.getServer().getWorldManager()
                        .getAllProperties()
                        .stream()
                        .filter(x -> !x.getWorld().isPresent())
                        .map(x -> x.getKey().asString())
                        .filter(x -> x.startsWith(currentInput))
                        .collect(Collectors.toList()))
                .setKey("world").build();

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
                    .parameters(playerParameter, dimensionTypeParameter)
                    .setPermission(this.plugin.getMetadata().getId() + ".command.environment.change")
                    .setExecutor(context -> {
                        final ServerPlayer player = context.getOne(playerParameter).orElse(this.getSourcePlayer(context));
                        final DimensionType dimensionType = context.requireOne(dimensionTypeParameter);
                        player.sendEnvironment(dimensionType);
                        return CommandResult.success();
                    })
                    .build()
                , "ce", "changeenvironment"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameters(worldParameter, dimensionTypeParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.dimension.change")
                        .setExecutor(context -> {
                            final ServerWorldProperties world = context.requireOne(worldParameter);
                            final DimensionType dimensionType = context.requireOne(dimensionTypeParameter);
                            world.setDimensionType(dimensionType);
                            return CommandResult.success();
                        })
                        .build()
                , "cd", "changedimension"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameters(playerParameter, optWorldParameter, optVector3Parameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.position.change")
                        .setExecutor(context -> {
                            final ServerPlayer player = context.getOne(playerParameter).orElse(this.getSourcePlayer(context));
                            final ServerWorldProperties properties = context.getOne(optWorldParameter).orElse(player.getWorld().getProperties());
                            final Vector3d position =
                                    context.getOne(optVector3Parameter).orElse(properties.getSpawnPosition().toDouble());
                            return player.setLocation(ServerLocation.of(properties.getWorld().get(), position)) ? CommandResult.success() :
                                    CommandResult.error(Component.text("Could not teleport!"));
                        })
                        .build()
                , "cl", "changelocation"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(unloadedWorldKeyParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.world.load")
                        .setExecutor(context -> {
                            final ResourceKey key = context.requireOne(unloadedWorldKeyParameter);
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

        event.register(this.plugin, Command
                        .builder()
                        .parameters(worldKeyParameter, dimensionTypeParameter, biomeListTypeParameter)
                        .setPermission(this.plugin.getMetadata().getId() + ".command.world.create")
                        .setExecutor(context -> {
                            final ResourceKey key = context.requireOne(worldKeyParameter);
                            final DimensionType dimensionType = context.requireOne(dimensionTypeParameter);
                            final Collection<? extends BiomeType> biomes = context.getAll(biomeListTypeParameter);
//                            GeneratorModifierType modifierType = GeneratorModifierTypes.NONE.get();
//                            if (!biomes.isEmpty()) {
//                                if (biomes.size() == 1) {
//                                    settings.set(DataQuery.of("biome_source", "type"), "minecraft:fixed");
//                                } else {
//                                    settings.set(DataQuery.of("biome_source", "type"), "minecraft:checkerboard");
//                                }
//                                settings.set(DataQuery.of("biome_source", "options", "biomes"), biomes.stream().map(BiomeType::getKey).map(ResourceKey::asString).collect(Collectors.toList()));
//                                modifierType = Registries.GENERATOR_MODIFIER_TYPE
//                                        .asReference()
//                                        .get(Sponge.getGame().registries())
//                                        .findEntry(ResourceKey.resolve("buffet"))
//                                        .get()
//                                        .value();
//                            }
                            final WorldArchetype archetype = WorldArchetype.builder()
                                    .dimensionType(dimensionType)
                                    .generateSpawnOnLoad(true)
                                    .build();
                            Sponge.getServer().getWorldManager().createProperties(key, archetype).whenComplete(((worldProperties, throwable) -> {
                                if (throwable != null) {
                                    context.getCause().getAudience().sendMessage(Identity.nil(), Component.text(throwable.getMessage()));
                                    return;
                                }

                                Sponge.getServer().getWorldManager().loadWorld(worldProperties).whenComplete(((serverWorld, throwable1) -> {
                                    if (throwable1 != null) {
                                        context.getCause().getAudience().sendMessage(Identity.nil(), Component.text(throwable1.getMessage()));
                                    }
                                }));
                            }));

                            return CommandResult.success();
                        })
                        .build()
                , "cw", "createworld"
        );

        event.register(this.plugin, Command
                        .builder()
                        .parameter(worldParameter)
                        .setExecutor(context -> {
                            final ServerWorldProperties properties = context.requireOne(worldParameter);
                            Sponge.getServer().getWorldManager().unloadWorld(properties.getKey()).whenComplete((aBoolean, throwable) -> {
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
                        .parameters(worldKeyParameter, renameWorldKeyParameter)
                        .setExecutor(context -> {
                            final ResourceKey worldKey = context.requireOne(worldKeyParameter);
                            final ResourceKey renameWorld = context.requireOne(renameWorldKeyParameter);

                            Sponge.getServer().getWorldManager().moveWorld(worldKey, renameWorld).whenComplete((aBoolean, throwable) -> {
                                if (throwable != null) {
                                    context.getCause().getAudience().sendMessage(Identity.nil(), Component.text(throwable.getMessage()));
                                }
                            });

                            return CommandResult.success();
                        })
                        .build()
                , "rw", "renameworld"
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
    }

    private ServerPlayer getSourcePlayer(CommandContext context) {
        if (context.getCause().root() instanceof ServerPlayer) {
            return (ServerPlayer) context.getCause().root();
        }
        throw new NoSuchElementException("Source is not a player");
    }

    @Listener
    public void onRespawnPlayer(final RespawnPlayerEvent event) {
        this.plugin.getLogger().error(event);
    }
}
