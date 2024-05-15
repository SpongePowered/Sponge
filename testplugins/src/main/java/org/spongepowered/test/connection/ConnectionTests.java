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
package org.spongepowered.test.connection;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ServerConnectionState;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Plugin("connectiontests")
public class ConnectionTests implements LoadableModule {

    private final PluginContainer plugin;

    private boolean acceptTransfer;

    @Inject
    public ConnectionTests(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, new Listeners());
    }

    @Override
    public void disable(CommandContext ctx) {
        Sponge.eventManager().unregisterListeners(this.plugin);
    }

    @Listener
    private void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Command.builder()
                .executor(context -> {
                    this.acceptTransfer = !this.acceptTransfer;
                    final Component newState = Component.text(
                            this.acceptTransfer ? "ON" : "OFF", this.acceptTransfer ? NamedTextColor.GREEN : NamedTextColor.RED);
                    context.sendMessage(Identity.nil(), Component.text("Accept Transfers: ").append(newState));
                    return CommandResult.success();
                })
                .build(), "toggleAcceptTransfers"
        );
    }

    private final class Listeners {

        private final Map<ServerSideConnection, ConnectionData> connectionData = new ConcurrentHashMap<>();

        @Listener
        @IsCancelled(Tristate.UNDEFINED)
        private void onIntent(final ServerSideConnectionEvent.Intent event) {
            if (ConnectionTests.this.acceptTransfer && event.isTransfer()) {
                event.setCancelled(false);
            }
            this.connectionData.put(event.connection(), new ConnectionData(event.isTransfer()));
            Sponge.game().systemSubject().sendMessage(Component.text("Intent from " + event.connection().address() + " with transfer " + event.isTransfer()));
            this.checkState(event);
        }

        @Listener
        private void onAuth(final ServerSideConnectionEvent.Auth event) {
            Sponge.game().systemSubject().sendMessage(Component.text("User " + event.profile().name() + " authenticated!"));
            final @Nullable ConnectionData connectionData = this.connectionData.get(event.connection());
            if (connectionData != null) {
                connectionData.profile = event.profile();
            }
            this.checkState(event);
        }

        @Listener
        private void onHandshake(final ServerSideConnectionEvent.Handshake event) {
            Sponge.game().systemSubject().sendMessage(Component.text("User " + event.profile().name() + " performing handshake!"));
            this.checkState(event);
        }

        @Listener
        private void onConfiguration(final ServerSideConnectionEvent.Configuration event) {
            Sponge.game().systemSubject().sendMessage(Component.text("User " + event.profile().name() + " in configuration phase!"));
            this.checkState(event);
        }

        @Listener
        private void onLogin(final ServerSideConnectionEvent.Login event) {
            Sponge.game().systemSubject().sendMessage(Component.text("User " + event.profile().name() + " login!"));
            this.checkState(event);
        }

        @Listener
        private void onJoin(final ServerSideConnectionEvent.Join event) {
            Sponge.game().systemSubject().sendMessage(Component.text("User " + event.player().name() + " joined!"));
            this.checkState(event);
        }

        @Listener
        private void onLeave(final ServerSideConnectionEvent.Leave event) {
            Sponge.game().systemSubject().sendMessage(Component.text("User " + event.player().name() + " left!"));
            this.checkState(event);
        }

        @Listener
        private void onDisconnect(final ServerSideConnectionEvent.Disconnect event) {
            Sponge.game().systemSubject().sendMessage(Component.text("Connection from " + event.connection().address() + " disconnected! " + event.profile()));
            this.checkState(event);
            this.connectionData.remove(event.connection());
        }

        private void checkState(final ServerSideConnectionEvent event) {
            final @Nullable ConnectionData connectionData = this.connectionData.get(event.connection());
            if (connectionData == null) {
                return;
            }

            event.connection().state().ifPresent(state -> {
                if (!Objects.equals(connectionData.transferred, state.transferred())) {
                    Sponge.game().systemSubject().sendMessage(Component.text("Transfer data mismatch in " + event, NamedTextColor.RED));
                }

                if (state instanceof ServerConnectionState.Authenticated authenticatedState) {
                    if (!Objects.equals(connectionData.profile, authenticatedState.profile())) {
                        Sponge.game().systemSubject().sendMessage(Component.text("Profile data mismatch in " + event, NamedTextColor.RED));
                    }
                }
            });
        }

        private static final class ConnectionData {
            private final boolean transferred;

            private GameProfile profile;

            ConnectionData(boolean transferred) {
                this.transferred = transferred;
            }
        }
    }
}
