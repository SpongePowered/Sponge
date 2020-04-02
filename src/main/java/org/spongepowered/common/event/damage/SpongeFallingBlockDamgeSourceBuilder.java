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
package org.spongepowered.common.event.damage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.entity.item.FallingBlockEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSourceBuilder;
import org.spongepowered.common.accessor.util.DamageSourceAccessor;

import java.lang.ref.WeakReference;

public class SpongeFallingBlockDamgeSourceBuilder extends AbstractDamageSourceBuilder<FallingBlockDamageSource, FallingBlockDamageSource.Builder>
    implements FallingBlockDamageSource.Builder {

    protected WeakReference<Entity> reference = null;

    @Override
    public SpongeFallingBlockDamgeSourceBuilder entity(final Entity entity) {
        checkArgument(entity instanceof FallingBlock);
        this.reference = new WeakReference<>(entity);
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder fire() {
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public FallingBlockDamageSource build() throws IllegalStateException {
        checkState(this.reference.get() != null);
        checkState(this.damageType != null);
        final MinecraftFallingBlockDamageSource damageSource =
            new MinecraftFallingBlockDamageSource(this.damageType.getKey().getFormatted(),
                (FallingBlockEntity) this.reference.get());
        final DamageSourceAccessor accessor = (DamageSourceAccessor) damageSource;
        if (this.creative) {
            accessor.accessor$setDamageAllowedInCreativeMode();
        }
        if (this.scales) {
            damageSource.setDifficultyScaled();
        }
        if (this.magical) {
            damageSource.setMagicDamage();
        }
        if (this.bypasses) {
            accessor.accessor$setDamageBypassesArmor();
        }
        if (this.absolute) {
            accessor.accessor$setDamageIsAbsolute();
        }
        if (this.explosion) {
            damageSource.setExplosion();
        }
        if (this.exhaustion != null) {
            accessor.accessor$setHungerDamage(this.exhaustion.floatValue());
        }
        return (FallingBlockDamageSource) damageSource;
    }

    @Override
    public FallingBlockDamageSource.Builder from(final FallingBlockDamageSource value) {
        super.from(value);
        this.reference = new WeakReference<>(value.getSource());
        return this;
    }

    @Override
    public SpongeFallingBlockDamgeSourceBuilder reset() {
        super.reset();
        this.reference = null;
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder places(final boolean canPlace) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public FallingBlockDamageSource.Builder fallTime(final int time) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public FallingBlockDamageSource.Builder hurtsEntities(final boolean hurts) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public FallingBlockDamageSource.Builder maxDamage(final double damage) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public FallingBlockDamageSource.Builder damagePerBlock(final double damagePer) {
        throw new UnsupportedOperationException("implement me");
    }
}
