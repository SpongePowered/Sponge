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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSourceBuilder;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.mixin.core.util.DamageSourceAccessor;

public class SpongeBlockDamageSourceBuilder extends AbstractDamageSourceBuilder<BlockDamageSource, BlockDamageSource.Builder> implements BlockDamageSource.Builder {

    private Location<World> location;
    private BlockSnapshot blockSnapshot;

    @Override
    public BlockDamageSource.Builder block(final Location<World> location) {
        this.location = location;
        return this;
    }

    @Override
    public BlockDamageSource.Builder block(final BlockSnapshot blockState) {
        this.blockSnapshot = checkNotNull(blockState);
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public BlockDamageSource build() throws IllegalStateException {
        checkState(this.location != null);
        checkState(this.blockSnapshot != null);
        checkState(this.damageType != null);
        final MinecraftBlockDamageSource damageSource = new MinecraftBlockDamageSource(this.damageType.getId(), this.location);
        final DamageSourceAccessor accessor = (DamageSourceAccessor) damageSource;
        if (this.absolute) {
            accessor.accessor$setDamageIsAbsolute();
        }
        if (this.bypasses) {
            accessor.accessor$setDamageBypassesArmor();
        }
        if (this.scales) {
            damageSource.setDifficultyScaled();
        }
        if (this.explosion) {
            damageSource.setExplosion();
        }
        if (this.magical) {
            damageSource.setMagicDamage();
        }
        if (this.creative) {
            accessor.accessor$setDamageAllowedInCreativeMode();
        }
        if (this.exhaustion != null) {
            accessor.accessor$setHungerDamage(this.exhaustion.floatValue());
        }
        return (BlockDamageSource) damageSource;
    }

    @Override
    public BlockDamageSource.Builder reset() {
        super.reset();
        this.location = null;
        this.blockSnapshot = null;
        return this;
    }
}
