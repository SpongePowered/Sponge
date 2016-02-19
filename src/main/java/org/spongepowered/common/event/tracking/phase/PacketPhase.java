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
package org.spongepowered.common.event.tracking.phase;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ISpawnableState;
import org.spongepowered.common.event.tracking.PhaseContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

public class PacketPhase extends TrackingPhase {

    final static int MASK_OUTSIDE = 0x20; // 100000
    final static int MASK_MODE = 0x1C;    // 011100
    final static int MASK_BUTTON = 0x03;  // 000011

    final static int MASK_ALL = MASK_OUTSIDE | MASK_MODE | MASK_BUTTON;
    final static int MASK_NORMAL = MASK_MODE | MASK_BUTTON;

    final static int CLICK_WINDOW = 0x00;
    final static int CLICK_OUTSIDE_WINDOW = 0x20;

    // Inventory static fields
    public final static int BUTTON_PRIMARY = 0;
    public final static int BUTTON_SECONDARY = 1;
    public final static int BUTTON_MIDDLE = 2;
    public final static int CLICK_DRAG_LEFT = 2;
    public final static int CLICK_DRAG_RIGHT = 6;
    public final static int CLICK_OUTSIDE = -999;
    public final static int CLICK_OUTSIDE_CREATIVE = -1;

    public final static int MODE_CLICK = 0;
    public final static int MODE_SHIFT_CLICK = 1 << 2;
    public final static int MODE_HOTBAR = 2 << 2;
    public final static int MODE_PICKBLOCK = 3 << 2;
    public final static int MODE_DROP = 4 << 2;
    public final static int MODE_DRAG = 5 << 2;
    public final static int MODE_DOUBLE_CLICK = 6 << 2;

    public final static int DRAG_MODE_SPLIT_ITEMS = 0;
    public final static int DRAG_MODE_ONE_ITEM = 1;
    public final static int DRAG_STATUS_STARTED = 0;
    public final static int DRAG_STATUS_ADD_SLOT = 1;
    public final static int DRAG_STATUS_STOPPED = 2;

    public enum State implements IPhaseState, ISpawnableState {
        DROP_SINGLE_ITEM_FROM_INVENTORY(MODE_CLICK | MODE_PICKBLOCK | BUTTON_SECONDARY | CLICK_OUTSIDE_WINDOW),
        PRIMARY_INVENTORY_CLICK(MODE_CLICK | MODE_PICKBLOCK | BUTTON_PRIMARY | CLICK_WINDOW),
        DROP_ITEM(MODE_CLICK | MODE_PICKBLOCK | BUTTON_PRIMARY | CLICK_OUTSIDE_WINDOW),
        SECONDARY_INVENTORY_CLICK(MODE_CLICK | MODE_PICKBLOCK | BUTTON_SECONDARY | CLICK_WINDOW),
        SECONDARY_DRAG_INVENTORY(MODE_DRAG | CLICK_DRAG_RIGHT | CLICK_WINDOW),
        PRIMARY_DRAG_INVENTORY(MODE_DRAG | CLICK_DRAG_LEFT | CLICK_WINDOW),
        MIDDLE_INVENTORY_CLICK(MODE_CLICK | MODE_PICKBLOCK | BUTTON_MIDDLE, MASK_NORMAL),
        DRAGGING_INVENTORY(MODE_DRAG | CLICK_DRAG_LEFT | CLICK_DRAG_RIGHT, MASK_NORMAL),
        PRIMARY_INVENTORY_SHIFT_CLICK(MODE_SHIFT_CLICK | BUTTON_PRIMARY, MASK_NORMAL),
        SECONDARY_INVENTORY_SHIFT_CLICK(MODE_SHIFT_CLICK | BUTTON_SECONDARY, MASK_NORMAL),
        PRIMARY_INVENTORY_CLICK_DROP(MODE_DROP | BUTTON_PRIMARY, MASK_NORMAL),
        SECONDARY_INVENTORY_CLICK_DROP(MODE_DROP | BUTTON_SECONDARY, MASK_NORMAL),
        DOUBLE_CLICK_INVENTORY(MODE_DOUBLE_CLICK, MASK_MODE),
        SWITCH_HOTBAR_NUMBER_PRESS(MODE_HOTBAR, MASK_MODE),
        UNKNOWN,
        INVENTORY,
        DROP_ITEMS,
        DROP_INVENTORY,
        MOVEMENT,
        INTERACTION,
        IGNORED;

        final int stateId;

        final int stateMask;

        State() {
            this(1, 0);
        }

        State(int stateId) {
            this(stateId, MASK_ALL);
        }

        State(int stateId, int stateMask) {
            this.stateId = stateId & stateMask;
            this.stateMask = stateMask;
        }

        @Override
        public PacketPhase getPhase() {
            return TrackingPhases.PACKET;
        }

        @Override
        public boolean isBusy() {
            return true;
        }

        @Override
        public boolean isManaged() {
            return false;
        }

        @Nullable
        @Override
        public SpawnEntityEvent createEventPostPrcess(Cause cause, CauseTracker causeTracker, List<EntitySnapshot> entitySnapshots) {


            return null;
        }

        public boolean matches(int packetState) {
            return ((packetState & this.stateMask) | this.stateId) == this.stateId;
        }

        public static State fromPacket(C0EPacketClickWindow packetClickWindow) {
            final int clickMode = packetClickWindow.getMode();
            final int usedButton = packetClickWindow.getUsedButton();
            final boolean isClickOutside = packetClickWindow.getSlotId() == CLICK_OUTSIDE;

            final int packetState = (isClickOutside ? CLICK_OUTSIDE_WINDOW : CLICK_WINDOW) | clickMode << 2 | usedButton;
            for (State state : State.values()) {
                if (state.matches(packetState)) {
                    return state;
                }
            }
            return State.INVENTORY;
        }
    }

    private final Map<Class<? extends Packet<?>>, Function<Packet<?>, State>> packetTranslationMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public State getStateForPacket(Packet<?> packet) {
        final Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>) packet.getClass();
        final Function<Packet<?>, State> packetStateFunction = this.packetTranslationMap.get(packetClass);
        if (packetStateFunction != null) {
            return packetStateFunction.apply(packet);
        }
        return State.UNKNOWN;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.DROP_ITEM) {

        }
    }

    public PacketPhase(TrackingPhase parent) {
        super(parent);

        this.packetTranslationMap.put(C07PacketPlayerDigging.class, packet -> {
            final C07PacketPlayerDigging playerDigging = (C07PacketPlayerDigging) packet;
            final C07PacketPlayerDigging.Action action = playerDigging.getStatus();
            if (action == C07PacketPlayerDigging.Action.DROP_ITEM) {
                return State.DROP_ITEM;
            } else if (action == C07PacketPlayerDigging.Action.DROP_ALL_ITEMS) {
                return State.DROP_INVENTORY;
            } else if ( action == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                return State.INTERACTION;
            } else if ( action == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                return State.INTERACTION;
            } else if ( action == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                return State.INTERACTION;
            } else if ( action == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                return State.INTERACTION;
            } else {
                return State.IGNORED;
            }
        });
        this.packetTranslationMap.put(C10PacketCreativeInventoryAction.class, packet -> {
            final C10PacketCreativeInventoryAction creativePacket = (C10PacketCreativeInventoryAction) packet;

            return State.IGNORED;
        });
        this.packetTranslationMap.put(C0EPacketClickWindow.class, packet -> State.fromPacket((C0EPacketClickWindow) packet));
    }

}
