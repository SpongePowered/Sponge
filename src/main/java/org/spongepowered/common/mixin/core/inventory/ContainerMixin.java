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
package org.spongepowered.common.mixin.core.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
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
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.LensProviderBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.SlotCollectionIterator;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = Container.class, priority = 998)
public abstract class ContainerMixin implements ContainerBridge, InventoryAdapter, TrackedInventoryBridge, InventoryAdapterBridge {

    @Shadow public List<Slot> inventorySlots;
    @Shadow public NonNullList<ItemStack> inventoryItemStacks;
    @Shadow protected List<IContainerListener> listeners;
    @Shadow public abstract NonNullList<ItemStack> getInventory();
    @Shadow public abstract Slot shadow$getSlot(int slotId);
    @Shadow public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player) {
        throw new IllegalStateException("Shadowed.");
    }

    private boolean impl$spectatorChest;
    private boolean impl$dropCancelled = false;
    private ItemStackSnapshot impl$itemStackSnapshot = ItemStackSnapshot.NONE;
    @Nullable private Slot impl$lastSlotUsed = null;
    @Nullable private CraftItemEvent.Craft impl$lastCraft = null;
    @Nullable private Location<org.spongepowered.api.world.World> impl$lastOpenLocation;
    private boolean impl$firePreview = true;
    private boolean impl$inUse = false;
    private boolean impl$captureSuccess = false;
    private boolean impl$captureInventory = false;
    private boolean impl$shiftCraft = false;
    //private boolean postPreCraftEvent = true; // used to prevent multiple craft events to fire when setting multiple slots simultaneously
    private List<SlotTransaction> impl$capturedSlotTransactions = new ArrayList<>();
    private List<SlotTransaction> impl$capturedCraftShiftTransactions = new ArrayList<>();
    private List<SlotTransaction> impl$capturedCraftPreviewTransactions = new ArrayList<>();
    private boolean impl$isLensInitialized;
    @Nullable private Map<Integer, SlotAdapter> impl$adapters;
    @Nullable private InventoryArchetype impl$archetype;
    @Nullable private Carrier impl$carrier;
    @Nullable Predicate<PlayerEntity> impl$canInteractWithPredicate;
    @Nullable private LinkedHashMap<IInventory, Set<Slot>> impl$allInventories;
    @Nullable private ItemStack impl$previousCursor;

    @Override
    public SlotProvider bridge$generateSlotProvider() {
        return ContainerUtil.countSlots((Container) (Object) this, bridge$getFabric());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Lens bridge$generateLens(SlotProvider slots) {
        if (this.impl$isLensInitialized) {
            return null; // Means that we've tried to generate a lens before, but it was null. And because the lens is null,
            // the generate will try again. So, we stop trying to generate it.
        }
        this.impl$isLensInitialized = true;
        final Fabric fabric = bridge$getFabric();
        final Lens lens;
        if (this.impl$spectatorChest) {
            lens = null;
        } else {
            if (this instanceof LensProviderBridge) {
                // TODO LensProviders for all Vanilla Containers
                lens = ((LensProviderBridge) this).bridge$rootLens(fabric, this);
            } else if (getInventory().size() == 0) {
                lens = new DefaultEmptyLens(this); // Empty Container
            } else {
                lens = ContainerUtil.generateLens((Container) (Object) this, slots);
            }
        }
        return lens;
    }

    @SuppressWarnings("ConstantConditions")
    private Map<Integer, SlotAdapter> impl$getAdapters() {
        if (this.impl$adapters == null) {
            this.impl$adapters = new Int2ObjectArrayMap<>();
            // If we know the lens, we can cache the adapters now

            final Lens lens = bridge$getRootLens();
            if (lens != null) {
                final SlotCollectionIterator iter = new SlotCollectionIterator((Inventory) this, bridge$getFabric(), lens, bridge$getSlotProvider());
                for (final org.spongepowered.api.item.inventory.Slot slot : iter) {
                    this.impl$adapters.put(((SlotAdapter) slot).slotNumber, (SlotAdapter) slot);
                }
            }
        }
        return this.impl$adapters;
    }

    @Override
    public InventoryArchetype bridge$getArchetype() {
        if (this.impl$archetype == null) {
            this.impl$archetype = ContainerUtil.getArchetype((Container) (Object) this);
        }
        return this.impl$archetype;
    }

    @Override
    public Optional<Carrier> bridge$getCarrier() {
        if (this.impl$carrier == null) {
            this.impl$carrier = ContainerUtil.getCarrier((org.spongepowered.api.item.inventory.Container) this);
        }
        return Optional.ofNullable(this.impl$carrier);
    }

    @SuppressWarnings("unused")
    @Override
    public LinkedHashMap<IInventory, Set<Slot>> bridge$getInventories() {
        if (this.impl$allInventories == null) {
            this.impl$allInventories = new LinkedHashMap<>();
            this.inventorySlots.forEach(slot -> this.impl$allInventories.computeIfAbsent(slot.inventory, (i) -> new HashSet<>()).add(slot));
        }
        return this.impl$allInventories;
    }

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
        this.impl$captureSuccess = true; // Detect mod overrides
    }

    @Override
    public boolean bridge$capturePossible() {
        return this.impl$captureSuccess;
    }

    @Override
    public void bridge$detectAndSendChanges(final boolean captureOnly) {
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            final Slot slot = this.inventorySlots.get(i);
            final ItemStack itemstack = slot.getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {

                // Sponge start
                if (this.impl$captureInventory) {
                    final ItemStackSnapshot originalItem = itemstack1.isEmpty() ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) itemstack1).createSnapshot();
                    final ItemStackSnapshot newItem = itemstack.isEmpty() ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();

                    org.spongepowered.api.item.inventory.Slot adapter = null;
                    try {
                        adapter = this.bridge$getContainerSlot(i);
                        SlotTransaction newTransaction = new SlotTransaction(adapter, originalItem, newItem);
                        if (this.impl$shiftCraft) {
                            this.impl$capturedCraftShiftTransactions.add(newTransaction);
                        } else {
                            if (!this.impl$capturedCraftPreviewTransactions.isEmpty()) { // Check if Preview transaction is this transaction
                                final SlotTransaction previewTransaction = this.impl$capturedCraftPreviewTransactions.get(0);
                                if (previewTransaction.equals(newTransaction)) {
                                    newTransaction = null;
                                }
                            }
                            if (newTransaction != null) {
                                this.impl$capturedSlotTransactions.add(newTransaction);
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        SpongeImpl.getLogger().error("SlotIndex out of LensBounds! Did the Container change after creation?", e);
                    }

                    // This flag is set only when the client sends an invalid CPacketWindowClickItem packet.
                    // We simply capture in order to send the proper changes back to client.
                    if (captureOnly) {
                        continue;
                    }
                }
                // Sponge end

                itemstack1 = itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                for (final IContainerListener listener : this.listeners) {
                    listener.sendSlotContents((Container) (Object) this, i, itemstack1);
                }
            }
        }
        this.impl$markClean();
    }

    protected void impl$markClean() {
    }

    @Inject(method = "addSlotToContainer", at = @At(value = "HEAD"))
    private void impl$onAddSlotToContainer(final Slot slotIn, final CallbackInfoReturnable<Slot> cir) {
        this.impl$isLensInitialized = false;
        // Reset the lense and slot provider
        bridge$setSlotProvider(null);
        bridge$setLens(null);
        this.impl$adapters = null;
    }

    @Inject(method = "putStackInSlot", at = @At(value = "HEAD") )
    private void impl$addTransaction(final int slotId, final ItemStack itemstack, final CallbackInfo ci) {
        if (this.impl$captureInventory) {
            final Slot slot = shadow$getSlot(slotId);
            if (slot != null) {
                final ItemStackSnapshot originalItem = slot.getStack().isEmpty() ? ItemStackSnapshot.NONE
                                                                                 : ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot();
                final ItemStackSnapshot newItem =
                        itemstack.isEmpty() ? ItemStackSnapshot.NONE : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();

                final org.spongepowered.api.item.inventory.Slot adapter = this.bridge$getContainerSlot(slotId);
                this.impl$capturedSlotTransactions.add(new SlotTransaction(adapter, originalItem, newItem));
            }
        }
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
            this.impl$itemStackSnapshot = ItemStackSnapshot.NONE;
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
        this.impl$itemStackSnapshot = ItemStackSnapshot.NONE;
        this.impl$lastSlotUsed = null;
        ((EntityPlayerBridge) player).bridge$shouldRestoreInventory(false);
        return entityItem;
    }

    @Redirect(method = "slotChangedCraftingGrid",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/inventory/InventoryCraftResult;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V"))
    private void beforeSlotChangedCraftingGrid(final CraftResultInventory output, final int index, final ItemStack itemstack)
    {
        if (!this.impl$captureInventory) {
            // Capture Inventory is true when caused by a vanilla inventory packet
            // This is to prevent infinite loops when a client mod re-requests the recipe result after we modified/cancelled it
            output.setInventorySlotContents(index, itemstack);
            return;
        }
        this.impl$capturedCraftPreviewTransactions.clear();

        final ItemStackSnapshot orig = ItemStackUtil.snapshotOf(output.getStackInSlot(index));
        output.setInventorySlotContents(index, itemstack);
        final ItemStackSnapshot repl = ItemStackUtil.snapshotOf(output.getStackInSlot(index));

        final SlotAdapter slot = this.impl$getAdapters().get(index);
        this.impl$capturedCraftPreviewTransactions.add(new SlotTransaction(slot, orig, repl));
    }

    @Inject(method = "slotChangedCraftingGrid", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void afterSlotChangedCraftingGrid(
        final World world, final PlayerEntity player, final net.minecraft.inventory.CraftingInventory craftingInventory, final CraftResultInventory output, final CallbackInfo ci)
    {
        if (this.impl$firePreview && !this.impl$capturedCraftPreviewTransactions.isEmpty()) {
            final Inventory inv = ((CarriedInventory<?>) this).query(QueryOperationTypes.INVENTORY_TYPE.of(CraftingInventory.class));
            if (!(inv instanceof CraftingInventory)) {
                SpongeImpl.getLogger().warn("Detected crafting but Sponge could not get a CraftingInventory for " + this.getClass().getName());
                return;
            }
            final SlotTransaction previewTransaction = this.impl$capturedCraftPreviewTransactions.get(this.impl$capturedCraftPreviewTransactions.size() - 1);

            final IRecipe recipe = CraftingManager.findMatchingRecipe(craftingInventory, world);
            SpongeCommonEventFactory.callCraftEventPre(player, ((CraftingInventory) inv), previewTransaction, ((CraftingRecipe) recipe),
                    ((Container)(Object) this), this.impl$capturedCraftPreviewTransactions);
            this.impl$capturedCraftPreviewTransactions.clear();
        }
    }


    @Override
    public ItemStack bridge$getPreviousCursor() {
        return this.impl$previousCursor;
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;grow(I)V", ordinal = 1))
    private void beforeOnTakeClickWithItem(
        final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<Integer> cir) {
       this.impl$previousCursor = player.inventory.getItemStack().copy(); // capture previous cursor for CraftItemEvent.Craft
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;setItemStack(Lnet/minecraft/item/ItemStack;)V", ordinal = 3))
    private void beforeOnTakeClick(
        final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<Integer> cir) {
        this.impl$previousCursor = player.inventory.getItemStack().copy(); // capture previous cursor for CraftItemEvent.Craft
    }

    @Redirect(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Slot;onTake(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", ordinal = 5))
    private ItemStack redirectOnTakeThrow(final Slot slot, final PlayerEntity player, final ItemStack stackOnCursor) {
        this.impl$lastCraft = null;
        final ItemStack result = slot.onTake(player, stackOnCursor);
        if (this.impl$lastCraft != null) {
            if (slot instanceof CraftingResultSlot) {
                if (this.impl$lastCraft.isCancelled()) {
                    stackOnCursor.setCount(0); // do not drop crafted item when cancelled
                }
            }
        }
        return result;
    }

    @Inject(method = "slotClick", at = @At("RETURN"))
    private void onReturn(final int slotId, final int dragType, final ClickType clickTypeIn, final PlayerEntity player, final CallbackInfoReturnable<ItemStack> cir) {
        // Reset variables needed for CraftItemEvent.Craft
        this.impl$lastCraft = null;
        this.impl$previousCursor = null;
    }


    @Redirect(method = "slotClick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectTransferStackInSlot(final Container thisContainer, final PlayerEntity player, final int slotId) {
        final Slot slot = thisContainer.getSlot(slotId);
        if (!(slot instanceof CraftingResultSlot)) {
            return thisContainer.transferStackInSlot(player, slotId);
        }
        this.impl$lastCraft = null;
        this.impl$shiftCraft = true;
        ItemStack result = thisContainer.transferStackInSlot(player, slotId);
        if (this.impl$lastCraft != null) {
            if (this.impl$lastCraft.isCancelled()) {
                result = ItemStack.EMPTY; // Return empty to stop shift-crafting
            }
        }
        this.impl$shiftCraft = false;

        return result;
    }

    @Override
    public boolean bridge$capturingInventory() {
        return this.impl$captureInventory;
    }

    @Override
    public void bridge$setCaptureInventory(final boolean flag) {
        this.impl$captureInventory = flag;
    }

    @Override
    public void bridge$setSpectatorChest(final boolean spectatorChest) {
        this.impl$spectatorChest = spectatorChest;
    }

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.impl$capturedSlotTransactions;
    }

    @Override
    public List<SlotTransaction> bridge$getPreviewTransactions() {
        return this.impl$capturedCraftPreviewTransactions;
    }

    @Override
    public void bridge$setLastCraft(final CraftItemEvent.Craft event) {
        this.impl$lastCraft = event;
    }

    @Override
    public void bridge$setFirePreview(final boolean firePreview) {
        this.impl$firePreview = firePreview;
    }

    @Override
    public void bridge$setShiftCrafting(final boolean flag) {
        this.impl$shiftCraft = flag;
    }

    @Override
    public boolean bridge$isShiftCrafting() {
        return this.impl$shiftCraft;
    }

    @Override
    public void bridge$setCanInteractWith(@Nullable final Predicate<PlayerEntity> predicate) {
        this.impl$canInteractWithPredicate = predicate;
    }

    @Override
    public org.spongepowered.api.item.inventory.Slot bridge$getContainerSlot(final int slot) {
        final org.spongepowered.api.item.inventory.Slot adapter = this.impl$getAdapters().get(slot);
        if (adapter == null) // Slot is not in Lens
        {
            if (slot >= this.inventorySlots.size()) {
                SpongeImpl.getLogger().warn("Could not find slot #{} in Container {}", slot, getClass().getName());
                return null;
            }
            final Slot mcSlot = this.inventorySlots.get(slot); // Try falling back to vanilla slot
            if (mcSlot == null)
            {
                SpongeImpl.getLogger().warn("Could not find slot #{} in Container {}", slot, getClass().getName());
                return null;
            }
            return ((org.spongepowered.api.item.inventory.Slot) mcSlot);
        }
        return adapter;
    }

    @Override
    public Location<org.spongepowered.api.world.World> bridge$getOpenLocation() {
        return this.impl$lastOpenLocation;
    }

    @Override
    public void bridge$setOpenLocation(final Location<org.spongepowered.api.world.World> loc) {
        this.impl$lastOpenLocation = loc;
    }

    @Override
    public void bridge$setInUse(final boolean inUse) {
        this.impl$inUse = inUse;
    }

    @Override
    public boolean bridge$isInUse() {
        return this.impl$inUse;
    }

}
