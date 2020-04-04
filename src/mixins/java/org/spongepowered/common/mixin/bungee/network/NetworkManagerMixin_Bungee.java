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
package org.spongepowered.common.mixin.bungee.network;

import com.mojang.authlib.properties.Property;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.network.NetworkManagerBridge_Bungee;

import java.util.UUID;

@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin_Bungee extends SimpleChannelInboundHandler<IPacket<?>> implements NetworkManagerBridge_Bungee {

    private UUID bungee$spoofedUUID;
    private Property[] bungee$spoofedProfile;

    @Override
    public UUID bungeeBridge$getSpoofedUUID() {
        return this.bungee$spoofedUUID;
    }

    @Override
    public void bungeeBridge$setSpoofedUUID(final UUID uuid) {
        this.bungee$spoofedUUID = uuid;
    }

    @Override
    public Property[] bungeeBridge$getSpoofedProfile() {
        return this.bungee$spoofedProfile;
    }

    @Override
    public void bungeeBridge$setSpoofedProfile(final Property[] profile) {
        this.bungee$spoofedProfile = profile;
    }
}
