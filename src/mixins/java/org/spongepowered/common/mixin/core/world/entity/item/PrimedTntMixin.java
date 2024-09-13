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
package org.spongepowered.common.mixin.core.world.entity.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.bridge.world.entity.item.PrimedTntBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin extends EntityMixin implements PrimedTntBridge, FusedExplosiveBridge, ExplosiveBridge {

    // @formatter:off
    @Shadow public abstract int shadow$getFuse();
    @Shadow public abstract void shadow$setFuse(int var1);
    @Shadow private float explosionPower;

    // @formatter:on

    @Nullable private LivingEntity impl$detonator;
    private int bridge$fuseDuration = Constants.Entity.PrimedTNT.DEFAULT_FUSE_DURATION;

    @Override
    public void bridge$setDetonator(final LivingEntity detonator) {
        this.impl$detonator = detonator;
    }

    @Override
    public boolean bridge$isExploding() {
        return this.shadow$isRemoved() && this.shadow$getFuse() <= 0;
    }

    @Override
    public Optional<Float> bridge$getExplosionRadius() {
        return Optional.of(this.explosionPower);
    }

    @Override
    public void bridge$setExplosionRadius(final @Nullable Float radius) {
        this.explosionPower = radius == null ? Constants.Entity.PrimedTNT.DEFAULT_EXPLOSION_RADIUS : radius;
    }

    @Override
    public int bridge$getFuseDuration() {
        return this.bridge$fuseDuration;
    }

    @Override
    public void bridge$setFuseDuration(final int fuseTicks) {
        this.bridge$fuseDuration = fuseTicks;
    }

    @Override
    public int bridge$getFuseTicksRemaining() {
        return this.shadow$getFuse();
    }

    @Override
    public void bridge$setFuseTicksRemaining(final int fuseTicks) {
        this.shadow$setFuse(fuseTicks);
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void impl$updateTNTPushPrime(final CallbackInfo ci) {
        if (this.shadow$getFuse() == this.bridge$fuseDuration - 1 && !this.shadow$level().isClientSide) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                if (this.impl$detonator != null) {
                    frame.pushCause(this.impl$detonator);
                }
                frame.pushCause(this);
                this.bridge$postPrime();
            }
        }
    }

}
