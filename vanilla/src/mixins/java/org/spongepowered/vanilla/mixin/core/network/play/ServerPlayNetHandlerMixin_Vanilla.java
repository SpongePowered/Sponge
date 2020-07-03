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
package org.spongepowered.vanilla.mixin.core.network.play;

import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.network.play.client.CCustomPayloadPacketAccessor;
import org.spongepowered.common.network.channel.SpongeChannelRegistry;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin_Vanilla implements IServerPlayNetHandler {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "processCustomPayload", at = @At(value = "HEAD"))
    private void onHandleCustomPayload(final CCustomPayloadPacket packet, final CallbackInfo ci) {
        // For some reason, "CCustomPayloadPacket" is released in the processPacket
        // method of its class, only applicable to this packet, so just retain here.
        ((CCustomPayloadPacketAccessor) packet).accessor$getPayload().retain();

        final SpongeChannelRegistry channelRegistry = (SpongeChannelRegistry) Sponge.getChannelRegistry();
        this.server.execute(() -> channelRegistry.handlePlayPayload((EngineConnection) this, packet));
    }
}
