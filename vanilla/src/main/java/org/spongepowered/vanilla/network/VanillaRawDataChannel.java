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
package org.spongepowered.vanilla.network;

import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.vanilla.bridge.network.NetHandlerPlayServerBridge_Vanilla;

import java.util.Set;
import java.util.function.Consumer;

public final class VanillaRawDataChannel extends VanillaChannelBinding implements ChannelBinding.RawDataChannel {

    private final Set<RawDataListener> listeners = Sets.newIdentityHashSet();

    public VanillaRawDataChannel(ChannelRegistrar registrar, ResourceKey registration, PluginContainer owner) {
        super(registrar, registration, owner);
    }

    @Override
    public void addListener(RawDataListener listener) {
        validate();
        this.listeners.add(listener);
    }

    @Override
    public void addListener(Platform.Type side, RawDataListener listener) {
        if (side == Platform.Type.SERVER) {
            addListener(listener);
        }
    }

    @Override
    public void removeListener(RawDataListener listener) {
        validate();
        this.listeners.remove(listener);
    }

    @Override
    public void post(RemoteConnection connection, PacketBuffer payload) {
        final ChannelBuf buf = (ChannelBuf) payload;
        for (RawDataListener listener : this.listeners) {
            try {
                listener.handlePayload(buf, connection, Platform.Type.SERVER);
            } catch (Throwable e) {
                getOwner().getLogger().error("Could not pass payload on channel '{}' to {}", getKey(), getOwner(), e);
            }
        }
    }

    private SCustomPayloadPlayPacket createPacket(Consumer<ChannelBuf> consumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        consumer.accept((ChannelBuf) buffer);
        return new SCustomPayloadPlayPacket((ResourceLocation) (Object) getKey(), buffer);
    }

    @Override
    public void sendTo(ServerPlayer player, Consumer<ChannelBuf> payload) {
        validate();
        final ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
        if (((NetHandlerPlayServerBridge_Vanilla) playerMP.connection).vanillaBridge$supportsChannel((ResourceLocation) (Object) getKey())) {
            playerMP.connection.sendPacket(createPacket(payload));
        }
    }

    @Override
    public void sendToServer(Consumer<ChannelBuf> payload) {
        validate();
        // Nothing to do here
    }

    @Override
    public void sendToAll(Consumer<ChannelBuf> payload) {
        validate();
        SCustomPayloadPlayPacket packet = null;
        for (ServerPlayerEntity player : SpongeCommon.getServer().getPlayerList().getPlayers()) {
            if (((NetHandlerPlayServerBridge_Vanilla) player.connection).vanillaBridge$supportsChannel((ResourceLocation) (Object) getKey())) {
                if (packet == null) {
                    packet = createPacket(payload);
                }

                player.connection.sendPacket(packet);
            }
        }
    }

}
