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

import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.generation.GenerationChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.SpongeTicks;

@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin_API extends ChunkAccess implements GenerationChunk {

    // @formatter:off
    @Shadow private volatile ChunkStatus status;
    // @formatter:on

    public ProtoChunkMixin_API(
        final ChunkPos $$0, final UpgradeData $$1, final LevelHeightAccessor $$2, final Registry<net.minecraft.world.level.biome.Biome> $$3, final long $$4,
        final LevelChunkSection[] $$5, final BlendingData $$6
    ) {
        super($$0, $$1, $$2, $$3, $$4, $$5, $$6);
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        // TODO ChunkBiomeContainerAccessor is dead
        // return VolumeStreamUtils.setBiomeOnNativeChunk(x, y, z, biome, () -> (ChunkBiomeContainerAccessor) this.biomes, () -> {});
        return false;
    }

    @Override
    public Ticks inhabitedTime() {
        return new SpongeTicks(this.getInhabitedTime());
    }

    @Override
    public void setInhabitedTime(final Ticks newInhabitedTime) {
        this.setInhabitedTime(newInhabitedTime.ticks());
    }

    @Override
    public boolean isEmpty() {
        return this.status == ChunkStatus.EMPTY;
    }

}
