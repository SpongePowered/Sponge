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

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

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
    public BiConsumer<CauseStackManager.StackFrame, InventoryPacketContext> getFrameModifier() {
        return super.getFrameModifier().andThen((frame, context) -> {
            final net.minecraft.server.level.ServerPlayer player = context.getPacketPlayer();
            final net.minecraft.world.inventory.AbstractContainerMenu openContainer = player.containerMenu;
            frame.pushCause(openContainer);
            frame.pushCause(player);
        });
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final InventoryPacketContext context, final net.minecraft.world.entity.Entity entityToSpawn
    ) {
        return SpawnTypes.DROPPED_ITEM;
    }

    @Override
    public @Nullable ClickContainerEvent createInventoryEvent(final InventoryPacketContext context, final net.minecraft.server.level.ServerPlayer playerMP, final Container openContainer, final Transaction<ItemStackSnapshot> transaction,
                                                              final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities, final int usedButton, final @Nullable Slot slot) {
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
    public void populateContext(final net.minecraft.server.level.ServerPlayer playerMP, final Packet<?> packet, final InventoryPacketContext context) {
        ((TrackedInventoryBridge) playerMP.containerMenu).bridge$setCaptureInventory(true);
    }

    @Override
    public InventoryPacketContext createNewContext(final PhaseTracker tracker) {
        return new InventoryPacketContext(this, tracker);
    }


    private static Set<Class<?>> containersFailedCapture = new ReferenceOpenHashSet<>();

    @Override
    public void unwind(final InventoryPacketContext context) {
        final net.minecraft.server.level.ServerPlayer player = context.getPacketPlayer();

        final ServerboundContainerClickPacket packetIn = context.getPacket();
        final Transaction<ItemStackSnapshot> cursorTransaction = this.getCursorTransaction(context, player);

        final net.minecraft.world.inventory.AbstractContainerMenu openContainer = player.containerMenu;
        final List<SlotTransaction> slotTransactions = Collections.emptyList();

        final int usedButton = packetIn.getButtonNum();
        final List<Entity> capturedItems = new ArrayList<>();
        // MAKE SURE THAT THIS IS KEPT IN SYNC WITH THE REST OF THE METHOD
        // If you add any logic that does something even if no event listenres
        // are registered, add it here.

        // If there aren't any registered listeners,
        // we can skip an enormous amount of logic (creating transactions,
        // firing an event, checking for cancelled transaction, etc.)
        if (!TrackingUtil.processBlockCaptures(context)) {
            return;
        }

        Slot slot = null;
        if (packetIn.getButtonNum() >= 0) {
            slot = ((InventoryAdapter) trackedInventory).inventoryAdapter$getSlot(packetIn.getSlotNum()).orElse(null);
        }
        // else TODO slot for ClickContainerEvent.Drag
        try {
            final @Nullable ClickContainerEvent inventoryEvent;


            inventoryEvent = this.createInventoryEvent(context, player, ContainerUtil.fromNative(openContainer), cursorTransaction,
                        new ArrayList<>(slotTransactions), capturedItems, usedButton, slot);

            if (inventoryEvent != null) {

                SpongeCommon.post(inventoryEvent);

                // Handle cursor
                if (!inventoryEvent.cursorTransaction().isValid()) {
                    PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.cursorTransaction().original());
                } else if (inventoryEvent.cursorTransaction().custom().isPresent()){
                    PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.cursorTransaction().finalReplacement());
                }

                // Handle slots
                PacketPhaseUtil.handleSlotRestore(player, openContainer, inventoryEvent.transactions(), inventoryEvent.isCancelled());

                if (!inventoryEvent.isCancelled()) {
                    if (inventoryEvent instanceof SpawnEntityEvent) {
                        PacketState.processSpawnedEntities(player, (SpawnEntityEvent) inventoryEvent);
                    } else if (!capturedItems.isEmpty()) {
                        SpongeCommonEventFactory.callSpawnEntity(capturedItems, context);
                    }
                } else if (inventoryEvent instanceof ClickContainerEvent.Drop) {
                    capturedItems.clear();
                }

            }
        }
    }

    @Override
    public void restoreClickContainerEvent(
        final InventoryPacketContext context, final ClickContainerEvent event
    ) {
        final net.minecraft.server.level.ServerPlayer player = context.getPacketPlayer();
        PacketPhaseUtil.handleCustomCursor(player, event.cursorTransaction().original());

        super.restoreClickContainerEvent(context, event);
    }

    public Transaction<ItemStackSnapshot> getCursorTransaction(final InventoryPacketContext context, final net.minecraft.server.level.ServerPlayer player) {
        final ItemStackSnapshot lastCursor = context.getCursor();
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getCarried());
        return new Transaction<>(lastCursor, newCursor);
    }
}
