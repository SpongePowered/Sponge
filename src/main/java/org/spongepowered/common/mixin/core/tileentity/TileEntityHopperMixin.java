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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(HopperTileEntity.class)
public abstract class TileEntityHopperMixin extends TileEntityLockableLootMixin implements TrackedInventoryBridge {

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

    @Override
    public ReusableLens<?> bridge$generateReusableLens(final Fabric fabric, final InventoryAdapter adapter) {
        return ReusableLens.getLens(GridInventoryLens.class, this, this::impl$generateSlotProvider, this::impl$generateRootLens);
    }

    private SlotProvider impl$generateSlotProvider() {
        return new SlotCollection.Builder().add(5).build();
    }

    @SuppressWarnings("unchecked")
    private GridInventoryLens impl$generateRootLens(final SlotProvider slots) {
        return new GridInventoryLensImpl(0, 5, 1, 5, (Class<? extends Inventory>) this.getClass(), slots);
    }

    @Inject(method = "putDropInInventoryAllSlots",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;getItem()Lnet/minecraft/item/ItemStack;"))
    private static void impl$trackNotifierWhenTransferring(final IInventory inventory, final IInventory hopper, final ItemEntity entityItem,
        final CallbackInfoReturnable<Boolean> callbackInfo) {
        if (entityItem instanceof OwnershipTrackedBridge) {
            ((OwnershipTrackedBridge) entityItem).tracked$getOwnerReference().ifPresent(owner -> {
                if (inventory instanceof ActiveChunkReferantBridge && inventory instanceof TileEntity) {
                    final TileEntity te = (TileEntity) inventory;
                    final ChunkBridge spongeChunk = ((ActiveChunkReferantBridge) inventory).bridge$getActiveChunk();
                    spongeChunk.bridge$addTrackedBlockPosition(te.func_145838_q(), te.func_174877_v(), owner, PlayerTracker.Type.NOTIFIER);
                }
            });
        }
    }

    // Call PreEvents

    @Redirect(
        method = "pullItems",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryEmpty(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/EnumFacing;)Z"
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
            target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryFull(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/EnumFacing;)Z"
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
            target = "Lnet/minecraft/tileentity/TileEntityHopper;insertStack(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/EnumFacing;)Lnet/minecraft/item/ItemStack;"
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
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST && itemStack1.func_190926_b()) {
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
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST && itemStack2.func_190926_b()) {
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
