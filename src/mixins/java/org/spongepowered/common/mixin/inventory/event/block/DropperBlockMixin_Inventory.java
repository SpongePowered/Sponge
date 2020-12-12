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
package org.spongepowered.common.mixin.inventory.event.block;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.dispenser.ProxyBlockSource;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;

import javax.annotation.Nullable;

@Mixin(DropperBlock.class)
public abstract class DropperBlockMixin_Inventory {

    @Inject(method = "dispenseFrom", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/DispenserTileEntity;setItem(ILnet/minecraft/item/ItemStack;)V"))
    private void afterDispense(final ServerWorld worldIn, final BlockPos pos, final CallbackInfo callbackInfo,
            final ProxyBlockSource proxyblocksource, final DispenserTileEntity dispensertileentity, final int i, final ItemStack itemstack,
            final Direction direction, final IInventory iinventory, final ItemStack itemstack1) {
        // after setItem
        dispensertileentity.setItem(i, itemstack1);

        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            // Transfer worked if remainder is one less than the original stack
            if (itemstack1.getCount() == itemstack.getCount() - 1) {
                final TrackedInventoryBridge capture = DropperBlockMixin_Inventory.impl$forCapture(dispensertileentity);
                final Inventory sourceInv = ((Inventory) dispensertileentity);
                SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, sourceInv, i, itemstack);
                InventoryEventFactory.callTransferPost(capture, sourceInv, ((Inventory) iinventory), itemstack, sourceSlotTransaction);
            }
        }

        // dont call setItem twice
        callbackInfo.cancel();
    }

    @Surrogate
    private void afterDispense(final ServerWorld worldIn, final BlockPos pos, final CallbackInfo callbackInfo,
            final ProxyBlockSource proxyblocksource, final DispenserTileEntity dispensertileentity, final int i, final ItemStack itemstack,
            final ItemStack itemstack1) {
        // after setItem
        dispensertileentity.setItem(i, itemstack1);

        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            // Transfer worked if remainder is one less than the original stack
            if (itemstack1.getCount() == itemstack.getCount() - 1) {
                final TrackedInventoryBridge capture = DropperBlockMixin_Inventory.impl$forCapture(dispensertileentity);
                final Inventory sourceInv = ((Inventory) dispensertileentity);
                SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, sourceInv, i, itemstack);
                final Direction enumfacing = worldIn.getBlockState(pos).getValue(DispenserBlock.FACING);
                final BlockPos blockpos = pos.relative(enumfacing);
                final IInventory iinventory = HopperTileEntity.getContainerAt(worldIn, blockpos.getX(), blockpos.getY(), blockpos.getZ());
                InventoryEventFactory.callTransferPost(capture, sourceInv, ((Inventory) iinventory), itemstack, sourceSlotTransaction);
            }
        }

        // dont call setInventorySlotContents twice
        callbackInfo.cancel();
    }

    @Inject(method = "dispenseFrom", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/HopperTileEntity;addItem(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Direction;)Lnet/minecraft/item/ItemStack;"))
    private void onDispense(final ServerWorld world, final BlockPos pos, final CallbackInfo ci,
            final ProxyBlockSource proxyblocksource, final DispenserTileEntity dispensertileentity, final int i, final ItemStack itemstack,
            final Direction direction, final IInventory iinventory) {
        // Before putStackInInventoryAllSlots
        if (InventoryEventFactory.callTransferPre(((Inventory) dispensertileentity), ((Inventory) iinventory)).isCancelled()) {
            ci.cancel();
        }
    }

    @Nullable
    private static TrackedInventoryBridge impl$forCapture(final Object toCapture) {
        if (toCapture instanceof TrackedInventoryBridge) {
            return ((TrackedInventoryBridge) toCapture);
        }
        return null;
    }
}
