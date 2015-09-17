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

import com.google.common.base.Objects;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SpongeBlockDamageSource implements BlockDamageSource {

    private final Location<World> location;
    private final BlockSnapshot blockSnapshot;
    private final DamageType damageType;
    private final boolean isAbsolute;
    private final boolean bypassesArmor;
    private final boolean difficultyScaled;
    private final boolean explosive;
    private final boolean magical;

    public SpongeBlockDamageSource(Location<World> location, BlockSnapshot blockState, DamageType damageType, boolean isAbsolute, boolean bypassesArmor,
                                   boolean difficultyScaled, boolean explosive, boolean magical) {
        this.location = location;
        this.blockSnapshot = blockState;
        this.damageType = damageType;
        this.isAbsolute = isAbsolute;
        this.bypassesArmor = bypassesArmor;
        this.difficultyScaled = difficultyScaled;
        this.explosive = explosive;
        this.magical = magical;
    }

    @Override
    public Location<World> getLocation() {
        return this.location;
    }

    @Override
    public BlockSnapshot getBlockSnapshot() {
        return this.blockSnapshot;
    }

    @Override
    public DamageType getDamageType() {
        return this.damageType;
    }

    @Override
    public boolean isAbsolute() {
        return this.isAbsolute;
    }

    @Override
    public boolean isBypassingArmor() {
        return this.bypassesArmor;
    }

    @Override
    public boolean isDifficultyScaled() {
        return this.difficultyScaled;
    }

    @Override
    public boolean isExplosion() {
        return this.explosive;
    }

    @Override
    public boolean isMagic() {
        return this.magical;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("location", this.location)
            .add("blockState", this.blockSnapshot)
            .add("damageType", this.damageType.getId())
            .add("isAbsolute", this.isAbsolute)
            .add("bypassesArmor", this.bypassesArmor)
            .add("difficultyScaled", this.difficultyScaled)
            .add("explosive", this.explosive)
            .add("magical", this.magical)
            .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.location,
                                this.blockSnapshot,
                                this.damageType,
                                this.isAbsolute,
                                this.bypassesArmor,
                                this.difficultyScaled,
                                this.explosive,
                                this.magical);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SpongeBlockDamageSource other = (SpongeBlockDamageSource) obj;
        return Objects.equal(this.location, other.location)
               && Objects.equal(this.blockSnapshot, other.blockSnapshot)
               && Objects.equal(this.damageType, other.damageType)
               && Objects.equal(this.isAbsolute, other.isAbsolute)
               && Objects.equal(this.bypassesArmor, other.bypassesArmor)
               && Objects.equal(this.difficultyScaled, other.difficultyScaled)
               && Objects.equal(this.explosive, other.explosive)
               && Objects.equal(this.magical, other.magical);
    }
}
