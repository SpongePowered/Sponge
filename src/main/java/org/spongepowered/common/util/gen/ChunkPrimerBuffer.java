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

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.common.world.extent.MutableBlockViewDownsize;
import org.spongepowered.common.world.extent.MutableBlockViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBlockVolumeWrapper;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.Optional;

/**
 * Makes a {@link ChunkPrimer} usable as a {@link MutableBlockVolume}.
 *
 */
@NonnullByDefault
public final class ChunkPrimerBuffer extends AbstractBlockBuffer implements MutableBlockVolume {

    private final ChunkPrimer chunkPrimer;

    public ChunkPrimerBuffer(ChunkPrimer chunkPrimer, int chunkX, int chunkZ) {
        super(getBlockStart(chunkX, chunkZ), SpongeChunkLayout.CHUNK_SIZE);
        this.chunkPrimer = chunkPrimer;
    }

    private static Vector3i getBlockStart(int chunkX, int chunkZ) {
        final Optional<Vector3i> worldCoords = SpongeChunkLayout.instance.toWorld(chunkX, 0, chunkZ);
        checkArgument(worldCoords.isPresent(), "Chunk coordinates are not valid" + chunkX + ", " + chunkZ);
        return worldCoords.get();
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkRange(x, y, z);
        return (BlockState) this.chunkPrimer.getBlockState(x & 0xf, y, z & 0xf);
    }

    @Override
    public void setBlock(Vector3i position, BlockState block) {
        setBlock(position.getX(), position.getY(), position.getZ(), block);
    }

    @Override
    public void setBlockType(Vector3i position, BlockType type) {
        setBlockType(position.getX(), position.getY(), position.getZ(), type);
    }

    @Override
    public void setBlockType(int x, int y, int z, BlockType type) {
        setBlock(x, y, z, type.getDefaultState());
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        checkRange(x, y, z);
        this.chunkPrimer.setBlockState(x & 0xf, y, z & 0xF, (IBlockState) block);
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
    public MutableBlockVolume getRelativeBlockView() {
        return getBlockView(DiscreteTransform3.fromTranslation(this.start.negate()));
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return new UnmodifiableBlockVolumeWrapper(this);
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        switch (type) {
            case STANDARD:
                return new ShortArrayMutableBlockBuffer(this.chunkPrimer.data.clone(), this.start, this.size);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return new ShortArrayImmutableBlockBuffer(this.chunkPrimer.data, this.start, this.size);
    }
}
