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
package org.spongepowered.common.ipforward.velocity;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.ChannelExceptionHandler;
import org.spongepowered.api.network.channel.NoResponseException;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.handshake.RawHandshakeDataChannel;
import org.spongepowered.common.accessor.network.ConnectionAccessor;
import org.spongepowered.common.accessor.server.network.ServerLoginPacketListenerImplAccessor;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VelocityForwardingInfo {
    private static final int SUPPORTED_FORWARDING_VERSION = 1;
    private static final Logger LOGGER = LogManager.getLogger();

    private static class VelocityChannel {

        private static final ResourceKey PLAYER_INFO_CHANNEL = ResourceKey.of("velocity", "player_info");
        private static final RawHandshakeDataChannel CHANNEL;

        static {
            final RawDataChannel rawData = Sponge.channelManager().ofType(VelocityChannel.PLAYER_INFO_CHANNEL, RawDataChannel.class);
            rawData.setExceptionHandler(ChannelExceptionHandler.logEverything().suppressIfFutureIsPresent(NoResponseException.class));
            CHANNEL = rawData.handshake();
        }

    }

    public static void sendQuery(final ServerLoginPacketListenerImpl mcConn) {
        final EngineConnection conn = (EngineConnection) mcConn;
        VelocityChannel.CHANNEL
            .sendTo(conn, cbuf -> {})
            .whenComplete((response, error) -> {
                if (error != null) {
                    if (error instanceof NoResponseException) {
                        conn.close(Component.text("This server requires you to connect with Velocity."));
                    }
                    return;
                }

                if (!VelocityForwardingInfo.checkIntegrity(response)) {
                    conn.close(Component.text("Unable to verify player details. Is your forwarding secret correct?"));
                    return;
                }

                final ConnectionAccessor connectionAccessor = (ConnectionAccessor) mcConn.getConnection();
                connectionAccessor.accessor$address(new InetSocketAddress(VelocityForwardingInfo.readAddress(response), ((InetSocketAddress) mcConn.getConnection()
                    .getRemoteAddress()).getPort()));

                ((ServerLoginPacketListenerImplAccessor) mcConn).accessor$gameProfile(VelocityForwardingInfo.createProfile(response));
        }).exceptionally(err -> {
            if (!(err instanceof NoResponseException)) { // Handled above
                VelocityForwardingInfo.LOGGER.error("Failed to process velocity forwarding info", err);
                conn.close(Component.text("Invalid forwarding information received!"));
            }
            return null;
        });
    }

    public static boolean checkIntegrity(final ChannelBuf buf) {
        final byte[] signature = buf.readBytes(32);
        final byte[] data = new byte[buf.available()];
        ((ByteBuf) buf).getBytes(buf.readerIndex(), data); // TODO: figure out a ChannelBuf method

        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SpongeConfigs.getCommon().get().ipForwarding.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            final byte[] mySignature = mac.doFinal(data);
            if (!MessageDigest.isEqual(signature, mySignature)) {
                return false;
            }
        } catch (final InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        int version = buf.readVarInt();
        if (version != VelocityForwardingInfo.SUPPORTED_FORWARDING_VERSION) {
            throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted " + VelocityForwardingInfo.SUPPORTED_FORWARDING_VERSION);
        }

        return true;
    }

    public static InetAddress readAddress(final ChannelBuf buf) {
        return InetAddresses.forString(buf.readString());
    }

    public static GameProfile createProfile(final ChannelBuf buf) {
        final GameProfile profile = new GameProfile(buf.readUniqueId(), ((FriendlyByteBuf) buf).readUtf(16)); // TODO: ChannelBuf length-limited strings
        VelocityForwardingInfo.readProperties(buf, profile);
        return profile;
    }

    private static void readProperties(final ChannelBuf buf, final GameProfile profile) {
        final int properties = buf.readVarInt();
        for (int i1 = 0; i1 < properties; i1++) {
            final String name = buf.readString();
            final String value = buf.readString();
            final String signature = buf.readBoolean() ? buf.readString() : null;
            profile.getProperties().put(name, new Property(name, value, signature));
        }
    }
}
