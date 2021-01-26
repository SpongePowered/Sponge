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
package org.spongepowered.common.mixin.api.mcp.world.level.chunk;

import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.chunk.ChunkBiomeContainerAccessor;

@Mixin(net.minecraft.world.level.chunk.LevelChunk.class)
public abstract class LevelChunkMixin_API implements Chunk {

    //@formatter:off
    @Shadow private ChunkBiomeContainer biomes;
    @Shadow private long inhabitedTime;
    //@formatter:on

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        final net.minecraft.world.level.biome.Biome[] biomes = ((ChunkBiomeContainerAccessor) this.biomes).accessor$biomes();

        int maskedX = x & ChunkBiomeContainer.HORIZONTAL_MASK;
        int maskedY = Mth.clamp(y, 0, ChunkBiomeContainer.VERTICAL_MASK);
        int maskedZ = z & ChunkBiomeContainer.HORIZONTAL_MASK;

        final int WIDTH_BITS = ChunkBiomeContainerAccessor.accessor$WIDTH_BITS();
        final int posKey = maskedY << WIDTH_BITS + WIDTH_BITS | maskedZ << WIDTH_BITS | maskedX;
        biomes[posKey] = (net.minecraft.world.level.biome.Biome) (Object) biome;

        return true;
    }

    @Override
    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    // TODO implement the rest of it
}
