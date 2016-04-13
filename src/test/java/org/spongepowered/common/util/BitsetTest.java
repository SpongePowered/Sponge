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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

public class BitsetTest {

    @Test
    public void testLength() {
        Bitset bitset = new Bitset(4);
        assertEquals(4, bitset.getLength());
    }

    @Test
    public void testGet() {
        Bitset bitset = new Bitset(8, new long[] { 2 });

        assertFalse(bitset.get(0));
        assertTrue(bitset.get(1));
        for (int i = 2; i < 8; i++) {
            assertFalse(bitset.get(i));
        }

        try {
            bitset.get(-1);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testSet() {
        Bitset bitset = new Bitset(8);

        for (int i = 0; i < 8; i++) {
            assertFalse(bitset.get(i));
        }

        bitset.set(3);

        for (int i = 0; i < 8; i++) {
            if (i == 3) {
                assertTrue(bitset.get(i));
            } else {
                assertFalse(bitset.get(i));
            }
        }

        bitset.set(4, true);
        bitset.set(3, false);

        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                assertTrue(bitset.get(i));
            } else {
                assertFalse(bitset.get(i));
            }
        }

        bitset.unset(4);
        bitset.unset(5);

        for (int i = 0; i < 8; i++) {
            assertFalse(bitset.get(i));
        }
    }

    @Test
    public void testResizeing() {
        Bitset bitset = new Bitset(4);
        assertEquals(4, bitset.getLength());
        bitset.set(8);
        assertTrue(bitset.get(8));
        assertTrue(bitset.getLength() > 8);
    }

    @Test
    public void testHigh() {
        Bitset bitset = new Bitset(500);
        assertFalse(bitset.get(480));
        bitset.set(480);
        assertTrue(bitset.get(480));
    }

    @Test
    public void testUnion() {
        Bitset a = new Bitset(8);
        Bitset b = new Bitset(8);

        a.set(3);
        b.set(4);

        a.union(b);

        for (int i = 0; i < 8; i++) {
            if (i == 3 || i == 4) {
                assertTrue(a.get(i));
            } else {
                assertFalse(a.get(i));
            }
        }
    }

    @Test
    public void testIntersect() {
        Bitset a = new Bitset(8);
        Bitset b = new Bitset(8);

        a.set(3);
        a.set(4);
        b.set(4);

        a.intersect(b);

        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                assertTrue(a.get(i));
            } else {
                assertFalse(a.get(i));
            }
        }
    }

    @Test
    public void testSubtract() {
        Bitset a = new Bitset(8);
        Bitset b = new Bitset(8);

        a.set(3);
        a.set(4);
        b.set(4);

        a.subtract(b);

        for (int i = 0; i < 8; i++) {
            if (i == 3) {
                assertTrue(a.get(i));
            } else {
                assertFalse(a.get(i));
            }
        }
    }

    @Test
    public void testXor() {
        Bitset a = new Bitset(8);
        Bitset b = new Bitset(8);

        a.set(3);
        a.set(4);
        b.set(4);

        a.intersect(b);
        System.out.println(a);
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                assertTrue(a.get(i));
            } else {
                assertFalse(a.get(i));
            }
        }
    }

    @Test
    public void testInvert() {
        Bitset a = new Bitset(8);

        a.set(3);
        a.set(4);

        a.invert();

        for (int i = 0; i < 8; i++) {
            if (i == 3 || i == 4) {
                assertFalse(a.get(i));
            } else {
                assertTrue(a.get(i));
            }
        }
    }

}
