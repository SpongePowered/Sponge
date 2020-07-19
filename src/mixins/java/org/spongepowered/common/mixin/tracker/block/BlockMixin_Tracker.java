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
package org.spongepowered.common.mixin.tracker.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;

@Mixin(Block.class)
public abstract class BlockMixin_Tracker {

    @Shadow public static void shadow$spawnDrops(BlockState state, World worldIn, BlockPos pos) {
        throw new IllegalStateException("untransformed shadow");
    }

    /**
     * @author gabizou - July 23rd, 2019 - 1.12
     * @reason Because adding a few redirects for the massive if
     * statement is less performant than doing the fail fast check
     * of "is main thread or are we restoring", before we reach the
     * {@link net.minecraft.world.World#isRemote} check or
     * {@link ItemStack#isEmpty()} check, we can eliminate a larger
     * majority of the hooks that would otherwise be required for
     * doing an overwrite of this method.
     *
     * @param worldIn The world
     * @param pos The position
     * @param stack The stack
     * @param ci Callbackinfo to cancel if we're not on the main thread or we're restoring
     */
    @Inject(method = "spawnAsEntity", at = @At("HEAD"), cancellable = true)
    private static void tracker$checkMainThreadAndRestoring(final net.minecraft.world.World worldIn, final BlockPos pos, final ItemStack stack,
                                                         final CallbackInfo ci) {
        if (!PhaseTracker.SERVER.onSidedThread() || PhaseTracker.SERVER.getCurrentState().isRestoring()) {
            ci.cancel();
        }
    }

    // TODO - Ask Mumfrey about matching these two methods, both having the same signature/name but one with additional parameters.
    @Inject(method = "spawnDrops(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void checkBlockDropForTransactions(final BlockState state, final net.minecraft.world.World worldIn, final BlockPos pos, final TileEntity tileEntityIn, final CallbackInfo ci) {
        if (((WorldBridge) worldIn).bridge$isFake()) {
            return;
        }
        final SpongeProxyBlockAccess proxyAccess = ((TrackedWorldBridge) worldIn).bridge$getProxyAccess();
        if (proxyAccess.hasProxy() && proxyAccess.isProcessingTransactionWithNextHavingBreak(pos, state)) {
            ci.cancel();
        }
    }


    @Inject(method = "spawnAsEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/ItemEntity;setDefaultPickupDelay()V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true)
    private static void tracker$attemptCaptureOrAllowSpawn(final net.minecraft.world.World worldIn, final BlockPos pos, final ItemStack stack,
                                                        final CallbackInfo ci, final float unused, final double xOffset, final double yOffset, final double zOffset,
                                                        final ItemEntity toSpawn) {
        // Sponge Start - Tell the phase state to track this position, and then unset it.
        final PhaseContext<?> context = PhaseTracker.SERVER.getPhaseContext();

        if (context.allowsBulkEntityCaptures() && context.allowsBlockPosCapturing()) {
            context.getCaptureBlockPos().setPos(pos);
            worldIn.addEntity(toSpawn);
            context.getCaptureBlockPos().setPos(null);
            ci.cancel();
        }
    }

}
