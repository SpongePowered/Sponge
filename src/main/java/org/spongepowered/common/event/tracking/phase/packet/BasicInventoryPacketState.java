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
package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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
        this(0, PacketPhase.MASK_NONE);
    }

    /**
     * We care a lot
     *
     * @param stateId state
     */
    public BasicInventoryPacketState(int stateId) {
        this(stateId, PacketPhase.MASK_ALL);
    }

    /**
     * We care about some things
     *
     * @param stateId flags we care about
     * @param stateMask caring mask
     */
    public BasicInventoryPacketState(int stateId, int stateMask) {
        this.stateId = stateId & stateMask;
        this.stateMask = stateMask;
    }

    @Override
    public boolean doesBulkBlockCapture(InventoryPacketContext context) {
        return false;
    }

    @Nullable
    public ClickInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
            List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, int usedButton) {
        return null;
    }

    @Override
    public boolean matches(int packetState) {
        return this.stateMask != PacketPhase.MASK_NONE && ((packetState & this.stateMask & this.stateId) == (packetState & this.stateMask));
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, InventoryPacketContext context) {
        ((IMixinContainer) playerMP.openContainer).setCaptureInventory(true);
        context.addBlockCaptures()
               .addEntityCaptures()
               .addEntityDropCaptures();
    }

    @Override
    public boolean shouldCaptureEntity() {
        // Example: Furnaces dropping XP when an item is picked up
        return true;
    }

    @Override
    public InventoryPacketContext createPhaseContext() {
        return new InventoryPacketContext(this);
    }

    @Override
    public void unwind(InventoryPacketContext context) {
        final EntityPlayerMP player = context.getPacketPlayer();

        // The server will disable the player's crafting after receiving a client packet
        // that did not pass validation (server click item != packet click item)
        // The server then sends a SPacketConfirmTransaction and waits for a
        // CPacketConfirmTransaction to re-enable crafting confirming that the
        // client acknowledged the denied transaction.
        // To detect when this happens, we turn off capturing so we can avoid
        // firing invalid events.
        // See MixinNetHandlerPlayServer processClickWindow redirect for rest of fix.
        // --bloodmc
        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
        if (!mixinContainer.capturingInventory()) {
            mixinContainer.getCapturedTransactions().clear();
            return;
        }

        // TODO clear this shit out of the context
        final CPacketClickWindow packetIn = context.getPacket();
        final Transaction<ItemStackSnapshot> cursorTransaction = this.getCursorTransaction(context, player);

        final net.minecraft.inventory.Container openContainer = player.openContainer;
        final List<SlotTransaction> slotTransactions = mixinContainer.getCapturedTransactions();

        final int usedButton = packetIn.getUsedButton();
        final List<Entity> capturedItems = new ArrayList<>();
        for (EntityItem entityItem : context.getCapturedItems()) {
            capturedItems.add(EntityUtil.fromNative(entityItem));
        }
        context.getCapturedItems().clear();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(openContainer);
            Sponge.getCauseStackManager().pushCause(player);
            final ClickInventoryEvent inventoryEvent;

            if (slotTransactions.isEmpty() && packetIn.getSlotId() >= 0) {
                if (!mixinContainer.capturePossible()) {
                    // TODO When this happens a mod probably overrides Container#detectAndSendChanges
                    // We are currently unable to detect changes in this case.
                    SpongeImpl.getLogger().info("Container was not captured. No ClickInventoryEvent will be fired! Container: " + openContainer.getClass() + " PacketState: " + this.getClass().getSimpleName());
                    return;
                }
                // No SlotTransaction was captured. So we add the clicked slot as a transaction
                Slot slot = mixinContainer.getContainerSlot(packetIn.getSlotId());
                ItemStackSnapshot item = slot.peek().createSnapshot();
                slotTransactions.add(new SlotTransaction(slot, item, item));
            }

            inventoryEvent = this.createInventoryEvent(player, ContainerUtil.fromNative(openContainer), cursorTransaction,
                        new ArrayList<>(slotTransactions),
                        capturedItems, usedButton);

            if (inventoryEvent != null) {

                // The client sends several packets all at once for drag events
                // we only care about the last one.
                // Therefore, we never add any 'fake' transactions, as the final
                // packet has everything we want.
                if (!(inventoryEvent instanceof ClickInventoryEvent.Drag)) {
                    PacketPhaseUtil.validateCapturedTransactions(packetIn.getSlotId(), openContainer, inventoryEvent.getTransactions());
                }

                SpongeImpl.postEvent(inventoryEvent);

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
                    } else {
                        context.getCapturedEntitySupplier().acceptAndClearIfNotEmpty((entities -> {
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
                            SpongeCommonEventFactory.callSpawnEntity(entities, context);
                        }));
                    }
                } else if (inventoryEvent instanceof ClickInventoryEvent.Drop) {
                    capturedItems.clear();
                }

            }
        } finally { // cleanup
            slotTransactions.clear();
            mixinContainer.setCaptureInventory(false);
        }
    }

    public Transaction<ItemStackSnapshot> getCursorTransaction(InventoryPacketContext context, EntityPlayerMP player) {
        final ItemStackSnapshot lastCursor = context.getCursor();
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        return new Transaction<>(lastCursor, newCursor);
    }
}
