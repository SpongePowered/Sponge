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

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil.handleCustomCursor;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.inventory.Inventory;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
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
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
    // Set if any of the events fired during interaction with a block (open inventory or interact block) were cancelled
    public static boolean interactBlockEventCancelled = false;
    // Dummy ChangeBlockEvent.Pre
    public static ChangeBlockEvent.Pre DUMMY_BLOCK_PRE_EVENT = null;

    // For animation packet
    public static int lastAnimationPacketTick = 0;
    public static int lastSecondaryPacketTick = 0;
    public static int lastPrimaryPacketTick = 0;
    public static WeakReference<EntityPlayerMP> lastAnimationPlayer;

    public static boolean callPlayerChangeInventoryPickupEvent(EntityPlayer player, EntityItem itemToPickup, int pickupDelay, UUID creator) {
        ItemStack itemStack = itemToPickup.getEntityItem();
        int slotId = ((IMixinInventoryPlayer) player.inventory).getFirstAvailableSlot(itemStack);
        Slot slot = null;
        if (slotId != -1) {
            if (slotId < InventoryPlayer.getHotbarSize()) {
                slot = player.inventoryContainer.getSlot(slotId + player.inventory.mainInventory.length);
            } else {
                slot = player.inventoryContainer.getSlot(slotId);
            }
        }

        if (pickupDelay <= 0 && slot != null) {
            ItemStackSnapshot sourceSnapshot =
                    slot.getStack() != null ? ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot()
                                            : ItemStackSnapshot.NONE;
            ItemStackSnapshot targetSnapshot = null;
            if (sourceSnapshot != ItemStackSnapshot.NONE) {
                // combined slot
                targetSnapshot = org.spongepowered.api.item.inventory.ItemStack.builder().from((org.spongepowered.api.item.inventory.ItemStack) itemStack).quantity(itemStack.stackSize + slot.getStack().stackSize).build().createSnapshot();
            } else {
                // empty slot
                targetSnapshot = ((org.spongepowered.api.item.inventory.ItemStack) itemStack).createSnapshot();
            }

            ((SpongeItemStackSnapshot) targetSnapshot).setCreator(creator);
            SlotTransaction slotTransaction =
                    new SlotTransaction(new SlotAdapter(slot), sourceSnapshot, targetSnapshot);
            ImmutableList<SlotTransaction> transactions =
                    new ImmutableList.Builder<SlotTransaction>().add(slotTransaction).build();
            ChangeInventoryEvent.Pickup event = SpongeEventFactory.createChangeInventoryEventPickup(Cause.of(NamedCause.source(player)),
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
        Cause.Builder builder = null;
        if (sourceEntity != null) {
            builder = Cause.source(sourceEntity);
        } else {
            PhaseContext context = causeTracker.getCurrentContext();

            final Optional<LocatableBlock> currentTickingBlock = context.getSource(LocatableBlock.class);
            final Optional<TileEntity> currentTickingTileEntity = context.getSource(TileEntity.class);
            final Optional<Entity> currentTickingEntity = context.getSource(Entity.class);
            if (currentTickingBlock.isPresent()) {
                builder = Cause.source(currentTickingBlock.get());
            } else if (currentTickingTileEntity.isPresent()) {
                builder = Cause.source(currentTickingTileEntity.get());
            } else if (currentTickingEntity.isPresent()) {
                builder = Cause.source(currentTickingEntity.get());
            }

            if (builder == null) {
                return null;
            }
        }
        final Optional<User> owner = causeTracker.getCurrentPhaseData()
                .context
                .firstNamed(NamedCause.OWNER, User.class);
        if (owner.isPresent()) {
            builder.named(NamedCause.OWNER, owner.get());
        }

        List<Entity> spEntities = (List<Entity>) (List<?>) entities;
        CollideEntityEvent event = SpongeEventFactory.createCollideEntityEvent(builder.build(), spEntities, (World) world);
        SpongeImpl.postEvent(event);
        return event;
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, BlockPos pos, NamedCause namedWorldCause) {
        return callChangeBlockEventPre(worldIn, ImmutableList.of(new Location<>((World) worldIn, pos.getX(), pos.getY(), pos.getZ())), namedWorldCause, null);
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, BlockPos pos, NamedCause namedWorldCause, Object source) {
        return callChangeBlockEventPre(worldIn, ImmutableList.of(new Location<>((World) worldIn, pos.getX(), pos.getY(), pos.getZ())), namedWorldCause, source);
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(IMixinWorldServer worldIn, ImmutableList<Location<World>> locations, NamedCause namedWorldCause, Object source) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final PhaseData data = causeTracker.getCurrentPhaseData();
        final IPhaseState phaseState = data.state;
        if (source == null) {
            source = data.context.getSource(LocatableBlock.class).orElse(null);
            if (source == null) {
                // safety measure, return a dummy event
                if (DUMMY_BLOCK_PRE_EVENT == null) {
                    DUMMY_BLOCK_PRE_EVENT = SpongeEventFactory.createChangeBlockEventPre(Cause.source(worldIn).build(), ImmutableList.of(), worldIn.asSpongeWorld());
                }
                return DUMMY_BLOCK_PRE_EVENT;
            }
        }

        EntityPlayer player = null;
        Cause.Builder builder = null;
        User owner = data.context.getOwner().orElse(null);
        User notifier = data.context.getNotifier().orElse(null);
        // handle FakePlayer
        if (source instanceof EntityPlayer) {
            player = (EntityPlayer) source;
            if (SpongeImplHooks.isFakePlayer(player)) {
                if (owner != null) {
                    builder = Cause.source(owner);
                    builder.named(NamedCause.FAKE_PLAYER, player);
                } else if (notifier != null) {
                    builder = Cause.source(notifier);
                    builder.named(NamedCause.FAKE_PLAYER, player);
                } else {
                    builder = Cause.builder().named(NamedCause.FAKE_PLAYER, player);
                }
            }
        }
        if (builder == null) {
            builder = Cause.source(source);
        }

        if (owner != null) {
            builder.owner(owner);
        }
        if (notifier != null) {
            if (!(phaseState.getPhase().appendPreBlockProtectedCheck(builder, phaseState, data.context, causeTracker))) {
                builder.notifier(notifier);
            }
        }

        builder.named(namedWorldCause);

        ChangeBlockEvent.Pre event = SpongeEventFactory.createChangeBlockEventPre(builder.build(), locations, worldIn.asSpongeWorld());
        SpongeImpl.postEvent(event);
        return event;
    }

    /**
     * This simulates the blocks a piston moves and calls the event for saner debugging.
     * @return if the event was cancelled
     */
    public static boolean handlePistonEvent(IMixinWorldServer world, WorldServer.ServerBlockEventList list, Object obj, BlockPos pos, Block blockIn, int eventId, int eventParam) {
        boolean extending = (eventId == 0);
        final IBlockState blockstate = ((net.minecraft.world.World) world).getBlockState(pos);
        EnumFacing direction = (EnumFacing) blockstate.getValue(BlockDirectional.FACING);
        final LocatableBlock locatable = LocatableBlock.builder()
                .location(new Location<World>((World) world, pos.getX(), pos.getY(), pos.getZ()))
                .state((BlockState) blockstate)
                .build();

        // Sets toss out duplicate values (even though there shouldn't be any)
        HashSet<Location<org.spongepowered.api.world.World>> locations = new HashSet<>();
        locations.add(new Location<>((World) world, pos.getX(), pos.getY(), pos.getZ()));

        BlockPistonStructureHelper movedBlocks = new BlockPistonStructureHelper((WorldServer) (Object) world, pos, direction, extending);
        movedBlocks.canMove(); // calculates blocks to be moved

        Stream.concat(movedBlocks.getBlocksToMove().stream(), movedBlocks.getBlocksToDestroy().stream())
            .map(block -> new Location<>((World) world, block.getX(), block.getY(), block.getZ()))
            .collect(Collectors.toCollection(() -> locations)); // SUPER efficient code!

        String namedCause = extending ? NamedCause.PISTON_EXTEND : NamedCause.PISTON_RETRACT;
        return SpongeCommonEventFactory.callChangeBlockEventPre(world, ImmutableList.copyOf(locations), NamedCause.of(namedCause, world), locatable).isCancelled();
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

        User user = context.first(User.class).orElse(null);
        Object rootCause = context.first(Object.class).orElse(null);
        Cause.Builder builder;

        if (rootCause instanceof PhaseContext) {
            PhaseContext phaseContext = (PhaseContext) rootCause;
            rootCause = phaseContext.first(Object.class).orElse(null);
        }

        final BlockState blockstate = (BlockState)((net.minecraft.world.World) world).getBlockState(sourcePos);
        final LocatableBlock locatable = LocatableBlock.builder()
                .location(new Location<World>((World) world, sourcePos.getX(), sourcePos.getY(), sourcePos.getZ()))
                .state(blockstate)
                .build();
        builder = Cause.source(locatable);
        if (rootCause instanceof User) {
            builder.named(NamedCause.notifier(rootCause));
        } else {
            if (user != null) {
                builder.named(NamedCause.notifier(user));
            } else {
                final IMixinChunk mixinChunk = (IMixinChunk) ((WorldServer) world).getChunkFromBlockCoords(sourcePos);
                peek.state.getPhase().populateCauseForNotifyNeighborEvent(peek.state, context, builder, causeTracker, mixinChunk, sourcePos);
            }
        }

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

        //ImmutableMap<Direction, BlockState> originalNeighbors = ImmutableMap.copyOf(neighbors);

        NotifyNeighborBlockEvent event = SpongeEventFactory.createNotifyNeighborBlockEvent(builder.build(), neighbors, neighbors);
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractEntityEvent.Primary callInteractEntityEventPrimary(EntityPlayerMP player, net.minecraft.entity.Entity entity, EnumHand hand, Vec3d hitVec) {
        InteractEntityEvent.Primary event;
        Optional<Vector3d> hitVector = hitVec == null ? Optional.empty() : Optional.of(VecHelper.toVector3d(hitVec));
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractEntityEventPrimaryMainHand(
                    Cause.of(NamedCause.source(player)), HandTypes.MAIN_HAND, hitVector, EntityUtil.fromNative(entity));
        } else {
            event = SpongeEventFactory.createInteractEntityEventPrimaryOffHand(
                    Cause.of(NamedCause.source(player)), HandTypes.OFF_HAND, hitVector, EntityUtil.fromNative(entity));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractEntityEvent.Secondary callInteractEntityEventSecondary(EntityPlayerMP player, net.minecraft.entity.Entity entity, EnumHand hand, Optional<Vector3d> interactionPoint) {
        InteractEntityEvent.Secondary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractEntityEventSecondaryMainHand(
                    Cause.of(NamedCause.source(player)), HandTypes.MAIN_HAND, interactionPoint, EntityUtil.fromNative(entity));
        } else {
            event = SpongeEventFactory.createInteractEntityEventSecondaryOffHand(
                    Cause.of(NamedCause.source(player)), HandTypes.OFF_HAND, interactionPoint, EntityUtil.fromNative(entity));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractItemEvent callInteractItemEventPrimary(EntityPlayer player, ItemStack stack, EnumHand hand, Optional<Vector3d> interactionPoint, Object hitTarget) {
        InteractItemEvent.Primary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractItemEventPrimaryMainHand(Cause.of(NamedCause.source(player), NamedCause.hitTarget(hitTarget)), HandTypes.MAIN_HAND, interactionPoint, ItemStackUtil.snapshotOf(stack));
        } else {
            event = SpongeEventFactory.createInteractItemEventPrimaryOffHand(Cause.of(NamedCause.source(player), NamedCause.hitTarget(hitTarget)), HandTypes.OFF_HAND, interactionPoint, ItemStackUtil.snapshotOf(stack));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractItemEvent callInteractItemEventSecondary(EntityPlayer player, ItemStack stack, EnumHand hand, Optional<Vector3d> interactionPoint, Object hitTarget) {
        InteractItemEvent.Secondary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractItemEventSecondaryMainHand(Cause.of(NamedCause.source(player), NamedCause.hitTarget(hitTarget)), HandTypes.MAIN_HAND, interactionPoint, ItemStackUtil.snapshotOf(stack));
        } else {
            event = SpongeEventFactory.createInteractItemEventSecondaryOffHand(Cause.of(NamedCause.source(player), NamedCause.hitTarget(hitTarget)), HandTypes.OFF_HAND, interactionPoint, ItemStackUtil.snapshotOf(stack));
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(EntityPlayer player, EnumHand hand) {
        InteractBlockEvent.Primary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(Cause.of(NamedCause.source(player)), HandTypes.MAIN_HAND, Optional.empty(), BlockSnapshot.NONE, Direction.NONE);
        } else {
            event = SpongeEventFactory.createInteractBlockEventPrimaryOffHand(Cause.of(NamedCause.source(player)), HandTypes.OFF_HAND, Optional.empty(), BlockSnapshot.NONE, Direction.NONE);
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(EntityPlayer player, BlockSnapshot blockSnapshot, EnumHand hand, EnumFacing side) {
        InteractBlockEvent.Primary event;
        Direction direction = DirectionFacingProvider.getInstance().getKey(side).get();
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(Cause.of(NamedCause.source(player)), HandTypes.MAIN_HAND, Optional.empty(), blockSnapshot, direction);
        } else {
            event = SpongeEventFactory.createInteractBlockEventPrimaryOffHand(Cause.of(NamedCause.source(player)), HandTypes.OFF_HAND, Optional.empty(), blockSnapshot, direction);
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(Cause cause, Optional<Vector3d> interactionPoint, BlockSnapshot targetBlock, Direction targetSide, EnumHand hand) {
        return callInteractBlockEventSecondary(cause, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, interactionPoint, targetBlock, targetSide, hand);
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(Cause cause, Tristate originalUseBlockResult, Tristate useBlockResult, Tristate originalUseItemResult, Tristate useItemResult, Optional<Vector3d> interactionPoint, BlockSnapshot targetBlock, Direction targetSide, EnumHand hand) {
        InteractBlockEvent.Secondary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractBlockEventSecondaryMainHand(cause, originalUseBlockResult, useBlockResult, originalUseItemResult, useItemResult, HandTypes.MAIN_HAND, interactionPoint, targetBlock, targetSide);
        } else {
            event = SpongeEventFactory.createInteractBlockEventSecondaryOffHand(cause, originalUseBlockResult, useBlockResult, originalUseItemResult, useItemResult, HandTypes.OFF_HAND, interactionPoint, targetBlock, targetSide);
        }
        SpongeImpl.postEvent(event);
        return event;
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
            IMixinEntity spongeEntity = (IMixinEntity) damageSource.getSourceOfDamage();
            if (spongeEntity != null) {
                sourceCreator = spongeEntity.getCreatorUser();
            }
        }

        originalMessage = SpongeTexts.toText(entity.getCombatTracker().getDeathMessage());
        formatter.getBody().add(new MessageEvent.DefaultBodyApplier(originalMessage));
        List<NamedCause> causes = new ArrayList<>();
        causes.add(NamedCause.of("Attacker", source));
        if (sourceCreator.isPresent()) {
            causes.add(NamedCause.owner(sourceCreator.get()));
        }

        Cause cause = Cause.of(causes);
        DestructEntityEvent.Death event = SpongeEventFactory.createDestructEntityEventDeath(cause, originalChannel, Optional.of(channel), formatter, (Living) entity, messageCancelled);
        SpongeImpl.postEvent(event);
        Text message = event.getMessage();
        if (!event.isMessageCancelled() && !message.isEmpty()) {
            event.getChannel().ifPresent(eventChannel -> eventChannel.send(entity, event.getMessage()));
        }
        return true;
    }

    public static boolean handleCollideBlockEvent(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, net.minecraft.entity.Entity entity, Direction direction) {
        if (pos.getY() <= 0) {
            return false;
        }

        final CauseTracker causeTracker = CauseTracker.getInstance();
        final Cause.Builder builder = Cause.source(entity);
        builder.named(NamedCause.of(NamedCause.PHYSICAL, entity));

        if (!(entity instanceof EntityPlayer)) {
            IMixinEntity spongeEntity = (IMixinEntity) entity;
            Optional<User> user = spongeEntity.getCreatorUser();
            if (user.isPresent()) {
                builder.named(NamedCause.owner(user.get()));
            }
        }

        // TODO: Add target side support
        CollideBlockEvent event = SpongeEventFactory.createCollideBlockEvent(builder.build(), (BlockState) state,
                new Location<>((World) world, VecHelper.toVector3d(pos)), direction);
        boolean cancelled = SpongeImpl.postEvent(event);
        if (!cancelled) {
            IMixinEntity spongeEntity = (IMixinEntity) entity;
            if (!pos.equals(spongeEntity.getLastCollidedBlockPos())) {
                final PhaseData peek = causeTracker.getCurrentPhaseData();
                final Optional<User> notifier = peek.context.firstNamed(NamedCause.NOTIFIER, User.class);
                if (notifier.isPresent()) {
                    IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(pos);
                    spongeChunk.addTrackedBlockPosition(block, pos, notifier.get(), PlayerTracker.Type.NOTIFIER);
                }
            }
        }

        return cancelled;
    }

    public static boolean handleCollideImpactEvent(net.minecraft.entity.Entity projectile, @Nullable ProjectileSource projectileSource,
            RayTraceResult movingObjectPosition) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        RayTraceResult.Type movingObjectType = movingObjectPosition.typeOfHit;
        final Cause.Builder builder = Cause.source(projectile).named("ProjectileSource", projectileSource == null
                                                                                         ? ProjectileSource.UNKNOWN
                                                                                         : projectileSource);
        final Optional<User> owner = causeTracker.getCurrentPhaseData()
                .context
                .firstNamed(NamedCause.OWNER, User.class);
        owner.ifPresent(user -> builder.named(NamedCause.OWNER, user));

        Location<World> impactPoint = new Location<>((World) projectile.worldObj, VecHelper.toVector3d(movingObjectPosition.hitVec));
        boolean cancelled = false;

        if (movingObjectType == RayTraceResult.Type.BLOCK) {
            final BlockPos blockPos = movingObjectPosition.getBlockPos();
            if (blockPos.getY() <= 0) {
                return false;
            }

            BlockSnapshot targetBlock = ((World) projectile.worldObj).createSnapshot(VecHelper.toVector3i(blockPos));
            Direction side = Direction.NONE;
            if (movingObjectPosition.sideHit != null) {
                side = DirectionFacingProvider.getInstance().getKey(movingObjectPosition.sideHit).get();
            }

            CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(builder.build(), impactPoint, targetBlock.getState(),
                    targetBlock.getLocation().get(), side);
            cancelled = SpongeImpl.postEvent(event);
            // Track impact block if event is not cancelled
            if (!cancelled && owner.isPresent()) {
                BlockPos targetPos = VecHelper.toBlockPos(impactPoint.getBlockPosition());
                IMixinChunk spongeChunk = (IMixinChunk) projectile.worldObj.getChunkFromBlockCoords(targetPos);
                spongeChunk.addTrackedBlockPosition((Block) targetBlock.getState().getType(), targetPos, owner.get(), PlayerTracker.Type.NOTIFIER);
            }
        } else if (movingObjectPosition.entityHit != null) { // entity
            ArrayList<Entity> entityList = new ArrayList<>();
            entityList.add((Entity) movingObjectPosition.entityHit);
            CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(builder.build(), entityList, impactPoint, impactPoint.getExtent());
            return SpongeImpl.postEvent(event);
        }

        return cancelled;
    }


    public static void checkSpawnEvent(Entity entity, Cause cause) {
        checkArgument(cause.root() instanceof SpawnCause, "The cause does not have a SpawnCause! It has instead: {}", cause.root().toString());
        checkArgument(cause.containsNamed(NamedCause.SOURCE), "The cause does not have a \"Source\" named object!");
        checkArgument(cause.get(NamedCause.SOURCE, SpawnCause.class).isPresent(), "The SpawnCause is not the \"Source\" of the cause!");

    }


    public static ClickInventoryEvent.Creative callCreativeClickInventoryEvent(EntityPlayerMP player, CPacketCreativeInventoryAction packetIn) {
        Cause cause = Cause.of(NamedCause.owner(player));
        // Creative doesn't inform server of cursor status
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
        if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0
            && packetIn.getSlotId() < player.openContainer.inventorySlots.size()) {
            Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
            if (slot != null) {
                SlotTransaction slotTransaction =
                        new SlotTransaction(new SlotAdapter(slot), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
            }
        }
        ClickInventoryEvent.Creative event = SpongeEventFactory.createClickInventoryEventCreative(cause, cursorTransaction,
                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                ((IMixinContainer) player.openContainer).getCapturedTransactions());
        SpongeImpl.postEvent(event);
        return event;
    }

    public static boolean callInteractInventoryOpenEvent(Cause cause, EntityPlayerMP player) {
        ItemStackSnapshot newCursor =
                player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, newCursor);
        InteractInventoryEvent.Open event =
                SpongeEventFactory.createInteractInventoryEventOpen(cause, cursorTransaction,
                        (org.spongepowered.api.item.inventory.Container) player.openContainer);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            player.closeScreen();
            return false;
        } else {
            // TODO - determine if/how we want to fire inventory events outside of click poaket handlers
            //((IMixinContainer) player.openContainer).setCaptureInventory(true);
            // Custom cursor
            if (event.getCursorTransaction().getCustom().isPresent()) {
                handleCustomCursor(player, event.getCursorTransaction().getFinal());
            }
            return true;
        }
    }

    public static InteractInventoryEvent.Close callInteractInventoryCloseEvent(Cause cause, Container container, EntityPlayerMP player, ItemStackSnapshot lastCursor, ItemStackSnapshot newCursor, boolean clientSource) {
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        final InteractInventoryEvent.Close event
                = SpongeEventFactory.createInteractInventoryEventClose(cause, cursorTransaction, ContainerUtil.fromNative(container));
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            if (clientSource && container.getSlot(0) != null) {
                if (!(container instanceof ContainerPlayer)) {
                    // Inventory closed by client, reopen window and send container
                    player.openContainer = container;
                    final String guiId;
                    final Slot slot = container.getSlot(0);
                    final IInventory slotInventory = slot.inventory;
                    if (slotInventory instanceof IInteractionObject) {
                        guiId = ((IInteractionObject) slotInventory).getGuiID();
                    } else {
                        guiId = "minecraft:container"; // expected fallback for unknown types
                    }
                    slotInventory.openInventory(player);
                    player.connection.sendPacket(new SPacketOpenWindow(container.windowId, guiId, slotInventory
                        .getDisplayName(), slotInventory.getSizeInventory()));
                    // resync data to client
                    player.sendContainerToPlayer(container);
                } else {
                    // TODO: Maybe print a warning or throw an exception here? The player gui cannot be opened from the
                    // server so allowing this event to be cancellable when the GUI has been closed already would result
                    // in opening the wrong GUI window.
                }
            }
        } else {
            IMixinContainer mixinContainer = (IMixinContainer) player.openContainer;
            mixinContainer.getCapturedTransactions().clear();
            mixinContainer.setCaptureInventory(false);
            // Custom cursor
            if (event.getCursorTransaction().getCustom().isPresent()) {
                handleCustomCursor(player, event.getCursorTransaction().getFinal());
            }
            if (!clientSource) {
                player.closeScreen();
            }
        }

        return event;
    }

    @Nullable
    public static Container displayContainer(Cause cause, EntityPlayerMP player, Inventory inventory) {
        net.minecraft.inventory.Container previousContainer = player.openContainer;
        net.minecraft.inventory.Container container = null;

        if (inventory instanceof IInteractionObject) {
            final String guiId = ((IInteractionObject) inventory).getGuiID();

            switch (guiId) {
                case "EntityHorse":
                    // If Carrier is Horse open Inventory
                    if (inventory instanceof CarriedInventory) {
                        if (((CarriedInventory) inventory).getCarrier().isPresent()
                                && ((CarriedInventory) inventory).getCarrier().get() instanceof EntityHorse) {
                            player.openGuiHorseInventory(((EntityHorse) ((CarriedInventory) inventory).getCarrier().get()), (IInventory) inventory);
                            container = player.openContainer;
                        }
                    }
                    break;
                case "minecraft:chest":
                    player.displayGUIChest((IInventory) inventory);
                    container = player.openContainer;
                    break;
                case "minecraft:crafting_table":
                case "minecraft:anvil":
                case "minecraft:enchanting_table":
                    player.displayGui((IInteractionObject) inventory);
                    container = player.openContainer;
                    break;
                default:
                    player.displayGUIChest((IInventory) inventory);
                    container = player.openContainer;
                    break;
            }
        } else if (inventory instanceof IInventory) {
            player.displayGUIChest(((IInventory) inventory));
            container = player.openContainer;
        } else {
            return null;
        }

        if (previousContainer == container) {
            return null;
        }

        if (!callInteractInventoryOpenEvent(cause, player)) {
            return null;
        }

        return container;
    }
}
