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
package org.spongepowered.common.mixin.core.item.inventory;

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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinContainer;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = Container.class, priority = 998)
@Implements({@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$")})
public abstract class MixinContainer implements org.spongepowered.api.item.inventory.Container, IMixinContainer, CarriedInventory<Carrier> {

    @Shadow public List<Slot> inventorySlots;
    @Shadow public NonNullList<ItemStack> inventoryItemStacks ;
    @Shadow public int windowId;
    @Shadow protected List<IContainerListener> listeners;
    private boolean spectatorChest;
    private boolean dirty = true;
    private boolean crafting = false;
    @Nullable private CraftItemEvent.Craft lastCraft = null;

    @Shadow
    public abstract NonNullList<ItemStack> getInventory();

    @Shadow
    public abstract Slot getSlot(int slotId);

    @Shadow
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        throw new IllegalStateException("Shadowed.");
    }

    @Shadow protected abstract void resetDrag();

    private boolean captureInventory = false;
    private boolean shiftCraft = false;
    //private boolean postPreCraftEvent = true; // used to prevent multiple craft events to fire when setting multiple slots simultaneously
    private List<SlotTransaction> capturedSlotTransactions = new ArrayList<>();
    private List<SlotTransaction> capturedCraftShiftTransactions = new ArrayList<>();
    private List<SlotTransaction> capturedCraftPreviewTransactions = new ArrayList<>();
    private Fabric<IInventory> fabric;
    private SlotProvider<IInventory, ItemStack> slots;
    private Lens<IInventory, ItemStack> lens;
    private boolean initialized;
    private Map<Integer, SlotAdapter> adapters = new HashMap<>();
    private InventoryArchetype archetype;
    protected Optional<Carrier> carrier = Optional.empty();
    protected Optional<Predicate<EntityPlayer>> canInteractWithPredicate = Optional.empty();
    @Nullable private PluginContainer plugin = null;

    private void init() {
        if (this.initialized && !this.dirty) {
            return;
        }

        this.dirty = false;
        this.initialized = true;
        this.adapters.clear();
        this.fabric = MinecraftFabric.of(this);
        this.slots = ContainerUtil.countSlots((Container) (Object) this, this.fabric);
        this.lens = null;
        this.lens = this.spectatorChest ? null : ContainerUtil.getLens(this.fabric, (Container) (Object) this, this.slots); // TODO handle spectator
        this.archetype = ContainerUtil.getArchetype((Container) (Object) this);
        this.carrier = Optional.ofNullable(ContainerUtil.getCarrier(this));

        // If we know the lens, we can cache the adapters now
        if (this.lens != null) {
            for (org.spongepowered.api.item.inventory.Slot slot : new SlotCollectionIterator<>(this, this.fabric, this.lens, this.slots)) {
                this.adapters.put(((SlotAdapter) slot).slotNumber, (SlotAdapter) slot);
            }
        }
    }

    @Override
    public InventoryArchetype getArchetype() {
        this.init();
        return this.archetype;
    }

    /**
     * @author bloodmc
     * @reason If listener already exists, avoid firing an exception
     * and simply send the inventory changes to client.
     */
    @Overwrite
    public void addListener(IContainerListener listener) {
        Container container = (Container) (Object) this;
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
    }

    @Override
    public void detectAndSendChanges(boolean captureOnly) {
        this.init();

        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            final Slot slot = this.inventorySlots.get(i);
            final ItemStack itemstack = slot.getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {

                // Sponge start
                if (this.captureInventory) {
                    final ItemStackSnapshot originalItem = itemstack1.isEmpty() ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) itemstack1).createSnapshot();
                    final ItemStackSnapshot newItem = itemstack.isEmpty() ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();

                    org.spongepowered.api.item.inventory.Slot adapter = null;
                    try {
                        adapter = this.getContainerSlot(i);
                        if (this.shiftCraft) {
                            this.capturedCraftShiftTransactions.add(new SlotTransaction(adapter, originalItem, newItem));
                        } else {
                            this.capturedSlotTransactions.add(new SlotTransaction(adapter, originalItem, newItem));
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

                for (IContainerListener listener : this.listeners) {
                    listener.sendSlotContents((Container) (Object) this, i, itemstack1);
                }
            }
        }
    }

    @Inject(method = "addSlotToContainer", at = @At(value = "HEAD"))
    public void onAddSlotToContainer(Slot slotIn, CallbackInfoReturnable<Slot> cir) {
        this.dirty = true;
    }

    @Inject(method = "putStackInSlot", at = @At(value = "HEAD") )
    public void onPutStackInSlot(int slotId, ItemStack itemstack, CallbackInfo ci) {
        if (this.captureInventory) {
            this.init();

            final Slot slot = getSlot(slotId);
            if (slot != null) {
                ItemStackSnapshot originalItem = slot.getStack().isEmpty() ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot();
                ItemStackSnapshot newItem =
                        itemstack.isEmpty() ? ItemStackSnapshot.NONE : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();

                org.spongepowered.api.item.inventory.Slot adapter = this.getContainerSlot(slotId);
                this.capturedSlotTransactions.add(new SlotTransaction(adapter, originalItem, newItem));
            }
        }
    }

    @Redirect(method = "slotChangedCraftingGrid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/InventoryCraftResult;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V"))
    private void beforeSlotChangedCraftingGrid(InventoryCraftResult output, int index, ItemStack itemstack)
    {
        if (!this.captureInventory) {
            // Capture Inventory is true when caused by a vanilla inventory packet
            // This is to prevent infinite loops when a client mod re-requests the recipe result after we modified/cancelled it
            return;
        }
        this.init();
        this.capturedCraftPreviewTransactions.clear();

        ItemStackSnapshot orig = ItemStackUtil.snapshotOf(output.getStackInSlot(index));
        output.setInventorySlotContents(index, itemstack);
        ItemStackSnapshot repl = ItemStackUtil.snapshotOf(output.getStackInSlot(index));

        SlotAdapter slot = this.adapters.get(index);
        this.capturedCraftPreviewTransactions.add(new SlotTransaction(slot, orig, repl));
    }

    @Inject(method = "slotChangedCraftingGrid", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void afterSlotChangedCraftingGrid(World world, EntityPlayer player, InventoryCrafting craftingInventory, InventoryCraftResult output, CallbackInfo ci)
    {
        if (!this.capturedCraftPreviewTransactions.isEmpty()) {
            Inventory inv = this.query(QueryOperationTypes.INVENTORY_TYPE.of(CraftingInventory.class));
            if (!(inv instanceof CraftingInventory)) {
                SpongeImpl.getLogger().warn("Detected crafting but Sponge could not get a CraftingInventory for " + this.getClass().getName());
                return;
            }
            SlotTransaction previewTransaction = this.capturedCraftPreviewTransactions.get(this.capturedCraftPreviewTransactions.size() - 1);

            IRecipe recipe = CraftingManager.findMatchingRecipe(craftingInventory, world);
            SpongeCommonEventFactory.callCraftEventPre(player, ((CraftingInventory) inv), previewTransaction, ((CraftingRecipe) recipe),
                    ((Container)(Object) this), this.capturedCraftPreviewTransactions);
            this.capturedCraftPreviewTransactions.clear();
        }
    }

    private ItemStack previousCursor;

    @Override
    public ItemStack getPreviousCursor() {
        return this.previousCursor;
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;grow(I)V", ordinal = 1))
    private void beforeOnTakeClickWithItem(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<Integer> cir) {
       this.previousCursor = player.inventory.getItemStack().copy(); // capture previous cursor for CraftItemEvent.Craft
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;setItemStack(Lnet/minecraft/item/ItemStack;)V", ordinal = 3))
    private void beforeOnTakeClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<Integer> cir) {
        this.previousCursor = player.inventory.getItemStack().copy(); // capture previous cursor for CraftItemEvent.Craft
    }

    @Redirect(method = "slotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Slot;onTake(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", ordinal = 5))
    private ItemStack redirectOnTakeThrow(Slot slot, EntityPlayer player, ItemStack stackOnCursor) {
        this.lastCraft = null;
        ItemStack result = slot.onTake(player, stackOnCursor);
        if (lastCraft != null) {
            if (slot instanceof SlotCrafting) {
                if (this.lastCraft.isCancelled()) {
                    stackOnCursor.setCount(0); // do not drop crafted item when cancelled
                }
            }
        }
        return result;
    }

    @Inject(method = "slotClick", at = @At("RETURN"))
    private void onReturn(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        // Reset variables needed for CraftItemEvent.Craft
        this.lastCraft = null;
        this.previousCursor = null;
    }


    @Redirect(method = "slotClick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectTransferStackInSlot(Container thisContainer, EntityPlayer player, int slotId) {
        this.lastCraft = null;
        this.shiftCraft = true;
        ItemStack result = thisContainer.transferStackInSlot(player, slotId);
        if (lastCraft != null) {
            if (this.lastCraft.isCancelled()) {
                result = ItemStack.EMPTY; // Return empty to stop shift-crafting
            }
        }
        this.shiftCraft = false;

        return result;
    }

    @Override
    public boolean capturingInventory() {
        return this.captureInventory;
    }

    @Override
    public void setCaptureInventory(boolean flag) {
        this.captureInventory = flag;
    }

    @Override
    public void setSpectatorChest(boolean spectatorChest) {
        this.spectatorChest = spectatorChest;
    }

    @Override
    public List<SlotTransaction> getCapturedTransactions() {
        return this.capturedSlotTransactions;
    }

    @Override
    public void setLastCraft(CraftItemEvent.Craft event) {
        this.lastCraft = event;
    }

    @Override
    public void setShiftCrafting(boolean flag) {
        this.shiftCraft = flag;
    }

    @Override
    public boolean isShiftCrafting() {
        return this.shiftCraft;
    }

    public SlotProvider<IInventory, ItemStack> inventory$getSlotProvider() {
        this.init();
        return this.slots;
    }

    public Lens<IInventory, ItemStack> inventory$getRootLens() {
        this.init();
        return this.lens;
    }

    public Fabric<IInventory> inventory$getFabric() {
        this.init();
        return this.fabric;
    }

    @Override
    public void setCanInteractWith(@Nullable Predicate<EntityPlayer> predicate) {
        this.canInteractWithPredicate = Optional.ofNullable(predicate); // TODO mixin into all classes extending container
    }

    @Override
    public Optional<Carrier> getCarrier() {
        return this.carrier;
    }

    @Override
    public org.spongepowered.api.item.inventory.Slot getContainerSlot(int slot) {
        org.spongepowered.api.item.inventory.Slot adapter = this.adapters.get(slot);
        if (adapter == null) // Slot is not in Lens
        {
            Slot mcSlot = this.inventorySlots.get(slot); // Try falling back to vanilla slot
            if (mcSlot == null)
            {
                SpongeImpl.getLogger().warn("Could not find slot #%s in Container %s", slot, getClass().getName());
                return null;
            }
            return ((org.spongepowered.api.item.inventory.Slot) mcSlot);
        }
        return adapter;
    }

    @Override
    public void setPlugin(PluginContainer plugin) {
        this.plugin = plugin;
    }
}
