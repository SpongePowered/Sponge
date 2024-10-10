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
package org.spongepowered.common.network.channel.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.ChannelException;
import org.spongepowered.api.network.channel.ChannelIOException;
import org.spongepowered.api.network.channel.NoResponseException;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.api.network.channel.packet.PacketChannel;
import org.spongepowered.api.network.channel.packet.RequestPacket;
import org.spongepowered.api.network.channel.packet.RequestPacketHandler;
import org.spongepowered.common.network.PacketUtil;
import org.spongepowered.common.network.SpongeEngineConnection;
import org.spongepowered.common.network.channel.ConnectionUtil;
import org.spongepowered.common.network.channel.PacketSender;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.network.channel.TransactionResult;
import org.spongepowered.common.network.channel.TransactionStore;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SpongePacketChannel extends AbstractPacketChannel implements PacketChannel {

    /**
     * Represents a normal packet type.
     */
    static final int TYPE_NORMAL = 0;

    /**
     * Represents a request packet type. See {@link RequestPacket}.
     */
    static final int TYPE_REQUEST = 1;

    /**
     * Represents a response packet type.
     */
    static final int TYPE_RESPONSE = 2;

    /**
     * Represents a no response packet type.
     */
    static final int TYPE_NO_RESPONSE = 3;

    /**
     * Represents a response packet type when a request
     * type supports multiple response types.
     */
    static final int TYPE_DYNAMIC_RESPONSE = 4;

    static final int TYPE_BITS = 3; // 3 bits are reserved for types
    static final int TYPE_MASK = (1 << SpongePacketChannel.TYPE_BITS) - 1;

    /**
     * A value which is used when "not" a dynamic opcode is represented.
     */
    static final int NO_DYNAMIC_OPCODE = -1;

    public SpongePacketChannel(final int type, final ResourceKey key, final SpongeChannelManager manager) {
        super(type, key, manager);
    }

    private <P extends RequestPacket<R>, R extends Packet> void sendRequestPacketTo(final EngineConnection connection, final EngineConnectionState state,
            final P packet, final CompletableFuture<?> future, final @Nullable Consumer<R> response, final @Nullable Runnable sendSuccess) {
        final SpongeTransactionalPacketBinding<P, R> binding =
                (SpongeTransactionalPacketBinding) this.requireBinding(packet.getClass());

        final TransactionStore transactionStore = ConnectionUtil.getTransactionStore(connection);
        final int transactionId = transactionStore.nextId();

        final boolean isLoginPhase = ConnectionUtil.isLoginPhase(connection);
        final EngineConnectionSide<?> side = connection.side();

        final ChannelBuf payload = this.manager().getBufferAllocator().buffer();
        final Supplier<net.minecraft.network.protocol.Packet<?>> mcPacketSupplier;

        if (isLoginPhase) {
            if (side == EngineConnectionSide.CLIENT) {
                payload.writeString(this.key().formatted());
                payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_REQUEST, transactionId));
                payload.writeVarInt(binding.opcode());
                mcPacketSupplier = () -> PacketUtil.createLoginPayloadResponse(var1 -> {
                    var1.writeBytes((FriendlyByteBuf) payload);
                }, Constants.Channels.LOGIN_PAYLOAD_TRANSACTION_ID);
            } else {
                payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_REQUEST, binding.opcode()));
                final ResourceKey key = this.key();
                mcPacketSupplier = () -> PacketUtil.createLoginPayloadRequest(new CustomQueryPayload() {
                    @Override
                    public ResourceLocation id() {
                        return (ResourceLocation) (Object) key;
                    }

                    @Override
                    public void write(FriendlyByteBuf var1) {
                        var1.writeBytes((FriendlyByteBuf) payload);
                    }
                }, transactionId);
            }
        } else {
            payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_REQUEST, transactionId));
            payload.writeVarInt(binding.opcode());
            mcPacketSupplier = () -> PacketUtil.createPlayPayload(this.payloadType(), payload, side);
        }

        try {
            this.encodePayload(payload, packet);
        } catch (final Throwable ex) {
            this.handleException(connection, state, ex, future);
            return;
        }

        if (response != null) {
            final TransactionData<P, R> transactionData = new TransactionData<>(packet, binding, response, future);
            transactionStore.put(transactionId, this, transactionData);
        }

        final net.minecraft.network.protocol.Packet<?> mcPacket = mcPacketSupplier.get();
        PacketSender.sendTo(connection, mcPacket, throwable -> {
            if (throwable != null) {
                this.handleException(connection, state, throwable, future);
                // Failed before it could reach the client, so complete it
                // and remove it from the store
                if (response != null) {
                    transactionStore.remove(transactionId);
                }
            } else if (sendSuccess != null) {
                sendSuccess.run();
            }
        });
    }

    private <P extends RequestPacket<R>, R extends Packet> void sendResponsePacketTo(final EngineConnection connection,
            final EngineConnectionState state, final @Nullable SpongeTransactionalPacketBinding<P, R> requestBinding,
             final @Nullable R packet, final int transactionId) {
        final boolean isLoginPhase = ConnectionUtil.isLoginPhase(connection);
        final EngineConnectionSide<?> side = connection.side();

        final ChannelBuf payload = this.manager().getBufferAllocator().buffer();
        final Supplier<net.minecraft.network.protocol.Packet<?>> mcPacketSupplier;

        if (packet == null || requestBinding instanceof SpongeFixedTransactionalPacketBinding) {
            final int type = packet == null ? SpongePacketChannel.TYPE_NO_RESPONSE : SpongePacketChannel.TYPE_RESPONSE;
            if (isLoginPhase) {
                if (side == EngineConnectionSide.CLIENT) {
                    payload.writeVarLong(SpongePacketChannel.packTypeAndValue(type, 0));
                    mcPacketSupplier = () -> PacketUtil.createLoginPayloadResponse(var1 -> {
                        var1.writeBytes((FriendlyByteBuf) payload);
                    }, transactionId);
                } else {
                    payload.writeVarLong(SpongePacketChannel.packTypeAndValue(type, transactionId));
                    final ResourceKey key = this.key();
                    mcPacketSupplier = () -> PacketUtil.createLoginPayloadRequest(new CustomQueryPayload() {
                        @Override
                        public ResourceLocation id() {
                            return (ResourceLocation) (Object) key;
                        }

                        @Override
                        public void write(FriendlyByteBuf var1) {
                            var1.writeBytes((FriendlyByteBuf) payload);
                        }
                    }, Constants.Channels.LOGIN_PAYLOAD_TRANSACTION_ID);
                }
            } else {
                payload.writeVarLong(SpongePacketChannel.packTypeAndValue(type, transactionId));
                mcPacketSupplier = () -> PacketUtil.createPlayPayload(this.payloadType(), payload, side);
            }
        } else {
            // Dynamic opcode
            final int opcode = this.requireBinding(packet.getClass()).opcode();
            if (isLoginPhase) {
                if (side == EngineConnectionSide.CLIENT) {
                    payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_DYNAMIC_RESPONSE, opcode));
                    mcPacketSupplier = () -> PacketUtil.createLoginPayloadResponse(var1 -> {
                        var1.writeBytes((FriendlyByteBuf) payload);
                    }, transactionId);
                } else {
                    payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_DYNAMIC_RESPONSE, transactionId));
                    payload.writeVarInt(opcode);
                    final ResourceKey key = this.key();
                    mcPacketSupplier = () -> PacketUtil.createLoginPayloadRequest(new CustomQueryPayload() {
                        @Override
                        public ResourceLocation id() {
                            return (ResourceLocation) (Object) key;
                        }

                        @Override
                        public void write(FriendlyByteBuf var1) {
                            var1.writeBytes((FriendlyByteBuf) payload);
                        }
                    }, Constants.Channels.LOGIN_PAYLOAD_TRANSACTION_ID);
                }
            } else {
                payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_DYNAMIC_RESPONSE, transactionId));
                payload.writeVarInt(opcode);
                mcPacketSupplier = () -> PacketUtil.createPlayPayload(this.payloadType(), payload, side);
            }
        }

        if (packet != null) {
            try {
                this.encodePayload(payload, packet);
            } catch (final Throwable ex) {
                this.handleException(connection, state, new ChannelIOException("Failed to encode request response", ex), null);
                return;
            }
        }

        final net.minecraft.network.protocol.Packet<?> mcPacket = mcPacketSupplier.get();
        PacketSender.sendTo(connection, mcPacket);
    }

    private <P extends Packet> void sendNormalPacketTo(final EngineConnection connection, final P packet, final CompletableFuture<Void> future) {
        final SpongePacketBinding<P> binding =
                (SpongePacketBinding) this.requireBinding(packet.getClass());

        final boolean isLoginPhase = ConnectionUtil.isLoginPhase(connection);
        final EngineConnectionSide<?> side = connection.side();

        final ChannelBuf payload = this.manager().getBufferAllocator().buffer();
        final Supplier<net.minecraft.network.protocol.Packet<?>> mcPacketSupplier;

        if (isLoginPhase) {
            if (side == EngineConnectionSide.CLIENT) {
                payload.writeString(this.key().formatted());
                payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_NORMAL, binding.opcode()));
                mcPacketSupplier = () -> PacketUtil.createLoginPayloadResponse(var1 -> {
                    var1.writeBytes((FriendlyByteBuf) payload);
                }, Constants.Channels.LOGIN_PAYLOAD_TRANSACTION_ID);
            } else {
                payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_NORMAL, binding.opcode()));
                final int transactionId = ConnectionUtil.getTransactionStore(connection).nextId();
                final ResourceKey key = this.key();
                mcPacketSupplier = () -> PacketUtil.createLoginPayloadRequest(new CustomQueryPayload() {
                    @Override
                    public ResourceLocation id() {
                        return (ResourceLocation) (Object) key;
                    }

                    @Override
                    public void write(FriendlyByteBuf var1) {
                        var1.writeBytes((FriendlyByteBuf) payload);
                    }
                }, transactionId);
            }
        } else {
            payload.writeVarLong(SpongePacketChannel.packTypeAndValue(SpongePacketChannel.TYPE_NORMAL, binding.opcode()));
            mcPacketSupplier = () -> PacketUtil.createPlayPayload(this.payloadType(), payload, side);
        }

        try {
            this.encodePayload(payload, packet);
        } catch (final Throwable ex) {
            future.completeExceptionally(ex);
            return;
        }

        final net.minecraft.network.protocol.Packet<?> mcPacket = mcPacketSupplier.get();
        PacketSender.sendTo(connection, mcPacket, future);
    }

    @Override
    public <R extends Packet> CompletableFuture<R> sendTo(final EngineConnection connection, final RequestPacket<R> packet) {
        final EngineConnectionState state = (EngineConnectionState) ((SpongeEngineConnection) connection).connection().getPacketListener();
        final CompletableFuture<R> future = new CompletableFuture<>();
        if (!this.checkSupported(connection, state, future)) {
            return future;
        }
        this.sendRequestPacketTo(connection, state, packet, future, future::complete, null);
        return future;
    }

    @Override
    public boolean isSupportedBy(final EngineConnection connection) {
        Objects.requireNonNull(connection, "connection");
        return ConnectionUtil.getRegisteredChannels(connection).contains(this.key());
    }

    @Override
    public CompletableFuture<Void> sendTo(final EngineConnection connection, final Packet packet) {
        final EngineConnectionState state = (EngineConnectionState) ((SpongeEngineConnection) connection).connection().getPacketListener();
        final CompletableFuture<Void> future = new CompletableFuture<>();
        if (!this.checkSupported(connection, state, future)) {
            return future;
        }
        if (packet instanceof RequestPacket) {
            this.sendRequestPacketTo(connection, state, (RequestPacket<Packet>) packet,
                    future, null, () -> future.complete(null));
        } else {
            this.sendNormalPacketTo(connection, packet, future);
        }
        return future;
    }

    private void handleResponsePacket(final EngineConnection connection, final EngineConnectionState state, final int transactionId, final @Nullable ChannelBuf payload,
            final int dynamicOpcode) {
        final TransactionStore store = ConnectionUtil.getTransactionStore(connection);
        final TransactionStore.Entry stored = store.remove(transactionId);
        if (stored == null || stored.getData() == null) {
            return;
        }
        final TransactionData<RequestPacket<Packet>, Packet> transactionData =
                (TransactionData<RequestPacket<Packet>, Packet>) stored.getData();
        final TransactionResult result = payload == null ?
                TransactionResult.failure(new NoResponseException()) : TransactionResult.success(payload);
        this.handleTransactionResponse(connection, state, transactionData, result, dynamicOpcode);
    }

    private void handleRequestPacket(final EngineConnection connection, final EngineConnectionState state,
            final int opcode, final int transactionId, final ChannelBuf payload) {
        final SpongePacketBinding<Packet> binding = this.requireBinding(opcode);

        final Packet request;
        try {
            request = this.decodePayload(binding.getPacketConstructor(), payload);
        } catch (final Throwable ex) {
            this.sendResponsePacketTo(connection, state, null, null, transactionId);
            this.handleException(connection, state, new ChannelIOException("Failed to decode request packet", ex), null);
            return;
        }

        boolean success = false;
        Throwable responseFailure = null;

        // TODO: Send cause of failure somehow?
        if (binding instanceof SpongeTransactionalPacketBinding) {
            final RequestPacketHandler<RequestPacket<Packet>, Packet, ? super EngineConnectionState> handler =
                    ((SpongeTransactionalPacketBinding) binding).getRequestHandler(state);

            if (handler != null) {
                final SpongeRequestPacketResponse<Packet> requestPacketResponse = new SpongeRequestPacketResponse<Packet>() {

                    @Override
                    protected void fail0(final ChannelException exception) {
                        // TODO: Use response failure?
                        SpongePacketChannel.this.sendResponsePacketTo(connection, state, null, null, transactionId);
                    }

                    @Override
                    protected void success0(final Packet response) {
                        SpongePacketChannel.this.sendResponsePacketTo(connection, state, (SpongeTransactionalPacketBinding) binding, response, transactionId);
                    }
                };
                try {
                    handler.handleRequest((RequestPacket) request, state, requestPacketResponse);
                    success = true;
                } catch (final Throwable ex) {
                    this.handleException(connection, state, new ChannelException("Failed to handle request packet", ex), null);
                    responseFailure = ex;
                }
            }
        }
        if (!success) {
            // TODO: Use response failure?
            this.sendResponsePacketTo(connection, state, null, null, transactionId);
        }
    }

    private void handleNormalPacket(final EngineConnection connection, final EngineConnectionState state, final int opcode, final ChannelBuf payload) {
        final SpongePacketBinding<Packet> binding = this.requireBinding(opcode);
        final Packet packet = this.decodePayload(binding.getPacketConstructor(), payload);

        if (binding instanceof SpongeHandlerPacketBinding) {
            this.handle(connection, state, (SpongeHandlerPacketBinding<Packet>) binding, packet);
        }
    }

    @Override
    protected void handlePlayPayload(final EngineConnection connection, final EngineConnectionState state, final ChannelBuf payload) {
        final long typeAndValue = payload.readVarLong();

        final int type = SpongePacketChannel.extractType(typeAndValue);
        final int value = SpongePacketChannel.extractValue(typeAndValue);

        if (type == SpongePacketChannel.TYPE_NORMAL) {
            this.handleNormalPacket(connection, state, value, payload);
        } else if (type == SpongePacketChannel.TYPE_REQUEST) {
            final int opcode = payload.readVarInt();
            this.handleRequestPacket(connection, state, opcode, value, payload);
        } else if (type == SpongePacketChannel.TYPE_RESPONSE) {
            this.handleResponsePacket(connection, state, value, payload, SpongePacketChannel.NO_DYNAMIC_OPCODE);
        } else if (type == SpongePacketChannel.TYPE_NO_RESPONSE) {
            this.handleResponsePacket(connection, state, value, null, SpongePacketChannel.NO_DYNAMIC_OPCODE);
        } else if (type == SpongePacketChannel.TYPE_DYNAMIC_RESPONSE) {
            final int opcode = payload.readVarInt();
            this.handleResponsePacket(connection, state, value, payload, opcode);
        } else {
            this.handleException(connection, state, new ChannelIOException("Unknown packet type: " + type), null);
        }
    }

    @Override
    protected void handleLoginRequestPayload(final EngineConnection connection, EngineConnectionState state, final int transactionId, final ChannelBuf payload) {
        final long typeAndValue = payload.readVarLong();

        final int type = SpongePacketChannel.extractType(typeAndValue);
        final int value = SpongePacketChannel.extractValue(typeAndValue);

        if (type == SpongePacketChannel.TYPE_NORMAL) {
            this.handleNormalPacket(connection, state, value, payload);
        } else if (type == SpongePacketChannel.TYPE_REQUEST) {
            this.handleRequestPacket(connection, state, value, transactionId, payload);
        } else if (type == SpongePacketChannel.TYPE_RESPONSE) {
            this.handleResponsePacket(connection, state, transactionId, payload, SpongePacketChannel.NO_DYNAMIC_OPCODE);
        } else if (type == SpongePacketChannel.TYPE_NO_RESPONSE) {
            this.handleResponsePacket(connection, state, transactionId, null, SpongePacketChannel.NO_DYNAMIC_OPCODE);
        } else if (type == SpongePacketChannel.TYPE_DYNAMIC_RESPONSE) {
            this.handleResponsePacket(connection, state, transactionId, payload, value);
        } else {
            this.handleException(connection, state, new ChannelIOException("Unknown packet type: " + type), null);
        }
    }

    @Override
    protected void handleTransactionResponse(final EngineConnection connection, EngineConnectionState state, final Object stored, final TransactionResult result) {
        if (result.isSuccess()) {
            final ChannelBuf payload = result.getPayload();
            final long typeAndValue = payload.readVarLong();

            final int type = SpongePacketChannel.extractType(typeAndValue);
            final int value = SpongePacketChannel.extractValue(typeAndValue);

            if (type == SpongePacketChannel.TYPE_RESPONSE ||
                    type == SpongePacketChannel.TYPE_NO_RESPONSE ||
                    type == SpongePacketChannel.TYPE_DYNAMIC_RESPONSE) {
                final TransactionData<RequestPacket<Packet>, Packet> transactionData =
                        (TransactionData<RequestPacket<Packet>, Packet>) stored;
                if (type == SpongePacketChannel.TYPE_RESPONSE) {
                    this.handleTransactionResponse(connection, state, transactionData, result, SpongePacketChannel.NO_DYNAMIC_OPCODE);
                } else if (type == SpongePacketChannel.TYPE_NO_RESPONSE) {
                    this.handleTransactionResponse(connection, state, transactionData,
                            TransactionResult.failure(new NoResponseException()), SpongePacketChannel.NO_DYNAMIC_OPCODE
                    );
                } else {
                    this.handleTransactionResponse(connection, state, transactionData, result, value);
                }
            } else {
                this.handleException(connection, state, new ChannelIOException("Unknown packet type: " + type), null);
            }
        }
    }

    private <P extends RequestPacket<R>, R extends Packet> void handleTransactionResponse(final EngineConnection connection,
            EngineConnectionState state, final TransactionData<P, R> transactionData, final TransactionResult result, final int dynamicOpcode) {
        if (result.isSuccess()) {
            final ChannelBuf payload = result.getPayload();

            SpongePacketBinding<R> responseBinding = null;

            final Supplier<R> packetSupplier;
            if (dynamicOpcode != SpongePacketChannel.NO_DYNAMIC_OPCODE) {
                responseBinding = (SpongePacketBinding<R>) this.requireBinding(dynamicOpcode);
                packetSupplier = responseBinding.getPacketConstructor();
            } else if (transactionData.binding instanceof SpongeFixedTransactionalPacketBinding) {
                packetSupplier = (Supplier<R>) ((SpongeFixedTransactionalPacketBinding<RequestPacket<Packet>, Packet>) transactionData.binding)
                        .getResponsePacketConstructor();
            } else {
                throw new ChannelException("A fixed response was send but no fixed response was bound to the request: "
                        + transactionData.request.getClass());
            }

            final R packet;
            try {
                packet = this.decodePayload(packetSupplier, payload);
            } catch (final Throwable ex) {
                this.handleException(connection, state, new ChannelIOException("Failed to decode packet", ex), transactionData.future);
                return;
            }

            if (responseBinding != null) {
                this.handle(connection, state, (SpongeHandlerPacketBinding<Packet>) responseBinding, packet);
            } else {
                this.handleResponse(connection, state, transactionData.binding, transactionData.request, packet);
            }

            if (transactionData.success != null) {
                transactionData.success.accept(packet);
            }
        } else {
            this.handleException(connection, state, result.getCause(), transactionData.future);
            this.handleResponseFailure(connection, state, transactionData.binding, transactionData.request, result.getCause());
        }
    }

    private static long packTypeAndValue(final int type, final int value) {
        return type | Integer.toUnsignedLong(value) << SpongePacketChannel.TYPE_BITS;
    }

    private static int extractType(final long typeAndValue) {
        return (int) (typeAndValue & SpongePacketChannel.TYPE_MASK);
    }

    private static int extractValue(final long typeAndValue) {
        return (int) (typeAndValue >>> SpongePacketChannel.TYPE_BITS);
    }
}
