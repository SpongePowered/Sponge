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
package org.spongepowered.common.event.tracking.context.transaction.inventory;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.crafting.CraftingOutput;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.event.tracking.context.transaction.world.SpawnEntityTransaction;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

abstract class ContainerBasedTransaction extends MenuBasedTransaction<ClickContainerEvent> {
    private static Set<Class<?>> containersFailedCapture = new ReferenceOpenHashSet<>();

    @MonotonicNonNull List<net.minecraft.world.entity.Entity> entities;
    @MonotonicNonNull protected List<SlotTransaction> acceptedTransactions;
    // Crafting Preview
    @MonotonicNonNull private CraftingInventory craftingInventory;
    @MonotonicNonNull private CraftingContainer craftingContainer;
    // Crafting Event
    @Nullable private ItemStack craftedStack;
    @Nullable private RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> onTakeRecipe;
    @Nullable private ResourceKey onTakeRecipeKey;
    protected boolean used = false;
    @Nullable ItemStack shiftCraftingResult;


    protected ContainerBasedTransaction(
        final AbstractContainerMenu menu
    ) {
        super(TransactionTypes.CLICK_CONTAINER_EVENT.get(), menu);
    }


    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable final GameTransaction<@NonNull ?> parent
    ) {
        return Optional.of((context, frame) -> {
            if (!frame.currentCause().all().contains(this.menu)) {
                frame.pushCause(this.menu);
            }
        });
    }


    @Override
    public Optional<ClickContainerEvent> generateEvent(
        final PhaseContext<@NonNull ?> context, @Nullable final GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<ClickContainerEvent>> gameTransactions, final Cause currentCause
    ) {
        final ImmutableList<ContainerBasedTransaction> containerBasedTransactions = gameTransactions.stream()
            .filter(tx -> tx instanceof ContainerBasedTransaction)
            .map(tx -> (ContainerBasedTransaction) tx)
            .filter(tx -> !tx.used).collect(ImmutableList.toImmutableList());
        if (containerBasedTransactions.stream().map(c -> c.isContainerEventAllowed(context))
            .filter(b -> !b)
            .findAny()
            .orElse(false)) {
            SpongeCommon.logger().warn("No event will be fired for existing ContainerBasedTransactions: {}", containerBasedTransactions.size());
            return Optional.empty();
        }
        if (!((TrackedContainerBridge) this.menu).bridge$capturePossible()) {
            //        if (ContainerBasedTransaction.containersFailedCapture.add(this.menu.getClass())) {
            //            SpongeCommon.logger()
            //                .warn("Changes in modded Container were not captured. Inventory events will not fire for this. Container: " + this.menu.getClass());
            //        }
        }

        final List<Entity> entities = containerBasedTransactions.stream()
            .map(ContainerBasedTransaction::getEntitiesSpawned)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        final List<SlotTransaction> slotTransactions = containerBasedTransactions
            .stream()
            .map(ContainerBasedTransaction::getSlotTransactions)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        if (this.craftingInventory != null) { // Event with Preview transaction on crafting inventory?
            Slot slot = this.craftingInventory.result();
            @Nullable final SlotTransaction preview = this.findPreviewTransaction(this.craftingInventory.result(), slotTransactions);
            final ItemStackSnapshot previewItem = ItemStackUtil.snapshotOf(this.craftingInventory.result().peek());
            if (preview != null) {
                slot = preview.slot();
                // Check if preview transaction is correct
                if (!preview.defaultReplacement().equals(previewItem)) {
                    slotTransactions.remove(preview);
                    slotTransactions.add(new SlotTransaction(slot, preview.original(), previewItem));
                }
            } else if (!previewItem.isEmpty()) {
                slotTransactions.add(new SlotTransaction(slot, previewItem, previewItem));
            }
        }

        for (final ContainerBasedTransaction transaction : containerBasedTransactions) {
            transaction.used = true;
        }

        final Optional<ClickContainerEvent> event = containerBasedTransactions.stream()
                .map(t -> t.createInventoryEvent(slotTransactions, entities, context, currentCause))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
        if (!event.isPresent() && !slotTransactions.isEmpty()) {
            SpongeCommon.logger().warn("Logged slot transactions without event! {} {}", gameTransactions.size(), this.menu.getClass().getName(), new Exception(""));
            for (final SlotTransaction slotTransaction : slotTransactions) {
                SpongeCommon.logger().warn(slotTransaction);
            }
        }
        return event;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Entity> getEntitiesSpawned() {
        return this.entities == null ? Collections.emptyList() : (List<Entity>) (List) this.entities;
    }

    boolean isContainerEventAllowed(final PhaseContext<@NonNull ?> context) {
        return true;
    }

    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions,
        final List<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause currentCause
    ) {
        return Optional.empty();
    }

    @Override
    public void restore(
        final PhaseContext<@NonNull ?> context,
        final ClickContainerEvent event
    ) {

    }

    @Override
    public boolean markCancelledTransactions(
        final ClickContainerEvent event,
        final ImmutableList<? extends GameTransaction<ClickContainerEvent>> gameTransactions
    ) {
        if (event.isCancelled()) {
            event.transactions().forEach(SlotTransaction::invalidate);
            event.cursorTransaction().invalidate();
            if (event instanceof CraftItemEvent.Preview) {
                ((CraftItemEvent.Preview) event).preview().invalidate();
            }
            return true;
        }
        boolean cancelledAny = false;
        for (final SlotTransaction transaction : event.transactions()) {
            if (!transaction.isValid()) {
                cancelledAny = true;
                for (final GameTransaction<ClickContainerEvent> gameTransaction : gameTransactions) {
                    ((ContainerBasedTransaction) gameTransaction).getSlotTransactions()
                        .forEach(tx -> {
                            if (tx == transaction) {
                                gameTransaction.markCancelled();
                            }
                        });
                }
            }
        }
        return cancelledAny;
    }

    protected void handleEventResults(final Player player, final ClickContainerEvent event) {
        PacketPhaseUtil.handleSlotRestore(player, this.menu, event.transactions(), event.isCancelled());
        PacketPhaseUtil.handleCursorRestore(player, event.cursorTransaction(), event.isCancelled());

        if (this.entities != null && event instanceof SpawnEntityEvent) {
            EntityUtil.despawnFilteredEntities(this.entities, (SpawnEntityEvent) event);
        }

        // If this is not a crafting event try to call crafting events
        if (!event.isCancelled()) {
            if (!(event instanceof CraftItemEvent.Craft) && !(event instanceof CraftItemEvent.Preview)) {
                this.handleCrafting(player, event);
                this.handleCraftingPreview(player, event);
            }
        }
    }

    private void handleCrafting(final Player player, final ClickContainerEvent event) {
        if (this.craftedStack != null && this.craftingInventory != null) {
            if (this.acceptedTransactions != null) {
                this.acceptedTransactions.clear();
            }
            // TODO push event to cause?
            ItemStackSnapshot craftedItem = null;
            for (final SlotTransaction transaction : event.transactions()) {
                if (transaction.slot().equals(this.craftingInventory.result())) {
                    // Use transaction on slot if possible
                    craftedItem = transaction.original();
                    break;
                }
            }
            // shift-crafting wont have it because the crafted item only changes on the last shift-craft
            if (craftedItem == null) {
                craftedItem = ItemStackUtil.snapshotOf(this.craftedStack);
            }

            final CraftItemEvent.Craft craftEvent =
                    SpongeEventFactory.createCraftItemEventCraft(PhaseTracker.getCauseStackManager().currentCause(),
                            ContainerUtil.fromNative(this.menu), craftedItem, this.craftingInventory, event.cursorTransaction(),
                            Optional.ofNullable(this.onTakeRecipe).map(r -> (CraftingRecipe) r.value()),
                            Optional.ofNullable(this.onTakeRecipe).map(r -> (ResourceKey) (Object) r.id()),
                            Optional.of(this.craftingInventory.result()), event.transactions());
            SpongeCommon.post(craftEvent);
            this.handleEventResults(player, craftEvent);
            if (craftEvent.isCancelled() && this.shiftCraftingResult != null) {
                this.shiftCraftingResult.setCount(0);
            }
        }
    }

    private void handleCraftingPreview(final Player player, final ClickContainerEvent event) {
        if (this.craftingInventory != null) {
            // TODO push event to cause?
            // TODO prevent event when there is no preview?
            final SlotTransaction previewTransaction = this.getPreviewTransaction(this.craftingInventory.result(), event.transactions());
            final var recipe = player.level().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.craftingContainer.asCraftInput(), player.level());
            final CraftItemEvent.Preview previewEvent = SpongeEventFactory.createCraftItemEventPreview(event.cause(), (Container) this.menu, this.craftingInventory, event.cursorTransaction(), previewTransaction,
                    recipe.map(RecipeHolder::value).map(CraftingRecipe.class::cast), recipe.map(h -> h.id()).map(ResourceKey.class::cast), Optional.empty(), event.transactions());
            SpongeCommon.post(previewEvent);
            this.handleEventResults(player, previewEvent);

            if (player instanceof ServerPlayer && previewEvent instanceof CraftItemEvent.Preview) {
                final SlotTransaction preview = previewEvent.preview();
                // Resend modified output if needed
                if (!preview.isValid() || previewEvent.isCancelled()) {
                    ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(0, this.menu.getStateId(), 0,
                            ItemStackUtil.fromSnapshotToNative(previewEvent.preview().original())));
                    // TODO handle preview event cancel during shift-crafting
                } else if (preview.custom().isPresent()) {
                    ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(0, this.menu.getStateId(), 0,
                            ItemStackUtil.fromSnapshotToNative(previewEvent.preview().finalReplacement())));
                    // TODO handle preview event modification during shift-crafting
                }
            }
        }
    }

    protected SlotTransaction getPreviewTransaction(final CraftingOutput result, final List<SlotTransaction> slotTransactions) {
        @Nullable final SlotTransaction preview = this.findPreviewTransaction(result, slotTransactions);
        if (preview == null) {
            final ItemStackSnapshot previewItem = ItemStackUtil.snapshotOf(result.peek());
            return new SlotTransaction(result, previewItem, previewItem);
        }
        return preview;
    }

    private @Nullable SlotTransaction findPreviewTransaction(final CraftingOutput result, final List<SlotTransaction> slotTransactions) {
        for (final SlotTransaction slotTransaction : slotTransactions) {
            if (result.viewedSlot().equals(slotTransaction.slot().viewedSlot())) {
                return slotTransaction; // get last transaction
            }
        }
        return null;
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {

    }

    List<SlotTransaction> getSlotTransactions() {
        return this.acceptedTransactions == null ? Collections.emptyList() : this.acceptedTransactions;
    }

    @Override
    public boolean absorbSlotTransaction(
        final ContainerSlotTransaction slotTransaction
    ) {
        if (this.menu != slotTransaction.menu) {
            return false;
        }
        if (this.acceptedTransactions == null) {
            this.acceptedTransactions = new ArrayList<>();
        }
        this.acceptedTransactions.add(slotTransaction.transaction);
        return true;
    }

    @Override
    public boolean absorbSpawnEntity(final PhaseContext<@NonNull ?> context, final SpawnEntityTransaction spawn) {
        if (context.doesContainerCaptureEntitySpawn(spawn.entityToSpawn)) {
            if (this.entities == null) {
                this.entities = new LinkedList<>();
            }
            this.entities.add(spawn.entityToSpawn);
            return true;
        }
        return super.absorbSpawnEntity(context, spawn);
    }

    @Override
    public boolean acceptCraftingPreview(
        final PhaseContext<@NonNull ?> ctx, final CraftingPreviewTransaction transaction
    ) {
        if (this.menu == transaction.menu) {
            this.craftingInventory = transaction.craftingInventory;
            this.craftingContainer = transaction.craftSlots;
            return true;
        }
        return false;
    }

    @Override
    public boolean acceptCrafting(
        final PhaseContext<@NonNull ?> ctx, final CraftingTransaction transaction
    ) {
        if (this.menu == transaction.menu) {
            this.used = false;
            this.craftedStack = transaction.craftedStack;
            this.craftingInventory = transaction.craftingInventory;
            this.onTakeRecipe = transaction.recipe;
            return true;
        }
        return false;
    }

    @Override
    public boolean absorbShiftClickResult(
        final PhaseContext<@NonNull ?> context, final ShiftCraftingResultTransaction transaction
    ) {
        // todo - maybe we might need to verify this?
        this.shiftCraftingResult = transaction.itemStack;
        return true;
    }

}
