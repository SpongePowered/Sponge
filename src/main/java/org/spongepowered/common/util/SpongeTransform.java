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
package org.spongepowered.common.util;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.util.Transform;
import org.spongepowered.math.imaginary.Quaterniond;
import org.spongepowered.math.matrix.Matrix4d;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Objects;

public final class SpongeTransform implements Transform {

    private final Vector3d position;
    private final Vector3d rotation;
    private final Vector3d scale;
    @Nullable private Quaterniond rotationQuaternion = null;

    public SpongeTransform(final Vector3d position, final Vector3d rotation, final Vector3d scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    @Override
    public Transform withPosition(final Vector3d position) {
        Objects.requireNonNull(position);

        return new SpongeTransform(position, this.rotation, this.scale);
    }

    @Override
    public Vector3d getRotation() {
        return this.rotation;
    }

    @Override
    public Transform withRotation(final Vector3d rotation) {
        Objects.requireNonNull(rotation);

        return new SpongeTransform(this.position, rotation, this.scale);
    }

    @Override
    public Transform withRotation(final Quaterniond rotation) {
        Objects.requireNonNull(rotation);

        return this.withRotation(toAxesAngles(rotation));
    }

    @Override
    public Quaterniond getRotationAsQuaternion() {
        if (this.rotationQuaternion == null) {
            this.rotationQuaternion = fromAxesAngles(this.rotation);
        }
        return this.rotationQuaternion;
    }

    @Override
    public double getPitch() {
        return this.rotation.getX();
    }

    @Override
    public double getYaw() {
        return this.rotation.getY();
    }

    @Override
    public double getRoll() {
        return this.rotation.getZ();
    }

    @Override
    public Vector3d getScale() {
        return this.scale;
    }

    @Override
    public Transform withScale(final Vector3d scale) {
        Objects.requireNonNull(scale);

        return new SpongeTransform(this.position, this.rotation, scale);
    }

    @Override
    public Transform add(final Transform other) {
        Objects.requireNonNull(other);

        return new SpongeTransform(
                this.position.add(other.getPosition()),
                toAxesAngles(this.getRotationAsQuaternion().mul(other.getRotationAsQuaternion())),
                this.scale.add(other.getScale())
        );
    }

    @Override
    public Transform translate(final Vector3d translation) {
        Objects.requireNonNull(translation);

        return new SpongeTransform(
                this.position.add(translation),
                this.rotation,
                this.scale
        );
    }

    @Override
    public Transform rotate(final Vector3d rotation) {
        Objects.requireNonNull(rotation);

        return this.rotate(fromAxesAngles(rotation));
    }

    @Override
    public Transform rotate(final Quaterniond rotation) {
        Objects.requireNonNull(rotation);

        return new SpongeTransform(
                this.position,
                toAxesAngles(this.getRotationAsQuaternion().mul(rotation)),
                this.scale
        );
    }

    @Override
    public Transform scale(final Vector3d scale) {
        Objects.requireNonNull(scale);

        return new SpongeTransform(
                this.position,
                this.rotation,
                this.scale.mul(scale)
        );
    }

    @Override
    public Matrix4d toMatrix() {
        return Matrix4d.createScaling(this.scale.toVector4(1))
                .rotate(this.getRotationAsQuaternion())
                .translate(this.position);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SpongeTransform)) return false;

        final SpongeTransform that = (SpongeTransform) obj;
        return Objects.equals(this.position, that.position) &&
                Objects.equals(this.rotation, that.rotation) &&
                Objects.equals(this.scale, that.scale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.position, this.rotation, this.scale);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("position", this.position)
                .add("rotation", this.rotation)
                .add("scale", this.scale)
                .toString();
    }

    private static Vector3d toAxesAngles(final Quaterniond quaternion) {
        final Vector3d axesAngles = quaternion.getAxesAnglesDeg();
        return new Vector3d(axesAngles.getX(), -axesAngles.getY(), axesAngles.getZ());
    }

    private static Quaterniond fromAxesAngles(final Vector3d angles) {
        return Quaterniond.fromAxesAnglesDeg(angles.getX(), -angles.getY(), angles.getZ());
    }

    public static final class Factory implements Transform.Factory {

        @Override
        public Transform create(final Vector3d position, final Vector3d rotation, final Vector3d scale) {
            Objects.requireNonNull(position);
            Objects.requireNonNull(rotation);
            Objects.requireNonNull(scale);

            return new SpongeTransform(position, rotation, scale);
        }
    }
}
