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
package org.spongepowered.common.mixin.api.mcp.entity.item;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.FireworkEffectData;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeFireworkEffectData;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.mixin.api.mcp.entity.EntityMixin_API;

import java.util.Collection;
import net.minecraft.entity.item.FireworkRocketEntity;

@Mixin(FireworkRocketEntity.class)
public abstract class EntityFireworkRocketMixin_API extends EntityMixin_API implements Firework {

    @Shadow private int fireworkAge;
    @Shadow private int lifetime;

    @Shadow public abstract void onUpdate();

    private ProjectileSource projectileSource = ProjectileSource.UNKNOWN;

    @Override
    public ProjectileSource getShooter() {
        return this.projectileSource;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        this.projectileSource = shooter;
    }


    @Override
    public FireworkEffectData getFireworkData() {
        return new SpongeFireworkEffectData(FireworkUtils.getFireworkEffects(this).orElse(Lists.newArrayList()));
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(this.getFireworkData());
    }

    @Override
    public void detonate() {
        this.fireworkAge = this.lifetime + 1;
        this.onUpdate();
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
        if (((FusedExplosiveBridge) this).bridge$shouldDefuse()) {
            setDead();
            ((FusedExplosiveBridge) this).bridge$postDefuse();
        }
    }

    @Override
    public boolean isPrimed() {
        return this.fireworkAge > 0 && this.fireworkAge <= this.lifetime && !this.isDead;
    }

}
