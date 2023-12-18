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
package org.spongepowered.common.mixin.core.network;

import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalAddress;
import io.netty.util.concurrent.Future;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeMinecraftVersion;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.network.SpongePacketHolder;
import org.spongepowered.common.network.channel.PacketSender;
import org.spongepowered.common.network.channel.TransactionStore;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(Connection.class)
public abstract class ConnectionMixin extends SimpleChannelInboundHandler<Packet<?>> implements ConnectionBridge {

    @Shadow private PacketListener packetListener;
    @Shadow private Channel channel;
    @Shadow private boolean disconnectionHandled;
    @Shadow @Final private Queue<Consumer<Connection>> pendingActions;
    @Shadow public abstract SocketAddress getRemoteAddress();

    private final TransactionStore impl$transactionStore = new TransactionStore(() -> (EngineConnection) this.packetListener);
    private final Set<ResourceKey> impl$registeredChannels = Sets.newConcurrentHashSet();

    @Nullable private InetSocketAddress impl$virtualHost;
    @Nullable private MinecraftVersion impl$version;
    private net.minecraft.network.chat.@Nullable Component impl$kickReason;

    private ClientType impl$clientType = ClientType.VANILLA;

    private volatile boolean impl$disconnected;

    @Override
    public TransactionStore bridge$getTransactionStore() {
        return this.impl$transactionStore;
    }

    @Override
    public Set<ResourceKey> bridge$getRegisteredChannels() {
        return this.impl$registeredChannels;
    }

    @Override
    public ClientType bridge$getClientType() {
        return this.impl$clientType;
    }

    @Override
    public void bridge$setClientType(final ClientType clientType) {
        this.impl$clientType = clientType;
    }

    @Override
    public InetSocketAddress bridge$getAddress() {
        final SocketAddress remoteAddress = this.getRemoteAddress();
        if (remoteAddress instanceof LocalAddress) { // Single player
            return Constants.Networking.LOCALHOST;
        }
        return (InetSocketAddress) remoteAddress;
    }

    @Override
    public InetSocketAddress bridge$getVirtualHost() {
        if (this.impl$virtualHost != null) {
            return this.impl$virtualHost;
        }
        final SocketAddress local = this.channel.localAddress();
        if (local instanceof LocalAddress) {
            return Constants.Networking.LOCALHOST;
        }
        return (InetSocketAddress) local;
    }

    @Override
    public void bridge$setVirtualHost(final String host, final int port) {
        try {
            this.impl$virtualHost = new InetSocketAddress(InetAddress.getByAddress(host,
                    ((InetSocketAddress) this.channel.localAddress()).getAddress().getAddress()), port);
        } catch (final UnknownHostException e) {
            this.impl$virtualHost = InetSocketAddress.createUnresolved(host, port);
        }
    }

    @Override
    @Nullable
    public Component bridge$getKickReason() {
        return this.impl$kickReason;
    }

    @Override
    public void bridge$setKickReason(final Component component) {
        this.impl$kickReason = component;
    }

    @Override
    public MinecraftVersion bridge$getVersion() {
        return this.impl$version;
    }

    @Override
    public void bridge$setVersion(final int version) {
        this.impl$version = new SpongeMinecraftVersion(String.valueOf(version), version);
    }

    @Inject(method = "lambda$doSendPacket$10", at = @At(value = "INVOKE", target = "Lio/netty/util/concurrent/Future;isSuccess()Z"))
    public void impl$onPacketSent(final PacketSendListener $$0x, final Future $$1x, final CallbackInfo ci) {
        if ($$0x instanceof final PacketSender.SpongePacketSendListener spongeListener) {
            spongeListener.accept($$1x.cause());
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onSend(final Packet<?> $$0, final @Nullable PacketSendListener $$1, final CallbackInfo ci) {
        if (this.disconnectionHandled) {
            if ($$1 instanceof final PacketSender.SpongePacketSendListener spongeListener) {
                spongeListener.accept(new IOException("Connection has been closed."));
            }

            ci.cancel();
        }
    }

//    org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException: @Redirect handler method net/minecraft/network/Connection::impl$onQueue
//    from mixins.sponge.core.json:network.ConnectionMixin has an invalid signature.
//    Found unexpected argument type java.util.function.Consumer at index 1,
//    expected java.lang.Object.
//    Handler signature:  (Ljava/util/Queue;Ljava/util/function/Consumer;Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)Z
//    Expected signature: (Ljava/util/Queue;Ljava/lang/Object;Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)Z [INJECT Applicator Phase -> mixins.sponge.core.json:network.ConnectionMixin -> Apply Injections ->  -> Inject -> mixins.sponge.core.json:network.ConnectionMixin->@Redirect::impl$onQueue(Ljava/util/Queue;Ljava/util/function/Consumer;Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)Z]
    @Redirect(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z"))
    private boolean impl$onQueue(final Queue instance, final Object consumer,
            final Packet<?> $$0, final @Nullable PacketSendListener $$1, final boolean $$2) {
        if ($$1 instanceof PacketSender.SpongePacketSendListener spongeListener) {
            return instance.add(new SpongePacketHolder() {
                @Override
                public void apply(final Throwable t) {
                    spongeListener.accept(t);
                }

                @Override
                public void accept(final Connection connection) {
                    ((Consumer<Connection>) consumer).accept(connection);
                }
            });
        }
        return instance.add(consumer);
    }

    @Inject(method = "handleDisconnection",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/network/Connection;disconnectionHandled:Z",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER))
    private void impl$onDisconnected(final CallbackInfo ci) {
        Consumer<Connection> consumer;
        while ((consumer = this.pendingActions.poll()) != null) {
            if (consumer instanceof SpongePacketHolder packetHolder) {
                packetHolder.apply(new IOException("Connection has been closed."));
            }
        }
    }

    @Inject(method = "disconnect", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelFuture;awaitUninterruptibly()Lio/netty/channel/ChannelFuture;"), cancellable = true)
    private void impl$disconnectAsync(final CallbackInfo ci) {
        ci.cancel(); //This can cause deadlock within the event loop

        //Because we are now disconnecting asynchronously the channel might not
        //be immediately flagged as closed so special case it
        this.impl$disconnected = true;
    }

    @Inject(method = "isConnected", at = @At("HEAD"), cancellable = true)
    private void impl$onIsConnected(final CallbackInfoReturnable<Boolean> cir) {
        if (this.impl$disconnected) {
            cir.setReturnValue(false);
        }
    }
}
