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
package org.spongepowered.common.mixin.api.minecraft.world.level.chunk;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.api.world.HeightTypes;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.api.world.chunk.ChunkState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin_API<P extends Chunk<P>> implements Chunk<P>, LevelHeightAccessor {

    // @formatter:off
    @Shadow public abstract ChunkStatus shadow$getStatus();
    @Shadow public abstract void shadow$addEntity(net.minecraft.world.entity.Entity entity);
    @Shadow public abstract void shadow$setInhabitedTime(long var1);
    @Shadow public abstract long shadow$getInhabitedTime();
    @Shadow public abstract ChunkPos shadow$getPos();
    @Shadow public abstract int shadow$getHeight(Heightmap.Types var1, int var2, int var3);
    @Shadow public abstract LevelChunkSection shadow$getSection(int p_187657_);
    @Shadow public abstract void shadow$setUnsaved(boolean p_62094_);
    // @formatter:on

    @Override
    public void addEntity(final Entity entity) {
        this.shadow$addEntity((net.minecraft.world.entity.Entity) entity);
    }

    @Override
    public ChunkState state() {
        return (ChunkState) this.shadow$getStatus();
    }

    @Override
    public boolean isEmpty() {
        return this.shadow$getStatus() == ChunkStatus.EMPTY;
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        return VolumeStreamUtils.setBiomeOnNativeChunk(x, y, z, biome, () -> this.shadow$getSection(this.getSectionIndex(y)), () -> this.shadow$setUnsaved(true));
    }

    @Override
    public Ticks inhabitedTime() {
        return new SpongeTicks(this.shadow$getInhabitedTime());
    }

    @Override
    public void setInhabitedTime(final Ticks newInhabitedTime) {
        this.shadow$setInhabitedTime(newInhabitedTime.ticks());
    }

    @Override
    public Vector3i chunkPosition() {
        final ChunkPos chunkPos = this.shadow$getPos();
        return new Vector3i(chunkPos.x, 0, chunkPos.z);
    }

    @Override
    public boolean contains(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    @Override
    public boolean isAreaAvailable(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    @Override
    public int highestYAt(final int x, final int z) {
        return this.shadow$getHeight((Heightmap.Types) (Object) HeightTypes.WORLD_SURFACE.get(), x, z);
    }

    @Override
    public int height(final HeightType type, final int x, final int z) {
        return this.shadow$getHeight((Heightmap.Types) (Object) Objects.requireNonNull(type, "type"), x, z);
    }

    @Override
    public ScheduledUpdateList<BlockType> scheduledBlockUpdates() {
        throw new MissingImplementationException("ChunkAccess", "scheduledBlockUpdates");
    }

    @Override
    public ScheduledUpdateList<FluidType> scheduledFluidUpdates() {
        throw new MissingImplementationException("ChunkAccess", "scheduledFluidUpdates");
    }


}
