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
package org.spongepowered.common.network.channel.raw;

import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.ChannelException;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.handshake.RawHandshakeDataChannel;
import org.spongepowered.api.network.channel.raw.handshake.RawHandshakeDataRequestHandler;
import org.spongepowered.api.network.channel.raw.handshake.RawHandshakeDataRequestResponse;
import org.spongepowered.common.network.channel.ConnectionUtil;
import org.spongepowered.common.network.channel.PacketSender;
import org.spongepowered.common.network.PacketUtil;
import org.spongepowered.common.network.channel.SpongeChannel;
import org.spongepowered.common.network.channel.TransactionResult;
import org.spongepowered.common.network.channel.TransactionStore;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;

@SuppressWarnings("unchecked")
public class SpongeRawLoginDataChannel implements RawHandshakeDataChannel {

    private final SpongeRawDataChannel parent;

    private final Map<Class<?>, RawHandshakeDataRequestHandler<?>> requestHandlers = new ConcurrentHashMap<>();

    public SpongeRawLoginDataChannel(final SpongeRawDataChannel parent) {
        this.parent = parent;
    }

    private <C extends EngineConnection> RawHandshakeDataRequestHandler<? super C> getRequestHandler(final C connection) {
        return (RawHandshakeDataRequestHandler<? super C>) SpongeChannel.getRequestHandler(connection, this.requestHandlers);
    }

    @Override
    public RawDataChannel parent() {
        return this.parent;
    }

    @Override
    public <C extends EngineConnection> void setRequestHandler(final EngineConnectionSide<C> side,
            final RawHandshakeDataRequestHandler<? super C> handler) {
        Objects.requireNonNull(side, "side");
        this.setRequestHandler(SpongeChannel.getConnectionClass(side), handler);
    }

    @Override
    public void setRequestHandler(final RawHandshakeDataRequestHandler<EngineConnection> handler) {
        this.setRequestHandler(EngineConnection.class, handler);
    }

    @Override
    public <C extends EngineConnection> void setRequestHandler(final Class<C> connectionType,
            final RawHandshakeDataRequestHandler<? super C> handler) {
        Objects.requireNonNull(connectionType, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.requestHandlers.put(connectionType, handler);
    }

    <C extends EngineConnection> void handleRequestPayload(final C connection, final ChannelBuf payload, final int transactionId) {
        final RawHandshakeDataRequestHandler<? super C> handler = this.getRequestHandler(connection);
        final RawHandshakeDataRequestResponse response = new RawHandshakeDataRequestResponse() {
            private boolean completed;

            private void checkCompleted() {
                if (this.completed) {
                    throw new ChannelException("The request response was already completed.");
                }
                this.completed = true;
            }

            @Override
            public void fail(final ChannelException exception) {
                Objects.requireNonNull(exception, "exception");
                this.checkCompleted();
                PacketSender.sendTo(connection, PacketUtil.createLoginPayloadResponse(null, transactionId));
            }

            @Override
            public void success(final Consumer<ChannelBuf> response) {
                Objects.requireNonNull(response, "response");
                this.checkCompleted();
                final ChannelBuf payload;
                try {
                    payload = SpongeRawLoginDataChannel.this.parent.encodePayload(response);
                } catch (final Throwable t) {
                    SpongeRawLoginDataChannel.this.parent.handleException(connection, new ChannelException("Failed to encode login data response", t), null);
                    PacketSender.sendTo(connection, PacketUtil.createLoginPayloadResponse(null, transactionId));
                    return;
                }
                PacketSender.sendTo(connection, PacketUtil.createLoginPayloadResponse(payload, transactionId));
            }
        };
        boolean success = false;
        if (handler != null) {
            try {
                handler.handleRequest(payload, connection, response);
                success = true;
            } catch (final Throwable t) {
                this.parent.handleException(connection, new ChannelException("Failed to handle login data request", t), null);
            }
        }
        if (!success) {
            PacketSender.sendTo(connection, PacketUtil.createLoginPayloadResponse(null, transactionId));
        }
    }

    void handleTransactionResponse(final EngineConnection connection, final Object stored, final TransactionResult result) {
        if (!(stored instanceof Consumer)) {
            return;
        }
        ((Consumer<TransactionResult>) stored).accept(result);
    }

    @Override
    public CompletableFuture<ChannelBuf> sendTo(final EngineConnection connection, final Consumer<ChannelBuf> payload) {
        ConnectionUtil.checkHandshakePhase(connection);

        final CompletableFuture<ChannelBuf> future = new CompletableFuture<>();
        final ChannelBuf buf;
        try {
            buf = this.parent.encodePayload(payload);
        } catch (final Throwable t) {
            this.parent.handleException(connection, t, future);
            return future;
        }

        final TransactionStore transactionStore = ConnectionUtil.getTransactionStore(connection);
        final int transactionId = transactionStore.nextId();

        final Consumer<TransactionResult> resultConsumer = result -> {
            if (result.isSuccess()) {
                future.complete(result.getPayload());
            } else {
                this.parent.handleException(connection, result.getCause(), future);
            }
        };

        final Packet<?> mcPacket = PacketUtil.createLoginPayloadRequest(this.parent.key(), buf, transactionId);

        PacketSender.sendTo(connection, mcPacket, sendFuture -> {
            if (sendFuture.isSuccess()) {
                transactionStore.put(transactionId, this.parent, resultConsumer);
            } else {
                // The packet already failed before it could reach the client
                future.completeExceptionally(sendFuture.cause());
            }
        });

        return future;
    }
}
