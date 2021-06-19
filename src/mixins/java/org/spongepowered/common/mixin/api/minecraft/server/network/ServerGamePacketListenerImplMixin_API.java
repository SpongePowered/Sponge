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
package org.spongepowered.common.mixin.api.minecraft.server.network;

import static java.util.Objects.requireNonNull;

import net.kyori.adventure.text.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.network.ServerPlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.net.InetSocketAddress;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_API implements ServerPlayerConnection {

    // @formatter:off
    @Shadow @Final public Connection connection;
    @Shadow public net.minecraft.server.level.ServerPlayer player;
    @Shadow public abstract void shadow$disconnect(net.minecraft.network.chat.Component reason);
    // @formatter:on

    @Override
    public GameProfile profile() {
        return SpongeGameProfile.of(this.player.getGameProfile());
    }

    @Override
    public void close() {
        this.shadow$disconnect(new TranslatableComponent("disconnect.disconnected"));
    }

    @Override
    public void close(final Component reason) {
        requireNonNull(reason, "reason");
        this.shadow$disconnect(SpongeAdventure.asVanilla(reason));
    }

    @Override
    public ServerPlayer player() {
        return (ServerPlayer) this.player;
    }

    @Override
    public InetSocketAddress address() {
        return ((ConnectionBridge) this.connection).bridge$getAddress();
    }

    @Override
    public InetSocketAddress virtualHost() {
        return ((ConnectionBridge) this.connection).bridge$getVirtualHost();
    }

    @Override
    public int latency() {
        return this.player.latency;
    }
}
