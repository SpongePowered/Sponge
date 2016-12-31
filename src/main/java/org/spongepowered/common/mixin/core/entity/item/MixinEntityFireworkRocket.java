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
package org.spongepowered.common.mixin.core.entity.item;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.interfaces.entity.IMixinEntityFireworkRocket;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityFireworkRocket.class)
public abstract class MixinEntityFireworkRocket extends MixinEntity implements Firework, IMixinEntityFireworkRocket {

    private static final String TARGET_ENTITY_STATE = "Lnet/minecraft/world/World;setEntityState"
            + "(Lnet/minecraft/entity/Entity;B)V";
    private static final int DEFAULT_EXPLOSION_RADIUS = 0;

    @Shadow private int fireworkAge;
    @Shadow private int lifetime;

    @Nullable private Cause detonationCause;
    private ProjectileSource projectileSource = ProjectileSource.UNKNOWN;
    private int explosionRadius = DEFAULT_EXPLOSION_RADIUS;

    @Override
    public ProjectileSource getShooter() {
        return this.projectileSource;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        this.projectileSource = shooter;
    }

    @Override
    public void setModifier(byte modifier) {
        this.lifetime = 10 * modifier + this.rand.nextInt(6) + this.rand.nextInt(7);
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        ProjectileSourceSerializer.readSourceFromNbt(compound, this);
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        ProjectileSourceSerializer.writeSourceToNbt(compound, this.projectileSource, null);
    }

    // FusedExplosive Impl

    @Override
    public void detonate() {
        this.fireworkAge = this.lifetime + 1;
    }

    @Override
    public void prime() {
        checkState(!isPrimed(), "already primed");
        checkState(this.isDead, "firework about to be primed");
        getWorld().spawnEntity(this);
    }

    @Override
    public void defuse() {
        checkState(isPrimed(), "not primed");
        if (shouldDefuse()) {
            setDead();
            postDefuse();
        }
    }

    @Override
    public boolean isPrimed() {
        return this.fireworkAge > 0 && this.fireworkAge <= this.lifetime && !this.isDead;
    }

    @Override
    public int getFuseDuration() {
        return this.lifetime;
    }

    @Override
    public void setFuseDuration(int fuseTicks) {
        this.lifetime = fuseTicks;
    }

    @Override
    public int getFuseTicksRemaining() {
        return this.lifetime - this.fireworkAge;
    }

    @Override
    public void setFuseTicksRemaining(int fuseTicks) {
        this.fireworkAge = 0;
        this.lifetime = fuseTicks;
    }

    @Override
    public Optional<Integer> getExplosionRadius() {
        return Optional.of(this.explosionRadius);
    }

    @Override
    public void setExplosionRadius(Optional<Integer> radius) {
        this.explosionRadius = radius.orElse(DEFAULT_EXPLOSION_RADIUS);
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = TARGET_ENTITY_STATE))
    protected void onExplode(World world, Entity self, byte state) {
        // Fireworks don't typically explode like other explosives, but we'll
        // post an event regardless and if the radius is zero the explosion
        // won't be triggered (the default behavior).
        Sponge.getCauseStackManager().pushCause(getShooter());
        detonate(Explosion.builder()
                .sourceExplosive(this)
                .location(getLocation())
                .radius(this.explosionRadius))
                .ifPresent(explosion -> world.setEntityState(self, state));
        Sponge.getCauseStackManager().popCause();
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    protected void onUpdate(CallbackInfo ci) {
        if (this.fireworkAge == 1) {
            postPrime();
        }
    }
}
