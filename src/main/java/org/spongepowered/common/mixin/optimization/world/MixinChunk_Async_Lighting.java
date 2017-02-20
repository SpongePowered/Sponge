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
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = Chunk.class)
public abstract class MixinChunk_Async_Lighting implements IMixinChunk {

    public AtomicInteger pendingLightUpdates = new AtomicInteger();
    public long lightUpdateTime;

    @Shadow @Final private World world;
    @Shadow private boolean isTerrainPopulated;
    @Shadow private boolean isLightPopulated;
    @Shadow private boolean chunkTicked;

    @Shadow public abstract void recheckGaps(boolean isClient);

    @Override
    public AtomicInteger getPendingLightUpdates() {
        return this.pendingLightUpdates;
    }

    @Override
    public long getLightUpdateTime() {
        return this.lightUpdateTime;
    }

    @Override
    public void setLightUpdateTime(long time) {
        this.lightUpdateTime = time;
    }

    @Redirect(method = "updateSkylightNeighborHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkLightFor(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onCheckLightForSkylight(World world, EnumSkyBlock enumSkyBlock, BlockPos pos) {
        if (world.isRemote) {
            return world.checkLightFor(enumSkyBlock, pos);
        }

        return ((IMixinWorldServer) world).updateLightAsync(enumSkyBlock, pos);
    }

    @Inject(method = "recheckGaps", at = @At("HEAD"), cancellable = true)
    private void onRecheckGaps(boolean isClient, CallbackInfo ci) {
        if (!this.world.isRemote) {
            ((IMixinWorldServer) this.world).getLightingExecutor().execute(() -> {
                this.recheckGaps(isClient);
            });
            ci.cancel();
        }
    }

    /**
     * @author blood - February 20th, 2017
     * @reason Since lighting updates run async, we need to always return true to send client updates.
     *
     * @param pos The position to get the light for
     * @return Whether block position can see sky
     */
    @Overwrite
    public boolean isPopulated() {
        if (this.world.isRemote) {
            return this.chunkTicked && this.isTerrainPopulated && this.isLightPopulated;
        }

        return true;
    }
}
