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
package org.spongepowered.common.mixin.api.minecraft.world.entity;

import net.minecraft.world.entity.Display;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.display.DisplayEntity;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.util.SpongeTicks;

@Mixin(Display.class)
public abstract class DisplayMixin_API extends EntityMixin_API implements DisplayEntity {

    // @formatter:off
    @Shadow protected abstract void setPosRotInterpolationDuration(int $$0);
    @Shadow protected abstract int getPosRotInterpolationDuration();
    // @formatter:on

    @Override
    public Ticks bridge$getTeleportDuration() {
        return Ticks.of(getPosRotInterpolationDuration());
    }

    @Override
    public boolean bridge$setTeleportDuration(final Ticks duration) {
        if (duration.isInfinite()) {
            return false;
        }

        setPosRotInterpolationDuration(SpongeTicks.toSaturatedIntOrInfinite(duration));
        ((SpongeDataHolderBridge) this).bridge$offer(Keys.TELEPORT_DURATION, duration);
        return true;
    }
}
