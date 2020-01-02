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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;
import org.spongepowered.common.mixin.core.entity.passive.AbstractChestHorseAccessor;
import org.spongepowered.common.mixin.core.entity.passive.EntityPigAccessor;
import org.spongepowered.common.mixin.core.entity.passive.EntitySheepAccessor;
import org.spongepowered.common.mixin.core.entity.passive.EntityWolfAccessor;
import org.spongepowered.common.mixin.core.network.play.client.CPacketPlayerAccessor;

import java.util.List;

import javax.annotation.Nullable;

public final class PacketPhaseUtil {

    @SuppressWarnings("rawtypes")
    public static void handleSlotRestore(final EntityPlayer player, @Nullable final Container openContainer, final List<SlotTransaction> slotTransactions, final boolean eventCancelled) {
        for (final SlotTransaction slotTransaction : slotTransactions) {

            if ((!slotTransaction.getCustom().isPresent() && slotTransaction.isValid()) && !eventCancelled) {
                continue;
            }
            final SlotAdapter slot = (SlotAdapter) slotTransaction.getSlot();
            final ItemStackSnapshot snapshot = eventCancelled || !slotTransaction.isValid() ? slotTransaction.getOriginal() : slotTransaction.getCustom().get();
            final ItemStack originalStack = ItemStackUtil.fromSnapshotToNative(snapshot);
            if (openContainer == null) {
                slot.set(((org.spongepowered.api.item.inventory.ItemStack) originalStack));
            } else {
                final int slotNumber = slot.slotNumber;
                final Slot nmsSlot = openContainer.getSlot(slotNumber);
                if (nmsSlot != null) {
                    nmsSlot.putStack(originalStack);
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
            if (eventCancelled && player.openContainer == openContainer && player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).sendContainerToPlayer(openContainer);
            }
        }
    }

    public static void handleCustomCursor(final EntityPlayer player, final ItemStackSnapshot customCursor) {
        final ItemStack cursor = ItemStackUtil.fromSnapshotToNative(customCursor);
        player.inventory.setItemStack(cursor);
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(-1, -1, cursor));
        }
    }

    public static void validateCapturedTransactions(final int slotId, final Container openContainer, final List<SlotTransaction> capturedTransactions) {
        if (capturedTransactions.size() == 0 && slotId >= 0 && slotId < openContainer.inventorySlots.size()) {
            final Slot slot = openContainer.getSlot(slotId);
            if (slot != null) {
                final ItemStackSnapshot snapshot = slot.getHasStack() ? ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot() : ItemStackSnapshot.NONE;
                final SlotTransaction slotTransaction = new SlotTransaction(ContainerUtil.getSlot(openContainer, slotId), snapshot, snapshot);
                capturedTransactions.add(slotTransaction);
            }
        }
    }

    public static void handlePlayerSlotRestore(final EntityPlayerMP player, final ItemStack itemStack, final EnumHand hand) {
        if (itemStack.isEmpty()) { // No need to check if it's NONE, NONE is checked by isEmpty.
            return;
        }

        player.isChangingQuantityOnly = false;
        int slotId = 0;
        if (hand == EnumHand.OFF_HAND) {
            player.inventory.offHandInventory.set(0, itemStack);
            slotId = (player.inventory.mainInventory.size() + InventoryPlayer.getHotbarSize());
        } else {
            player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
            final Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
            slotId = slot.slotNumber;
        }

        player.openContainer.detectAndSendChanges();
        player.isChangingQuantityOnly = false;
        player.connection.sendPacket(new SPacketSetSlot(player.openContainer.windowId, slotId, itemStack));
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
    public static void onProcessPacket(final Packet packetIn, final INetHandler netHandler) {
        if (netHandler instanceof NetHandlerPlayServer) {
            EntityPlayerMP packetPlayer = ((NetHandlerPlayServer) netHandler).player;
            // Only process the CustomPayload & Respawn packets from players if they are dead.
            if (!packetPlayer.isEntityAlive()
                    && (!(packetIn instanceof CPacketCustomPayload)
                    && (!(packetIn instanceof CPacketClientStatus)
                    || ((CPacketClientStatus) packetIn).getStatus() != CPacketClientStatus.State.PERFORM_RESPAWN))) {
                return;
            }

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(packetPlayer);
                if (SpongeImplHooks.creativeExploitCheck(packetIn, packetPlayer)) {
                    return;
                }

                // Don't process movement capture logic if player hasn't moved
                final boolean ignoreMovementCapture;
                if (packetIn instanceof CPacketPlayer) {
                    final CPacketPlayer movingPacket = ((CPacketPlayer) packetIn);
                    if (movingPacket instanceof CPacketPlayer.Rotation) {
                        ignoreMovementCapture = true;
                    } else if (packetPlayer.posX == ((CPacketPlayerAccessor) movingPacket).accessor$getX() && packetPlayer.posY == ((CPacketPlayerAccessor) movingPacket).accessor$getY() && packetPlayer.posZ == ((CPacketPlayerAccessor) movingPacket).accessor$getZ()) {
                        ignoreMovementCapture = true;
                    } else {
                        ignoreMovementCapture = false;
                    }
                } else {
                    ignoreMovementCapture = false;
                }
                if (ignoreMovementCapture || (packetIn instanceof CPacketClientSettings)) {
                    packetIn.processPacket(netHandler);
                } else {
                    final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(packetPlayer.inventory.getItemStack());
                    final IPhaseState<? extends PacketContext<?>> packetState = PacketPhase.getInstance().getStateForPacket(packetIn);
                    // At the very least make an unknown packet state case.
                    final PacketContext<?> context = packetState.createPhaseContext();
                    if (!PacketPhase.getInstance().isPacketInvalid(packetIn, packetPlayer, packetState)) {
                        context
                            .source(packetPlayer)
                            .packetPlayer(packetPlayer)
                            .packet(packetIn)
                            .cursor(cursor);

                        PacketPhase.getInstance().populateContext(packetIn, packetPlayer, packetState, context);
                        context.owner((Player) packetPlayer);
                        context.notifier((Player) packetPlayer);
                    }
                    try (final PhaseContext<?> packetContext = context) {
                        packetContext.buildAndSwitch();
                        packetIn.processPacket(netHandler);

                    }

                    if (packetIn instanceof CPacketClientStatus) {
                        // update the reference of player
                        packetPlayer = ((NetHandlerPlayServer) netHandler).player;
                    }
                    ((EntityPlayerMPBridge) packetPlayer).bridge$setPacketItem(ItemStack.EMPTY);
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

        if (item == Items.DYE) {
            // ItemDye.itemInteractionForEntity
            if (entity instanceof EntitySheep) {
                return EntitySheepAccessor.accessor$getDyeColorParameter();
            }

            // EntityWolf.processInteract
            if (entity instanceof EntityWolf) {
                return EntityWolfAccessor.accessor$getCollarColorParameter();
            }

            return null;
        }

        if (item == Items.NAME_TAG) {
            // ItemNameTag.itemInteractionForEntity
            return entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer) && stack.hasDisplayName() ? EntityAccessor.accessor$getCustomNameParameter() : null;
        }

        if (item == Items.SADDLE) {
            // ItemSaddle.itemInteractionForEntity
            return entity instanceof EntityPig ? EntityPigAccessor.accessor$getSaddledParameter() : null;
        }

        if (item instanceof ItemBlock && ((ItemBlock) item).getBlock() == Blocks.CHEST) {
            // AbstractChestHorse.processInteract
            return entity instanceof AbstractChestHorse ? AbstractChestHorseAccessor.accessor$getDataIdChestParameter() : null;
        }

        return null;
    }
}
