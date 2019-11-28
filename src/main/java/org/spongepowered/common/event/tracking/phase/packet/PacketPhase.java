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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlaceRecipe;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.drag.DragInventoryAddSlotState;
import org.spongepowered.common.event.tracking.phase.packet.drag.DragInventoryStartState;
import org.spongepowered.common.event.tracking.phase.packet.drag.MiddleDragInventoryStopState;
import org.spongepowered.common.event.tracking.phase.packet.drag.PrimaryDragInventoryStopState;
import org.spongepowered.common.event.tracking.phase.packet.drag.SecondaryDragInventoryStopState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.BasicInventoryPacketState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.CloseWindowState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.CreativeInventoryPacketState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.DoubleClickInventoryState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.DropInventoryState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.DropItemOutsideWindowState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.DropItemWithHotkeyState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.EnchantItemPacketState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.MiddleInventoryClickState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.OpenInventoryState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.PlaceRecipePacketState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.PrimaryInventoryClickState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.PrimaryInventoryShiftClick;
import org.spongepowered.common.event.tracking.phase.packet.inventory.SecondaryInventoryClickState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.SecondaryInventoryShiftClickState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.SwapHandItemsState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.SwitchHotbarNumberPressState;
import org.spongepowered.common.event.tracking.phase.packet.inventory.SwitchHotbarScrollState;
import org.spongepowered.common.event.tracking.phase.packet.player.AnimationPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.AttackEntityPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.IgnoredPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.InteractAtEntityPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.InteractEntityPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.InteractionPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.player.InteractionPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.InvalidPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.MovementPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.PlaceBlockPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.ResourcePackState;
import org.spongepowered.common.event.tracking.phase.packet.player.StopSleepingPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.UnknownPacketState;
import org.spongepowered.common.event.tracking.phase.packet.player.UpdateSignState;
import org.spongepowered.common.event.tracking.phase.packet.player.UseItemPacketState;
import org.spongepowered.common.util.Constants;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public final class PacketPhase {


    public static final class General {

        public static final IPhaseState<BasicPacketContext> UNKNOWN = new UnknownPacketState();
        static final IPhaseState<BasicPacketContext> MOVEMENT = new MovementPacketState();
        static final IPhaseState<InteractionPacketContext> INTERACTION = new InteractionPacketState();
        static final IPhaseState<BasicPacketContext> IGNORED = new IgnoredPacketState();
        static final IPhaseState<BasicPacketContext> INTERACT_ENTITY = new InteractEntityPacketState();
        static final IPhaseState<BasicPacketContext> ATTACK_ENTITY = new AttackEntityPacketState();
        static final IPhaseState<BasicPacketContext> INTERACT_AT_ENTITY = new InteractAtEntityPacketState();
        static final IPhaseState<BasicPacketContext> CREATIVE_INVENTORY = new CreativeInventoryPacketState();
        static final IPhaseState<BasicPacketContext> PLACE_BLOCK = new PlaceBlockPacketState();
        static final IPhaseState<BasicPacketContext> REQUEST_RESPAWN = new BasicPacketState();
        static final IPhaseState<BasicPacketContext> USE_ITEM = new UseItemPacketState();
        static final IPhaseState<BasicPacketContext> INVALID = new InvalidPacketState();
        static final IPhaseState<BasicPacketContext> START_RIDING_JUMP = new BasicPacketState();
        static final IPhaseState<BasicPacketContext> ANIMATION = new AnimationPacketState();
        static final IPhaseState<BasicPacketContext> START_SNEAKING = new BasicPacketState();
        static final IPhaseState<BasicPacketContext> STOP_SNEAKING = new BasicPacketState();
        static final IPhaseState<BasicPacketContext> START_SPRINTING = new BasicPacketState();
        static final IPhaseState<BasicPacketContext> STOP_SPRINTING = new BasicPacketState();
        static final IPhaseState<BasicPacketContext> STOP_SLEEPING = new StopSleepingPacketState();
        public static final IPhaseState<BasicPacketContext> CLOSE_WINDOW = new CloseWindowState();
        static final IPhaseState<BasicPacketContext> UPDATE_SIGN = new UpdateSignState();
        static final IPhaseState<BasicPacketContext> RESOURCE_PACK = new ResourcePackState();
        static final IPhaseState<BasicPacketContext> STOP_RIDING_JUMP = new BasicPacketState();
        static final IPhaseState<BasicPacketContext> HANDLED_EXTERNALLY = new UnknownPacketState();
        static final IPhaseState<BasicPacketContext> START_FALL_FLYING = new BasicPacketState();
    }

    public static final class Inventory {
        static final BasicInventoryPacketState INVENTORY = new BasicInventoryPacketState();
        public static final BasicInventoryPacketState PRIMARY_INVENTORY_CLICK = new PrimaryInventoryClickState();
        public static final BasicInventoryPacketState SECONDARY_INVENTORY_CLICK = new SecondaryInventoryClickState();
        public static final BasicInventoryPacketState MIDDLE_INVENTORY_CLICK = new MiddleInventoryClickState();
        public static final BasicInventoryPacketState DROP_ITEM_OUTSIDE_WINDOW = new DropItemOutsideWindowState(
            Constants.Networking.MODE_CLICK | Constants.Networking.BUTTON_PRIMARY | Constants.Networking.BUTTON_SECONDARY | Constants.Networking.CLICK_OUTSIDE_WINDOW);
        public static final BasicInventoryPacketState DROP_ITEM_WITH_HOTKEY = new DropItemWithHotkeyState();
        public static final BasicInventoryPacketState DROP_ITEM_OUTSIDE_WINDOW_NOOP = new DropItemOutsideWindowState(
            Constants.Networking.MODE_DROP | Constants.Networking.BUTTON_PRIMARY | Constants.Networking.BUTTON_SECONDARY | Constants.Networking.CLICK_OUTSIDE_WINDOW);
        public static final BasicInventoryPacketState DROP_ITEMS = new BasicInventoryPacketState();
        static final BasicInventoryPacketState DROP_INVENTORY = new DropInventoryState();
        public static final BasicInventoryPacketState SWITCH_HOTBAR_NUMBER_PRESS = new SwitchHotbarNumberPressState();
        public static final BasicInventoryPacketState PRIMARY_INVENTORY_SHIFT_CLICK = new PrimaryInventoryShiftClick();
        public static final BasicInventoryPacketState SECONDARY_INVENTORY_SHIFT_CLICK = new SecondaryInventoryShiftClickState();
        static final BasicInventoryPacketState DOUBLE_CLICK_INVENTORY = new DoubleClickInventoryState();

        static final BasicInventoryPacketState PRIMARY_DRAG_INVENTORY_START = new DragInventoryStartState("PRIMARY_DRAG_INVENTORY_START", Constants.Networking.DRAG_MODE_PRIMARY_BUTTON);
        static final BasicInventoryPacketState SECONDARY_DRAG_INVENTORY_START = new DragInventoryStartState("SECONDARY_DRAG_INVENTORY_START", Constants.Networking.DRAG_MODE_SECONDARY_BUTTON);
        static final BasicInventoryPacketState MIDDLE_DRAG_INVENTORY_START = new DragInventoryStartState("MIDDLE_DRAG_INVENTORY_START", Constants.Networking.DRAG_MODE_MIDDLE_BUTTON);

        static final BasicInventoryPacketState PRIMARY_DRAG_INVENTORY_ADDSLOT = new DragInventoryAddSlotState("PRIMARY_DRAG_INVENTORY_ADD_SLOT", Constants.Networking.DRAG_MODE_PRIMARY_BUTTON);
        static final BasicInventoryPacketState SECONDARY_DRAG_INVENTORY_ADDSLOT = new DragInventoryAddSlotState("SECONDARY_DRAG_INVENTORY_ADD_SLOT", Constants.Networking.DRAG_MODE_SECONDARY_BUTTON);
        static final BasicInventoryPacketState MIDDLE_DRAG_INVENTORY_ADDSLOT = new DragInventoryAddSlotState("MIDDLE_DRAG_INVENTORY_ADD_SLOT", Constants.Networking.DRAG_MODE_MIDDLE_BUTTON);

        static final BasicInventoryPacketState PRIMARY_DRAG_INVENTORY_STOP = new PrimaryDragInventoryStopState();
        static final BasicInventoryPacketState SECONDARY_DRAG_INVENTORY_STOP = new SecondaryDragInventoryStopState();
        static final BasicInventoryPacketState MIDDLE_DRAG_INVENTORY_STOP = new MiddleDragInventoryStopState();

        public static final BasicInventoryPacketState SWITCH_HOTBAR_SCROLL = new SwitchHotbarScrollState();
        public static final BasicInventoryPacketState OPEN_INVENTORY = new OpenInventoryState();
        static final BasicInventoryPacketState ENCHANT_ITEM = new EnchantItemPacketState();
        public static final BasicInventoryPacketState SWAP_HAND_ITEMS = new SwapHandItemsState();

        public static final BasicInventoryPacketState PLACE_RECIPE = new PlaceRecipePacketState();

        static final ImmutableList<BasicInventoryPacketState> VALUES = ImmutableList.<BasicInventoryPacketState>builder()
                .add(INVENTORY)
                .add(PRIMARY_INVENTORY_CLICK)
                .add(SECONDARY_INVENTORY_CLICK)
                .add(MIDDLE_INVENTORY_CLICK)
                .add(DROP_ITEM_OUTSIDE_WINDOW)
                .add(DROP_ITEM_WITH_HOTKEY)
                .add(DROP_ITEM_OUTSIDE_WINDOW_NOOP)
                .add(DROP_ITEMS)
                .add(DROP_INVENTORY)
                .add(SWITCH_HOTBAR_NUMBER_PRESS)
                .add(PRIMARY_INVENTORY_SHIFT_CLICK)
                .add(SECONDARY_INVENTORY_SHIFT_CLICK)
                .add(DOUBLE_CLICK_INVENTORY)

                .add(PRIMARY_DRAG_INVENTORY_START)
                .add(SECONDARY_DRAG_INVENTORY_START)
                .add(MIDDLE_DRAG_INVENTORY_START)

                .add(PRIMARY_DRAG_INVENTORY_ADDSLOT)
                .add(SECONDARY_DRAG_INVENTORY_ADDSLOT)
                .add(MIDDLE_DRAG_INVENTORY_ADDSLOT)

                .add(PRIMARY_DRAG_INVENTORY_STOP)
                .add(SECONDARY_DRAG_INVENTORY_STOP)
                .add(MIDDLE_DRAG_INVENTORY_STOP)

                .add(SWITCH_HOTBAR_SCROLL)
                .add(OPEN_INVENTORY)
                .add(ENCHANT_ITEM)
                .build();

    }


    private final Map<Class<? extends Packet<?>>, Function<Packet<?>, IPhaseState<? extends PacketContext<?>>>> packetTranslationMap = new IdentityHashMap<>();

    // General use methods

    boolean isPacketInvalid(final Packet<?> packetIn, final EntityPlayerMP packetPlayer, final IPhaseState<? extends PacketContext<?>> packetState) {
        return ((PacketState<?>) packetState).isPacketIgnored(packetIn, packetPlayer);
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    IPhaseState<? extends PacketContext<?>> getStateForPacket(final Packet<?> packet) {
        final Function<Packet<?>, IPhaseState<? extends PacketContext<?>>> packetStateFunction = this.packetTranslationMap.get(packet.getClass());
        if (packetStateFunction != null) {
            return packetStateFunction.apply(packet);
        }
        return PacketPhase.General.UNKNOWN;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PhaseContext<?> populateContext(final Packet<?> packet, final EntityPlayerMP entityPlayerMP, final IPhaseState<?> state, final PhaseContext<?> context) {
        checkNotNull(packet, "Packet cannot be null!");
        checkArgument(!context.isComplete(), "PhaseContext cannot be marked as completed!");
        ((PacketState) state).populateContext(entityPlayerMP, packet, (PacketContext) context);
        return context;
    }

    // Inventory packet specific methods

    private static BasicInventoryPacketState fromWindowPacket(final CPacketClickWindow windowPacket) {
        final int mode = 0x01 << 9 << windowPacket.func_186993_f().ordinal();
        final int packed = windowPacket.func_149543_e();
        final int unpacked = mode == Constants.Networking.MODE_DRAG ? (0x01 << 6 << (packed >> 2 & 3)) | (0x01 << 3 << (packed & 3)) : (0x01 << (packed & 3));

        final BasicInventoryPacketState inventory = fromState(clickType(windowPacket.func_149544_d()) | mode | unpacked);
        if (inventory == PacketPhase.Inventory.INVENTORY) {
            SpongeImpl.getLogger().warn(String.format("Unable to find InventoryPacketState handler for click window packet: %s", windowPacket));
        }
        return inventory;
    }


    private static int clickType(final int slotId) {
        return (slotId == Constants.Networking.MAGIC_CLICK_OUTSIDE_SURVIVAL
                || slotId == Constants.Networking.MAGIC_CLICK_OUTSIDE_CREATIVE)
               ? Constants.Networking.CLICK_OUTSIDE_WINDOW : Constants.Networking.CLICK_INSIDE_WINDOW;
    }


    private static BasicInventoryPacketState fromState(final int state) {
        for (final BasicInventoryPacketState inventory : PacketPhase.Inventory.VALUES) {
            if (inventory.matches(state)) {
                return inventory;
            }
        }
        return PacketPhase.Inventory.INVENTORY;
    }

    // General methods

    public static PacketPhase getInstance() {
        return PacketPhase.Holder.INSTANCE;
    }

    @SuppressWarnings("WeakerAccess")
    PacketPhase() {
        setupPacketToStateMapping();
    }

    private static final class Holder {
        static final PacketPhase INSTANCE = new PacketPhase();
    }


    private void setupPacketToStateMapping() {
        this.packetTranslationMap.put(CPacketKeepAlive.class, packet -> PacketPhase.General.IGNORED);
        this.packetTranslationMap.put(CPacketChatMessage.class, packet -> PacketPhase.General.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketUseEntity.class, packet -> {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
            final CPacketUseEntity.Action action = useEntityPacket.func_149565_c();
            if (action == CPacketUseEntity.Action.INTERACT) {
                return PacketPhase.General.INTERACT_ENTITY;
            } else if (action == CPacketUseEntity.Action.ATTACK) {
                return PacketPhase.General.ATTACK_ENTITY;
            } else if (action == CPacketUseEntity.Action.INTERACT_AT) {
                return PacketPhase.General.INTERACT_AT_ENTITY;
            } else {
                return PacketPhase.General.INVALID;
            }
        });
        this.packetTranslationMap.put(CPacketPlayer.class, packet -> PacketPhase.General.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.Position.class, packet -> PacketPhase.General.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.Rotation.class, packet -> PacketPhase.General.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.PositionRotation.class, packet -> PacketPhase.General.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayerDigging.class, packet -> {
            final CPacketPlayerDigging playerDigging = (CPacketPlayerDigging) packet;
            final CPacketPlayerDigging.Action action = playerDigging.func_180762_c();
            final IPhaseState<? extends PacketContext<?>> state = INTERACTION_ACTION_MAPPINGS.get(action);
            return state == null ? PacketPhase.General.UNKNOWN : state;
        });
        this.packetTranslationMap.put(CPacketPlayerTryUseItemOnBlock.class, packet -> {
            // Note that CPacketPlayerTryUseItem is swapped with CPacketPlayerBlockPlacement
            final CPacketPlayerTryUseItemOnBlock blockPlace = (CPacketPlayerTryUseItemOnBlock) packet;
            final BlockPos blockPos = blockPlace.func_187023_a();
            final EnumFacing front = blockPlace.func_187024_b();
            final MinecraftServer server = SpongeImpl.getServer();
            if (blockPos.func_177956_o() < server.func_71207_Z() - 1 || front != EnumFacing.UP && blockPos.func_177956_o() < server.func_71207_Z()) {
                return PacketPhase.General.PLACE_BLOCK;
            }
            return PacketPhase.General.INVALID;
        });
        this.packetTranslationMap.put(CPacketPlayerTryUseItem.class, packet -> PacketPhase.General.USE_ITEM);
        this.packetTranslationMap.put(CPacketHeldItemChange.class, packet -> PacketPhase.Inventory.SWITCH_HOTBAR_SCROLL);
        this.packetTranslationMap.put(CPacketAnimation.class, packet -> PacketPhase.General.ANIMATION);
        this.packetTranslationMap.put(CPacketEntityAction.class, packet -> {
            final CPacketEntityAction playerAction = (CPacketEntityAction) packet;
            final CPacketEntityAction.Action action = playerAction.func_180764_b();
            return PLAYER_ACTION_MAPPINGS.get(action);
        });
        this.packetTranslationMap.put(CPacketInput.class, packet -> PacketPhase.General.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketCloseWindow.class, packet -> PacketPhase.General.CLOSE_WINDOW);
        this.packetTranslationMap.put(CPacketClickWindow.class, packet -> fromWindowPacket((CPacketClickWindow) packet));
        this.packetTranslationMap.put(CPacketConfirmTransaction.class, packet -> PacketPhase.General.UNKNOWN);
        this.packetTranslationMap.put(CPacketCreativeInventoryAction.class, packet -> PacketPhase.General.CREATIVE_INVENTORY);
        this.packetTranslationMap.put(CPacketEnchantItem.class, packet -> PacketPhase.Inventory.ENCHANT_ITEM);
        this.packetTranslationMap.put(CPacketUpdateSign.class, packet -> PacketPhase.General.UPDATE_SIGN);
        this.packetTranslationMap.put(CPacketPlayerAbilities.class, packet -> PacketPhase.General.IGNORED);
        this.packetTranslationMap.put(CPacketTabComplete.class, packet -> PacketPhase.General.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketClientStatus.class, packet -> {
            final CPacketClientStatus clientStatus = (CPacketClientStatus) packet;
            final CPacketClientStatus.State status = clientStatus.func_149435_c();
            if (status == CPacketClientStatus.State.PERFORM_RESPAWN) {
                return PacketPhase.General.REQUEST_RESPAWN;
            }
            return PacketPhase.General.IGNORED;
        });
        this.packetTranslationMap.put(CPacketCustomPayload.class, packet -> PacketPhase.General.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketSpectate.class, packet -> PacketPhase.General.IGNORED);
        this.packetTranslationMap.put(CPacketResourcePackStatus.class, packet -> PacketPhase.General.RESOURCE_PACK);
        this.packetTranslationMap.put(CPacketPlaceRecipe.class, packet -> PacketPhase.Inventory.PLACE_RECIPE);
    }

    private static final ImmutableMap<CPacketEntityAction.Action, IPhaseState<? extends PacketContext<?>>> PLAYER_ACTION_MAPPINGS = ImmutableMap.<CPacketEntityAction.Action, IPhaseState<? extends PacketContext<?>>>builder()
            .put(CPacketEntityAction.Action.START_SNEAKING, PacketPhase.General.START_SNEAKING)
            .put(CPacketEntityAction.Action.STOP_SNEAKING, PacketPhase.General.STOP_SNEAKING)
            .put(CPacketEntityAction.Action.STOP_SLEEPING, PacketPhase.General.STOP_SLEEPING)
            .put(CPacketEntityAction.Action.START_SPRINTING, PacketPhase.General.START_SPRINTING)
            .put(CPacketEntityAction.Action.STOP_SPRINTING, PacketPhase.General.STOP_SPRINTING)
            .put(CPacketEntityAction.Action.START_RIDING_JUMP, PacketPhase.General.START_RIDING_JUMP)
            .put(CPacketEntityAction.Action.STOP_RIDING_JUMP, PacketPhase.General.STOP_RIDING_JUMP)
            .put(CPacketEntityAction.Action.OPEN_INVENTORY, PacketPhase.Inventory.OPEN_INVENTORY)
            .put(CPacketEntityAction.Action.START_FALL_FLYING, PacketPhase.General.START_FALL_FLYING)
            .build();
    private static final ImmutableMap<CPacketPlayerDigging.Action, IPhaseState<? extends PacketContext<?>>> INTERACTION_ACTION_MAPPINGS = ImmutableMap.<CPacketPlayerDigging.Action, IPhaseState<? extends PacketContext<?>>>builder()
            .put(CPacketPlayerDigging.Action.DROP_ITEM, PacketPhase.Inventory.DROP_ITEM_WITH_HOTKEY)
            .put(CPacketPlayerDigging.Action.DROP_ALL_ITEMS, PacketPhase.Inventory.DROP_ITEM_WITH_HOTKEY)
            .put(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, PacketPhase.General.INTERACTION)
            .put(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, PacketPhase.General.INTERACTION)
            .put(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, PacketPhase.General.INTERACTION)
            .put(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, PacketPhase.General.INTERACTION)
            .put(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, PacketPhase.Inventory.SWAP_HAND_ITEMS)
            .build();

}
