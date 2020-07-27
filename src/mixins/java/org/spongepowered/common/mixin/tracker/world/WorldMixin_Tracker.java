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
package org.spongepowered.common.mixin.tracker.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.tileentity.TileEntityTypeBridge;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.bridge.world.WorldBridge;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Mixin(World.class)
public abstract class WorldMixin_Tracker implements WorldBridge {

    @Shadow @Final public Random rand;
    @Shadow @Final protected WorldInfo worldInfo;

    @Shadow public abstract Chunk shadow$getChunk(int chunkX, int chunkZ);
    @Shadow public abstract Chunk shadow$getChunkAt(BlockPos pos);
    @Shadow public abstract void shadow$guardEntityTick(Consumer<Entity> p_217390_1_, Entity p_217390_2_);
    @Shadow public boolean setBlockState(final BlockPos pos, final BlockState state, final int flags) { throw new IllegalStateException("Untransformed shadow!"); }

    /**
     * @author gabizou - January 10th, 2020 - Minecraft 1.14.3
     * @reason We introduce the protected method to be overridden in
     * {@code org.spongepowered.common.mixin.core.world.server.ServerWorldMixin#tracker$wrapTileEntityTick(ITickableTileEntity)}
     * to appropriately wrap where needed.
     *
     * @param tileEntity The tile entity
     */
    @Redirect(method = "tickBlockEntities",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/ITickableTileEntity;tick()V"))
    protected void tracker$wrapTileEntityTick(final ITickableTileEntity tileEntity) {
        tileEntity.tick();
    }

    @Redirect(method = "addTileEntity",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false),
        slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/World;tickableTileEntities:Ljava/util/List;"),
            to =   @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z")))
    private boolean tracker$onlyAddTileEntitiesToTickIfEnabled(final List<? super TileEntity> list, final Object tile) {
        if (!this.bridge$isFake() && !((TrackableBridge) tile).bridge$shouldTick()) {
            return false;
        }

        return list.add((TileEntity) tile);
    }

}
