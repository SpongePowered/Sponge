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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class SpongeBlockDamageSourceBuilder implements BlockDamageSource.Builder {

    private boolean scales;
    private boolean armor;
    private boolean explosion;
    private boolean absolute;
    private boolean magical;
    private DamageType damageType;
    private Location<World> location;
    private BlockSnapshot blockSnapshot;

    public SpongeBlockDamageSourceBuilder() {
    }


    @Override
    public BlockDamageSource.Builder scalesWithDifficulty() {
        this.scales = true;
        return this;
    }

    @Override
    public BlockDamageSource.Builder bypassesArmor() {
        this.armor = true;
        return this;
    }

    @Override
    public BlockDamageSource.Builder explosion() {
        this.explosion = true;
        return this;
    }

    @Override
    public BlockDamageSource.Builder absolute() {
        this.absolute = true;
        return this;
    }

    @Override
    public BlockDamageSource.Builder magical() {
        this.magical = true;
        return this;
    }

    @Override
    public BlockDamageSource.Builder type(DamageType damageType) {
        this.damageType = checkNotNull(damageType);
        return this;
    }

    @Override
    public BlockDamageSource.Builder block(Location<World> location) {
        this.location = location;
        return this;
    }

    @Override
    public BlockDamageSource.Builder block(BlockSnapshot blockState) {
        this.blockSnapshot = checkNotNull(blockState);
        return this;
    }

    @Override
    public BlockDamageSource build() throws IllegalStateException {
        checkState(this.location != null);
        checkState(this.blockSnapshot != null);
        checkState(this.damageType != null);
        MinecraftBlockDamageSource damageSource = new MinecraftBlockDamageSource(this.damageType.getId(), this.location);
        if (this.absolute) {
            damageSource.setDamageIsAbsolute();
        } else if (this.armor) {
            damageSource.setDamageBypassesArmor();
        } else if (this.scales) {
            damageSource.setDifficultyScaled();
        } else if (this.explosion) {
            damageSource.setExplosion();
        } else if (this.magical) {
            damageSource.setMagicDamage();
        }
        return (BlockDamageSource) damageSource;
    }

    @Override
    public BlockDamageSource.Builder reset() {
        this.scales = false;
        this.armor = false;
        this.explosion = false;
        this.absolute = false;
        this.magical = false;
        this.damageType = null;
        this.location = null;
        this.blockSnapshot = null;
        return this;
    }
}
