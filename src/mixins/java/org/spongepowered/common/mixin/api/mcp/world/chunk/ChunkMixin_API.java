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
package org.spongepowered.common.mixin.api.mcp.world.chunk;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.biome.BiomeContainerAccessor;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin_API implements Chunk {

    @Shadow private BiomeContainer blockBiomeArray;
    @Shadow private long inhabitedTime;

    @Override
    public boolean setBiome(final int x, final int y, final int z, final BiomeType biome) {
        final Biome[] biomes = ((BiomeContainerAccessor) this.blockBiomeArray).accessor$biomes();

        int maskedX = x & BiomeContainer.HORIZONTAL_MASK;
        int maskedY = MathHelper.clamp(y, 0, BiomeContainer.VERTICAL_MASK);
        int maskedZ = z & BiomeContainer.HORIZONTAL_MASK;

        final int WIDTH_BITS = BiomeContainerAccessor.accessor$WIDTH_BITS();
        final int posKey = maskedY << WIDTH_BITS + WIDTH_BITS | maskedZ << WIDTH_BITS | maskedX;
        biomes[posKey] = (Biome) biome;

        return true;
    }

    @Override
    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    // TODO implement the rest of it
}
