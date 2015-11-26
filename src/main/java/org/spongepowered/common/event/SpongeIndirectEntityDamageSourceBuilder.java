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
package org.spongepowered.common.event;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;

import java.lang.ref.WeakReference;

public class SpongeIndirectEntityDamageSourceBuilder extends SpongeEntityDamageSourceBuilder implements IndirectEntityDamageSource.Builder {

    private WeakReference<Entity> proxy = null;

    @Override
    public IndirectEntityDamageSource.Builder scalesWithDifficulty() {
        super.scalesWithDifficulty();
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder bypassesArmor() {
        super.bypassesArmor();
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder explosion() {
        super.explosion();
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder absolute() {
        super.absolute();
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder magical() {
        super.magical();
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder creative() {
        super.creative();
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder type(DamageType damageType) {
        super.type(damageType);
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder proxySource(Entity projectile) {
        this.proxy = new WeakReference<>(projectile);
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder entity(Entity entity) {
        this.reference = new WeakReference<>(entity);
        return this;
    }

    @Override
    public IndirectEntityDamageSource build() throws IllegalStateException {
        checkState(this.reference.get() != null);
        checkState(this.proxy.get() != null);
        checkState(this.damageType != null);
        net.minecraft.util.EntityDamageSourceIndirect damageSource =
            new net.minecraft.util.EntityDamageSourceIndirect(this.damageType.getId(),
                (net.minecraft.entity.Entity) this.reference.get(),
                (net.minecraft.entity.Entity) this.proxy.get());
        if (this.creative) {
            damageSource.setDamageAllowedInCreativeMode();
        }
        if (this.scales) {
            damageSource.setDifficultyScaled();
        }
        if (this.magical) {
            damageSource.setMagicDamage();
        }
        if (this.bypasses) {
            damageSource.setDamageBypassesArmor();
        }
        if (this.absolute) {
            damageSource.setDamageIsAbsolute();
        }
        if (this.explosion) {
            damageSource.setExplosion();
        }
        return (IndirectEntityDamageSource) damageSource;
    }

    @Override
    public IndirectEntityDamageSource.Builder reset() {
        super.reset();
        this.reference = null;
        this.proxy = null;
        return this;
    }
}
