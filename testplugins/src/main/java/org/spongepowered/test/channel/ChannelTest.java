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
package org.spongepowered.test.channel;

import com.google.inject.Inject;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterChannelEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ClientConnectionState;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.api.network.channel.Channel;
import org.spongepowered.api.network.channel.NoResponseException;
import org.spongepowered.api.network.channel.packet.PacketChannel;
import org.spongepowered.api.network.channel.packet.basic.BasicPacketChannel;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.concurrent.CompletionException;

@Plugin("channeltest")
public final class ChannelTest {

    private static final boolean ENABLED = Boolean.getBoolean("sponge.channelTest");

    private final PluginContainer plugin;

    @Inject
    public ChannelTest(final PluginContainer plugin) {
        this.plugin = plugin;
        if (ChannelTest.ENABLED) {
            Sponge.game().eventManager().registerListeners(this.plugin, new Listeners());
        }
    }

    final class Listeners {

        private PacketChannel channel;
        private BasicPacketChannel basicChannel;
        private RawDataChannel rawChannel;

        @Listener
        private void onRegisterChannel(final RegisterChannelEvent event) {
            this.channel = event.register(ResourceKey.of("channeltest", "default"), PacketChannel.class);
            this.channel.register(PrintTextPacket.class, 0)
                    .addHandler((packet, connection) -> {
                        this.logReceived(this.channel, packet, connection);
                        ChannelTest.this.plugin.logger().info(packet.getText());
                    });
            this.channel.registerTransactional(PingPacket.class, PongPacket.class, 1)
                    .setRequestHandler((requestPacket, connection, response) -> {
                        this.logReceived(this.channel, requestPacket, connection);
                        response.success(new PongPacket(requestPacket.getId()));
                    });

            this.basicChannel = event.register(ResourceKey.of("channeltest", "basic"), BasicPacketChannel.class);
            this.basicChannel.register(PrintTextPacket.class, 0)
                    .addHandler((packet, connection) -> {
                        this.logReceived(this.basicChannel, packet, connection);
                        ChannelTest.this.plugin.logger().info(packet.getText());
                    });
            this.basicChannel.registerTransactional(PingPacket.class, PongPacket.class, 1)
                    .setRequestHandler((requestPacket, connection, response) -> {
                        this.logReceived(this.basicChannel, requestPacket, connection);
                        response.success(new PongPacket(requestPacket.getId()));
                    });

            this.rawChannel = event.register(ResourceKey.of("channeltest", "raw"), RawDataChannel.class);
            this.rawChannel.handshake()
                    .setRequestHandler((request, connection, response) -> {
                        final int value = request.readVarInt();
                        this.logReceived(this.rawChannel, value, connection);
                        if (value == 0) {
                            response.fail(new NoResponseException());
                        } else {
                            response.success(buf -> buf.writeVarInt(value * 2));
                        }
                    });
        }

        @Listener
        private void onConnectionHandshake(final ServerSideConnectionEvent.Handshake event) {
            ChannelTest.this.plugin.logger().info("Starting handshake phase.");
            final PingPacket pingPacket1 = new PingPacket(123);
            final ServerSideConnection connection = event.connection();
            this.channel.sendTo(connection, pingPacket1)
                    .thenAccept(response1 -> {
                        this.logReceived(this.channel, response1, connection);
                        final PingPacket pingPacket2 = new PingPacket(456);
                        this.channel.sendTo(connection, pingPacket2)
                                .thenAccept(response2 -> {
                                    this.logReceived(this.channel, response2, connection);
                                    this.channel.sendTo(connection, new PrintTextPacket("Finished handshake phase."));
                                    ChannelTest.this.plugin.logger().info("Finished handshake phase.");
                                })
                                .exceptionally(cause -> {
                                    ChannelTest.this.plugin.logger().error("Failed to get a response to {}", pingPacket2, cause);
                                    return null;
                                });
                    })
                    .exceptionally(cause -> {
                        ChannelTest.this.plugin.logger().error("Failed to get a response to {}", pingPacket1, cause);
                        return null;
                    });

            final PingPacket basicPingPacket1 = new PingPacket(1123);
            this.basicChannel.handshake().sendTo(connection, basicPingPacket1)
                    .thenAccept(response1 -> {
                        this.logReceived(this.basicChannel, response1, connection);
                        final PingPacket basicPingPacket2 = new PingPacket(1456);
                        this.basicChannel.handshake().sendTo(connection, basicPingPacket2)
                                .thenAccept(response2 -> {
                                    this.logReceived(this.channel, response2, connection);
                                    this.basicChannel.handshake().sendTo(connection, new PrintTextPacket("Finished handshake phase for basic channel."));
                                    ChannelTest.this.plugin.logger().info("Finished handshake phase for basic channel.");
                                })
                                .exceptionally(cause -> {
                                    ChannelTest.this.plugin.logger().error("Failed to get a response to {}", basicPingPacket2, cause);
                                    return null;
                                });
                    })
                    .exceptionally(cause -> {
                        ChannelTest.this.plugin.logger().error("Failed to get a response to {}", pingPacket1, cause);
                        return null;
                    });

            this.rawChannel.handshake().sendTo(connection, buf -> buf.writeVarInt(200))
                    .thenAccept(response -> this.logReceived(this.rawChannel, response.readVarInt(), connection))
                    .exceptionally(cause -> {
                        ChannelTest.this.plugin.logger().error("Failed to get a response to raw 200 value", cause);
                        return null;
                    });

            this.rawChannel.handshake().sendTo(connection, buf -> buf.writeVarInt(0))
                    .thenAccept(response -> this.logReceived(this.rawChannel, response.readVarInt(), connection))
                    .exceptionally(cause -> {
                        if (cause instanceof CompletionException) {
                            cause = cause.getCause();
                        }
                        if (cause instanceof NoResponseException) {
                            ChannelTest.this.plugin.logger().error("Successfully received no response exception");
                        } else {
                            ChannelTest.this.plugin.logger().error("Failed to get a response to raw 0 value", cause);
                        }
                        return null;
                    });
        }

        @Listener
        private void onConnectionLogin(final ServerSideConnectionEvent.Login event) {
            ChannelTest.this.plugin.logger().info("Player \"" + event.user().profile().name().orElse("unknown") + "\" is logging in.");
        }

        @Listener
        private void onConnectionJoin(final ServerSideConnectionEvent.Join event) {
            ChannelTest.this.plugin.logger().info("Player \"" + event.player().name() + "\" joined.");

            final ServerSideConnection connection = event.connection();
            final PingPacket pingPacket1 = new PingPacket(789);
            this.channel.sendTo(connection, pingPacket1)
                    .thenAccept(response1 -> this.logReceived(this.channel, response1, connection))
                    .exceptionally(cause -> {
                        ChannelTest.this.plugin.logger().error("Failed to get a response to {}", pingPacket1, cause);
                        return null;
                    });

            this.basicChannel.play().sendTo(connection, new PrintTextPacket("You successfully joined the server."))
                    .exceptionally(cause -> {
                        ChannelTest.this.plugin.logger().error(cause);
                        return null;
                    });
        }

        private static String getName(final EngineConnectionSide side) {
            return side == EngineConnectionSide.CLIENT ? "client" : "server";
        }

        private static String getName(final EngineConnectionState side) {
            return side instanceof ClientConnectionState ? "client" : "server";
        }

        private void logReceived(final Channel channel, final Object packet, final EngineConnection connection) {
            ChannelTest.this.plugin.logger().info("Received {} through {} on the {} side.", packet, channel.key(), Listeners.getName(connection.side()));
        }

        private void logReceived(final Channel channel, final Object packet, final EngineConnectionState connection) {
            ChannelTest.this.plugin.logger().info("Received {} through {} on the {} side.", packet, channel.key(), Listeners.getName(connection));
        }
    }
}
