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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.projectile.source.EntityProjectileSource;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.projectile.UnknownProjectileSource;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends EntityMixin {

    // @formatter:off
    @Shadow protected abstract void shadow$onHit(HitResult result);
    @Shadow public abstract void shadow$setOwner(@Nullable Entity p_212361_1_);
    @Shadow public abstract @Nullable Entity shadow$getOwner();
    @Shadow protected abstract ProjectileDeflection shadow$hitTargetOrDeflectSelf(HitResult result);
    @Shadow protected abstract void onHit(HitResult result);
    @Shadow protected abstract ProjectileDeflection hitTargetOrDeflectSelf(HitResult result);

    // @formatter:on
    private ProjectileSource impl$projectileSource = UnknownProjectileSource.UNKNOWN;

    protected ProjectileSource impl$getProjectileSource() {
        if (this.impl$projectileSource != UnknownProjectileSource.UNKNOWN) {
            return this.impl$projectileSource;
        }
        final @org.checkerframework.checker.nullness.qual.Nullable Entity owner = this.shadow$getOwner();
        if (owner != null) {
            return (EntityProjectileSource) owner;
        }
        return this.impl$projectileSource;
    }

    @Inject(method = "setOwner", at = @At("RETURN"))
    private void impl$assignProjectileSource(final @Nullable Entity owner, final CallbackInfo ci) {
        this.impl$projectileSource = owner == null ? UnknownProjectileSource.UNKNOWN : (EntityProjectileSource) owner;
    }

    @Override
    protected void impl$callExpireEntityEvent() {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, this.impl$getProjectileSource());
            Sponge.eventManager().post(SpongeEventFactory.createExpireEntityEvent(frame.currentCause(), (org.spongepowered.api.entity.Entity) this));
        }
    }

}
