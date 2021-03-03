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
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.Jukebox;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operations;
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
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
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
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.entity.projectile.UnknownProjectileSource;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeLocatableBlockBuilder;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class SpongeCommonEventFactory {

    public static int lastAnimationPacketTick = 0;
    // For animation packet
    public static int lastSecondaryPacketTick = 0;
    public static int lastPrimaryPacketTick = 0;
    @Nullable public static WeakReference<net.minecraft.server.level.ServerPlayer> lastAnimationPlayer;

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

    public static void callDropItemDrop(final net.minecraft.server.level.ServerPlayer player, final List<ItemEntity> items,
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
                        ((org.spongepowered.api.item.inventory.Container) player.containerMenu), cursorTransaction, entities,
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
    public static <T extends net.minecraft.world.entity.Entity> CollideEntityEvent callCollideEntityEvent(
            final net.minecraft.world.level.Level world, @Nullable final net.minecraft.world.entity.Entity sourceEntity,
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

    /**
     * This simulates the blocks a piston moves and calls the event for saner
     * debugging.
     *
     * @return if the event was cancelled
     */
    public static boolean handlePistonEvent(
        final TrackedWorldBridge world, final BlockPos pos, final net.minecraft.world.level.block.state.BlockState blockstate, final int eventId
    ) {
        final boolean extending = (eventId == 0);
        final net.minecraft.core.Direction direction = blockstate.getValue(DirectionalBlock.FACING);
        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world((org.spongepowered.api.world.server.ServerWorld) world).state((BlockState) blockstate).position(pos.getX(), pos.getY(), pos.getZ()).build();

        // Sets toss out duplicate values (even though there shouldn't be any)
        final HashSet<ServerLocation> locations = new HashSet<>();
        locations.add(ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) world, pos.getX(), pos.getY(), pos.getZ()));

        final PistonStructureResolver movedBlocks = new PistonStructureResolver((ServerLevel) world, pos, direction, extending);
        movedBlocks.resolve(); // calculates blocks to be moved

        Stream.concat(movedBlocks.getToPush().stream(), movedBlocks.getToDestroy().stream())
            .map(block -> ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) world, block.getX(), block.getY(), block.getZ()))
            .collect(Collectors.toCollection(() -> locations)); // SUPER
        // efficient
        // code!

        // If the piston is extending and there are no blocks to destroy, add the offset location for protection purposes
        if (extending && movedBlocks.getToDestroy().isEmpty()) {
            final List<BlockPos> movedPositions = movedBlocks.getToPush();
            final BlockPos offsetPos;
            // If there are no blocks to move, add the offset of piston
            if (movedPositions.isEmpty()) {
                offsetPos = pos.relative(direction);
            } else {
                // Add the offset of last block set to move
                offsetPos = movedPositions.get(movedPositions.size() - 1).relative(direction);
            }
            locations.add(ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) world, offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()));
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            if (extending) {
                frame.addContext(EventContextKeys.PISTON_EXTEND, (org.spongepowered.api.world.server.ServerWorld) world);
            } else {
                frame.addContext(EventContextKeys.PISTON_RETRACT, (org.spongepowered.api.world.server.ServerWorld) world);
            }
            return SpongeCommonEventFactory.callChangeBlockEventPre((ServerLevelBridge) world, ImmutableList.copyOf(locations), locatable)
                .isCancelled();
        }
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(final ServerLevelBridge worldIn, final BlockPos pos) {

        return SpongeCommonEventFactory.callChangeBlockEventPre(worldIn, ImmutableList.of(
            ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) worldIn, pos.getX(), pos.getY(), pos.getZ())), null);
    }

    public static ChangeBlockEvent.Pre callChangeBlockEventPre(final ServerLevelBridge worldIn, final BlockPos pos, final Object source) {
        return SpongeCommonEventFactory.callChangeBlockEventPre(worldIn, ImmutableList.of(
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ChangeBlockEvent.Pre callChangeBlockEventPre(final ServerLevelBridge worldIn, final ImmutableList<ServerLocation> locations, @Nullable Object source) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getPhaseContext();
            if (source == null) {
                source = phaseContext.getSource() == null ? worldIn : phaseContext.getSource();
            }

            // TODO - All of this bit should be nuked since PhaseContext has lazy initializing frames.
            net.minecraft.world.entity.player.Player player = null;
            frame.pushCause(source);
            if (source instanceof Player) {
                player = (net.minecraft.world.entity.player.Player) source;
                if (((PlatformEntityBridge) player).bridge$isFakePlayer()) {
                    frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
                }
            }
            if (phaseContext.getCreator().isPresent()) {
                phaseContext.getCreator().ifPresent(creator -> frame.addContext(EventContextKeys.CREATOR, creator));
            } else if (player instanceof ServerPlayerBridge) {
                final @Nullable User user = ((ServerPlayerBridge) player).bridge$getUser();
                if (user != null) {
                    frame.addContext(EventContextKeys.CREATOR, user);
                }
            }

            if (!phaseContext.shouldProvideModifiers()) {
                phaseContext.getSource(BlockBridge.class).ifPresent(bridge -> {
                    bridge.bridge$getTickFrameModifier().accept(frame, worldIn);
                });
            }

            phaseContext.applyNotifierIfAvailable(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));

            final ChangeBlockEvent.Pre event =
                SpongeEventFactory.createChangeBlockEventPre(frame.getCurrentCause(), locations,
                    (org.spongepowered.api.world.server.ServerWorld) worldIn
                );
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent callChangeBlockEventModifyLiquidMix(
        final net.minecraft.world.level.Level worldIn, final BlockPos pos, final net.minecraft.world.level.block.state.BlockState state, @Nullable Object source) {

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

            final ServerLocation location = ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) worldIn, position);
            final ChangeBlockEvent event = SpongeEventFactory.createChangeBlockEventPre(frame.getCurrentCause(),
                    Collections.singletonList(location), ((org.spongepowered.api.world.server.ServerWorld) worldIn));

            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static ChangeBlockEvent callChangeBlockEventModifyLiquidBreak(
        final net.minecraft.world.level.Level worldIn, final BlockPos pos, final net.minecraft.world.level.block.state.BlockState targetState) {
        return SpongeCommonEventFactory.callChangeBlockEventModifyLiquidBreak(worldIn, pos, worldIn.getBlockState(pos), targetState);
    }

    public static ChangeBlockEvent callChangeBlockEventModifyLiquidBreak(
        final net.minecraft.world.level.Level worldIn, final BlockPos pos, final net.minecraft.world.level.block.state.BlockState fromState, final net.minecraft.world.level.block.state.BlockState toState) {
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

            final SpongeBlockSnapshot from = SpongeBlockSnapshotBuilder.pooled().blockState(fromState).world((ServerLevel) worldIn).position(position).build();
            final SpongeBlockSnapshot to = SpongeBlockSnapshotBuilder.pooled().blockState(toState).world((ServerLevel) worldIn).position(position).build();
            final BlockTransaction transaction = new BlockTransaction(from, to, Operations.LIQUID_SPREAD.get());
            final ChangeBlockEvent event = SpongeEventFactory.createChangeBlockEventAll(frame.getCurrentCause(),
                Collections.singletonList(transaction), ((org.spongepowered.api.world.server.ServerWorld) worldIn));

            SpongeCommon.postEvent(event);
            return event;
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(final World world, final BlockPos sourcePos, final EnumSet<net.minecraft.core.Direction> notifiedSides) {
        final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
        // Don't fire notify events during world gen or while restoring
        if (context.isWorldGeneration() || context.isRestoring()) {
            return null;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final BlockState blockstate = (BlockState) ((net.minecraft.world.level.Level) world).getBlockState(sourcePos);
            final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world((org.spongepowered.api.world.server.ServerWorld) world).position(sourcePos.getX(), sourcePos.getY(),
                    sourcePos.getZ())
                    .state(blockstate)
                    .build();
            if (context.getNotifier().isPresent()) {
                context.addCreatorAndNotifierToCauseStack(frame);
            } else {

                final LevelChunkBridge mixinChunk = (LevelChunkBridge) ((ServerLevel) world).getChunkAt(sourcePos);
                mixinChunk.bridge$getBlockCreator(sourcePos).ifPresent(creator -> frame.addContext(EventContextKeys.CREATOR, creator));
                mixinChunk.bridge$getBlockNotifier(sourcePos).ifPresent(user -> frame.addContext(EventContextKeys.NOTIFIER, user));
            }
            PhaseTracker.getCauseStackManager().pushCause(locatable);

            final Map<Direction, BlockState> neighbors = new EnumMap<>(Direction.class);
            for (final net.minecraft.core.Direction notificationSide : notifiedSides) {
                final BlockPos offset = sourcePos.relative(notificationSide);
                final Direction direction = DirectionFacingProvider.INSTANCE.getKey(notificationSide).get();
                final net.minecraft.world.level.block.state.BlockState notificationState = ((ServerLevel) world).getBlockState(offset);
                neighbors.put(direction, (BlockState) notificationState);
            }

            final NotifyNeighborBlockEvent event =
                    SpongeEventFactory.createNotifyNeighborBlockEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), neighbors, neighbors);
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractEntityEvent.Primary callInteractEntityEventPrimary(final net.minecraft.server.level.ServerPlayer player, final ItemStack stack, final net.minecraft.world.entity.Entity entity, final InteractionHand hand) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            SpongeCommonEventFactory.applyCommonInteractContext(player, stack, hand, null, entity, frame);
            final InteractEntityEvent.Primary event = SpongeEventFactory.createInteractEntityEventPrimary(frame.getCurrentCause(), (Entity) entity);
            if (entity instanceof Player && !((org.spongepowered.api.world.server.ServerWorld) player.getLevel()).getProperties().pvp()) {
                event.setCancelled(true); // if PvP is disabled for world, cancel
            }
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractEntityEvent.Secondary callInteractEntityEventSecondary(final net.minecraft.server.level.ServerPlayer player, final ItemStack stack, final net.minecraft.world.entity.Entity entity,
            final InteractionHand hand, @Nullable final Vector3d hitVec) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            SpongeCommonEventFactory.applyCommonInteractContext(player, stack, hand, null, entity, frame);
            final InteractEntityEvent.Secondary event = hitVec == null ?
                    SpongeEventFactory.createInteractEntityEventSecondaryOn(frame.getCurrentCause(), (Entity) entity) :
                    SpongeEventFactory.createInteractEntityEventSecondaryAt(frame.getCurrentCause(), (Entity) entity, hitVec);
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractItemEvent.Primary callInteractItemEventPrimary(final net.minecraft.world.entity.player.Player player, final ItemStack stack, final InteractionHand hand) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            SpongeCommonEventFactory.applyCommonInteractContext(player, stack, hand, null, null, frame);
            final InteractItemEvent.Primary event = SpongeEventFactory.createInteractItemEventPrimary(frame.getCurrentCause(), ItemStackUtil.snapshotOf(stack));
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractItemEvent.Secondary callInteractItemEventSecondary(final net.minecraft.world.entity.player.Player player, final ItemStack stack, final InteractionHand hand) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            SpongeCommonEventFactory.applyCommonInteractContext(player, stack, hand, null, null, frame);
            final InteractItemEvent.Secondary event = SpongeEventFactory.createInteractItemEventSecondary(frame.getCurrentCause(), ItemStackUtil.snapshotOf(stack));
            SpongeCommon.postEvent(event);
            return event;
        }

    }

    public static InteractBlockEvent.Primary callInteractBlockEventPrimary(
        final ServerboundPlayerActionPacket.Action action,
            final net.minecraft.world.entity.player.Player player, final ItemStack heldItem, final BlockSnapshot blockSnapshot, final InteractionHand hand,
            @Nullable final net.minecraft.core.Direction side) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            SpongeCommonEventFactory.applyCommonInteractContext(player, heldItem, hand, blockSnapshot, null, frame);
            final Direction direction;
            if (side != null) {
                direction = DirectionFacingProvider.INSTANCE.getKey(side).get();
            } else {
                direction = Direction.NONE;
            }

            final InteractBlockEvent.Primary event;
            switch (action) {
                case START_DESTROY_BLOCK:
                    event = SpongeEventFactory.createInteractBlockEventPrimaryStart(frame.getCurrentCause(), blockSnapshot, direction);
                    break;
                case ABORT_DESTROY_BLOCK:
                    event = SpongeEventFactory.createInteractBlockEventPrimaryStop(frame.getCurrentCause(), blockSnapshot, direction);
                    break;
                case STOP_DESTROY_BLOCK:
                    event = SpongeEventFactory.createInteractBlockEventPrimaryFinish(frame.getCurrentCause(), blockSnapshot, direction);
                    break;
                default:
                    throw new IllegalStateException("unreachable code");
            }

            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(final net.minecraft.world.entity.player.Player player, final ItemStack heldItem, final Vector3d hitVec, final BlockSnapshot targetBlock, final Direction targetSide, final InteractionHand hand) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            SpongeCommonEventFactory.applyCommonInteractContext(player, heldItem, hand, targetBlock, null, frame);
            final InteractBlockEvent.Secondary event = SpongeEventFactory.createInteractBlockEventSecondary(frame.getCurrentCause(),
                    Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, targetBlock, hitVec,
                    targetSide);
            SpongeCommon.postEvent(event);
            return event;
        }
    }

    public static void applyCommonInteractContext(
        final net.minecraft.world.entity.player.Player player, final ItemStack stack, final InteractionHand hand, @Nullable
    final BlockSnapshot targetBlock,
            @Nullable final net.minecraft.world.entity.Entity entity, final CauseStackManager.StackFrame frame) {
        if (((PlatformEntityBridge) player).bridge$isFakePlayer()) {
            frame.addContext(EventContextKeys.FAKE_PLAYER, (Player) player);
        } else {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.CREATOR, ((ServerPlayerBridge) player).bridge$getUser());
            frame.addContext(EventContextKeys.NOTIFIER, ((ServerPlayerBridge) player).bridge$getUser());
        }

        if (!stack.isEmpty()) {
            frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(stack));
        }
        frame.addContext(EventContextKeys.USED_HAND, (HandType) (Object) hand);
        if (targetBlock != null) {
            frame.addContext(EventContextKeys.BLOCK_HIT, targetBlock);
        }
        if (entity != null) {
            frame.addContext(EventContextKeys.ENTITY_HIT, (Entity) entity);
        }
    }

    /**
     * Performs the logic necessary to post the {@link MoveEntityEvent position event} for an {@link Entity}.
     *
     * @param entity The event
     */
    public static void callNaturalMoveEntityEvent(final net.minecraft.world.entity.Entity entity) {
        if (entity.isRemoved()) {
            return;
        }

        final double deltaX = entity.xOld - entity.getX();
        final double deltaY = entity.yOld - entity.getY();
        final double deltaZ = entity.zOld - entity.getZ();
        final double deltaChange = Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2);
        if (deltaChange < 1f / 256) {
            return;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity);
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.NATURAL);

            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(), (Entity) entity,
                    new Vector3d(entity.xOld, entity.yOld, entity.zOld), new Vector3d(entity.getX(), entity.getY(), entity.getZ()),
                    new Vector3d(entity.getX(), entity.getY(), entity.getZ()));

            if (SpongeCommon.postEvent(event)) {
                entity.setPos(entity.xOld, entity.yOld, entity.zOld);
            } else {
                entity.setPos(event.getDestinationPosition().getX(), event.getDestinationPosition().getY(), event.getDestinationPosition().getZ());
            }
        }
    }

    /**
     * Performs the logic necessary to post the {@link RotateEntityEvent rotation event} for an {@link Entity}.
     *
     * @param entity The event
     */
    public static void callNaturalRotateEntityEvent(final net.minecraft.world.entity.Entity entity) {
        if (entity.isRemoved() || (entity.xRot == entity.xRotO && entity.yRot == entity.yRotO)) {
            return;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity);

            final RotateEntityEvent event = SpongeEventFactory.createRotateEntityEvent(frame.getCurrentCause(), (Entity) entity,
                    new Vector3d(entity.xRotO, entity.yRotO, 0), new Vector3d(entity.xRot, entity.yRot, 0));

            if (SpongeCommon.postEvent(event)) {
                entity.xRot = entity.xRotO;
                entity.yRot = entity.yRotO;
            } else {
                entity.xRot = (float) event.getToRotation().getX();
                entity.yRot = (float) event.getToRotation().getY();
            }
        }
    }

    public static DestructEntityEvent.Death callDestructEntityEventDeath(final LivingEntity entity, @Nullable final DamageSource source) {
        return SpongeCommonEventFactory.callDestructEntityEventDeath(entity, source, Audience.empty());
    }

    public static DestructEntityEvent.Death callDestructEntityEventDeath(final LivingEntity entity, @Nullable final DamageSource source,
            final Audience originalChannel) {

        final Component originalMessage;
        Optional<User> sourceCreator = Optional.empty();
        final boolean messageCancelled = false;

        if (source instanceof EntityDamageSource) {
            final EntityDamageSource damageSource = (EntityDamageSource) source;
            if (damageSource.getDirectEntity() instanceof CreatorTrackedBridge) {
                final CreatorTrackedBridge creatorBridge = (CreatorTrackedBridge) damageSource.getDirectEntity();
                if (creatorBridge != null) {
                    sourceCreator = creatorBridge.tracked$getCreatorReference();
                }
            }
        }

        originalMessage = SpongeAdventure.asAdventure(entity.getCombatTracker().getDeathMessage());
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            if (source != null) {
                frame.pushCause(source);
            }

            sourceCreator.ifPresent(user -> frame.addContext(EventContextKeys.CREATOR, user));

            final DestructEntityEvent.Death event = SpongeEventFactory.createDestructEntityEventDeath(frame.getCurrentCause(),
                    originalChannel, Optional.of(originalChannel), originalMessage, originalMessage, (Living) entity,
                    entity.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY), messageCancelled);
            SpongeCommon.postEvent(event);

            return event;
        }
    }

    public static boolean handleCollideBlockEvent(final Block block, final net.minecraft.world.level.Level world, final BlockPos pos, final net.minecraft.world.level.block.state.BlockState state, final net.minecraft.world.entity.Entity entity, final Direction direction) {
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
                        final LevelChunkBridge spongeChunk = (LevelChunkBridge) world.getChunkAt(pos);
                        spongeChunk.bridge$addTrackedBlockPosition(block, pos, notifier, PlayerTracker.Type.NOTIFIER);

                    });
                }
            }
            return cancelled;
        }
    }

    public static boolean handleCollideImpactEvent(final net.minecraft.world.entity.Entity projectile, @Nullable final ProjectileSource projectileSource,
            final HitResult movingObjectPosition) {
        final HitResult.Type movingObjectType = movingObjectPosition.getType();
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(projectile);
            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, projectileSource == null
                    ? UnknownProjectileSource.UNKNOWN
                    : projectileSource);
            final Optional<User> creator = PhaseTracker.getInstance().getPhaseContext().getCreator();
            creator.ifPresent(user -> frame.addContext(EventContextKeys.CREATOR, user));

            final ServerLocation impactPoint = ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) projectile.level, VecHelper.toVector3d(movingObjectPosition.getLocation()));
            boolean cancelled = false;

            if (movingObjectType == HitResult.Type.BLOCK) {
                final BlockHitResult blockMovingObjectPosition = (BlockHitResult) movingObjectPosition;
                final BlockPos blockPos = blockMovingObjectPosition.getBlockPos();
                if (blockPos.getY() <= 0) {
                    return false;
                }

                final BlockSnapshot targetBlock = ((org.spongepowered.api.world.server.ServerWorld) projectile.level).createSnapshot(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                final Direction side = DirectionFacingProvider.INSTANCE.getKey(blockMovingObjectPosition.getDirection()).get();

                final CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(frame.getCurrentCause(),
                        impactPoint, targetBlock.getState(),
                        targetBlock.getLocation().get(), side);
                cancelled = SpongeCommon.postEvent(event);
                // Track impact block if event is not cancelled
                if (!cancelled && creator.isPresent()) {
                    final BlockPos targetPos = VecHelper.toBlockPos(impactPoint.getBlockPosition());
                    final LevelChunkBridge spongeChunk = (LevelChunkBridge) projectile.level.getChunkAt(targetPos);
                    spongeChunk.bridge$addTrackedBlockPosition((Block) targetBlock.getState().getType(), targetPos, creator.get(), PlayerTracker.Type.NOTIFIER);
                }
            } else if (movingObjectType == HitResult.Type.ENTITY) { // entity
                final EntityHitResult entityMovingObjectPosition = (EntityHitResult) movingObjectPosition;
                final ArrayList<Entity> entityList = new ArrayList<>();
                entityList.add((Entity) entityMovingObjectPosition.getEntity());
                final CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(frame.getCurrentCause(), entityList, impactPoint);
                        cancelled = SpongeCommon.postEvent(event);
            }

            return cancelled;
        }
    }

    public static InteractContainerEvent.Close callInteractInventoryCloseEvent(final AbstractContainerMenu container, final net.minecraft.server.level.ServerPlayer player,
            final ItemStackSnapshot lastCursor, final ItemStackSnapshot newCursor, final boolean clientSource) {
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        final InteractContainerEvent.Close event =
                SpongeEventFactory.createInteractContainerEventClose(PhaseTracker.getCauseStackManager().getCurrentCause(), ContainerUtil.fromNative(container), cursorTransaction);
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            if (clientSource && container.getSlot(0) != null) {
                if (!(container instanceof InventoryMenu)) {
                    // Inventory closed by client, reopen window and send container
                    player.containerMenu = container;
                    final Slot slot = container.getSlot(0);
                    final Container slotInventory = slot.container;
                    final net.minecraft.network.chat.Component title;
                    // TODO get name from last open
                    if (slotInventory instanceof MenuProvider) {
                        title = ((MenuProvider) slotInventory).getDisplayName();
                    } else {
                        // expected fallback for unknown types
                        title = null;
                    }
                    slotInventory.startOpen(player);
                    player.connection.send(new ClientboundOpenScreenPacket(container.containerId, container.getType(), title));
                    // resync data to client
                    player.refreshContainer(container);
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
            final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) player.containerMenu;
            mixinContainer.bridge$getCapturedSlotTransactions().clear();
            mixinContainer.bridge$setCaptureInventory(false);
            // Handle cursor
            if (!event.getCursorTransaction().isValid()) {
                handleCustomCursor(player, event.getCursorTransaction().getOriginal());
            } else if (event.getCursorTransaction().getCustom().isPresent()) {
                handleCustomCursor(player, event.getCursorTransaction().getFinal());
            }
            if (!clientSource && player.containerMenu != null && player.connection != null) {
                player.closeContainer();
            }
        }

        return event;
    }

    public static SetAITargetEvent callSetAttackTargetEvent(@Nullable final Entity target, final Agent agent) {
        final SetAITargetEvent event = SpongeEventFactory.createSetAITargetEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), agent, Optional.ofNullable(target));
        SpongeCommon.postEvent(event);
        return event;
    }

    public static Optional<net.minecraft.world.level.Explosion> detonateExplosive(final ExplosiveBridge explosiveBridge, final Explosion.Builder builder) {
        final DetonateExplosiveEvent event = SpongeEventFactory.createDetonateExplosiveEvent(
                PhaseTracker.getCauseStackManager().getCurrentCause(), builder, (Explosive) explosiveBridge, builder.build()
        );
        if (!Sponge.getEventManager().post(event)) {
            final Explosion explosion = event.getExplosionBuilder().build();
            if (explosion.getRadius() > 0) {
                ((TrackedWorldBridge) ((Explosive) explosiveBridge).getWorld())
                    .tracker$triggerInternalExplosion(
                        explosion,
                        e -> GeneralPhase.State.EXPLOSION.createPhaseContext(PhaseTracker.SERVER).explosion(e)
                    );
            }
            return Optional.of((net.minecraft.world.level.Explosion) explosion);
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
    public static ItemStack throwDropItemAndConstructEvent(final net.minecraft.world.entity.Entity entity, final double posX, final double posY,
        final double posZ, final ItemStackSnapshot snapshot, final List<ItemStackSnapshot> original, final CauseStackManager.StackFrame frame) {
        final PlayerBridge mixinPlayer;
        if (entity instanceof PlayerBridge) {
            mixinPlayer = (PlayerBridge) entity;
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
        final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) entity.level, posX, posY, posZ), new Vector3d(0, 0, 0), EntityTypes.ITEM.get());
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

    public static PlaySoundEvent.Record callPlaySoundRecordEvent(final Cause cause, final JukeboxBlockEntity jukebox,
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
    public static PlaySoundEvent.AtEntity callPlaySoundAtEntityEvent(final Cause cause, @Nullable final net.minecraft.world.entity.player.Player entity,
        final WorldBridge worldMixin, final double x, final double y, final double z, final net.minecraft.sounds.SoundSource category,
        final SoundEvent name, final float pitch, final float volume) {
        final ServerLocation location = ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) worldMixin, x, y, z);
        final PlaySoundEvent.AtEntity event = SpongeEventFactory.createPlaySoundEventAtEntity(cause, location,
            Optional.ofNullable((ServerPlayer) entity), SpongeAdventure.asAdventure(category), (SoundType) name, pitch, volume);
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
