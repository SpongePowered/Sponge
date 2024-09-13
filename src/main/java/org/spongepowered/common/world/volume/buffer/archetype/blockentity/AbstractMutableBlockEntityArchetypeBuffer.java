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
package org.spongepowered.common.world.volume.buffer.archetype.blockentity;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.volume.archetype.block.entity.BlockEntityArchetypeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.bridge.world.level.block.state.BlockStateBridge;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.block.AbstractBlockBuffer;
import org.spongepowered.common.world.volume.buffer.block.ArrayMutableBlockBuffer;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractMutableBlockEntityArchetypeBuffer extends AbstractBlockBuffer implements BlockEntityArchetypeVolume.Mutable {

    // This is our backing block buffer
    private final ArrayMutableBlockBuffer blockBuffer;

    protected AbstractMutableBlockEntityArchetypeBuffer(final Vector3i start, final Vector3i size) {
        super(start, size);
        this.blockBuffer = new ArrayMutableBlockBuffer(start, size);
    }

    protected AbstractMutableBlockEntityArchetypeBuffer(final ArrayMutableBlockBuffer buffer) {
        super(buffer.min(), buffer.size());
        this.blockBuffer = buffer;
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        if (this.blockBuffer.setBlock(x, y, z, block)) {
            if (block instanceof BlockStateBridge && ((BlockStateBridge) block).bridge$hasTileEntity()) {
                this.removeBlockEntity(x, y, z);
            }
        }
        return false;
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        if (this.blockBuffer.removeBlock(x, y, z)) {
            this.removeBlockEntity(x, y, z);
        }
        return false;
    }


    @Override
    public Palette<BlockState, BlockType> blockPalette() {
        return this.blockBuffer.blockPalette();
    }

    @Override
    public BlockState block(final int x, final int y, final int z) {
        return this.blockBuffer.block(x, y, z);
    }

    @Override
    public FluidState fluid(final int x, final int y, final int z) {
        return this.blockBuffer.fluid(x, y, z);
    }

    @Override
    public int highestYAt(final int x, final int z) {
        return this.blockBuffer.highestYAt(x, z);
    }

    @Override
    public VolumeStream<BlockEntityArchetypeVolume.Mutable, BlockState> blockStateStream(final Vector3i min, final Vector3i max,
        final StreamOptions options) {
        VolumeStreamUtils.validateStreamArgs(min, max, this.min(), this.max(), options);
        final ArrayMutableBlockBuffer buffer;
        if (options.carbonCopy()) {
            buffer = this.blockBuffer.copy();
        } else {
            buffer = this.blockBuffer;
        }
        final Stream<VolumeElement<BlockEntityArchetypeVolume.Mutable, BlockState>> stateStream = IntStream.rangeClosed(min.x(), max.x())
            .mapToObj(x -> IntStream.rangeClosed(min.z(), max.z())
                .mapToObj(z -> IntStream.rangeClosed(min.y(), max.y())
                    .mapToObj(y -> VolumeElement.of((BlockEntityArchetypeVolume.Mutable) this, () -> buffer.block(x, y, z), new Vector3d(x, y, z)))
                ).flatMap(Function.identity())
            ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }
}
