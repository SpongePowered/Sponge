package org.spongepowered.common.mixin.core.inventory.impl;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
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
import org.spongepowered.common.bridge.inventory.ContainerPlayerBridge;
import org.spongepowered.common.bridge.inventory.TrackedContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.inventory.ViewableInventoryBridge;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(Container.class)
public abstract class ContainerMixin_TrackedBridge implements TrackedInventoryBridge, TrackedContainerBridge, InventoryAdapter {

    // TrackedInventoryBridge

    private List<SlotTransaction> impl$capturedSlotTransactions = new ArrayList<>();
    private boolean impl$captureInventory = false;

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.impl$capturedSlotTransactions;
    }

    @Override
    public boolean bridge$capturingInventory() {
        return this.impl$captureInventory;
    }

    @Override
    public void bridge$setCaptureInventory(final boolean flag) {
        this.impl$captureInventory = flag;
    }

    // TrackedContainerBridge

    private boolean impl$shiftCraft = false;

    @Override
    public void bridge$setShiftCrafting(final boolean flag) {
        this.impl$shiftCraft = flag;
    }

    @Override
    public boolean bridge$isShiftCrafting() {
        return this.impl$shiftCraft;
    }


    @Nullable private CraftItemEvent.Craft impl$lastCraft = null;

    @Override
    public void bridge$setLastCraft(final CraftItemEvent.Craft event) {
        this.impl$lastCraft = event;
    }

    @Nullable @Override
    public CraftItemEvent.Craft bridge$getLastCraft() {
        return this.impl$lastCraft;
    }

    @Nullable private ItemStack impl$previousCursor;

    @Override public void bridge$setPreviousCursor(@Nullable ItemStack stack) {
        this.impl$previousCursor = stack;
    }

    @Override
    public ItemStack bridge$getPreviousCursor() {
        return this.impl$previousCursor;
    }

    private boolean impl$firePreview = true;

    @Override
    public void bridge$setFirePreview(final boolean firePreview) {
        this.impl$firePreview = firePreview;
    }

    @Override
    public boolean bridge$firePreview() {
        return this.impl$firePreview;
    }

    private List<SlotTransaction> impl$capturedCraftPreviewTransactions = new ArrayList<>();

    @Override
    public List<SlotTransaction> bridge$getPreviewTransactions() {
        return this.impl$capturedCraftPreviewTransactions;
    }

    // Detects if a mod overrides detectAndSendChanges
    private boolean impl$captureSuccess = false;

    @Override
    public boolean bridge$capturePossible() {
        return this.impl$captureSuccess;
    }

    @Override
    public void bridge$setCapturePossible() {
        this.impl$captureSuccess = true;
    }


    @Nullable private SpongeInventoryMenu impl$menu;

    @Override
    public void bridge$setMenu(SpongeInventoryMenu menu) {
        this.impl$menu = menu;
    }

    @Nullable @Override
    public SpongeInventoryMenu bridge$getMenu() {
        return this.impl$menu;
    }

    @Nullable private Object impl$viewed;

    @Override
    public void bridge$setViewed(@Nullable Object viewed) {
        if (viewed == null) {
            this.impl$unTrackInteractable(this.impl$viewed);
        }
        this.impl$viewed = viewed;
    }

    private void impl$unTrackInteractable(@Nullable Object inventory) {
        if (inventory instanceof Carrier) {
            inventory = ((Carrier) inventory).getInventory();
        }
        if (inventory instanceof Inventory) {
            ((Inventory) inventory).asViewable().ifPresent(i -> ((ViewableInventoryBridge) i).bridge$removeContainer(((Container) (Object) this)));
        }
        // TODO else unknown inventory - try to provide wrapper Interactable
    }

    // Injects/Redirects -------------------------------------------------------------------------

    @Shadow public abstract Slot shadow$getSlot(int slotId);

    // Called when changing a Slot while in creative mode
    // Captures the SlotTransaction for later event
    @Inject(method = "putStackInSlot", at = @At(value = "HEAD") )
    private void impl$addTransaction(final int slotId, final ItemStack itemstack, final CallbackInfo ci) {
        if (this.bridge$capturingInventory()) {
            final Slot slot = this.shadow$getSlot(slotId);
            if (slot != null) {
                final ItemStackSnapshot originalItem = ItemStackUtil.snapshotOf(slot.getStack());
                final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(itemstack);

                final org.spongepowered.api.item.inventory.Slot adapter = this.bridge$getSlot(slotId).get();
                this.bridge$getCapturedSlotTransactions().add(new SlotTransaction(adapter, originalItem, newItem));
            }
        }
    }

    // Called when clicking in an inventory
    // InventoryMenu Callback
    @Inject(method = "slotClick", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        if (this.bridge$getMenu() != null) {
            if (!this.bridge$getMenu().onClick(slotId, dragType, clickTypeIn, player, (org.spongepowered.api.item.inventory.Container) this)) {
                cir.setReturnValue(ItemStack.EMPTY);
                // TODO maybe need to send rollback packets to client
            }
        }
    }

    // Called when a Container is closed
    // InventoryMenu Callback and resetting viewed and menu state
    @Inject(method = "onContainerClosed", at = @At(value = "HEAD"))
    private void onOnContainerClosed(PlayerEntity player, CallbackInfo ci) {
        SpongeInventoryMenu menu = this.bridge$getMenu();
        if (menu != null) {
            menu.onClose(player, (org.spongepowered.api.item.inventory.Container) this);
        }
        this.bridge$setViewed(null);
        this.bridge$setMenu(null);
    }


    // ClickType.PICKUP or ClickType.QUICK_MOVE with slotId == -999 ; Dropping Items --------------------------------------

    private boolean impl$dropCancelled = false;

    // Called when dropping a full itemstack out of the inventory ; PART 1/3
    // Restores the cursor item if needed
    @Nullable
    @Redirect(method = "slotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/ItemEntity;",
                    ordinal = 0))
    private ItemEntity impl$RestoreOnDragDrop(final PlayerEntity player, final ItemStack itemStackIn, final boolean unused) {
        final ItemStackSnapshot original = ItemStackUtil.snapshotOf(itemStackIn);
        final ItemEntity entityItem = player.dropItem(itemStackIn, unused);
        if (!((EntityPlayerBridge) player).bridge$shouldRestoreInventory()) {
            return entityItem;
        }
        if (entityItem == null) {
            this.impl$dropCancelled = true;
            PacketPhaseUtil.handleCustomCursor(player, original);
        }
        return entityItem;
    }

    // Called when dropping a full itemstack out of the inventory ; PART 2/3
    // Resets Player and Container for canceled drop
    @Redirect(method = "slotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;setItemStack(Lnet/minecraft/item/ItemStack;)V",
                    ordinal = 1))
    private void impl$ClearOnSlot(final PlayerInventory inventoryPlayer, final ItemStack itemStackIn) {
        if (!this.impl$dropCancelled || !((EntityPlayerBridge) inventoryPlayer.player).bridge$shouldRestoreInventory()) {
            inventoryPlayer.setItemStack(itemStackIn); // original behaviour
        }
        ((EntityPlayerBridge) inventoryPlayer.player).bridge$shouldRestoreInventory(false);
        this.impl$dropCancelled = false;
    }

    // Called when splitting and dropping an itemstack out of the inventory ; PART 3/3
    // Restores the cursor item if needed and resets Player and Container for canceled drop
    @Redirect(method = "slotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/ItemEntity;",
                    ordinal = 1))
    @Nullable
    private ItemEntity impl$restoreOnDragSplit(final PlayerEntity player, final ItemStack itemStackIn, final boolean unused) {
        final ItemEntity entityItem = player.dropItem(itemStackIn, unused);
        if (!((EntityPlayerBridge) player).bridge$shouldRestoreInventory()) {
            return entityItem;
        }
        if (entityItem == null) {
            ItemStack original;
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

    // ClickType.THROW ; throwing items out (Q) -------

    private ItemStackSnapshot impl$itemStackSnapshot = ItemStackSnapshot.empty();
    @Nullable private Slot impl$lastSlotUsed = null;

    // Called before the item is thrown ; PART 1/2
    // Captures the original state and affected slot
    @Redirect(method = "slotClick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/inventory/container/Slot;canTakeStack(Lnet/minecraft/entity/player/PlayerEntity;)Z",
            ordinal = 4))
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

    // Called dropping the item ; PART 2/2
    // Restores the slot if needed
    @Nullable
    @Redirect(method = "slotClick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/ItemEntity;",
            ordinal = 3))
    private ItemEntity onThrowClick(final PlayerEntity player, final ItemStack itemStackIn, final boolean unused) {
        final ItemEntity entityItem = player.dropItem(itemStackIn, true);
        if (entityItem == null && ((EntityPlayerBridge) player).bridge$shouldRestoreInventory()) {
            final ItemStack original = ItemStackUtil.fromSnapshotToNative(this.impl$itemStackSnapshot);
            this.impl$lastSlotUsed.putStack(original);
            player.openContainer.detectAndSendChanges(); // TODO check if this is needed?
            ((ServerPlayerEntity) player).isChangingQuantityOnly = false;
            ((ServerPlayerEntity) player).connection.sendPacket(new SSetSlotPacket(player.openContainer.windowId, this.impl$lastSlotUsed.slotNumber, original));
        }
        this.impl$itemStackSnapshot = ItemStackSnapshot.empty();
        this.impl$lastSlotUsed = null;
        ((EntityPlayerBridge) player).bridge$shouldRestoreInventory(false);
        return entityItem;
    }

    // ClickType.PICKUP ; pick up item on cursor -------------------------

    // Called when adding items to the cursor (pickup with item on cursor)
    // Captures the previous cursor for later use
    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;grow(I)V", ordinal = 1))
    private void beforeOnTakeClickWithItem(
            final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<Integer> cir) {
        this.bridge$setPreviousCursor(player.inventory.getItemStack().copy()); // capture previous cursor for CraftItemEvent.Craft
    }

    // Called when setting the cursor item (pickup with empty cursor)
    // Captures the previous cursor for later use
    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setItemStack(Lnet/minecraft/item/ItemStack;)V", ordinal = 3))
    private void beforeOnTakeClick(
            final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<Integer> cir) {
        this.bridge$setPreviousCursor(player.inventory.getItemStack().copy()); // capture previous cursor for CraftItemEvent.Craft
    }

    // ClickType.THROW (for Crafting) -------------------------
    // Called when taking items out of a slot (only crafting-output slots relevant here)
    // When it is crafting check if it was cancelled and prevent item drop
    @Redirect(method = "slotClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/inventory/container/Slot;onTake(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                    ordinal = 5))
    private ItemStack redirectOnTakeThrow(final Slot slot, final PlayerEntity player, final ItemStack stackToDrop) {
        this.bridge$setLastCraft(null);
        final ItemStack result = slot.onTake(player, stackToDrop);
        CraftItemEvent.Craft lastCraft = this.bridge$getLastCraft();
        if (lastCraft != null) {
            if (slot instanceof CraftingResultSlot) {
                if (lastCraft.isCancelled()) {
                    stackToDrop.setCount(0); // do not drop crafted item when cancelled
                }
            }
        }
        return result;
    }

    // ClickType.QUICK_MOVE (for Crafting) -------------------------
    // Called when Shift-Crafting - 2 Injection points
    // Crafting continues until the returned ItemStack is empty OR the returned ItemStack is not the same as the item in the output slot
    @Redirect(method = "slotClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/inventory/container/Container;transferStackInSlot(Lnet/minecraft/entity/player/PlayerEntity;I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectTransferStackInSlot(final Container thisContainer, final PlayerEntity player, final int slotId) {
        final Slot slot = thisContainer.getSlot(slotId);
        if (!(slot instanceof CraftingResultSlot)) { // is this crafting?
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

    // cleanup after slot click was captured
    @Inject(method = "slotClick", at = @At("RETURN"))
    private void onReturn(final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<ItemStack> cir) {
        // Reset variables needed for CraftItemEvent.Craft
        this.bridge$setLastCraft(null);
        this.bridge$setPreviousCursor(null);

        // TODO check if when canceling crafting etc. the client is getting informed correctly already - maybe this is not needed
        // previously from CraftingContainerMixin
        if (((Object) this) instanceof WorkbenchContainer || ((Object) this) instanceof PlayerContainer) {
            for (IContainerListener listener : this.listeners) {
                if (slotId == 0) {
                    listener.sendAllContents((Container) (Object) this, this.getInventory());
                } else {
                    listener.sendSlotContents((Container) (Object) this, 0, this.getInventory().get(0));
                }
            }

        }
    }

    // detectAndSendChanges

    /**
     * @author bloodmc
     * @reason All player fabric changes that need to be synced to
     * client flow through this method. Overwrite is used as no mod
     * should be touching this method.
     */
    @Overwrite
    public void detectAndSendChanges() {
        this.bridge$detectAndSendChanges(false);
        this.bridge$setCapturePossible(); // Detect mod overrides
    }

    @Final @Shadow private NonNullList<ItemStack> inventoryItemStacks;
    @Final @Shadow public List<Slot> inventorySlots;
    @Final @Shadow private List<IContainerListener> listeners;
    @Final @Shadow private List<IntReferenceHolder> trackedIntReferences;

    @Shadow public abstract NonNullList<ItemStack> getInventory();

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
                if (menu != null && menu.isReadOnly()) { // readonly menu cancels if there is any change outside of the players inventory
                    if (!(slot.inventory instanceof PlayerInventory)) {
                        readOnlyCancel = true;
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
                if (menu != null && !menu.onChange(newStack, oldStack, (org.spongepowered.api.item.inventory.Container) this, i, slot)) {
                    slot.putStack(oldStack); // revert changes
                } else {
                    // Capture changes for inventory events
                    this.impl$capture(i, newStack, oldStack);

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

        // like vanilla send property changes
        this.impl$detectAndSendPropertyChanges();

        if (this instanceof ContainerPlayerBridge) {
            ((ContainerPlayerBridge) this).bridge$markClean();
        }
    }

    private void impl$detectAndSendPropertyChanges() {
        for(int j = 0; j < this.trackedIntReferences.size(); ++j) {
            IntReferenceHolder intreferenceholder = this.trackedIntReferences.get(j);
            if (intreferenceholder.isDirty()) {
                for(IContainerListener icontainerlistener1 : this.listeners) {
                    icontainerlistener1.sendWindowProperty((Container) (Object) this, j, intreferenceholder.get());
                }
            }
        }
    }

    private void impl$capture(Integer index, ItemStack itemstack, ItemStack itemstack1) {
        if (this.bridge$capturingInventory()) {
            final ItemStackSnapshot originalItem = ItemStackUtil.snapshotOf(itemstack1);
            final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(itemstack);

            org.spongepowered.api.item.inventory.Slot adapter;
            try {
                adapter = this.bridge$getSlot(index).get();
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

    // TODO check if addListener with existing listener needs to resend

}
