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

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.matrix.Matrix4d;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import javax.annotation.Nullable;

@NonnullByDefault
public class SpongeTransform implements Transform {

    private Extent extent;
    private Vector3d position;
    private Vector3d rotation;
    private Vector3d scale;
    @Nullable
    private Location location = null;
    @Nullable
    private Quaterniond rotationQuaternion = null;

    public SpongeTransform(Extent extent, Vector3d position) {
        this(extent, position, Vector3d.ZERO, Vector3d.ONE);
    }

    public SpongeTransform(Extent extent, Vector3d position, Vector3d rotation, Vector3d scale) {
        this.extent = checkNotNull(extent, "extent");
        this.position = checkNotNull(position, "position");
        this.rotation = checkNotNull(rotation, "rotation");
        this.scale = checkNotNull(scale, "scale");
    }

    @Override
    public Location getLocation() {
        if (this.location == null) {
            this.location = new Location(this.extent, this.position);
        }
        return this.location;
    }

    @Override
    public Transform setLocation(Location location) {
        checkNotNull(location, "location");
        setExtent(location.getExtent());
        setPosition(location.getPosition());
        return this;
    }

    @Override
    public Extent getExtent() {
        return this.extent;
    }

    @Override
    public Transform setExtent(Extent extent) {
        checkNotNull(extent, "extent");
        this.extent = extent;
        this.location = null;
        return this;
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    @Override
    public Transform setPosition(Vector3d position) {
        checkNotNull(position, "position");
        this.position = position;
        this.location = null;
        return this;
    }

    @Override
    public Vector3d getRotation() {
        return this.rotation;
    }

    @Override
    public Transform setRotation(Vector3d rotation) {
        checkNotNull(rotation, "rotation");
        this.rotation = rotation;
        this.rotationQuaternion = null;
        return this;
    }

    @Override
    public Quaterniond getRotationAsQuaternion() {
        if (this.rotationQuaternion == null) {
            this.rotationQuaternion = Quaterniond.fromAxesAnglesDeg(this.rotation.getX(), -this.rotation.getY(), this.rotation.getZ());
        }
        return this.rotationQuaternion;
    }

    @Override
    public Transform setRotation(Quaterniond rotation) {
        checkNotNull(rotation, "rotation");
        final Vector3d axesAngles = rotation.getAxesAngleDeg();
        this.rotation = new Vector3d(axesAngles.getX(), -axesAngles.getY(), axesAngles.getZ());
        this.rotationQuaternion = rotation;
        return this;
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
    public Transform setScale(Vector3d scale) {
        checkNotNull(scale, "scale");
        this.scale = scale;
        return this;
    }

    @Override
    public Transform add(Transform other) {
        checkNotNull(other, "other");
        addTranslation(other.getPosition());
        addRotation(other.getRotationAsQuaternion());
        addScale(other.getScale());
        return this;
    }

    @Override
    public Transform addTranslation(Vector3d translation) {
        checkNotNull(translation, "translation");
        setPosition(getPosition().add(translation));
        return this;
    }

    @Override
    public Transform addRotation(Vector3d rotation) {
        checkNotNull(rotation, "rotation");
        return addRotation(Quaterniond.fromAxesAnglesDeg(rotation.getX(), -rotation.getY(), rotation.getZ()));
    }

    @Override
    public Transform addRotation(Quaterniond rotation) {
        checkNotNull(rotation, "rotation");
        setRotation(rotation.mul(getRotationAsQuaternion()));
        return this;
    }

    @Override
    public Transform addScale(Vector3d scale) {
        checkNotNull(scale, "scale");
        setScale(getScale().mul(scale));
        return this;
    }

    @Override
    public Matrix4d toMatrix() {
        return Matrix4d.createScaling(getScale().toVector4(1)).rotate(getRotationAsQuaternion()).translate(getPosition());
    }

    @Override
    public boolean isValid() {
        return getExtent().isLoaded();
    }

    @Override
    public String toString() {
        return "Transform{location=" + getLocation() + ", rotation=" + getRotation() + ", scale=" + getScale() + '}';
    }

}
