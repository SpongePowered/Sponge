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
package org.spongepowered.common.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.matrix.Matrix4d;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import javax.annotation.Nullable;

@NonnullByDefault
public class SpongeTransform<E extends Extent> implements Transform<E> {

    private final E extent;
    private final Vector3d position;
    private final Vector3d rotation;
    private final Vector3d scale;
    @Nullable private Location<E> location = null;
    @Nullable private Quaterniond rotationQuaternion = null;

    public SpongeTransform(Location<E> location) {
        this(location.getExtent(), location.getPosition());
    }

    public SpongeTransform(E extent) {
        this(extent, Vector3d.ZERO);
    }

    public SpongeTransform(E extent, Vector3d position) {
        this(extent, position, Vector3d.ZERO);
    }

    public SpongeTransform(E extent, Vector3d position, Vector3d rotation) {
        this(extent, position, rotation, Vector3d.ONE);
    }

    public SpongeTransform(Location<E> location, Vector3d rotation, Vector3d scale) {
        this(location.getExtent(), location.getPosition(), rotation, scale);
    }

    public SpongeTransform(E extent, Vector3d position, Vector3d rotation, Vector3d scale) {
        this.extent = checkNotNull(extent, "extent");
        this.position = checkNotNull(position, "position");
        this.rotation = checkNotNull(rotation, "rotation");
        this.scale = checkNotNull(scale, "scale");
    }

    @Override
    public Location<E> getLocation() {
        if (this.location == null) {
            this.location = new Location<E>(this.extent, this.position);
        }
        return this.location;
    }

    @Override
    public E getExtent() {
        return this.extent;
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    @Override
    public Vector3d getRotation() {
        return this.rotation;
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
    public Transform<E> setLocation(Location<E> location) {
        checkNotNull(location, "location");
        return new SpongeTransform<E>(location, getRotation(), getScale());
    }

    @Override
    public Transform<E> setExtent(E extent) {
        checkNotNull(extent, "extent");
        return new SpongeTransform<E>(extent, getPosition(), getRotation(), getScale());
    }

    @Override
    public Transform<E> setPosition(Vector3d position) {
        checkNotNull(position, "position");
        return new SpongeTransform<E>(getExtent(), position, getRotation(), getScale());
    }

    @Override
    public Transform<E> setRotation(Quaterniond rotation) {
        checkNotNull(rotation, "rotation");
        return setRotation(toAxesAngles(rotation));
    }

    @Override
    public Transform<E> setRotation(Vector3d rotation) {
        checkNotNull(rotation, "rotation");
        return new SpongeTransform<E>(getExtent(), getPosition(), rotation, getScale());
    }

    @Override
    public Transform<E> setScale(Vector3d scale) {
        checkNotNull(scale, "scale");
        return new SpongeTransform<E>(getExtent(), getPosition(), getRotation(), scale);
    }

    @Override
    public Transform<E> add(Transform<E> other) {
        checkNotNull(other, "other");
        return new SpongeTransform<E>(
            getExtent(),
            getPosition().add(other.getPosition()),
            toAxesAngles(other.getRotationAsQuaternion().mul(getRotationAsQuaternion())),
            getScale().mul(other.getScale())
        );
    }

    @Override
    public Transform<E> addTranslation(Vector3d translation) {
        checkNotNull(translation, "translation");
        return new SpongeTransform<E>(getExtent(), getPosition().add(translation));
    }

    @Override
    public Transform<E> addRotation(Vector3d rotation) {
        checkNotNull(rotation, "rotation");
        return addRotation(fromAxesAngles(rotation));
    }

    @Override
    public Transform<E> addRotation(Quaterniond rotation) {
        checkNotNull(rotation, "rotation");
        return new SpongeTransform<E>(getExtent(), getPosition(), toAxesAngles(rotation.mul(getRotationAsQuaternion())), getScale());
    }

    @Override
    public Transform<E> addScale(Vector3d scale) {
        checkNotNull(scale, "scale");
        return new SpongeTransform<E>(getExtent(), getPosition(), getRotation(), getScale().mul(scale));
    }

    @Override
    public Matrix4d toMatrix() {
        return Matrix4d.createScaling(getScale().toVector4(1)).rotate(getRotationAsQuaternion()).translate(getPosition());
    }

    @Override
    public boolean isValid() {
        return this.extent.isLoaded();
    }

    @Override
    public String toString() {
        return "Transform{location=" + getLocation() + ", rotation=" + getRotation() + ", scale=" + getScale() + '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SpongeTransform<?>)) {
            return false;
        }
        final SpongeTransform<?> otherTransform = (SpongeTransform<?>) other;
        return otherTransform.extent.equals(this.extent) && otherTransform.getPosition().equals(getPosition())
            && otherTransform.getRotation().equals(getRotation()) && otherTransform.getScale().equals(getScale());
    }

    private static Vector3d toAxesAngles(Quaterniond quaternion) {
        final Vector3d axesAngles = quaternion.getAxesAnglesDeg();
        return new Vector3d(axesAngles.getX(), -axesAngles.getY(), axesAngles.getZ());
    }

    private static Quaterniond fromAxesAngles(Vector3d angles) {
        return Quaterniond.fromAxesAnglesDeg(angles.getX(), -angles.getY(), angles.getZ());
    }

    public static class SpongeBuilder<E extends Extent> implements Transform.Builder<E> {

        @Nullable private E extent = null;
        private Vector3d position = Vector3d.ZERO;
        private Vector3d rotation = Vector3d.ZERO;
        private Vector3d scale = Vector3d.ONE;

        @Override
        public Builder<E> extent(E extent) {
            this.extent = checkNotNull(extent, "extent");
            return this;
        }

        @Override
        public Builder<E> position(Vector3d position) {
            this.position = checkNotNull(position, "position");
            return this;
        }

        @Override
        public Builder<E> rotation(Vector3d rotation) {
            this.rotation = checkNotNull(rotation, "rotation");
            return this;
        }

        @Override
        public Builder<E> rotation(Quaterniond rotation) {
            return rotation(toAxesAngles(checkNotNull(rotation, "rotation")));
        }

        @Override
        public Builder<E> scale(Vector3d scale) {
            this.scale = checkNotNull(scale, "scale");
            return this;
        }

        @Override
        public Transform<E> build() {
            checkState(extent != null, "Extent hasn't been set");
            return new SpongeTransform<E>(extent, position, rotation, scale);
        }

    }

}
