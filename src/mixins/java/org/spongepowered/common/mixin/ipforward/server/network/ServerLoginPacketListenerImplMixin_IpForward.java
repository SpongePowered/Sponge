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
package org.spongepowered.common.mixin.ipforward.server.network;


import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.applaunch.config.common.IpForwardingCategory;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.network.ConnectionBridge_IpForward;
import org.spongepowered.common.ipforward.velocity.VelocityForwardingInfo;
import org.spongepowered.common.util.Preconditions;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin_IpForward {

    // @formatter:off
    @Shadow @Final MinecraftServer server;
    @Shadow @Final Connection connection;
    @Shadow @Nullable String requestedUsername;
    // @formatter:on

    private boolean ipForward$sentVelocityForwardingRequest;

    // Velocity
    @Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
    private void ipForward$sendVelocityIndicator(final ServerboundHelloPacket packet, final CallbackInfo info) {
        if (!this.server.usesAuthentication() && SpongeConfigs.getCommon().get().ipForwarding.mode == IpForwardingCategory.Mode.MODERN) {
            Preconditions.checkState(!this.ipForward$sentVelocityForwardingRequest, "Sent additional login start message!");
            this.ipForward$sentVelocityForwardingRequest = true;

            this.requestedUsername = packet.name();

            VelocityForwardingInfo.sendQuery((ServerLoginPacketListenerImpl) (Object) this);

            info.cancel();
        }
    }

    // Bungee
    @ModifyArg(method = "handleHello",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V",
            ordinal = 1))
    private GameProfile bungee$initUuid(GameProfile $$0) {
        if (!this.server.usesAuthentication() && SpongeConfigs.getCommon().get().ipForwarding.mode == IpForwardingCategory.Mode.LEGACY) {
            final UUID uuid;
            if (((ConnectionBridge_IpForward) this.connection).bungeeBridge$getSpoofedUUID() != null) {
                uuid = ((ConnectionBridge_IpForward) this.connection).bungeeBridge$getSpoofedUUID();
            } else {
                uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + $$0.getName()).getBytes(StandardCharsets.UTF_8));
            }

            $$0 = new GameProfile(uuid, $$0.getName());

            if (((ConnectionBridge_IpForward) this.connection).bungeeBridge$getSpoofedProfile() != null) {
                for (final Property property : ((ConnectionBridge_IpForward) this.connection).bungeeBridge$getSpoofedProfile()) {
                    $$0.getProperties().put(property.name(), property);
                }
            }
        }

        return $$0;
    }

}
