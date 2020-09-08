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

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.Hand;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.accessor.entity.passive.AbstractChestedHorseEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.PigEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.SheepEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.WolfEntityAccessor;
import org.spongepowered.common.accessor.inventory.container.SlotAccessor;
import org.spongepowered.common.accessor.network.play.client.CPlayerPacketAccessor;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;

import javax.annotation.Nullable;

public final class PacketPhaseUtil {

    @SuppressWarnings("rawtypes")
    public static boolean handleSlotRestore(final PlayerEntity player, @Nullable final Container openContainer, final List<SlotTransaction> slotTransactions, final boolean eventCancelled) {
        boolean restoredAny = false;
        for (final SlotTransaction slotTransaction : slotTransactions) {

            if ((!slotTransaction.getCustom().isPresent() && slotTransaction.isValid()) && !eventCancelled) {
                continue;
            }
            restoredAny = true;
            final SlotAdapter slot = (SlotAdapter) slotTransaction.getSlot();
            final ItemStackSnapshot snapshot = eventCancelled || !slotTransaction.isValid() ? slotTransaction.getOriginal() : slotTransaction.getCustom().get();
            if (openContainer == null) {
                slot.set(snapshot.createStack());
            } else {
                final int slotNumber = slot.getOrdinal();
                final Slot nmsSlot = openContainer.getSlot(slotNumber);
                if (nmsSlot != null) {
                    nmsSlot.putStack(ItemStackUtil.fromSnapshotToNative(snapshot));
                }
            }
        }
        if (openContainer != null) {
            final boolean capture = ((TrackedInventoryBridge) openContainer).bridge$capturingInventory();
            ((TrackedInventoryBridge) openContainer).bridge$setCaptureInventory(false);
            openContainer.detectAndSendChanges();
            ((TrackedInventoryBridge) openContainer).bridge$setCaptureInventory(capture);
            // If event is cancelled, always resync with player
            // we must also validate the player still has the same container open after the event has been processed
            if (eventCancelled && player.openContainer == openContainer && player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).sendContainerToPlayer(openContainer);
            }
        }
        return restoredAny;
    }

    public static void handleCustomCursor(final PlayerEntity player, final ItemStackSnapshot customCursor) {
        final ItemStack cursor = ItemStackUtil.fromSnapshotToNative(customCursor);
        player.inventory.setItemStack(cursor);
        if (player instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) player).connection.sendPacket(new SSetSlotPacket(-1, -1, cursor));
        }
    }

    public static void validateCapturedTransactions(final int slotId, final Container openContainer, final List<SlotTransaction> capturedTransactions) {
        if (capturedTransactions.size() == 0 && slotId >= 0 && slotId < openContainer.inventorySlots.size()) {
            final Slot slot = openContainer.getSlot(slotId);
            if (slot != null) {
                final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(slot.getStack());
                final SlotTransaction slotTransaction = new SlotTransaction(
                        ((InventoryAdapter) openContainer).inventoryAdapter$getSlot(slotId).get(), snapshot, snapshot);
                capturedTransactions.add(slotTransaction);
            }
        }
    }

    public static void handlePlayerSlotRestore(final ServerPlayerEntity player, final ItemStack itemStack, final Hand hand) {
        if (itemStack.isEmpty()) { // No need to check if it's NONE, NONE is checked by isEmpty.
            return;
        }

        player.isChangingQuantityOnly = false;
        int slotId = 0;
        if (hand == Hand.OFF_HAND) {
            player.inventory.offHandInventory.set(0, itemStack);
            slotId = (player.inventory.mainInventory.size() + PlayerInventory.getHotbarSize());
        } else {
            player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
            // TODO check if window id -2 and slotid = player.inventory.currentItem works instead of this:
            for (Slot containerSlot : player.openContainer.inventorySlots) {
                if (containerSlot.inventory == player.inventory && ((SlotAccessor) containerSlot).accessor$getSlotIndex() == player.inventory.currentItem) {
                    slotId = containerSlot.slotNumber;
                    break;
                }
            }
        }

        player.openContainer.detectAndSendChanges();
        player.isChangingQuantityOnly = false;
        player.connection.sendPacket(new SSetSlotPacket(player.openContainer.windowId, slotId, itemStack));
    }

    // Check if all transactions are invalid
    public static boolean allTransactionsInvalid(final List<SlotTransaction> slotTransactions) {
        if (slotTransactions.size() == 0) {
            return false;
        }

        for (final SlotTransaction slotTransaction : slotTransactions) {
            if (slotTransaction.isValid()) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onProcessPacket(final IPacket packetIn, final INetHandler netHandler) {
        if (netHandler instanceof ServerPlayNetHandler) {
            ServerPlayerEntity packetPlayer = ((ServerPlayNetHandler) netHandler).player;
            // Only process the CustomPayload & Respawn packets from players if they are dead.
            if (!packetPlayer.isAlive()
                    && (!(packetIn instanceof CCustomPayloadPacket)
                    && (!(packetIn instanceof CClientStatusPacket)
                    || ((CClientStatusPacket) packetIn).getStatus() != CClientStatusPacket.State.PERFORM_RESPAWN))) {
                return;
            }
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(packetPlayer);
                if (SpongeImplHooks.creativeExploitCheck(packetIn, packetPlayer)) {
                    return;
                }

                // Don't process movement capture logic if player hasn't moved
                final boolean ignoreMovementCapture;
                if (packetIn instanceof CPlayerPacket) {
                    final CPlayerPacket movingPacket = ((CPlayerPacket) packetIn);
                    if (movingPacket instanceof CPlayerPacket.RotationPacket) {
                        ignoreMovementCapture = true;
                    } else if (packetPlayer.getPosX() == ((CPlayerPacketAccessor) movingPacket).accessor$getX() && packetPlayer.getPosY() == ((CPlayerPacketAccessor) movingPacket).accessor$getY() && packetPlayer.getPosZ() == ((CPlayerPacketAccessor) movingPacket).accessor$getZ()) {
                        ignoreMovementCapture = true;
                    } else {
                        ignoreMovementCapture = false;
                    }
                } else {
                    ignoreMovementCapture = false;
                }
                if (ignoreMovementCapture || (packetIn instanceof CClientSettingsPacket)) {
                    packetIn.processPacket(netHandler);
                } else {
                    final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(packetPlayer.inventory.getItemStack());
                    final IPhaseState<? extends PacketContext<?>> packetState = PacketPhase.getInstance().getStateForPacket(packetIn);
                    // At the very least make an unknown packet state case.
                    final PacketContext<?> context = packetState.createPhaseContext(PhaseTracker.SERVER);
                    if (!PacketPhase.getInstance().isPacketInvalid(packetIn, packetPlayer, packetState)) {
                        context
                            .source(packetPlayer)
                            .packetPlayer(packetPlayer)
                            .packet(packetIn)
                            .cursor(cursor);

                        PacketPhase.getInstance().populateContext(packetIn, packetPlayer, packetState, context);
                        context.creator(((ServerPlayer) packetPlayer).getUser());
                        context.notifier(((ServerPlayer) packetPlayer).getUser());
                    }
                    try (final PhaseContext<?> packetContext = context) {
                        packetContext.buildAndSwitch();
                        packetIn.processPacket(netHandler);

                    }

                    if (packetIn instanceof CClientStatusPacket) {
                        // update the reference of player
                        packetPlayer = ((ServerPlayNetHandler) netHandler).player;
                    }
                    ((ServerPlayerEntityBridge) packetPlayer).bridge$setPacketItem(ItemStack.EMPTY);
                }
            }
        } else { // client
            packetIn.processPacket(netHandler);
        }
    }

    /**
     * Attempts to find the {@link DataParameter} that was potentially modified
     * when a player interacts with an entity.
     *
     * @param stack The item the player is holding
     * @param entity The entity
     * @return A possible data parameter or null if unknown
     */
    @Nullable
    public static DataParameter<?> findModifiedEntityInteractDataParameter(final ItemStack stack, final Entity entity) {
        final Item item = stack.getItem();

        if (item instanceof DyeItem) {
            // ItemDye.itemInteractionForEntity
            if (entity instanceof SheepEntity) {
                return SheepEntityAccessor.accessor$getDyeColor();
            }

            // EntityWolf.processInteract
            if (entity instanceof WolfEntity) {
                return WolfEntityAccessor.accessor$getCollarColor();
            }

            return null;
        }

        if (item == Items.NAME_TAG) {
            // ItemNameTag.itemInteractionForEntity
            return entity instanceof LivingEntity && !(entity instanceof PlayerEntity) && stack.hasDisplayName() ? EntityAccessor.accessor$getCustomName() : null;
        }

        if (item == Items.SADDLE) {
            // ItemSaddle.itemInteractionForEntity
            return entity instanceof PigEntity ? PigEntityAccessor.accessor$getSaddled() : null;
        }

        if (item instanceof BlockItem && ((BlockItem) item).getBlock() == Blocks.CHEST) {
            // AbstractChestHorse.processInteract
            return entity instanceof AbstractChestedHorseEntity ? AbstractChestedHorseEntityAccessor.accessor$getDataIdChest() : null;
        }

        return null;
    }
}
