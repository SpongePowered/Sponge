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

import com.google.common.base.MoreObjects;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.bridge.util.DamageSourceBridge;

public final class MinecraftBlockDamageSource extends DamageSource implements BlockDamageSource {

    private final Location location;
    private final BlockSnapshot blockSnapshot;

    public MinecraftBlockDamageSource(String damageTypeIn, Location location) {
        super(damageTypeIn);
        this.blockSnapshot = location.createSnapshot();
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public BlockSnapshot getBlockSnapshot() {
        return this.blockSnapshot;
    }

    @Override
    public DamageType getType() {
        return ((DamageSourceBridge) (Object) this).bridge$getDamageType();
    }

    @Override
    public boolean isAbsolute() {
        return this.isDamageAbsolute();
    }

    @Override
    public boolean isBypassingArmor() {
        return this.isUnblockable();
    }

    @Override
    public boolean isScaledByDifficulty() {
        return this.isDifficultyScaled();
    }

    @Override
    public boolean isExplosive() {
        return this.isExplosion();
    }

    @Override
    public boolean isMagic() {
        return this.isMagicDamage();
    }

    @Override
    public boolean doesAffectCreative() {
        return this.canHarmInCreative();
    }

    @Override
    public double getExhaustion() {
        return this.getExhaustion();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("BlockDamageSource")
            .add("Name", this.damageType)
            .add("Type", this.getType().getKey())
            .add("BlockSnapshot", this.getBlockSnapshot())
            .add("Location", this.location)
            .toString();
    }
}
