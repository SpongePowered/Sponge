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

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;

@Mixin(World.class)
public abstract class MixinWorld_Lighting {

    private static final String WORLD_GET_CHUNK =
            "Lnet/minecraft/world/World;getChunkFromBlockCoords(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/Chunk;";

    @Shadow protected IChunkProvider chunkProvider;

    // To be overridden in MixinWorldServer_Lighting
    @Shadow public abstract int getLight(BlockPos pos);
    @Shadow public abstract int getLight(BlockPos pos, boolean checkNeighbors);

    @Shadow @Final public boolean isRemote;
    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract int getSkylightSubtracted();

    /**
     * Adds an injection check on the World where if the chunk is not loaded
     * at the desired location, the chunk is not loaded. Note that in the
     * case of {@link WorldServer}, this is bypassed entirely to use
     * a more optimized chunk loaded check.
     *
     * @param pos The position of the block
     * @param callbackInfo The callback info to return and cancel if necessary
     */
    @Inject(method = "getLight(Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = WORLD_GET_CHUNK), cancellable = true)
    private void checkChunkLoadedForLight(BlockPos pos, CallbackInfoReturnable<Integer> callbackInfo) {
        if (!this.isBlockLoaded(pos)) {
            callbackInfo.setReturnValue(0);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "checkLightFor", at = @At(value = "HEAD"), cancellable = true)
    public void onCheckLightFor(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!this.isRemote) {
            final Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getChunkIfLoaded(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null || !((IMixinChunk) chunk).areNeighborsLoaded()) {
                callbackInfo.setReturnValue(false);
                callbackInfo.cancel();
            }
        }
    }
}
