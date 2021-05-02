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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Random;

final class SpongeAABBTest {
    private static final Random RANDOM = new Random();

    @Test
    void testConstructor() {
        final AABB aabb1 = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        Assertions.assertEquals(new Vector3d(1, 2, 3), aabb1.min());
        Assertions.assertEquals(new Vector3d(7, 10, 13), aabb1.max());

        final AABB aabb2 = new SpongeAABB(new Vector3d(11, 2, 3), new Vector3d(7, -10, 13));
        Assertions.assertEquals(new Vector3d(7, -10, 3), aabb2.min());
        Assertions.assertEquals(new Vector3d(11, 2, 13), aabb2.max());

        try {
            new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(1, 10, 13));
            Assertions.fail();
        } catch (IllegalArgumentException ignored) {
            // pass
        }

        try {
            new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 2, 13));
            Assertions.fail();
        } catch (IllegalArgumentException ignored) {
            // pass
        }

        try {
            new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 3));
            Assertions.fail();
        } catch (IllegalArgumentException ignored) {
            // pass
        }
    }

    @Test
    void testSize() {
        final AABB aabb = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        Assertions.assertEquals(new Vector3d(6, 8, 10), aabb.size());
    }

    @Test
    void testCenter() {
        final AABB aabb = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        Assertions.assertEquals(new Vector3d(4, 6, 8), aabb.center());
    }

    @Test
    void testContainsCoordinates() {
        final AABB aabb = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        Assertions.assertTrue(aabb.contains(5, 3, 11));
        Assertions.assertTrue(aabb.contains(7, 3, 11));
        Assertions.assertTrue(aabb.contains(5, 4, 11));
        Assertions.assertTrue(aabb.contains(5, 3, 13));
        Assertions.assertFalse(aabb.contains(-1, 3, 11));
        Assertions.assertFalse(aabb.contains(5, 11, 11));
        Assertions.assertFalse(aabb.contains(5, 3, 14));
    }

    @Test
    void testContainsVector3d() {
        final AABB aabb = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        Assertions.assertTrue(aabb.contains(new Vector3d(5, 3, 11)));
        Assertions.assertFalse(aabb.contains(new Vector3d(-1, 3, 11)));
        Assertions.assertFalse(aabb.contains(new Vector3d(5, 11, 11)));
        Assertions.assertFalse(aabb.contains(new Vector3d(5, 3, 14)));
    }

    @Test
    void testContainsVector3i() {
        final AABB aabb = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        Assertions.assertTrue(aabb.contains(new Vector3i(5, 3, 11)));
        Assertions.assertFalse(aabb.contains(new Vector3i(-1, 3, 11)));
        Assertions.assertFalse(aabb.contains(new Vector3i(5, 11, 11)));
        Assertions.assertFalse(aabb.contains(new Vector3i(5, 3, 14)));
    }

    @Test
    void testIntersectsAABB() {
        for (int i = 0; i < 1000; i++) {
            final AABB aabb1 = newAABB();
            final AABB aabb2 = newIntersectingAABB(aabb1);
            final AABB aabb3 = aabb2.offset(aabb1.size().add(aabb2.size()));
            Assertions.assertTrue(aabb1.intersects(aabb2));
            Assertions.assertTrue(aabb2.intersects(aabb1));
            Assertions.assertFalse(aabb1.intersects(aabb3));
            Assertions.assertFalse(aabb3.intersects(aabb1));
        }
    }

    @Test
    void testIntersectsRay() {
        final AABB aabb = new SpongeAABB(new Vector3d(0, 0, 0), new Vector3d(2, 2, 2));
        Assertions.assertEquals(new Tuple<>(new Vector3d(2, 1, 1), new Vector3d(1, 0, 0)),
            aabb.intersects(new Vector3d(1, 1, 1), new Vector3d(1, 0, 0)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(1, 2, 1), new Vector3d(0, 1, 0)),
            aabb.intersects(new Vector3d(1, 1, 1), new Vector3d(0, 1, 0)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(1, 1, 2), new Vector3d(0, 0, 1)),
            aabb.intersects(new Vector3d(1, 1, 1), new Vector3d(0, 0, 1)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(0, 0, 0), new Vector3d(-1, -1, -1).normalize()),
            aabb.intersects(new Vector3d(-1, -1, -1), new Vector3d(1, 1, 1)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(0, 0, 1), new Vector3d(-1, -1, -0.0).normalize()),
            aabb.intersects(new Vector3d(-1, -1, 1), new Vector3d(1, 1, 0)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(0, 1, 1), new Vector3d(-1, -0.0, -0.0)),
            aabb.intersects(new Vector3d(-1, 1, 1), new Vector3d(1, 0, 0)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(2, 1, 1), new Vector3d(1, 0, 0)),
            aabb.intersects(new Vector3d(3, 1, 1), new Vector3d(-1, 0, 0)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(1, 0, 1), new Vector3d(-0.0, -1, -0.0)),
            aabb.intersects(new Vector3d(1, -1, 1), new Vector3d(0, 1, 0)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(1, 2, 1), new Vector3d(0, 1, 0)),
            aabb.intersects(new Vector3d(1, 3, 1), new Vector3d(0, -1, 0)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(1, 1, 0), new Vector3d(-0.0, -0.0, -1)),
            aabb.intersects(new Vector3d(1, 1, -1), new Vector3d(0, 0, 1)).get());
        Assertions.assertEquals(new Tuple<>(new Vector3d(1, 1, 2), new Vector3d(0, 0, 1)),
            aabb.intersects(new Vector3d(1, 1, 3), new Vector3d(0, 0, -1)).get());
        Assertions.assertFalse(aabb.intersects(new Vector3d(-1, -1, -1), new Vector3d(0, 1, 0)).isPresent());
    }

    @Test
    void testOffsetCoordinates() {
        final AABB aabb1 = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        final AABB aabb2 = new SpongeAABB(new Vector3d(11, 0, 4), new Vector3d(17, 8, 14));
        Assertions.assertEquals(aabb2, aabb1.offset(10, -2, 1));
    }

    @Test
    void testOffsetVector3d() {
        final AABB aabb1 = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        final AABB aabb2 = new SpongeAABB(new Vector3d(11, 0, 4), new Vector3d(17, 8, 14));
        Assertions.assertEquals(aabb2, aabb1.offset(new Vector3d(10, -2, 1)));
    }

    @Test
    void testOffsetVector3i() {
        final AABB aabb1 = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        final AABB aabb2 = new SpongeAABB(new Vector3d(11, 0, 4), new Vector3d(17, 8, 14));
        Assertions.assertEquals(aabb2, aabb1.offset(new Vector3i(10, -2, 1)));
    }

    @Test
    void testExpandCoordinates() {
        final AABB aabb1 = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        final AABB aabb2 = new SpongeAABB(new Vector3d(-4, 3, 2.5), new Vector3d(12, 9, 13.5));
        Assertions.assertEquals(aabb2, aabb1.expand(10, -2, 1));
    }

    @Test
    void testExpandVector3d() {
        final AABB aabb1 = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        final AABB aabb2 = new SpongeAABB(new Vector3d(-4, 3, 2.5), new Vector3d(12, 9, 13.5));
        Assertions.assertEquals(aabb2, aabb1.expand(new Vector3d(10, -2, 1)));
    }

    @Test
    void testExpandVector3i() {
        final AABB aabb1 = new SpongeAABB(new Vector3d(1, 2, 3), new Vector3d(7, 10, 13));
        final AABB aabb2 = new SpongeAABB(new Vector3d(-4, 3, 2.5), new Vector3d(12, 9, 13.5));
        Assertions.assertEquals(aabb2, aabb1.expand(new Vector3i(10, -2, 1)));
    }

    private static AABB newAABB() {
        final Vector3d min = new Vector3d(RANDOM.nextDouble() * 20 - 10, RANDOM.nextDouble() * 20 - 10, RANDOM.nextDouble() * 20 - 10);
        return new SpongeAABB(min, min.add(RANDOM.nextDouble() * 4 + 4, RANDOM.nextDouble() * 4 + 4, RANDOM.nextDouble() * 4 + 4));
    }

    private static AABB newIntersectingAABB(AABB with) {
        final Vector3d wMin = with.min();
        final Vector3d wSize = with.size();
        final double iSizeX = RANDOM.nextDouble() * wSize.x();
        final double iSizeY = RANDOM.nextDouble() * wSize.y();
        final double iSizeZ = RANDOM.nextDouble() * wSize.z();
        final double eSizeX = RANDOM.nextDouble() * 4 + 4;
        final double eSizeY = RANDOM.nextDouble() * 4 + 4;
        final double eSizeZ = RANDOM.nextDouble() * 4 + 4;
        final Vector3d min = wMin.sub(eSizeX, eSizeY, eSizeZ);
        final Vector3d max = wMin.add(iSizeX, iSizeY, iSizeZ);
        return new SpongeAABB(min, max);
    }
}
