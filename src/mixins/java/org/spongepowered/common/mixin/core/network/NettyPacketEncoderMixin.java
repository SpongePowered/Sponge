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
package org.spongepowered.common.mixin.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.IPacket;
import net.minecraft.network.NettyPacketEncoder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ProtocolType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.PacketBufferBridge;

@Mixin(NettyPacketEncoder.class)
public class NettyPacketEncoderMixin {

    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;writeVarInt(I)"
            + "Lnet/minecraft/network/PacketBuffer;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void applyLocaleToBuffer(final ChannelHandlerContext ctx, final IPacket<?> pkt, final ByteBuf orig, final CallbackInfo ci,
            final ProtocolType unused$proto, final Integer unused$id, final PacketBuffer buffer) {
        ((PacketBufferBridge) buffer).bridge$setLocale(ctx.channel().attr(SpongeAdventure.CHANNEL_LOCALE).get());
    }
}
