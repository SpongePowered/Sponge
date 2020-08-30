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
package org.spongepowered.common.event.tracking.phase.packet.inventory;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicInventoryPacketState extends PacketState<InventoryPacketContext> {


    /**
     * Flags we care about
     */
    final int stateId;

    /**
     * Mask for flags we care about, the caremask if you will
     */
    final int stateMask;

    /**
     * Don't care about anything
     */
    public BasicInventoryPacketState() {
        this(0, Constants.Networking.MASK_NONE);
    }

    /**
     * We care a lot
     *
     * @param stateId state
     */
    public BasicInventoryPacketState(final int stateId) {
        this(stateId, Constants.Networking.MASK_ALL);
    }

    /**
     * We care about some things
     *
     * @param stateId flags we care about
     * @param stateMask caring mask
     */
    public BasicInventoryPacketState(final int stateId, final int stateMask) {
        this.stateId = stateId & stateMask;
        this.stateMask = stateMask;
    }

    @Override
    public boolean doesBulkBlockCapture(final InventoryPacketContext context) {
        return false;
    }

    @Nullable
    public ClickContainerEvent createInventoryEvent(final ServerPlayerEntity playerMP, final Container openContainer, final Transaction<ItemStackSnapshot> transaction,
            final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities, final int usedButton, @Nullable final Slot slot) {
        return null;
    }

    // Checks the proper ShouldFire flag for the event to be fired.
    // This should be the most specific known event that will be fired.
    // It's fine for this to be a supertype of the actual event - that will
    // just result in unnecessarily firing events.
    protected boolean shouldFire() {
       return ShouldFire.CLICK_CONTAINER_EVENT;
    }

    @Override
    public boolean matches(final int packetState) {
        return this.stateMask != Constants.Networking.MASK_NONE && ((packetState & this.stateMask & this.stateId) == (packetState & this.stateMask));
    }

    @Override
    public void populateContext(final ServerPlayerEntity playerMP, final IPacket<?> packet, final InventoryPacketContext context) {
        ((TrackedInventoryBridge) playerMP.openContainer).bridge$setCaptureInventory(true);
    }

    @Override
    public boolean shouldCaptureEntity() {
        // Example: Furnaces dropping XP when an item is picked up
        return true;
    }

    @Override
    public InventoryPacketContext createNewContext(final PhaseTracker tracker) {
        return new InventoryPacketContext(this, tracker).addCaptures().addEntityDropCaptures(); // if for whatever reason there's a capture.. i don't know...
    }


    private static Set<Class<?>> containersFailedCapture = new HashSet<>();

    @Override
    public void unwind(final InventoryPacketContext context) {
        final ServerPlayerEntity player = context.getPacketPlayer();

        // The server will disable the player's crafting after receiving a client packet
        // that did not pass validation (server click item != packet click item)
        // The server then sends a SPacketConfirmTransaction and waits for a
        // CPacketConfirmTransaction to re-enable crafting confirming that the
        // client acknowledged the denied transaction.
        // To detect when this happens, we turn off capturing so we can avoid
        // firing invalid events.
        // See NetHandlerPlayServerMixin processClickWindow redirect for rest of fix.
        // --bloodmc
        final TrackedInventoryBridge trackedInventory = (TrackedInventoryBridge) player.openContainer;
        if (!trackedInventory.bridge$capturingInventory() && !context.hasCaptures()) {
            trackedInventory.bridge$getCapturedSlotTransactions().clear();
            return;
        }

        final CClickWindowPacket packetIn = context.getPacket();
        final Transaction<ItemStackSnapshot> cursorTransaction = this.getCursorTransaction(context, player);

        final net.minecraft.inventory.container.Container openContainer = player.openContainer;
        final List<SlotTransaction> slotTransactions = trackedInventory.bridge$getCapturedSlotTransactions();

        final int usedButton = packetIn.getUsedButton();
        final List<Entity> capturedItems = new ArrayList<>();
        // MAKE SURE THAT THIS IS KEPT IN SYNC WITH THE REST OF THE METHOD
        // If you add any logic that does something even if no event listenres
        // are registered, add it here.

        // If there aren't any registered listeners,
        // we can skip an enormous amount of logic (creating transactions,
        // firing an event, checking for cancelled transaction, etc.)
        if (!this.shouldFire()) {
            if (ShouldFire.SPAWN_ENTITY_EVENT && !capturedItems.isEmpty()) {
                for (final Entity entiy: capturedItems) {
                    if (entiy instanceof CreatorTrackedBridge) {
                        ((CreatorTrackedBridge) entiy).tracked$setCreatorReference(((ServerPlayer) player).getUser());
                    } else {
                        entiy.offer(Keys.CREATOR, player.getUniqueID());
                    }
                }
                try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    PhaseTracker.getCauseStackManager().pushCause(openContainer);
                    PhaseTracker.getCauseStackManager().pushCause(player);
                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
                    SpongeCommonEventFactory.callSpawnEntity(capturedItems, context);
                }
            }
            slotTransactions.clear();
            trackedInventory.bridge$setCaptureInventory(false);
            return;
        }

        Slot slot = null;
        if (packetIn.getSlotId() >= 0) {
            slot = ((InventoryAdapter) trackedInventory).inventoryAdapter$getSlot(packetIn.getSlotId()).orElse(null);
        }
        // else TODO slot for ClickContainerEvent.Drag
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            PhaseTracker.getCauseStackManager().pushCause(openContainer);
            PhaseTracker.getCauseStackManager().pushCause(player);
            final ClickContainerEvent inventoryEvent;

            // We can only proceed with normal if and only if there are no entities spawned,
            // slot transactions exist, and or the slot id being touched is greater than
            // 0, avoiding idiotic negative slot id's.
            if (slotTransactions.isEmpty() && packetIn.getSlotId() >= 0 && capturedItems.isEmpty()) {
                if (player.openContainer.windowId != packetIn.getWindowId()) {
                    return; // Container mismatch - ignore this.
                }
                if (!((TrackedContainerBridge) trackedInventory).bridge$capturePossible()) {
                    // TODO When this happens a mod probably overrides Container#detectAndSendChanges
                    // We are currently unable to detect changes in this case.
                    if (!containersFailedCapture.contains(trackedInventory.getClass())) {
                        containersFailedCapture.add(trackedInventory.getClass());
                        SpongeCommon
                            .getLogger().warn("Changes in modded Container were not captured. Inventory events will not fire for this. Container: " + openContainer.getClass());
                    }
                    return;
                }
                // No SlotTransaction was captured. So we add the clicked slot as a transaction
                if (slot != null) {
                    final ItemStackSnapshot item = slot.peek().createSnapshot();
                    slotTransactions.add(new SlotTransaction(slot, item, item));
                }
            }

            inventoryEvent = this.createInventoryEvent(player, ContainerUtil.fromNative(openContainer), cursorTransaction,
                        new ArrayList<>(slotTransactions), capturedItems, usedButton, slot);

            if (inventoryEvent != null) {

                // The client sends several packets all at once for drag events
                // we only care about the last one.
                // Therefore, we never add any 'fake' transactions, as the final
                // packet has everything we want.
                if (!(inventoryEvent instanceof ClickContainerEvent.Drag)) {
                    PacketPhaseUtil.validateCapturedTransactions(packetIn.getSlotId(), openContainer, inventoryEvent.getTransactions());
                }

                SpongeCommon.postEvent(inventoryEvent);

                // Handle cursor
                if (inventoryEvent.isCancelled() || !inventoryEvent.getCursorTransaction().isValid()) {
                    PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.getCursorTransaction().getOriginal());
                } else if (inventoryEvent.getCursorTransaction().getCustom().isPresent()){
                    PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.getCursorTransaction().getFinal());
                }

                // Handle slots
                PacketPhaseUtil.handleSlotRestore(player, openContainer, inventoryEvent.getTransactions(), inventoryEvent.isCancelled());

                if (!inventoryEvent.isCancelled()) {
                    if (inventoryEvent instanceof SpawnEntityEvent) {
                        processSpawnedEntities(player, (SpawnEntityEvent) inventoryEvent);
                    } else if (!capturedItems.isEmpty()) {
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
                        SpongeCommonEventFactory.callSpawnEntity(capturedItems, context);
                    }
                } else if (inventoryEvent instanceof ClickContainerEvent.Drop) {
                    capturedItems.clear();
                }

            }
        } finally { // cleanup
            slotTransactions.clear();
            trackedInventory.bridge$setCaptureInventory(false);
        }
    }

    public Transaction<ItemStackSnapshot> getCursorTransaction(final InventoryPacketContext context, final ServerPlayerEntity player) {
        final ItemStackSnapshot lastCursor = context.getCursor();
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        return new Transaction<>(lastCursor, newCursor);
    }
}
