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
package org.spongepowered.vanilla.mixin.core.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.network.channel.SpongeChannelManager;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin_Vanilla extends ClientCommonPacketListenerImpl implements ClientGamePacketListener {

    protected ClientPacketListenerMixin_Vanilla(Minecraft $$0, Connection $$1, CommonListenerCookie $$2) {
        super($$0, $$1, $$2);
    }

    @Inject(method = "handleCustomPayload", cancellable = true, at = @At(value = "HEAD"))
    private void vanilla$handleCustomPayload(final CustomPacketPayload packet, CallbackInfo ci) {
        final SpongeChannelManager channelRegistry = (SpongeChannelManager) Sponge.channelManager();
        if (channelRegistry.handlePlayPayload(((ConnectionBridge) this.connection).bridge$getEngineConnection(), (EngineConnectionState) this, packet)) {
            ci.cancel();
        }
    }
}
