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
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer.PackedBackingData;
import org.spongepowered.common.world.extent.MutableBlockViewDownsize;
import org.spongepowered.common.world.extent.MutableBlockViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBlockVolumeWrapper;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.BlockPaletteWrapper;
import org.spongepowered.common.world.schematic.GlobalPalette;

import java.util.Arrays;
import java.util.Objects;

public class ArrayMutableBlockBuffer extends AbstractBlockBuffer implements MutableBlockVolume {

    /**
     * If the area is lower than this amount, a global palette will be used.<br/>
     * Example: If its only a 4 block area why allocate a local palette that
     * by its self will take up more space in memory than it saves.
     */
    private static final int SMALL_AREA_THRESHOLD = 256;

    @SuppressWarnings("ConstantConditions")
    private static final BlockState AIR = BlockTypes.AIR.getDefaultState();

    private Palette<BlockState> palette;
    private BackingData data;

    @SuppressWarnings("deprecation")
    public ArrayMutableBlockBuffer(Vector3i start, Vector3i size) {
        this(size.getX() * size.getY() * size.getZ() > SMALL_AREA_THRESHOLD ?
             new BlockPaletteWrapper(new BimapPalette<>(PaletteTypes.LOCAL_BLOCKS), org.spongepowered.api.world.schematic.BlockPaletteTypes.LOCAL) : GlobalPalette.getBlockPalette(), start, size);
    }

    public ArrayMutableBlockBuffer(Palette<BlockState> palette, Vector3i start, Vector3i size) {
        super(start, size);
        this.palette = palette;
        int airId = palette.getOrAssign(AIR);

        int dataSize = area();
        this.data = new PackedBackingData(dataSize, palette.getHighestId());

        // all blocks default to air
        if (airId != 0) {
            for (int i = 0; i < dataSize; i++) {
                this.data.set(i, airId);
            }
        }
    }

    public ArrayMutableBlockBuffer(Palette<BlockState> palette, Vector3i start, Vector3i size, char[] blocks) {
        super(start, size);
        this.palette = palette;
        this.data = new CharBackingData(blocks);
    }

    /**
     * Does not clone!
     *
     * @param palette The palette
     * @param blocks The backing data
     * @param start The start block position
     * @param size The block size
     */
    ArrayMutableBlockBuffer(Palette<BlockState> palette, BackingData blocks, Vector3i start, Vector3i size) {
        super(start, size);
        this.palette = palette;
        this.data = blocks;
    }

    @Override
    public Palette<BlockState> getPalette() {
        return this.palette;
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block) {
        checkRange(x, y, z);
        int id = this.palette.getOrAssign(block);
        if (id > this.data.getMax()) {

            int highId = this.palette.getHighestId();
            int dataSize = area();
            BackingData newdata;
            if (highId * 2 > GlobalPalette.getBlockPalette().getHighestId()) {
                // we are only saving about 1 bit at this point, so transition to a global palette
                Palette<BlockState> newpalette = GlobalPalette.getBlockPalette();
                id = newpalette.getOrAssign(block);
                highId = newpalette.getHighestId();

                newdata = new PackedBackingData(dataSize, highId);
                for (int i = 0; i < dataSize; i++) {
                    newdata.set(i, newpalette.getOrAssign(this.palette.get(this.data.get(i)).orElse(AIR)));
                }
                this.palette = newpalette;
            } else {

                newdata = new PackedBackingData(dataSize, highId);
                for (int i = 0; i < dataSize; i++) {
                    newdata.set(i, this.data.get(i));
                }
            }
            this.data = newdata;
        }
        this.data.set(getIndex(x, y, z), id);
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
    public MutableBlockVolumeWorker<? extends MutableBlockVolume> getBlockWorker() {
        return new SpongeMutableBlockVolumeWorker<>(this);
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return new UnmodifiableBlockVolumeWrapper(this);
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        switch (type) {
            case STANDARD:
                return new ArrayMutableBlockBuffer(this.palette, this.data.copyOf(), this.start, this.size);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return new ArrayImmutableBlockBuffer(this.palette, this.data.copyOf(), this.start, this.size);
    }

    private int area() {
        return this.size.getX() * this.size.getY() * this.size.getZ();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ArrayMutableBlockBuffer that = (ArrayMutableBlockBuffer) o;
        return this.palette.equals(that.palette) &&
               this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.palette, this.data);
    }

    /**
     * Basically a fixed length list of non negative numbers/ids.
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

        /**
         * Gets the maximum id supported by this BackingData
         */
        int getMax();
    }

    static class CharBackingData implements BackingData {

        private final char[] data;

        public CharBackingData(char[] data) {
            this.data = data;
        }

        @Override
        public int get(int index) {
            return this.data[index];
        }

        @Override
        public void set(int index, int val) {
            this.data[index] = (char) val;
        }

        @Override
        public BackingData copyOf() {
            return new CharBackingData(this.data.clone());
        }

        @Override
        public int getMax() {
            return Character.MAX_VALUE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CharBackingData that = (CharBackingData) o;
            return Arrays.equals(this.data, that.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.data);
        }
    }

    static class PackedBackingData implements BackingData {

        /** A long array used to store the packed values */
        private long[] longArray;
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
        public PackedBackingData(int size, int highestValue) {
            this.arraySize = size;
            int bits;
            for (bits = 0; 1 << bits <= highestValue; bits++);
            this.bits = bits;

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
            int bitIndex = index * this.bits;
            int longIndex = bitIndex / Long.SIZE;
            int bitOffset = bitIndex % Long.SIZE;

            this.longArray[longIndex] = this.longArray[longIndex] & ~(this.maxValue << bitOffset) | (long) value << bitOffset;

            if (bitOffset + this.bits > Long.SIZE) {
                // The entry is split between two longs (lets call them left long, and right long)
                int bitsInLeft = Long.SIZE - bitOffset;
                int bitsInRight = this.bits - bitsInLeft;
                longIndex++;
                this.longArray[longIndex] = this.longArray[longIndex] >>> bitsInRight << bitsInRight | (long) value >> bitsInLeft;
            }
        }

        @Override
        public int get(int index) {
            int bitIndex = index * this.bits;
            int longIndex = bitIndex / Long.SIZE;
            int rightLongIndex = (bitIndex + this.bits - 1) / 64;
            int bitOffset = bitIndex % 64;

            if (bitOffset + this.bits > Long.SIZE) {
                // The entry is split between two longs
                int bitsInLeft = Long.SIZE - bitOffset;
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
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PackedBackingData that = (PackedBackingData) o;
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
