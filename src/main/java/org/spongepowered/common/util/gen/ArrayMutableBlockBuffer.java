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
package org.spongepowered.common.util.gen;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.schematic.BlockPalette;
import org.spongepowered.common.world.extent.MutableBlockViewDownsize;
import org.spongepowered.common.world.extent.MutableBlockViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBlockVolumeWrapper;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.GlobalPalette;

public class ArrayMutableBlockBuffer extends AbstractBlockBuffer implements MutableBlockVolume {

    private static final int LOCAL_PALETTE_THRESHOLD = 256;

    @SuppressWarnings("ConstantConditions")
    private static final BlockState AIR = BlockTypes.AIR.getDefaultState();

    private final BlockPalette palette;
    private final BackingData data;

    public ArrayMutableBlockBuffer(Vector3i start, Vector3i size) {
        this(size.getX() * size.getY() * size.getZ() > LOCAL_PALETTE_THRESHOLD ?
                new BimapPalette() : GlobalPalette.instance, start, size);
    }

    public ArrayMutableBlockBuffer(BlockPalette palette, Vector3i start, Vector3i size) {
        super(start, size);
        this.palette = palette;
        int dataSize = size.getX() * size.getY() * size.getZ();
        this.data = new PackedBackingData(dataSize, palette.getHighestId());

        // all blocks default to air
        int airId = this.palette.getOrAssign(AIR);
        if (airId != 0) {
            for (int i = 0; i < dataSize; i++) {
                data.set(i, airId);
            }
        }
    }

    public ArrayMutableBlockBuffer(BlockPalette palette, Vector3i start, Vector3i size, char[] blocks) {
        super(start, size);
        this.palette = palette;
        this.data = new CharBackingData(blocks);
    }

    ArrayMutableBlockBuffer(BlockPalette palette, Vector3i start, Vector3i size, BackingData blocks) {
        super(start, size);
        this.palette = palette;
        this.data = blocks;
    }

    @Override
    public BlockPalette getPalette() {
        return this.palette;
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block, Cause cause) {
        checkRange(x, y, z);
        this.data.set(getIndex(x, y, z), this.palette.getOrAssign(block));
        return true;
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkRange(x, y, z);
        return this.palette.get(this.data.get(getIndex(x, y, z))).orElse(AIR);
    }

    @Override
    public MutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        checkRange(newMin.getX(), newMin.getY(), newMin.getZ());
        checkRange(newMax.getX(), newMax.getY(), newMax.getZ());
        return new MutableBlockViewDownsize(this, newMin, newMax);
    }

    @Override
    public MutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return new MutableBlockViewTransform(this, transform);
    }

    @Override
    public MutableBlockVolumeWorker<? extends MutableBlockVolume> getBlockWorker(Cause cause) {
        return new SpongeMutableBlockVolumeWorker<>(this, cause);
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return new UnmodifiableBlockVolumeWrapper(this);
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        switch (type) {
            case STANDARD:
                return new ArrayMutableBlockBuffer(this.palette, this.start, this.size, this.data.copyOf());
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return new ArrayImmutableBlockBuffer(this.palette, this.start, this.size, this.data);
    }

    /**
     * Basically a fixed length list of numbers/ids.
     * It may be a good idea to optimize for lower numbers/ids,
     * however up to Block.BLOCK_STATE_IDS.size() must be supported.
     */
    interface BackingData {

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
        BackingData copyOf();

    }

    static class CharBackingData implements BackingData {

        private final char[] data;

        public CharBackingData(char[] data) {
            this.data = data;
        }

        @Override
        public int get(int index) {
            return this.data[index] & 0xFFFF;
        }

        @Override
        public void set(int index, int val) {
            this.data[index] = (char) val;
        }

        @Override
        public BackingData copyOf() {
            return new CharBackingData(this.data.clone());
        }
    }

    static class PackedBackingData implements BackingData {
        /** A long array used to store the packed values */
        private long[] longArray;
        /** Number of bits a single entry takes up */
        private int bits;
        /**
         * The maximum value for a single entry. This also asks as a bitmask for a single entry.
         * For instance, if bits were 5, this value would be 31 (ie, {@code 0b00011111}).
         */
        private long maxValue;
        /** Number of entries in this array (<b>not</b> the length of the long array that internally backs this array) */
        private final int arraySize;

        /**
         * @param size The number of elements
         */
        public PackedBackingData(int size) {
            this(size, 0);
        }

        /**
         * Creates a new PackedBackingData starting out with enough bits to store values of {@code highestValue}.
         *
         * @param size The number of elements
         * @param highestValue The highest value to prepare for
         */
        public PackedBackingData(int size, int highestValue) {
            this.arraySize = size;
            for (bits = 0; 1 << bits <= highestValue; bits++);

            this.maxValue = (1 << bits) - 1;
            this.longArray = new long[MathHelper.roundUp(size * bits, Long.SIZE) / Long.SIZE];
        }

        private PackedBackingData(int size, int bits, long[] array) {
            this.arraySize = size;
            this.bits = bits;
            this.maxValue = (1 << bits) - 1;
            this.longArray = array;
        }

        @Override
        public void set(int index, int value) {
            if (value > maxValue) {
                remax(value);
            }

            int bitIndex = index * bits;
            int longIndex = bitIndex / Long.SIZE;
            int bitOffset = bitIndex % Long.SIZE;

            longArray[longIndex] = longArray[longIndex] & ~(maxValue << bitOffset) | (long) value << bitOffset;

            if (bitOffset + bits > Long.SIZE) {
                // The entry is split between two longs (lets call them left long, and right long)
                int bitsInLeft = Long.SIZE - bitOffset;
                int bitsInRight = bits - bitsInLeft;
                longIndex++;
                longArray[longIndex] = longArray[longIndex] >>> bitsInRight << bitsInRight | (long) value >> bitsInLeft;
            }
        }

        /**
         * Increases the maximum value by adding more bits to each value.
         */
        private void remax(int highestValue) {
            int newBits;
            for (newBits = bits; 1 << newBits <= highestValue; newBits++);

            long newMaxValue = (1 << newBits) - 1;
            long[] newLongArray = new long[MathHelper.roundUp(arraySize * newBits, Long.SIZE) / Long.SIZE];

            for (int i = 0; i < arraySize; i++) {
                int value = get(i);

                int bitIndex = i * newBits;
                int longIndex = bitIndex / Long.SIZE;
                int bitOffset = bitIndex % Long.SIZE;

                newLongArray[longIndex] = newLongArray[longIndex] & ~(newMaxValue << bitOffset) | (long) value << bitOffset;

                if (bitOffset + newBits > Long.SIZE) {
                    // The entry is split between two longs (lets call them left long, and right long)
                    int bitsInLeft = Long.SIZE - bitOffset;
                    int bitsInRight = newBits - bitsInLeft;
                    longIndex++;
                    newLongArray[longIndex] = newLongArray[longIndex] >>> bitsInRight << bitsInRight | (long) value >> bitsInLeft;
                }
            }
            bits = newBits;
            maxValue = newMaxValue;
            longArray = newLongArray;
        }

        @Override
        public int get(int index) {
            int bitIndex = index * bits;
            int longIndex = bitIndex / Long.SIZE;
            int rightLongIndex = (bitIndex + bits - 1) / 64;
            int bitOffset = bitIndex % 64;

            if (bitOffset + bits > Long.SIZE) {
                // The entry is split between two longs
                int bitsInLeft = Long.SIZE - bitOffset;
                return (int) ((longArray[longIndex] >>> bitOffset | longArray[rightLongIndex] << bitsInLeft) & maxValue);
            } else {
                return (int) (longArray[longIndex] >>> bitOffset & maxValue);
            }
        }

        @Override
        public PackedBackingData copyOf() {
            return new PackedBackingData(arraySize, bits, longArray.clone());
        }
    }
}
