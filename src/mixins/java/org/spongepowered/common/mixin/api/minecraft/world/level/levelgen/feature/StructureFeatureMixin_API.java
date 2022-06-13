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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen.feature;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

@Mixin(Structure.class)
public abstract class StructureFeatureMixin_API implements org.spongepowered.api.world.generation.structure.Structure {

    @Override
    public boolean place(final ServerWorld world, final Vector3i pos) {
        // see PlaceCommand#placeStructure
        final ServerLevel level = (ServerLevel) world;
        final ServerChunkCache chunkSource = level.getChunkSource();
        final StructureStart start = ((Structure) (Object) this).generate(level.registryAccess(), chunkSource.getGenerator(), chunkSource.getGenerator().getBiomeSource(),
                        chunkSource.randomState(), level.getStructureManager(), level.getSeed(), new ChunkPos(VecHelper.toBlockPos(pos)), 0, level, b -> true);

        if (!start.isValid()) {
            return false;
        }

        final BoundingBox bb = start.getBoundingBox();
        ChunkPos minPos = new ChunkPos(SectionPos.blockToSectionCoord(bb.minX()), SectionPos.blockToSectionCoord(bb.minZ()));
        ChunkPos maxPos = new ChunkPos(SectionPos.blockToSectionCoord(bb.maxX()), SectionPos.blockToSectionCoord(bb.maxZ()));
        if (ChunkPos.rangeClosed(minPos, maxPos).anyMatch(($$1x) -> !level.isLoaded($$1x.getWorldPosition()))) {
            return false;
        }
        ChunkPos.rangeClosed(minPos, maxPos).forEach((chunkPos) -> start.placeInChunk(level, level.structureManager(), chunkSource.getGenerator(), level.getRandom(),
                new BoundingBox(chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), level.getMaxBuildHeight(), chunkPos.getMaxBlockZ()), chunkPos));
        return true;
    }

    @Override
    public boolean place(final ServerLocation location) {
        return this.place(location.world(), location.blockPosition());
    }

}
