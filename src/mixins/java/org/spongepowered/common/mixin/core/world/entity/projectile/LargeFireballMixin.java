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
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.world.entity.projectile.LargeFireballBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@Mixin(LargeFireball.class)
public abstract class LargeFireballMixin extends AbstractHurtingProjectileMixin implements LargeFireballBridge, ExplosiveBridge {

    // @formatter:off
    @Shadow private int explosionPower;
    // @formatter:on

    @Redirect(method = "onHit",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"
        )
    )
    public void impl$onHitExplode(final Level world, final Entity thisEntity,
        final double x, final double y, final double z, final float explosionRadius, final boolean causesFire,
        final Level.ExplosionInteraction mode) {
        this.bridge$wrappedExplode(x, y, z, explosionRadius, causesFire, mode);
    }

    @Override
    public void bridge$wrappedExplode(final double x, final double y, final double z,
        final float explosionRadius, final boolean causesFire, final Level.ExplosionInteraction mode) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            ((Projectile) this).get(Keys.SHOOTER).ifPresent(shooter -> frame.addContext(EventContextKeys.PROJECTILE_SOURCE, shooter));
            this.level().explode((LargeFireball) (Object) this, x, y, z, explosionRadius, causesFire, mode);
        }
    }

    @Override
    public Optional<Float> bridge$getExplosionRadius() {
        return Optional.of((float) this.explosionPower);
    }

    @Override
    public void bridge$setExplosionRadius(final @Nullable Float radius) {
        this.explosionPower = radius == null ? Constants.Entity.Fireball.DEFAULT_EXPLOSION_RADIUS : radius.intValue();
    }

}
