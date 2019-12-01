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
package org.spongepowered.common.mixin.core.inventory.impl;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.player.EntityPlayerBridge;
import org.spongepowered.common.bridge.inventory.TrackedContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = Container.class, priority = 998)
public abstract class ContainerMixin implements TrackedInventoryBridge, TrackedContainerBridge {

    @Final @Shadow public List<Slot> inventorySlots;
    @Final @Shadow private NonNullList<ItemStack> inventoryItemStacks;
    @Final @Shadow private List<IContainerListener> listeners;
    @Shadow public abstract NonNullList<ItemStack> getInventory();
    @Shadow public abstract Slot shadow$getSlot(int slotId);
    @Shadow public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player) {
        throw new IllegalStateException("Shadowed.");
    }

    @Shadow public abstract Slot getSlot(int slotId);

    private boolean impl$dropCancelled = false;
    private ItemStackSnapshot impl$itemStackSnapshot = ItemStackSnapshot.empty();
    @Nullable private Slot impl$lastSlotUsed = null;

    /**
     * @author bloodmc
     * @reason If listener already exists, avoid firing an exception
     * and simply send the inventory changes to client.
     */
    @Overwrite
    public void addListener(final IContainerListener listener) {
        final Container container = (Container) (Object) this;
        if (this.listeners.contains(listener)) {
            // Sponge start
            // throw new IllegalArgumentException("Listener already listening");
            listener.sendAllContents(container, this.getInventory());
            container.detectAndSendChanges();
            // Sponge end
        } else {
            this.listeners.add(listener);
            listener.sendAllContents(container, this.getInventory());
            container.detectAndSendChanges();
        }
    }

    /**
     * @author bloodmc
     * @reason All player fabric changes that need to be synced to
     * client flow through this method. Overwrite is used as no mod
     * should be touching this method.
     *
     */
    @Overwrite
    public void detectAndSendChanges() {
        this.bridge$detectAndSendChanges(false);
        this.bridge$setCapturePossible(); // Detect mod overrides
    }

    @Override
    public void bridge$detectAndSendChanges(final boolean captureOnly) {
        // Code-Flow changed from vanilla completely!

        SpongeInventoryMenu menu = this.bridge$getMenu();
        // We first collect all differences and check if cancelled for readonly menu changes
        boolean readOnlyCancel = false;
        List<Integer> changes = new ArrayList<>();

        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            final Slot slot = this.inventorySlots.get(i);
            final ItemStack newStack = slot.getStack();
            ItemStack oldStack = this.inventoryItemStacks.get(i);
            if (!ItemStack.areItemStacksEqual(oldStack, newStack)) {
                changes.add(i);
                if (menu != null) {
                    if (menu.isReadOnly()) { // readonly menu cancels if there is any change outside of the players inventory
                        if (!(slot.inventory instanceof PlayerInventory)) {
                            readOnlyCancel = true;
                        }
                    }
                }
            }
        }

        if (readOnlyCancel) {
            // revert all changes if readonly
            for (Integer i : changes) {
                final Slot slot = this.inventorySlots.get(i);
                ItemStack oldStack = this.inventoryItemStacks.get(i);
                slot.putStack(oldStack);
                // Send reverted slots to clients
                for (IContainerListener listener : this.listeners) {
                    listener.sendSlotContents(((Container) (Object) this), i, oldStack);
                }
            }
        } else {
            // For each change
            for (Integer i : changes) {
                final Slot slot = this.inventorySlots.get(i);
                ItemStack newStack = slot.getStack();
                ItemStack oldStack = this.inventoryItemStacks.get(i);

                // Check for on change menu callbacks
                if (menu != null && !menu
                        .onChange(newStack, oldStack, (org.spongepowered.api.item.inventory.Container) this, i, slot)) {
                    slot.putStack(oldStack); // revert changes
                } else {
                    // Capture changes for inventory events
                    this.capture(i, newStack, oldStack);

                    // This flag is set only when the client sends an invalid CPacketWindowClickItem packet.
                    // We simply capture in order to send the proper changes back to client.
                    if (captureOnly) {
                        continue;
                    }
                    // Perform vanilla logic - updating inventory stack - notify listeners
                    newStack = newStack.isEmpty() ? ItemStack.EMPTY : newStack.copy();
                    this.inventoryItemStacks.set(i, newStack);
                    // TODO forge checks !itemstack1.equals(itemstack, true) before doing this
                    for (IContainerListener listener : this.listeners) {
                        listener.sendSlotContents(((Container) (Object) this), i, newStack);
                    }
                }
            }
        }

        this.impl$markClean();
    }

    private void capture(Integer index, ItemStack itemstack, ItemStack itemstack1) {
        if (this.bridge$capturingInventory()) {
            final ItemStackSnapshot originalItem = ItemStackUtil.snapshotOf(itemstack1);
            final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(itemstack);

            org.spongepowered.api.item.inventory.Slot adapter = null;
            try {
                adapter = ((InventoryAdapter) this).bridge$getSlot(index).get();
                SlotTransaction newTransaction = new SlotTransaction(adapter, originalItem, newItem);
                List<SlotTransaction> previewTransactions = this.bridge$getPreviewTransactions();
                if (this.bridge$isShiftCrafting()) {
                    previewTransactions.add(newTransaction);
                } else {
                    if (!previewTransactions.isEmpty()) { // Check if Preview transaction is this transaction
                        SlotTransaction previewTransaction = previewTransactions.get(0);
                        if (previewTransaction.equals(newTransaction)) {
                            newTransaction = null;
                        }
                    }
                    if (newTransaction != null) {
                        previewTransactions.add(newTransaction);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                SpongeImpl.getLogger().error("SlotIndex out of LensBounds! Did the Container change after creation?", e);
            }
        }
    }

    protected void impl$markClean() {
    }

    @Inject(method = "putStackInSlot", at = @At(value = "HEAD") )
    private void impl$addTransaction(final int slotId, final ItemStack itemstack, final CallbackInfo ci) {
        if (this.bridge$capturingInventory()) {
            final Slot slot = this.shadow$getSlot(slotId);
            if (slot != null) {
                final ItemStackSnapshot originalItem = ItemStackUtil.snapshotOf(slot.getStack());
                final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(itemstack);

                final org.spongepowered.api.item.inventory.Slot adapter = ((InventoryAdapter) this).bridge$getSlot(slotId).get();
                this.bridge$getCapturedSlotTransactions().add(new SlotTransaction(adapter, originalItem, newItem));
            }
        }
    }

    @Inject(method = "slotClick", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        if (this.bridge$getMenu() != null) {
            if (!this.bridge$getMenu().onClick(slotId, dragType, clickTypeIn, player, (org.spongepowered.api.item.inventory.Container) this)) {
                cir.setReturnValue(ItemStack.EMPTY);
                // TODO maybe need to send rollback packets to client
            }
        }
    }

    @Inject(method = "onContainerClosed", at = @At(value = "HEAD"))
    private void onOnContainerClosed(PlayerEntity player, CallbackInfo ci) {
        SpongeInventoryMenu menu = this.bridge$getMenu();
        if (menu != null) {
            menu.onClose(player, (org.spongepowered.api.item.inventory.Container) this);
        }
        this.bridge$setViewed(null);
        this.bridge$setMenu(null);
    }

    @Nullable
    @Redirect(method = "slotClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;",
            ordinal = 0))
    private ItemEntity impl$RestoreOnDrag(final PlayerEntity player, final ItemStack itemStackIn, final boolean unused) {
        final ItemStackSnapshot original = ItemStackUtil.snapshotOf(itemStackIn);
        final ItemEntity entityItem = player.dropItem(itemStackIn, unused);
        if (!((EntityPlayerBridge) player).bridge$shouldRestoreInventory()) {
            return entityItem;
        }
        if (entityItem  == null) {
            this.impl$dropCancelled = true;
            PacketPhaseUtil.handleCustomCursor((ServerPlayerEntity) player, original);
        }
        return entityItem;
    }

    @Redirect(method = "slotClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;",
            ordinal = 1))
    @Nullable
    private ItemEntity impl$restoreOnDragSplit(final PlayerEntity player, final ItemStack itemStackIn, final boolean unused) {
        final ItemEntity entityItem = player.dropItem(itemStackIn, unused);
        if (!((EntityPlayerBridge) player).bridge$shouldRestoreInventory()) {
            return entityItem;
        }
        if (entityItem  == null) {
            ItemStack original = null;
            if (player.inventory.getItemStack().isEmpty()) {
                original = itemStackIn;
            } else {
                player.inventory.getItemStack().grow(1);
                original = player.inventory.getItemStack();
            }
            player.inventory.setItemStack(original);
            ((ServerPlayerEntity) player).connection.sendPacket(new SSetSlotPacket(-1, -1, original));
        }
        ((EntityPlayerBridge) player).bridge$shouldRestoreInventory(false);
        return entityItem;
    }

    @Redirect(method = "slotClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;setItemStack(Lnet/minecraft/item/ItemStack;)V",
            ordinal = 1))
    private void impl$ClearOnSlot(final PlayerInventory inventoryPlayer, final ItemStack itemStackIn) {
        if (!this.impl$dropCancelled || !((EntityPlayerBridge) inventoryPlayer.player).bridge$shouldRestoreInventory()) {
            inventoryPlayer.setItemStack(itemStackIn);
        }
        ((EntityPlayerBridge) inventoryPlayer.player).bridge$shouldRestoreInventory(false);
        this.impl$dropCancelled = false;
    }

    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Slot;canTakeStack(Lnet/minecraft/entity/player/EntityPlayer;)Z", ordinal = 4))
    public boolean onCanTakeStack(final Slot slot, final PlayerEntity playerIn) {
        final boolean result = slot.canTakeStack(playerIn);
        if (result) {
            this.impl$itemStackSnapshot = ItemStackUtil.snapshotOf(slot.getStack());
            this.impl$lastSlotUsed = slot;
        } else {
            this.impl$itemStackSnapshot = ItemStackSnapshot.empty();
            this.impl$lastSlotUsed = null;
        }
        return result;
    }

    @Nullable
    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;", ordinal = 3))
    private ItemEntity onThrowClick(final PlayerEntity player, final ItemStack itemStackIn, final boolean unused) {
        final ItemEntity entityItem = player.dropItem(itemStackIn, true);
        if (entityItem == null && ((EntityPlayerBridge) player).bridge$shouldRestoreInventory()) {
            final ItemStack original = ItemStackUtil.toNative(this.impl$itemStackSnapshot.createStack());
            this.impl$lastSlotUsed.putStack(original);
            player.openContainer.detectAndSendChanges();
            ((ServerPlayerEntity) player).isChangingQuantityOnly = false;
            ((ServerPlayerEntity) player).connection.sendPacket(new SSetSlotPacket(player.openContainer.windowId, this.impl$lastSlotUsed.slotNumber, original));
        }
        this.impl$itemStackSnapshot = ItemStackSnapshot.empty();
        this.impl$lastSlotUsed = null;
        ((EntityPlayerBridge) player).bridge$shouldRestoreInventory(false);
        return entityItem;
    }


    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;grow(I)V", ordinal = 1))
    private void beforeOnTakeClickWithItem(
        final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<Integer> cir) {
       this.bridge$setPreviousCursor(player.inventory.getItemStack().copy()); // capture previous cursor for CraftItemEvent.Craft
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;setItemStack(Lnet/minecraft/item/ItemStack;)V", ordinal = 3))
    private void beforeOnTakeClick(
        final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<Integer> cir) {
        this.bridge$setPreviousCursor(player.inventory.getItemStack().copy()); // capture previous cursor for CraftItemEvent.Craft
    }

    @Redirect(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Slot;onTake(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", ordinal = 5))
    private ItemStack redirectOnTakeThrow(final Slot slot, final PlayerEntity player, final ItemStack stackOnCursor) {
        this.bridge$setLastCraft(null);
        final ItemStack result = slot.onTake(player, stackOnCursor);
        CraftItemEvent.Craft lastCraft = this.bridge$getLastCraft();
        if (lastCraft != null) {
            if (slot instanceof CraftingResultSlot) {
                if (lastCraft.isCancelled()) {
                    stackOnCursor.setCount(0); // do not drop crafted item when cancelled
                }
            }
        }
        return result;
    }

    @Inject(method = "slotClick", at = @At("RETURN"))
    private void onReturn(final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<ItemStack> cir) {
        // Reset variables needed for CraftItemEvent.Craft
        this.bridge$setLastCraft(null);
        this.bridge$setPreviousCursor(null);
    }


    @Redirect(method = "slotClick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectTransferStackInSlot(final Container thisContainer, final PlayerEntity player, final int slotId) {
        final Slot slot = thisContainer.getSlot(slotId);
        if (!(slot instanceof CraftingResultSlot)) {
            return thisContainer.transferStackInSlot(player, slotId);
        }
        this.bridge$setLastCraft(null);
        this.bridge$setShiftCrafting(true);
        ItemStack result = thisContainer.transferStackInSlot(player, slotId);
        CraftItemEvent.Craft lastCraft = this.bridge$getLastCraft();
        if (lastCraft != null) {
            if (lastCraft.isCancelled()) {
                result = ItemStack.EMPTY; // Return empty to stop shift-crafting
            }
        }

        this.bridge$setShiftCrafting(false);

        return result;
    }

}
