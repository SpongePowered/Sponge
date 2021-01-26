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
package org.spongepowered.common.mixin.realtime.world.entity.player;

import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.mixin.realtime.world.entity.LivingEntityMixin_RealTime;

@Mixin(Player.class)
public abstract class PlayerMixin_RealTime extends LivingEntityMixin_RealTime {

    @Shadow public int takeXpDelay;
    @Shadow private int sleepCounter;

    @Redirect(method = "tick",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;takeXpDelay:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void realTimeImpl$adjustForRealTimeXpCooldown(final Player self, final int modifier) {
        if (((PlatformEntityBridge) (Player) (Object) this).bridge$isFakePlayer() || ((WorldBridge) this.level).bridge$isFake()) {
            this.takeXpDelay = modifier;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) self.getCommandSenderWorld()).realTimeBridge$getRealTimeTicks();
        this.takeXpDelay = Math.max(0, this.takeXpDelay - ticks);
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Player;sleepCounter:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/player/Player;isSleeping()Z"
            ),
            to = @At(
                value = "CONSTANT",
                args = "intValue=100",
                ordinal = 0
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeSleepTimer(final Player self, final int modifier) {
        if (((PlatformEntityBridge) (Player) (Object) this).bridge$isFakePlayer() || ((WorldBridge) this.level).bridge$isFake()) {
            this.sleepCounter = modifier;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) self.getCommandSenderWorld()).realTimeBridge$getRealTimeTicks();
        this.sleepCounter += ticks;
    }

    @Redirect(
        method = "tick()V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Player;sleepCounter:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/player/Player;stopSleepInBed(ZZ)V",
                ordinal = 1
            ),
            to = @At(
                value = "CONSTANT",
                args = "intValue=110"
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeWakeTimer(final Player self, final int modifier) {
        if (((PlatformEntityBridge) (Player) (Object) this).bridge$isFakePlayer() || ((WorldBridge) this.level).bridge$isFake()) {
            this.sleepCounter = modifier;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) self.getCommandSenderWorld()).realTimeBridge$getRealTimeTicks();
        this.sleepCounter += ticks;
    }

}
