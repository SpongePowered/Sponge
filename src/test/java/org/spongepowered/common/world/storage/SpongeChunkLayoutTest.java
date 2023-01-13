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
package org.spongepowered.common.world.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.spongepowered.api.util.Direction;
import org.spongepowered.math.vector.Vector3i;

public class SpongeChunkLayoutTest {

    @Test
    public void testConstants() {
        assertEquals(new Vector3i(16, 256, 16), SpongeChunkLayout.INSTANCE.chunkSize());
        assertEquals(new Vector3i(1874999, 0, 1874999), SpongeChunkLayout.INSTANCE.spaceMax());
        assertEquals(new Vector3i(-1875000, 0, -1875000), SpongeChunkLayout.INSTANCE.spaceMin());
        assertEquals(new Vector3i(3750000, 1, 3750000), SpongeChunkLayout.INSTANCE.spaceSize());
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.spaceOrigin());
    }

    @Test
    public void testCoordValidation() {
        assertTrue(SpongeChunkLayout.INSTANCE.isValidChunk(new Vector3i(0, 0, 0)));
        assertTrue(SpongeChunkLayout.INSTANCE.isValidChunk(0, 0, 0));
        assertTrue(SpongeChunkLayout.INSTANCE.isValidChunk(new Vector3i(1874999, 0, 1874999)));
        assertTrue(SpongeChunkLayout.INSTANCE.isValidChunk(1874999, 0, 1874999));
        assertTrue(SpongeChunkLayout.INSTANCE.isValidChunk(new Vector3i(-1875000, 0, -1875000)));
        assertTrue(SpongeChunkLayout.INSTANCE.isValidChunk(-1875000, 0, -1875000));

        assertFalse(SpongeChunkLayout.INSTANCE.isValidChunk(1875000, 0, 1874999));
        assertFalse(SpongeChunkLayout.INSTANCE.isValidChunk(1874999, 1, 1874999));
        assertFalse(SpongeChunkLayout.INSTANCE.isValidChunk(1874999, 0, 1875000));
        assertFalse(SpongeChunkLayout.INSTANCE.isValidChunk(-1875001, 0, -1875000));
        assertFalse(SpongeChunkLayout.INSTANCE.isValidChunk(-1875000, -1, -1875000));
        assertFalse(SpongeChunkLayout.INSTANCE.isValidChunk(-1875000, 0, -1875001));

        assertTrue(SpongeChunkLayout.INSTANCE.isInChunk(0, 0, 0));
        assertTrue(SpongeChunkLayout.INSTANCE.isInChunk(new Vector3i(14, 125, 9)));
        assertTrue(SpongeChunkLayout.INSTANCE.isInChunk(14, 125, 9));
        assertTrue(SpongeChunkLayout.INSTANCE.isInChunk(15, 255, 15));

        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(new Vector3i(-1, 0, 0)));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(new Vector3i(0, -1, 0)));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(new Vector3i(0, 0, -1)));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(-1, 0, 0));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(0, -1, 0));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(0, 0, -1));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(16, 255, 15));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(15, 256, 15));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(15, 255, 16));

        assertTrue(SpongeChunkLayout.INSTANCE.isInChunk(0, 0, 0, 0, 0, 0));
        assertTrue(SpongeChunkLayout.INSTANCE.isInChunk(new Vector3i(30, 125, -7), new Vector3i(1, 0, -1)));
        assertTrue(SpongeChunkLayout.INSTANCE.isInChunk(30, 125, -7, 1, 0, -1));
        assertTrue(SpongeChunkLayout.INSTANCE.isInChunk(65, 255, -33, 4, 0, -3));

        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(new Vector3i(-17, 0, 0), new Vector3i(-1, 0, 0)));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(new Vector3i(0, -257, 0), new Vector3i(0, -1, 0)));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(new Vector3i(0, 0, -17), new Vector3i(0, 0, -1)));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(-17, 0, 0, -1, 0, 0));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(0, -257, 0, 0, -1, 0));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(0, 0, -17, 0, 0, -1));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(32, 255, 31, 1, 0, 1));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(31, 256, 31, 1, 0, 1));
        assertFalse(SpongeChunkLayout.INSTANCE.isInChunk(31, 255, 32, 1, 0, 1));
    }

    @Test
    public void testCoordConversion() {
        // chunk to world
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.toChunk(Vector3i.ZERO).get());
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.toChunk(0, 0, 0).get());
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.toChunk(new Vector3i(15, 255, 15)).get());
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.toChunk(15, 255, 15).get());

        assertEquals(new Vector3i(2, 0, 4), SpongeChunkLayout.INSTANCE.toChunk(new Vector3i(34, 121, 72)).get());
        assertEquals(new Vector3i(2, 0, 4), SpongeChunkLayout.INSTANCE.toChunk(34, 121, 72).get());

        assertEquals(new Vector3i(-6, 0, -13), SpongeChunkLayout.INSTANCE.toChunk(new Vector3i(-83, 62, -203)).get());
        assertEquals(new Vector3i(-6, 0, -13), SpongeChunkLayout.INSTANCE.toChunk(-83, 62, -203).get());

        assertFalse(SpongeChunkLayout.INSTANCE.toChunk(30000000, 0, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toChunk(-30000001, 0, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toChunk(0, 256, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toChunk(0, -1, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toChunk(0, 0, 30000000).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toChunk(0, 0, -30000001).isPresent());

        // world to chunk
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.toWorld(Vector3i.ZERO).get());
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.toWorld(0, 0, 0).get());

        assertEquals(new Vector3i(32, 0, 64), SpongeChunkLayout.INSTANCE.toWorld(new Vector3i(2, 0, 4)).get());
        assertEquals(new Vector3i(32, 0, 64), SpongeChunkLayout.INSTANCE.toWorld(2, 0, 4).get());

        assertEquals(new Vector3i(-96, 0, -208), SpongeChunkLayout.INSTANCE.toWorld(new Vector3i(-6, 0, -13)).get());
        assertEquals(new Vector3i(-96, 0, -208), SpongeChunkLayout.INSTANCE.toWorld(-6, 0, -13).get());

        assertFalse(SpongeChunkLayout.INSTANCE.toWorld(1875000, 0, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toWorld(-1875001, 0, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toWorld(0, 1, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toWorld(0, -1, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toWorld(0, 0, 1875000).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.toWorld(0, 0, -1875001).isPresent());
    }

    @Test
    public void testCoordAdd() {
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.addToChunk(Vector3i.ZERO, Vector3i.ZERO).get());
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.addToChunk(0, 0, 0, 0, 0, 0).get());

        assertEquals(new Vector3i(7, 0, 5), SpongeChunkLayout.INSTANCE.addToChunk(3, 0, 5, 4, 0, 0).get());
        assertEquals(new Vector3i(7, 0, 9), SpongeChunkLayout.INSTANCE.addToChunk(3, 0, 5, 4, 0, 4).get());

        assertFalse(SpongeChunkLayout.INSTANCE.addToChunk(1874999, 0, 0, 1, 0, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.addToChunk(0, 0, 0, 0, 1, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.addToChunk(0, 0, 1874999, 0, 0, 1).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.addToChunk(-1875000, 0, 0, -1, 0, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.addToChunk(0, 0, 0, 0, -1, 0).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.addToChunk(0, 0, -1875000, 0, 0, -1).isPresent());
    }

    @Test
    public void testCoordMove() {
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.moveToChunk(Vector3i.ZERO, Direction.NONE).get());
        assertEquals(Vector3i.ZERO, SpongeChunkLayout.INSTANCE.moveToChunk(0, 0, 0, Direction.NONE).get());

        assertEquals(new Vector3i(4, 0, 5), SpongeChunkLayout.INSTANCE.moveToChunk(3, 0, 5, Direction.EAST).get());
        assertEquals(new Vector3i(7, 0, 5), SpongeChunkLayout.INSTANCE.moveToChunk(3, 0, 5, Direction.EAST, 4).get());
        assertEquals(new Vector3i(4, 0, 6), SpongeChunkLayout.INSTANCE.moveToChunk(3, 0, 5, Direction.SOUTHEAST).get());
        assertEquals(new Vector3i(7, 0, 9), SpongeChunkLayout.INSTANCE.moveToChunk(3, 0, 5, Direction.SOUTHEAST, 4).get());

        assertFalse(SpongeChunkLayout.INSTANCE.moveToChunk(1874999, 0, 0, Direction.EAST).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.moveToChunk(0, 0, 0, Direction.UP).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.moveToChunk(0, 0, 1874999, Direction.SOUTH).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.moveToChunk(-1875000, 0, 0, Direction.WEST).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.moveToChunk(0, 0, 0, Direction.DOWN).isPresent());
        assertFalse(SpongeChunkLayout.INSTANCE.moveToChunk(0, 0, -1875000, Direction.NORTH).isPresent());

        assertThrows(IllegalArgumentException.class, () -> SpongeChunkLayout.INSTANCE.moveToChunk(0, 0, 0, Direction.SOUTH_SOUTHEAST));
    }

}
