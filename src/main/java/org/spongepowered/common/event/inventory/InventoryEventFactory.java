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
package org.spongepowered.common.event.inventory;

import static org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil.handleCustomCursor;

import net.kyori.adventure.text.Component;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.EnchantItemEvent;
import org.spongepowered.api.event.item.inventory.TransferInventoryEvent;
import org.spongepowered.api.event.item.inventory.UpdateAnvilEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.enchantment.SpongeRandomEnchantmentListBuilder;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class InventoryEventFactory {


    public static boolean callPlayerChangeInventoryPickupPreEvent(final PlayerEntity player, final ItemEntity itemToPickup, final int pickupDelay) {
        final ItemStack stack = itemToPickup.getItem();
        final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
        causeStackManager.pushCause(player);
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
        final ChangeInventoryEvent.Pickup.Pre event =
                SpongeEventFactory.createChangeInventoryEventPickupPre(
                    causeStackManager.getCurrentCause(),
                        Optional.empty(), Collections.singletonList(snapshot), ((Inventory) player.inventory), (Item) itemToPickup, snapshot);
        SpongeCommon.postEvent(event);
        causeStackManager.popCause();
        if (event.isCancelled()) {
            return false;
        }
        if (event.getCustom().isPresent()) {
            final List<ItemStackSnapshot> list = event.getCustom().get();
            if (list.isEmpty()) {
                itemToPickup.getItem().setCount(0);
                return false;
            }

            boolean fullTransfer = true;
            final TrackedInventoryBridge capture = (TrackedInventoryBridge) player.inventory;
            capture.bridge$setCaptureInventory(true);
            for (final ItemStackSnapshot item : list) {
                final org.spongepowered.api.item.inventory.ItemStack itemStack = item.createStack();
                player.inventory.addItemStackToInventory(ItemStackUtil.toNative(itemStack));
                if (!itemStack.isEmpty()) {
                    fullTransfer = false;
                    break;
                }

            }
            capture.bridge$setCaptureInventory(false);
            if (!fullTransfer) {
                for (final SlotTransaction trans : capture.bridge$getCapturedSlotTransactions()) {
                    trans.getSlot().set(trans.getOriginal().createStack());
                }
                return false;
            }
            if (!callPlayerChangeInventoryPickupEvent(player, capture)) {
                return false;
            }
            itemToPickup.getItem().setCount(0);
        }
        return true;
    }


    public static boolean callPlayerChangeInventoryPickupEvent(final PlayerEntity player, final TrackedInventoryBridge inventory) {
        if (inventory.bridge$getCapturedSlotTransactions().isEmpty()) {
            return true;
        }
        PhaseTracker.getCauseStackManager().pushCause(player);
        final ChangeInventoryEvent.Pickup event = SpongeEventFactory.createChangeInventoryEventPickup(PhaseTracker.getCauseStackManager().getCurrentCause(), (Inventory) player.container,
                inventory.bridge$getCapturedSlotTransactions());
        SpongeCommon.postEvent(event);
        PhaseTracker.getCauseStackManager().popCause();
        applyTransactions(event);
        inventory.bridge$getCapturedSlotTransactions().clear();
        return !event.isCancelled();
    }


    public static ItemStack callInventoryPickupEvent(final IInventory inventory, final ItemEntity item, final ItemStack stack) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(inventory);

            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final ChangeInventoryEvent.Pickup.Pre event =
                    SpongeEventFactory.createChangeInventoryEventPickupPre(frame.getCurrentCause(),
                            Optional.empty(), Collections.singletonList(snapshot), (Inventory) inventory, (Item) item, snapshot);
            SpongeCommon.postEvent(event);
            if (event.isCancelled()) {
                return stack;
            }

            final int size = inventory.getSizeInventory();
            final ItemStack[] prevInventory = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                prevInventory[i] = inventory.getStackInSlot(i);
            }

            if (event.getCustom().isPresent()) {
                if (event.getCustom().get().isEmpty()) {
                    return ItemStack.EMPTY;
                }

                boolean fullTransfer = true;
                for (final ItemStackSnapshot snap : event.getCustom().get()) {
                    final ItemStack stackToAdd = ItemStackUtil.fromSnapshotToNative(snap);
                    final ItemStack remaining = HopperTileEntity.putStackInInventoryAllSlots(null, inventory, stackToAdd, null);
                    if (!remaining.isEmpty()) {
                        fullTransfer = false;
                        break;
                    }
                }
                if (!fullTransfer) {
                    for (int i = 0; i < prevInventory.length; i++) {
                        inventory.setInventorySlotContents(i, prevInventory[i]);
                    }
                    return stack;
                }

                if (callInventoryPickupEvent(inventory, prevInventory)) {
                    return ItemStack.EMPTY;
                }
                return stack;
            } else {
                final ItemStack remainder = HopperTileEntity.putStackInInventoryAllSlots(null, inventory, stack, null);
                if (callInventoryPickupEvent(inventory, prevInventory)) {
                    return remainder;
                }
                return stack;
            }
        }
    }

    private static boolean callInventoryPickupEvent(final IInventory inventory, final ItemStack[] prevInventory) {
        final Inventory spongeInventory = InventoryUtil.toInventory(inventory, null);
        final List<SlotTransaction> trans = generateTransactions(spongeInventory, inventory, prevInventory);
        if (trans.isEmpty()) {
            return true;
        }
        final ChangeInventoryEvent.Pickup event = SpongeEventFactory.createChangeInventoryEventPickup(PhaseTracker.getCauseStackManager().getCurrentCause(), spongeInventory, trans);
        SpongeCommon.postEvent(event);
        applyTransactions(event);
        return !event.isCancelled();
    }

    private static List<SlotTransaction> generateTransactions(@Nullable final Inventory inv, final IInventory inventory, final ItemStack[] previous) {
        if (inv == null) {
            return Collections.emptyList();
        }
        final List<SlotTransaction> trans = new ArrayList<>();
        List<org.spongepowered.api.item.inventory.Slot> slots = inv.slots();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            final org.spongepowered.api.item.inventory.Slot slot = slots.get(i);
            final ItemStack newStack = inventory.getStackInSlot(i);
            final ItemStack prevStack = previous[i];
            if (!ItemStack.areItemStacksEqual(newStack, prevStack)) {
                trans.add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(prevStack), ItemStackUtil.snapshotOf(newStack)));
            }
        }
        return trans;
    }



    private static void applyTransactions(final ChangeInventoryEvent.Pickup event) {
        if (event.isCancelled()) {
            for (final SlotTransaction trans : event.getTransactions()) {
                trans.getSlot().set(trans.getOriginal().createStack());
            }
            return;
        }
        for (final SlotTransaction trans : event.getTransactions()) {
            if (!trans.isValid()) {
                trans.getSlot().set(trans.getOriginal().createStack());
            } else if (trans.getCustom().isPresent()) {
                trans.getSlot().set(trans.getFinal().createStack());
            }
        }
    }


    public static ClickContainerEvent.Creative callCreativeClickContainerEvent(final ServerPlayerEntity player, final CCreativeInventoryActionPacket packetIn) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            // Creative doesn't inform server of cursor status so there is no way of knowing what the final stack is
            // Due to this, we can only send the original item that was clicked in slot
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.empty(), ItemStackSnapshot.empty());
            org.spongepowered.api.item.inventory.Slot slot = null;
            final List<SlotTransaction> captures = ((TrackedInventoryBridge) player.openContainer).bridge$getCapturedSlotTransactions();
            if (captures.isEmpty() && packetIn.getSlotId() >= 0 && packetIn.getSlotId() < player.openContainer.inventorySlots.size()) {
                slot = ((InventoryAdapter)player.openContainer).inventoryAdapter$getSlot(packetIn.getSlotId()).orElse(null);
                if (slot != null) {
                    final ItemStackSnapshot clickedItem = ItemStackUtil.snapshotOf(slot.peek());
                    final ItemStackSnapshot replacement = ItemStackUtil.snapshotOf(packetIn.getStack());
                    final SlotTransaction slotTransaction = new SlotTransaction(slot, clickedItem, replacement);
                    captures.add(slotTransaction);
                }
            }
            final ClickContainerEvent.Creative event =
                    SpongeEventFactory.createClickContainerEventCreative(frame.getCurrentCause(), (org.spongepowered.api.item.inventory.Container) player.openContainer, cursorTransaction,
                            Optional.ofNullable(slot),
                            new ArrayList<>(captures));
            captures.clear();
            ((TrackedInventoryBridge) player.openContainer).bridge$setCaptureInventory(false);
            SpongeCommon.postEvent(event);
            frame.popCause();
            return event;
        }
    }

    public static boolean callInteractContainerOpenEvent(final ServerPlayerEntity player) {
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.empty(), newCursor);
        final InteractContainerEvent.Open event =
                SpongeEventFactory.createInteractContainerEventOpen(PhaseTracker.getCauseStackManager().getCurrentCause(),
                        (org.spongepowered.api.item.inventory.Container) player.openContainer, cursorTransaction);
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            player.closeScreen();
            return false;
        }
        // TODO - determine if/how we want to fire inventory events outside of click packet handlers
        //((ContainerBridge) player.openContainer).bridge$setCaptureInventory(true);
        // Custom cursor
        if (event.getCursorTransaction().getCustom().isPresent()) {
            handleCustomCursor(player, event.getCursorTransaction().getFinal());
        }
        return true;
    }

    @Nullable
    public static Container displayContainer(final ServerPlayerEntity player, final Inventory inventory, final Component displayName) {
        final net.minecraft.inventory.container.Container previousContainer = player.openContainer;
        final net.minecraft.inventory.container.Container container;

        Optional<ViewableInventory> viewable = inventory.asViewable();
        if (viewable.isPresent()) {
            try {
                if (displayName != null) {
                    ((ServerPlayerEntityBridge) player).bridge$setContainerDisplay(displayName);
                }

                // TODO custom displayname
                if (viewable.get() instanceof INamedContainerProvider) {
                    player.openContainer((INamedContainerProvider) viewable.get());
                } else if (viewable.get() instanceof CarriedInventory) {
                    Optional carrier = ((CarriedInventory) viewable.get()).getCarrier();
                    if (carrier.get() instanceof AbstractHorseEntity) {
                        player.openHorseInventory(((AbstractHorseEntity) carrier.get()), ((IInventory) viewable.get()));
                    }

                } else if (viewable.get() instanceof IMerchant) {
                    IMerchant merchant = (IMerchant) viewable.get();
                    ITextComponent display = null;
                    int level = 0;
                    if (merchant instanceof VillagerEntity) {
                        display = ((VillagerEntity) merchant).getDisplayName();
                        level = ((VillagerEntity) merchant).getVillagerData().getLevel();
                    } else if (merchant instanceof WanderingTraderEntity) {
                        display = ((WanderingTraderEntity) merchant).getDisplayName();
                        level = 1;
                    }
                    if (displayName != null) {
                        display = SpongeAdventure.asVanilla(displayName);
                    }
                    OptionalInt containerId = player.openContainer(new SimpleNamedContainerProvider((id, playerInv, p) ->
                            new MerchantContainer(id, playerInv, merchant), display));
                    if (containerId.isPresent() && !merchant.getOffers().isEmpty()) {
                        player.openMerchantContainer(containerId.getAsInt(), merchant.getOffers(), level, merchant.getXp(), merchant.func_213705_dZ(), merchant.func_223340_ej());
                    }
                }
            } finally {
                if (displayName != null) {
                    ((ServerPlayerEntityBridge) player).bridge$setContainerDisplay(null);
                }
            }
        }

        container = player.openContainer;

        if (previousContainer == container) {
            return null;
        }

        if (!callInteractContainerOpenEvent(player)) {
            return null;
        }

        if (container instanceof ContainerBridge) {
            // This overwrites the normal container behaviour and allows viewing
            // inventories that are more than 8 blocks away
            // This currently actually only works for the Containers mixed into
            // by InteractableContainerMixin ; but throws no errors for other
            // containers

            // Allow viewing inventory; except when dead
            ((ContainerBridge) container).bridge$setCanInteractWith(p -> !p.removed);
        }

        return container;
    }

    public static TransferInventoryEvent.Pre callTransferPre(final Inventory source, final Inventory destination) {
        PhaseTracker.getCauseStackManager().pushCause(source);
        final TransferInventoryEvent.Pre event = SpongeEventFactory.createTransferInventoryEventPre(
                PhaseTracker.getCauseStackManager().getCurrentCause(), source, destination);
        SpongeCommon.postEvent(event);
        PhaseTracker.getCauseStackManager().popCause();
        return event;
    }

    public static void callTransferPost(@Nullable final TrackedInventoryBridge captureSource, @Nullable final Inventory source,
            @Nullable final Inventory destination, ItemStack sourceStack, SlotTransaction sourceSlotTransaction) {
        // TODO make sure we never got null
        if (captureSource == null || source == null || destination == null || sourceSlotTransaction == null) {
            return;
        }
        PhaseTracker.getCauseStackManager().pushCause(source);
        List<SlotTransaction> slotTransactions = captureSource.bridge$getCapturedSlotTransactions();

        sourceStack = sourceStack.copy();
        sourceStack.setCount(1);
        ItemStackSnapshot transferredStack = ItemStackUtil.snapshotOf(sourceStack);

        Slot sourceSlot = sourceSlotTransaction.getSlot();
        Slot targetSlot = null;
        // There should only be 2 transactions - the other is the transaction on the target slot
        for (SlotTransaction transaction : slotTransactions) {
            if (transaction != sourceSlotTransaction) {
                targetSlot = transaction.getSlot();
                break;
            }
        }

        final TransferInventoryEvent.Post event =
                SpongeEventFactory.createTransferInventoryEventPost(PhaseTracker.getCauseStackManager().getCurrentCause(),
                        source, sourceSlot, destination, targetSlot, transferredStack);
        SpongeCommon.postEvent(event);
        slotTransactions.clear();
        PhaseTracker.getCauseStackManager().popCause();
    }


    /**
     * Captures a transaction
     *
     * @param captureIn the {@link TrackedInventoryBridge} to capture the transaction in
     * @param inv the Inventory
     * @param index the affected SlotIndex
     * @param originalStack the original Stack
     */
    public static SlotTransaction captureTransaction(@Nullable final TrackedInventoryBridge captureIn, @Nullable final Inventory inv, final int index, final ItemStack originalStack) {
        // TODO make sure we never got null
        if (captureIn == null || inv == null) {
            return null;
        }

        Optional<org.spongepowered.api.item.inventory.Slot> slot = inv.getSlot(index);
        if (slot.isPresent()) {
            SlotTransaction trans = new SlotTransaction(slot.get(),
                    ItemStackUtil.snapshotOf(originalStack),
                    ItemStackUtil.snapshotOf(slot.get().peek()));
            captureIn.bridge$getCapturedSlotTransactions().add(trans);
            return trans;
        } else {
            SpongeCommon.getLogger().warn("Unable to capture transaction from " + inv.getClass() + " at index " + index);
            return null;
        }
    }



    /**
     * Captures a transaction
     *
     * @param captureIn the {@link TrackedInventoryBridge} to capture the transaction in
     * @param inv the Inventory
     * @param index the affected SlotIndex
     * @param transaction the transaction to execute
     * @return the result if the transaction
     */
    public static ItemStack captureTransaction(@Nullable final TrackedInventoryBridge captureIn, @Nullable final Inventory inv, final int index, final Supplier<ItemStack> transaction) {
        // TODO make sure we never got null
        if (captureIn == null || inv == null) {
            return transaction.get();
        }

        Optional<org.spongepowered.api.item.inventory.Slot> slot = inv.getSlot(index);
        if (!slot.isPresent()) {
            SpongeCommon.getLogger().warn("Unable to capture transaction from " + inv.getClass() + " at index " + index);
            return transaction.get();
        }
        ItemStackSnapshot original = ItemStackUtil.snapshotOf(slot.get().peek());
        ItemStack remaining = transaction.get();
        if (remaining.isEmpty()) {
            ItemStackSnapshot replacement = ItemStackUtil.snapshotOf(slot.get().peek());
            captureIn.bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot.get(), original, replacement));
        }
        return remaining;
    }

    public static CraftItemEvent.Preview callCraftEventPre(final PlayerEntity player, final CraftingInventory inventory,
            final SlotTransaction previewTransaction, @Nullable final CraftingRecipe recipe, final Container container, final List<SlotTransaction> transactions) {
        final CraftItemEvent.Preview event = SpongeEventFactory
                .createCraftItemEventPreview(PhaseTracker.getCauseStackManager().getCurrentCause(), inventory, (Inventory) container, previewTransaction, Optional.ofNullable(recipe), transactions);
        SpongeCommon.postEvent(event);
        PacketPhaseUtil.handleSlotRestore(player, container, new ArrayList<>(transactions), event.isCancelled());
        if (player instanceof ServerPlayerEntity) {
            if (event.getPreview().getCustom().isPresent() || event.isCancelled() || !event.getPreview().isValid()) {
                ItemStackSnapshot stack = event.getPreview().getFinal();
                if (event.isCancelled() || !event.getPreview().isValid()) {
                    stack = event.getPreview().getOriginal();
                }
                // Resend modified output
                ((ServerPlayerEntity) player).connection.sendPacket(new SSetSlotPacket(0, 0, ItemStackUtil.fromSnapshotToNative(stack)));
            }

        }
        return event;
    }


    public static CraftItemEvent.Craft callCraftEventPost(final PlayerEntity player, final CraftingInventory inventory, final ItemStackSnapshot result,
            @Nullable final CraftingRecipe recipe, final Container container, final List<SlotTransaction> transactions) {
        // Get previous cursor if captured
        ItemStack previousCursor = ((TrackedContainerBridge) container).bridge$getPreviousCursor();
        if (previousCursor == null) {
            previousCursor = player.inventory.getItemStack(); // or get the current one
        }
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackUtil.snapshotOf(previousCursor), ItemStackUtil.snapshotOf(player.inventory.getItemStack()));
        final org.spongepowered.api.item.inventory.Slot slot = inventory.getResult();
        final CraftItemEvent.Craft event = SpongeEventFactory.createCraftItemEventCraft(PhaseTracker.getCauseStackManager().getCurrentCause(),
                ContainerUtil.fromNative(container), result, inventory, cursorTransaction, Optional.ofNullable(recipe), Optional.of(slot), transactions);
        SpongeCommon.postEvent(event);

        final boolean capture = ((TrackedInventoryBridge) container).bridge$capturingInventory();
        ((TrackedInventoryBridge) container).bridge$setCaptureInventory(false);
        // handle slot-transactions
        PacketPhaseUtil.handleSlotRestore(player, container, new ArrayList<>(transactions), event.isCancelled());
        if (event.isCancelled() || !event.getCursorTransaction().isValid() || event.getCursorTransaction().getCustom().isPresent()) {
            // handle cursor-transaction
            final ItemStackSnapshot newCursor = event.isCancelled() || event.getCursorTransaction().isValid() ? event.getCursorTransaction().getOriginal() : event.getCursorTransaction().getFinal();
            player.inventory.setItemStack(ItemStackUtil.fromSnapshotToNative(newCursor));
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).connection.sendPacket(new SSetSlotPacket(-1, -1, player.inventory.getItemStack()));
            }
        }

        transactions.clear();
        ((TrackedInventoryBridge) container).bridge$setCaptureInventory(capture);
        return event;
    }

    public static UpdateAnvilEvent callUpdateAnvilEvent(final RepairContainer anvil, final ItemStack slot1, final ItemStack slot2, final ItemStack result, final String name, final int levelCost, final int materialCost) {
        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(result));
        final UpdateAnvilEventCost costs = new UpdateAnvilEventCost(levelCost, materialCost);
        final UpdateAnvilEvent event = SpongeEventFactory.createUpdateAnvilEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                new Transaction<>(costs, costs), (Inventory)anvil, name, ItemStackUtil.snapshotOf(slot1), transaction, ItemStackUtil.snapshotOf(slot2));
        SpongeCommon.postEvent(event);
        return event;
    }


    public static ChangeEntityEquipmentEvent callChangeEntityEquipmentEvent(
            final LivingEntity entity, final ItemStackSnapshot before, final ItemStackSnapshot after, final SlotAdapter slot) {
        final ChangeEntityEquipmentEvent event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity);
            final Transaction<ItemStackSnapshot> transaction = new Transaction<>(before, after);
            if (after.isEmpty()) {
                event = SpongeEventFactory.createChangeEntityEquipmentEventBreak(frame.getCurrentCause(), (Entity) entity, slot, transaction);
            } else {
                event = SpongeEventFactory.createChangeEntityEquipmentEvent(frame.getCurrentCause(), (Entity)  entity, slot, transaction);
            }
            SpongeCommon.postEvent(event);
            return event;
        }
    }


    public static int callEnchantEventLevelRequirement(EnchantmentContainer container, int seed, int option, int power, ItemStack itemStack, int levelRequirement) {
        org.spongepowered.api.item.inventory.Container enchantContainer = ContainerUtil.fromNative(container);

        PlayerEntity viewer = (PlayerEntity) enchantContainer.getViewer();
        ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(viewer.inventory.getCurrentItem());
        Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(cursor, cursor);

        EnchantItemEvent.CalculateLevelRequirement event =
                SpongeEventFactory.createEnchantItemEventCalculateLevelRequirement(PhaseTracker.getCauseStackManager().getCurrentCause(),
                        levelRequirement, levelRequirement, enchantContainer, cursorTrans, ItemStackUtil.snapshotOf(itemStack), option, power, seed);

        SpongeCommon.postEvent(event);

        return event.getLevelRequirement();
    }

    public static List<EnchantmentData> callEnchantEventEnchantmentList(EnchantmentContainer container,
            int seed, ItemStack itemStack, int option, int level, List<EnchantmentData> list) {

        List<Enchantment> enchList = Collections.unmodifiableList(SpongeRandomEnchantmentListBuilder.fromNative(list));

        org.spongepowered.api.item.inventory.Container enchantContainer = ContainerUtil.fromNative(container);

        PlayerEntity viewer = (PlayerEntity) enchantContainer.getViewer();
        ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(viewer.inventory.getCurrentItem());
        Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(cursor, cursor);

        EnchantItemEvent.CalculateEnchantment event =
                SpongeEventFactory.createEnchantItemEventCalculateEnchantment(PhaseTracker.getCauseStackManager().getCurrentCause(),
                        enchList, enchList, enchantContainer, cursorTrans, ItemStackUtil.snapshotOf(itemStack), level, option, seed);

        SpongeCommon.postEvent(event);

        if (event.getEnchantments() != event.getOriginalEnchantments()) {
            return SpongeRandomEnchantmentListBuilder.toNative(event.getEnchantments());
        }
        return list;
    }


    public static EnchantItemEvent.Post callEnchantEventEnchantPost(PlayerEntity playerIn, EnchantmentContainer container,
            SlotTransaction enchantedItem, SlotTransaction lapisItem, int option, int seed) {
        org.spongepowered.api.item.inventory.Container enchantContainer = ContainerUtil.fromNative(container);

        ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(playerIn.inventory.getItemStack());
        Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(cursor, cursor);

        List<SlotTransaction> slotTrans = new ArrayList<>();
        slotTrans.add(lapisItem);
        slotTrans.add(enchantedItem);

        EnchantItemEvent.Post event =
                SpongeEventFactory.createEnchantItemEventPost(PhaseTracker.getCauseStackManager().getCurrentCause(), enchantContainer,
                        cursorTrans, enchantedItem.getSlot(), Optional.empty(),slotTrans, option, seed);

        SpongeCommon.postEvent(event);

        PacketPhaseUtil.handleSlotRestore(playerIn, container, event.getTransactions(), event.isCancelled());
        if (event.isCancelled() || !event.getCursorTransaction().isValid()) {
            PacketPhaseUtil.handleCustomCursor(playerIn, event.getCursorTransaction().getOriginal());
        } else if (event.getCursorTransaction().getCustom().isPresent()) {
            PacketPhaseUtil.handleCustomCursor(playerIn, event.getCursorTransaction().getFinal());
        }

        return event;
    }


}
