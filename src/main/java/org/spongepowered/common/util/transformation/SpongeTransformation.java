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
package org.spongepowered.common.util.transformation;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.matrix.Matrix4d;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector4d;

import java.util.Objects;

public final class SpongeTransformation implements Transformation {

    final Vector3d origin;
    final Matrix4d transformation;
    final Matrix4d directionTransformation;
    final boolean performRounding;
    final Rotation rotation;
    final boolean flipx;
    final boolean flipz;

    public SpongeTransformation(final Vector3d origin, final Matrix4d transformation, final Matrix4d directionTransformation,
            final boolean performRounding, final Rotation rotation, final boolean flipx, final boolean flipz) {
        this.origin = origin;
        this.transformation = transformation;
        this.directionTransformation = directionTransformation;
        this.performRounding = performRounding;
        this.rotation = rotation;
        this.flipx = flipx;
        this.flipz = flipz;
    }

    @Override
    public boolean performsRounding() {
        return this.performRounding;
    }

    @Override
    public @NonNull Vector3d transformPosition(final @NonNull Vector3d original) {
        final Vector4d transformed = this.transformation.transform(original.toVector4(1));
        if (this.performRounding) {
            return new Vector3d(
                    GenericMath.round(transformed.x(), 14),
                    GenericMath.round(transformed.y(), 14),
                    GenericMath.round(transformed.z(), 14)
            );
        } else {
            return transformed.toVector3();
        }
    }

    @Override
    public @NonNull Vector3d transformDirection(final @NonNull Vector3d original) {
        final Vector4d transformed = this.directionTransformation.transform(original.normalize().toVector4(1));
        final Vector3d result;
        if (this.performRounding) {
            result = new Vector3d(
                    GenericMath.round(transformed.x(), 14),
                    GenericMath.round(transformed.y(), 14),
                    GenericMath.round(transformed.z(), 14)
            );
        } else {
            result = transformed.toVector3();
        }
        return result.normalize();
    }

    @Override
    public @NonNull Matrix4d positionTransformationMatrix() {
        return this.transformation;
    }

    @Override
    public @NonNull Matrix4d directionTransformationMatrix() {
        return this.directionTransformation;
    }

    @Override
    public @NonNull Vector3d origin() {
        return this.origin;
    }

    @Override
    public @NonNull Rotation rotation() {
        return this.rotation;
    }

    @Override
    public boolean mirror(final @NonNull Axis axis) {
        if (Objects.requireNonNull(axis, "axis") == Axis.X) {
            return this.flipx;
        } else if (axis == Axis.Z) {
            return this.flipz;
        }
        return false;
    }

    @Override
    public boolean initialMirror(final @NonNull Axis axis) {
        final boolean isRightAngle = this.rotation == Rotations.CLOCKWISE_90.get() || this.rotation == Rotations.COUNTERCLOCKWISE_90.get();
        if (isRightAngle) {
            if (Objects.requireNonNull(axis, "axis") == Axis.X) {
                return this.flipz;
            } else if (axis == Axis.Z) {
                return this.flipx;
            }
            return false;
        } else {
            return this.mirror(axis);
        }
    }

    @Override
    public @NonNull Transformation inverse() {
        final Rotation inverseRotation;
        final boolean flipMirror;
        if (this.rotation == Rotations.CLOCKWISE_90.get()) {
            inverseRotation = Rotations.COUNTERCLOCKWISE_90.get();
            flipMirror = true;
        } else if (this.rotation == Rotations.COUNTERCLOCKWISE_90.get()) {
            inverseRotation = Rotations.CLOCKWISE_90.get();
            flipMirror = true;
        } else {
            inverseRotation = this.rotation;
            flipMirror = false;
        }

        final boolean inverseFlipX;
        final boolean inverseFlipZ;
        if (flipMirror && (this.flipz != this.flipx)) {
            inverseFlipX = this.flipz;
            inverseFlipZ = this.flipx;
        } else {
            inverseFlipX = this.flipx;
            inverseFlipZ = this.flipz;
        }
        return new SpongeTransformation(
                this.origin,
                this.transformation.invert(),
                this.directionTransformation.invert(),
                this.performRounding,
                inverseRotation,
                inverseFlipX,
                inverseFlipZ
        );
    }

    @Override
    public Builder toBuilder() {
        return new SpongeTransformationBuilder(this);
    }

}
