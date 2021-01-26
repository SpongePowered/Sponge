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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.explosive.fireball.ExplosiveFireball;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.entity.projectile.FireballEntityBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion.BlockInteraction;

@Mixin(LargeFireball.class)
public abstract class LargeFireballMixin extends AbstractHurtingProjectileMixin implements FireballEntityBridge, ExplosiveBridge {

    // @formatter:off
    @Shadow public int explosionPower;
    // @formatter:on

    /**
     * @author gabizou April 13th, 2018
     * @reason Due to changes from Forge, we have to redirect osr modify the gamerule check,
     * but since forge doesn't allow us to continue to check the gamerule method call here,
     * we have to modify the arguments passed in (the two booleans). There may be a better way,
     * which may include redirecting the world.newExplosion method call instead of modifyargs,
     * but, it is what it is.
     */
    @Redirect(method = "onHit",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;"
        )
    )
    @Nullable
    public net.minecraft.world.level.Explosion impl$throwExplosionEventAndExplode(final net.minecraft.world.level.Level worldObj, @Nullable final Entity nil,
        final double x, final double y, final double z, final float strength, final boolean flaming, final BlockInteraction mode) {
        return this.bridge$throwExplosionEventAndExplode(worldObj, nil, x, y, z, strength, flaming, mode);
    }

    @Override
    public net.minecraft.world.level.Explosion bridge$throwExplosionEventAndExplode(net.minecraft.world.level.Level worldObj, @Nullable Entity nil,
            double x, double y, double z, float strength, boolean flaming, net.minecraft.world.level.Explosion.BlockInteraction mode) {
        final boolean griefer = ((GrieferBridge) this).bridge$canGrief();
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            ((Projectile) this).get(Keys.SHOOTER).ifPresent(shooter -> frame.addContext(EventContextKeys.PROJECTILE_SOURCE, shooter));
            final Optional<net.minecraft.world.level.Explosion> ex = SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                    .location(ServerLocation.of((ServerWorld) worldObj, x, y, z))
                    .sourceExplosive(((ExplosiveFireball) this))
                    .radius(strength)
                    .canCauseFire(flaming && griefer)
                    .shouldPlaySmoke(mode != BlockInteraction.NONE && griefer)
                    .shouldBreakBlocks(mode != BlockInteraction.NONE && griefer));

            return ex.orElse(null);
        }
    }

    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.of(this.explosionPower);
    }

    @Override
    public void bridge$setExplosionRadius(@Nullable final Integer radius) {
        this.explosionPower = radius == null ? Constants.Entity.Fireball.DEFAULT_EXPLOSION_RADIUS : radius;
    }

}
