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
package org.spongepowered.common.mixin.api.mcp.world.level.border;

import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3d;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Mixin(net.minecraft.world.level.border.WorldBorder.class)
@Implements(@Interface(iface = WorldBorder.class, prefix = "worldBorder$"))
public abstract class WorldBorderMixin_API implements WorldBorder {

    //@formatter:off
    @Shadow public abstract double getCenterX();
    @Shadow public abstract double getCenterZ();
    @Shadow public abstract double shadow$getLerpTarget();
    @Shadow public abstract void shadow$setSize(double newSize);
    @Shadow public abstract void shadow$lerpSizeBetween(double oldSize, double newSize, long time);
    @Shadow public abstract long shadow$getLerpRemainingTime();
    @Shadow public abstract double shadow$getDamageSafeZone();
    @Shadow public abstract void shadow$setDamageSafeZone(double buffer);
    @Shadow public abstract void shadow$setCenter(double x, double z);
    @Shadow public abstract double shadow$getDamagePerBlock();
    @Shadow public abstract void shadow$setDamagePerBlock(double amount);
    @Shadow public abstract int shadow$getWarningTime();
    @Shadow public abstract void shadow$setWarningTime(int time);
    @Shadow public abstract int shadow$getWarningBlocks();
    @Shadow public abstract void shadow$setWarningBlocks(int distance);
    @Shadow public abstract double shadow$getSize();
    //@formatter:on

    @Override
    public Duration warningTime() {
        return Duration.of(this.shadow$getWarningTime(), ChronoUnit.MILLIS);
    }

    @Intrinsic
    public void worldBorder$setWarningTime(final Duration time) {
        this.shadow$setWarningTime((int) time.toMillis());
    }

    @Override
    public double warningDistance() {
        return this.shadow$getWarningBlocks();
    }

    @Override
    public void setWarningDistance(final double distance) {
        this.shadow$setWarningBlocks((int) distance);
    }

    @Override
    public double newDiameter() {
        return this.shadow$getLerpTarget();
    }

    @Override
    public double diameter() {
        return this.shadow$getSize();
    }

    @Override
    public void setDiameter(final double diameter) {
        this.shadow$setSize(diameter);
    }

    @Override
    public void setDiameter(final double diameter, final Duration time) {
        this.shadow$lerpSizeBetween(this.diameter(), diameter, time.toMillis());
    }

    @Override
    public void setDiameter(final double startDiameter, final double endDiameter, final Duration time) {
        this.shadow$lerpSizeBetween(startDiameter, endDiameter, time.toMillis());
    }

    @Override
    public Duration timeRemaining() {
        return Duration.of(this.shadow$getLerpRemainingTime(), ChronoUnit.MILLIS);
    }

    @Override
    public Vector3d center() {
        return new Vector3d(this.getCenterX(), 0, this.getCenterZ());
    }

    @Intrinsic
    public void worldBorder$setCenter(final double x, final double z) {
        this.shadow$setCenter(x, z);
    }

    @Override
    public double damageThreshold() {
        return this.shadow$getDamageSafeZone();
    }

    @Override
    public void setDamageThreshold(final double distance) {
        this.shadow$setDamageSafeZone(distance);
    }

    @Override
    public double damageAmount() {
        return this.shadow$getDamagePerBlock();
    }

    @Override
    public void setDamageAmount(final double damage) {
        this.shadow$setDamagePerBlock(damage);
    }

}
