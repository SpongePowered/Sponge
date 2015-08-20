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
import net.minecraft.world.border.EnumBorderStatus;
import net.minecraft.world.border.IBorderListener;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@NonnullByDefault
@Mixin(net.minecraft.world.border.WorldBorder.class)
@Implements(@Interface(iface = WorldBorder.class, prefix = "border$"))
public abstract class MixinWorldBorder implements WorldBorder {

    @Shadow private int warningTime;
    @Shadow private int warningDistance;
    @Shadow private double startDiameter;
    @Shadow private double endDiameter;
    @Shadow private long endTime;
    @Shadow private long startTime;
    @Shadow private double damageAmount;


    @Shadow public abstract double getCenterX();
    @Shadow public abstract double getCenterZ();
    @Shadow public abstract double getTargetSize();
    @Shadow public abstract void setTransition(double newSize);
    @Shadow public abstract void setTransition(double oldSize, double newSize, long time);
    @Shadow public abstract long getTimeUntilTarget();
    @Shadow public abstract EnumBorderStatus getStatus();
    @Shadow public abstract double getDamageBuffer();
    @Shadow
    public abstract void setDamageBuffer(double buffer);
    @Shadow(prefix = "shadow$")
    public abstract double shadow$getDamageAmount();
    @Shadow(prefix = "shadow$")
    public abstract void shadow$setDamageAmount(double amount);
    @Shadow(prefix = "shadow$")
    public abstract int shadow$getWarningTime();
    @Shadow(prefix = "shadow$")
    public abstract void shadow$setWarningTime(int time);
    @Shadow(prefix = "shadow$")
    public abstract int shadow$getWarningDistance();
    @Shadow(prefix = "shadow$")
    public abstract void shadow$setWarningDistance(int distance);

    @SuppressWarnings("rawtypes")
    @Shadow
    public abstract List getListeners();

    @Override
    public int getWarningTime() {
        return this.warningTime;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setWarningTime(int time) {
        this.warningTime = time;
        Iterator var2 = this.getListeners().iterator();

        while (var2.hasNext()) {
            IBorderListener var3 = (IBorderListener) var2.next();
            var3.onWarningTimeChanged((net.minecraft.world.border.WorldBorder) ((Object) this), this.warningTime);
        }
    }

    @Override
    public int getWarningDistance() {
        return this.warningDistance;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setWarningDistance(int distance) {
        this.warningDistance = distance;
        Iterator var2 = this.getListeners().iterator();

        while (var2.hasNext()) {
            IBorderListener var3 = (IBorderListener) var2.next();
            var3.onWarningDistanceChanged((net.minecraft.world.border.WorldBorder) ((Object) this), this.warningDistance);
        }
    }

    @Override
    public double getNewDiameter() {
        return getTargetSize();
    }

    @Override
    public double getDiameter() {
        if (this.getStatus() != EnumBorderStatus.STATIONARY) {
            double time = (float) (System.currentTimeMillis() - this.startTime) / (float) (this.endTime - this.startTime);

            if (time < 1.0D) {
                return (this.startDiameter + (this.endDiameter - this.startDiameter) * time);
            }

            this.setTransition(this.endDiameter);
        }

        return this.startDiameter;
    }

    @Override
    public void setDiameter(double diameter) {
        setTransition(diameter);
    }

    @Override
    public void setDiameter(double diameter, long time) {
        setTransition(getDiameter(), diameter, time);
    }

    @Override
    public void setDiameter(double startDiameter, double endDiameter, long time) {
        setTransition(startDiameter, endDiameter, time);
    }

    @Override
    public long getTimeRemaining() {
        return getTimeUntilTarget();
    }

    @Override
    public Vector3d getCenter() {
        return new Vector3d(getCenterX(), 0, getCenterZ());
    }

    @Override
    public double getDamageThreshold() {
        return getDamageBuffer();
    }

    @Override
    public void setDamageThreshold(double distance) {
        setDamageBuffer(distance);
    }

    public double border$getDamageAmount() {
        return this.damageAmount;
    }

    public void border$setDamageAmount(double damage) {
        this.damageAmount = damage;
    }
}
