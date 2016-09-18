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

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientSettings;
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
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.interfaces.IMixinChunk;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

public final class PacketPhase extends TrackingPhase {

    // Inventory static fields
    final static int MAGIC_CLICK_OUTSIDE_SURVIVAL = -999;
    final static int MAGIC_CLICK_OUTSIDE_CREATIVE = -1;

    // Flag masks
    final static int MASK_NONE              = 0x00000;
    final static int MASK_OUTSIDE           = 0x30000;
    final static int MASK_MODE              = 0x0FE00;
    final static int MASK_DRAGDATA          = 0x001F8;
    final static int MASK_BUTTON            = 0x00007;

    // Mask presets
    final static int MASK_ALL               = MASK_OUTSIDE | MASK_MODE | MASK_BUTTON | MASK_DRAGDATA;
    final static int MASK_NORMAL            = MASK_MODE | MASK_BUTTON | MASK_DRAGDATA;
    final static int MASK_DRAG              = MASK_OUTSIDE | MASK_NORMAL;

    // Click location semaphore flags
    final static int CLICK_INSIDE_WINDOW    = 0x01 << 16 << 0;
    final static int CLICK_OUTSIDE_WINDOW   = 0x01 << 16 << 1;
    final static int CLICK_ANYWHERE         = CLICK_INSIDE_WINDOW | CLICK_OUTSIDE_WINDOW;

    // Modes flags
    final static int MODE_CLICK             = 0x01 << 9 << ClickType.PICKUP.ordinal();
    final static int MODE_SHIFT_CLICK       = 0x01 << 9 << ClickType.QUICK_MOVE.ordinal();
    final static int MODE_HOTBAR            = 0x01 << 9 << ClickType.SWAP.ordinal();
    final static int MODE_PICKBLOCK         = 0x01 << 9 << ClickType.CLONE.ordinal();
    final static int MODE_DROP              = 0x01 << 9 << ClickType.THROW.ordinal();
    final static int MODE_DRAG              = 0x01 << 9 << ClickType.QUICK_CRAFT.ordinal();
    final static int MODE_DOUBLE_CLICK      = 0x01 << 9 << ClickType.PICKUP_ALL.ordinal();

    // Drag mode flags, bitmasked from button and only set if MODE_DRAG
    final static int DRAG_MODE_SPLIT_ITEMS  = 0x01 << 6 << 0;
    final static int DRAG_MODE_ONE_ITEM     = 0x01 << 6 << 1;
    final static int DRAG_MODE_ANY          = DRAG_MODE_SPLIT_ITEMS | DRAG_MODE_ONE_ITEM;

    // Drag status flags, bitmasked from button and only set if MODE_DRAG
    final static int DRAG_STATUS_STARTED    = 0x01 << 3 << 0;
    final static int DRAG_STATUS_ADD_SLOT   = 0x01 << 3 << 1;
    final static int DRAG_STATUS_STOPPED    = 0x01 << 3 << 2;

    // Buttons flags, only set if *not* MODE_DRAG
    final static int BUTTON_PRIMARY         = 0x01 << 0 << 0;
    final static int BUTTON_SECONDARY       = 0x01 << 0 << 1;
    final static int BUTTON_MIDDLE          = 0x01 << 0 << 2;


    private final Map<Class<? extends Packet<?>>, Function<Packet<?>, IPacketState>> packetTranslationMap = new IdentityHashMap<>();
    private final Map<Class<? extends Packet<?>>, PacketFunction> packetUnwindMap = new IdentityHashMap<>();

    // General use methods

    public boolean isPacketInvalid(Packet<?> packetIn, EntityPlayerMP packetPlayer, IPacketState packetState) {
        return packetState.isPacketIgnored(packetIn, packetPlayer);
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    public IPacketState getStateForPacket(Packet<?> packet) {
        final Function<Packet<?>, IPacketState> packetStateFunction = this.packetTranslationMap.get(packet.getClass());
        if (packetStateFunction != null) {
            return packetStateFunction.apply(packet);
        }
        return GeneralPacketState.UNKNOWN;
    }

    public PhaseContext populateContext(Packet<?> packet, EntityPlayerMP entityPlayerMP, IPhaseState state, PhaseContext context) {
        checkNotNull(packet, "Packet cannot be null!");
        checkArgument(!context.isComplete(), "PhaseContext cannot be marked as completed!");
        ((IPacketState) state).populateContext(entityPlayerMP, packet, context);
        return context;
    }


    // TrackingPhase specific methods overridden for state specific handling

    @Override
    public boolean populateCauseForNotifyNeighborEvent(IPhaseState state, PhaseContext context, Cause.Builder builder, CauseTracker causeTracker,
            IMixinChunk mixinChunk, BlockPos pos) {
        if (!super.populateCauseForNotifyNeighborEvent(state, context, builder, causeTracker, mixinChunk, pos)) {
            final Player player = context.getSource(Player.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Processing a Player PAcket, expecting a player, but had none!", context));
            builder.named(NamedCause.notifier(player));
        }
        return true;
    }

    @Override
    public void associateNeighborStateNotifier(IPhaseState state, PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        final Player player = context.getSource(Player.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be tracking a player, but not!", context));
        ((IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos)).setBlockNotifier(notifyPos, player.getUniqueId());

    }

    @Override
    public boolean doesCaptureEntityDrops(IPhaseState currentState) {
        return ((IPacketState) currentState).doesCaptureEntityDrops();
    }

    @Override
    public boolean alreadyCapturingItemSpawns(IPhaseState currentState) {
        return currentState == GeneralPacketState.INTERACTION;
    }

    @Override
    public boolean ignoresItemPreMerging(IPhaseState currentState) {
        return ((IPacketState) currentState).ignoresItemPreMerges();
    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return ((IPacketState) phaseState).shouldCaptureEntity()
               ? context.getCapturedEntities().add(entity)
               : ((IPacketState) phaseState).spawnEntity(context, entity, chunkX, chunkZ);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState phaseState, PhaseContext phaseContext) {
        if (phaseState == GeneralPacketState.INVALID) { // Invalid doesn't capture any packets.
            return;
        }
        final Packet<?> packetIn = phaseContext.firstNamed(InternalNamedCauses.Packet.CAPTURED_PACKET, Packet.class).get();
        final EntityPlayerMP player = phaseContext.getSource(EntityPlayerMP.class).get();
        final Class<? extends Packet<?>> packetInClass = (Class<? extends Packet<?>>) packetIn.getClass();

        final PacketFunction unwindFunction = this.packetUnwindMap.get(packetInClass);
        checkArgument(phaseState instanceof IPacketState, "PhaseState passed in is not an instance of IPacketState! Got %s", phaseState);
        if (unwindFunction != null) {
            unwindFunction.unwind(packetIn, (IPacketState) phaseState, player, phaseContext);
        } else {
            PacketFunction.UNKONWN_PACKET.unwind(packetIn, (IPacketState) phaseState, player, phaseContext);
        }
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return currentState instanceof GeneralPacketState;
    }

    @Override
    public boolean requiresPost(IPhaseState state) {
        return state != GeneralPacketState.INVALID;
    }

    @Override
    protected void processPostEntitySpawns(CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext phaseContext,
            ArrayList<Entity> entities) {
        ((IPacketState) unwindingState).postSpawnEntities(causeTracker, phaseContext, entities);
    }

    public static PacketPhase getInstance() {
        return Holder.INSTANCE;
    }

    private PacketPhase() {
        setupPacketToStateMapping();
        setupPacketToUnwindMapping();
    }

    private static final class Holder {
        static final PacketPhase INSTANCE = new PacketPhase();
    }

    public void setupPacketToUnwindMapping() {
        this.packetUnwindMap.put(CPacketKeepAlive.class, PacketFunction.IGNORED);
        this.packetUnwindMap.put(CPacketChatMessage.class, PacketFunction.HANDLED_EXTERNALLY);
        this.packetUnwindMap.put(CPacketUseEntity.class, PacketFunction.USE_ENTITY);
        this.packetUnwindMap.put(CPacketPlayer.class, PacketFunction.MOVEMENT);
        this.packetUnwindMap.put(CPacketPlayer.Position.class, PacketFunction.MOVEMENT); // We only care when the player is moving blocks because of falling states
        this.packetUnwindMap.put(CPacketPlayer.Rotation.class, PacketFunction.MOVEMENT);
        this.packetUnwindMap.put(CPacketPlayer.PositionRotation.class, PacketFunction.MOVEMENT); // We only care when the player is moving blocks because of falling states
        this.packetUnwindMap.put(CPacketPlayerDigging.class, PacketFunction.ACTION);
        this.packetUnwindMap.put(CPacketPlayerTryUseItem.class, PacketFunction.USE_ITEM);
        this.packetUnwindMap.put(CPacketPlayerTryUseItemOnBlock.class, PacketFunction.PLACE_BLOCK);
        this.packetUnwindMap.put(CPacketHeldItemChange.class, PacketFunction.HELD_ITEM_CHANGE);
        this.packetUnwindMap.put(CPacketAnimation.class, PacketFunction.IGNORED);
        this.packetUnwindMap.put(CPacketEntityAction.class, PacketFunction.IGNORED);
        this.packetUnwindMap.put(CPacketInput.class, PacketFunction.IGNORED);
        this.packetUnwindMap.put(CPacketCloseWindow.class, PacketFunction.CLOSE_WINDOW);
        this.packetUnwindMap.put(CPacketClickWindow.class, PacketFunction.INVENTORY);
        this.packetUnwindMap.put(CPacketConfirmTransaction.class, PacketFunction.IGNORED);
        this.packetUnwindMap.put(CPacketCreativeInventoryAction.class, PacketFunction.CREATIVE);
        this.packetUnwindMap.put(CPacketEnchantItem.class, PacketFunction.ENCHANTMENT);
        this.packetUnwindMap.put(CPacketUpdateSign.class, PacketFunction.HANDLED_EXTERNALLY);
        this.packetUnwindMap.put(CPacketPlayerAbilities.class, PacketFunction.IGNORED);
        this.packetUnwindMap.put(CPacketTabComplete.class, PacketFunction.HANDLED_EXTERNALLY);
        this.packetUnwindMap.put(CPacketClientSettings.class, PacketFunction.CLIENT_SETTINGS);
        this.packetUnwindMap.put(CPacketClientStatus.class, PacketFunction.CLIENT_STATUS);
        this.packetUnwindMap.put(CPacketCustomPayload.class, PacketFunction.HANDLED_EXTERNALLY);
        this.packetUnwindMap.put(CPacketSpectate.class, PacketFunction.IGNORED);
        this.packetUnwindMap.put(CPacketResourcePackStatus.class, PacketFunction.RESOURCE_PACKET);
    }


    public void setupPacketToStateMapping() {
        this.packetTranslationMap.put(CPacketKeepAlive.class, packet -> GeneralPacketState.IGNORED);
        this.packetTranslationMap.put(CPacketChatMessage.class, packet -> GeneralPacketState.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketUseEntity.class, packet -> {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
            final CPacketUseEntity.Action action = useEntityPacket.getAction();
            if (action == CPacketUseEntity.Action.INTERACT) {
                return GeneralPacketState.INTERACT_ENTITY;
            } else if (action == CPacketUseEntity.Action.ATTACK) {
                return GeneralPacketState.ATTACK_ENTITY;
            } else if (action == CPacketUseEntity.Action.INTERACT_AT) {
                return GeneralPacketState.INTERACT_AT_ENTITY;
            } else {
                return GeneralPacketState.INVALID;
            }
        });
        this.packetTranslationMap.put(CPacketPlayer.class, packet -> GeneralPacketState.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.Position.class, packet -> GeneralPacketState.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.Rotation.class, packet -> GeneralPacketState.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.PositionRotation.class, packet -> GeneralPacketState.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayerDigging.class, packet -> {
            final CPacketPlayerDigging playerDigging = (CPacketPlayerDigging) packet;
            final CPacketPlayerDigging.Action action = playerDigging.getAction();
            final IPacketState state = INTERACTION_ACTION_MAPPINGS.get(action);
            return state == null ? GeneralPacketState.UNKNOWN : state;
        });
        this.packetTranslationMap.put(CPacketPlayerTryUseItemOnBlock.class, packet -> {
            // Note that CPacketPlayerTryUseItem is swapped with CPacketPlayerBlockPlacement
            final CPacketPlayerTryUseItemOnBlock blockPlace = (CPacketPlayerTryUseItemOnBlock) packet;
            final BlockPos blockPos = blockPlace.getPos();
            final EnumFacing front = blockPlace.getDirection();
            final MinecraftServer server = SpongeImpl.getServer();
            if (blockPos.getY() < server.getBuildLimit() - 1 || front != EnumFacing.UP && blockPos.getY() < server.getBuildLimit()) {
                return GeneralPacketState.PLACE_BLOCK;
            } else {
                return GeneralPacketState.INVALID;
            }
        });
        this.packetTranslationMap.put(CPacketPlayerTryUseItem.class, packet -> GeneralPacketState.USE_ITEM);
        this.packetTranslationMap.put(CPacketHeldItemChange.class, packet -> InventoryPacketState.SWITCH_HOTBAR_SCROLL);
        this.packetTranslationMap.put(CPacketAnimation.class, packet -> GeneralPacketState.ANIMATION);
        this.packetTranslationMap.put(CPacketEntityAction.class, packet -> {
            final CPacketEntityAction playerAction = (CPacketEntityAction) packet;
            final CPacketEntityAction.Action action = playerAction.getAction();
            return PLAYER_ACTION_MAPPINGS.get(action);
        });
        this.packetTranslationMap.put(CPacketInput.class, packet -> GeneralPacketState.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketCloseWindow.class, packet -> GeneralPacketState.CLOSE_WINDOW);
        this.packetTranslationMap.put(CPacketClickWindow.class, packet -> InventoryPacketState.fromWindowPacket((CPacketClickWindow) packet));
        this.packetTranslationMap.put(CPacketConfirmTransaction.class, packet -> GeneralPacketState.UNKNOWN);
        this.packetTranslationMap.put(CPacketCreativeInventoryAction.class, packet -> GeneralPacketState.CREATIVE_INVENTORY);
        this.packetTranslationMap.put(CPacketEnchantItem.class, packet -> InventoryPacketState.ENCHANT_ITEM);
        this.packetTranslationMap.put(CPacketUpdateSign.class, packet -> GeneralPacketState.UPDATE_SIGN);
        this.packetTranslationMap.put(CPacketPlayerAbilities.class, packet -> GeneralPacketState.IGNORED);
        this.packetTranslationMap.put(CPacketTabComplete.class, packet -> GeneralPacketState.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketClientSettings.class, packet -> GeneralPacketState.CLIENT_SETTINGS);
        this.packetTranslationMap.put(CPacketClientStatus.class, packet -> {
            final CPacketClientStatus clientStatus = (CPacketClientStatus) packet;
            final CPacketClientStatus.State status = clientStatus.getStatus();
            if ( status == CPacketClientStatus.State.OPEN_INVENTORY_ACHIEVEMENT) {
                return InventoryPacketState.OPEN_INVENTORY;
            } else if ( status == CPacketClientStatus.State.PERFORM_RESPAWN) {
                return GeneralPacketState.REQUEST_RESPAWN;
            } else {
                return GeneralPacketState.IGNORED;
            }
        });
        this.packetTranslationMap.put(CPacketCustomPayload.class, packet -> GeneralPacketState.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketSpectate.class, packet -> GeneralPacketState.IGNORED);
        this.packetTranslationMap.put(CPacketResourcePackStatus.class, packet -> GeneralPacketState.RESOURCE_PACK);
    }

    public static final ImmutableMap<CPacketEntityAction.Action, IPacketState> PLAYER_ACTION_MAPPINGS = ImmutableMap.<CPacketEntityAction.Action, IPacketState>builder()
            .put(CPacketEntityAction.Action.START_SNEAKING, GeneralPacketState.START_SNEAKING)
            .put(CPacketEntityAction.Action.STOP_SNEAKING, GeneralPacketState.STOP_SNEAKING)
            .put(CPacketEntityAction.Action.STOP_SLEEPING, GeneralPacketState.STOP_SLEEPING)
            .put(CPacketEntityAction.Action.START_SPRINTING, GeneralPacketState.START_SPRINTING)
            .put(CPacketEntityAction.Action.STOP_SPRINTING, GeneralPacketState.STOP_SPRINTING)
            .put(CPacketEntityAction.Action.START_RIDING_JUMP, GeneralPacketState.START_RIDING_JUMP)
            .put(CPacketEntityAction.Action.STOP_RIDING_JUMP, GeneralPacketState.STOP_RIDING_JUMP)
            .put(CPacketEntityAction.Action.OPEN_INVENTORY, InventoryPacketState.OPEN_INVENTORY)
            .put(CPacketEntityAction.Action.START_FALL_FLYING, GeneralPacketState.START_FALL_FLYING)
            .build();
    public static final ImmutableMap<CPacketPlayerDigging.Action, IPacketState> INTERACTION_ACTION_MAPPINGS = ImmutableMap.<CPacketPlayerDigging.Action, IPacketState>builder()
            .put(CPacketPlayerDigging.Action.DROP_ITEM, InventoryPacketState.DROP_ITEM)
            .put(CPacketPlayerDigging.Action.DROP_ALL_ITEMS, InventoryPacketState.DROP_ITEM)
            .put(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, GeneralPacketState.INTERACTION)
            .put(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, GeneralPacketState.INTERACTION)
            .put(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, GeneralPacketState.INTERACTION)
            .put(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, GeneralPacketState.INTERACTION)
            .put(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, GeneralPacketState.SWAP_HAND_ITEMS)
            .build();
}
