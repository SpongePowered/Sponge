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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ISpawnableState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.util.StaticMixinHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

public class PacketPhase extends TrackingPhase {

    final static int MASK_NONE              = 0x00000;
    final static int MASK_OUTSIDE           = 0x30000;
    final static int MASK_MODE              = 0x0FE00;
    final static int MASK_DRAGOPERATION     = 0x001C0;
    final static int MASK_DRAGEVENT         = 0x00038;
    final static int MASK_BUTTON            = 0x00007;

    final static int MASK_ALL               = MASK_OUTSIDE | MASK_MODE | MASK_BUTTON;
    final static int MASK_NORMAL            = MASK_MODE | MASK_BUTTON;
    
    // Inventory static fields
    final static int CLICK_OUTSIDE          = -999;
    final static int CLICK_OUTSIDE_CREATIVE = -1;
    
    final static int BUTTON_PRIMARY         = 0x01 << 0 << 0;
    final static int BUTTON_SECONDARY       = 0x01 << 0 << 1;
    final static int BUTTON_MIDDLE          = 0x01 << 0 << 2;

    final static int MODE_CLICK             = 0x01 << 9 << 0;
    final static int MODE_SHIFT_CLICK       = 0x01 << 9 << 1;
    final static int MODE_HOTBAR            = 0x01 << 9 << 2;
    final static int MODE_PICKBLOCK         = 0x01 << 9 << 3;
    final static int MODE_DROP              = 0x01 << 9 << 4;
    final static int MODE_DRAG              = 0x01 << 9 << 5;
    final static int MODE_DOUBLE_CLICK      = 0x01 << 9 << 6;

    final static int CLICK_WINDOW           = 0x01 << 16 << 0;
    final static int CLICK_OUTSIDE_WINDOW   = 0x01 << 16 << 1;
    
    final static int DRAG_MODE_SPLIT_ITEMS  = 0x01 << 6 << 0;
    final static int DRAG_MODE_ONE_ITEM     = 0x01 << 6 << 1;
    
    final static int DRAG_STATUS_STARTED    = 0x01 << 3 << 0;
    final static int DRAG_STATUS_ADD_SLOT   = 0x01 << 3 << 1;
    final static int DRAG_STATUS_STOPPED    = 0x01 << 3 << 2;

    public enum State implements IPhaseState, ISpawnableState {
        UNKNOWN,
        INVENTORY,
        DROP_ITEM(MODE_CLICK | MODE_PICKBLOCK | BUTTON_PRIMARY | CLICK_OUTSIDE_WINDOW),
        DROP_ITEMS,
        DROP_INVENTORY,
        MOVEMENT,
        INTERACTION,
        IGNORED,
        DROP_SINGLE_ITEM_FROM_INVENTORY(MODE_CLICK | MODE_PICKBLOCK | BUTTON_SECONDARY | CLICK_OUTSIDE_WINDOW),
        SWITCH_HOTBAR_NUMBER_PRESS(MODE_HOTBAR, MASK_MODE),
        PRIMARY_INVENTORY_CLICK(MODE_CLICK | MODE_DROP | MODE_PICKBLOCK | BUTTON_PRIMARY | CLICK_WINDOW),
        PRIMARY_INVENTORY_SHIFT_CLICK(MODE_SHIFT_CLICK | BUTTON_PRIMARY, MASK_NORMAL),
//        PRIMARY_DRAG_INVENTORY(MODE_DRAG | CLICK_DRAG_LEFT | CLICK_WINDOW),
        MIDDLE_INVENTORY_CLICK(MODE_CLICK | MODE_PICKBLOCK | BUTTON_MIDDLE, MASK_NORMAL),
        SECONDARY_INVENTORY_CLICK(MODE_CLICK | MODE_PICKBLOCK | BUTTON_SECONDARY | CLICK_WINDOW),
        SECONDARY_INVENTORY_CLICK_DROP(MODE_DROP | BUTTON_SECONDARY, MASK_NORMAL),
        SECONDARY_INVENTORY_SHIFT_CLICK(MODE_SHIFT_CLICK | BUTTON_SECONDARY, MASK_NORMAL),
//        SECONDARY_DRAG_INVENTORY(MODE_DRAG | CLICK_DRAG_RIGHT | CLICK_WINDOW),
//        DRAGGING_INVENTORY(MODE_DRAG | CLICK_DRAG_LEFT | CLICK_DRAG_RIGHT, MASK_NORMAL),
        DOUBLE_CLICK_INVENTORY(MODE_DOUBLE_CLICK, MASK_MODE),
        INTERACT_ENTITY,
        ATTACK_ENTITY,
        INTERACT_AT_ENTITY,
        CHAT, CREATIVE_INVENTORY;
        
        final int stateId;

        final int stateMask;

        State() {
            this(0, MASK_NONE);
        }

        State(int stateId) {
            this(stateId, MASK_ALL);
        }

        State(int stateId, int stateMask) {
            this.stateId = stateId & stateMask;
            this.stateMask = stateMask;
            System.err.printf(">> %-36s [%22s] [%22s]\n", this.name(), bin(this.stateId), bin(this.stateMask));
        }

        private static String bin(int value) {
            String str = Integer.toBinaryString(value);
            while (str.length() < 18) {
                str = "0" + str;
            }
            return str.substring(0, 2) + " " + str.substring(2, 9) + " " + str.substring(9, 12) + " " + str.substring(12, 15) + " " + str.substring(15, 18);
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
        public SpawnEntityEvent createSpawnEventPostProcess(Cause cause, CauseTracker causeTracker, List<EntitySnapshot> entitySnapshots) {


            return null;
        }

        public boolean matches(int packetState) {
            if (this.stateMask == MASK_NONE) {
                System.err.printf(" -->  [%22s] %-40s", "SKIP", this.name());
                return false;
            }
            
            int masked = packetState & this.stateMask;
            int anded = masked & this.stateId;
            System.err.printf("[%22s] %-40s    STATE=[%22s]    MASKED=[%22s]    ANDED=[%22s]", bin(this.stateId), this.name(), bin(packetState), bin(masked), bin(anded));
            boolean result = anded == masked;
            if (result) {
                System.err.printf("    MATCH");
            }
            return false;
        }

        public static State fromWindowPacket(C0EPacketClickWindow packetClickWindow) {
            final int clickMode = packetClickWindow.getMode();
            final int usedButton = packetClickWindow.getUsedButton();
            final boolean isClickOutside = packetClickWindow.getSlotId() == CLICK_OUTSIDE;
            
            final int dragMode = 0x01 << 6 << (usedButton >> 2 & 3);
            final int dragEvent = 0x01 << 3 << usedButton & 3;

            final int packetState = (isClickOutside ? CLICK_OUTSIDE_WINDOW : CLICK_WINDOW) | 0x01 << 9 << clickMode | dragMode | dragEvent  | 0x01 << (usedButton & 3);
            System.err.printf("======================================================================\n");
            System.err.printf("Comparing incoming state [%12s] (MODE=%s, DRAGOP=%s, DRAGEVT=%s, OUTSIDE=%s):\n", bin(packetState), clickMode, usedButton >> 2 & 3, usedButton & 3, isClickOutside);
            State retState = State.INVENTORY;
            for (State state : State.values()) {
                if (state.matches(packetState)) {
                    System.err.printf(" >>>> MATCHED: %s\n", state);
                    retState = state;
                } else {
                    System.err.printf("\n");
                }
            }
            
            return retState;
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

    public PhaseContext populateContext(Packet<?> packet, EntityPlayerMP entityPlayerMP, IPhaseState state, PhaseContext context) {
        checkNotNull(packet, "Packet cannot be null!");
        checkArgument(!context.isComplete(), "PhaseContext cannot be marked as completed!");
        if (state == State.ATTACK_ENTITY) {
            final C02PacketUseEntity useEntityPacket = (C02PacketUseEntity) packet;
            net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(entityPlayerMP.worldObj);
            context.add(NamedCause.of(TrackingHelper.TARGETED_ENTITY, entity));
            context.add(NamedCause.of(TrackingHelper.TRACKED_ENTITY_ID, entity.getEntityId()));
        } else if (state == State.CHAT) {
            context.add(NamedCause.of("Player", entityPlayerMP));
            C01PacketChatMessage chatMessage = (C01PacketChatMessage) packet;
            if (chatMessage.getMessage().contains("kill")) {
                context.add(NamedCause.of(TrackingHelper.DESTRUCT_ITEM_DROPS, true));
            }
        } else if (state == State.DROP_ITEM) {
            context.add(NamedCause.of(TrackingHelper.DESTRUCT_ITEM_DROPS, false));
        }
        return context;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState phaseState, PhaseContext phaseContext) {
        Packet<?> packetIn = phaseContext.firstNamed(TrackingHelper.CAPTURED_PACKET, Packet.class).get();
        EntityPlayerMP player = phaseContext.firstNamed(TrackingHelper.PACKET_PLAYER, EntityPlayerMP.class).get();
        World minecraftWorld = player.worldObj;

        if (phaseState == State.DROP_ITEM) {

        } else if (phaseState == State.ATTACK_ENTITY) {
            final C02PacketUseEntity useEntityPacket = (C02PacketUseEntity) packetIn;
            net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(minecraftWorld);
            if (entity != null && entity.isDead && !(entity instanceof EntityLivingBase)) {
                Player spongePlayer = (Player) player;
                MessageChannel originalChannel = spongePlayer.getMessageChannel();
                Cause cause = Cause.of(NamedCause.source(spongePlayer));

                DestructEntityEvent event = SpongeEventFactory.createDestructEntityEvent(cause, originalChannel, Optional.of(originalChannel),
                        Optional.empty(), Optional.empty(), (Entity) entity);
                SpongeImpl.getGame().getEventManager().post(event);
                event.getMessage().ifPresent(text -> event.getChannel().ifPresent(channel -> channel.send(text)));
            }
        } else if (phaseState == State.CREATIVE_INVENTORY) {
            boolean ignoringCreative = phaseContext.firstNamed(TrackingHelper.IGNORING_CREATIVE, Boolean.class).orElse(false);
            if (!ignoringCreative) {
                SpongeCommonEventFactory.handleCreativeClickInventoryEvent(Cause.of(NamedCause.source(player)), player,
                                ((C10PacketCreativeInventoryAction) packetIn),
                                phaseState, phaseContext);
            }
        }
    }

    public PacketPhase(TrackingPhase parent) {
        super(parent);
        this.packetTranslationMap.put(C01PacketChatMessage.class, packet -> State.CHAT);
        this.packetTranslationMap.put(C02PacketUseEntity.class, packet -> {
            final C02PacketUseEntity useEntityPacket = (C02PacketUseEntity) packet;
            final C02PacketUseEntity.Action action = useEntityPacket.getAction();
            if ( action == C02PacketUseEntity.Action.INTERACT) {
                return State.INTERACT_ENTITY;
            } else if (action == C02PacketUseEntity.Action.ATTACK) {
                return State.ATTACK_ENTITY;
            } else if (action == C02PacketUseEntity.Action.INTERACT_AT) {
                return State.INTERACT_AT_ENTITY;
            } else {
                return State.UNKNOWN;
            }
        });
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
        this.packetTranslationMap.put(C10PacketCreativeInventoryAction.class, packet -> State.CREATIVE_INVENTORY);
        this.packetTranslationMap.put(C0EPacketClickWindow.class, packet -> State.fromWindowPacket((C0EPacketClickWindow) packet));

    }

}
