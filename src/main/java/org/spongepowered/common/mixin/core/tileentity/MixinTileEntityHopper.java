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

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
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
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.bridge.world.ChunkBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(TileEntityHopper.class)
public abstract class MixinTileEntityHopper extends MixinTileEntityLockableLoot implements TrackedInventoryBridge {

    @Shadow private static ItemStack insertStack(IInventory source, IInventory destination, ItemStack stack, int index, EnumFacing direction) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow private static boolean isInventoryEmpty(IInventory inventoryIn, EnumFacing side) {
        throw new AbstractMethodError("Shadow");
    }
    @Shadow protected abstract boolean isInventoryFull(IInventory inventoryIn, EnumFacing side);

    private List<SlotTransaction> impl$capturedTransactions = new ArrayList<>();

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.impl$capturedTransactions;
    }

    @Override
    public ReusableLens<?> generateLens(Fabric fabric, InventoryAdapter adapter) {
        return ReusableLens.getLens(GridInventoryLens.class, this, this::generateSlotProvider, this::generateRootLens);
    }

    private SlotProvider generateSlotProvider() {
        return new SlotCollection.Builder().add(5).build();
    }

    private GridInventoryLens generateRootLens(SlotProvider slots) {
        return new GridInventoryLensImpl(0, 5, 1, 5, this.getClass(), slots);
    }

    @Inject(method = "putDropInInventoryAllSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;getItem()Lnet/minecraft/item/ItemStack;"))
    private static void impl$trackNotifierWhenTransferring(IInventory inventory, IInventory hopper, EntityItem entityItem,
        CallbackInfoReturnable<Boolean> callbackInfo) {
        ((EntityBridge) entityItem).getCreatorUser().ifPresent(owner -> {
            if (inventory instanceof TileEntity) {
                TileEntity te = (TileEntity) inventory;
                ChunkBridge spongeChunk = ((TileEntityBridge) te).getActiveChunk();
                spongeChunk.addTrackedBlockPosition(te.getBlockType(), te.getPos(), owner, PlayerTracker.Type.NOTIFIER);
            }
        });
    }

    // Call PreEvents

    @Redirect(
        method = "pullItems",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryEmpty(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/EnumFacing;)Z"
        )
    )
    private static boolean impl$throwTransferPreIfNotEmpty(IInventory inventory, EnumFacing facing, IHopper hopper) {
        boolean result = isInventoryEmpty(inventory, facing);
        if (result || !ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            return result;
        }
        return SpongeCommonEventFactory.callTransferPre(((Inventory) inventory), ((Inventory) hopper)).isCancelled();
    }

    @Redirect(
        method = "transferItemsOut",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryFull(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/EnumFacing;)Z"
        )
    )
    private boolean impl$throwTransferPreIfNotFull(TileEntityHopper hopper, IInventory inventory, EnumFacing enumfacing) {
        boolean result = this.isInventoryFull(inventory, enumfacing);
        if (result || !ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_PRE) {
            return result;
        }
        return SpongeCommonEventFactory.callTransferPre(((Inventory) hopper), ((Inventory) inventory)).isCancelled();
    }

    // Capture Transactions

    @Redirect(
        method = "putStackInInventoryAllSlots",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntityHopper;insertStack(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/EnumFacing;)Lnet/minecraft/item/ItemStack;"
        )
    )
    private static ItemStack impl$throwEventsForInsertion(IInventory source, IInventory destination, ItemStack stack, int index,
        EnumFacing direction) {
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
        return SpongeCommonEventFactory.captureTransaction(captureIn, ((Inventory) destination), index,
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
    private void afterPutStackInSlots(CallbackInfoReturnable<Boolean> cir, IInventory iInventory, EnumFacing enumFacing, int i, ItemStack itemStack, ItemStack itemStack1) {
        // after putStackInInventoryAllSlots if the transfer worked
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST && itemStack1.isEmpty()) {
            // Capture Insert in Origin
            TrackedInventoryBridge capture = impl$forCapture(this);
            SpongeCommonEventFactory.captureTransaction(capture, this, i, itemStack);
            // Call event
            if (SpongeCommonEventFactory.callTransferPost(capture, this, ((Inventory) iInventory))) {
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
    private static void imlp$throwTransferEventsWhenPullingItems(IHopper hopper, IInventory iInventory, int index, EnumFacing direction,
        CallbackInfoReturnable<Boolean> cir, ItemStack itemStack, ItemStack itemStack1, ItemStack itemStack2) {
        // after putStackInInventoryAllSlots if the transfer worked
        if (ShouldFire.CHANGE_INVENTORY_EVENT_TRANSFER_POST && itemStack2.isEmpty()) {
            // Capture Insert in Origin
            TrackedInventoryBridge capture = impl$forCapture(hopper);
            SpongeCommonEventFactory.captureTransaction(capture, ((Inventory) iInventory), index, itemStack1);
            // Call event
            if (SpongeCommonEventFactory.callTransferPost(capture, ((Inventory) iInventory), ((Inventory) hopper))) {
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
    private static ItemStack impl$onPutStackInInventoryAllSlots(IInventory source, IInventory destination, ItemStack stack, EnumFacing direction,
        IInventory s2, IInventory d2, EntityItem entity) {
        return SpongeCommonEventFactory.callInventoryPickupEvent(destination, entity, stack);
    }

    @Nullable
    private static TrackedInventoryBridge impl$forCapture(Object toCapture) {
        if (toCapture instanceof TrackedInventoryBridge) {
            return ((TrackedInventoryBridge) toCapture);
        }
        return null;
    }

}
