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
package org.spongepowered.common.event;

import static org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil.handleCustomCursor;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase.State;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class SpongeCommonEventFactory {

    public static boolean convertingMapFormat = false;
    // Set if the player's held item changes during InteractBlockEvent.Secondary
    public static boolean playerInteractItemChanged = false;
    // Set if any of the events fired during interaction with a block (open
    // inventory or interact block) were cancelled
    public static boolean interactBlockEventCancelled = false;
    // Dummy ChangeBlockEvent.Pre
    public static ChangeBlockEvent.Pre DUMMY_BLOCK_PRE_EVENT = null;

    // For animation packet
    public static int lastAnimationPacketTick = 0;
    public static int lastSecondaryPacketTick = 0;
    public static int lastPrimaryPacketTick = 0;
    public static WeakReference<EntityPlayerMP> lastAnimationPlayer;

    public static boolean callPlayerChangeInventoryPickupEvent(EntityPlayer player, EntityItem itemToPickup, int pickupDelay, UUID creator) {
        ItemStack itemStack = itemToPickup.getItem();
        int slotId = ((IMixinInventoryPlayer) player.inventory).getFirstAvailableSlot(itemStack);
        Slot slot = null;
        if (slotId != -1) {
            if (slotId < InventoryPlayer.getHotbarSize()) {
                slot = player.inventoryContainer.getSlot(slotId + player.inventory.mainInventory.size());
            } else {
                slot = player.inventoryContainer.getSlot(slotId);
            }
        }

        if (pickupDelay <= 0 && slot != null) {
            ItemStackSnapshot sourceSnapshot = slot.getStack().isEmpty() ? ItemStackSnapshot.NONE
                    : ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot();
            ItemStackSnapshot targetSnapshot;
            if (sourceSnapshot != ItemStackSnapshot.NONE) {
                // combined slot
                targetSnapshot =
                        org.spongepowered.api.item.inventory.ItemStack.builder().from((org.spongepowered.api.item.inventory.ItemStack) itemStack)
                                .quantity(itemStack.getCount() + slot.getStack().getCount()).build().createSnapshot();
            } else {
                // empty slot
                targetSnapshot = ((org.spongepowered.api.item.inventory.ItemStack) itemStack).createSnapshot();
            }

            ((SpongeItemStackSnapshot) targetSnapshot).setCreator(creator);
            SlotTransaction slotTransaction =
                    new SlotTransaction(new SlotAdapter(slot), sourceSnapshot, targetSnapshot);
            ImmutableList<SlotTransaction> transactions =
                    new ImmutableList.Builder<SlotTransaction>().add(slotTransaction).build();
            ChangeInventoryEvent.Pickup event = SpongeEventFactory.createChangeInventoryEventPickup(Sponge.getCauseStackManager().getCurrentCause(),
                    (Item) itemToPickup, (Inventory) player.inventoryContainer, transactions);
            if (SpongeImpl.postEvent(event)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static CollideEntityEvent callCollideEntityEvent(net.minecraft.world.World world, @Nullable net.minecraft.entity.Entity sourceEntity,
            List<net.minecraft.entity.Entity> entities) {

        CauseTracker causeTracker = CauseTracker.getInstance();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (sourceEntity != null) {
                Sponge.getCauseStackManager().pushCause(sourceEntity);
            } else {
                PhaseContext context = causeTracker.getCurrentContext();

                final Optional<LocatableBlock> currentTickingBlock = context.getSource(LocatableBlock.class);
                if (currentTickingBlock.isPresent()) {
                    Sponge.getCauseStackManager().pushCause(currentTickingBlock.get());
                } else {
                    final Optional<TileEntity> currentTickingTileEntity = context.getSource(TileEntity.class);
                    if (currentTickingTileEntity.isPresent()) {
                        Sponge.getCauseStackManager().pushCause(currentTickingTileEntity.get());
                    } else {
                        final Optional<Entity> currentTickingEntity = context.getSource(Entity.class);
                        if (currentTickingEntity.isPresent()) {
                            Sponge.getCauseStackManager().pushCause(currentTickingEntity.get());
                        } else {
                            return null;
                        }
                    }
                }
            }
            causeTracker.getCurrentPhaseData().context.addNotifierAndOwnerToCauseStack();

            List<Entity> spEntities = (List<Entity>) (List<?>) entities;
            CollideEntityEvent event =
                    SpongeEventFactory.createCollideEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), spEntities);
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, BlockPos pos) {
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            return callChangeBlockEventPre(worldIn, ImmutableList.of(new Location<>((World) worldIn, pos.getX(), pos.getY(), pos.getZ())), null);
        }
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, BlockPos pos, Object source) {
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            return callChangeBlockEventPre(worldIn, ImmutableList.of(new Location<>((World) worldIn, pos.getX(), pos.getY(), pos.getZ())), source);
        }
    }

    /**
     * Processes pre block event data then fires event.
     * 
     * Note: This method does not create a stack frame.
     * Any caller to this method should have a frame created to
     * avoid stack corruption.
     * 
     * @param worldIn The world
     * @param locations The locations affected
     * @param source The source of event
     * @return The event
     */
    private static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, ImmutableList<Location<World>> locations, @Nullable Object source) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final PhaseData data = causeTracker.getCurrentPhaseData();
        if (source == null) {
            source = data.context.getSource(LocatableBlock.class).orElse(null);
            if (source == null) {
                // safety measure, return a dummy event
                if (DUMMY_BLOCK_PRE_EVENT == null) {
                    DUMMY_BLOCK_PRE_EVENT = SpongeEventFactory.createChangeBlockEventPre(Sponge.getCauseStackManager().getCurrentCause(), ImmutableList.of());
                }
                return DUMMY_BLOCK_PRE_EVENT;
            }
        }

        EntityPlayer player = null;
        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);
        // handle FakePlayer
        boolean isFake = false;
        if (source instanceof EntityPlayer) {
            player = (EntityPlayer) source;
            if (SpongeImplHooks.isFakePlayer(player)) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
                Sponge.getCauseStackManager().pushCause(owner);
                isFake = true;
            }
        }

        if (!isFake) {
            Sponge.getCauseStackManager().pushCause(source);
        }

        if(owner != null) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner);
        }
        if (notifier != null) {
            Optional<Player> oplayer = data.context.getSource(Player.class);
            if (oplayer.isPresent()) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, oplayer.get());
            } else {
                Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier);
            }
        }

        ChangeBlockEvent.Pre event =
                SpongeEventFactory.createChangeBlockEventPre(Sponge.getCauseStackManager().getCurrentCause(), locations);
        SpongeImpl.postEvent(event);
        return event;
    }

    public static ChangeBlockEvent.Modify callChangeBlockEventModifyLiquidMix(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, @Nullable Object source) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final PhaseData data = causeTracker.getCurrentPhaseData();

        BlockState fromState = BlockUtil.fromNative(worldIn.getBlockState(pos));
        BlockState toState = BlockUtil.fromNative(state);
        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);

        if (source == null) {
            // If source is null the source is the block itself
            source = LocatableBlock.builder().state(fromState).world(((World) worldIn)).position(pos.getX(), pos.getY(), pos.getZ()).build();
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(source);
            Sponge.getCauseStackManager().addContext(EventContextKeys.LIQUID_MIX, (World) worldIn);
            if (owner != null) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner);
            }
            if (notifier != null) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier);
            }

            WorldProperties world = ((World) worldIn).getProperties();
            Vector3i position = new Vector3i(pos.getX(), pos.getY(), pos.getZ());

            Transaction<BlockSnapshot> transaction = new Transaction<>(BlockSnapshot.builder().blockState(fromState).world(world).position(position).build(),
                                                                       BlockSnapshot.builder().blockState(toState).world(world).position(position).build());
            ChangeBlockEvent.Modify event = SpongeEventFactory.createChangeBlockEventModify(Sponge.getCauseStackManager().getCurrentCause(),
                    Collections.singletonList(transaction));

            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent.Break callChangeBlockEventModifyLiquidBreak(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int flags) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final PhaseData data = causeTracker.getCurrentPhaseData();

        BlockState fromState = BlockUtil.fromNative(worldIn.getBlockState(pos));
        BlockState toState = BlockUtil.fromNative(state);
        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);
        Object source = data.context.getSource(LocatableBlock.class).orElse(null);
        if (source == null) {
            source = worldIn; // Fallback
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(source);
            Sponge.getCauseStackManager().addContext(EventContextKeys.LIQUID_MIX, (World) worldIn);
            if (owner != null) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner);
            }
            if (notifier != null) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier);
            }
            WorldProperties world = ((World) worldIn).getProperties();
            Vector3i position = new Vector3i(pos.getX(), pos.getY(), pos.getZ());

            Transaction<BlockSnapshot> transaction = new Transaction<>(BlockSnapshot.builder().blockState(fromState).world(world).position(position).build(),
                    BlockSnapshot.builder().blockState(toState).world(world).position(position).build());
            ChangeBlockEvent.Break event = SpongeEventFactory.createChangeBlockEventBreak(Sponge.getCauseStackManager().getCurrentCause(),
                    Collections.singletonList(transaction));

            SpongeImpl.postEvent(event);
            return event;
        }
    }


    /**
     * This simulates the blocks a piston moves and calls the event for saner
     * debugging.
     *
     * @return if the event was cancelled
     */
    public static boolean handlePistonEvent(IMixinWorldServer world, WorldServer.ServerBlockEventList list, Object obj, BlockPos pos, Block blockIn,
            int eventId, int eventParam) {
        boolean extending = (eventId == 0);
        final IBlockState blockstate = ((net.minecraft.world.World) world).getBlockState(pos);
        EnumFacing direction = blockstate.getValue(BlockDirectional.FACING);
        final LocatableBlock locatable = LocatableBlock.builder()
                .location(new Location<>((World) world, pos.getX(), pos.getY(), pos.getZ()))
                .state((BlockState) blockstate)
                .build();

        // Sets toss out duplicate values (even though there shouldn't be any)
        HashSet<Location<org.spongepowered.api.world.World>> locations = new HashSet<>();
        locations.add(new Location<>((World) world, pos.getX(), pos.getY(), pos.getZ()));

        BlockPistonStructureHelper movedBlocks = new BlockPistonStructureHelper((WorldServer) world, pos, direction, extending);
        movedBlocks.canMove(); // calculates blocks to be moved

        Stream.concat(movedBlocks.getBlocksToMove().stream(), movedBlocks.getBlocksToDestroy().stream())
                .map(block -> new Location<>((World) world, block.getX(), block.getY(), block.getZ()))
                .collect(Collectors.toCollection(() -> locations)); // SUPER
                                                                    // efficient
                                                                    // code!

        // If the piston is extending and there are no blocks to destroy, add the offset location for protection purposes
        if (extending && movedBlocks.getBlocksToDestroy().isEmpty()) {
            final List<BlockPos> movedPositions = movedBlocks.getBlocksToMove();
            BlockPos offsetPos;
            // If there are no blocks to move, add the offset of piston
            if (movedPositions.isEmpty()) {
                offsetPos = pos.offset(direction);
            } else {
                // Add the offset of last block set to move
                offsetPos = movedPositions.get(movedPositions.size() - 1).offset(direction);
            }
            locations.add(new Location<>((World) world, offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()));
        }

        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (extending) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.PISTON_EXTEND, world.asSpongeWorld());
            } else {
                Sponge.getCauseStackManager().addContext(EventContextKeys.PISTON_RETRACT, world.asSpongeWorld());
            }
            return SpongeCommonEventFactory.callChangeBlockEventPre(world, ImmutableList.copyOf(locations), locatable)
                .isCancelled();
        }
    }

    @SuppressWarnings("rawtypes")
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(World world, BlockPos sourcePos, EnumSet notifiedSides) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final PhaseData peek = causeTracker.getCurrentPhaseData();
        final PhaseContext context = peek.context;
        // Don't fire notify events during world gen or while restoring
        if (peek.state.getPhase().isWorldGeneration(peek.state) || peek.state == State.RESTORING_BLOCKS) {
            return null;
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final BlockState blockstate = (BlockState) ((net.minecraft.world.World) world).getBlockState(sourcePos);
            final LocatableBlock locatable = LocatableBlock.builder()
                    .location(new Location<World>(world, sourcePos.getX(), sourcePos.getY(), sourcePos.getZ()))
                    .state(blockstate)
                    .build();
            if (context.getNotifier().isPresent()) {
                context.addNotifierAndOwnerToCauseStack();
            } else {

                final IMixinChunk mixinChunk = (IMixinChunk) ((WorldServer) world).getChunkFromBlockCoords(sourcePos);
                mixinChunk.getBlockNotifier(sourcePos).ifPresent(user -> Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, user));
                mixinChunk.getBlockOwner(sourcePos).ifPresent(owner -> Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner));
            }
            Sponge.getCauseStackManager().pushCause(locatable);

            final Map<Direction, BlockState> neighbors = new HashMap<>();
            for (Object obj : notifiedSides) {
                EnumFacing notifiedSide = (EnumFacing) obj;
                BlockPos offset = sourcePos.offset(notifiedSide);

                Direction direction = DirectionFacingProvider.getInstance().getKey(notifiedSide).get();
                Location<World> location = new Location<>(world, VecHelper.toVector3i(offset));
                if (location.getBlockY() >= 0 && location.getBlockY() <= 255) {
                    neighbors.put(direction, location.getBlock());
                }
            }

            // ImmutableMap<Direction, BlockState> originalNeighbors =
            // ImmutableMap.copyOf(neighbors);

            NotifyNeighborBlockEvent event =
                    SpongeEventFactory.createNotifyNeighborBlockEvent(Sponge.getCauseStackManager().getCurrentCause(), neighbors, neighbors);
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static InteractEntityEvent.Primary callInteractEntityEventPrimary(EntityPlayerMP player, net.minecraft.entity.Entity entity, EnumHand hand,@Nullable Vec3d hitVec) {Sponge.getCauseStackManager().pushCause(player);
        InteractEntityEvent.Primary event;
        Optional<Vector3d> hitVector = hitVec == null ? Optional.empty() : Optional.of(VecHelper.toVector3d(hitVec));
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractEntityEventPrimaryMainHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND, hitVector, EntityUtil.fromNative(entity));
        } else {
            event = SpongeEventFactory.createInteractEntityEventPrimaryOffHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.OFF_HAND, hitVector, EntityUtil.fromNative(entity));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractEntityEvent.Secondary callInteractEntityEventSecondary(EntityPlayerMP player, net.minecraft.entity.Entity entity,
            EnumHand hand, Optional<Vector3d> interactionPoint) {
        InteractEntityEvent.Secondary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractEntityEventSecondaryMainHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND, interactionPoint, EntityUtil.fromNative(entity));
        } else {
            event = SpongeEventFactory.createInteractEntityEventSecondaryOffHand(
                    Sponge.getCauseStackManager().getCurrentCause(), HandTypes.OFF_HAND, interactionPoint, EntityUtil.fromNative(entity));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractItemEvent callInteractItemEventPrimary(EntityPlayer player, ItemStack stack, EnumHand hand,
        Optional<Vector3d> interactionPoint, Object hitTarget) {
        if (hitTarget instanceof Entity) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.ENTITY_HIT, ((Entity) hitTarget));
        } else if (hitTarget instanceof BlockSnapshot) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.BLOCK_HIT, (BlockSnapshot) hitTarget);
        }
        InteractItemEvent.Primary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractItemEventPrimaryMainHand(Sponge.getCauseStackManager().getCurrentCause(),
                    HandTypes.MAIN_HAND, interactionPoint, ItemStackUtil.snapshotOf(stack));
        } else {
            event = SpongeEventFactory.createInteractItemEventPrimaryOffHand(Sponge.getCauseStackManager().getCurrentCause(),
                    HandTypes.OFF_HAND, interactionPoint, ItemStackUtil.snapshotOf(stack));
        }
        return event;
    }

    public static InteractItemEvent callInteractItemEventSecondary(EntityPlayer player, ItemStack stack, EnumHand hand,
            Optional<Vector3d> interactionPoint, Object hitTarget) {
        if (hitTarget instanceof Entity) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.ENTITY_HIT, ((Entity) hitTarget));
        } else if (hitTarget instanceof BlockSnapshot) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.BLOCK_HIT, (BlockSnapshot) hitTarget);
        }
        InteractItemEvent.Secondary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractItemEventSecondaryMainHand(Sponge.getCauseStackManager().getCurrentCause(),
                    HandTypes.MAIN_HAND, interactionPoint, ItemStackUtil.snapshotOf(stack));
        } else {
            event = SpongeEventFactory.createInteractItemEventSecondaryOffHand(Sponge.getCauseStackManager().getCurrentCause(),
                    HandTypes.OFF_HAND, interactionPoint, ItemStackUtil.snapshotOf(stack));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(EntityPlayer player, EnumHand hand) {
        InteractBlockEvent.Primary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND,
                    Optional.empty(), BlockSnapshot.NONE, Direction.NONE);
        } else {
            event = SpongeEventFactory.createInteractBlockEventPrimaryOffHand(Sponge.getCauseStackManager().getCurrentCause(), HandTypes.OFF_HAND, Optional.empty(), BlockSnapshot.NONE, Direction.NONE);
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(EntityPlayer player, BlockSnapshot blockSnapshot, EnumHand hand,
            EnumFacing side) {
        InteractBlockEvent.Primary event;
        Direction direction = DirectionFacingProvider.getInstance().getKey(side).get();
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(Sponge.getCauseStackManager().getCurrentCause(), HandTypes.MAIN_HAND,
                    Optional.empty(), blockSnapshot, direction);
        } else {
            event = SpongeEventFactory.createInteractBlockEventPrimaryOffHand(Sponge.getCauseStackManager().getCurrentCause(), HandTypes.OFF_HAND, Optional.empty(), blockSnapshot, direction);
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(EntityPlayer player, ItemStack heldItem, Optional<Vector3d> interactionPoint,
            BlockSnapshot targetBlock, Direction targetSide, EnumHand hand) {
        return callInteractBlockEventSecondary(player, heldItem, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED,
                interactionPoint, targetBlock, targetSide, hand);
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(EntityPlayer player, ItemStack heldItem, Tristate originalUseBlockResult, Tristate useBlockResult,
            Tristate originalUseItemResult, Tristate useItemResult, Optional<Vector3d> interactionPoint, BlockSnapshot targetBlock,
            Direction targetSide, EnumHand hand) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            InteractBlockEvent.Secondary event;
            if (heldItem != null) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
            }
            if (hand == EnumHand.MAIN_HAND) {
                event = SpongeEventFactory.createInteractBlockEventSecondaryMainHand(Sponge.getCauseStackManager().getCurrentCause(),
                        originalUseBlockResult, useBlockResult, originalUseItemResult,
                        useItemResult, HandTypes.MAIN_HAND, interactionPoint, targetBlock, targetSide);
            } else {
                event = SpongeEventFactory.createInteractBlockEventSecondaryOffHand(Sponge.getCauseStackManager().getCurrentCause(),
                        originalUseBlockResult, useBlockResult, originalUseItemResult,
                        useItemResult, HandTypes.OFF_HAND, interactionPoint, targetBlock, targetSide);
            }
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static boolean callDestructEntityEventDeath(EntityLivingBase entity, DamageSource source) {
        final MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter();
        MessageChannel originalChannel;
        MessageChannel channel;
        Text originalMessage;
        Optional<User> sourceCreator = Optional.empty();
        boolean messageCancelled = false;

        if (entity instanceof EntityPlayerMP) {
            originalChannel = channel = ((IMixinEntityPlayerMP) entity).getDeathMessageChannel();
        } else {
            originalChannel = MessageChannel.TO_NONE;
            channel = MessageChannel.TO_NONE;
        }
        if (source instanceof EntityDamageSource) {
            EntityDamageSource damageSource = (EntityDamageSource) source;
            IMixinEntity spongeEntity = (IMixinEntity) damageSource.getImmediateSource();
            if (spongeEntity != null) {
                sourceCreator = spongeEntity.getCreatorUser();
            }
        }

        originalMessage = SpongeTexts.toText(entity.getCombatTracker().getDeathMessage());
        formatter.getBody().add(new MessageEvent.DefaultBodyApplier(originalMessage));
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        // Try-with-resources will not produce an NPE when trying to autoclose the frame if it is null. Client sided
        // checks need to be made here since entities can die on the client world.
        try (final StackFrame frame = isMainThread ? Sponge.getCauseStackManager().pushCauseFrame() : null) {
            if (isMainThread) {
                Sponge.getCauseStackManager().pushCause(source);
                if (sourceCreator.isPresent()) {
                    Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, sourceCreator.get());
                }
            }

            final Cause cause = isMainThread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), source);
            DestructEntityEvent.Death event = SpongeEventFactory.createDestructEntityEventDeath(cause,
                originalChannel, Optional.of(channel), formatter,
                (Living) entity, messageCancelled);
            SpongeImpl.postEvent(event);
            Text message = event.getMessage();
            if (!event.isMessageCancelled() && !message.isEmpty()) {
                event.getChannel().ifPresent(eventChannel -> eventChannel.send(entity, event.getMessage()));
            }
            return true;
        }
    }

    public static boolean handleCollideBlockEvent(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, net.minecraft.entity.Entity entity, Direction direction) {
        if (pos.getY() <= 0) {
            return false;
        }

        final CauseTracker causeTracker = CauseTracker.getInstance();
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause( entity);

            if (!(entity instanceof EntityPlayer)) {
                IMixinEntity spongeEntity = (IMixinEntity) entity;
                Optional<User> user = spongeEntity.getCreatorUser();
                if (user.isPresent()) {
                    Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, user.get());
                }
            }

            // TODO: Add target side support
            CollideBlockEvent event = SpongeEventFactory.createCollideBlockEvent(Sponge.getCauseStackManager().getCurrentCause(), (BlockState) state,
                    new Location<>((World) world, VecHelper.toVector3d(pos)), direction);
            boolean cancelled = SpongeImpl.postEvent(event);
            if (!cancelled) {
                IMixinEntity spongeEntity = (IMixinEntity) entity;
                if (!pos.equals(spongeEntity.getLastCollidedBlockPos())) {
                    final PhaseData peek = causeTracker.getCurrentPhaseData();
                    final Optional<User> notifier = peek.context.getNotifier();
                    if (notifier.isPresent()) {
                        IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(pos);
                        spongeChunk.addTrackedBlockPosition(block, pos, notifier.get(), PlayerTracker.Type.NOTIFIER);
                    }
                }
            }
            return cancelled;
        }
    }

    public static boolean handleCollideImpactEvent(net.minecraft.entity.Entity projectile, @Nullable ProjectileSource projectileSource,
            RayTraceResult movingObjectPosition) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        RayTraceResult.Type movingObjectType = movingObjectPosition.typeOfHit;
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(projectile);
            Sponge.getCauseStackManager().addContext(EventContextKeys.PROJECTILE_SOURCE, projectileSource == null
                    ? ProjectileSource.UNKNOWN
                    : projectileSource);
            final Optional<User> owner = causeTracker.getCurrentPhaseData().context.getOwner();
            owner.ifPresent(user -> Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, user));

            Location<World> impactPoint = new Location<>((World) projectile.world, VecHelper.toVector3d(movingObjectPosition.hitVec));
            boolean cancelled = false;

            if (movingObjectType == RayTraceResult.Type.BLOCK) {
                final BlockPos blockPos = movingObjectPosition.getBlockPos();
                if (blockPos.getY() <= 0) {
                    return false;
                }

                BlockSnapshot targetBlock = ((World) projectile.world).createSnapshot(VecHelper.toVector3i(movingObjectPosition.getBlockPos()));
                Direction side = Direction.NONE;
                if (movingObjectPosition.sideHit != null) {
                    side = DirectionFacingProvider.getInstance().getKey(movingObjectPosition.sideHit).get();
                }

                CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(Sponge.getCauseStackManager().getCurrentCause(),
                        impactPoint, targetBlock.getState(),
                        targetBlock.getLocation().get(), side);
                cancelled = SpongeImpl.postEvent(event);
                // Track impact block if event is not cancelled
                if (!cancelled && owner.isPresent()) {
                    BlockPos targetPos = VecHelper.toBlockPos(impactPoint.getBlockPosition());
                    IMixinChunk spongeChunk = (IMixinChunk) projectile.world.getChunkFromBlockCoords(targetPos);
                    spongeChunk.addTrackedBlockPosition((Block) targetBlock.getState().getType(), targetPos, owner.get(), PlayerTracker.Type.NOTIFIER);
                }
            } else if (movingObjectPosition.entityHit != null) { // entity
                ArrayList<Entity> entityList = new ArrayList<>();
                entityList.add((Entity) movingObjectPosition.entityHit);
                CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(Sponge.getCauseStackManager().getCurrentCause(), entityList, impactPoint);
                        cancelled = SpongeImpl.postEvent(event);
            }

            if (cancelled) {
                // Entities such as EnderPearls call setDead during onImpact. However, if the event is cancelled
                // setDead will never be called resulting in a bad state such as falling through world.
                projectile.setDead();
            }
            return cancelled;
        }
    }

    public static ClickInventoryEvent.Creative callCreativeClickInventoryEvent(EntityPlayerMP player, CPacketCreativeInventoryAction packetIn) {
        Sponge.getCauseStackManager().pushCause(player);
        // Creative doesn't inform server of cursor status so there is no way of knowing what the final stack is
        // Due to this, we can only send the original item that was clicked in slot
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
        if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0
                && packetIn.getSlotId() < player.openContainer.inventorySlots.size()) {
            Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
            if (slot != null) {
                ItemStackSnapshot clickedItem = slot.getStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot();
                SlotTransaction slotTransaction =
                        new SlotTransaction(new SlotAdapter(slot), clickedItem, ItemStackSnapshot.NONE);
                ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
            }
        }
        ClickInventoryEvent.Creative event =
                SpongeEventFactory.createClickInventoryEventCreative(Sponge.getCauseStackManager().getCurrentCause(), cursorTransaction,
                        (org.spongepowered.api.item.inventory.Container) player.openContainer,
                        ((IMixinContainer) player.openContainer).getCapturedTransactions());
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        return event;
    }

    public static boolean callInteractInventoryOpenEvent(EntityPlayerMP player) {
        ItemStackSnapshot newCursor =
                player.inventory.getItemStack().isEmpty() ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, newCursor);
        InteractInventoryEvent.Open event =
                SpongeEventFactory.createInteractInventoryEventOpen(Sponge.getCauseStackManager().getCurrentCause(), cursorTransaction,
                        (org.spongepowered.api.item.inventory.Container) player.openContainer);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            player.closeScreen();
            return false;
        }
        // TODO - determine if/how we want to fire inventory events outside of click packet handlers
        //((IMixinContainer) player.openContainer).setCaptureInventory(true);
        // Custom cursor
        if (event.getCursorTransaction().getCustom().isPresent()) {
            handleCustomCursor(player, event.getCursorTransaction().getFinal());
        }
        return true;
    }

    public static InteractInventoryEvent.Close callInteractInventoryCloseEvent(Container container, EntityPlayerMP player,
            ItemStackSnapshot lastCursor, ItemStackSnapshot newCursor, boolean clientSource) {
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        final InteractInventoryEvent.Close event =
                SpongeEventFactory.createInteractInventoryEventClose(Sponge.getCauseStackManager().getCurrentCause(), cursorTransaction, ContainerUtil.fromNative(container));
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            if (clientSource && container.getSlot(0) != null) {
                if (!(container instanceof ContainerPlayer)) {
                    // Inventory closed by client, reopen window and send
                    // container
                    player.openContainer = container;
                    final String guiId;
                    final Slot slot = container.getSlot(0);
                    final IInventory slotInventory = slot.inventory;
                    if (slotInventory instanceof IInteractionObject) {
                        guiId = ((IInteractionObject) slotInventory).getGuiID();
                    } else {
                        // expected fallback for unknown types
                        guiId = "minecraft:container";
                    }
                    slotInventory.openInventory(player);
                    player.connection.sendPacket(new SPacketOpenWindow(container.windowId, guiId, slotInventory
                            .getDisplayName(), slotInventory.getSizeInventory()));
                    // resync data to client
                    player.sendContainerToPlayer(container);
                } else {
                    // TODO: Maybe print a warning or throw an exception here?
                    // The player gui cannot be opened from the
                    // server so allowing this event to be cancellable when the
                    // GUI has been closed already would result
                    // in opening the wrong GUI window.
                }
            }
            // Handle cursor
            if (!event.getCursorTransaction().isValid()) {
                handleCustomCursor(player, event.getCursorTransaction().getOriginal());
            }
        } else {
            IMixinContainer mixinContainer = (IMixinContainer) player.openContainer;
            mixinContainer.getCapturedTransactions().clear();
            mixinContainer.setCaptureInventory(false);
            // Handle cursor
            if (!event.getCursorTransaction().isValid()) {
                handleCustomCursor(player, event.getCursorTransaction().getOriginal());
            } else if (event.getCursorTransaction().getCustom().isPresent()) {
                handleCustomCursor(player, event.getCursorTransaction().getFinal());
            }
            if (!clientSource) {
                player.closeScreen();
            }
        }

        return event;
    }

    @Nullable
    public static Container displayContainer(EntityPlayerMP player, Inventory inventory) {
        net.minecraft.inventory.Container previousContainer = player.openContainer;
        net.minecraft.inventory.Container container;

        if (inventory instanceof CustomInventory) {
            if (!checkValidVanillaCustomInventory(((CustomInventory) inventory))) {
                return null; // Invalid size for vanilla inventory ; This is to
                             // prevent crashing the client with invalid data
            }
        }

        if (inventory instanceof IInteractionObject) {
            final String guiId = ((IInteractionObject) inventory).getGuiID();

            switch (guiId) {
                case "EntityHorse":
                    if (inventory instanceof CarriedInventory) {
                        CarriedInventory<?> cinventory = (CarriedInventory<?>) inventory;
                        if (cinventory.getCarrier().isPresent() && cinventory.getCarrier().get() instanceof AbstractHorse) {
                            player.openGuiHorseInventory(((AbstractHorse) cinventory.getCarrier().get()), (IInventory) inventory);
                        }
                    }
                    break;
                case "minecraft:chest":
                    player.displayGUIChest((IInventory) inventory);
                    break;
                case "minecraft:crafting_table":
                case "minecraft:anvil":
                case "minecraft:enchanting_table":
                    player.displayGui((IInteractionObject) inventory);
                    break;
                default:
                    player.displayGUIChest((IInventory) inventory);
                    break;
            }
        } else if (inventory instanceof IInventory) {
            player.displayGUIChest(((IInventory) inventory));
        } else {
            return null;
        }

        container = player.openContainer;

        if (previousContainer == container) {
            return null;
        }

        if (!callInteractInventoryOpenEvent(player)) {
            return null;
        }

        if (container instanceof IMixinContainer) {
            // This overwrites the normal container behaviour and allows viewing
            // inventories that are more than 8 blocks away
            // This currently actually only works for the Containers mixed into
            // by MixinContainerCanInteract ; but throws no errors for other
            // containers

            // Allow viewing inventory; except when dead
            ((IMixinContainer) container).setCanInteractWith(p -> !p.isDead);
        }
        return container;
    }

    private static boolean checkValidVanillaCustomInventory(CustomInventory inventory) {
        InventoryArchetype archetype = inventory.getArchetype();
        if (InventoryArchetypes.CHEST.equals(archetype) || InventoryArchetypes.DOUBLE_CHEST.equals(archetype)) {
            int size = inventory.getSizeInventory(); // Divisible by
            // 9 AND less than 6 rows of 9 slots
            return size % 9 == 0 && size / 9 <= 6 && size != 0;
        }
        if (InventoryArchetypes.HOPPER.equals(archetype)) {
            return inventory.getSizeInventory() == 5 * 1;
        }
        if (InventoryArchetypes.DISPENSER.equals(archetype)) {
            return inventory.getSizeInventory() == 3 * 3;
        }
        if (InventoryArchetypes.WORKBENCH.equals(archetype)) {
            return inventory.getSizeInventory() == 3 * 3 + 1;
        }
        if (InventoryArchetypes.FURNACE.equals(archetype)) {
            return inventory.getSizeInventory() == 3;
        }
        if (InventoryArchetypes.ENCHANTING_TABLE.equals(archetype)) {
            return inventory.getSizeInventory() == 2;
        }
        if (InventoryArchetypes.ANVIL.equals(archetype)) {
            return inventory.getSizeInventory() == 3;
        }
        if (InventoryArchetypes.BREWING_STAND.equals(archetype)) {
            return inventory.getSizeInventory() == 5;
        }
        if (InventoryArchetypes.BEACON.equals(archetype)) {
            return inventory.getSizeInventory() == 1;
        }
        // TODO horse container are actually dependent on an horse entity
        if (InventoryArchetypes.HORSE.equals(archetype)) {
            return inventory.getSizeInventory() == 2;
        }
        if (InventoryArchetypes.HORSE_WITH_CHEST.equals(archetype)) {
            return inventory.getSizeInventory() == 2 + 5 * 3;
        }
        if (InventoryArchetypes.VILLAGER.equals(archetype)) {
            return inventory.getSizeInventory() == 3;
        }
        // else any other Archetype we cannot be sure which size is correct
        return true;
    }

}
