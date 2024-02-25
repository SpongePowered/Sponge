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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
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
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.InventoryMenuBridge;
import org.spongepowered.common.bridge.world.inventory.container.MenuBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.ResultingTransactionBySideEffect;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.context.transaction.effect.InventoryEffect;
import org.spongepowered.common.event.tracking.phase.tick.TileEntityTickContext;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_Inventory implements TrackedContainerBridge, InventoryAdapter {

    //@formatter:off
    @Final @Shadow private NonNullList<ItemStack> lastSlots;
    @Final @Shadow public List<Slot> slots;
    @Final @Shadow private List<ContainerListener> containerListeners;
    @Final @Shadow private List<DataSlot> dataSlots;

    @Shadow protected abstract ItemStack shadow$doClick(int param0, int param1, ClickType param2, Player param3);
    //@formatter:on

    private boolean impl$isClicking; // Menu Callbacks are only called when clicking in a container
    // Detects if a mod overrides detectAndSendChanges
    private boolean impl$captureSuccess = false;

    @Override
    public boolean bridge$capturePossible() {
        return this.impl$captureSuccess;
    }

    // Injects/Redirects -------------------------------------------------------------------------

    @Redirect(method = "doClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V"
        ),
        slice = @Slice(
            from = @At(value = "FIELD",
                target = "Lnet/minecraft/world/inventory/ClickType;SWAP:Lnet/minecraft/world/inventory/ClickType;"
            ),
            to = @At(value = "FIELD",
                target = "Lnet/minecraft/world/inventory/ClickType;CLONE:Lnet/minecraft/world/inventory/ClickType;"
            )
        )
    )
    private void impl$handleSwapOnSetItem(final Inventory inv, final int index, final ItemStack newStack) {
        if (!PhaseTracker.SERVER.onSidedThread() || inv.player.inventoryMenu == inv.player.containerMenu || Inventory.isHotbarSlot(index)) {
            inv.setItem(index, newStack);
        } else {
            final ItemStackSnapshot oldItem = ItemStackUtil.snapshotOf(inv.getItem(index));
            final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(newStack);
            inv.setItem(index, newStack);
            this.impl$captureSwap(index, inv, oldItem, newItem);
        }
    }

    @Redirect(method = "doClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"
        ),
        slice = @Slice(
            from = @At(value = "FIELD",
                target = "Lnet/minecraft/world/inventory/ClickType;SWAP:Lnet/minecraft/world/inventory/ClickType;"
            ),
            to = @At(value = "FIELD",
                target = "Lnet/minecraft/world/inventory/ClickType;CLONE:Lnet/minecraft/world/inventory/ClickType;"
            )
        )
    )
    private ItemStack impl$handleSwapOnSplit(final ItemStack origin, final int splitOff, final int clickedIndex, final int swappedIndex, final ClickType clickType, final Player player) {
        final Inventory inv = player.inventory;
        if (!PhaseTracker.SERVER.onSidedThread() || inv.player.inventoryMenu == inv.player.containerMenu || Inventory.isHotbarSlot(swappedIndex)) {
            return origin.split(splitOff);
        } else {
            final ItemStackSnapshot oldItem = ItemStackUtil.snapshotOf(origin);
            final ItemStack newStack = origin.split(splitOff);
            final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(newStack);
            this.impl$captureSwap(swappedIndex, inv, oldItem, newItem);
            return newStack;
        }
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
    private boolean impl$verifyReadOnlyMenu(final Slot slot, final Player playerIn) {
        if (((LevelBridge) playerIn.level).bridge$isFake()) {
            slot.mayPickup(playerIn);
        }
        if (((MenuBridge) this).bridge$isReadonlyMenu(slot)) {
            ((MenuBridge) this).bridge$refreshListeners();
            return false;
        }
        return slot.mayPickup(playerIn);
    }

    // ClickType.QUICK_MOVE (for Crafting) -------------------------
    // Called when Shift-Crafting - 2 Injection points
    // Crafting continues until the returned ItemStack is empty OR the returned ItemStack is not the same as the item in the output slot
    @Redirect(method = "doClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack impl$transferStackInSlot(final AbstractContainerMenu thisContainer, final Player player, final int slotId) {
        final ItemStack result = thisContainer.quickMoveStack(player, slotId);
        // Crafting on Serverside?
        if (((LevelBridge) player.level).bridge$isFake() || !(thisContainer.getSlot(slotId) instanceof ResultSlot)) {
            return result;
        }
        this.bridge$detectAndSendChanges(true);
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        TrackingUtil.processBlockCaptures(context); // ClickContainerEvent -> CraftEvent -> PreviewEvent
        // result is modified by the ClickMenuTransaction
        return result;
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
            this.impl$isClicking = true;
            return this.shadow$doClick(slotId, dragType, clickType, player);
        } finally {
            this.impl$isClicking = false;
        }

    }

    // broadcastChanges

    /**
     * We inject at head and cancel for the server side, but on client side, we
     * are still the same class, so we must consider that when we're on the
     * client thread, don't perform any logic.
     *
     * @author gabizou
     */
    @Inject(method = "broadcastChanges", at = @At("HEAD"), cancellable = true)
    private void impl$broadcastChangesWithTransactions(final CallbackInfo ci) {
        if (!PhaseTracker.SERVER.onSidedThread()) {
            return;
        }
        this.bridge$detectAndSendChanges(false);
        this.impl$captureSuccess = true; // Detect mod overrides
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

            // Only call Menu Callbacks when clicking
            if (this.impl$isClicking && menu != null && !menu.onChange(newStack, oldStack, (org.spongepowered.api.item.inventory.Container) this, i, slot)) {
                this.lastSlots.set(i, oldStack.copy());  // revert changes
                this.impl$sendSlotContents(i, oldStack); // Send reverted slots to clients
            } else {
                this.impl$capture(i, newStack, oldStack); // Capture changes for inventory events

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

    private void impl$sendSlotContents(final Integer i, final ItemStack oldStack) {

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
        final PhaseContext<?> phaseContext = PhaseTracker.SERVER.getPhaseContext();
        if (PhaseTracker.SERVER.onSidedThread() &&
                 !(phaseContext.isRestoring() // do not capture when block restoring & initial sync on inventory open
                || phaseContext instanceof TileEntityTickContext)) { // do not capture for open inventories when ticking BlockEntities
            final ItemStackSnapshot oldItem = ItemStackUtil.snapshotOf(oldStack);
            final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(newStack);
            try {
                final org.spongepowered.api.item.inventory.Slot adapter = this.inventoryAdapter$getSlot(index).get();
                final SlotTransaction newTransaction = new SlotTransaction(adapter, oldItem, newItem);
                phaseContext.getTransactor().logSlotTransaction(phaseContext, newTransaction, (AbstractContainerMenu) (Object) this);
            } catch (final IndexOutOfBoundsException e) {
                SpongeCommon.logger().error("SlotIndex out of LensBounds! Did the Container change after creation?", e);
            }
        }
    }

    private void impl$captureSwap(final int index, final Inventory inv, final ItemStackSnapshot oldItem, final ItemStackSnapshot newItem) {
        final PhaseContext<@NonNull ?> phaseContext = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = phaseContext.getTransactor();
        final org.spongepowered.api.item.inventory.Slot adapter = ((InventoryAdapter) inv).inventoryAdapter$getSlot(index).get();
        final SlotTransaction newTransaction = new SlotTransaction(adapter, oldItem, newItem);
        transactor.logSlotTransaction(phaseContext, newTransaction, (AbstractContainerMenu) (Object) this);
        transactor.pushEffect(new ResultingTransactionBySideEffect(InventoryEffect.getInstance()));
    }

    // TODO check if addListener with existing listener needs to resend

}
