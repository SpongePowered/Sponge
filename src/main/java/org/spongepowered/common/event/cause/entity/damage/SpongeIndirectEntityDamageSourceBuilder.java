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
package org.spongepowered.common.event.cause.entity.damage;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSourceBuilder;
import org.spongepowered.common.accessor.world.damagesource.DamageSourceAccessor;
import java.lang.ref.WeakReference;

public final class SpongeIndirectEntityDamageSourceBuilder extends AbstractDamageSourceBuilder<IndirectEntityDamageSource, IndirectEntityDamageSource.Builder> implements IndirectEntityDamageSource.Builder {

    protected WeakReference<Entity> reference = null;
    private WeakReference<Entity> proxy = null;

    @Override
    public IndirectEntityDamageSource.Builder proxySource(final Entity projectile) {
        this.proxy = new WeakReference<>(projectile);
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder entity(final Entity entity) {
        this.reference = new WeakReference<>(entity);
        return this;
    }

    @Override
    public IndirectEntityDamageSource build() throws IllegalStateException {
        checkState(this.reference.get() != null);
        checkState(this.proxy.get() != null);
        checkState(this.damageType != null);

        final net.minecraft.world.damagesource.IndirectEntityDamageSource damageSource =
            new net.minecraft.world.damagesource.IndirectEntityDamageSource(this.damageType.name(),
                (net.minecraft.world.entity.Entity) this.reference.get(),
                (net.minecraft.world.entity.Entity) this.proxy.get());
        final DamageSourceAccessor accessor = (DamageSourceAccessor) damageSource;
        if (this.creative) {
            accessor.invoker$bypassInvul();
        }
        if (this.scales) {
            damageSource.setScalesWithDifficulty();
        }
        if (this.magical) {
            damageSource.setMagic();
        }
        if (this.bypasses) {
            accessor.invoker$bypassArmor();
        }
        if (this.absolute) {
            accessor.invoker$bypassMagic();
        }
        if (this.explosion) {
            damageSource.setExplosion();
        }
        if (this.exhaustion != null) {
            accessor.accessor$exhaustion(this.exhaustion.floatValue());
        }
        return (IndirectEntityDamageSource) damageSource;
    }

    @Override
    public IndirectEntityDamageSource.Builder from(final IndirectEntityDamageSource value) {
        super.from(value);
        this.reference = new WeakReference<>(value.source());
        this.proxy = new WeakReference<>(value.indirectSource());
        return this;
    }

    @Override
    public IndirectEntityDamageSource.Builder reset() {
        super.reset();
        this.reference = null;
        this.proxy = null;
        return this;
    }
}
