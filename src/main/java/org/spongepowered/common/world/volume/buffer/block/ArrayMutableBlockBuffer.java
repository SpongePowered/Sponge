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

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.volume.block.MutableBlockVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.MutableBimapPalette;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3i;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ArrayMutableBlockBuffer extends AbstractBlockBuffer implements MutableBlockVolume<ArrayMutableBlockBuffer> {

    /**
     * If the area is lower than this amount, a global palette will be used.
     * <p>
     * Example: If its only a 4 block area why allocate a local palette that
     * by its self will take up more space in memory than it saves.
     */
    private static final int SMALL_AREA_THRESHOLD = 256;

    private static final BlockState AIR = BlockTypes.AIR.get().getDefaultState();

    private Palette.Mutable<BlockState> palette;
    private BackingData data;

    public ArrayMutableBlockBuffer(final Vector3i start, final Vector3i size) {
        this(size.getX() * size.getY() * size.getZ() > ArrayMutableBlockBuffer.SMALL_AREA_THRESHOLD
            ?
             new MutableBimapPalette<>(PaletteTypes.BLOCK_STATE_PALETTE.get()) : GlobalPalette.getBlockPalette(), start, size);
    }

    public ArrayMutableBlockBuffer(final Palette<BlockState> palette, final Vector3i start, final Vector3i size) {
        super(start, size);
        final Palette.Mutable<BlockState> mutablePalette = palette.asMutable();
        this.palette = mutablePalette;
        final int airId = mutablePalette.getOrAssign(ArrayMutableBlockBuffer.AIR);

        final int dataSize = this.area();
        this.data = new PackedBackingData(dataSize, palette.getHighestId());

        // all blocks default to air
        if (airId != 0) {
            for (int i = 0; i < dataSize; i++) {
                this.data.set(i, airId);
            }
        }
    }

    public ArrayMutableBlockBuffer(final Palette<BlockState> palette, final Vector3i start, final Vector3i size, final char[] blocks) {
        super(start, size);
        this.palette = palette.asMutable();
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
    ArrayMutableBlockBuffer(final Palette<BlockState> palette, final BackingData blocks, final Vector3i start, final Vector3i size) {
        super(start, size);
        this.palette = palette.asMutable();
        this.data = blocks;
    }

    @Override
    public Palette<BlockState> getPalette() {
        return this.palette;
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        this.checkRange(x, y, z);
        int id = this.palette.getOrAssign(block);
        if (id > this.data.getMax()) {

            int highId = this.palette.getHighestId();
            final int dataSize = this.area();
            final BackingData newdata;
            if (highId * 2 > GlobalPalette.getBlockPalette().getHighestId()) {
                // we are only saving about 1 bit at this point, so transition to a global palette
                final Palette.Mutable<BlockState> newpalette = GlobalPalette.getBlockPalette().asMutable();
                id = newpalette.getOrAssign(block);
                highId = newpalette.getHighestId();

                newdata = new PackedBackingData(dataSize, highId);
                for (int i = 0; i < dataSize; i++) {
                    newdata.set(i, newpalette.getOrAssign(this.palette.get(this.data.get(i)).orElse(ArrayMutableBlockBuffer.AIR)));
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
        this.data.set(this.getIndex(x, y, z), id);
        return true;
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        return this.setBlock(x, y, z, BlockTypes.AIR.get().getDefaultState());
    }

    @Override
    public BlockState getBlock(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        return this.palette.get(this.data.get(this.getIndex(x, y, z))).orElse(ArrayMutableBlockBuffer.AIR);
    }

    @Override
    public FluidState getFluid(final int x, final int y, final int z) {
        return this.getBlock(x, y, z).getFluidState();
    }

    @Override
    public int getHighestYAt(final int x, final int z) {
        return 0;
    }

    private int area() {
        return this.size.getX() * this.size.getY() * this.size.getZ();
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ArrayMutableBlockBuffer that = (ArrayMutableBlockBuffer) o;
        return this.palette.equals(that.palette) &&
               this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.palette, this.data);
    }

    @Override
    public VolumeStream<ArrayMutableBlockBuffer, BlockState> getBlockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        final Vector3i blockMin = this.getBlockMin();
        final Vector3i blockMax = this.getBlockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final ArrayMutableBlockBuffer buffer;
        if (options.carbonCopy()) {
            buffer = new ArrayMutableBlockBuffer(this.palette, this.data.copyOf(), this.start, this.size);
        } else {
            buffer = this;
        }
        final Stream<VolumeElement<ArrayMutableBlockBuffer, BlockState>> stateStream = IntStream.range(blockMin.getX(), blockMax.getX() + 1)
            .mapToObj(x -> IntStream.range(blockMin.getZ(), blockMax.getZ() + 1)
                .mapToObj(z -> IntStream.range(blockMin.getY(), blockMax.getY() + 1)
                    .mapToObj(y -> VolumeElement.of(this, () -> buffer.getBlock(x, y, z), new Vector3i(x, y, z)))
                ).flatMap(Function.identity())
            ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    public void setBlock(final BlockPos pos, final net.minecraft.block.BlockState blockState) {
        this.setBlock(pos.getX(), pos.getY(), pos.getZ(), (BlockState) blockState);
    }

    public net.minecraft.block.BlockState getBlock(final BlockPos blockPos) {
        return (net.minecraft.block.BlockState) this.getBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public ArrayMutableBlockBuffer copy() {
        return  new ArrayMutableBlockBuffer(this.palette, this.data.copyOf(), this.start, this.size);
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
        public BackingData copyOf() {
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

    static class PackedBackingData implements BackingData {

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
            this.longArray = new long[MathHelper.roundUp(size * bits, Long.SIZE) / Long.SIZE];
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
