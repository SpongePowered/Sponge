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

import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.common.accessor.world.level.border.WorldBorder_SettingsAccessor;
import org.spongepowered.common.bridge.world.level.border.WorldBorderBridge;
import org.spongepowered.math.vector.Vector2d;

import java.time.Duration;

public final class SpongeWorldBorderBuilder implements WorldBorder.Builder {

    private double diameter = -1;
    private double initialDiameter = -1;
    private Duration time = Duration.ZERO;
    private Vector2d center = Vector2d.ZERO; //use a default value otherwise null is used
    private Duration warningTime = Duration.ZERO;
    private double warningDistance;
    private double safeZone;
    private double damagePerBlock;

    public WorldBorder.Builder from(final WorldBorderBridge border) {
        return this.from(border.bridge$asImmutable());
    }

    @Override
    public WorldBorder.Builder from(final WorldBorder border) {
        this.diameter = border.targetDiameter();
        this.initialDiameter = border.diameter();
        this.time = border.timeUntilTargetDiameter();
        this.center = border.center();
        this.warningTime = border.warningTime();
        this.warningDistance = border.warningDistance();
        this.safeZone = border.safeZone();
        this.damagePerBlock = border.damagePerBlock();
        return this;
    }

    @Override
    public WorldBorder.Builder overworldDefaults() {
        return this.from((WorldBorder) net.minecraft.world.level.border.WorldBorder.DEFAULT_SETTINGS);
    }

    @Override
    public WorldBorder.Builder targetDiameter(final double diameter) {
        if (diameter <= 0) {
            throw new IllegalArgumentException("diameter cannot be non-positive");
        }
        this.diameter = diameter;
        if (this.initialDiameter == -1) {
            this.initialDiameter = diameter;
        }
        return this;
    }

    @Override
    public WorldBorder.Builder timeToTargetDiameter(final Duration time) {
        if (time.isNegative()) {
            throw new IllegalArgumentException("time cannot be negative");
        }
        this.time = time;
        return this;
    }

    @Override
    public WorldBorder.Builder center(final double x, final double z) {
        this.center = new Vector2d(x, z);
        return this;
    }

    @Override
    public WorldBorder.Builder initialDiameter(final double initialDiameter) {
        if (initialDiameter <= 0) {
            throw new IllegalArgumentException("diameter cannot be non-positive");
        }
        this.initialDiameter = initialDiameter;
        if (this.diameter == -1) {
            this.diameter = initialDiameter;
        }
        return this;
    }

    @Override
    public WorldBorder.Builder safeZone(final double safeZone) {
        if (safeZone < 0) {
            throw new IllegalArgumentException("damagePerBlock cannot be negative");
        }
        this.safeZone = safeZone;
        return this;
    }

    @Override
    public WorldBorder.Builder damagePerBlock(final double damagePerBlock) {
        if (damagePerBlock <= 0) {
            throw new IllegalArgumentException("damagePerBlock cannot be non-positive");
        }
        this.damagePerBlock = damagePerBlock;
        return this;
    }

    @Override
    public WorldBorder.Builder warningTime(final Duration warningTime) {
        if (warningTime.isNegative()) {
            throw new IllegalArgumentException("warning time cannot be negative");
        }
        this.warningTime = warningTime;
        return this;
    }

    @Override
    public WorldBorder.Builder warningDistance(final double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("warning distance cannot be negative");
        }
        this.warningDistance = distance;
        return this;
    }


    @Override
    public WorldBorder build() throws IllegalStateException {
        if (this.diameter == -1) {
            throw new IllegalStateException("The diameter or initial diameter has not been set!");
        }
        return (WorldBorder) WorldBorder_SettingsAccessor.invoker$new(
                this.center.x(),
                this.center.y(),
                this.damagePerBlock,
                this.safeZone,
                (int) this.warningDistance,
                (int) this.warningTime.getSeconds(),
                this.initialDiameter == -1 ? this.diameter : this.initialDiameter,
                this.time.toMillis(),
                this.diameter
        );
    }

    @Override
    public WorldBorder.Builder reset() {
        this.center = Vector2d.ZERO;
        this.damagePerBlock = 0;
        this.safeZone = 0;
        this.diameter = -1;
        this.initialDiameter = -1;
        this.time = Duration.ZERO;
        this.warningDistance = 0;
        this.warningTime = Duration.ZERO;
        return this;
    }

}
