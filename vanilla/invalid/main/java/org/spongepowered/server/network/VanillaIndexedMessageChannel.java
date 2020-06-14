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
package org.spongepowered.server.network;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.network.SpongeNetworkManager;
import org.spongepowered.server.bridge.network.NetHandlerPlayServerBridge_Vanilla;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class VanillaIndexedMessageChannel extends VanillaChannelBinding implements ChannelBinding.IndexedMessageChannel {

    private final Byte2ObjectMap<IndexedMessageType<?>> messageIds = new Byte2ObjectOpenHashMap<>();
    private final Map<Class<? extends Message>, IndexedMessageType<?>> messageClasses = new HashMap<>();

    public VanillaIndexedMessageChannel(ChannelRegistrar registrar, String name, PluginContainer owner) {
        super(registrar, name, owner);
    }

    @Override
    public void registerMessage(Class<? extends Message> messageClass, int messageId) {
        validate();
        checkNotNull(messageClass, "messageClass");

        byte id = (byte) messageId;
        IndexedMessageType<?> type = this.messageIds.get(id);
        checkState(type == null, "Message ID %s is already assigned to %s", id, type);

        type = new IndexedMessageType<>(id, messageClass);
        this.messageIds.put(id, type);
        this.messageClasses.put(messageClass, type);
    }

    @Override
    public <M extends Message> void registerMessage(Class<M> messageClass, int messageId, MessageHandler<M> handler) {
        checkNotNull(handler, "handler");
        registerMessage(messageClass, messageId);
        registerHandler(messageClass, handler);
    }

    @Override
    public <M extends Message> void registerMessage(Class<M> messageClass, int messageId, Platform.Type side, MessageHandler<M> handler) {
        checkNotNull(handler, "handler");
        registerMessage(messageClass, messageId);
        registerHandler(messageClass, side, handler);
    }

    @SuppressWarnings("unchecked")
    private <M extends Message> void registerHandler(Class<M> messageClass, MessageHandler<M> handler) {
        IndexedMessageType<M> message = (IndexedMessageType<M>) this.messageClasses.get(messageClass);
        checkArgument(message != null, "Unregistered message class: %s", messageClass);

        message.handlers.add(handler);
    }

    private <M extends Message> void registerHandler(Class<M> messageClass, Platform.Type side, MessageHandler<M> handler) {
        if (side == Platform.Type.SERVER) {
            registerHandler(messageClass, handler);
        }
    }

    @Override
    public <M extends Message> void addHandler(Class<M> messageClass, MessageHandler<M> handler) {
        validate();
        checkNotNull(handler, "handler");
        registerHandler(messageClass, handler);
    }

    @Override
    public <M extends Message> void addHandler(Class<M> messageClass, Platform.Type side, MessageHandler<M> handler) {
        validate();
        checkNotNull(handler, "handler");
        registerHandler(messageClass, side, handler);
    }

    @Override
    public void post(RemoteConnection connection, PacketBuffer payload) {
        try {
            byte id = payload.readByte();
            IndexedMessageType<?> type = this.messageIds.get(id);
            checkNotNull(type, "Unknown message with id %s", id);

            type.post(connection, payload.slice());
        } catch (Throwable e) {
            getOwner().getLogger().error("Failed to read indexed message for channel {} of {}", getName(), getOwner(), e);
        }
    }

    private SPacketCustomPayload createPacket(Message message) {
        Class<? extends Message> messageClass = message.getClass();
        IndexedMessageType<?> type = this.messageClasses.get(messageClass);
        checkNotNull(type, "Unknown message type %s of %s", messageClass, message);

        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeByte(type.id);
        buffer.markWriterIndex();
        message.writeTo(SpongeNetworkManager.toChannelBuf(buffer));

        return new SPacketCustomPayload(getName(), buffer);
    }

    @Override
    public void sendTo(Player player, Message message) {
        validate();
        final EntityPlayerMP playerMP = (EntityPlayerMP) player;
        if (((NetHandlerPlayServerBridge_Vanilla) playerMP.connection).vanillaBridge$supportsChannel(getName())) {
            playerMP.connection.sendPacket(createPacket(message));
        }
    }

    @Override
    public void sendToServer(Message message) {
        validate();
        // Nothing to do here
    }

    @Override
    public void sendToAll(Message message) {
        validate();
        final String name = getName();
        SPacketCustomPayload packet = null;
        for (EntityPlayerMP player : SpongeImpl.getServer().getPlayerList().getPlayers()) {
            if (((NetHandlerPlayServerBridge_Vanilla) player.connection).vanillaBridge$supportsChannel(name)) {
                if (packet == null) {
                    packet = createPacket(message);
                }

                player.connection.sendPacket(packet);
            }
        }
    }

    private final class IndexedMessageType<T extends Message> {

        private final byte id;
        private final Class<T> messageClass;
        private final Set<MessageHandler<T>> handlers = Sets.newIdentityHashSet();

        private IndexedMessageType(byte id, Class<T> messageClass) {
            this.id = id;
            this.messageClass = messageClass;
        }

        private T read(ByteBuf buf) throws Exception {
            // Woo, reflection!
            T message = this.messageClass.newInstance();
            message.readFrom(SpongeNetworkManager.toChannelBuf(buf));
            return message;
        }

        private void post(RemoteConnection connection, T message) {
            for (MessageHandler<T> listener : this.handlers) {
                try {
                    listener.handleMessage(message, connection, Platform.Type.SERVER);
                } catch (Throwable e) {
                    getOwner().getLogger().error("Could not pass indexed message {} on channel '{}' to {}", message, getName(), getOwner(), e);
                }
            }
        }

        private void post(RemoteConnection connection, ByteBuf buf) throws Exception {
            post(connection, read(buf));
        }


        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", this.id)
                    .add("class", this.messageClass)
                    .toString();
        }

    }

}
