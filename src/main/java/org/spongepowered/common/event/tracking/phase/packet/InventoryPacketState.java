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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.List;

import javax.annotation.Nullable;

public enum InventoryPacketState implements IPacketState, IPhaseState {

    // ORDER MATTERS. TEST IF YOU RE-ARRANGE

    INVENTORY,
    PRIMARY_INVENTORY_CLICK(
            PacketPhase.MODE_CLICK | PacketPhase.MODE_DROP | PacketPhase.MODE_PICKBLOCK | PacketPhase.BUTTON_PRIMARY | PacketPhase.CLICK_INSIDE_WINDOW) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventPrimary(cause, transaction, openContainer, slotTransactions);
        }
    },
    DROP_ITEM(PacketPhase.MODE_CLICK | PacketPhase.MODE_DROP | PacketPhase.MODE_PICKBLOCK | PacketPhase.BUTTON_PRIMARY | PacketPhase.CLICK_ANYWHERE) {
        @Override
        public boolean doesCaptureEntityDrops() {
            return true;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            super.populateContext(playerMP, packet, context);
            context
                    .add(NamedCause.of(InternalNamedCauses.General.DESTRUCT_ITEM_DROPS, false));
        }

        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            final Cause spawnCause = Cause.source(EntitySpawnCause.builder()
                    .entity(EntityUtil.fromNative(playerMP))
                    .type(InternalSpawnTypes.DROPPED_ITEM)
                    .build())
                    .named(NamedCause.of("Container", openContainer))
                    .build();

            for (Entity currentEntity : capturedEntities) {
                currentEntity.setCreator(playerMP.getUniqueID());
            }
            final org.spongepowered.api.world.World spongeWorld = (org.spongepowered.api.world.World) playerMP.worldObj;
            return SpongeEventFactory.createClickInventoryEventDropFull(spawnCause, transaction, capturedEntities, openContainer, spongeWorld, slotTransactions);
        }

        @Override
        public boolean ignoresItemPreMerges() {
            return true;
        }
    },
    DROP_ITEMS() {

    },
    DROP_INVENTORY() {
    },
    DROP_SINGLE_ITEM_FROM_INVENTORY(
            PacketPhase.MODE_CLICK | PacketPhase.MODE_PICKBLOCK | PacketPhase.BUTTON_SECONDARY | PacketPhase.CLICK_OUTSIDE_WINDOW) {
        @Override
        public boolean doesCaptureEntityDrops() {
            return true;
        }

        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            final Cause spawnCause = Cause.source(EntitySpawnCause.builder()
                        .entity(EntityUtil.fromNative(playerMP))
                        .type(InternalSpawnTypes.DROPPED_ITEM)
                        .build())
                    .named(NamedCause.of("Container", openContainer))
                    .build();

            for (Entity currentEntity : capturedEntities) {
                currentEntity.setCreator(playerMP.getUniqueID());
            }
            final org.spongepowered.api.world.World spongeWorld = (org.spongepowered.api.world.World) playerMP.worldObj;
            return SpongeEventFactory.createClickInventoryEventDropSingle(spawnCause, transaction, capturedEntities, openContainer, spongeWorld, slotTransactions);

        }

        @Override
        public boolean ignoresItemPreMerges() {
            return true;
        }
    },
    SWITCH_HOTBAR_NUMBER_PRESS(PacketPhase.MODE_HOTBAR, PacketPhase.MASK_MODE) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventNumberPress(cause, transaction, openContainer,
                    slotTransactions, usedButton);
        }
    },
    PRIMARY_INVENTORY_SHIFT_CLICK(PacketPhase.MODE_SHIFT_CLICK | PacketPhase.BUTTON_PRIMARY, PacketPhase.MASK_NORMAL) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventShiftPrimary(cause, transaction, openContainer, slotTransactions);
        }
    },
    MIDDLE_INVENTORY_CLICK(PacketPhase.MODE_CLICK | PacketPhase.MODE_PICKBLOCK | PacketPhase.BUTTON_MIDDLE, PacketPhase.MASK_NORMAL) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventMiddle(cause, transaction, openContainer, slotTransactions);
        }
    },
    SECONDARY_INVENTORY_CLICK(PacketPhase.MODE_CLICK | PacketPhase.MODE_PICKBLOCK | PacketPhase.BUTTON_SECONDARY | PacketPhase.CLICK_INSIDE_WINDOW) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventSecondary(cause, transaction, openContainer, slotTransactions);
        }
    },
    SECONDARY_INVENTORY_CLICK_DROP(PacketPhase.MODE_DROP | PacketPhase.BUTTON_SECONDARY, PacketPhase.MASK_NORMAL) {
        @Override
        public boolean doesCaptureEntityDrops() {
            return true;
        }

        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventSecondary(cause, transaction, openContainer, slotTransactions);
        }
    },
    SECONDARY_INVENTORY_SHIFT_CLICK(PacketPhase.MODE_SHIFT_CLICK | PacketPhase.BUTTON_SECONDARY, PacketPhase.MASK_NORMAL) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventShiftSecondary(cause, transaction, openContainer, slotTransactions);

        }
    },
    DOUBLE_CLICK_INVENTORY(PacketPhase.MODE_DOUBLE_CLICK | PacketPhase.BUTTON_PRIMARY | PacketPhase.BUTTON_SECONDARY, PacketPhase.MASK_MODE | PacketPhase.MASK_BUTTON) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventDouble(cause, transaction, openContainer, slotTransactions);
        }
    },
    PRIMARY_DRAG_INVENTORY_START(
            PacketPhase.MODE_DRAG | PacketPhase.DRAG_MODE_SPLIT_ITEMS | PacketPhase.DRAG_STATUS_STARTED | PacketPhase.CLICK_OUTSIDE_WINDOW, PacketPhase.MASK_DRAG),
    SECONDARY_DRAG_INVENTORY_START(PacketPhase.MODE_DRAG | PacketPhase.DRAG_MODE_ONE_ITEM | PacketPhase.DRAG_STATUS_STARTED | PacketPhase.CLICK_OUTSIDE_WINDOW, PacketPhase.MASK_DRAG),
    PRIMARY_DRAG_INVENTORY_ADDSLOT(
            PacketPhase.MODE_DRAG | PacketPhase.DRAG_MODE_SPLIT_ITEMS | PacketPhase.DRAG_STATUS_ADD_SLOT | PacketPhase.CLICK_INSIDE_WINDOW, PacketPhase.MASK_DRAG),
    SECONDARY_DRAG_INVENTORY_ADDSLOT(PacketPhase.MODE_DRAG | PacketPhase.DRAG_MODE_ONE_ITEM | PacketPhase.DRAG_STATUS_ADD_SLOT | PacketPhase.CLICK_INSIDE_WINDOW, PacketPhase.MASK_DRAG),
    PRIMARY_DRAG_INVENTORY_STOP(
            PacketPhase.MODE_DRAG | PacketPhase.DRAG_MODE_SPLIT_ITEMS | PacketPhase.DRAG_STATUS_STOPPED | PacketPhase.CLICK_OUTSIDE_WINDOW, PacketPhase.MASK_DRAG) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventDragPrimary(cause, transaction, openContainer, slotTransactions);
        }
    },
    SECONDARY_DRAG_INVENTORY_STOP(PacketPhase.MODE_DRAG | PacketPhase.DRAG_MODE_ONE_ITEM | PacketPhase.DRAG_STATUS_STOPPED | PacketPhase.CLICK_OUTSIDE_WINDOW, PacketPhase.MASK_DRAG) {
        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventDragSecondary(cause, transaction, openContainer, slotTransactions);
        }
    },
    SWITCH_HOTBAR_SCROLL() {
        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            super.populateContext(playerMP, packet, context);
            context.add(NamedCause.of(InternalNamedCauses.Packet.PREVIOUS_HIGHLIGHTED_SLOT, playerMP.inventory.currentItem));
        }

        @Override
        public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
                List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
            return SpongeEventFactory.createClickInventoryEventNumberPress(cause, transaction, openContainer,
                    slotTransactions, usedButton);
        }
    },
    OPEN_INVENTORY,
    ENCHANT_ITEM;

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
    InventoryPacketState() {
        this(0, PacketPhase.MASK_NONE);
    }

    /**
     * We care a lot
     *
     * @param stateId state
     */
    InventoryPacketState(int stateId) {
        this(stateId, PacketPhase.MASK_ALL);
    }

    /**
     * We care about some things
     *
     * @param stateId flags we care about
     * @param stateMask caring mask
     */
    InventoryPacketState(int stateId, int stateMask) {
        this.stateId = stateId & stateMask;
        this.stateMask = stateMask;
    }

    @Override
    public PacketPhase getPhase() {
        return TrackingPhases.PACKET;
    }

    @Override
    public boolean matches(int packetState) {
        return this.stateMask != PacketPhase.MASK_NONE && ((packetState & this.stateMask & this.stateId) == (packetState & this.stateMask));
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        if (playerMP.openContainer != null) {
            ((IMixinContainer) playerMP.openContainer).setCaptureInventory(true);
        }
        context
                .addBlockCaptures()
                .addEntityCaptures()
                .addEntityDropCaptures();
    }

    @Nullable
    public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
            List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
        return null;
    }

    public static InventoryPacketState fromWindowPacket(CPacketClickWindow windowPacket) {
        final int mode = 0x01 << 9 << windowPacket.getClickType().ordinal();
        final int packed = windowPacket.getUsedButton();
        final int unpacked = mode == PacketPhase.MODE_DRAG ? (0x01 << 6 << (packed >> 2 & 3)) | (0x01 << 3 << (packed & 3)) : (0x01 << (packed & 3));
        return InventoryPacketState.fromState(InventoryPacketState.clickType(windowPacket.getSlotId()) | mode | unpacked);
    }

    public static InventoryPacketState fromState(final int state) {
        for (InventoryPacketState inventory : InventoryPacketState.values()) {
            if (inventory.matches(state)) {
                return inventory;
            }
        }
        return InventoryPacketState.INVENTORY;
    }

    private static int clickType(int slotId) {
        return (slotId == PacketPhase.MAGIC_CLICK_OUTSIDE_SURVIVAL || slotId == PacketPhase.MAGIC_CLICK_OUTSIDE_CREATIVE) ? PacketPhase.CLICK_OUTSIDE_WINDOW : PacketPhase.CLICK_INSIDE_WINDOW;
    }

}
