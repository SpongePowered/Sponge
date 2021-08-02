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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.util.Angle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.matrix.Matrix4d;

@Mixin(Rotation.class)
public abstract class RotationMixin_API implements org.spongepowered.api.util.rotation.Rotation {

    // @formatter:off
    @Shadow public abstract Rotation shadow$getRotated(Rotation rotation);
    // @formatter:on

    private @Nullable Angle impl$angle = null;
    private @Nullable Matrix4d impl$rotationMatrix = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    public org.spongepowered.api.util.rotation.Rotation and(final org.spongepowered.api.util.rotation.Rotation rotation) {
        return (org.spongepowered.api.util.rotation.Rotation) (Object) this.shadow$getRotated((Rotation) (Object) rotation);
    }

    @Override
    public Angle angle() {
        this.lazyInit();
        return this.impl$angle;
    }

    @Override
    public Matrix4d toRotationMatrix() {
        this.lazyInit();
        return this.impl$rotationMatrix;
    }

    private Matrix4d createRotationMatrix(final int cos, final int sin) {
        return Matrix4d.from(
                cos, 0, sin, 0,
                0, 1, 0, 0,
                -sin, 0, cos, 0,
                0, 0, 0, 1
        );
    }

    @SuppressWarnings({"ConstantConditions", "RedundantCast"})
    private void lazyInit() {
        if (this.impl$angle == null) {
            if ((Rotation) (Object) this == Rotation.CLOCKWISE_90) {
                this.impl$angle = Angle.fromDegrees(90);
                // we have to reverse the sine as Mojang co-ords are backwards on the Z axis - (+,-) and (-,+) quadrants are swapped.
                this.impl$rotationMatrix = this.createRotationMatrix(0, -1);
            } else if ((Rotation) (Object) this == Rotation.CLOCKWISE_180) {
                this.impl$angle = Angle.fromDegrees(180);
                this.impl$rotationMatrix = this.createRotationMatrix(-1, 0);
            } else if ((Rotation) (Object) this == Rotation.COUNTERCLOCKWISE_90) {
                this.impl$angle = Angle.fromDegrees(270);
                this.impl$rotationMatrix = this.createRotationMatrix(0, 1);
            } else {
                this.impl$angle = Angle.fromDegrees(0); // Either NONE or someone has tried to extend the enum for some reason.
                this.impl$rotationMatrix = this.createRotationMatrix(1, 0);
            }
        }
    }


}
