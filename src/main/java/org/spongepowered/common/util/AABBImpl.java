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

import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.math.vector.Vector3d;

public final class AABBImpl implements AABB {
    private final Vector3d min;
    private final Vector3d max;
    @Nullable private Vector3d size = null;
    @Nullable private Vector3d center = null;

    public AABBImpl(final Vector3d v1, final Vector3d v2) {
        this.min = v1.min(v2);
        this.max = v1.max(v2);
        if (this.min.getX() == this.max.getX()) {
            throw new IllegalArgumentException("The box is generate on x!");
        }
        if (this.min.getY() == this.max.getY()) {
            throw new IllegalArgumentException("The box is generate on y!");
        }
        if (this.min.getZ() == this.max.getZ()) {
            throw new IllegalArgumentException("The box is generate on z!");
        }
    }

    @Override
    public Vector3d getMin() {
        return this.min;
    }

    @Override
    public Vector3d getMax() {
        return this.max;
    }

    @Override
    public Vector3d getCenter() {
        if (this.center == null) {
            this.center = this.min.add(this.getSize().div(2));
        }
        return this.center;
    }

    @Override
    public Vector3d getSize() {
        if (this.size == null) {
            this.size = this.max.sub(this.min);
        }
        return this.size;
    }

    @Override
    public boolean contains(final double x, final double y, final double z) {
        final Vector3d min = this.min;
        final Vector3d max = this.max;
        return min.getX() <= x && max.getX() >= x
          && min.getY() <= y && max.getY() >= y
          && min.getZ() <= z && max.getZ() >= z;
    }

    @Override
    public boolean intersects(final AABB other) {
        Objects.requireNonNull(other, "other");
        final Vector3d mins = this.min;
        final Vector3d maxs = this.max;
        final Vector3d mino = other.getMin();
        final Vector3d maxo = other.getMax();
        return maxs.getX() >= mino.getX() && maxo.getX() >= mins.getX()
          && maxs.getY() >= mino.getY() && maxo.getY() >= mins.getY()
          && maxs.getZ() >= mino.getZ() && maxo.getZ() >= mins.getZ();
    }

    @Override
    public Optional<Tuple<Vector3d, Vector3d>> intersects(final Vector3d start, final Vector3d direction) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(direction, "direction");
        // Adapted from: https://github.com/flow/react/blob/develop/src/main/java/com/flowpowered/react/collision/RayCaster.java#L156
        // The box is interpreted as 6 infinite perpendicular places, one for each face (being expanded infinitely)
        // "t" variables are multipliers: start + direction * t gives the intersection point
        // Find the intersections on the -x and +x planes, oriented by direction
        final double txMin;
        final double txMax;
        final Vector3d xNormal;
        if (Math.copySign(1, direction.getX()) > 0) {
            txMin = (this.min.getX() - start.getX()) / direction.getX();
            txMax = (this.max.getX() - start.getX()) / direction.getX();
            xNormal = Vector3d.UNIT_X;
        } else {
            txMin = (this.max.getX() - start.getX()) / direction.getX();
            txMax = (this.min.getX() - start.getX()) / direction.getX();
            xNormal = Vector3d.UNIT_X.negate();
        }
        // Find the intersections on the -y and +y planes, oriented by direction
        final double tyMin;
        final double tyMax;
        final Vector3d yNormal;
        if (Math.copySign(1, direction.getY()) > 0) {
            tyMin = (this.min.getY() - start.getY()) / direction.getY();
            tyMax = (this.max.getY() - start.getY()) / direction.getY();
            yNormal = Vector3d.UNIT_Y;
        } else {
            tyMin = (this.max.getY() - start.getY()) / direction.getY();
            tyMax = (this.min.getY() - start.getY()) / direction.getY();
            yNormal = Vector3d.UNIT_Y.negate();
        }
        // The ray should intersect the -x plane before the +y plane and intersect
        // the -y plane before the +x plane, else it is outside the box
        if (txMin > tyMax || txMax < tyMin) {
            return Optional.empty();
        }
        // Keep track of the intersection normal which also helps with floating point errors
        Vector3d normalMax;
        Vector3d normalMin;
        // The ray intersects only the furthest min plane on the box and only the closest
        // max plane on the box
        double tMin;
        if (tyMin == txMin) {
            tMin = tyMin;
            normalMin = xNormal.negate().sub(yNormal);
        } else if (tyMin > txMin) {
            tMin = tyMin;
            normalMin = yNormal.negate();
        } else {
            tMin = txMin;
            normalMin = xNormal.negate();
        }
        double tMax;
        if (tyMax == txMax) {
            tMax = tyMax;
            normalMax = xNormal.add(yNormal);
        } else if (tyMax < txMax) {
            tMax = tyMax;
            normalMax = yNormal;
        } else {
            tMax = txMax;
            normalMax = xNormal;
        }
        // Find the intersections on the -z and +z planes, oriented by direction
        final double tzMin;
        final double tzMax;
        final Vector3d zNormal;
        if (Math.copySign(1, direction.getZ()) > 0) {
            tzMin = (this.min.getZ() - start.getZ()) / direction.getZ();
            tzMax = (this.max.getZ() - start.getZ()) / direction.getZ();
            zNormal = Vector3d.UNIT_Z;
        } else {
            tzMin = (this.max.getZ() - start.getZ()) / direction.getZ();
            tzMax = (this.min.getZ() - start.getZ()) / direction.getZ();
            zNormal = Vector3d.UNIT_Z.negate();
        }
        // The ray intersects only the furthest min plane on the box and only the closest
        // max plane on the box
        if (tMin > tzMax || tMax < tzMin) {
            return Optional.empty();
        }
        // The ray should intersect the closest plane outside first and the furthest
        // plane outside last
        if (tzMin == tMin) {
            normalMin = normalMin.sub(zNormal);
        } else if (tzMin > tMin) {
            tMin = tzMin;
            normalMin = zNormal.negate();
        }
        if (tzMax == tMax) {
            normalMax = normalMax.add(zNormal);
        } else if (tzMax < tMax) {
            tMax = tzMax;
            normalMax = zNormal;
        }
        // Both intersection points are behind the start, there are no intersections
        if (tMax < 0) {
            return Optional.empty();
        }
        // Find the final intersection multiplier and normal
        final double t;
        Vector3d normal;
        if (tMin < 0) {
            // Only the furthest intersection is after the start, so use it
            t = tMax;
            normal = normalMax;
        } else {
            // Both are after the start, use the closest one
            t = tMin;
            normal = normalMin;
        }
        normal = normal.normalize();
        // To avoid rounding point errors leaving the intersection point just off the plane
        // we check the normal to use the actual plane value from the box coordinates
        final double x;
        final double y;
        final double z;
        if (normal.getX() > 0) {
            x = this.max.getX();
        } else if (normal.getX() < 0) {
            x = this.min.getX();
        } else {
            x = direction.getX() * t + start.getX();
        }
        if (normal.getY() > 0) {
            y = this.max.getY();
        } else if (normal.getY() < 0) {
            y = this.min.getY();
        } else {
            y = direction.getY() * t + start.getY();
        }
        if (normal.getZ() > 0) {
            z = this.max.getZ();
        } else if (normal.getZ() < 0) {
            z = this.min.getZ();
        } else {
            z = direction.getZ() * t + start.getZ();
        }
        return Optional.of(new Tuple<>(new Vector3d(x, y, z), normal));
    }

    @Override
    public AABBImpl offset(final double x, final double y, final double z) {
        return new AABBImpl(this.min.add(x, y, z), this.max.add(x, y, z));
    }

    @Override
    public AABBImpl expand(double x, double y, double z) {
        x /= 2;
        y /= 2;
        z /= 2;
        return new AABBImpl(this.min.sub(x, y, z), this.max.add(x, y, z));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AABBImpl)) {
            return false;
        }
        final AABBImpl aabb = (AABBImpl) other;
        return this.min.equals(aabb.min) && this.max.equals(aabb.max);
    }

    @Override
    public int hashCode() {
        int result = this.min.hashCode();
        result = 31 * result + this.max.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AABBImpl(" + this.min + " to " + this.max + ")";
    }

    public static class FactoryImpl implements Factory {
        @Override
        public AABB create(final Vector3d v1, final Vector3d v2) {
            Objects.requireNonNull(v1, "v1");
            Objects.requireNonNull(v2, "v2");
            return new AABBImpl(v1, v2);
        }
    }
}
