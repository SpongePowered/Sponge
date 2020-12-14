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
package org.spongepowered.common.mixin.api.mcp.world.border;

import org.spongepowered.api.world.border.MutableWorldBorder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector2d;

import java.time.Duration;

@Mixin(net.minecraft.world.border.WorldBorder.class)
@Implements(@Interface(iface = MutableWorldBorder.class, prefix = "worldBorder$"))
public abstract class WorldBorderMixin_API implements MutableWorldBorder {

    //@formatter:off
    @Shadow public abstract double shadow$getCenterX();
    @Shadow public abstract double shadow$getCenterZ();
    @Shadow public abstract void shadow$setCenter(final double x, final double z);
    @Shadow public abstract double shadow$getSize();
    @Shadow public abstract void shadow$setSize(final double newSize);
    @Shadow public abstract void shadow$lerpSizeBetween(final double oldSize, final double newSize, final long time);
    @Shadow public abstract double shadow$getLerpTarget();
    @Shadow public abstract long shadow$getLerpRemainingTime();
    @Shadow public abstract double shadow$getDamageSafeZone();
    @Shadow public abstract void shadow$setDamageSafeZone(final double buffer);
    @Shadow public abstract double shadow$getDamagePerBlock();
    @Shadow public abstract void shadow$setDamagePerBlock(final double amount);
    @Shadow public abstract int shadow$getWarningTime();
    @Shadow public abstract void shadow$setWarningTime(final int time);
    @Shadow public abstract int shadow$getWarningBlocks();
    @Shadow public abstract void shadow$setWarningBlocks(final int distance);
    @Shadow public abstract net.minecraft.world.border.WorldBorder.Serializer shadow$createSettings();
    @Shadow public abstract void shadow$applySettings(final net.minecraft.world.border.WorldBorder.Serializer settings);
    //@formatter:on

    @Override
    public Vector2d getCenter() {
        return new Vector2d(this.shadow$getCenterX(), this.shadow$getCenterZ());
    }

    @Intrinsic
    @Override
    public void setCenter(final double x, final double z) {
        this.shadow$setCenter(x, z);
    }

    @Intrinsic
    @Override
    public double getSize() {
        return this.shadow$getSize();
    }

    @Intrinsic
    @Override
    public void setSize(final double size) {
        this.shadow$setSize(size);
    }

    @Override
    public void setSize(final double oldSize, final double newSize, final Duration duration) {
        this.shadow$lerpSizeBetween(oldSize, newSize, duration.toMillis());
    }

    @Override
    public double getLerpSizeTarget() {
        return this.shadow$getLerpTarget();
    }

    @Override
    public Duration getLerpSizeTimeRemaining() {
        return Duration.ofMillis(this.shadow$getLerpRemainingTime());
    }

    @Override
    public double getSafeZone() {
        return this.shadow$getDamageSafeZone();
    }

    @Override
    public void setSafeZone(final double safeZone) {
        this.shadow$setDamageSafeZone(safeZone);
    }

    @Intrinsic
    @Override
    public double getDamagePerBlock() {
        return this.shadow$getDamagePerBlock();
    }

    @Intrinsic
    @Override
    public void setDamagePerBlock(final double damagePerBlock) {
        this.shadow$setDamagePerBlock(damagePerBlock);
    }

    @Override
    public Duration getWarningTime() {
        return Duration.ofMillis(this.shadow$getWarningTime());
    }

    @Override
    public void setWarningTime(final Duration time) {
        this.shadow$setWarningTime((int) time.toMillis());
    }

    @Intrinsic
    @Override
    public int getWarningBlocks() {
        return this.shadow$getWarningBlocks();
    }

    @Intrinsic
    @Override
    public void setWarningBlocks(final int warningBlocks) {
        this.shadow$setWarningBlocks(warningBlocks);
    }

    @Override
    public Snapshot createSnapshot() {
        return (Snapshot) this.shadow$createSettings();
    }

    @Override
    public void applySnapshot(final Snapshot snapshot) {
        this.shadow$applySettings((net.minecraft.world.border.WorldBorder.Serializer) snapshot);
    }
}
