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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSourceBuilder;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class SpongeBlockDamageSourceBuilder implements BlockDamageSourceBuilder {

    private boolean creative;
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
    public BlockDamageSourceBuilder scalesWithDifficulty() {
        this.scales = true;
        return this;
    }

    @Override
    public BlockDamageSourceBuilder bypassesArmor() {
        this.armor = true;
        return this;
    }

    @Override
    public BlockDamageSourceBuilder explosion() {
        this.explosion = true;
        return this;
    }

    @Override
    public BlockDamageSourceBuilder absolute() {
        this.absolute = true;
        return this;
    }

    @Override
    public BlockDamageSourceBuilder magical() {
        this.magical = true;
        return this;
    }

    @Override
    public BlockDamageSourceBuilder type(DamageType damageType) {
        this.damageType = checkNotNull(damageType);
        return this;
    }

    @Override
    public BlockDamageSourceBuilder block(Location<World> location) {
        this.location = location;
        return this;
    }

    @Override
    public BlockDamageSourceBuilder block(BlockSnapshot blockState) {
        this.blockSnapshot = checkNotNull(blockState);
        return this;
    }

    @Override
    public BlockDamageSource build() throws IllegalStateException {
        checkState(this.location != null);
        checkState(this.blockSnapshot != null);
        checkState(this.damageType != null);
        return new SpongeBlockDamageSource(this.location,
                                           this.blockSnapshot,
                                           this.damageType,
                                           this.absolute,
                                           this.armor,
                                           this.scales,
                                           this.explosion,
                                           this.magical);
    }
}
