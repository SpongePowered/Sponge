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

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetHandlerHandshakeTCP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.CPacketHandshake;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.network.NetworkManagerBridge_Bungee;

import java.net.InetSocketAddress;

@Mixin(NetHandlerHandshakeTCP.class)
public abstract class NetHandlerHandshakeTCPMixin_Bungee {

    private static final Gson gson = new Gson();

    @Shadow @Final private NetworkManager networkManager;

    @Inject(method = "processHandshake", at = @At(value = "HEAD"), cancellable = true)
    private void bungee$patchHandshake(final CPacketHandshake packetIn, final CallbackInfo ci) {
        if (SpongeImpl.getGlobalConfigAdapter().getConfig().getBungeeCord().getIpForwarding() && packetIn.func_149594_c().equals(EnumConnectionState.LOGIN)) {
            final String[] split = packetIn.field_149598_b.split("\00\\|", 2)[0].split("\00"); // ignore any extra data

            if (split.length == 3 || split.length == 4) {
                packetIn.field_149598_b = split[0];
                ((NetworkManagerBridge_Bungee) this.networkManager).bungeeBridge$setRemoteAddress(new InetSocketAddress(split[1],
                        ((InetSocketAddress) this.networkManager.func_74430_c()).getPort()));
                ((NetworkManagerBridge_Bungee) this.networkManager).bungeeBridge$setSpoofedUUID(UUIDTypeAdapter.fromString(split[2]));

                if (split.length == 4) {
                    ((NetworkManagerBridge_Bungee) this.networkManager).bungeeBridge$setSpoofedProfile(gson.fromJson(split[3], Property[].class));
                }
            } else {
                final TextComponentString chatcomponenttext =
                        new TextComponentString("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                this.networkManager.func_179290_a(new SPacketDisconnect(chatcomponenttext));
                this.networkManager.func_150718_a(chatcomponenttext);
            }
        }
    }
}
