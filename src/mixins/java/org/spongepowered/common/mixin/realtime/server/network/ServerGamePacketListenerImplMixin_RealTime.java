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
package org.spongepowered.common.mixin.realtime.server.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_RealTime {

    @Shadow private int chatSpamTickCount;
    @Shadow private int dropSpamTickCount;
    @Shadow @Final private MinecraftServer server;
    @Shadow public ServerPlayer player;

    @Redirect(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;chatSpamTickCount:I",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0
        )
    )
    private void realTimeImpl$adjustForRealTimeChatSpamCheck(final ServerGamePacketListenerImpl self, final int modifier) {
        if (((PlatformEntityBridge) this.player).bridge$isFakePlayer() || ((WorldBridge) this.player.level).bridge$isFake()) {
            this.chatSpamTickCount = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.server).realTimeBridge$getRealTimeTicks();
        this.chatSpamTickCount = Math.max(0, this.chatSpamTickCount - ticks);
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;dropSpamTickCount:I",
            opcode = Opcodes.PUTFIELD, ordinal = 0
        )
    )
    private void realTimeImpl$adjustForRealTimeDropSpamCheck(final ServerGamePacketListenerImpl self, final int modifier) {
        if (((PlatformEntityBridge) this.player).bridge$isFakePlayer() || ((WorldBridge) this.player.level).bridge$isFake()) {
            this.dropSpamTickCount = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.server).realTimeBridge$getRealTimeTicks();
        this.dropSpamTickCount = Math.max(0, this.dropSpamTickCount - ticks);
    }

}
