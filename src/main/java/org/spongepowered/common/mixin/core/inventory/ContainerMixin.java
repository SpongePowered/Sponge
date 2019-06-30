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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.SlotCollectionIterator;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(value = Container.class, priority = 998)
public abstract class ContainerMixin implements org.spongepowered.api.item.inventory.Container, ContainerBridge, CarriedInventory<Carrier>, MinecraftInventoryAdapter {

    @Shadow public List<Slot> inventorySlots;
    @Shadow public NonNullList<ItemStack> inventoryItemStacks;
    @Shadow public int windowId;
    @Shadow protected List<IContainerListener> listeners;
    @Shadow public abstract NonNullList<ItemStack> getInventory();
    @Shadow public abstract Slot shadow$getSlot(int slotId);
    @Shadow protected abstract void resetDrag();
    @Shadow public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickTypeIn, final EntityPlayer player) {
        throw new IllegalStateException("Shadowed.");
    }

    private boolean impl$spectatorChest;
    private boolean impl$dirty = true;
    private boolean impl$dropCancelled = false;
    @Nullable private ItemStackSnapshot impl$itemStackSnapshot;
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
    private Fabric impl$fabric;
    private SlotProvider impl$slots;
    private Lens impl$lens;
    private boolean impl$initialized;
    private Map<Integer, SlotAdapter> impl$adapters = new HashMap<>();
    private InventoryArchetype impl$archetype;
    protected Optional<Carrier> impl$carrier = Optional.empty();
    protected Optional<Predicate<EntityPlayer>> impl$canInteractWithPredicate = Optional.empty();
    @Nullable private PluginContainer impl$plugin = null;
    private LinkedHashMap<IInventory, Set<Slot>> impl$allInventories = new LinkedHashMap<>();

    /*
    Named specifically for sponge to avoid potential illegal access errors when a mod container
    implements an interface that adds a defaulted method. Due to the JVM and compiled bytecode,
    this could be called in the event the interface with the defaulted method doesn't get
    overridden in the subclass, and therefor, will call the superclass (this class) method, and
    then bam... error.
    More specifically fixes: https://github.com/BuildCraft/BuildCraft/issues/4005
     */
    private void impl$spongeInit() {
        if (this.impl$initialized && !this.impl$dirty) {
            return;
        }

        this.impl$dirty = false;
        this.impl$initialized = true;
        this.impl$adapters.clear();
        this.impl$fabric = MinecraftFabric.of(this);
        this.impl$slots = ContainerUtil.countSlots((Container) (Object) this, this.impl$fabric);
        this.impl$lens = null;
        this.impl$lens = this.impl$spectatorChest ? null : ContainerUtil.getLens(this.impl$fabric, (Container) (Object) this, this.impl$slots); // TODO handle spectator
        this.impl$archetype = ContainerUtil.getArchetype((Container) (Object) this);
        this.impl$carrier = Optional.ofNullable(ContainerUtil.getCarrier(this));

        // If we know the lens, we can cache the adapters now
        if (this.impl$lens != null) {
            for (final org.spongepowered.api.item.inventory.Slot slot : new SlotCollectionIterator(this, this.impl$fabric, this.impl$lens, this.impl$slots)) {
                this.impl$adapters.put(((SlotAdapter) slot).slotNumber, (SlotAdapter) slot);
            }
        }

        this.impl$allInventories.clear();
        this.inventorySlots.forEach(slot -> this.impl$allInventories.computeIfAbsent(slot.inventory, (i) -> new HashSet<>()).add(slot));

    }

    @Override
    public InventoryArchetype getArchetype() {
        this.impl$spongeInit();
        return this.impl$archetype;
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
        this.detectAndSendChanges(false);
        this.impl$captureSuccess = true; // Detect mod overrides
    }

    @Override
    public boolean capturePossible() {
        return this.impl$captureSuccess;
    }

    @Override
    public void detectAndSendChanges(final boolean captureOnly) {
        this.impl$spongeInit();

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
                        adapter = this.getContainerSlot(i);
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
        this.markClean();
    }

    protected void markClean() {
    }

    @Inject(method = "addSlotToContainer", at = @At(value = "HEAD"))
    public void onAddSlotToContainer(final Slot slotIn, final CallbackInfoReturnable<Slot> cir) {
        this.impl$dirty = true;
    }

    @Inject(method = "putStackInSlot", at = @At(value = "HEAD") )
    public void onPutStackInSlot(final int slotId, final ItemStack itemstack, final CallbackInfo ci) {
        if (this.impl$captureInventory) {
            this.impl$spongeInit();

            final Slot slot = shadow$getSlot(slotId);
            if (slot != null) {
                final ItemStackSnapshot originalItem = slot.getStack().isEmpty() ? ItemStackSnapshot.NONE
                                                                                 : ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot();
                final ItemStackSnapshot newItem =
                        itemstack.isEmpty() ? ItemStackSnapshot.NONE : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();

                final org.spongepowered.api.item.inventory.Slot adapter = this.getContainerSlot(slotId);
                this.impl$capturedSlotTransactions.add(new SlotTransaction(adapter, originalItem, newItem));
            }
        }
    }

    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;", ordinal = 0))
    public EntityItem onDragDrop(final EntityPlayer player, final ItemStack itemStackIn, final boolean unused) {
        final ItemStackSnapshot original = ItemStackUtil.snapshotOf(itemStackIn);
        final EntityItem entityItem = player.dropItem(itemStackIn, unused);
        if (!((PlayerEntityBridge) player).shouldRestoreInventory()) {
            return entityItem;
        }
        if (entityItem  == null) {
            this.impl$dropCancelled = true;
            PacketPhaseUtil.handleCustomCursor((EntityPlayerMP) player, original);
        }
        return entityItem;
    }

    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;", ordinal = 1))
    public EntityItem onDragDropSplit(final EntityPlayer player, final ItemStack itemStackIn, final boolean unused) {
        final EntityItem entityItem = player.dropItem(itemStackIn, unused);
        if (!((PlayerEntityBridge) player).shouldRestoreInventory()) {
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
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(-1, -1, original));
        }
        ((PlayerEntityBridge) player).shouldRestoreInventory(false);
        return entityItem;
    }

    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;setItemStack(Lnet/minecraft/item/ItemStack;)V", ordinal = 1))
    public void onDragCursorClear(final InventoryPlayer inventoryPlayer, final ItemStack itemStackIn) {
        if (!this.impl$dropCancelled || !((PlayerEntityBridge) inventoryPlayer.player).shouldRestoreInventory()) {
            inventoryPlayer.setItemStack(itemStackIn);
        }
        ((PlayerEntityBridge) inventoryPlayer.player).shouldRestoreInventory(false);
        this.impl$dropCancelled = false;
    }

    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Slot;canTakeStack(Lnet/minecraft/entity/player/EntityPlayer;)Z", ordinal = 4))
    public boolean onCanTakeStack(final Slot slot, final EntityPlayer playerIn) {
        final boolean result = slot.canTakeStack(playerIn);
        if (result) {
            this.impl$itemStackSnapshot = ItemStackUtil.snapshotOf(slot.getStack());
            this.impl$lastSlotUsed = slot;
        } else {
            this.impl$itemStackSnapshot = null;
            this.impl$lastSlotUsed = null;
        }
        return result;
    }

    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;", ordinal = 3))
    public EntityItem onThrowClick(final EntityPlayer player, final ItemStack itemStackIn, final boolean unused) {
        final EntityItem entityItem = player.dropItem(itemStackIn, true);
        if (entityItem == null && ((PlayerEntityBridge) player).shouldRestoreInventory()) {
            final ItemStack original = ItemStackUtil.toNative(this.impl$itemStackSnapshot.createStack());
            this.impl$lastSlotUsed.putStack(original);
            player.openContainer.detectAndSendChanges();
            ((EntityPlayerMP) player).isChangingQuantityOnly = false;
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(player.openContainer.windowId, this.impl$lastSlotUsed.slotNumber, original));
        }
        this.impl$itemStackSnapshot = null;
        this.impl$lastSlotUsed = null;
        ((PlayerEntityBridge) player).shouldRestoreInventory(false);
        return entityItem;
    }

    @Redirect(method = "slotChangedCraftingGrid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/InventoryCraftResult;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V"))
    private void beforeSlotChangedCraftingGrid(final InventoryCraftResult output, final int index, final ItemStack itemstack)
    {
        if (!this.impl$captureInventory) {
            // Capture Inventory is true when caused by a vanilla inventory packet
            // This is to prevent infinite loops when a client mod re-requests the recipe result after we modified/cancelled it
            output.setInventorySlotContents(index, itemstack);
            return;
        }
        this.impl$spongeInit();
        this.impl$capturedCraftPreviewTransactions.clear();

        final ItemStackSnapshot orig = ItemStackUtil.snapshotOf(output.getStackInSlot(index));
        output.setInventorySlotContents(index, itemstack);
        final ItemStackSnapshot repl = ItemStackUtil.snapshotOf(output.getStackInSlot(index));

        final SlotAdapter slot = this.impl$adapters.get(index);
        this.impl$capturedCraftPreviewTransactions.add(new SlotTransaction(slot, orig, repl));
    }

    @Inject(method = "slotChangedCraftingGrid", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void afterSlotChangedCraftingGrid(
        final World world, final EntityPlayer player, final InventoryCrafting craftingInventory, final InventoryCraftResult output, final CallbackInfo ci)
    {
        if (this.impl$firePreview && !this.impl$capturedCraftPreviewTransactions.isEmpty()) {
            final Inventory inv = this.query(QueryOperationTypes.INVENTORY_TYPE.of(CraftingInventory.class));
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

    private ItemStack previousCursor;

    @Override
    public ItemStack getPreviousCursor() {
        return this.previousCursor;
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;grow(I)V", ordinal = 1))
    private void beforeOnTakeClickWithItem(
        final int slotId, final int dragType, final ClickType clickTypeIn, final EntityPlayer player, final CallbackInfoReturnable<Integer> cir) {
       this.previousCursor = player.inventory.getItemStack().copy(); // capture previous cursor for CraftItemEvent.Craft
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;setItemStack(Lnet/minecraft/item/ItemStack;)V", ordinal = 3))
    private void beforeOnTakeClick(
        final int slotId, final int dragType, final ClickType clickTypeIn, final EntityPlayer player, final CallbackInfoReturnable<Integer> cir) {
        this.previousCursor = player.inventory.getItemStack().copy(); // capture previous cursor for CraftItemEvent.Craft
    }

    @Redirect(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Slot;onTake(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", ordinal = 5))
    private ItemStack redirectOnTakeThrow(final Slot slot, final EntityPlayer player, final ItemStack stackOnCursor) {
        this.impl$lastCraft = null;
        final ItemStack result = slot.onTake(player, stackOnCursor);
        if (this.impl$lastCraft != null) {
            if (slot instanceof SlotCrafting) {
                if (this.impl$lastCraft.isCancelled()) {
                    stackOnCursor.setCount(0); // do not drop crafted item when cancelled
                }
            }
        }
        return result;
    }

    @Inject(method = "slotClick", at = @At("RETURN"))
    private void onReturn(final int slotId, final int dragType, final ClickType clickTypeIn, final EntityPlayer player, final CallbackInfoReturnable<ItemStack> cir) {
        // Reset variables needed for CraftItemEvent.Craft
        this.impl$lastCraft = null;
        this.previousCursor = null;
    }


    @Redirect(method = "slotClick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectTransferStackInSlot(final Container thisContainer, final EntityPlayer player, final int slotId) {
        final Slot slot = thisContainer.getSlot(slotId);
        if (!(slot instanceof SlotCrafting)) {
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
    public boolean capturingInventory() {
        return this.impl$captureInventory;
    }

    @Override
    public void setCaptureInventory(final boolean flag) {
        this.impl$captureInventory = flag;
    }

    @Override
    public void setSpectatorChest(final boolean spectatorChest) {
        this.impl$spectatorChest = spectatorChest;
    }

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.impl$capturedSlotTransactions;
    }

    @Override
    public List<SlotTransaction> getPreviewTransactions() {
        return this.impl$capturedCraftPreviewTransactions;
    }

    @Override
    public void setLastCraft(final CraftItemEvent.Craft event) {
        this.impl$lastCraft = event;
    }

    @Override
    public void setFirePreview(final boolean firePreview) {
        this.impl$firePreview = firePreview;
    }

    @Override
    public void setShiftCrafting(final boolean flag) {
        this.impl$shiftCraft = flag;
    }

    @Override
    public boolean isShiftCrafting() {
        return this.impl$shiftCraft;
    }

    @Override
    public SlotProvider bridge$getSlotProvider() {
        this.impl$spongeInit();
        return this.impl$slots;
    }

    @Override
    public Lens bridge$getRootLens() {
        this.impl$spongeInit();
        return this.impl$lens;
    }

    @Override
    public Fabric bridge$getFabric() {
        this.impl$spongeInit();
        return this.impl$fabric;
    }

    @Override
    public void setCanInteractWith(@Nullable final Predicate<EntityPlayer> predicate) {
        this.impl$canInteractWithPredicate = Optional.ofNullable(predicate); // TODO mixin into all classes extending container
    }

    @Override
    public Optional<Carrier> getCarrier() {
        return this.impl$carrier;
    }

    @Override
    public org.spongepowered.api.item.inventory.Slot getContainerSlot(final int slot) {
        final org.spongepowered.api.item.inventory.Slot adapter = this.impl$adapters.get(slot);
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
    public void setPlugin(final PluginContainer plugin) {
        this.impl$plugin = plugin;
    }

    @Override
    public Location<org.spongepowered.api.world.World> getOpenLocation() {
        return this.impl$lastOpenLocation;
    }

    @Override
    public void setOpenLocation(final Location<org.spongepowered.api.world.World> loc) {
        this.impl$lastOpenLocation = loc;
    }

    @Override
    public void setInUse(final boolean inUse) {
        this.impl$inUse = inUse;
    }

    @Override
    public boolean isInUse() {
        return this.impl$inUse;
    }

    @Override
    public boolean isViewedSlot(final org.spongepowered.api.item.inventory.Slot slot) {
        this.impl$spongeInit();
        if (slot instanceof Slot) {
            final Set<Slot> set = this.impl$allInventories.get(((Slot) slot).inventory);
            if (set != null) {
                if (set.contains(slot)) {
                    if (this.impl$allInventories.size() == 1) {
                        return true;
                    }
                    // TODO better detection of viewer inventory - needs tracking of who views a container
                    // For now assume that a player inventory is always the viewers inventory
                    if (((Slot) slot).inventory.getClass() != InventoryPlayer.class) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
