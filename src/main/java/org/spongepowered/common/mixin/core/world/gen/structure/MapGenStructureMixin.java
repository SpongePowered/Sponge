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
package org.spongepowered.common.mixin.core.world.gen.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.feature.Structure;
import net.minecraft.world.gen.feature.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;

import java.util.Random;

@Mixin(Structure.class)
public abstract class MapGenStructureMixin extends MapGenBase {

    private static boolean impl$isGeneratingStructures = false;

    @Shadow protected Long2ObjectMap<StructureStart> structureMap;
    @Shadow protected abstract void initializeStructureData(World worldIn);
    @Shadow protected abstract void setStructureStart(int chunkX, int chunkZ, StructureStart start);


    /**
     * @author blood - October 22nd, 2016
     * @reason Prevents CME's by avoiding recursive calls while generating structures
     *
     * @param worldIn The world
     * @param randomIn The rand
     * @param chunkCoord The chunk position
     * @return true if generation was successful
     */
    @Overwrite
    public synchronized boolean generateStructure(final World worldIn, final Random randomIn, final ChunkPos chunkCoord) {
        if (impl$isGeneratingStructures) {
            return false;
        }
        final Chunk chunk = ((ChunkProviderBridge) worldIn.getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(chunkCoord.x, chunkCoord.z);
        if (chunk == null) {
            return false;
        }

        this.initializeStructureData(worldIn);
        final int i = (chunkCoord.x << 4) + 8;
        final int j = (chunkCoord.z << 4) + 8;
        boolean flag = false;

        impl$isGeneratingStructures = true;
        for (final StructureStart structurestart : this.structureMap.values())
        {
            if (structurestart.isValid() && structurestart.func_175788_a(chunkCoord) && structurestart.getBoundingBox().intersectsWith(i, j, i + 15, j + 15))
            {
                structurestart.generateStructure(worldIn, randomIn, new MutableBoundingBox(i, j, i + 15, j + 15));
                structurestart.func_175787_b(chunkCoord);
                flag = true;
                this.setStructureStart(structurestart.getChunkPosX(), structurestart.getChunkPosZ(), structurestart);
            }
        }
        impl$isGeneratingStructures = false;

        return flag;
    }
}
