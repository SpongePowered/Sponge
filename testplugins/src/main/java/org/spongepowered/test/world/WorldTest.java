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
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.DataPackManager;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
        final Parameter.Value<WorldType> worldTypeParameter = Parameter.registryElement(TypeToken.get(WorldType.class), RegistryTypes.WORLD_TYPE, "minecraft", "sponge").key("world_type").build();
        final Parameter.Value<ResourceKey> copyWorldKeyParameter = Parameter.resourceKey().key("copy_world").build();
        final Parameter.Value<ResourceKey> moveWorldKeyParameter = Parameter.resourceKey().key("move_world").build();

        event.register(this.plugin, Command.builder().addParameters(CommonParameters.LOCATION_ONLINE_ONLY)
                     .permission(this.plugin.metadata().id() + ".command.portal.create")
                     .executor(this::createPortal).build(), "cp", "createportal")
             .register(this.plugin, Command.builder().addParameters(optPlayerParameter, CommonParameters.LOCATION_ONLINE_ONLY)
                     .permission(this.plugin.metadata().id() + ".command.portal.use")
                     .executor(context -> this.useportal(context, optPlayerParameter)).build(), "up", "useportal")
             .register(this.plugin, Command.builder().addParameters(optPlayerParameter, worldTypeParameter)
                     .permission(this.plugin.metadata().id() + ".command.environment.change")
                     .executor(context -> this.changeEnvironement(context, optPlayerParameter, worldTypeParameter)).build(), "ce", "changeenvironment")
             .register(this.plugin, Command.builder().addParameters(CommonParameters.WORLD, worldTypeParameter)
                     .permission(this.plugin.metadata().id() + ".command.worldtype.change")
                     .executor(context -> this.changeworldType(context, worldTypeParameter)).build(), "cwt", "changeworldtype")
             .register(this.plugin, Command.builder().addParameters(optPlayerParameter, optWorldParameter, optPositionParameter)
                     .permission(this.plugin.metadata().id() + ".command.location.change")
                     .executor(context -> this.changelocation(context, optPlayerParameter, optWorldParameter, optPositionParameter)).build(), "cl", "changelocation")
             .register(this.plugin, Command.builder().addParameter(worldKeyParameter)
                     .permission(this.plugin.metadata().id() + ".command.world.load")
                     .executor(context -> this.loadWorld(context, worldKeyParameter)).build(), "lw", "loadworld")
             .register(this.plugin, Command.builder().addParameters(worldKeyParameter, worldTypeParameter)
                     .permission(this.plugin.metadata().id() + ".command.world.create")
                     .executor(context -> this.createWorld(context, worldKeyParameter, worldTypeParameter)).build(), "cw", "createworld")
             .register(this.plugin, Command.builder().addParameter(CommonParameters.WORLD)
                     .executor(this::unloadWorld).build(), "uw", "unloadworld")
             .register(this.plugin, Command.builder().addParameters(worldKeyParameter, copyWorldKeyParameter)
                     .executor(context -> this.copyWorld(context, worldKeyParameter, copyWorldKeyParameter)).build(), "cpw", "copyworld")
             .register(this.plugin, Command.builder().addParameters(worldKeyParameter, moveWorldKeyParameter)
                     .executor(context -> this.moveWorld(context, worldKeyParameter, moveWorldKeyParameter)).build(), "mw", "moveworld")
             .register(this.plugin, Command.builder().addParameters(worldKeyParameter)
                     .executor(context -> this.deleteWorld(context, worldKeyParameter)).build(), "dw", "deleteworld")
             .register(this.plugin, Command.builder().addParameter(optPlayerParameter)
                     .executor(context -> this.whereami(context, optPlayerParameter)) .build(), "wai", "whereami")
             .register(this.plugin, Command.builder()
                     .executor(this::worldTypes).build(), "worldtypes")
            .register(this.plugin, Command.builder()
                    .executor(this::worldTemplates).build(), "worldtemplates")
        ;
    }

    private CommandResult createPortal(final CommandContext context) {
        final ServerLocation location = context.requireOne(CommonParameters.LOCATION_ONLINE_ONLY);
        PortalLogic.factory().netherPortal().generator().get().generatePortal(location, Axis.X);
        return CommandResult.success();
    }
    private CommandResult useportal(final CommandContext context, final Parameter.Value<ServerPlayer> optPlayerParameter) {
        final ServerPlayer player = context.one(optPlayerParameter).orElse(this.getSourcePlayer(context));
        final ServerLocation location = context.requireOne(CommonParameters.LOCATION_ONLINE_ONLY);
        final PortalLogic portalType = PortalLogic.factory().netherPortal();
        return portalType.teleport(player, location, true) ? CommandResult.success() : CommandResult.error(Component.text("Could not teleport!"));
    }

    private CommandResult changeEnvironement(final CommandContext context, final Parameter.Value<ServerPlayer> optPlayerParameter, final Parameter.Value<WorldType> worldTypeParameter) {
        final ServerPlayer player = context.one(optPlayerParameter).orElse(this.getSourcePlayer(context));
        final WorldType worldType = context.requireOne(worldTypeParameter);
        player.sendWorldType(worldType);
        return CommandResult.success();
    }
    private CommandResult changeworldType(final CommandContext context, final Parameter.Value<WorldType> worldTypeParameter) {
        final ServerWorld world = context.requireOne(CommonParameters.WORLD);
        final WorldType worldType = context.requireOne(worldTypeParameter);
        world.properties().setWorldType(worldType);
        return CommandResult.success();
    }

    private CommandResult changelocation(final CommandContext context, final Parameter.Value<ServerPlayer> optPlayerParameter, final Parameter.Value<ServerWorld> optWorldParameter, final Parameter.Value<Vector3d> optPositionParameter) {
        final ServerPlayer player = context.one(optPlayerParameter).orElse(this.getSourcePlayer(context));
        final ServerWorld world = context.one(optWorldParameter).orElse(player.world());
        final Vector3d position = context.one(optPositionParameter).orElse(world.properties().spawnPosition().toDouble());
        return player.setLocation(ServerLocation.of(world, position)) ? CommandResult.success() : CommandResult.error(Component.text("Could not teleport!"));
    }

    private CommandResult loadWorld(final CommandContext context, final Parameter.Value<ResourceKey> worldKeyParameter){
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
    }

    private CommandResult createWorld(final CommandContext context,final Parameter.Value<ResourceKey> worldKeyParameter, final Parameter.Value<WorldType> worldTypeParameter) {
        final ResourceKey key = context.requireOne(worldKeyParameter);
        final WorldType worldType = context.requireOne(worldTypeParameter);
        final WorldTemplate template = WorldTemplate.builder()
                .from(WorldTemplate.overworld())
                .key(key)
                .add(Keys.WORLD_TYPE, worldType)
                .add(Keys.PERFORM_SPAWN_LOGIC, true)
                .build();

        this.game.server().worldManager().loadWorld(template).whenComplete((r, t) -> {
            if (t != null) {
                context.cause().audience().sendMessage(Identity.nil(), Component.text(t.getMessage()));
            } else if (r != null) {
                context.cause().audience().sendMessage(Identity.nil(), Component.text("World created successfully!"));
            } else {
                context.cause().audience().sendMessage(Identity.nil(), Component.text("World failed to create!"));
            }
        });

        return CommandResult.success();
    }
    private CommandResult unloadWorld(final CommandContext context) {
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
    }

    private CommandResult copyWorld(final CommandContext context, final Parameter.Value<ResourceKey> worldKeyParameter, final Parameter.Value<ResourceKey> copyWorldKeyParameter){
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
    }

    private CommandResult moveWorld(final CommandContext context, final Parameter.Value<ResourceKey> worldKeyParameter, final Parameter.Value<ResourceKey> moveWorldKeyParameter) {
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
    }

    private CommandResult deleteWorld(final CommandContext context, final Parameter.Value<ResourceKey> worldKeyParameter) {
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
    }

    private CommandResult whereami(final CommandContext context, final Parameter.Value<ServerPlayer> optPlayerParameter) {
        final ServerPlayer player = context.one(optPlayerParameter).orElse(this.getSourcePlayer(context));
        player.sendMessage(Identity.nil(),
                Component.text("You are in World ").append(player.world().properties().displayName().orElseGet(() -> Component.text(player.world().key().toString(),
                                NamedTextColor.AQUA)))
                        .append(Component.text(" at (" + player.position().floorX() + ", " + player.position().floorY() +
                                ", " + player.position().floorZ() + ")")));
        return CommandResult.success();
    }



    private CommandResult worldTypes(final CommandContext commandContext) {
        final Optional<ServerPlayer> optPlayer = commandContext.cause().first(ServerPlayer.class);
        for (final WorldType wt : WorldTypes.registry().stream().toList()) {
            final WorldTypeTemplate template = WorldTypeTemplate.builder().fromValue(wt).key(ResourceKey.of(this.plugin, "test")).build();
            final DataContainer dataContainer = template.toContainer();
            optPlayer.ifPresent(player -> player.sendMessage(Component.text(template.key().toString())));
            System.out.println(template.key());
            try {
                System.out.println(DataFormats.JSON.get().write(dataContainer));
                final WorldTypeTemplate rebuiltTemplate = WorldTypeTemplate.builder().fromDataPack(dataContainer)
                        .key(ResourceKey.of(this.plugin, "custom" + template.key().value())).build();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return CommandResult.success();
    }

    private CommandResult worldTemplates(final CommandContext commandContext) {
        final DataPackManager dm = Sponge.server().dataPackManager();
        final List<ResourceKey> templates = dm.list(DataPacks.WORLD);
        for (final ResourceKey key : templates) {

            try {
                final Optional<WorldTemplate> template = dm.load(DataPacks.WORLD, key).join();
                if (template.isPresent()) {
                    System.out.println(key + DataFormats.JSON.get().write(template.get().toContainer()));
                } else {
                    System.out.println(key + " (no template)");
                }
            } catch (final Exception e) {
                System.err.println(key + " " + e.getMessage());
            }

        }
        return CommandResult.success();
    }


    public static void transportToWorld(final ServerPlayer player, final ServerWorld world) {
        player.sendMessage(Identity.nil(), Component.text("Teleporting..."));
        final ServerLocation spawn = world.location(world.properties().spawnPosition());
        final Optional<ServerLocation> safeLoc = Sponge.server().teleportHelper().findSafeLocation(spawn);
        player.setLocation(safeLoc.orElse(spawn));
        player.showTitle(Title.title(Component.text("Welcome to your world"), Component.text(player.name() + " spawn" + spawn.blockPosition())));
    }

    private ServerPlayer getSourcePlayer(final CommandContext context) {
        if (context.cause().root() instanceof ServerPlayer) {
            return (ServerPlayer) context.cause().root();
        }
        throw new NoSuchElementException("Source is not a player");
    }

}
