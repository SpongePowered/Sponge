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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.interfaces.entity.IMixinEntityTNTPrimed;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityTNTPrimed.class)
public abstract class MixinEntityTNTPrimed extends MixinEntity implements IMixinEntityTNTPrimed {

    @Shadow private int fuse;

    @Nullable private EntityLivingBase detonator;
    private int explosionRadius = DataConstants.Entity.PrimedTNT.DEFAULT_EXPLOSION_RADIUS;
    private int fuseDuration = 80;
    private boolean detonationCancelled;

    @Override
    public void setDetonator(EntityLivingBase detonator) {
        this.detonator = detonator;
    }

    @Override
    public Optional<Integer> getExplosionRadius() {
        return Optional.of(this.explosionRadius);
    }

    @Override
    public void setExplosionRadius(Optional<Integer> radius) {
        this.explosionRadius = radius.orElse(DataConstants.Entity.PrimedTNT.DEFAULT_EXPLOSION_RADIUS);
    }

    @Override
    public int getFuseDuration() {
        return this.fuseDuration;
    }

    @Override
    public void setFuseDuration(int fuseTicks) {
        this.fuseDuration = fuseTicks;
    }

    @Override
    public int getFuseTicksRemaining() {
        return this.fuse;
    }

    @Override
    public void setFuseTicksRemaining(int fuseTicks) {
        this.fuse = fuseTicks;
    }


    @Inject(method = "explode", at = @At("HEAD"))
    private void preExplode(CallbackInfo ci) {
    }

    @Nullable
    @Redirect(
        method = "explode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/Explosion;"
        )
    )
    private net.minecraft.world.Explosion spongeImpl$UseSpongeExplosionInstead(net.minecraft.world.World worldObj, Entity self, double x,
                                                      double y, double z, float strength, boolean smoking) {
        return detonate(Explosion.builder()
                .location(new Location<>((World) worldObj, new Vector3d(x, y, z)))
                .sourceExplosive((PrimedTNT) this)
                .radius(this.explosionRadius)
                .shouldPlaySmoke(smoking)
                .shouldBreakBlocks(smoking))
                .orElseGet(() -> {
                    this.detonationCancelled = true;
                    return null;
                });
    }

    @Inject(method = "explode", at = @At("RETURN"))
    private void postExplode(CallbackInfo ci) {
        if (this.detonationCancelled) {
            defuse();
            this.detonationCancelled = false;
        }
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void onSpongeUpdateTNTPushPrime(CallbackInfo ci) {
        if (this.fuse == this.fuseDuration - 1 && !this.world.isRemote) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                if (this.detonator != null) {
                    frame.pushCause(this.detonator);
                }
                frame.pushCause(this);
                postPrime();
            }
        }
    }

}
