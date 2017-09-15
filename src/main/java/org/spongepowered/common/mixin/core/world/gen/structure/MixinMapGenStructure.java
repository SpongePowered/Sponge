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

import com.flowpowered.math.vector.Vector3i;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.mixin.core.world.gen.MixinMapGenBase;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.Random;

/**
 * This mixin is making MapGenStructure be a populator as well as a
 * generationpopulator as the structures are called both from the generation
 * phase and the population phase of chunk creation.
 */
@Mixin(MapGenStructure.class)
public abstract class MixinMapGenStructure extends MixinMapGenBase implements Populator {

    private static boolean generatingStructures = false;

    @Shadow protected Long2ObjectMap<StructureStart> structureMap;
    @Shadow protected abstract void initializeStructureData(World worldIn);
    @Shadow public abstract void setStructureStart(int chunkX, int chunkZ, StructureStart start);

    @Override
    public PopulatorType getType() {
        return InternalPopulatorTypes.STRUCTURE;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        World world = (World) worldIn;
        generateStructure(world, random, new ChunkPos((min.getX() - 8) / 16, (min.getZ() - 8) / 16));
    }

    /**
     * @author blood - October 22nd, 2016
     * @reason Prevents CME's by avoiding recursive calls while generating structures
     *
     * @param worldIn The world
     * @param randomIn The rand
     * @Param chunkCoord The chunk position
     * @return true if generation was successful
     */
    @Overwrite
    public synchronized boolean generateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
    {
        if (generatingStructures) {
            return false;
        }
        Chunk chunk = ((IMixinChunkProviderServer) worldIn.getChunkProvider()).getLoadedChunkWithoutMarkingActive(chunkCoord.x, chunkCoord.z);
        if (chunk == null) {
            return false;
        }

        this.initializeStructureData(worldIn);
        int i = (chunkCoord.x << 4) + 8;
        int j = (chunkCoord.z << 4) + 8;
        boolean flag = false;

        generatingStructures = true;
        for (StructureStart structurestart : this.structureMap.values())
        {
            if (structurestart.isSizeableStructure() && structurestart.isValidForPostProcess(chunkCoord) && structurestart.getBoundingBox().intersectsWith(i, j, i + 15, j + 15))
            {
                structurestart.generateStructure(worldIn, randomIn, new StructureBoundingBox(i, j, i + 15, j + 15));
                structurestart.notifyPostProcessAt(chunkCoord);
                flag = true;
                this.setStructureStart(structurestart.getChunkPosX(), structurestart.getChunkPosZ(), structurestart);
            }
        }
        generatingStructures = false;

        return flag;
    }
}
