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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("movementtest")
public final class MovementTest implements LoadableModule {

    static final Marker marker = MarkerManager.getMarker("MOVEMENT");

    final PluginContainer plugin;
    boolean cancelAll = false;
    boolean waterProofRedstone = false;
    boolean printMoveEntityEvents = false;
    boolean printRotationEvents = false;
    boolean cancelMovement = false;
    boolean teleportOnMove = false;

    @Inject
    public MovementTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, new MoveEntityListener());
        Sponge.eventManager().registerListeners(this.plugin, new RotationEventListener());
        Sponge.eventManager().registerListeners(this.plugin, new MoveEntityTeleportListener());
        Sponge.eventManager().registerListeners(this.plugin, new MoveEntityCancellation());
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {


        event.register(this.plugin, Command.builder()
            .executor(context -> {
                this.cancelMovement = !this.cancelMovement;
                final Component newState = Component.text(this.cancelMovement ? "OFF" : "ON", this.cancelMovement ? NamedTextColor.RED : NamedTextColor.GREEN);
                context.sendMessage(Identity.nil(), Component.text("Cancel Entity Movement : ").append(newState));
                return CommandResult.success();
            })
            .build(), "toggleMovement"
        );
        event.register(this.plugin, Command.builder()
            .executor(context -> {
                this.teleportOnMove = !this.teleportOnMove;
                final Component newState = Component.text(this.teleportOnMove ? "ON" : "OFF", this.teleportOnMove ? NamedTextColor.GREEN : NamedTextColor.RED);
                context.sendMessage(Identity.nil(), Component.text("Teleport Entity on Movement : ").append(newState));
                return CommandResult.success();
            })
            .build(), "toggleTeleportOnMove"
        );
    }

    public class RotationEventListener {
        @Listener
        public void onEntitySpawn(final RotateEntityEvent event) {
            if (!MovementTest.this.printRotationEvents) {
                return;
            }
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
    }

    public class MoveEntityTeleportListener {
        @Listener
        public void onEntitySpawn(final MoveEntityEvent event, final @Root Player player) {
            if (!MovementTest.this.teleportOnMove) {
                return;
            }
            final Logger pluginLogger = MovementTest.this.plugin.logger();
            pluginLogger.log(Level.INFO, MovementTest.marker, "/*************");
            pluginLogger.log(Level.INFO, MovementTest.marker, "/* MoveEntityEvent testing with teleport");
            pluginLogger.log(Level.INFO, MovementTest.marker, "/");
            pluginLogger.log(Level.INFO, MovementTest.marker, "/ Cause:");
            for (final Object o : event.cause()) {
                pluginLogger.log(Level.INFO, MovementTest.marker, "/ - " + o);
            }
            pluginLogger.log(Level.INFO, MovementTest.marker, "/");
            final Vector3d sub = event.originalDestinationPosition().sub(event.originalPosition());
            final Vector3d mul = sub.mul(5);
            event.setDestinationPosition(event.originalDestinationPosition().add(mul));
            MovementTest.this.teleportOnMove = false;
        }
    }

    public class MoveEntityCancellation {
        @Listener
        public void onEntitySpawn(final MoveEntityEvent event, @Root Player player) {
            if (!MovementTest.this.cancelMovement) {
                return;
            }
            event.setCancelled(true);
        }
    }

    public class MoveEntityListener {
        @Listener
        public void onChangeBlock(final MoveEntityEvent post) {
            if (!MovementTest.this.printMoveEntityEvents) {
                return;
            }
            final Logger pluginLogger = MovementTest.this.plugin.logger();
            pluginLogger.log(Level.INFO, MovementTest.marker, "/*************");
            pluginLogger.log(Level.INFO, MovementTest.marker, "/* MoveEntityEvent");
            pluginLogger.log(Level.INFO, MovementTest.marker, "/");
            pluginLogger.log(Level.INFO, MovementTest.marker, "/ Cause:");
            for (final Object o : post.cause()) {
                pluginLogger.log(Level.INFO, MovementTest.marker, "/ - " + o);
            }
            pluginLogger.log(Level.INFO, MovementTest.marker, "/");
            if (MovementTest.this.cancelAll && post.cause().containsType(BlockSnapshot.class)) {
                post.setCancelled(true);
            }
            if (MovementTest.this.waterProofRedstone) {

            }
        }
    }
}
