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
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.bridge.world.WorldBridge;

import java.util.Random;
import java.util.function.Consumer;

@Mixin(World.class)
public abstract class WorldMixin_Tracker implements WorldBridge {

    @Shadow @Final public Random rand;
    @Shadow @Final protected WorldInfo worldInfo;

    @Shadow public abstract Chunk shadow$getChunk(int chunkX, int chunkZ);
    @Shadow public abstract Chunk shadow$getChunkAt(BlockPos pos);
    @Shadow public abstract void func_217390_a(Consumer<Entity> p_217390_1_, Entity p_217390_2_);
    @Shadow public abstract boolean setBlockState(BlockPos pos, BlockState state, int flags);
    @Shadow public static boolean shadow$isOutsideBuildHeight(final BlockPos pos) {
        throw new UnsupportedOperationException("Untransformed shadow");
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
        final IChunk chunk = this.shadow$getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk.getBlockState(pos);
    }

}
