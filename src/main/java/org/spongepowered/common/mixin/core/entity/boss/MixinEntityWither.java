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
package org.spongepowered.common.mixin.core.entity.boss;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.world.GameRules;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.monster.Wither;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.interfaces.entity.explosive.IMixinFusedExplosive;
import org.spongepowered.common.mixin.core.entity.monster.MixinEntityMob;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = EntityWither.class)
public abstract class MixinEntityWither extends MixinEntityMob implements Wither, IMixinFusedExplosive {

    private static final String TARGET_NEW_EXPLOSION = "Lnet/minecraft/world/World;newExplosion"
            + "(Lnet/minecraft/entity/Entity;DDDFZZ)Lnet/minecraft/world/Explosion;";
    private static final int DEFAULT_EXPLOSION_RADIUS = 7;

    @Shadow public abstract int getWatchedTargetId(int p_82203_1_);
    @Shadow public abstract void updateWatchedTargetId(int targetOffset, int newId);
    @Shadow public abstract void setInvulTime(int ticks);
    @Shadow public abstract int getInvulTime();

    private int explosionRadius = DEFAULT_EXPLOSION_RADIUS;
    private int fuseDuration = 220;

    @Override
    public List<Living> getTargets() {
        List<Living> values = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int id = getWatchedTargetId(i);
            if (id > 0) {
                values.add((Living) this.world.getEntityByID(id));
            }
        }
        return values;
    }

    @Override
    public void setTargets(List<Living> targets) {
        checkNotNull(targets, "Targets are null!");
        for (int i = 0; i < 2; i++) {
            updateWatchedTargetId(i, targets.size() > i ? ((EntityLivingBase) targets.get(i)).getEntityId() : 0);
        }
    }

    @Redirect(method = "updateAITasks", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z"))
    private boolean onCanGrief(GameRules gameRules, String rule) {
        return gameRules.getBoolean(rule) && ((IMixinGriefer) this).canGrief();
    }

    @ModifyArg(method = "launchWitherSkullToCoords", at = @At(value = "INVOKE",
               target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    protected Entity onSpawnWitherSkull(Entity entity) {
        ((IMixinGriefer) entity).setCanGrief(((IMixinGriefer) this).canGrief());
        return entity;
    }

    // FusedExplosive Impl

    @Override
    public Optional<Integer> getExplosionRadius() {
        return Optional.of(this.explosionRadius);
    }

    @Override
    public void setExplosionRadius(Optional<Integer> radius) {
        this.explosionRadius = radius.orElse(DEFAULT_EXPLOSION_RADIUS);
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
        return getInvulTime();
    }

    @Override
    public void setFuseTicksRemaining(int fuseTicks) {
        setInvulTime(fuseTicks);
    }

    @Override
    public void detonate() {
        setFuseTicksRemaining(1);
    }

    @Override
    public void prime() {
        checkState(!isPrimed(), "already primed");
        if (shouldPrime()) {
            setFuseTicksRemaining(this.fuseDuration);
            postPrime();
        }
    }

    @Override
    public void defuse() {
        checkState(isPrimed(), "not primed");
        if (shouldDefuse()) {
            setInvulTime(0);
            postDefuse();
        }
    }

    @Override
    public boolean isPrimed() {
        return getFuseTicksRemaining() > 0;
    }

    /**
     * Called when a Wither is "primed" for the first time on spawn.
     *
     * @param self This entity
     * @param fuseTicks Ticks until detonation
     */
    @Redirect(method = "ignite", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/entity/boss/EntityWither;setInvulTime(I)V"))
    protected void onSpawnPrime(EntityWither self, int fuseTicks) {
        prime();
    }

    @Redirect(method = "updateAITasks", at = @At(value = "INVOKE", target = TARGET_NEW_EXPLOSION))
    protected net.minecraft.world.Explosion onExplode(net.minecraft.world.World worldObj, Entity self, double x,
                                                      double y, double z, float strength, boolean flaming,
                                                      boolean smoking) {
        return detonate(Explosion.builder()
                .sourceExplosive(this)
                .location(new Location<>((World) worldObj, new Vector3d(x, y, z)))
                .radius(this.explosionRadius)
                .canCauseFire(flaming)
                .shouldPlaySmoke(smoking)
                .shouldBreakBlocks(smoking && ((IMixinGriefer) this).canGrief()))
                .orElse(null);
    }

}
