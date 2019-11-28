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
package org.spongepowered.common.mixin.api.mcp.world.gen;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.OverworldChunkGenerator;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.ChunkGeneratorOverworldBridge;
import org.spongepowered.common.util.gen.ChunkBufferPrimer;

import java.util.Random;

import javax.annotation.Nullable;

@Mixin(OverworldChunkGenerator.class)
public abstract class ChunkGeneratorOverworldMixin_API
    implements GenerationPopulator {

    @Shadow @Final private Random rand;
    @Shadow @Nullable private Biome[] biomesForGeneration;

    @Shadow public abstract void setBlocksInChunk(int p_180518_1_, int p_180518_2_, ChunkPrimer p_180518_3_);

    @Override
    public void populate(final World world, final MutableBlockVolume buffer, final ImmutableBiomeVolume biomes) {
        final int x = GenericMath.floor(buffer.getBlockMin().getX() / 16f);
        final int z = GenericMath.floor(buffer.getBlockMin().getZ() / 16f);
        this.rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        this.biomesForGeneration = ((ChunkGeneratorOverworldBridge) this).bridge$getBiomesForGeneration(x, z);
        final ChunkPrimer chunkprimer = new ChunkBufferPrimer(buffer);
        this.setBlocksInChunk(x, z, chunkprimer);
        apiImpl$setBedrock(buffer);
    }

    private void apiImpl$setBedrock(final MutableBlockVolume buffer) {
        final Vector3i min = buffer.getBlockMin();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                final int x0 = min.getX() + x;
                final int z0 = min.getZ() + z;
                for (int y = 0; y < 6; y++) {
                    final int y0 = min.getY() + y;
                    if (y <= this.rand.nextInt(5)) {
                        buffer.setBlock(x0, y0, z0, BlockTypes.BEDROCK.getDefaultState());
                    }
                }
            }
        }
    }

}
