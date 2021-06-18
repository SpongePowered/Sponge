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

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.chunk.ChunkState;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.ChunkUtil;

import javax.annotation.Nullable;

@Mixin(ChunkAccess.class)
public interface ChunkAccessMixin_API<P extends ProtoChunk<P>> extends ProtoChunk<P> {

    // @formatter:on
    @Shadow ChunkStatus shadow$getStatus();
    @Shadow @Nullable ChunkBiomeContainer shadow$getBiomes();
    @Shadow void shadow$addEntity(net.minecraft.world.entity.Entity entity);
    // @formatter:off

    @Override
    default void addEntity(final Entity entity) {
        this.shadow$addEntity((net.minecraft.world.entity.Entity) entity);
    }

    @Override
    default ChunkState state() {
        return (ChunkState) this.shadow$getStatus();
    }

    @Override
    default boolean isEmpty() {
        return this.shadow$getStatus() == ChunkStatus.EMPTY;
    }

    @Override
    default boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        return ChunkUtil.setBiome(this.shadow$getBiomes(), x, y, z, biome);
    }

}
