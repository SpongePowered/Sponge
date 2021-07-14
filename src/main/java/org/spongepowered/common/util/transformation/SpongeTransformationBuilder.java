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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.math.matrix.Matrix4d;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector4d;

import java.util.Objects;

public final class SpongeTransformationBuilder implements Transformation.Builder {

    // x, y, z, w
    private Vector3d origin;
    private Matrix4d transformation;
    private Matrix4d directionTransformation;
    private boolean performRounding;

    private @Nullable Rotation rotation;
    private boolean flipx;
    private boolean flipz;

    public SpongeTransformationBuilder() {
        this.reset();
    }

    @Override
    public @NonNull SpongeTransformationBuilder reset() {
        this.transformation = Matrix4d.IDENTITY;
        this.directionTransformation = Matrix4d.IDENTITY;
        this.origin = Vector3d.ZERO;
        this.performRounding = true;
        this.rotation = null;
        this.flipx = false;
        this.flipz = false;
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder origin(final @NonNull Vector3d origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder rotate(final @NonNull Rotation rotation) {
        final Matrix4d rotationMatrix = rotation.toRotationMatrix();
        this.transformation = rotationMatrix.mul(this.transformation);
        this.directionTransformation = rotationMatrix.mul(this.directionTransformation);
        if (this.rotation == null) {
            this.rotation = rotation;
        } else {
            this.rotation = this.rotation.and(rotation);
        }
        if ((this.flipx ^ this.flipz) && rotation.angle().degrees() % 180 == 90) {
            // flip the mirror if we only have one
            this.flipz = this.flipx;
            this.flipx = !this.flipx;
        }
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder mirror(final @NonNull Axis axis) {
        if (Objects.requireNonNull(axis) == Axis.Y) {
            throw new IllegalArgumentException("The Y direction cannot be mirrored.");
        }
        final Vector4d scale = Vector4d.ONE.sub(axis.toVector3d().toVector4(0).mul(2));
        this.transformation = this.transformation.scale(scale);
        this.directionTransformation = this.directionTransformation.scale(scale);

        // Flip the x or z mirror if that's the axis.
        this.flipx = this.flipx ^ axis == Axis.X;
        this.flipz = this.flipz ^ axis == Axis.Z;
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder translate(final @NonNull Vector3d translate) {
        this.transformation = this.transformation.translate(translate);
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder performRounding(final boolean round) {
        this.performRounding = round;
        return this;
    }

    @Override
    public @NonNull SpongeTransformation build() {
        final Matrix4d partialRotation = this.transformation.translate(this.origin);
        final Matrix4d invertedMatrix = Matrix4d.createTranslation(this.origin.mul(-1));
        final Matrix4d rotatedAroundOrigin = partialRotation.mul(invertedMatrix);
        final SpongeTransformation transformation = new SpongeTransformation(
            this.origin,
            rotatedAroundOrigin,
            this.directionTransformation,
            this.performRounding,
            this.rotation == null ? Rotations.NONE.get() : this.rotation,
            this.flipx,
            this.flipz
        );
        this.reset();
        return transformation;
    }

}
