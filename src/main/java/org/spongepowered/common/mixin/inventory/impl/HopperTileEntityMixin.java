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
package org.spongepowered.common.mixin.inventory.impl;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.IHopper;
import net.minecraft.util.Direction;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(HopperTileEntity.class)
public abstract class HopperTileEntityMixin implements TrackedInventoryBridge {


    @Shadow private static ItemStack insertStack(
            final IInventory source, final IInventory destination, final ItemStack stack, final int index, final Direction direction) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow private static boolean isInventoryEmpty(final IInventory inventoryIn, final Direction side) {
        throw new AbstractMethodError("Shadow");
    }
    @Shadow protected abstract boolean isInventoryFull(IInventory inventoryIn, Direction side);

    private List<SlotTransaction> impl$capturedTransactions = new ArrayList<>();

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.impl$capturedTransactions;
    }



    // Call PreEvents

    @Redirect(
            method = "pullItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/HopperTileEntity;isInventoryEmpty(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/Direction;)Z"
            )
    )
    private static boolean impl$throwTransferPreIfNotEmpty(final IInventory inventory, final Direction facing, final IHopper hopper) {
        final boolean result = isInventoryEmpty(inventory, facing);
        if (result || !ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            return result;
        }
        return SpongeCommonEventFactory.callTransferPre(InventoryUtil.toInventory(inventory), InventoryUtil.toInventory(hopper)).isCancelled();
    }

    @Redirect(
            method = "transferItemsOut",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/HopperTileEntity;isInventoryFull(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/Direction;)Z"
            )
    )
    private boolean impl$throwTransferPreIfNotFull(final HopperTileEntity hopper, final IInventory inventory, final Direction enumfacing) {
        final boolean result = this.isInventoryFull(inventory, enumfacing);
        if (result || !ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            return result;
        }
        return SpongeCommonEventFactory.callTransferPre(InventoryUtil.toInventory(hopper), InventoryUtil.toInventory(inventory)).isCancelled();
    }

    // Capture Transactions

    @Redirect(
            method = "putStackInInventoryAllSlots",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/HopperTileEntity;insertStack(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/Direction;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack impl$throwEventsForInsertion(final IInventory source, final IInventory destination, final ItemStack stack,
            final int index, final Direction direction) {
        // capture Transaction
        if (!((source instanceof TrackedInventoryBridge || destination instanceof TrackedInventoryBridge) && destination instanceof InventoryAdapter)) {
            return insertStack(source, destination, stack, index, direction);
        }
        if (!ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST) {
            return insertStack(source, destination, stack, index, direction);
        }
        TrackedInventoryBridge captureIn = impl$forCapture(source);
        if (captureIn == null) {
            captureIn = impl$forCapture(destination);
        }
        return SpongeCommonEventFactory.captureTransaction(captureIn, InventoryUtil.toInventory(destination), index,
                () -> insertStack(source, destination, stack, index, direction));
    }

    // Post Captured Transactions

    @Inject(
            method = "transferItemsOut",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
                    ordinal = 1))
    private void impl$afterPutStackInSlots(final CallbackInfoReturnable<Boolean> cir, final IInventory iInventory, final Direction enumFacing,
            final int i, final ItemStack itemStack, ItemStack itemStack1) {
        // after putStackInInventoryAllSlots if the transfer worked
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST && itemStack1.isEmpty()) {
            // Capture Insert in Origin
            final TrackedInventoryBridge capture = impl$forCapture(this);
            SpongeCommonEventFactory.captureTransaction(capture, (Inventory) this, i, itemStack);
            // Call event
            if (SpongeCommonEventFactory.callTransferPost(capture, (Inventory) this, InventoryUtil.toInventory(iInventory))) {
                // Set remainder when cancelled
                // TODO - figure out what was intended to happen here....
                itemStack1 = itemStack;
            }
        }
    }


    @Inject(
            method = "pullItemFromSlot",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
                    ordinal = 1))
    private static void imlp$throwTransferEventsWhenPullingItems(final IHopper hopper, final IInventory iInventory, final int index,
            final Direction direction,
            final CallbackInfoReturnable<Boolean> cir, final ItemStack itemStack, ItemStack itemStack1, final ItemStack itemStack2) {
        // after putStackInInventoryAllSlots if the transfer worked
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST && itemStack2.isEmpty()) {
            // Capture Insert in Origin
            final TrackedInventoryBridge capture = impl$forCapture(hopper);
            SpongeCommonEventFactory.captureTransaction(capture, InventoryUtil.toInventory(iInventory), index, itemStack1);
            // Call event
            if (SpongeCommonEventFactory.callTransferPost(capture, InventoryUtil.toInventory(iInventory), InventoryUtil.toInventory(hopper))) {
                // Set remainder when cancelled
                // TODO - figure out what was intended to happen here....
                itemStack1 = itemStack;
            }
        }
    }


    @Redirect(
            method = "putDropInInventoryAllSlots",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/TileEntityHopper;putStackInInventoryAllSlots(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack impl$onPutStackInInventoryAllSlots(
            final IInventory source, final IInventory destination, final ItemStack stack, final Direction direction,
            final IInventory s2, final IInventory d2, final ItemEntity entity) {
        return SpongeCommonEventFactory.callInventoryPickupEvent(destination, entity, stack);
    }

    @Nullable
    private static TrackedInventoryBridge impl$forCapture(final Object toCapture) {
        if (toCapture instanceof TrackedInventoryBridge) {
            return ((TrackedInventoryBridge) toCapture);
        }
        return null;
    }
}
