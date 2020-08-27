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

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.Jukebox;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.explosive.fused.FusedExplosive;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.ai.SetAITargetEvent;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.event.sound.PlaySoundEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.entity.projectile.UnknownProjectileSource;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class SpongeCommonEventFactory {

    public static int lastAnimationPacketTick = 0;
    // For animation packet
    public static int lastSecondaryPacketTick = 0;
    public static int lastPrimaryPacketTick = 0;
    @Nullable public static WeakReference<ServerPlayerEntity> lastAnimationPlayer;

    public static void callDropItemDispense(final List<ItemEntity> items, final PhaseContext<?> context) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DISPENSE);
            final ArrayList<Entity> entities = new ArrayList<>();
            for (final ItemEntity item : items) {
                entities.add((Entity) item);
            }
            final DropItemEvent.Dispense dispense =
                SpongeEventFactory.createDropItemEventDispense(frame.getCurrentCause(), entities);
            SpongeCommon.postEvent(dispense);
            if (!dispense.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(context, dispense);
            }
        }
    }

    public static void callDropItemDrop(final ServerPlayerEntity player, final List<ItemEntity> items,
            final PhaseContext<?> context) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            final ArrayList<Entity> entities = new ArrayList<>();
            for (final ItemEntity item : items) {
                entities.add((Entity) item);
            }
            // Creative doesn't inform server of cursor status so there is no way of knowing
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.empty(), ItemStackSnapshot.empty());
            final DropItemEvent.Dispense dispense =
                SpongeEventFactory.createClickContainerEventDropOutsideCreative(frame.getCurrentCause(),
                        ((org.spongepowered.api.item.inventory.Container) player.openContainer), cursorTransaction, entities,
                        Optional.empty(), Collections.emptyList());
            SpongeCommon.postEvent(dispense);
            if (!dispense.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(context, dispense);
            }
        }
    }

    public static void callDropItemCustom(final List<Entity> items, final PhaseContext<?> context) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            final DropItemEvent.Custom event =
                SpongeEventFactory.createDropItemEventCustom(frame.getCurrentCause(), items);
            SpongeCommon.postEvent(event);
            if (!event.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(context, event);
            }
        }
    }

    public static void callDropItemCustom(final List<Entity> items, final PhaseContext<?> context, final Supplier<Optional<User>> supplier) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.getCurrentContext().require(EventContextKeys.SPAWN_TYPE);
            final DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(frame.getCurrentCause(), items);
            SpongeCommon.postEvent(event);
            if (!event.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(event, supplier);
            }
        }
    }

    public static void callDropItemClose(final List<Entity> items, final PhaseContext<?> context, final Supplier<Optional<User>> supplier) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.getCurrentContext().require(EventContextKeys.SPAWN_TYPE);
            final DropItemEvent.Close event = SpongeEventFactory.createDropItemEventClose(frame.getCurrentCause(), items);
            SpongeCommon.postEvent(event);
            if (!event.isCancelled()) {
                EntityUtil.processEntitySpawnsFromEvent(event, supplier);
            }
        }
    }

    public static boolean callSpawnEntitySpawner(final List<Entity> entities, final PhaseContext<?> context) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.WORLD_SPAWNER);

            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(frame.getCurrentCause(), entities);
            SpongeCommon.postEvent(event);
            if (!event.isCancelled() && event.getEntities().size() > 0) {
                return EntityUtil.processEntitySpawnsFromEvent(context, event);
            }
            return false;
        }
    }

    public static void callDropItemDestruct(final List<Entity> entities, final PhaseContext<?> context) {
        final DropItemEvent.Destruct destruct = SpongeEventFactory.createDropItemEventDestruct(PhaseTracker.getCauseStackManager().getCurrentCause(), entities);
        SpongeCommon.postEvent(destruct);
        if (!destruct.isCancelled()) {
            EntityUtil.processEntitySpawnsFromEvent(context, destruct);
        }
    }

    public static boolean callSpawnEntity(final List<Entity> entities, final PhaseContext<?> context) {
        PhaseTracker.getCauseStackManager().getCurrentContext().require(EventContextKeys.SPAWN_TYPE);
        try {
            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), entities);
            SpongeCommon.postEvent(event);
            return !event.isCancelled() && EntityUtil.processEntitySpawnsFromEvent(context, event);
        } catch (final Exception e) {
            final PrettyPrinter printer = new PrettyPrinter(60).add("Exception trying to create a Spawn Event").centre().hr()
                .addWrapped(
                    "Something did not go well trying to create an event or while trying to throw a SpawnEntityEvent. My bet is it's gremlins")
                .add()
                .add("At the very least here's some information about what's going to be directly spawned without an event:");
            printer.add("Entities:");
            for (final Entity entity : entities) {
                printer.add(" - " + entity);
            }
            printer.add("PhaseContext:");
            context.printCustom(printer, 4);
            printer.add();
            printer.add("Exception:");
            printer.add(e);
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
            for (final Entity entity : entities) {
                EntityUtil.processEntitySpawn(entity, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
            }
            return true;
        }
    }

    public static boolean callSpawnEntityCustom(final List<Entity> entities, final PhaseContext<?> context) {
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(PhaseTracker.getCauseStackManager().getCurrentCause(), entities);
        SpongeCommon.postEvent(event);
        return event.isCancelled() && EntityUtil.processEntitySpawnsFromEvent(context, event);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends net.minecraft.entity.Entity> CollideEntityEvent callCollideEntityEvent(
            final net.minecraft.world.World world, @Nullable final net.minecraft.entity.Entity sourceEntity,
            final List<T> entities) {

        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseContext<?> currentContext = phaseTracker.getPhaseContext();
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            if (sourceEntity != null) {
                // We only want to push the source entity if it's not the current entity being ticked or "sourced". They will be already pushed.
                if (currentContext.getSource() != sourceEntity) {
                    frame.pushCause(sourceEntity);
                }
            } else {
                // If there is no source, then... well... find one and push it.
                final Object source = currentContext.getSource();
                if (source instanceof LocatableBlock) {
                    frame.pushCause(source);
                } else if (source instanceof BlockEntity) {
                    frame.pushCause(source);
                } else if (source instanceof Entity) {
                    frame.pushCause(source);
                }
            }
            currentContext.addCreatorAndNotifierToCauseStack(frame);

            final List<Entity> spEntities = (List<Entity>) (List<?>) entities;
            final CollideEntityEvent event =
                    SpongeEventFactory.createCollideEntityEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), spEntities);
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(final ServerWorldBridge worldIn, final BlockPos pos) {

        return callChangeBlockEventPre(worldIn, ImmutableList.of(
            ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) worldIn, pos.getX(), pos.getY(), pos.getZ())), null);
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(final ServerWorldBridge worldIn, final BlockPos pos, final Object source) {
        return callChangeBlockEventPre(worldIn, ImmutableList.of(
            ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) worldIn, pos.getX(), pos.getY(), pos.getZ())), source);
    }

    /**
     * Processes pre block event data then fires event.
     *
     * @param worldIn The world
     * @param locations The locations affected
     * @param source The source of event
     * @return The event
     */
    @SuppressWarnings("unchecked") private static ChangeBlockEvent.Pre callChangeBlockEventPre(final ServerWorldBridge worldIn, final ImmutableList<ServerLocation> locations, @Nullable Object source) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getPhaseContext();
            if (source == null) {
                source = phaseContext.getSource() == null ? worldIn : phaseContext.getSource();
            }

            // TODO - All of this bit should be nuked since PhaseContext has lazy initializing frames.
            PlayerEntity player = null;
            frame.pushCause(source);
            if (source instanceof Player) {
                player = (PlayerEntity) source;
                if (SpongeImplHooks.isFakePlayer(player)) {
                    frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
                }
            }

            final User creator = phaseContext.getCreator().orElse((User) player);
            if (creator != null) {
                frame.addContext(EventContextKeys.CREATOR, creator);
            }

            if (!((IPhaseState) phaseContext.state).shouldProvideModifiers(phaseContext)) {
                phaseContext.getSource(BlockBridge.class).ifPresent(bridge -> {
                    bridge.bridge$getTickFrameModifier().accept(frame, worldIn);
                });
            }

            phaseContext.applyNotifierIfAvailable(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));

            final ChangeBlockEvent.Pre event =
                SpongeEventFactory.createChangeBlockEventPre(frame.getCurrentCause(), locations);
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent.Modify callChangeBlockEventModifyLiquidMix(
        final net.minecraft.world.World worldIn, final BlockPos pos, final net.minecraft.block.BlockState state, @Nullable Object source) {

        final BlockState fromState = (BlockState) worldIn.getBlockState(pos);
        final BlockState toState = (BlockState) state;
        boolean pushSource = false;
        if (source == null) {
            // If source is null the source is the block itself
            pushSource = true;
            source = new SpongeLocatableBlockBuilder().state(fromState).world((org.spongepowered.api.world.server.ServerWorld) worldIn).position(pos.getX(), pos.getY(), pos.getZ()).build();
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            if (!pushSource) {
                frame.pushCause(source);
            }
            frame.addContext(EventContextKeys.LIQUID_MIX, (org.spongepowered.api.world.server.ServerWorld) worldIn);

            final WorldProperties world = ((org.spongepowered.api.world.server.ServerWorld) worldIn).getProperties();
            final Vector3i position = new Vector3i(pos.getX(), pos.getY(), pos.getZ());

            final Transaction<BlockSnapshot> transaction = new Transaction<>(BlockSnapshot.builder().blockState(fromState).world(world).position(position).build(),
                                                                       BlockSnapshot.builder().blockState(toState).world(world).position(position).build());
            final ChangeBlockEvent.Modify event = SpongeEventFactory.createChangeBlockEventModify(frame.getCurrentCause(),
                    Collections.singletonList(transaction));

            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent.Break callChangeBlockEventModifyLiquidBreak(
        final net.minecraft.world.World worldIn, final BlockPos pos, final net.minecraft.block.BlockState targetState) {
        return callChangeBlockEventModifyLiquidBreak(worldIn, pos, worldIn.getBlockState(pos), targetState);
    }

    public static ChangeBlockEvent.Break callChangeBlockEventModifyLiquidBreak(
        final net.minecraft.world.World worldIn, final BlockPos pos, final net.minecraft.block.BlockState fromState, final net.minecraft.block.BlockState toState) {
        final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
        Object source =context.getSource(LocatableBlock.class).orElse(null);
        if (source == null) {
            source = worldIn; // Fallback
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            frame.addContext(EventContextKeys.LIQUID_BREAK, (org.spongepowered.api.world.server.ServerWorld) worldIn);

            final WorldProperties world = ((org.spongepowered.api.world.server.ServerWorld) worldIn).getProperties();
            final Vector3i position = new Vector3i(pos.getX(), pos.getY(), pos.getZ());

            final SpongeBlockSnapshot from = SpongeBlockSnapshotBuilder.pooled().blockState(fromState).world(world).position(position).build();
            final SpongeBlockSnapshot to = SpongeBlockSnapshotBuilder.pooled().blockState(toState).world(world).position(position).build();
            final Transaction<BlockSnapshot> transaction = new Transaction<>(from, to);
            final ChangeBlockEvent.Break event = SpongeEventFactory.createChangeBlockEventBreak(frame.getCurrentCause(),
                Collections.singletonList(transaction));

            SpongeCommon.postEvent(event);
            return event;
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(final World world, final BlockPos sourcePos, final EnumSet<net.minecraft.util.Direction> notifiedSides) {
        final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
        // Don't fire notify events during world gen or while restoring
        if (context.state.isWorldGeneration() || context.state.isRestoring()) {
            return null;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final BlockState blockstate = (BlockState) ((net.minecraft.world.World) world).getBlockState(sourcePos);
            final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world((org.spongepowered.api.world.server.ServerWorld) world).position(sourcePos.getX(), sourcePos.getY(),
                    sourcePos.getZ())
                    .state(blockstate)
                    .build();
            if (context.getNotifier().isPresent()) {
                context.addCreatorAndNotifierToCauseStack(frame);
            } else {

                final ChunkBridge mixinChunk = (ChunkBridge) ((ServerWorld) world).getChunkAt(sourcePos);
                mixinChunk.bridge$getBlockCreator(sourcePos).ifPresent(creator -> frame.addContext(EventContextKeys.CREATOR, creator));
                mixinChunk.bridge$getBlockNotifier(sourcePos).ifPresent(user -> frame.addContext(EventContextKeys.NOTIFIER, user));
            }
            PhaseTracker.getCauseStackManager().pushCause(locatable);

            final Map<Direction, BlockState> neighbors = new HashMap<>();
            for (final net.minecraft.util.Direction notificationSide : notifiedSides) {
                final BlockPos offset = sourcePos.offset(notificationSide);
                final Direction direction = DirectionFacingProvider.getInstance().getKey(notificationSide).get();
                final net.minecraft.block.BlockState notificationState = ((ServerWorld) world).getBlockState(offset);
                neighbors.put(direction, (BlockState) notificationState);
            }

            final NotifyNeighborBlockEvent event =
                    SpongeEventFactory.createNotifyNeighborBlockEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), neighbors, neighbors);
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractEntityEvent.Primary callInteractEntityEventPrimary(final ServerPlayerEntity player, final ItemStack stack, final net.minecraft.entity.Entity entity, final Hand
            hand, @Nullable final Vector3d hitVec) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.CREATOR, (User) player);
            frame.addContext(EventContextKeys.NOTIFIER, (User) player);
            frame.addContext(EventContextKeys.ENTITY_HIT, ((Entity) entity));
            if (!stack.isEmpty()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(stack));
            }
            final InteractEntityEvent.Primary event = SpongeEventFactory.createInteractEntityEventPrimary(frame.getCurrentCause(), (Entity) entity,
                    Optional.ofNullable(hitVec));
            if (entity instanceof Player && !((org.spongepowered.api.world.server.ServerWorld) player.world).getProperties().isPVPEnabled()) {
                event.setCancelled(true); // if PvP is disabled for world, cancel
            }
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractEntityEvent.Secondary callInteractEntityEventSecondary(final ServerPlayerEntity player, final ItemStack stack, final net.minecraft.entity.Entity entity,
            final Hand hand, @Nullable final Vector3d hitVec) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.CREATOR, (User) player);
            frame.addContext(EventContextKeys.NOTIFIER, (User) player);
            frame.addContext(EventContextKeys.ENTITY_HIT, (Entity) entity);
            if (!stack.isEmpty()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(stack));
            }
            final InteractEntityEvent.Secondary event = SpongeEventFactory.createInteractEntityEventSecondary(frame.getCurrentCause(),
                    (Entity) entity, Optional.ofNullable(hitVec));
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractItemEvent.Primary callInteractItemEventPrimary(final PlayerEntity player, final ItemStack stack, final Hand hand,
        @Nullable final Vector3d hitVec, final Object hitTarget) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            if (SpongeImplHooks.isFakePlayer(player)) {
                frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
            } else {
                frame.pushCause(player);
                frame.addContext(EventContextKeys.CREATOR, (User) player);
                frame.addContext(EventContextKeys.NOTIFIER, (User) player);
            }

            if (hitTarget instanceof Entity) {
                frame.addContext(EventContextKeys.ENTITY_HIT, ((Entity) hitTarget));
            } else if (hitTarget instanceof BlockSnapshot) {
                frame.addContext(EventContextKeys.BLOCK_HIT, (BlockSnapshot) hitTarget);
            }
            if (!stack.isEmpty()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(stack));
            }
            final HandType handType = (HandType) (Object) hand;
            frame.addContext(EventContextKeys.USED_HAND, handType);
            final InteractItemEvent.Primary event = SpongeEventFactory.createInteractItemEventPrimary(frame.getCurrentCause(),
                    Optional.ofNullable(hitVec), ItemStackUtil.snapshotOf(stack));
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractItemEvent.Secondary callInteractItemEventSecondary(final CauseStackManager.StackFrame frame, final PlayerEntity player,
        final ItemStack stack, final Hand hand,
        @Nullable final Vector3d hitVec, final Object hitTarget) {
        if (SpongeImplHooks.isFakePlayer(player)) {
            frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
        } else {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.CREATOR, (User) player);
            frame.addContext(EventContextKeys.NOTIFIER, (User) player);
        }

        if (hitTarget instanceof Entity) {
            frame.addContext(EventContextKeys.ENTITY_HIT, ((Entity) hitTarget));
        } else if (hitTarget instanceof BlockSnapshot) {
            frame.addContext(EventContextKeys.BLOCK_HIT, (BlockSnapshot) hitTarget);
        }
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
        if (!stack.isEmpty()) {
            frame.addContext(EventContextKeys.USED_ITEM, snapshot);
        }
        final HandType handType = (HandType) (Object) hand;
        frame.addContext(EventContextKeys.USED_HAND, handType);
        final InteractItemEvent.Secondary event = SpongeEventFactory.createInteractItemEventSecondary(frame.getCurrentCause(),
                Optional.ofNullable(hitVec), snapshot);
        SpongeCommon.postEvent(event);
        return event;

    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(
        final PlayerEntity player, final ItemStack heldItem, final Hand hand, @Nullable final Vector3d hitVec) {
        return callInteractBlockEventPrimary(player, heldItem, BlockSnapshot.empty(), hand, null, hitVec);
    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(final PlayerEntity player, final ItemStack heldItem, final BlockSnapshot blockSnapshot, final Hand hand,
            @Nullable final net.minecraft.util.Direction side, @Nullable final Vector3d hitVec) {
        final HandType handType = (HandType) (Object) hand;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            if (SpongeImplHooks.isFakePlayer(player)) {
                frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
            } else {
                frame.pushCause(player);
                frame.addContext(EventContextKeys.CREATOR, (User) player);
                frame.addContext(EventContextKeys.NOTIFIER, (User) player);
            }

            frame.addContext(EventContextKeys.BLOCK_HIT, blockSnapshot);
            frame.addContext(EventContextKeys.USED_HAND, handType);
            final Direction direction;
            if (side != null) {
                direction = DirectionFacingProvider.getInstance().getKey(side).get();
            } else {
                direction = Direction.NONE;
            }
            if (!heldItem.isEmpty()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
            }
            final InteractBlockEvent.Primary event = SpongeEventFactory.createInteractBlockEventPrimary(frame.getCurrentCause(), blockSnapshot,
                    Optional.ofNullable(hitVec), direction);
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractBlockEvent.Secondary createInteractBlockEventSecondary(
        final PlayerEntity player, final ItemStack heldItem, @Nullable final Vector3d hitVec,
            final BlockSnapshot targetBlock, final Direction targetSide, final Hand hand) {
        return createInteractBlockEventSecondary(player, heldItem, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED,
                hitVec, targetBlock, targetSide, hand);
    }

    public static InteractBlockEvent.Secondary createInteractBlockEventSecondary(final PlayerEntity player, final ItemStack heldItem, final Tristate originalUseBlockResult, final Tristate useBlockResult,
            final Tristate originalUseItemResult, final Tristate useItemResult, @Nullable final Vector3d hitVec, final BlockSnapshot targetBlock,
            final Direction targetSide, final Hand hand) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            if (SpongeImplHooks.isFakePlayer(player)) {
                frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
            } else {
                frame.pushCause(player);
                frame.addContext(EventContextKeys.CREATOR, (User) player);
                frame.addContext(EventContextKeys.NOTIFIER, (User) player);
            }

            frame.addContext(EventContextKeys.BLOCK_HIT, targetBlock);
            if (!heldItem.isEmpty()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
            }
            final HandType handType = (HandType) (Object) hand;
            frame.addContext(EventContextKeys.USED_HAND, handType);
            final InteractBlockEvent.Secondary event = SpongeEventFactory.createInteractBlockEventSecondary(frame.getCurrentCause(),
                    originalUseBlockResult, useBlockResult, originalUseItemResult, useItemResult, targetBlock, Optional.ofNullable(hitVec),
                    targetSide);
            return event;
        }
    }

    /**
     * Performs the logic necessary to post the {@link MoveEntityEvent position event} for an {@link Entity}.
     *
     * @param entity The event
     */
    public static void callNaturalMoveEntityEvent(final net.minecraft.entity.Entity entity) {
        if (entity.removed || (!ShouldFire.MOVE_ENTITY_EVENT)) {
            return;
        }

        final double deltaX = entity.prevPosX - entity.getPosX();
        final double deltaY = entity.prevPosY - entity.getPosY();
        final double deltaZ = entity.prevPosZ - entity.getPosZ();
        final double deltaChange = Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2);
        if (deltaChange < 1f / 256) {
            return;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity);
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.NATURAL);

            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(), (Entity) entity,
                    new Vector3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ), new Vector3d(entity.getPosX(), entity.getPosY(), entity.getPosZ()),
                    new Vector3d(entity.getPosX(), entity.getPosY(), entity.getPosZ()));

            if (SpongeCommon.postEvent(event)) {
                entity.setPosition(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
            } else {
                entity.setPosition(event.getDestinationPosition().getX(), event.getDestinationPosition().getY(),
                        event.getDestinationPosition().getZ());
            }
        }
    }

    /**
     * Performs the logic necessary to post the {@link RotateEntityEvent rotation event} for an {@link Entity}.
     *
     * @param entity The event
     */
    public static void callNaturalRotateEntityEvent(final net.minecraft.entity.Entity entity) {
        if (entity.removed || !ShouldFire.ROTATE_ENTITY_EVENT || (entity.rotationPitch == entity.prevRotationPitch && entity.rotationYaw == entity.prevRotationYaw)) {
            return;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity);

            final RotateEntityEvent event = SpongeEventFactory.createRotateEntityEvent(frame.getCurrentCause(), (Entity) entity,
                    new Vector3d(entity.prevRotationPitch, entity.prevRotationYaw, 0), new Vector3d(entity.rotationPitch,
                    entity.rotationYaw, 0));

            if (SpongeCommon.postEvent(event)) {
                entity.rotationPitch = entity.prevRotationPitch;
                entity.rotationYaw = entity.prevRotationYaw;
            } else {
                entity.rotationPitch = (float) event.getToRotation().getX();
                entity.rotationYaw = (float) event.getToRotation().getY();
            }
        }
    }

    public static Optional<DestructEntityEvent.Death> callDestructEntityEventDeath(final LivingEntity entity, @Nullable final DamageSource source, final boolean isMainThread) {
        final Audience originalChannel;
        final Audience channel;
        final Component originalMessage;
        Optional<User> sourceCreator = Optional.empty();
        final boolean messageCancelled = false;

        if (entity instanceof ServerPlayerEntity) {
            originalChannel = channel = (Player) entity;
        } else {
            originalChannel = Audience.empty();
            channel = Audience.empty();
        }
        if (source instanceof EntityDamageSource) {
            final EntityDamageSource damageSource = (EntityDamageSource) source;
            if (damageSource.getImmediateSource() instanceof CreatorTrackedBridge) {
                final CreatorTrackedBridge creatorBridge = (CreatorTrackedBridge) damageSource.getImmediateSource();
                if (creatorBridge != null) {
                    sourceCreator = creatorBridge.tracked$getCreatorReference();
                }
            }
        }

        originalMessage = SpongeAdventure.asAdventure(entity.getCombatTracker().getDeathMessage());
        // Try-with-resources will not produce an NPE when trying to autoclose the frame if it is null. Client sided
        // checks need to be made here since entities can die on the client world.
        try (final CauseStackManager.StackFrame frame = isMainThread ? PhaseTracker.getCauseStackManager().pushCauseFrame() : null) {
            if (isMainThread) {
                if (source != null) {
                    frame.pushCause(source);
                }
                if (sourceCreator.isPresent()) {
                    frame.addContext(EventContextKeys.CREATOR, sourceCreator.get());
                }
            }

            final Cause cause = isMainThread ? PhaseTracker.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), source == null ? entity : source);
            final DestructEntityEvent.Death event = SpongeEventFactory.createDestructEntityEventDeath(cause,
                originalChannel, Optional.of(channel), originalMessage, originalMessage, (Living) entity,
                entity.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY), messageCancelled);
            SpongeCommon.postEvent(event, true); // Client code should be able to cancel the death event if server cancels it.
            final Component message = event.getMessage();
            // Check the event isn't cancelled either. If it is, then don't spawn the message.
            if (!event.isCancelled() && !event.isMessageCancelled() && message != TextComponent.empty()) {
                event.getAudience().ifPresent(eventChannel -> eventChannel.sendMessage(message));
            }
            return Optional.of(event);
        }
    }

    public static boolean handleCollideBlockEvent(final Block block, final net.minecraft.world.World world, final BlockPos pos, final net.minecraft.block.BlockState state, final net.minecraft.entity.Entity entity, final Direction direction) {
        if (pos.getY() <= 0) {
            return false;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause( entity);

            if (entity instanceof CreatorTrackedBridge) {
                final CreatorTrackedBridge spongeEntity = (CreatorTrackedBridge) entity;
                spongeEntity.tracked$getCreatorReference().ifPresent(user -> frame.addContext(EventContextKeys.CREATOR, user));
            }

            // TODO: Add target side support
            final CollideBlockEvent event = SpongeEventFactory.createCollideBlockEvent(frame.getCurrentCause(), (BlockState) state,
                    ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) world, VecHelper.toVector3d(pos)), direction);
            final boolean cancelled = SpongeCommon.postEvent(event);
            if (!cancelled) {
                final EntityBridge spongeEntity = (EntityBridge) entity;
                if (!pos.equals(spongeEntity.bridge$getLastCollidedBlockPos())) {
                    final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
                    context.applyNotifierIfAvailable(notifier -> {
                        ChunkBridge spongeChunk = ((ActiveChunkReferantBridge) entity).bridge$getActiveChunk();
                        if (spongeChunk == null) {
                            spongeChunk = (ChunkBridge) world.getChunkAt(pos);
                        }
                        spongeChunk.bridge$addTrackedBlockPosition(block, pos, notifier, PlayerTracker.Type.NOTIFIER);

                    });
                }
            }
            return cancelled;
        }
    }

    public static boolean handleCollideImpactEvent(final net.minecraft.entity.Entity projectile, @Nullable final ProjectileSource projectileSource,
            final RayTraceResult movingObjectPosition) {
        final RayTraceResult.Type movingObjectType = movingObjectPosition.getType();
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(projectile);
            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, projectileSource == null
                    ? UnknownProjectileSource.UNKNOWN
                    : projectileSource);
            final Optional<User> creator = PhaseTracker.getInstance().getPhaseContext().getCreator();
            creator.ifPresent(user -> frame.addContext(EventContextKeys.CREATOR, user));

            final ServerLocation impactPoint = ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) projectile.world, VecHelper.toVector3d(movingObjectPosition.getHitVec()));
            boolean cancelled = false;

            if (movingObjectType == RayTraceResult.Type.BLOCK) {
                final BlockRayTraceResult blockMovingObjectPosition = (BlockRayTraceResult) movingObjectPosition;
                final BlockPos blockPos = blockMovingObjectPosition.getPos();
                if (blockPos.getY() <= 0) {
                    return false;
                }

                final BlockSnapshot targetBlock = ((World) projectile.world).createSnapshot(VecHelper.toVector3i(blockMovingObjectPosition.getPos()));
                Direction side = Direction.NONE;
                if (blockMovingObjectPosition.getFace() != null) {
                    side = DirectionFacingProvider.getInstance().getKey(blockMovingObjectPosition.getFace()).get();
                }

                final CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(frame.getCurrentCause(),
                        impactPoint, targetBlock.getState(),
                        targetBlock.getLocation().get(), side);
                cancelled = SpongeCommon.postEvent(event);
                // Track impact block if event is not cancelled
                if (!cancelled && creator.isPresent()) {
                    final BlockPos targetPos = VecHelper.toBlockPos(impactPoint.getBlockPosition());
                    final ChunkBridge spongeChunk = (ChunkBridge) projectile.world.getChunkAt(targetPos);
                    spongeChunk.bridge$addTrackedBlockPosition((Block) targetBlock.getState().getType(), targetPos, creator.get(), PlayerTracker.Type.NOTIFIER);
                }
            } else if (movingObjectType == RayTraceResult.Type.ENTITY) { // entity
                final EntityRayTraceResult entityMovingObjectPosition = (EntityRayTraceResult) movingObjectPosition;
                final ArrayList<Entity> entityList = new ArrayList<>();
                entityList.add((Entity) entityMovingObjectPosition.getEntity());
                final CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(frame.getCurrentCause(), entityList, impactPoint);
                        cancelled = SpongeCommon.postEvent(event);
            }

            if (cancelled) {
                // Entities such as EnderPearls call setDead during onImpact. However, if the event is cancelled
                // setDead will never be called resulting in a bad state such as falling through world.
                projectile.remove();
            }
            return cancelled;
        }
    }

    public static InteractContainerEvent.Close callInteractInventoryCloseEvent(final Container container, final ServerPlayerEntity player,
            final ItemStackSnapshot lastCursor, final ItemStackSnapshot newCursor, final boolean clientSource) {
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        final InteractContainerEvent.Close event =
                SpongeEventFactory.createInteractContainerEventClose(PhaseTracker.getCauseStackManager().getCurrentCause(), ContainerUtil.fromNative(container), cursorTransaction);
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            if (clientSource && container.getSlot(0) != null) {
                if (!(container instanceof PlayerContainer)) {
                    // Inventory closed by client, reopen window and send container
                    player.openContainer = container;
                    final Slot slot = container.getSlot(0);
                    final IInventory slotInventory = slot.inventory;
                    ITextComponent title;
                    // TODO get name from last open
                    if (slotInventory instanceof INamedContainerProvider) {
                        title = ((INamedContainerProvider) slotInventory).getDisplayName();
                    } else {
                        // expected fallback for unknown types
                        title = null;
                    }
                    slotInventory.openInventory(player);
                    player.connection.sendPacket(new SOpenWindowPacket(container.windowId, container.getType(), title));
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
            final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) player.openContainer;
            mixinContainer.bridge$getCapturedSlotTransactions().clear();
            mixinContainer.bridge$setCaptureInventory(false);
            // Handle cursor
            if (!event.getCursorTransaction().isValid()) {
                handleCustomCursor(player, event.getCursorTransaction().getOriginal());
            } else if (event.getCursorTransaction().getCustom().isPresent()) {
                handleCustomCursor(player, event.getCursorTransaction().getFinal());
            }
            if (!clientSource && player.openContainer != null && player.connection != null) {
                player.closeScreen();
            }
        }

        return event;
    }

    public static SetAITargetEvent callSetAttackTargetEvent(@Nullable final Entity target, final Agent agent) {
        final SetAITargetEvent event = SpongeEventFactory.createSetAITargetEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), agent, Optional.ofNullable(target));
        SpongeCommon.postEvent(event);
        return event;
    }

    @SuppressWarnings("unused")
    public static void callPostPlayerRespawnEvent(final ServerPlayerEntity playerMP, final boolean conqueredEnd) {
        // We overwrite this method in SpongeForge, in order to fire
        // Forge's PlayerRespawnEvent
    }

    public static Optional<net.minecraft.world.Explosion> detonateExplosive(final ExplosiveBridge explosiveBridge, final Explosion.Builder builder) {
        final DetonateExplosiveEvent event = SpongeEventFactory.createDetonateExplosiveEvent(
                PhaseTracker.getCauseStackManager().getCurrentCause(), builder, (FusedExplosive) explosiveBridge, builder.build()
        );
        if (!Sponge.getEventManager().post(event)) {
            final Explosion explosion = event.getExplosionBuilder().build();
            if (explosion.getRadius() > 0) {
                ((ServerWorldBridge) ((Explosive) explosiveBridge).getWorld())
                    .bridge$triggerInternalExplosion(explosion,
                        e -> GeneralPhase.State.EXPLOSION.createPhaseContext(PhaseTracker.SERVER).explosion(e));
            }
            return Optional.of((net.minecraft.world.Explosion) explosion);
        }
        return Optional.empty();
    }

    /**
     * @author gabizou - April 19th, 2018
     * Creates two events here:
     * - {@link DropItemEvent}
     * - {@link ConstructEntityEvent}
     *
     * This is to reduce the code size from normal entity drops and player drops.
     * While player drops usually require performing position and motion modifications,
     * we return the item stack if it is to be thrown (this allows the event to have a
     * say in what item is dropped).
     *
     * @param entity The entity throwing the item
     * @param posX The position x for the item stack to spawn
     * @param posY The position y for the item stack to spawn
     * @param posZ The position z for the item stack to spawn
     * @param snapshot The item snapshot of the item to drop
     * @param original The original list to be used
     * @param frame
     * @return The item if it is to be spawned, null if to be ignored
     */
    @Nullable
    public static ItemStack throwDropItemAndConstructEvent(final net.minecraft.entity.Entity entity, final double posX, final double posY,
        final double posZ, final ItemStackSnapshot snapshot, final List<ItemStackSnapshot> original, final CauseStackManager.StackFrame frame) {
        final PlayerEntityBridge mixinPlayer;
        if (entity instanceof PlayerEntityBridge) {
            mixinPlayer = (PlayerEntityBridge) entity;
        } else {
            mixinPlayer = null;
        }
        final ItemStack item;

        frame.pushCause(entity);

        // FIRST we want to throw the DropItemEvent.PRE
        final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(frame.getCurrentCause(),
            ImmutableList.of(snapshot), original);
        SpongeCommon.postEvent(dropEvent);
        if (dropEvent.isCancelled()) {
            if (mixinPlayer != null) {
                mixinPlayer.bridge$shouldRestoreInventory(true);
            }
            return null;
        }
        if (dropEvent.getDroppedItems().isEmpty()) {
            return null;
        }

        // SECOND throw the ConstructEntityEvent
        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
        final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) entity.world, posX, posY, posZ), new Vector3d(0, 0, 0), EntityTypes.ITEM.get());
        frame.removeContext(EventContextKeys.SPAWN_TYPE);
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            // Make sure the player is restoring inventories
            if (mixinPlayer != null) {
                mixinPlayer.bridge$shouldRestoreInventory(true);
            }
            return null;
        }

        item = event.isCancelled() ? null : ItemStackUtil.fromSnapshotToNative(dropEvent.getDroppedItems().get(0));
        if (item == null) {
            // Make sure the player is restoring inventories
            if (mixinPlayer != null) {
                mixinPlayer.bridge$shouldRestoreInventory(true);
            }
            return null;
        }
        return item;
    }

    @Nullable
    public static PlaySoundEvent.Broadcast callPlaySoundBroadcastEvent(final CauseStackManager.StackFrame frame, final WorldBridge bridge,
        final BlockPos pos, final int effectID) {
        final Supplier<SoundType> soundType;
        final float volume;
        if (effectID == Constants.WorldEvents.PLAY_WITHER_SPAWN_EVENT) {
            soundType = SoundTypes.ENTITY_WITHER_SPAWN;
            volume = 1.0F;
        } else if (effectID == Constants.WorldEvents.PLAY_ENDERDRAGON_DEATH_EVENT) {
            soundType = SoundTypes.ENTITY_ENDER_DRAGON_DEATH;
            volume = 5.0F;
        } else if (effectID == Constants.WorldEvents.PLAY_BLOCK_END_PORTAL_SPAWN_EVENT) {
            soundType = SoundTypes.BLOCK_END_PORTAL_SPAWN;
            volume = 1.0F;
        } else {
            return null;
        }
        final ServerLocation location = ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) bridge, pos.getX(), pos.getY(), pos.getZ());
        final PlaySoundEvent.Broadcast event = SpongeEventFactory.createPlaySoundEventBroadcast(frame.getCurrentCause(), location,
            Sound.Source.HOSTILE, soundType.get(), 1.0F, volume);
        SpongeCommon.postEvent(event);
        return event;
    }

    public static PlaySoundEvent.Record callPlaySoundRecordEvent(final Cause cause, final JukeboxTileEntity jukebox,
        final MusicDisc recordType, final int data) {
        final Jukebox apiJuke = (Jukebox) jukebox;
        final ServerLocation location = (ServerLocation) apiJuke.getLocation();
        final PlaySoundEvent.Record
            event =
            data == 0 ? SpongeEventFactory
                .createPlaySoundEventRecordStart(cause, apiJuke, location, recordType, Sound.Source.RECORD, recordType.getSound(), 1.0F, 4.0F)
                      : SpongeEventFactory
                .createPlaySoundEventRecordStop(cause, apiJuke, location, recordType, Sound.Source.RECORD, recordType.getSound(), 1.0F, 4.0F);
        SpongeCommon.postEvent(event);
        return event;
    }

    @SuppressWarnings("ConstantConditions")
    public static PlaySoundEvent.AtEntity callPlaySoundAtEntityEvent(final Cause cause, @Nullable final PlayerEntity entity,
        final WorldBridge worldMixin, final double x, final double y, final double z, final net.minecraft.util.SoundCategory category,
        final SoundEvent name, final float pitch, final float volume) {
        final ServerLocation location = ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) worldMixin, x, y, z);
        final PlaySoundEvent.AtEntity event = SpongeEventFactory.createPlaySoundEventAtEntity(cause, location, Optional.ofNullable(entity),
            SpongeAdventure.asAdventure(category), (SoundType) name, pitch, volume);
        SpongeCommon.postEvent(event);
        return event;
    }

    public static PlaySoundEvent.NoteBlock callPlaySoundNoteBlockEvent(final Cause cause, final World world, final BlockPos pos, final SoundEvent soundEvent, final InstrumentType instrument, final NotePitch notePitch, final Float pitch) {
        final ServerLocation location = ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) world, pos.getX(), pos.getY(), pos.getZ());
        final PlaySoundEvent.NoteBlock event = SpongeEventFactory.createPlaySoundEventNoteBlock(cause, instrument, location, notePitch, Sound.Source.RECORD, (SoundType)soundEvent, pitch, 3.0F);
        SpongeCommon.postEvent(event);
        return event;
    }
}
