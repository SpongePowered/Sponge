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
package org.spongepowered.common.test.gametest.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.SessionService;
import com.mojang.authlib.services.ProfileResult;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.login.ClientboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.crypto.CryptException;
import net.minecraft.util.crypto.PublicPeerInfo;
import net.minecraft.util.crypto.SessionKeyExchange;
import net.minecraft.util.crypto.SessionKeys;
import org.spongepowered.common.mixin.gametest.MinecraftServerAccessor_GameTest;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@GameTestGroup("login")
public final class LoginGameTests {

    // Vanilla authenticates on a new thread per login; Sponge redirects its start() onto the login thread pool.
    @GameTest(maxTicks = 100)
    public void authenticationRunsOnLoginPool(final GameTestHelper helper) throws CryptException {
        final MinecraftServer server = helper.getLevel().getServer();
        final MinecraftServerAccessor_GameTest accessor = (MinecraftServerAccessor_GameTest) server;
        final boolean usedAuthentication = server.usesAuthentication();
        final Services services = accessor.accessor$services();
        final AtomicReference<String> authThread = new AtomicReference<>();

        // Accepts everyone and records which thread asked
        final SessionService sessionService = (SessionService) Proxy.newProxyInstance(
            SessionService.class.getClassLoader(), new Class<?>[] {SessionService.class},
            (proxy, method, args) -> {
                if (!method.getName().equals("hasJoinedServer")) {
                    return null;
                }
                authThread.set(Thread.currentThread().getName());
                return new ProfileResult(new GameProfile(UUID.randomUUID(), (String) args[0]));
            });
        server.setUsesAuthentication(true);
        accessor.accessor$services(new Services(sessionService, services.servicesKeySet(), services.profileRepository(), services.nameToIdCache(), services.profileResolver()));

        final Connection connection = new Connection(PacketFlow.SERVERBOUND);
        // The handlers encryption is inserted next to
        final EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter(), new ChannelOutboundHandlerAdapter(), connection);
        channel.pipeline().replace(channel.pipeline().first(), "splitter", new ChannelInboundHandlerAdapter());
        channel.pipeline().replace(ChannelOutboundHandlerAdapter.class, "prepender", new ChannelOutboundHandlerAdapter());
        final ServerLoginPacketListenerImpl listener = new ServerLoginPacketListenerImpl(server, connection, false);

        listener.handleHello(new ServerboundHelloPacket("LoginPool", UUID.randomUUID()));
        channel.runPendingTasks();
        final ClientboundKeyPacket key = channel.readOutbound();
        // What the client answers with: its half of the key exchange, signed over the keys both sides derive
        final SessionKeyExchange client = SessionKeyExchange.createClientSide("LoginPool", SharedConstants.getCurrentVersion().protocolVersion(), key.salt(), key.shouldAuthenticate());
        final SessionKeys keys = client.createSessionKeys(new PublicPeerInfo(key.serverKey(), key.serverId()));
        listener.handleKey(new ServerboundKeyPacket(client.publicKey(), keys.computeIntegrityDigest()));

        helper.startSequence()
            .thenWaitUntil(() -> GameTestAssertions.assertTrue(helper, authThread.get() != null, "The session service was never asked"))
            .thenExecute(() -> {
                server.setUsesAuthentication(usedAuthentication);
                accessor.accessor$services(services);
                channel.close();
                GameTestAssertions.assertTrue(helper, authThread.get().startsWith("Sponge-LoginThread-"),
                    "Authentication ran on " + authThread.get() + " instead of the login thread pool");
            })
            .thenSucceed();
    }
}
