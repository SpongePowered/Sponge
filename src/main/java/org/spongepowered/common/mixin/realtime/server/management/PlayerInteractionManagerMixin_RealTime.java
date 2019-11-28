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
package org.spongepowered.common.mixin.realtime.server.management;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;
import org.spongepowered.common.bridge.world.WorldBridge;

@Mixin(PlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin_RealTime {

    @Shadow public World world;
    @Shadow private int curblockDamage;

    @Shadow public ServerPlayerEntity player;

    @Redirect(
        method = "updateBlockRemoving",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/management/PlayerInteractionManager;curblockDamage:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At("HEAD"),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/server/management/PlayerInteractionManager;receivedFinishDiggingPacket:Z",
                opcode = Opcodes.GETFIELD
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeDiggingTime(final PlayerInteractionManager self, final int modifier) {
        if (SpongeImplHooks.isFakePlayer(this.player) || ((WorldBridge) this.world).bridge$isFake()) {
            this.curblockDamage = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.world.getServer()).realTimeBridge$getRealTimeTicks();
        this.curblockDamage += ticks;
    }

}
