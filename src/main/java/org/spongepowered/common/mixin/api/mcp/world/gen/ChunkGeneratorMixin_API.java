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
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorNether;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.gen.ChunkBufferPrimer;

import java.util.Random;

@Mixin(ChunkGeneratorNether.class)
public abstract class ChunkGeneratorMixin_API implements GenerationPopulator {

    @Shadow @Final private Random rand;
    @Shadow public abstract void prepareHeights(int p_180515_1_, int p_180515_2_, ChunkPrimer p_180515_3_);
    @Shadow public abstract void buildSurfaces(int p_180515_1_, int p_180515_2_, ChunkPrimer p_180515_3_);

    @Override
    public void populate(final World world, final MutableBlockVolume buffer, final ImmutableBiomeVolume biomes) {
        final int x = GenericMath.floor(buffer.getBlockMin().getX() / 16f);
        final int z = GenericMath.floor(buffer.getBlockMin().getZ() / 16f);
        final ChunkPrimer chunkprimer = new ChunkBufferPrimer(buffer);
        this.rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        this.prepareHeights(x, z, chunkprimer);
        this.buildSurfaces(x, z, chunkprimer);
    }

}
