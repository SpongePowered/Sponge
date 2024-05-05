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
package org.spongepowered.test.movement;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Optional;

@Plugin("movementtest")
public final class MovementTest implements LoadableModule {

    static final Marker marker = MarkerManager.getMarker("MOVEMENT");

    final PluginContainer plugin;
    boolean cancelAll = false;
    boolean waterProofRedstone = false;
    boolean printMoveEntityEvents = false;
    boolean printRotationEvents = false;
    boolean cancelMovement = false;
    boolean cancelRotation = false;
    boolean teleportOnMove = false;
    boolean cancelUnnaturalMovement = false;

    @Inject
    public MovementTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, new MovementTestListener());
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {

        final Command.Parameterized toggleAllMovement = Command.builder().executor(this::toggleMovement).build();
        final Command.Parameterized toggleAllRotation = Command.builder().executor(this::toggleRotation).build();
        final Command.Parameterized toggleUnnaturalMovement = Command.builder().executor(this::toggleUnnatural).build();
        final Command.Parameterized teleportOnMove = Command.builder().executor(this::teleportOnMove).build();
        final Command.Parameterized setPos = Command.builder().addParameter(CommonParameters.POSITION).executor(this::setPos).build();
        final Command.Parameterized setLoc = Command.builder().addParameter(CommonParameters.LOCATION_ONLINE_ONLY).executor(this::setLoc).build();

        event.register(this.plugin, Command.builder()
                .addChild(toggleAllMovement, "toggleAllMovement")
                .addChild(toggleAllRotation, "toggleAllRotation")
                .addChild(toggleUnnaturalMovement, "toggleUnnaturalMovement")
                .addChild(teleportOnMove, "teleportOnMove")
                .addChild(setPos, "setpos")
                .addChild(setLoc, "setloc")
                .build(), "movementtest");
    }

    private CommandResult setPos(CommandContext context) {
        if (context.cause().root() instanceof ServerPlayer) {
            final ServerPlayer serverPlayer = (ServerPlayer) context.cause().root();
            final boolean success = serverPlayer.setPosition(context.requireOne(CommonParameters.POSITION));
            if (success) {
                context.sendMessage(Identity.nil(), Component.text("Successfully changed position"));
                return CommandResult.success();
            }
            return CommandResult.error(Component.text("Could not change position."));
        }
        return CommandResult.error(Component.text("You must be a player to set your position."));
    }

    private CommandResult setLoc(CommandContext context) {
        if (context.cause().root() instanceof ServerPlayer) {
            final ServerPlayer serverPlayer = (ServerPlayer) context.cause().root();
            final boolean success = serverPlayer.setLocation(context.requireOne(CommonParameters.LOCATION_ONLINE_ONLY));
            if (success) {
                context.sendMessage(Identity.nil(), Component.text("Successfully changed location"));
                return CommandResult.success();
            }
            return CommandResult.error(Component.text("Could not change location."));
        }
        return CommandResult.error(Component.text("You must be a player to set your location."));
    }

    private CommandResult toggleMovement(CommandContext context) {
        this.cancelMovement = !this.cancelMovement;
        final Component newState = Component.text(this.cancelMovement ? "ON" : "OFF", this.cancelMovement ? NamedTextColor.GREEN : NamedTextColor.RED);
        context.sendMessage(Identity.nil(), Component.text("Cancel Player All Movement : ").append(newState));
        return CommandResult.success();
    }

    private CommandResult toggleRotation(CommandContext context) {
        this.cancelRotation = !this.cancelRotation;
        final Component newState = Component.text(this.cancelRotation ? "ON" : "OFF", this.cancelRotation ? NamedTextColor.GREEN : NamedTextColor.RED);
        context.sendMessage(Identity.nil(), Component.text("Cancel Player All Rotation : ").append(newState));
        return CommandResult.success();
    }

    private CommandResult toggleUnnatural(CommandContext context) {
        this.cancelUnnaturalMovement = !this.cancelUnnaturalMovement;
        final Component newState = Component.text(this.cancelUnnaturalMovement ? "ON" : "OFF", this.cancelUnnaturalMovement ? NamedTextColor.GREEN : NamedTextColor.RED);
        context.sendMessage(Identity.nil(), Component.text("Cancel Player Unnatural Movement : ").append(newState));
        return CommandResult.success();
    }

    private CommandResult teleportOnMove(CommandContext context) {
        this.teleportOnMove = !this.teleportOnMove;
        final Component newState = Component.text(this.teleportOnMove ? "ON" : "OFF", this.teleportOnMove ? NamedTextColor.GREEN : NamedTextColor.RED);
        context.sendMessage(Identity.nil(), Component.text("Teleport Player on Movement : ").append(newState));
        return CommandResult.success();
    }

    public final class MovementTestListener {
        @Listener
        public void onRotatePlayer(final RotateEntityEvent event, final @Getter("entity") ServerPlayer player) {
            if (MovementTest.this.printRotationEvents) {
                final Logger pluginLogger = MovementTest.this.plugin.logger();
                pluginLogger.log(Level.INFO, MovementTest.marker, "/*************");
                pluginLogger.log(Level.INFO, MovementTest.marker, "/* RotateEntityEvent");
                pluginLogger.log(Level.INFO, MovementTest.marker, "/");
                pluginLogger.log(Level.INFO, MovementTest.marker, "/ Cause:");
                for (final Object o : event.cause()) {
                    pluginLogger.log(Level.INFO, MovementTest.marker, "/ - " + o);
                }
                pluginLogger.log(Level.INFO, MovementTest.marker, "/");
            }
            if (MovementTest.this.cancelRotation) {
                event.setCancelled(true);
                return;
            }
        }

        @Listener
        public void onMovePlayer(final MoveEntityEvent event) {
            final ServerPlayer player;
            final String name;
            if (event.entity() instanceof ServerPlayer) {
                player = (ServerPlayer) event.entity();
                name = player.name();
            } else {
                player = (ServerPlayer) event.entity().get(Keys.PASSENGERS).get().stream()
                        .filter(ServerPlayer.class::isInstance).findFirst().orElse(null);
                if (player == null) {
                    return;
                }
                name = player.name() + " (vehicle)";
            }

            final Optional<MovementType> type = event.context().get(EventContextKeys.MOVEMENT_TYPE);
            final Logger logger = MovementTest.this.plugin.logger();
            logger.info(MovementTest.marker, name + ": Movement Type: " + type.map(t -> RegistryTypes.MOVEMENT_TYPE.get().valueKey(t).toString()).orElse("?"));

            if (MovementTest.this.cancelMovement) {
                event.setCancelled(true);
                return;
            }
            if (MovementTest.this.cancelUnnaturalMovement && type.map(t -> !t.equals(MovementTypes.NATURAL.get())).orElse(false)) {
                event.setCancelled(true);
                return;
            }
            if (MovementTest.this.teleportOnMove) {
                final Vector3d sub = event.originalDestinationPosition().sub(event.originalPosition());
                if (sub.lengthSquared() != 0) {
                    final Vector3d mul = sub.normalize().mul(5);
                    event.setDestinationPosition(event.originalDestinationPosition().add(mul));
                    MovementTest.this.teleportOnMove = false;
                    return;
                }
            }
        }
    }
}
