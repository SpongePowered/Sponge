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
package org.spongepowered.common.mixin.realtime.mixin;

import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.mixin.realtime.IMixinRealTimeTicking;

@Mixin(EntityXPOrb.class)
public abstract class MixinEntityXPOrb_RealTime extends MixinEntity_RealTime {

    @Shadow public int delayBeforeCanPickup;
    @Shadow public int xpOrbAge;

    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/item/EntityXPOrb;delayBeforeCanPickup:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/Entity;onUpdate()V"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/item/EntityXPOrb;hasNoGravity()Z"
            )
        )
    )
    private void adjustForRealTimePickupDelay(EntityXPOrb self, int modifier) {
        if (((WorldBridge) this.world).isFake()) {
            this.delayBeforeCanPickup = modifier;
            return;
        }
        int ticks = (int) ((IMixinRealTimeTicking) this.world).getRealTimeTicks();
        this.delayBeforeCanPickup = Math.max(0, this.delayBeforeCanPickup - ticks);
    }

    @Redirect(
        method = "onUpdate",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/item/EntityXPOrb;xpOrbAge:I",
            opcode = Opcodes.PUTFIELD
        ),
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lnet/minecraft/entity/item/EntityXPOrb;xpColor:I",
                opcode = Opcodes.PUTFIELD
            ),
            to = @At(
                value = "CONSTANT",
                args = "intValue=6000"
            )
        )
    )
    private void adjustForRealTimeAge(EntityXPOrb self, int modifier) {
        if (((WorldBridge) this.world).isFake()) {
            this.xpOrbAge = modifier;
            return;
        }
        int ticks = (int) ((IMixinRealTimeTicking) self.getEntityWorld()).getRealTimeTicks();
        this.xpOrbAge += ticks;
    }

}
