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

import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.server.bridge.network.NetHandlerPlayServerBridge_Vanilla;

import java.util.Set;
import java.util.function.Consumer;

public final class VanillaRawDataChannel extends VanillaChannelBinding implements ChannelBinding.RawDataChannel {

    private final Set<RawDataListener> listeners = Sets.newIdentityHashSet();

    public VanillaRawDataChannel(ChannelRegistrar registrar, String name, PluginContainer owner) {
        super(registrar, name, owner);
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
                getOwner().getLogger().error("Could not pass payload on channel '{}' to {}", getName(), getOwner(), e);
            }
        }
    }

    private SPacketCustomPayload createPacket(Consumer<ChannelBuf> consumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        consumer.accept((ChannelBuf) buffer);
        return new SPacketCustomPayload(getName(), buffer);
    }

    @Override
    public void sendTo(Player player, Consumer<ChannelBuf> payload) {
        validate();
        final EntityPlayerMP playerMP = (EntityPlayerMP) player;
        if (((NetHandlerPlayServerBridge_Vanilla) playerMP.connection).vanillaBridge$supportsChannel(getName())) {
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
        final String name = getName();
        SPacketCustomPayload packet = null;
        for (EntityPlayerMP player : SpongeImpl.getServer().getPlayerList().getPlayers()) {
            if (((NetHandlerPlayServerBridge_Vanilla) player.connection).vanillaBridge$supportsChannel(name)) {
                if (packet == null) {
                    packet = createPacket(payload);
                }

                player.connection.sendPacket(packet);
            }
        }
    }

}
