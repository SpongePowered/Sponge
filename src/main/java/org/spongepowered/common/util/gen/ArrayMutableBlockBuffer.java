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

public class ArrayMutableBlockBuffer extends AbstractBlockBuffer implements MutableBlockVolume {

    @SuppressWarnings("ConstantConditions")
    private static final BlockState AIR = BlockTypes.AIR.getDefaultState();

    private final BlockPalette palette;
    private final BackingData data;

    public ArrayMutableBlockBuffer(BlockPalette palette, Vector3i start, Vector3i size, BackingDataType type) {
        super(start, size);
        this.palette = palette;
        this.data = type.create(size);
    }

    public ArrayMutableBlockBuffer(BlockPalette palette, Vector3i start, Vector3i size, byte[] blocks) {
        super(start, size);
        this.palette = palette;
        this.data = new ByteBackingData(blocks);
    }

    public ArrayMutableBlockBuffer(BlockPalette palette, Vector3i start, Vector3i size, char[] blocks) {
        super(start, size);
        this.palette = palette;
        this.data = new CharBackingData(blocks);
    }

    public ArrayMutableBlockBuffer(BlockPalette palette, Vector3i start, Vector3i size, int[] blocks) {
        super(start, size);
        this.palette = palette;
        this.data = new IntBackingData(blocks);
    }

    ArrayMutableBlockBuffer(BlockPalette palette, Vector3i start, Vector3i size, BackingData blocks) {
        super(start, size);
        this.palette = palette;
        this.data = blocks.copyOf();
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
                return new ArrayMutableBlockBuffer(this.palette, this.start, this.size, this.data);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return new ArrayImmutableBlockBuffer(this.palette, this.start, this.size, this.data);
    }

    public static enum BackingDataType {
        BYTE {

            @Override
            public BackingData create(Vector3i size) {
                return new ByteBackingData(size.getX() * size.getY() * size.getZ());
            }
        },
        CHAR {

            @Override
            public BackingData create(Vector3i size) {
                return new CharBackingData(size.getX() * size.getY() * size.getZ());
            }
        },
        INT {

            @Override
            public BackingData create(Vector3i size) {
                return new IntBackingData(size.getX() * size.getY() * size.getZ());
            }
        };

        public abstract BackingData create(Vector3i size);
    }

    static interface BackingData {

        Object getBacking();

        int get(int index);

        void set(int index, int val);

        BackingData copyOf();

    }

    static class ByteBackingData implements BackingData {

        private final byte[] data;

        public ByteBackingData(byte[] data) {
            this.data = data;
        }

        public ByteBackingData(int len) {
            this.data = new byte[len];
        }

        @Override
        public Object getBacking() {
            return this.data;
        }

        @Override
        public int get(int index) {
            return this.data[index] & 0xFF;
        }

        @Override
        public void set(int index, int val) {
            this.data[index] = (byte) val;
        }

        @Override
        public BackingData copyOf() {
            return new ByteBackingData(this.data.clone());
        }
    }

    static class CharBackingData implements BackingData {

        private final char[] data;

        public CharBackingData(char[] data) {
            this.data = data;
        }

        public CharBackingData(int len) {
            this.data = new char[len];
        }

        @Override
        public Object getBacking() {
            return this.data;
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

    static class IntBackingData implements BackingData {

        private final int[] data;

        public IntBackingData(int[] data) {
            this.data = data;
        }

        public IntBackingData(int len) {
            this.data = new int[len];
        }

        @Override
        public Object getBacking() {
            return this.data;
        }

        @Override
        public int get(int index) {
            return this.data[index];
        }

        @Override
        public void set(int index, int val) {
            this.data[index] = val;
        }

        @Override
        public BackingData copyOf() {
            return new IntBackingData(this.data.clone());
        }
    }
}
