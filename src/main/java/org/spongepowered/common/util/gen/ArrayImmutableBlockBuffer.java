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
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.BlockVolumeWorker;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer.BackingData;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer.CharBackingData;
import org.spongepowered.common.world.extent.ImmutableBlockViewDownsize;
import org.spongepowered.common.world.extent.ImmutableBlockViewTransform;
import org.spongepowered.common.world.extent.worker.SpongeBlockVolumeWorker;
import org.spongepowered.common.world.schematic.GlobalPalette;

import java.util.Objects;

public class ArrayImmutableBlockBuffer extends AbstractBlockBuffer implements ImmutableBlockVolume {

    @SuppressWarnings("ConstantConditions")
    private static final BlockState AIR = BlockTypes.AIR.getDefaultState();

    private final Palette<BlockState> palette;
    private final BackingData data;

    /**
     * Does not clone!
     *  @param palette The palette
     * @param data The backing data
     * @param start The start block position
     * @param size The block size
     */
    ArrayImmutableBlockBuffer(Palette<BlockState> palette, BackingData data, Vector3i start, Vector3i size) {
        super(start, size);
        this.data = data;
        this.palette = palette;
    }

    public ArrayImmutableBlockBuffer(Palette<BlockState> palette, Vector3i start, Vector3i size, char[] blocks) {
        super(start, size);
        this.data = new CharBackingData(blocks.clone());
        this.palette = palette;
    }

    @Override
    public Palette<BlockState> getPalette() {
        return GlobalPalette.getBlockPalette();
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkRange(x, y, z);
        return this.palette.get(this.data.get(getIndex(x, y, z))).orElse(AIR);
    }

    @Override
    public ImmutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        checkRange(newMin.getX(), newMin.getY(), newMin.getZ());
        checkRange(newMax.getX(), newMax.getY(), newMax.getZ());
        return new ImmutableBlockViewDownsize(this, newMin, newMax);
    }

    @Override
    public ImmutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return new ImmutableBlockViewTransform(this, transform);
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return this;
    }

    @Override
    public BlockVolumeWorker<? extends ImmutableBlockVolume> getBlockWorker() {
        return new SpongeBlockVolumeWorker<>(this);
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

    /**
     * This method doesn't clone the array passed into it. INTERNAL USE ONLY.
     * Make sure your code doesn't leak the reference if you're using it.
     *
     * @param blocks The blocks to store
     * @param start The start of the volume
     * @param size The size of the volume
     * @return A new buffer using the same array reference
     */
    public static ImmutableBlockVolume newWithoutArrayClone(Palette<BlockState> palette, Vector3i start, Vector3i size, char[] blocks) {
        return new ArrayImmutableBlockBuffer(palette, new CharBackingData(blocks), start, size);
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
        ArrayImmutableBlockBuffer that = (ArrayImmutableBlockBuffer) o;
        return this.palette.equals(that.palette) &&
               this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.palette, this.data);
    }
}
