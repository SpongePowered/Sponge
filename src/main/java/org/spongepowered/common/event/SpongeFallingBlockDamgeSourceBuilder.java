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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;

import java.lang.ref.WeakReference;

public class SpongeFallingBlockDamgeSourceBuilder extends SpongeEntityDamageSourceBuilder implements FallingBlockDamageSource.Builder {

    private ImmutableFallingBlockData blockData = null;

    @Override
    public FallingBlockDamageSource.Builder scalesWithDifficulty() {
        super.scalesWithDifficulty();
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder bypassesArmor() {
        super.bypassesArmor();
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder explosion() {
        super.explosion();
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder absolute() {
        super.absolute();
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder magical() {
        super.magical();
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder creative() {
        super.creative();
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder type(DamageType damageType) {
        super.type(damageType);
        return this;
    }

    @Override
    public FallingBlockDamageSource.Builder fallingBlock(ImmutableFallingBlockData fallingBlock) {
        this.blockData = fallingBlock;
        return null;
    }

    @Override
    public FallingBlockDamageSource.Builder entity(Entity entity) {
        checkArgument(entity instanceof FallingBlock);
        this.reference = new WeakReference<>(entity);
        return this;
    }

    @Override
    public FallingBlockDamageSource build() throws IllegalStateException {
        checkState(this.reference.get() != null);
        checkState(this.blockData != null);
        checkState(this.damageType != null);
        MinecraftFallingBlockDamageSource damageSource =
            new MinecraftFallingBlockDamageSource(this.damageType.getId(),
                (EntityFallingBlock) this.reference.get(),
                this.blockData);
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
        return (FallingBlockDamageSource) damageSource;
    }

    @Override
    public FallingBlockDamageSource.Builder reset() {
        super.reset();
        this.reference = null;
        this.blockData = null;
        return this;
    }
}
