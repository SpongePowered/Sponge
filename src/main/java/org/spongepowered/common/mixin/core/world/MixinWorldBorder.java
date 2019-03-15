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
package org.spongepowered.common.mixin.core.world;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.util.TemporalUnits;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.time.Duration;

@NonnullByDefault
@Mixin(net.minecraft.world.border.WorldBorder.class)
@Implements(@Interface(iface = WorldBorder.class, prefix = "border$"))
public abstract class MixinWorldBorder implements WorldBorder {

    @Shadow public abstract double getCenterX();
    @Shadow public abstract double getCenterZ();
    @Shadow public abstract double getTargetSize();
    @Shadow public abstract void setTransition(double newSize);
    @Shadow public abstract void setTransition(double oldSize, double newSize, long time);
    @Shadow public abstract long getTimeUntilTarget();
    @Shadow public abstract double getDamageBuffer();
    @Shadow public abstract void setDamageBuffer(double buffer);
    @Shadow public abstract void shadow$setCenter(double x, double z);
    @Shadow public abstract int shadow$getWarningTime();
    @Shadow public abstract void shadow$setWarningTime(int time);
    @Shadow public abstract int shadow$getWarningDistance();
    @Shadow public abstract void shadow$setWarningDistance(int distance);
    @Shadow public abstract double shadow$getDiameter();

    @Intrinsic
    public Duration border$getWarningTime() {
        return Duration.of(shadow$getWarningTime(), TemporalUnits.MINECRAFT_TICKS);
    }

    @Override
    public double getNewDiameter() {
        return getTargetSize();
    }

    @Intrinsic
    public double border$getDiameter() {
        return shadow$getDiameter();
    }

    @Override
    public void setDiameter(double diameter) {
        setTransition(diameter);
    }

    @Override
    public void setDiameter(double diameter, Duration time) {
        setTransition(getDiameter(), diameter, time.get(TemporalUnits.MINECRAFT_TICKS));
    }

    @Override
    public void setDiameter(double startDiameter, double endDiameter, Duration time) {
        setTransition(startDiameter, endDiameter, time.getNano() % 50);
    }

    @Override
    public Duration getTimeRemaining() {
        return null;// todo - implement
    }

    @Override
    public Vector3d getCenter() {
        return new Vector3d(getCenterX(), 0, getCenterZ());
    }

    @Intrinsic
    public void border$setCenter(double x, double z) {
        this.shadow$setCenter(x, z);
    }

    @Override
    public double getDamageThreshold() {
        return getDamageBuffer();
    }

    @Override
    public void setDamageThreshold(double distance) {
        setDamageBuffer(distance);
    }

    @Intrinsic
    public double border$getDamageAmount() {
        return shadow$getDamageAmount();
    }

    @Intrinsic
    public void border$setDamageAmount(double damage) {
        shadow$setDamageAmount(damage);
    }

}
