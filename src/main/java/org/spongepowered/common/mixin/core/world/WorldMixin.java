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
package org.spongepowered.common.mixin.core.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.tileentity.TileEntityTypeBridge;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin implements WorldBridge, IWorld {

    @Shadow @Final public boolean isRemote;
    @Shadow @Final protected WorldInfo worldInfo;
    @Shadow @Final public Dimension dimension;

    @Shadow public abstract Chunk shadow$getChunkAt(BlockPos pos);
    @Shadow public abstract Chunk shadow$getChunk(int chunkX, int chunkZ);


    @Shadow public abstract void func_217390_a(Consumer<Entity> p_217390_1_, Entity p_217390_2_);

    @Shadow @Final public Random rand;
    private boolean impl$isDefinitelyFake = false;
    private boolean impl$hasChecked = false;

    @Override
    public boolean bridge$isFake() {
        if (this.impl$hasChecked) {
            return this.impl$isDefinitelyFake;
        }
        this.impl$isDefinitelyFake = this.isRemote || this.worldInfo == null || this.worldInfo.getWorldName() == null || !(this instanceof ServerWorldBridge);
        this.impl$hasChecked = true;
        return this.impl$isDefinitelyFake;
    }

    @Override
    public void bridge$clearFakeCheck() {
        this.impl$hasChecked = false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean bridge$isAreaLoaded(final int xStart, final int yStart, final int zStart, final int xEnd, final int yEnd, final int zEnd, final boolean allowEmpty) {
        return this.isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd);
    }

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"), cancellable = true)
    public void onDestroyBlock(final BlockPos pos, final boolean dropBlock, final CallbackInfoReturnable<Boolean> cir) {

    }

    @Redirect(method = "addTileEntity",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/World;tickableTileEntities:Ljava/util/List;"),
                           to =   @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z")))
    private boolean onAddTileEntity(final List<? super TileEntity> list, final Object tile) {
        if (!this.bridge$isFake() && !this.canTileUpdate((TileEntity) tile)) {
            return false;
        }

        return list.add((TileEntity) tile);
    }

    @SuppressWarnings("ConstantConditions")
    private boolean canTileUpdate(final TileEntity tile) {
        final org.spongepowered.api.block.entity.BlockEntity spongeTile = (org.spongepowered.api.block.entity.BlockEntity) tile;
        return spongeTile.getType() == null || ((TileEntityTypeBridge) spongeTile.getType()).bridge$canTick();
    }

    /**
     * @author gabizou - January 10th, 2020 - Minecraft 1.14.3
     * @reason We introduce the protected method to be overridden in
     * {@code org.spongepowered.common.mixin.core.world.server.ServerWorldMixin#impl$wrapTileEntityTick(ITickableTileEntity)}
     * to appropriately wrap where needed.
     *
     * @param tileEntity The tile entity
     */
    @Redirect(method = "func_217391_K",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/ITickableTileEntity;tick()V"))
    protected void impl$wrapTileEntityTick(final ITickableTileEntity tileEntity) {
        tileEntity.tick();
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link BlockPosBridge}.
     *
     * @param pos The position
     * @return The block state at the desired position
     */
    @Overwrite
    public BlockState getBlockState(final BlockPos pos) {
        // Sponge - Replace with inlined method
        // if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((BlockPosBridge) pos).bridge$isInvalidYPosition()) {
            // Sponge end
            return Blocks.VOID_AIR.getDefaultState();
        }
        final IChunk chunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk.getBlockState(pos);
    }

}
