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
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

/**
 * Base class for block buffers that are exactly one chunk in size.
 *
 */
public abstract class AbstractChunkBuffer implements MutableBlockVolume {

    private final int chunkX;
    private final int chunkZ;

    private final Vector3i maxBlock;
    private final Vector3i minBlock;

    protected AbstractChunkBuffer(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        final Optional<Vector3i> worldCoords = SpongeChunkLayout.instance.toWorld(chunkX, 0, chunkZ);
        Preconditions.checkArgument(worldCoords.isPresent(), "Chunk coordinates are not valid" + chunkX + ", " + chunkZ);
        this.minBlock = worldCoords.get();
        this.maxBlock = this.minBlock.add(SpongeChunkLayout.CHUNK_SIZE).sub(Vector3i.ONE);
    }

    protected void checkRange(int x, int y, int z) {
        if ((x >> 4) != this.chunkX || (z >> 4) != this.chunkZ || (y >> 8) != 0) {
            throw new IndexOutOfBoundsException("Outside chunk: " + new Vector3i(x, y, z)
                    + " is outside chunk (" + this.chunkX + "," + this.chunkZ
                    + "), containing blocks " + this.minBlock + " to " + this.maxBlock);
        }
    }

    @Override
    public Vector3i getBlockMax() {
        return this.maxBlock;
    }

    @Override
    public Vector3i getBlockMin() {
        return this.minBlock;
    }

    @Override
    public Vector3i getBlockSize() {
        return SpongeChunkLayout.CHUNK_SIZE;
    }

    @Override
    public boolean containsBlock(Vector3i position) {
        return containsBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.minBlock, this.maxBlock);
    }

    @Override
    public BlockState getBlock(Vector3i position) {
        return getBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public void setBlock(Vector3i position, BlockState block) {
        setBlock(position.getX(), position.getY(), position.getZ(), block);
    }

    @Override
    public BlockType getBlockType(Vector3i position) {
        return getBlockType(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public void setBlockType(Vector3i position, BlockType type) {
        setBlockType(position.getX(), position.getY(), position.getZ(), type);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        return getBlock(x, y, z).getType();
    }

    @Override
    public void setBlockType(int x, int y, int z, BlockType type) {
        setBlock(x, y, z, type.getDefaultState());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("min", this.getBlockMin())
                .add("max", this.getBlockMax())
                .toString();
    }

}
