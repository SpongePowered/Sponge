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
package org.spongepowered.common.mixin.ipforward.network;

import com.mojang.authlib.properties.Property;
import io.netty.channel.SimpleChannelInboundHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.network.ConnectionBridge_IpForward;

import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

@Mixin(Connection.class)
public abstract class ConnectionMixin_IpForward extends SimpleChannelInboundHandler<Packet<?>> implements ConnectionBridge_IpForward {

    private UUID ipForward$spoofedUUID;
    private Property[] ipForward$spoofedProfile;

    @Override
    public UUID bungeeBridge$getSpoofedUUID() {
        return this.ipForward$spoofedUUID;
    }

    @Override
    public void bungeeBridge$setSpoofedUUID(final UUID uuid) {
        this.ipForward$spoofedUUID = uuid;
    }

    @Override
    public Property[] bungeeBridge$getSpoofedProfile() {
        return this.ipForward$spoofedProfile;
    }

    @Override
    public void bungeeBridge$setSpoofedProfile(final Property[] profile) {
        this.ipForward$spoofedProfile = profile;
    }
}
