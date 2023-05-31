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
package org.spongepowered.common.mixin.core.world.entity.projectile;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends ProjectileMixin {

    // @formatter:off
    @Shadow private boolean dealtDamage;
    // @formatter:on

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(final EntityHitResult hitResult, final CallbackInfo ci) {
        if (((LevelBridge) this.shadow$level()).bridge$isFake() || hitResult.getType() == HitResult.Type.MISS) {
            return;
        }

        if (SpongeCommonEventFactory.handleCollideImpactEvent((ThrownTrident) (Object) this,
                this.impl$getProjectileSource(), hitResult)) {
            this.shadow$playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
            this.dealtDamage = true;
            this.shadow$setDeltaMovement(this.shadow$getDeltaMovement().multiply(-0.01, -0.1, -0.01));
            ci.cancel();
        }
    }
}
