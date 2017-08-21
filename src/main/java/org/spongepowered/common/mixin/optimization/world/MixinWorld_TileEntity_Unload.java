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
package org.spongepowered.common.mixin.optimization.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;

import java.util.List;

@Mixin(value = World.class, priority = 1001)
public abstract class MixinWorld_TileEntity_Unload implements IMixinWorld {

    private it.unimi.dsi.fastutil.longs.LongCollection tileEntitiesChunkToBeRemoved = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();

    @Shadow private boolean processingLoadedTiles;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> loadedTileEntityList;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> tickableTileEntities;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> tileEntitiesToBeRemoved;

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Lorg/spongepowered/common/event/tracking/CauseTracker;switchToPhase(Lorg/spongepowered/common/event/tracking/IPhaseState;Lorg/spongepowered/common/event/tracking/PhaseContext;)V"), remap = false)
    public void onUnloadTileEntitiesStart(CallbackInfo ci) {
        this.processingLoadedTiles = true;
        this.removeTileEntitiesForRemovedChunks();
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Lorg/spongepowered/common/event/tracking/CauseTracker;completePhase(Lorg/spongepowered/common/event/tracking/IPhaseState;)V"), remap = false)
    public void onUnloadTileEntitiesEnd(CallbackInfo ci) {
        this.processingLoadedTiles = false;
    }

    @Override
    public void markTileEntitiesInChunkForRemoval(net.minecraft.world.chunk.Chunk chunk)
    {
        if (!chunk.getTileEntityMap().isEmpty())
        {
            long pos = net.minecraft.util.math.ChunkPos.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
            this.tileEntitiesChunkToBeRemoved.add(pos);
        }
    }

    private void removeTileEntitiesForRemovedChunks()
    {
        if (!this.tileEntitiesChunkToBeRemoved.isEmpty())
        {
            java.util.function.Predicate<net.minecraft.tileentity.TileEntity> isInChunk = (tileEntity) ->
            {
                BlockPos tilePos = tileEntity.getPos();
                long tileChunkPos = net.minecraft.util.math.ChunkPos.chunkXZ2Int(tilePos.getX() >> 4, tilePos.getZ() >> 4);
                return this.tileEntitiesChunkToBeRemoved.contains(tileChunkPos);
            };
            java.util.function.Predicate<net.minecraft.tileentity.TileEntity> isInChunkDoUnload = (tileEntity) ->
            {
                boolean inChunk = isInChunk.test(tileEntity);
                if (inChunk)
                {
                    SpongeImplHooks.onTileEntityChunkUnload(tileEntity);
                    ((IMixinTileEntity) tileEntity).setActiveChunk(null);
                }
                return inChunk;
            };
            this.tickableTileEntities.removeIf(isInChunk);
            this.loadedTileEntityList.removeIf(isInChunkDoUnload);
            this.tileEntitiesChunkToBeRemoved.clear();
        }
    }
}
