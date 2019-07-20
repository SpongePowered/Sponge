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

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;

@Mixin(value = World.class, priority = 1500)
public abstract class WorldMixin_UseActiveChunkForCollisions {

    @Shadow public boolean isFlammableWithin(final AxisAlignedBB bb) { return false; } // shadow

    @Shadow private boolean isAreaLoaded(
        final int xStart, final int yStart, final int zStart, final int xEnd, final int yEnd, final int zEnd, final boolean allowEmpty) { return false; } // SHADOW

    @Redirect(method = "isFlammableWithin", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(IIIIIIZ)Z"))
    private boolean activeCollision$IgnoreIsAreaLoaded(final World world, final int xStart, final int yStart, final int zStart,
        final int xEnd, final int yEnd, final int zEnd, final boolean allowEmpty) {
        return true;
    }

    @Inject(method = "handleMaterialAcceleration", at = @At("HEAD"), cancellable = true)
    private void activeCollision$BailIfNeighborsAreInactive(final AxisAlignedBB bb, final Material materialIn, final Entity entityIn,
        final CallbackInfoReturnable<Boolean> cir) {
        final ChunkBridge activeChunk = ((ActiveChunkReferantBridge) entityIn).bridge$getActiveChunk();
        if (activeChunk == null || activeChunk.bridge$isQueuedForUnload() || !activeChunk.bridge$areNeighborsLoaded()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "handleMaterialAcceleration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(IIIIIIZ)Z"))
    private boolean activeCollision$IgnoreAreaIsLoaded(final World world, final int xStart, final int yStart, final int zStart,
        final int xEnd, final int yEnd, final int zEnd, final boolean allowEmpty) {
        if (((WorldBridge) this).bridge$isFake()) {
            return this.isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd, allowEmpty);
        }
        return true;
    }

}
