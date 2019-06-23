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
package org.spongepowered.common.mixin.tracking.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ServerChunkProviderBridge;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(value = net.minecraft.world.World.class, priority = 1111)
public abstract class MixinWorld_Tracker implements World {

    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunk(BlockPos pos);
    @Shadow public abstract IChunkProvider getChunkProvider();

    @Override
    public Optional<UUID> getCreator(int x, int y, int z) {
        final Chunk chunk = ((ServerChunkProviderBridge) getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(x >> 4, z >> 4);
        if (chunk == null) {
            return Optional.empty();
        }

        BlockPos pos = new BlockPos(x, y, z);
        // The difference here saves the user lookup check for snapshot creation, very hot when considering
        // blocks changing with potentially n block notifiers and n block owners.
        return ((ChunkBridge) chunk).getBlockOwnerUUID(pos);
    }

    @Override
    public Optional<UUID> getNotifier(int x, int y, int z) {
        final Chunk chunk = ((ServerChunkProviderBridge) getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(x >> 4, z >> 4);
        if (chunk == null) {
            return Optional.empty();
        }

        BlockPos pos = new BlockPos(x, y, z);
        // The difference here saves the user lookup check for snapshot creation, very hot when considering
        // blocks changing with potentially n block notifiers and n block owners.
        return ((ChunkBridge) chunk).getBlockNotifierUUID(pos);
    }

    @Override
    public void setCreator(int x, int y, int z, @Nullable UUID uuid) {
        final Chunk chunk = ((ServerChunkProviderBridge) getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(x >> 4, z >> 4);
        if (chunk == null) {
            return;
        }

        BlockPos pos = new BlockPos(x, y, z);
        ((ChunkBridge) chunk).setBlockCreator(pos, uuid);
    }

    @Override
    public void setNotifier(int x, int y, int z, @Nullable UUID uuid) {
        final Chunk chunk = ((ServerChunkProviderBridge) getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(x >> 4, z >> 4);
        if (chunk == null) {
            return;
        }

        BlockPos pos = new BlockPos(x, y, z);
        ((ChunkBridge) chunk).setBlockNotifier(pos, uuid);
    }

}
