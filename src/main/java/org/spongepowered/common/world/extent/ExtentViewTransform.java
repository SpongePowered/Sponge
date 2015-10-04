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
package org.spongepowered.common.world.extent;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.common.mixin.core.world.extent.MixinExtent;
import org.spongepowered.common.mixin.core.world.extent.MixinExtentViewTransform;

/**
 * Code for this class is mixed in from {@link MixinExtent}
 * and {@link MixinExtentViewTransform}.
 * This prevents duplicate code.
 */
public class ExtentViewTransform {

    private final Extent extent;
    private final DiscreteTransform3 transform;
    private final DiscreteTransform3 inverseTransform;
    private final DiscreteTransform3to2 inverseTransform2;
    private final Vector3i blockMin;
    private final Vector3i blockMax;
    private final Vector3i blockSize;
    private final Vector2i biomeMin;
    private final Vector2i biomeMax;
    private final Vector2i biomeSize;

    private ExtentViewTransform(Extent extent, DiscreteTransform3 transform) {
        this.extent = extent;
        this.transform = transform;
        this.inverseTransform = transform.invert();
        this.inverseTransform2 = new DiscreteTransform3to2(this.inverseTransform);

        final Vector3i blockA = transform.transform(extent.getBlockMin());
        final Vector3i blockB = transform.transform(extent.getBlockMax());
        this.blockMin = blockA.min(blockB);
        this.blockMax = blockA.max(blockB);
        this.blockSize = this.blockMax.sub(this.blockMin).add(Vector3i.ONE);

        final Vector2i biomeMin = extent.getBiomeMin();
        final Vector2i biomeMax = extent.getBiomeMax();
        final Vector2i biomeA = transform.transform(new Vector3i(biomeMin.getX(), 0, biomeMin.getY())).toVector2(true);
        final Vector2i biomeB = transform.transform(new Vector3i(biomeMax.getX(), 0, biomeMax.getY())).toVector2(true);
        this.biomeMin = biomeA.min(biomeB);
        this.biomeMax = biomeA.max(biomeB);
        this.biomeSize = this.biomeMax.sub(this.biomeMin).add(Vector2i.ONE);
    }

    public static Extent newInstance(Extent extent, DiscreteTransform3 transform) {
        return (Extent) new ExtentViewTransform(extent, transform);
    }

    public static class DiscreteTransform3to2 {

        private final DiscreteTransform3 transform;
        private final boolean valid;

        private DiscreteTransform3to2(DiscreteTransform3 transform) {
            this.transform = transform;

            /*

                Biomes are 2 dimensional and form a plane on the x and z axes.
                The y axis is ignored when converting from 3D to 2D and is
                perpendicular to these 2 axes.

                We can only sample biomes if they stay in the xz plane. If we
                have something different we effectively have no biomes. We can
                transform the x and z axes to figure this out, finding the
                perpendicular axis using the cross product.

            */

            final Vector3i xTransformed = transform.transform(Vector3i.UNIT_X);
            final Vector3i zTransformed = transform.transform(Vector3i.UNIT_Z);
            final Vector3i perpendicular = zTransformed.cross(xTransformed);

            final float xSign = Math.copySign(1, perpendicular.getX());
            final float ySign = Math.copySign(1, perpendicular.getY());
            final float zSign = Math.copySign(1, perpendicular.getZ());
            this.valid = xSign == zSign && xSign != ySign;
        }

        public int transformX(int x, int y) {
            Preconditions.checkState(valid, "Cannot access biomes when rotated around an axis that isn't y");
            return transform.transformX(x, 0, y);
        }

        public int transformZ(int x, int y) {
            Preconditions.checkState(valid, "Cannot access biomes when rotated around an axis that isn't y");
            return transform.transformZ(x, 0, y);
        }

    }

}
