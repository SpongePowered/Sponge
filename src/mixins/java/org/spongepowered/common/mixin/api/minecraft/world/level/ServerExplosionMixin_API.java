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
package org.spongepowered.common.mixin.api.minecraft.world.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.ExplosionBridge;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

@Mixin(net.minecraft.world.level.ServerExplosion.class)
public abstract class ServerExplosionMixin_API implements Explosion {

    //@formatter:off
    @Shadow @Final private boolean fire;
    @Shadow @Final private net.minecraft.world.level.Level level;

    @Shadow @Final private Entity source;
    @Shadow @Final private float radius;
    @Shadow @Final private net.minecraft.world.level.Explosion.BlockInteraction blockInteraction;
    @Shadow @Final private Vec3 center;

    //@formatter:on

    @Nullable private ServerLocation api$location;

    @Override
    public ServerLocation location() {
        if (this.api$location == null) {
            this.api$location = ServerLocation.of((ServerWorld) this.level, VecHelper.toVector3d(this.center));
        }
        return this.api$location;
    }

    @Override
    public Optional<Explosive> sourceExplosive() {
        if (this.source instanceof Explosive) {
            return Optional.of((Explosive) this.source);
        }

        return Optional.empty();
    }

    @Override
    public float radius() {
        return this.radius;
    }

    @Override
    public boolean canCauseFire() {
        return this.fire;
    }

    @Override
    public boolean shouldPlaySmoke() {
        return ((ExplosionBridge) this).bridge$getShouldPlaySmoke();
    }

    @Override
    public boolean shouldBreakBlocks() {
        return switch (this.blockInteraction) {
            case KEEP, TRIGGER_BLOCK -> false;
            case DESTROY, DESTROY_WITH_DECAY -> true;
        };
    }

    @Override
    public boolean shouldDamageEntities() {
        return ((ExplosionBridge) this).bridge$getShouldDamageEntities();
    }

    @Override
    public int resolution() {
        return  ((ExplosionBridge) this).bridge$getResolution();
    }

    @Override
    public float randomness() {
        return  ((ExplosionBridge) this).bridge$getRandomness();
    }

    @Override
    public double knockback() {
        return  ((ExplosionBridge) this).bridge$getKnockback();
    }

}
