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
package org.spongepowered.common.mixin.optimization.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer_Lighting extends MixinWorld_Lighting {

    /**
     * @author gabizou - April 8th, 2016
     *
     * Instead of providing chunks which has potential chunk loads,
     * we simply get the chunk directly from the chunk provider, if it is loaded
     * and return the light value accordingly.
     *
     * @param pos The block position
     * @return The light at the desired block position
     */
    @Override
    public int getLight(BlockPos pos) {
        if (pos.getY() < 0) {
            return 0;
        } else {
            if (pos.getY() >= 256) {
                pos = new BlockPos(pos.getX(), 255, pos.getZ());
            }
            // Sponge Start - Use our hook to get the chunk only if it is loaded
            // return this.getChunkFromBlockCoords(pos).getLightSubtracted(pos, 0);
            final Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk == null ? 0 : chunk.getLightSubtracted(pos, 0);
            // Sponge End
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Overrides the same method from MixinWorld_Lighting that redirects
     * {@link #isAreaLoaded(BlockPos, int, boolean)} to simplify the check to
     * whether the chunk's neighbors are loaded. Since the passed radius is always
     * 17, the check is simply checking for whether neighboring chunks are loaded
     * properly.
     *
     * @param thisWorld This world
     * @param pos The block position to check light for
     * @param radius The radius, always 17
     * @param allowEmtpy Whether to allow empty chunks, always false
     * @param lightType The light type
     * @param samePosition The block position to check light for, again.
     * @return True if the chunk is loaded and neighbors are loaded
     */
    @Override
    protected boolean spongeIsAreaLoadedForCheckingLight(World thisWorld, BlockPos pos, int radius, boolean allowEmtpy, EnumSkyBlock lightType,
            BlockPos samePosition) {
        final Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
        return !(chunk == null || !((IMixinChunk) chunk).areNeighborsLoaded());
    }

    /**
     * @author gabizou - April 8th, 2016
     *
     * Rewrites the chunk accessor to get only a chunk if it is loaded.
     * This avoids loading chunks from file or generating new chunks
     * if the chunk didn't exist, when the only function of this method is
     * to get the light for the given block position.
     *
     * @param pos The block position
     * @param checkNeighbors Whether to check neighboring block lighting
     * @return The light value at the block position, if the chunk is loaded
     */
    @Override
    public int getLight(BlockPos pos, boolean checkNeighbors) {
        if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000) {
            if (checkNeighbors && this.getBlockState(pos).mth_000889_f()()) {
                int i1 = this.getLight(pos.up(), false);
                int i = this.getLight(pos.east(), false);
                int j = this.getLight(pos.west(), false);
                int k = this.getLight(pos.south(), false);
                int l = this.getLight(pos.north(), false);

                if (i > i1) {
                    i1 = i;
                }

                if (j > i1) {
                    i1 = j;
                }

                if (k > i1) {
                    i1 = k;
                }

                if (l > i1) {
                    i1 = l;
                }

                return i1;
            } else if (pos.getY() < 0) {
                return 0;
            } else {
                if (pos.getY() >= 256) {
                    pos = new BlockPos(pos.getX(), 255, pos.getZ());
                }

                // Sponge - Gets only loaded chunks, unloaded chunks will not get loaded to check lighting
                // Chunk chunk = this.getChunkFromBlockCoords(pos);
                // return chunk.getLightSubtracted(pos, this.skylightSubtracted);
                final Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
                return chunk == null ? 0 : chunk.getLightSubtracted(pos, this.getSkylightSubtracted());
                // Sponge End
            }
        } else {
            return 15;
        }
    }
}
