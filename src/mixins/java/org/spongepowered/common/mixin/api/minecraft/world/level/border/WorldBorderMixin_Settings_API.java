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
package org.spongepowered.common.mixin.api.minecraft.world.level.border;

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.math.vector.Vector2d;


@Mixin(WorldBorder.Settings.class)
@Implements(@Interface(iface = org.spongepowered.api.world.border.WorldBorder.class, prefix = "api$"))
public abstract class WorldBorderMixin_Settings_API implements org.spongepowered.api.world.border.WorldBorder {

    //@formatter:off
    @Shadow public abstract double shadow$centerX();
    @Shadow public abstract double shadow$centerZ();
    @Shadow public abstract double shadow$damagePerBlock();
    @Shadow public abstract double shadow$safeZone();
    @Shadow public abstract int shadow$warningBlocks();
    @Shadow public abstract int shadow$warningTime();
    @Shadow public abstract double shadow$size();
    @Shadow public abstract long shadow$lerpTime();
    @Shadow public abstract double shadow$lerpTarget();
    //@formatter:on

    @Override
    public Vector2d center() {
        return new Vector2d(this.shadow$centerX(), this.shadow$centerZ());
    }

    @Override
    public double targetDiameter() {
        return this.shadow$lerpTarget();
    }

    @Override
    public Ticks timeUntilTargetDiameter() {
        return SpongeTicks.ticksOrInfinite(this.shadow$lerpTime());
    }

    @Override
    public double diameter() {
        return this.shadow$size();
    }

    @Intrinsic
    public double api$safeZone() {
        return this.shadow$safeZone();
    }

    @Intrinsic
    public double api$damagePerBlock() {
        return this.shadow$damagePerBlock();
    }

    @Intrinsic
    public Ticks api$warningTime() {
        return SpongeTicks.ticksOrInfinite(this.shadow$warningTime());
    }

    @Override
    public int warningDistance() {
        return this.shadow$warningBlocks();
    }
}
