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
package org.spongepowered.common.mixin.inventory.event.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.InventoryMenuBridge;
import org.spongepowered.common.bridge.world.inventory.ViewableInventoryBridge;
import org.spongepowered.common.bridge.world.inventory.container.MenuBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;
import org.spongepowered.common.item.util.ItemStackUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_Inventory implements TrackedContainerBridge, InventoryAdapter {

    //@formatter:off
    @Final @Shadow private NonNullList<ItemStack> lastSlots;
    @Final @Shadow public List<Slot> slots;
    @Final @Shadow private List<ContainerListener> containerListeners;
    @Final @Shadow private List<DataSlot> dataSlots;

    @Shadow public abstract NonNullList<ItemStack> shadow$getItems();
    @Shadow protected abstract ItemStack shadow$doClick(int param0, int param1, ClickType param2, Player param3);
    //@formatter:on
    // TrackedContainerBridge

    private boolean impl$shiftCraft = false;
    private ItemStack impl$menuCapture;
    // Detects if a mod overrides detectAndSendChanges
    private boolean impl$captureSuccess = false;
    @Nullable private CraftItemEvent.Craft impl$lastCraft = null;
    @Nullable private Object impl$viewed;
    private boolean impl$dropCancelled = false;

    @Override
    public void bridge$setShiftCrafting(final boolean flag) {
        this.impl$shiftCraft = flag;
    }

    @Override
    public boolean bridge$isShiftCrafting() {
        return this.impl$shiftCraft;
    }

    @Override
    public void bridge$setLastCraft(final CraftItemEvent.Craft event) {
        this.impl$lastCraft = event;
    }

    @Nullable @Override
    public CraftItemEvent.Craft bridge$getLastCraft() {
        return this.impl$lastCraft;
    }

    @Override
    public boolean bridge$capturePossible() {
        return this.impl$captureSuccess;
    }

    @Override
    public void bridge$setCapturePossible() {
        this.impl$captureSuccess = true;
    }


    @Override
    public void bridge$trackViewable(Object inventory) {
        if (inventory instanceof Carrier) {
            inventory = ((Carrier) inventory).inventory();
        }
        if (inventory instanceof Inventory) {
            ((Inventory) inventory).asViewable().ifPresent(i -> ((ViewableInventoryBridge) i).viewableBridge$addContainer((AbstractContainerMenu) (Object) this));
        }
        this.impl$setViewed(inventory);
        // TODO else unknown inventory - try to provide wrapper ViewableInventory?
    }

    private void impl$setViewed(@Nullable final Object viewed) {
        if (viewed == null) {
            this.impl$unTrackViewable(this.impl$viewed);
        }
        this.impl$viewed = viewed;
    }

    private void impl$unTrackViewable(@Nullable Object inventory) {
        if (inventory instanceof Carrier) {
            inventory = ((Carrier) inventory).inventory();
        }
        if (inventory instanceof Inventory) {
            ((Inventory) inventory).asViewable().ifPresent(i -> ((ViewableInventoryBridge) i).viewableBridge$removeContainer(((AbstractContainerMenu) (Object) this)));
        }
        // TODO else unknown inventory - try to provide wrapper ViewableInventory?
    }

    // Injects/Redirects -------------------------------------------------------------------------

    @Inject(method = "removed", at = @At(value = "HEAD"))
    private void onOnContainerClosed(final Player player, final CallbackInfo ci) {
        if (((LevelBridge) player.level).bridge$isFake()) {
            return;
        }
        this.impl$setViewed(null);
    }

    @Redirect(method = "doClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;mayPickup(Lnet/minecraft/world/entity/player/Player;)Z",
            ordinal = 0
        ),
        slice = @Slice(
            from = @At(value = "FIELD",
                target = "Lnet/minecraft/world/inventory/ClickType;THROW:Lnet/minecraft/world/inventory/ClickType;"
            ),
            to = @At(value = "FIELD",
                target = "Lnet/minecraft/world/inventory/ClickType;PICKUP_ALL:Lnet/minecraft/world/inventory/ClickType;"
            )
        )
    )
    public boolean impl$verifyReadOnlyMenu(final Slot slot, final Player playerIn) {
        if (((LevelBridge) playerIn.level).bridge$isFake()) {
            slot.mayPickup(playerIn);
        }
        if (((MenuBridge) this).bridge$isReadonlyMenu(slot)) {
            ((MenuBridge) this).bridge$refreshListeners();
            return false;
        }
        return slot.mayPickup(playerIn);
    }

    // ClickType.THROW (for Crafting) -------------------------
    // Called when taking items out of a slot (only crafting-output slots relevant here)
    // When it is crafting check if it was cancelled and prevent item drop
    @Redirect(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 5))
    private ItemStack redirectOnTakeThrow(final Slot slot, final Player player, final ItemStack stackToDrop) {
        if (((LevelBridge) player.level).bridge$isFake()) {
            return slot.onTake(player, stackToDrop);
        }
        this.bridge$setLastCraft(null);
        final ItemStack result = slot.onTake(player, stackToDrop);
        final CraftItemEvent.Craft lastCraft = this.bridge$getLastCraft();
        if (lastCraft != null) {
            if (slot instanceof ResultSlot) {
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
    @Redirect(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack impl$transferStackInSlot(final AbstractContainerMenu thisContainer, final Player player, final int slotId) {
        if (((LevelBridge) player.level).bridge$isFake()) {
            return thisContainer.quickMoveStack(player, slotId);
        }
        final Slot slot = thisContainer.getSlot(slotId);
        if (!(slot instanceof ResultSlot)) { // is this crafting?
            return thisContainer.quickMoveStack(player, slotId);
        }
        this.bridge$setLastCraft(null);
        this.bridge$setShiftCrafting(true);
        ItemStack result = thisContainer.quickMoveStack(player, slotId);

        this.bridge$detectAndSendChanges(true);

        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        TrackingUtil.processBlockCaptures(context); // ClickContainerEvent -> CraftEvent -> PreviewEvent
        this.bridge$setShiftCrafting(false);

        final CraftItemEvent.Craft lastCraft = this.bridge$getLastCraft();
        if (lastCraft != null) {
            if (lastCraft.isCancelled()) {
                result = ItemStack.EMPTY; // Return empty to stop shift-crafting
            }
        }

        return result;
    }

    // cleanup after slot click was captured
    @Inject(method = "doClick", at = @At("RETURN"))
    private void impl$onReturn(final int slotId, final int dragType, final ClickType clickTypeIn, final Player player, final CallbackInfoReturnable<ItemStack> cir) {
        if (!PhaseTracker.SERVER.onSidedThread()) {
            return;
        }
        // Reset variables needed for CraftItemEvent.Craft
        this.bridge$setLastCraft(null);

        // TODO check if when canceling crafting etc. the client is getting informed correctly already - maybe this is not needed
        // previously from CraftingContainerMixin
        if (((Object) this) instanceof CraftingMenu || ((Object) this) instanceof InventoryMenu) {
            for (final ContainerListener listener : this.containerListeners) {
                if (slotId == 0) {
                    listener.refreshContainer((AbstractContainerMenu) (Object) this, this.shadow$getItems());
                } else {
                    listener.slotChanged((AbstractContainerMenu) (Object) this, 0, this.shadow$getItems().get(0));
                }
            }

        }
    }

    @Redirect(
            method = "clicked",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack inventory$wrapDoClickWithTransaction(
            final AbstractContainerMenu menu, final int slotId, final int dragType,
            final ClickType clickType,
            final Player player
    ) {
        if (((LevelBridge) player.level).bridge$isFake()) {
            return this.shadow$doClick(slotId, dragType, clickType, player);
        }
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        try (final EffectTransactor ignored = transactor.logClickContainer(menu, slotId, dragType, clickType, player)) {
            return this.shadow$doClick(slotId, dragType, clickType, player);
        }
    }



    // detectAndSendChanges

    /**
     * We inject at head and cancel for the server side, but on client side, we
     * are still the same class, so we must consider that when we're on the
     * client thread, don't perform any logic.
     *
     * @author gabizou
     */
    @Inject(method = "broadcastChanges", at = @At("HEAD"), cancellable = true)
    public void impl$broadcastChangesWithTransactions(final CallbackInfo ci) {
        if (!PhaseTracker.SERVER.onSidedThread()) {
            return;
        }
        this.bridge$detectAndSendChanges(false);
        this.bridge$setCapturePossible(); // Detect mod overrides
        ci.cancel();
    }


    @Override
    public void bridge$detectAndSendChanges(final boolean captureOnly) {
        // Code-Flow changed from vanilla completely!

        final SpongeInventoryMenu menu = ((MenuBridge)this).bridge$getMenu();
        // We first collect all differences and check if cancelled for readonly menu changes
        final List<Integer> changes = new ArrayList<>();

        for (int i = 0; i < this.slots.size(); ++i) {
            final Slot slot = this.slots.get(i);
            final ItemStack newStack = slot.getItem();
            final ItemStack oldStack = this.lastSlots.get(i);
            if (!ItemStack.matches(oldStack, newStack)) {
                changes.add(i);
            }
        }

        // For each change
        for (final Integer i : changes) {
            final Slot slot = this.slots.get(i);
            final ItemStack newStack = slot.getItem();
            ItemStack oldStack = this.lastSlots.get(i);

            // Check for on change menu callbacks
            if (this.impl$menuCapture != null && menu != null && !menu.onChange(newStack, oldStack, (org.spongepowered.api.item.inventory.Container) this, i, slot)) {
                this.lastSlots.set(i, oldStack.copy());  // revert changes
                // Send reverted slots to clients
                this.impl$sendSlotContents(i, oldStack);
            } else {
                // Capture changes for inventory events
                this.impl$capture(i, newStack, oldStack);

                // This flag is set only when the client sends an invalid CPacketWindowClickItem packet.
                // We simply capture in order to send the proper changes back to client.
                if (captureOnly) {
                    continue;
                }
                // Perform vanilla logic - updating inventory stack - notify listeners
                oldStack = newStack.isEmpty() ? ItemStack.EMPTY : newStack.copy();
                this.lastSlots.set(i, oldStack);
                // TODO forge checks !itemstack1.equals(itemstack, true) before doing this
                for (final ContainerListener listener : this.containerListeners) {
                    listener.slotChanged(((AbstractContainerMenu) (Object) this), i, oldStack);
                }
            }
        }

        // like vanilla send property changes
        this.impl$detectAndSendPropertyChanges();

        if (this instanceof InventoryMenuBridge) {
            ((InventoryMenuBridge) this).bridge$markClean();
        }
    }

    public void impl$sendSlotContents(final Integer i, final ItemStack oldStack) {

        for (final ContainerListener listener : this.containerListeners) {
            boolean isChangingQuantityOnly = true;
            if (listener instanceof ServerPlayer) {
                isChangingQuantityOnly = ((ServerPlayer) listener).ignoreSlotUpdateHack;
                ((ServerPlayer) listener).ignoreSlotUpdateHack = false;
            }
            listener.slotChanged(((AbstractContainerMenu) (Object) this), i, oldStack);
            if (listener instanceof ServerPlayer) {
                ((ServerPlayer) listener).ignoreSlotUpdateHack = isChangingQuantityOnly;
            }
        }
    }

    private void impl$detectAndSendPropertyChanges() {
        for(int j = 0; j < this.dataSlots.size(); ++j) {
            final DataSlot intreferenceholder = this.dataSlots.get(j);
            if (intreferenceholder.checkAndClearUpdateFlag()) {
                for(final ContainerListener icontainerlistener1 : this.containerListeners) {
                    icontainerlistener1.setContainerData((AbstractContainerMenu) (Object) this, j, intreferenceholder.get());
                }
            }
        }
    }

    private void impl$capture(final Integer index, final ItemStack newStack, final ItemStack oldStack) {
        if (PhaseTracker.SERVER.onSidedThread() && !PhaseTracker.SERVER.getPhaseContext().isRestoring()) {
            final ItemStackSnapshot oldItem = ItemStackUtil.snapshotOf(oldStack);
            final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(newStack);
            try {
                final org.spongepowered.api.item.inventory.Slot adapter = this.inventoryAdapter$getSlot(index).get();
                final SlotTransaction newTransaction = new SlotTransaction(adapter, oldItem, newItem);
                final PhaseContext<@NonNull ?> phaseContext = PhaseTracker.SERVER.getPhaseContext();
                phaseContext.getTransactor().logSlotTransaction(phaseContext, newTransaction, (AbstractContainerMenu) (Object) this);
            } catch (final IndexOutOfBoundsException e) {
                SpongeCommon.logger().error("SlotIndex out of LensBounds! Did the Container change after creation?", e);
            }
        }
    }

    // TODO check if addListener with existing listener needs to resend

}
