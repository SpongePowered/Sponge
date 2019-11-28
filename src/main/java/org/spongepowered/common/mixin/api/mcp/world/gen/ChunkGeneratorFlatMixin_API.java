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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.feature.Structure;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(FlatChunkGenerator.class)
public class ChunkGeneratorFlatMixin_API implements GenerationPopulator {

    @Shadow @Final private net.minecraft.block.BlockState[] cachedBlockIDs;
    @Shadow @Final private Map<String, Structure> structureGenerators;
    @Shadow @Final private boolean hasDecoration;
    @Shadow @Final private boolean hasDungeons;
    @Shadow @Final private FlatGeneratorInfo flatWorldGenInfo;

    @Override
    public void populate(final World world, final MutableBlockVolume buffer, final ImmutableBiomeVolume biomes) {
        int x;
        int z;
        final Vector3i min = buffer.getBlockMin();
        for (int y = 0; y < this.cachedBlockIDs.length; ++y) {
            final int y0 = min.getY() + y;
            final net.minecraft.block.BlockState iblockstate = this.cachedBlockIDs[y];
            if (iblockstate != null) {
                for (x = 0; x < 16; ++x) {
                    final int x0 = min.getX() + x;
                    for (z = 0; z < 16; ++z) {
                        buffer.setBlock(x0, y0, min.getZ() + z, (BlockState) iblockstate);
                    }
                }
            }
        }
    }

}
