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
package org.spongepowered.common.mixin.tracker.world.level;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.world.WorldBridge;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class LevelMixin_Tracker implements WorldBridge {

    // @formatter:off
    @Shadow @Final public Random random;
    @Shadow @Final protected WritableLevelData levelData;
    @Shadow protected boolean updatingBlockEntities;
    @Shadow @Final protected List<BlockEntity> pendingBlockEntities;
    @Shadow @Final public List<BlockEntity> blockEntityList;
    @Shadow @Final public List<BlockEntity> tickableBlockEntities;

    @Shadow public abstract LevelChunk shadow$getChunk(int chunkX, int chunkZ);
    @Shadow public abstract ChunkAccess shadow$getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull);
    @Shadow public abstract LevelChunk shadow$getChunkAt(BlockPos pos);
    @Shadow public abstract void shadow$guardEntityTick(Consumer<Entity> p_217390_1_, Entity p_217390_2_);
    @Shadow public boolean setBlock(final BlockPos pos, final BlockState state, final int flags, final int limit) { throw new IllegalStateException("Untransformed shadow!"); }
    @Shadow public void shadow$removeBlockEntity(final BlockPos pos) { } // shadowed
    @Shadow public boolean shadow$addBlockEntity(final BlockEntity tile) { return false; }
    @Shadow @Nullable public abstract BlockEntity shadow$getBlockEntity(BlockPos pos);
    @Shadow public void shadow$setBlockEntity(final BlockPos pos, @Nullable final BlockEntity tileEntity) { } // Shadowed
    @Shadow public void shadow$neighborChanged(final BlockPos pos, final Block blockIn, final BlockPos fromPos) { } // Shadowed
    @Shadow public abstract BlockState shadow$getBlockState(BlockPos pos);
    @Shadow public abstract boolean shadow$isDebug();
    @Shadow public boolean destroyBlock(final BlockPos p_241212_1_, final boolean p_241212_2_, @Nullable final Entity p_241212_3_, final int p_241212_4_) { throw new IllegalStateException("Untransformed shadow!"); }
    @Shadow public abstract FluidState shadow$getFluidState(BlockPos p_204610_1_);
    // @formatter:on


    /**
     * We introduce the protected method to be overridden in
     * {@code org.spongepowered.common.mixin.core.world.server.ServerWorldMixin#tracker$wrapTileEntityTick(ITickableTileEntity)}
     * to appropriately wrap where needed.
     *
     * @param tileEntity The tile entity
     * @author gabizou - January 10th, 2020 - Minecraft 1.14.3
     */
    @Redirect(method = "tickBlockEntities",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/TickableBlockEntity;tick()V"))
    protected void tracker$wrapBlockEntityTick(final TickableBlockEntity tileEntity) {
        tileEntity.tick();
    }

    @Redirect(method = "addBlockEntity",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false),
        slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;tickableBlockEntities:Ljava/util/List;"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z")))
    private boolean tracker$onlyAddTileEntitiesToTickIfEnabled(final List<? super BlockEntity> list, final Object tile) {
        if (!this.bridge$isFake() && !((TrackableBridge) tile).bridge$shouldTick()) {
            return false;
        }

        return list.add((BlockEntity) tile);
    }

}
