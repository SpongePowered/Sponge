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

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector2d;

import java.time.Duration;

@Mixin(WorldBorder.Settings.class)
public abstract class WorldBorderMixin_Settings_API implements org.spongepowered.api.world.border.WorldBorder {

    //@formatter:off
    @Shadow public abstract double shadow$getCenterX();
    @Shadow public abstract double shadow$getCenterZ();
    @Shadow public abstract double shadow$getDamagePerBlock();
    @Shadow public abstract double shadow$getSafeZone();
    @Shadow public abstract int shadow$getWarningBlocks();
    @Shadow public abstract int shadow$getWarningTime();
    @Shadow public abstract double shadow$getSize();
    @Shadow public abstract long shadow$getSizeLerpTime();
    @Shadow public abstract double shadow$getSizeLerpTarget();
    //@formatter:on

    @Override
    public Vector2d center() {
        return new Vector2d(this.shadow$getCenterX(), this.shadow$getCenterZ());
    }

    @Override
    public double targetDiameter() {
        return this.shadow$getSizeLerpTarget();
    }

    @Override
    public Duration timeUntilTargetDiameter() {
        return Duration.ofMillis(this.shadow$getSizeLerpTime());
    }

    @Override
    public double diameter() {
        return this.shadow$getSize();
    }

    @Override
    public double safeZone() {
        return this.shadow$getSafeZone();
    }

    @Override
    public double damagePerBlock() {
        return this.shadow$getDamagePerBlock();
    }

    @Override
    public Duration warningTime() {
        return Duration.ofMillis(this.shadow$getWarningTime());
    }

    @Override
    public int warningDistance() {
        return this.shadow$getWarningBlocks();
    }
}
