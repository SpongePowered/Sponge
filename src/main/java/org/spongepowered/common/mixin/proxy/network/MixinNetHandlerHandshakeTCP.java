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
package org.spongepowered.common.mixin.proxy.network;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinNetworkManager;
import org.spongepowered.common.interfaces.network.handshake.client.IMixinCHandshake;
import org.spongepowered.common.proxy.HandshakeResponse;
import org.spongepowered.common.proxy.ProxyManager;
import org.spongepowered.common.text.SpongeTexts;

import java.net.InetSocketAddress;

@Mixin(NetHandlerHandshakeTCP.class)
public abstract class MixinNetHandlerHandshakeTCP {

    @Shadow private NetworkManager networkManager;

    @Inject(method = "processHandshake", at = @At(value = "HEAD"), cancellable = true)
    public void onProcessHandshakeStart(C00Handshake packetIn, CallbackInfo ci) {
        if (packetIn.getRequestedState() != EnumConnectionState.LOGIN) {
            return;
        }

        if (!ProxyManager.INSTANCE.forwardsClientDetails()) {
            return;
        }

        HandshakeResponse response = new HandshakeResponse();
        ProxyManager.INSTANCE.handshake(((IMixinCHandshake) packetIn).getHandshakeRequest(), response);

        // Disconnect the client if we've failed handshaking
        if (response.isFailed()) {
            this.disconnect(response.getCancelMessage());
            return;
        }

        packetIn.ip = response.getServerHostname();
        ((IMixinNetworkManager) this.networkManager).setRemoteAddress(new InetSocketAddress(response.getClientHostname(),
            ((InetSocketAddress) this.networkManager.getRemoteAddress()).getPort()));
        ((IMixinNetworkManager) this.networkManager).setProxyProfile(response.getProfile());
    }

    private void disconnect(Text text) {
        IChatComponent component = SpongeTexts.toComponent(text);
        this.networkManager.sendPacket(new S00PacketDisconnect(component));
        this.networkManager.closeChannel(component);
    }
}
