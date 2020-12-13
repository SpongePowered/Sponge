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

import net.minecraft.entity.AgeableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.RealTimeTrackingBridge;

@Mixin(AgeableEntity.class)
public abstract class AgeableEntityMixin_RealTime extends EntityMixin_RealTime {

    @Shadow public abstract void shadow$setAge(int age);

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/AgeableEntity;setAge(I)V"))
    private void realTimeImpl$adjustForRealTimeGrowingUp(final AgeableEntity self, final int age) {
        if (((WorldBridge) this.level).bridge$isFake()) {
            this.shadow$setAge(age);
            return;
        }
        // Subtract the one the original update method added
        final int diff = (int) ((RealTimeTrackingBridge) this.level).realTimeBridge$getRealTimeTicks() - 1;
        if (diff == 0) {
            this.shadow$setAge(age);
        } else if (age > 0) {
            this.shadow$setAge(Math.max(0, age - diff));
        } else {
            this.shadow$setAge(Math.min(0, age + diff));
        }
    }


}
