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
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
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
import org.spongepowered.common.bridge.world.inventory.container.ContainerBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
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

public class InventoryEventFactory {


    public static boolean callPlayerChangeInventoryPickupPreEvent(final Player player, final ItemEntity itemToPickup, final int pickupDelay) {
        final ItemStack stack = itemToPickup.getItem();
        final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
        causeStackManager.pushCause(player);
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
        final ChangeInventoryEvent.Pickup.Pre event =
                SpongeEventFactory.createChangeInventoryEventPickupPre(
                    causeStackManager.currentCause(),
                        Optional.empty(), Collections.singletonList(snapshot), ((Inventory) player.inventory), (Item) itemToPickup, snapshot);
        SpongeCommon.post(event);
        causeStackManager.popCause();
        if (event.isCancelled()) {
            return false;
        }
        if (event.custom().isPresent()) {
            final List<ItemStackSnapshot> list = event.custom().get();
            if (list.isEmpty()) {
                itemToPickup.getItem().setCount(0);
                return false;
            }

            boolean fullTransfer = true;
            final TrackedInventoryBridge capture = (TrackedInventoryBridge) player.inventory;
            capture.bridge$setCaptureInventory(true);
            for (final ItemStackSnapshot item : list) {
                final org.spongepowered.api.item.inventory.ItemStack itemStack = item.createStack();
                player.inventory.add(ItemStackUtil.toNative(itemStack));
                if (!itemStack.isEmpty()) {
                    fullTransfer = false;
                    break;
                }

            }
            capture.bridge$setCaptureInventory(false);
            if (!fullTransfer) {
                for (final SlotTransaction trans : capture.bridge$getCapturedSlotTransactions()) {
                    trans.slot().set(trans.original().createStack());
                }
                return false;
            }
            if (!InventoryEventFactory.callPlayerChangeInventoryPickupEvent(player, capture)) {
                return false;
            }
            itemToPickup.getItem().setCount(0);
        }
        return true;
    }


    public static boolean callPlayerChangeInventoryPickupEvent(final Player player, final TrackedInventoryBridge inventory) {
        if (inventory.bridge$getCapturedSlotTransactions().isEmpty()) {
            return true;
        }
        PhaseTracker.getCauseStackManager().pushCause(player);
        final ChangeInventoryEvent.Pickup event = SpongeEventFactory.createChangeInventoryEventPickup(PhaseTracker.getCauseStackManager().currentCause(), (Inventory) player.containerMenu,
                inventory.bridge$getCapturedSlotTransactions());
        SpongeCommon.post(event);
        PhaseTracker.getCauseStackManager().popCause();
        InventoryEventFactory.applyTransactions(event);
        inventory.bridge$getCapturedSlotTransactions().clear();
        return !event.isCancelled();
    }


    public static ItemStack callInventoryPickupEvent(final Container inventory, final ItemEntity item, final ItemStack stack) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(inventory);

            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final ChangeInventoryEvent.Pickup.Pre event =
                    SpongeEventFactory.createChangeInventoryEventPickupPre(frame.currentCause(),
                            Optional.empty(), Collections.singletonList(snapshot), (Inventory) inventory, (Item) item, snapshot);
            SpongeCommon.post(event);
            if (event.isCancelled()) {
                return stack;
            }

            final int size = inventory.getContainerSize();
            final ItemStack[] prevInventory = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                prevInventory[i] = inventory.getItem(i);
            }

            if (event.custom().isPresent()) {
                if (event.custom().get().isEmpty()) {
                    return ItemStack.EMPTY;
                }

                boolean fullTransfer = true;
                for (final ItemStackSnapshot snap : event.custom().get()) {
                    final ItemStack stackToAdd = ItemStackUtil.fromSnapshotToNative(snap);
                    final ItemStack remaining = HopperBlockEntity.addItem(null, inventory, stackToAdd, null);
                    if (!remaining.isEmpty()) {
                        fullTransfer = false;
                        break;
                    }
                }
                if (!fullTransfer) {
                    for (int i = 0; i < prevInventory.length; i++) {
                        inventory.setItem(i, prevInventory[i]);
                    }
                    return stack;
                }

                if (InventoryEventFactory.callInventoryPickupEvent(inventory, prevInventory)) {
                    return ItemStack.EMPTY;
                }
                return stack;
            } else {
                final ItemStack remainder = HopperBlockEntity.addItem(null, inventory, stack, null);
                if (InventoryEventFactory.callInventoryPickupEvent(inventory, prevInventory)) {
                    return remainder;
                }
                return stack;
            }
        }
    }

    private static boolean callInventoryPickupEvent(final Container inventory, final ItemStack[] prevInventory) {
        final Inventory spongeInventory = InventoryUtil.toInventory(inventory, null);
        final List<SlotTransaction> trans = InventoryEventFactory.generateTransactions(spongeInventory, inventory, prevInventory);
        if (trans.isEmpty()) {
            return true;
        }
        final ChangeInventoryEvent.Pickup event = SpongeEventFactory.createChangeInventoryEventPickup(PhaseTracker.getCauseStackManager().currentCause(), spongeInventory, trans);
        SpongeCommon.post(event);
        InventoryEventFactory.applyTransactions(event);
        return !event.isCancelled();
    }

    private static List<SlotTransaction> generateTransactions(final @Nullable Inventory inv, final Container inventory, final ItemStack[] previous) {
        if (inv == null) {
            return Collections.emptyList();
        }
        final List<SlotTransaction> trans = new ArrayList<>();
        List<org.spongepowered.api.item.inventory.Slot> slots = inv.slots();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            final org.spongepowered.api.item.inventory.Slot slot = slots.get(i);
            final ItemStack newStack = inventory.getItem(i);
            final ItemStack prevStack = previous[i];
            if (!ItemStack.matches(newStack, prevStack)) {
                trans.add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(prevStack), ItemStackUtil.snapshotOf(newStack)));
            }
        }
        return trans;
    }



    private static void applyTransactions(final ChangeInventoryEvent.Pickup event) {
        if (event.isCancelled()) {
            for (final SlotTransaction trans : event.transactions()) {
                trans.slot().set(trans.original().createStack());
            }
            return;
        }
        for (final SlotTransaction trans : event.transactions()) {
            if (!trans.isValid()) {
                trans.slot().set(trans.original().createStack());
            } else if (trans.custom().isPresent()) {
                trans.slot().set(trans.finalReplacement().createStack());
            }
        }
    }


    public static ClickContainerEvent.Creative callCreativeClickContainerEvent(final ServerPlayer player, final ServerboundSetCreativeModeSlotPacket packetIn) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            // Creative doesn't inform server of cursor status so there is no way of knowing what the final stack is
            // Due to this, we can only send the original item that was clicked in slot
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.empty(), ItemStackSnapshot.empty());
            org.spongepowered.api.item.inventory.Slot slot = null;
            final List<SlotTransaction> captures = ((TrackedInventoryBridge) player.containerMenu).bridge$getCapturedSlotTransactions();
            if (captures.isEmpty() && packetIn.getSlotNum() >= 0 && packetIn.getSlotNum() < player.containerMenu.slots.size()) {
                slot = ((InventoryAdapter)player.containerMenu).inventoryAdapter$getSlot(packetIn.getSlotNum()).orElse(null);
                if (slot != null) {
                    final ItemStackSnapshot clickedItem = ItemStackUtil.snapshotOf(slot.peek());
                    final ItemStackSnapshot replacement = ItemStackUtil.snapshotOf(packetIn.getItem());
                    final SlotTransaction slotTransaction = new SlotTransaction(slot, clickedItem, replacement);
                    captures.add(slotTransaction);
                }
            }
            final ClickContainerEvent.Creative event =
                    SpongeEventFactory.createClickContainerEventCreative(frame.currentCause(), (org.spongepowered.api.item.inventory.Container) player.containerMenu, cursorTransaction,
                            Optional.ofNullable(slot),
                            new ArrayList<>(captures));
            captures.clear();
            ((TrackedInventoryBridge) player.containerMenu).bridge$setCaptureInventory(false);
            SpongeCommon.post(event);
            frame.popCause();
            return event;
        }
    }

    public static boolean callInteractContainerOpenEvent(final ServerPlayer player) {
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getCarried());
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.empty(), newCursor);
        final InteractContainerEvent.Open event =
                SpongeEventFactory.createInteractContainerEventOpen(PhaseTracker.getCauseStackManager().currentCause(),
                        (org.spongepowered.api.item.inventory.Container) player.containerMenu, cursorTransaction);
        SpongeCommon.post(event);
        if (event.isCancelled()) {
            player.closeContainer();
            return false;
        }
        // TODO - determine if/how we want to fire inventory events outside of click packet handlers
        //((ContainerBridge) player.openContainer).bridge$setCaptureInventory(true);
        // Custom cursor
        if (event.cursorTransaction().custom().isPresent()) {
            handleCustomCursor(player, event.cursorTransaction().finalReplacement());
        }
        return true;
    }

    public static @org.checkerframework.checker.nullness.qual.Nullable AbstractContainerMenu displayContainer(final ServerPlayer player, final Inventory inventory, final Component displayName) {
        final net.minecraft.world.inventory.AbstractContainerMenu previousContainer = player.containerMenu;
        final net.minecraft.world.inventory.AbstractContainerMenu container;

        Optional<ViewableInventory> viewable = inventory.asViewable();
        if (viewable.isPresent()) {
            if (viewable.get() instanceof MenuProvider) {
                MenuProvider namedContainerProvider = (MenuProvider) viewable.get();
                if (displayName != null) {
                    namedContainerProvider = new SimpleMenuProvider(namedContainerProvider, SpongeAdventure.asVanilla(displayName));
                }
                player.openMenu(namedContainerProvider);
            } else if (viewable.get() instanceof CarriedInventory) {
                Optional carrier = ((CarriedInventory) viewable.get()).carrier();
                if (carrier.get() instanceof AbstractHorse) {
                    player.openHorseInventory(((AbstractHorse) carrier.get()), ((Container) viewable.get()));
                }

            } else if (viewable.get() instanceof Merchant) {
                Merchant merchant = (Merchant) viewable.get();
                net.minecraft.network.chat.Component display = null;
                int level = 0;
                if (merchant instanceof Villager) {
                    display = ((Villager) merchant).getDisplayName();
                    level = ((Villager) merchant).getVillagerData().getLevel();
                } else if (merchant instanceof WanderingTrader) {
                    display = ((WanderingTrader) merchant).getDisplayName();
                    level = 1;
                }
                if (displayName != null) {
                    display = SpongeAdventure.asVanilla(displayName);
                }
                OptionalInt containerId = player.openMenu(new SimpleMenuProvider((id, playerInv, p) ->
                        new MerchantMenu(id, playerInv, merchant), display));
                if (containerId.isPresent() && !merchant.getOffers().isEmpty()) {
                    player.sendMerchantOffers(containerId.getAsInt(), merchant.getOffers(), level, merchant.getVillagerXp(), merchant.showProgressBar(), merchant.canRestock());
                }
            }
        }

        container = player.containerMenu;

        if (previousContainer == container) {
            return null;
        }

        if (!InventoryEventFactory.callInteractContainerOpenEvent(player)) {
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
                PhaseTracker.getCauseStackManager().currentCause(), source, destination);
        SpongeCommon.post(event);
        PhaseTracker.getCauseStackManager().popCause();
        return event;
    }

    public static void callTransferPost(final @Nullable TrackedInventoryBridge captureSource, final @Nullable Inventory source,
            final @Nullable Inventory destination, ItemStack sourceStack, SlotTransaction sourceSlotTransaction) {
        // TODO make sure we never got null
        if (captureSource == null || source == null || destination == null || sourceSlotTransaction == null) {
            return;
        }
        PhaseTracker.getCauseStackManager().pushCause(source);
        List<SlotTransaction> slotTransactions = captureSource.bridge$getCapturedSlotTransactions();

        sourceStack = sourceStack.copy();
        sourceStack.setCount(1);
        ItemStackSnapshot transferredStack = ItemStackUtil.snapshotOf(sourceStack);

        Slot sourceSlot = sourceSlotTransaction.slot();
        Slot targetSlot = null;
        // There should only be 2 transactions - the other is the transaction on the target slot
        for (SlotTransaction transaction : slotTransactions) {
            if (transaction != sourceSlotTransaction) {
                targetSlot = transaction.slot();
                break;
            }
        }

        final TransferInventoryEvent.Post event =
                SpongeEventFactory.createTransferInventoryEventPost(PhaseTracker.getCauseStackManager().currentCause(),
                        source, sourceSlot, destination, targetSlot, transferredStack);
        SpongeCommon.post(event);
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
    public static SlotTransaction captureTransaction(final @Nullable TrackedInventoryBridge captureIn, final @Nullable Inventory inv, final int index, final ItemStack originalStack) {
        // TODO make sure we never got null
        if (captureIn == null || inv == null) {
            return null;
        }

        Optional<org.spongepowered.api.item.inventory.Slot> slot = inv.slot(index);
        if (slot.isPresent()) {
            SlotTransaction trans = new SlotTransaction(slot.get(),
                    ItemStackUtil.snapshotOf(originalStack),
                    ItemStackUtil.snapshotOf(slot.get().peek()));
            captureIn.bridge$getCapturedSlotTransactions().add(trans);
            return trans;
        } else {
            SpongeCommon.logger().warn("Unable to capture transaction from " + inv.getClass() + " at index " + index);
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
    public static ItemStack captureTransaction(final @Nullable TrackedInventoryBridge captureIn, final @Nullable Inventory inv, final int index, final Supplier<ItemStack> transaction) {
        // TODO make sure we never got null
        if (captureIn == null || inv == null) {
            return transaction.get();
        }

        Optional<org.spongepowered.api.item.inventory.Slot> slot = inv.slot(index);
        if (!slot.isPresent()) {
            SpongeCommon.logger().warn("Unable to capture transaction from " + inv.getClass() + " at index " + index);
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

    public static CraftItemEvent.Preview callCraftEventPre(final Player player, final CraftingInventory inventory,
            final SlotTransaction previewTransaction, final @Nullable CraftingRecipe recipe, final AbstractContainerMenu container, final List<SlotTransaction> transactions) {
        final CraftItemEvent.Preview event = SpongeEventFactory
                .createCraftItemEventPreview(PhaseTracker.getCauseStackManager().currentCause(), inventory, (Inventory) container, previewTransaction, Optional.ofNullable(recipe), transactions);
        SpongeCommon.post(event);
        PacketPhaseUtil.handleSlotRestore(player, container, new ArrayList<>(transactions), event.isCancelled());
        if (player instanceof ServerPlayer) {
            if (event.preview().custom().isPresent() || event.isCancelled() || !event.preview().isValid()) {
                ItemStackSnapshot stack = event.preview().finalReplacement();
                if (event.isCancelled() || !event.preview().isValid()) {
                    stack = event.preview().original();
                }
                // Resend modified output
                ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(0, 0, ItemStackUtil.fromSnapshotToNative(stack)));
            }

        }
        return event;
    }


    public static CraftItemEvent.Craft callCraftEventPost(final Player player, final CraftingInventory inventory, final ItemStackSnapshot result,
            final @Nullable CraftingRecipe recipe, final AbstractContainerMenu container, final List<SlotTransaction> transactions) {
        // Get previous cursor if captured
        ItemStack previousCursor = ((TrackedContainerBridge) container).bridge$getPreviousCursor();
        if (previousCursor == null) {
            previousCursor = player.inventory.getCarried(); // or get the current one
        }
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackUtil.snapshotOf(previousCursor), ItemStackUtil.snapshotOf(player.inventory.getCarried()));
        final org.spongepowered.api.item.inventory.Slot slot = inventory.result();
        final CraftItemEvent.Craft event = SpongeEventFactory.createCraftItemEventCraft(PhaseTracker.getCauseStackManager().currentCause(),
                ContainerUtil.fromNative(container), result, inventory, cursorTransaction, Optional.ofNullable(recipe), Optional.of(slot), transactions);
        SpongeCommon.post(event);

        final boolean capture = ((TrackedInventoryBridge) container).bridge$capturingInventory();
        ((TrackedInventoryBridge) container).bridge$setCaptureInventory(false);
        // handle slot-transactions
        PacketPhaseUtil.handleSlotRestore(player, container, new ArrayList<>(transactions), event.isCancelled());
        if (event.isCancelled() || !event.cursorTransaction().isValid() || event.cursorTransaction().custom().isPresent()) {
            // handle cursor-transaction
            final ItemStackSnapshot newCursor = event.isCancelled() || event.cursorTransaction().isValid() ? event.cursorTransaction().original() : event.cursorTransaction().finalReplacement();
            player.inventory.setCarried(ItemStackUtil.fromSnapshotToNative(newCursor));
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(-1, -1, player.inventory.getCarried()));
            }
        }

        transactions.clear();
        ((TrackedInventoryBridge) container).bridge$setCaptureInventory(capture);
        return event;
    }

    public static UpdateAnvilEvent callUpdateAnvilEvent(final AnvilMenu anvil, final ItemStack slot1, final ItemStack slot2, final ItemStack result, final String name, final int levelCost, final int materialCost) {
        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(result));
        final UpdateAnvilEventCost costs = new UpdateAnvilEventCost(levelCost, materialCost);
        final UpdateAnvilEvent event = SpongeEventFactory.createUpdateAnvilEvent(PhaseTracker.getCauseStackManager().currentCause(),
                new Transaction<>(costs, costs), (Inventory)anvil, name, ItemStackUtil.snapshotOf(slot1), transaction, ItemStackUtil.snapshotOf(slot2));
        SpongeCommon.post(event);
        return event;
    }


    public static ChangeEntityEquipmentEvent callChangeEntityEquipmentEvent(
            final LivingEntity entity, final ItemStackSnapshot before, final ItemStackSnapshot after, final Slot slot) {
        final ChangeEntityEquipmentEvent event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity);
            final Transaction<ItemStackSnapshot> transaction = new Transaction<>(before, after);
            if (after.isEmpty()) {
                event = SpongeEventFactory.createChangeEntityEquipmentEventBreak(frame.currentCause(), (Entity) entity, slot, transaction);
            } else {
                event = SpongeEventFactory.createChangeEntityEquipmentEvent(frame.currentCause(), (Entity)  entity, slot, transaction);
            }
            SpongeCommon.post(event);
            return event;
        }
    }


    public static int callEnchantEventLevelRequirement(EnchantmentMenu container, int seed, int option, int power, ItemStack itemStack, int levelRequirement) {
        org.spongepowered.api.item.inventory.Container enchantContainer = ContainerUtil.fromNative(container);

        Player viewer = (Player) enchantContainer.viewer();
        ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(viewer.inventory.getCarried());
        Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(cursor, cursor);

        EnchantItemEvent.CalculateLevelRequirement event =
                SpongeEventFactory.createEnchantItemEventCalculateLevelRequirement(PhaseTracker.getCauseStackManager().currentCause(),
                        levelRequirement, levelRequirement, enchantContainer, cursorTrans, ItemStackUtil.snapshotOf(itemStack), option, power, seed);

        SpongeCommon.post(event);

        return event.levelRequirement();
    }

    public static List<EnchantmentInstance> callEnchantEventEnchantmentList(EnchantmentMenu container,
            int seed, ItemStack itemStack, int option, int level, List<EnchantmentInstance> list) {

        List<Enchantment> enchList = Collections.unmodifiableList(SpongeRandomEnchantmentListBuilder.fromNative(list));

        org.spongepowered.api.item.inventory.Container enchantContainer = ContainerUtil.fromNative(container);

        Player viewer = (Player) enchantContainer.viewer();
        ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(viewer.inventory.getCarried());
        Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(cursor, cursor);

        EnchantItemEvent.CalculateEnchantment event =
                SpongeEventFactory.createEnchantItemEventCalculateEnchantment(PhaseTracker.getCauseStackManager().currentCause(),
                        enchList, enchList, enchantContainer, cursorTrans, ItemStackUtil.snapshotOf(itemStack), level, option, seed);

        SpongeCommon.post(event);

        if (event.enchantments() != event.originalEnchantments()) {
            return SpongeRandomEnchantmentListBuilder.toNative(event.enchantments());
        }
        return list;
    }


    public static EnchantItemEvent.Post callEnchantEventEnchantPost(Player playerIn, EnchantmentMenu container,
            SlotTransaction enchantedItem, SlotTransaction lapisItem, int option, int seed) {
        org.spongepowered.api.item.inventory.Container enchantContainer = ContainerUtil.fromNative(container);

        ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(playerIn.inventory.getCarried());
        Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(cursor, cursor);

        List<SlotTransaction> slotTrans = new ArrayList<>();
        slotTrans.add(lapisItem);
        slotTrans.add(enchantedItem);

        EnchantItemEvent.Post event =
                SpongeEventFactory.createEnchantItemEventPost(PhaseTracker.getCauseStackManager().currentCause(), enchantContainer,
                        cursorTrans, enchantedItem.slot(), Optional.empty(),slotTrans, option, seed);

        SpongeCommon.post(event);

        PacketPhaseUtil.handleSlotRestore(playerIn, container, event.transactions(), event.isCancelled());
        if (event.isCancelled() || !event.cursorTransaction().isValid()) {
            PacketPhaseUtil.handleCustomCursor(playerIn, event.cursorTransaction().original());
        } else if (event.cursorTransaction().custom().isPresent()) {
            PacketPhaseUtil.handleCustomCursor(playerIn, event.cursorTransaction().finalReplacement());
        }

        return event;
    }


}
