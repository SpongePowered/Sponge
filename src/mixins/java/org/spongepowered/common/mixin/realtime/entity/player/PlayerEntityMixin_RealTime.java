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
package org.spongepowered.common.mixin.realtime.entity.player;

import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.mixin.realtime.entity.LivingEntityMixin_RealTime;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin_RealTime extends LivingEntityMixin_RealTime {

    @Shadow public int xpCooldown;
    @Shadow private int sleepTimer;

    @Redirect(method = "tick",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;xpCooldown:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void realTimeImpl$adjustForRealTimeXpCooldown(final PlayerEntity self, final int modifier) {
        if (SpongeImplHooks.isFakePlayer((PlayerEntity) (Object) this) || ((WorldBridge) this.world).bridge$isFake()) {
            this.xpCooldown = modifier;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) self.getEntityWorld()).realTimeBridge$getRealTimeTicks();
        this.xpCooldown = Math.max(0, this.xpCooldown - ticks);
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/PlayerEntity;sleepTimer:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/player/PlayerEntity;isSleeping()Z"
            ),
            to = @At(
                value = "CONSTANT",
                args = "intValue=100",
                ordinal = 0
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeSleepTimer(final PlayerEntity self, final int modifier) {
        if (SpongeImplHooks.isFakePlayer((PlayerEntity) (Object) this) || ((WorldBridge) this.world).bridge$isFake()) {
            this.sleepTimer = modifier;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) self.getEntityWorld()).realTimeBridge$getRealTimeTicks();
        this.sleepTimer += ticks;
    }

    @Redirect(
        method = "tick()V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/PlayerEntity;sleepTimer:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/player/PlayerEntity;stopSleepInBed(ZZ)V",
                ordinal = 1
            ),
            to = @At(
                value = "CONSTANT",
                args = "intValue=110"
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeWakeTimer(final PlayerEntity self, final int modifier) {
        if (SpongeImplHooks.isFakePlayer((PlayerEntity) (Object) this) || ((WorldBridge) this.world).bridge$isFake()) {
            this.sleepTimer = modifier;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) self.getEntityWorld()).realTimeBridge$getRealTimeTicks();
        this.sleepTimer += ticks;
    }

}
