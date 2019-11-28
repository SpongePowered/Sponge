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
package org.spongepowered.common.world.gen;

import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;
import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGenerator;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;

import javax.annotation.Nullable;

/**
 * Generator populator that wraps a Minecraft {@link IChunkGenerator}.
 */
public final class SpongeGenerationPopulator implements GenerationPopulator, TimingBridge {

    private final ChunkGenerator chunkGenerator;
    private final World world;
    @Nullable private Timing timing;
    @Nullable private Chunk cachedChunk = null;

    /**
     * Gets the {@link GenerationPopulator} from the given
     * {@link IChunkGenerator}. If the chunk generator wraps a
     * {@link GenerationPopulator}, that populator is returned, otherwise the
     * chunk generator is wrapped.
     *
     * @param world The world the chunk generator is bound to.
     * @param chunkGenerator The chunk generator.
     * @return The generator populator.
     */
    public static GenerationPopulator of(final World world, final ChunkGenerator chunkGenerator) {
        if (WorldGenConstants.isValid(chunkGenerator, GenerationPopulator.class)) {
            return (GenerationPopulator) chunkGenerator;
        }
        if (chunkGenerator instanceof SpongeChunkGenerator) {
            return ((SpongeChunkGenerator) chunkGenerator).getBaseGenerationPopulator();
        }
        return new SpongeGenerationPopulator(world, chunkGenerator);
    }

    private SpongeGenerationPopulator(final World world, final ChunkGenerator chunkGenerator) {
        this.world = checkNotNull(world, "world");
        this.chunkGenerator = checkNotNull(chunkGenerator, "chunkGenerator");
    }

    @Override
    public void populate(final org.spongepowered.api.world.World world, final MutableBlockVolume buffer, final ImmutableBiomeVolume biomes) {
        this.bridge$getTimingsHandler().startTimingIfSync();
        final Vector3i min = buffer.getBlockMin();
        final Vector3i max = buffer.getBlockMax();

        // The block buffer can be of any size. We generate all chunks that
        // have at least part of the chunk in the given area, and copy the
        // needed blocks into the buffer
        final int minChunkX = GenericMath.floor(min.getX() / 16.0);
        final int minChunkZ = GenericMath.floor(min.getZ() / 16.0);
        final int maxChunkX = GenericMath.floor(max.getX() / 16.0);
        final int maxChunkZ = GenericMath.floor(max.getZ() / 16.0);

        WorldGenConstants.disableLighting();
        if (minChunkX == maxChunkX && minChunkZ == maxChunkZ) {
            this.cachedChunk = this.chunkGenerator.generateChunk(minChunkX, minChunkZ);
            this.placeChunkInBuffer(this.cachedChunk, buffer, minChunkX, minChunkZ);
        } else {
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    final Chunk generated = this.chunkGenerator.generateChunk(chunkX, chunkZ);
                    this.placeChunkInBuffer(generated, buffer, chunkX, chunkZ);
                }
            }
        }
        WorldGenConstants.enableLighting();

        this.bridge$getTimingsHandler().stopTimingIfSync();
    }

    private void placeChunkInBuffer(final Chunk chunk, final MutableBlockVolume buffer, final int chunkX, final int chunkZ) {
        // Calculate bounds
        final int xOffset = chunkX * 16;
        final int zOffset = chunkZ * 16;
        final Vector3i minBound = buffer.getBlockMin();
        final Vector3i maxBound = buffer.getBlockMax();
        final int xInChunkStart = Math.max(0, minBound.getX() - xOffset);
        final int yStart = Math.max(0, minBound.getY());
        final int zInChunkStart = Math.max(0, minBound.getZ() - zOffset);
        final int xInChunkEnd = Math.min(15, maxBound.getX() - xOffset);
        final int yEnd = Math.min(255, maxBound.getY());
        final int zInChunkEnd = Math.min(15, maxBound.getZ() - zOffset);

        // Copy the right blocks in
        final ExtendedBlockStorage[] blockStorage = chunk.getSections();
        for (final ExtendedBlockStorage miniChunk : blockStorage) {
            if (miniChunk == null) {
                continue;
            }

            final int yOffset = miniChunk.getYLocation();
            final int yInChunkStart = Math.max(0, yStart);
            final int yInChunkEnd = Math.min(15, yEnd);
            for (int xInChunk = xInChunkStart; xInChunk <= xInChunkEnd; xInChunk++) {
                for (int yInChunk = yInChunkStart; yInChunk <= yInChunkEnd; yInChunk++) {
                    for (int zInChunk = zInChunkStart; zInChunk <= zInChunkEnd; zInChunk++) {
                        buffer.setBlock(xOffset + xInChunk, yOffset + yInChunk, zOffset + zInChunk,
                                (BlockState) miniChunk.getBlockState(xInChunk, yInChunk, zInChunk));
                    }
                }
            }
        }
    }

    public Chunk getCachedChunk() {
        return this.cachedChunk;
    }

    public ChunkGenerator getChunkGenerator() {
        return this.chunkGenerator;
    }

    public void clearCachedChunk() {
        this.cachedChunk = null;
    }

    /**
     * Gets the {@link IChunkGenerator}, if the target world matches the world this chunk generator is bound to.
     *
     * @param targetWorld The target world.
     * @return The chunk generator.
     * @throws IllegalArgumentException If the target world is not the world
     *         this chunk provider is bound to.`
     */
    public ChunkGenerator getHandle(final World targetWorld) {
        if (!this.world.equals(targetWorld)) {
            throw new IllegalArgumentException("Cannot reassign internal generator from world "
                    + getWorldName(this.world) + " to world " + getWorldName(targetWorld));
        }
        return this.chunkGenerator;
    }

    private static String getWorldName(final World world) {
        return ((org.spongepowered.api.world.World) world).getName();
    }

    @Override
    public Timing bridge$getTimingsHandler() {
        if (this.timing == null) {
            String modId = SpongeImplHooks.getModIdFromClass(this.chunkGenerator.getClass());
            if (!modId.equals("")) {
                modId = modId + ":";
            }
            this.timing = SpongeTimingsFactory.ofSafe("chunkGenerator - " + modId + this.chunkGenerator.getClass().getName());
        }
        return this.timing;
    }
}
