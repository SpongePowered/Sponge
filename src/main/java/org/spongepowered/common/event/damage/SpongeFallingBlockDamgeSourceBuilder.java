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

import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSourceBuilder;
import org.spongepowered.common.mixin.core.util.DamageSourceAccessor;

import java.lang.ref.WeakReference;
import net.minecraft.entity.item.FallingBlockEntity;

public class SpongeFallingBlockDamgeSourceBuilder extends AbstractDamageSourceBuilder<FallingBlockDamageSource, FallingBlockDamageSource.Builder>
    implements FallingBlockDamageSource.Builder {

    protected WeakReference<Entity> reference = null;
    private ImmutableFallingBlockData blockData = null;

    @Override
    public SpongeFallingBlockDamgeSourceBuilder fallingBlock(final ImmutableFallingBlockData fallingBlock) {
        this.blockData = fallingBlock;
        return this;
    }

    @Override
    public SpongeFallingBlockDamgeSourceBuilder entity(final Entity entity) {
        checkArgument(entity instanceof FallingBlock);
        this.reference = new WeakReference<>(entity);
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public FallingBlockDamageSource build() throws IllegalStateException {
        checkState(this.reference.get() != null);
        checkState(this.blockData != null);
        checkState(this.damageType != null);
        final MinecraftFallingBlockDamageSource damageSource =
            new MinecraftFallingBlockDamageSource(this.damageType.getId(),
                (FallingBlockEntity) this.reference.get(),
                this.blockData);
        final DamageSourceAccessor accessor = (DamageSourceAccessor) damageSource;
        if (this.creative) {
            accessor.accessor$setDamageAllowedInCreativeMode();
        }
        if (this.scales) {
            damageSource.func_76351_m();
        }
        if (this.magical) {
            damageSource.func_82726_p();
        }
        if (this.bypasses) {
            accessor.accessor$setDamageBypassesArmor();
        }
        if (this.absolute) {
            accessor.accessor$setDamageIsAbsolute();
        }
        if (this.explosion) {
            damageSource.func_94540_d();
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
        this.blockData = value.getFallingBlockData();
        return this;
    }

    @Override
    public SpongeFallingBlockDamgeSourceBuilder reset() {
        super.reset();
        this.reference = null;
        this.blockData = null;
        return this;
    }
}
