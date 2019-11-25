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
package org.spongepowered.common.mixin.realtime.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;
import org.spongepowered.common.bridge.world.WorldBridge;

@Mixin(Entity.class)
public abstract class EntityMixin_RealTime {

    @Shadow protected int rideCooldown;
    @Shadow public World world;
    @Shadow protected int portalCounter;
    @Shadow public int timeUntilPortal;

    @Redirect(method = "onEntityUpdate",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;rideCooldown:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/Entity;dismountRidingEntity()V"
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/entity/Entity;distanceWalkedModified:F",
                opcode = Opcodes.GETFIELD
            )
        )
    )
    private void realTimeImpl$adjustForRealTimeEntityCooldown(final Entity self, final int modifier) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            this.rideCooldown = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.world).realTimeBridge$getRealTimeTicks();
        this.rideCooldown = Math.max(0, this.rideCooldown - ticks);
    }

    @Redirect(method = "onEntityUpdate",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;portalCounter:I",
            opcode = Opcodes.PUTFIELD, ordinal = 0
        ),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getMaxInPortalTime()I"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPortalCooldown()I")
        )
    )
    private void realTimeImpl$adjustForRealTimePortalCounter(final Entity self, final int modifier) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            this.portalCounter = modifier;
            return;
        }
        final int ticks = (int) ((RealTimeTrackingBridge) this.world).realTimeBridge$getRealTimeTicks();
        this.portalCounter += ticks;
    }

}
