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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

public class Bitset implements Iterable<Integer> {

    private static int arrayLength(int length) {
        return (length - 1) / 64 + 1;
    }

    private long[] shape;
    private int length;

    public Bitset(int length) {
        this.shape = new long[arrayLength(length)];
        this.length = length;
    }

    public Bitset(int length, long[] data) {
        this(length);
        System.arraycopy(data, 0, this.shape, 0, Math.min(arrayLength(length), data.length));
    }

    public Bitset(Bitset other) {
        this(other.getLength());
        System.arraycopy(other.shape, 0, this.shape, 0, Math.min(arrayLength(this.length), arrayLength(other.shape.length)));
    }

    public int getLength() {
        return this.length;
    }

    public void set(int x, boolean state) {
        if (state) {
            set(x);
        } else {
            unset(x);
        }
    }

    public void set(int z) {
        if (z < 0) {
            throw new ArrayIndexOutOfBoundsException("Tried to set point outside of the shape: " + z);
        }
        if (z >= this.length) {
            resize(Math.max(this.length * 2, z + 1));
        }
        this.shape[z / 64] |= (1l << z % 64);
    }

    public void unset(int z) {
        if (z < 0) {
            throw new ArrayIndexOutOfBoundsException("Tried to set point outside of the shape: " + z);
        }
        if (z < this.length) {
            this.shape[z / 64] &= ~(1l << (z % 64));
        }
    }

    public boolean get(int z) {
        if (z < 0) {
            throw new ArrayIndexOutOfBoundsException("Tried to set point outside of the shape: " + z);
        }
        if (z >= this.length) {
            return false;
        }
        return ((this.shape[z / 64] >> z % 64) & 1) == 1;
    }

    public void resize(int nl) {
        if (this.shape.length == arrayLength(nl)) {
            this.length = nl;
            return;
        }
        long[] newarray = new long[arrayLength(nl)];
        System.arraycopy(this.shape, 0, newarray, 0, Math.min(newarray.length, this.shape.length));
        this.shape = newarray;
        this.length = nl;
    }

    public void union(Bitset s) {
        if (s.getLength() >= this.length) {
            resize(Math.max((this.length * 7) / 4 + 1, s.getLength()));
        }
        long[] sshape = s.shape;
        for (int z = 0; z < Math.min(arrayLength(s.getLength()), arrayLength(this.length)); z++) {
            this.shape[z] |= sshape[z];
        }
    }

    public void subtract(Bitset s) {
        long[] sshape = s.shape;
        for (int z = 0; z < Math.min(arrayLength(s.getLength()), arrayLength(this.length)); z++) {
            this.shape[z] &= ~sshape[z];
        }
    }

    public void intersect(Bitset s) {
        long[] sshape = s.shape;
        for (int z = 0; z < Math.min(arrayLength(s.getLength()), arrayLength(this.length)); z++) {
            this.shape[z] &= sshape[z];
        }
    }

    public void xor(Bitset s) {
        if (s.getLength() >= this.length) {
            resize(Math.max((this.length * 7) / 4 + 1, s.getLength()));
        }
        long[] sshape = s.shape;
        for (int z = 0; z < Math.min(arrayLength(s.getLength()), arrayLength(this.length)); z++) {
            this.shape[z] = (this.shape[z] & ~sshape[z]) | (~this.shape[z] & sshape[z]);
        }
    }

    public void invert() {
        for (int z = 0; z < arrayLength(this.length); z++) {
            this.shape[z] = ~this.shape[z];
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Itr();
    }

    @Override
    public Bitset clone() {
        return new Bitset(this);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("");
        string.append("Bitset (").append(getLength()).append(")\n");
        for (int z = 0; z < getLength(); z++) {
            string.append(get(z) ? "1" : "0");
        }
        return string.toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Bitset)) {
            return false;
        }
        Bitset b = (Bitset) o;
        if (b.getLength() != getLength()) {
            return false;
        }
        for (int z = 0; z < arrayLength(this.length); z++) {
            if (this.shape[z] != b.shape[z]) {
                return false;
            }
        }
        for (int z = (getLength() / 64) * 64; z < getLength(); z++) {
            if (get(z) != b.get(z)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int r = 1;
        r = r * 37 + this.length;
        for (int i = 0; i < this.shape.length; i++) {
            r = r * 37 + (int) ((this.shape[i] >>> 32) ^ this.shape[i]);
        }
        return r;
    }

    private class Itr implements Iterator<Integer> {

        private int last = -1;
        private int next = 0;

        public Itr() {
        }

        @Override
        public boolean hasNext() {
            return this.next < Bitset.this.getLength();
        }

        @Override
        public Integer next() {
            if (this.next >= Bitset.this.getLength()) {
                throw new NoSuchElementException();
            }
            int r = this.next++;
            while (this.next <= Bitset.this.getLength()) {
                if (Bitset.this.get(this.next)) {
                    break;
                }
                this.next++;
            }
            this.last = r;
            return r;
        }

        @Override
        public void remove() {
            if (this.last >= 0 && this.last < Bitset.this.getLength()) {
                Bitset.this.unset(this.last);
            } else {
                throw new IllegalStateException();
            }
        }

    }
}
