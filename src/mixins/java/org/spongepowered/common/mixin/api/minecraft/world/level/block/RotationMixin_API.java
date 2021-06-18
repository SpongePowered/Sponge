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
package org.spongepowered.common.mixin.api.minecraft.world.level.block;

import net.minecraft.world.level.block.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Rotation.class)
public abstract class RotationMixin_API implements org.spongepowered.api.util.rotation.Rotation {

    // @formatter:off
    @Shadow public abstract Rotation shadow$getRotated(Rotation rotation);
    // @formatter:on

    @SuppressWarnings("ConstantConditions")
    @Override
    public org.spongepowered.api.util.rotation.Rotation and(final org.spongepowered.api.util.rotation.Rotation rotation) {
        return (org.spongepowered.api.util.rotation.Rotation) (Object) this.shadow$getRotated((Rotation) (Object) rotation);
    }

    @SuppressWarnings({"ConstantConditions", "RedundantCast"})
    @Override
    public int angle() {
        if ((Rotation) (Object) this == Rotation.NONE) {
            return 0;
        } else if ((Rotation) (Object) this == Rotation.CLOCKWISE_90) {
            return 90;
        } else if ((Rotation) (Object) this == Rotation.CLOCKWISE_180) {
            return 180;
        } else if ((Rotation) (Object) this == Rotation.COUNTERCLOCKWISE_90) {
            return 270;
        }
        return 0; // ???? who the hell adds a new rotation?
    }
}
