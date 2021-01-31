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
package org.spongepowered.common.world.volume.buffer.block;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.util.Mth;

/**
 * Basically a fixed length list of non negative numbers/ids.
 */
public interface BlockBackingData {

    /**
     * Gets the id at an index.
     */
    int get(int index);

    /**
     * Sets the id at an index. The id must not be negative.
     */
    void set(int index, int val);

    /**
     * Creates a copy of this BackingData
     */
    BlockBackingData copyOf();

    /**
     * Gets the maximum id supported by this BackingData
     */
    int getMax();

    class CharBackingData implements BlockBackingData {

        private final char[] data;

        public CharBackingData(final char[] data) {
            this.data = data;
        }

        @Override
        public int get(final int index) {
            return this.data[index];
        }

        @Override
        public void set(final int index, final int val) {
            this.data[index] = (char) val;
        }

        @Override
        public BlockBackingData copyOf() {
            return new CharBackingData(this.data.clone());
        }

        @Override
        public int getMax() {
            return Character.MAX_VALUE;
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final CharBackingData that = (CharBackingData) o;
            return Arrays.equals(this.data, that.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.data);
        }
    }

    class PackedBackingData implements BlockBackingData {

        /** A long array used to store the packed values */
        private final long[] longArray;
        /** Number of bits a single entry takes up */
        private final int bits;
        /**
         * The maximum value for a single entry. This also asks as a bitmask for a single entry.
         * For instance, if bits were 5, this value would be 31 (ie, {@code 0b00011111}).
         */
        private final long maxValue;
        /** Number of entries in this array (<b>not</b> the length of the long array that internally backs this array) */
        private final int arraySize;

        /**
         * Creates a new PackedBackingData starting out with enough bits to store values of {@code highestValue}.
         *
         * @param size The number of elements
         * @param highestValue The highest value to prepare for
         */
        public PackedBackingData(final int size, final int highestValue) {
            this.arraySize = size;
            int bits;
            bits = 0;
            while (1 << bits <= highestValue) {
                bits++;
            }
            this.bits = bits;

            this.maxValue = (1 << bits) - 1;
            this.longArray = new long[Mth.roundToward(size * bits, Long.SIZE) / Long.SIZE];
        }

        private PackedBackingData(final int size, final int bits, final long[] array) {
            this.arraySize = size;
            this.bits = bits;
            this.maxValue = (1 << bits) - 1;
            this.longArray = array;
        }

        @Override
        public void set(final int index, final int value) {
            final int bitIndex = index * this.bits;
            int longIndex = bitIndex / Long.SIZE;
            final int bitOffset = bitIndex % Long.SIZE;

            this.longArray[longIndex] = this.longArray[longIndex] & ~(this.maxValue << bitOffset) | (long) value << bitOffset;

            if (bitOffset + this.bits > Long.SIZE) {
                // The entry is split between two longs (lets call them left long, and right long)
                final int bitsInLeft = Long.SIZE - bitOffset;
                final int bitsInRight = this.bits - bitsInLeft;
                longIndex++;
                this.longArray[longIndex] = this.longArray[longIndex] >>> bitsInRight << bitsInRight | (long) value >> bitsInLeft;
            }
        }

        @Override
        public int get(final int index) {
            final int bitIndex = index * this.bits;
            final int longIndex = bitIndex / Long.SIZE;
            final int rightLongIndex = (bitIndex + this.bits - 1) / 64;
            final int bitOffset = bitIndex % 64;

            if (bitOffset + this.bits > Long.SIZE) {
                // The entry is split between two longs
                final int bitsInLeft = Long.SIZE - bitOffset;
                return (int) ((this.longArray[longIndex] >>> bitOffset | this.longArray[rightLongIndex] << bitsInLeft) & this.maxValue);
            }
            return (int) (this.longArray[longIndex] >>> bitOffset & this.maxValue);
        }

        @Override
        public PackedBackingData copyOf() {
            return new PackedBackingData(this.arraySize, this.bits, this.longArray.clone());
        }

        @Override
        public int getMax() {
            return (int) this.maxValue;
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final PackedBackingData that = (PackedBackingData) o;
            return this.bits == that.bits &&
                   this.maxValue == that.maxValue &&
                   this.arraySize == that.arraySize &&
                   Arrays.equals(this.longArray, that.longArray);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(this.bits, this.maxValue, this.arraySize);
            result = 31 * result + Arrays.hashCode(this.longArray);
            return result;
        }
    }
}
