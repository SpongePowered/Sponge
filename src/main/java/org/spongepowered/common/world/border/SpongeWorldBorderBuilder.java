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
package org.spongepowered.common.world.border;

import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.math.vector.Vector3d;

import java.time.Duration;

public class SpongeWorldBorderBuilder implements WorldBorder.Builder {

    private double diameter;
    private Vector3d center = Vector3d.ZERO; //use a default value otherwise null is used
    private Duration warningTime;
    private double warningDistance;
    private double damageThreshold;
    private double damageAmount;

    @Override
    public WorldBorder.Builder from(final WorldBorder border) {
        this.diameter = border.getDiameter();
        this.center = border.getCenter();
        this.warningTime = border.getWarningTime();
        this.warningDistance = border.getWarningDistance();
        this.damageThreshold = border.getDamageThreshold();
        this.damageAmount = border.getDamageAmount();
        return this;
    }

    @Override
    public WorldBorder.Builder diameter(final double diameter) {
        this.diameter = diameter;
        return this;
    }

    @Override
    public WorldBorder.Builder center(final double x, final double z) {
        this.center = new Vector3d(x, 0, z);
        return this;
    }

    @Override
    public WorldBorder.Builder warningTime(final Duration time) {
        this.warningTime = time;
        return this;
    }

    @Override
    public WorldBorder.Builder warningDistance(final double distance) {
        this.warningDistance = distance;
        return this;
    }

    @Override
    public WorldBorder.Builder damageThreshold(final double distance) {
        this.damageThreshold = distance;
        return this;
    }

    @Override
    public WorldBorder.Builder damageAmount(final double damage) {
        this.damageAmount = damage;
        return this;
    }

    @Override
    public WorldBorder build() throws IllegalStateException {
        final net.minecraft.world.border.WorldBorder border = new net.minecraft.world.border.WorldBorder();
        border.setCenter(this.center.getX(), this.center.getZ());
        border.setDamagePerBlock(this.damageAmount);
        border.setDamageBuffer(this.damageThreshold);
        border.setTransition(this.diameter);
        border.setWarningDistance((int) this.warningDistance);
        border.setWarningTime((int) this.warningTime.getSeconds());
        return (WorldBorder) border;
    }

    @Override
    public WorldBorder.Builder reset() {
        this.center = Vector3d.ZERO;
        this.damageAmount = 0;
        this.damageThreshold = 0;
        this.diameter = 0;
        this.warningDistance = 0;
        this.warningTime = Duration.ZERO;
        return this;
    }
}
