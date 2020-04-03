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
package org.spongepowered.common.mixin.core.tileentity;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;

@Mixin(HopperTileEntity.class)
public abstract class HopperTileEntityMixin extends LockableLootTileEntityMixin {

    @Inject(method = "captureItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/ItemEntity;getItem()Lnet/minecraft/item/ItemStack;"))
    private static void impl$trackNotifierWhenTransferring(final IInventory inventory, final ItemEntity entityItem,
        final CallbackInfoReturnable<Boolean> callbackInfo) {
        if (entityItem instanceof OwnershipTrackedBridge) {
            ((OwnershipTrackedBridge) entityItem).tracked$getOwnerReference().ifPresent(owner -> {
                if (inventory instanceof ActiveChunkReferantBridge && inventory instanceof TileEntity) {
                    final TileEntity te = (TileEntity) inventory;
                    final ChunkBridge spongeChunk = ((ActiveChunkReferantBridge) inventory).bridge$getActiveChunk();
                    spongeChunk.bridge$addTrackedBlockPosition(te.getBlockType(), te.getPos(), owner, PlayerTracker.Type.NOTIFIER);
                }
            });
        }
    }


}
