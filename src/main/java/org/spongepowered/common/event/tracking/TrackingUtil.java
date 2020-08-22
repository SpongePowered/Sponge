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
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.TrackedChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.phase.tick.BlockEventTickContext;
import org.spongepowered.common.event.tracking.phase.tick.BlockTickContext;
import org.spongepowered.common.event.tracking.phase.tick.DimensionContext;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.event.tracking.phase.tick.TileEntityTickContext;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple utility for aiding in tracking, either with resolving notifiers
 * and owners, or proxying out the logic for ticking a block, entity, etc.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class TrackingUtil {

    public static final Marker ENTITY_TICK = MarkerManager.getMarker("ENTITY TICK");
    public static final Marker TILE_ENTITY_TICK = MarkerManager.getMarker("TILE ENTITY TICK");
    public static final Marker PLAYER_TICK = MarkerManager.getMarker("PLAYER TICK");
    public static final Marker BLOCK_TICK = MarkerManager.getMarker("BLOCK TICK");
    public static final Marker NEIGHBOR_UPDATE = MarkerManager.getMarker("NEIGHBOR UPDATE");


    public static final int BREAK_BLOCK_INDEX = BlockChange.BREAK.ordinal();
    public static final int PLACE_BLOCK_INDEX = BlockChange.PLACE.ordinal();
    public static final int DECAY_BLOCK_INDEX = BlockChange.DECAY.ordinal();
    public static final int CHANGE_BLOCK_INDEX = BlockChange.MODIFY.ordinal();
    private static final int MULTI_CHANGE_INDEX = BlockChange.values().length;
    static final Function<SpongeBlockSnapshot, Optional<Transaction<BlockSnapshot>>> TRANSACTION_CREATION =
        (blockSnapshot) -> blockSnapshot.getServerWorld().map(worldServer -> {
            final BlockPos targetPos = blockSnapshot.getBlockPos();
            final SpongeBlockSnapshot replacement = ((TrackedWorldBridge) worldServer).bridge$createSnapshot(targetPos, BlockChangeFlags.NONE);
            return new Transaction<>(blockSnapshot, replacement);
        });
    public static final int WIDTH = 40;

    public static void tickEntity(final Consumer<net.minecraft.entity.Entity> consumer, final net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        if (!((TrackableBridge) entity).bridge$shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext(PhaseTracker.SERVER).source(entity);
        try (final EntityTickContext context = tickContext;
             final Timing entityTiming = ((TimingBridge) entity).bridge$getTimingsHandler()
        ) {
            if (entity instanceof CreatorTrackedBridge) {
                ((CreatorTrackedBridge) entity).tracked$getNotifierReference()
                    .ifPresent(context::notifier);
                ((CreatorTrackedBridge) entity).tracked$getCreatorReference()
                    .ifPresent(context::creator);
            }
            context.buildAndSwitch();
            entityTiming.startTiming();
            PhaseTracker.LOGGER.trace(TrackingUtil.ENTITY_TICK, "Wrapping Ticked Entity: " + entity.toString());
            consumer.accept(entity);
            SpongeCommonEventFactory.callNaturalMoveEntityEvent(entity);
            SpongeCommonEventFactory.callNaturalRotateEntityEvent(entity);
        } catch (final Exception e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, tickContext);
        }
    }

    public static void tickGlobalEntity(final Consumer<net.minecraft.entity.Entity> consumer, final net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        // Forge has an override for whether an entity can update, and this is explicitly provided within the lambda
        // consumer, so we can have our own check whether the entity should tick as defined by configs/activation range/etc.
        if (!((TrackableBridge) entity).bridge$shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext(PhaseTracker.SERVER).source(entity);
        try (final EntityTickContext context = tickContext;
            final Timing entityTiming = ((TimingBridge) entity).bridge$getTimingsHandler()
        ) {
            if (entity instanceof CreatorTrackedBridge) {
                ((CreatorTrackedBridge) entity).tracked$getNotifierReference()
                    .ifPresent(context::notifier);
                ((CreatorTrackedBridge) entity).tracked$getCreatorReference()
                    .ifPresent(context::creator);
            }
            context.buildAndSwitch();
            entityTiming.startTiming();
            PhaseTracker.LOGGER.trace(TrackingUtil.ENTITY_TICK, "Wrapping Ticked Entity: " + entity.toString());
            consumer.accept(entity);
            SpongeCommonEventFactory.callNaturalMoveEntityEvent(entity);
            SpongeCommonEventFactory.callNaturalRotateEntityEvent(entity);
        } catch (final Exception e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, tickContext);
        }
    }

    public static void tickRidingEntity(final net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        if (!((TrackableBridge) entity).bridge$shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext(PhaseTracker.SERVER).source(entity);
        try (
             final EntityTickContext context = tickContext;
             final Timing entityTiming = ((TimingBridge) entity).bridge$getTimingsHandler()
             ) {
            entityTiming.startTiming();
            if (entity instanceof CreatorTrackedBridge) {
                ((CreatorTrackedBridge) entity).tracked$getNotifierReference()
                    .ifPresent(context::notifier);
                ((CreatorTrackedBridge) entity).tracked$getCreatorReference()
                    .ifPresent(context::creator);
            }
            context.buildAndSwitch();
            entity.updateRidden();
            SpongeCommonEventFactory.callNaturalMoveEntityEvent(entity);
            SpongeCommonEventFactory.callNaturalRotateEntityEvent(entity);
        } catch (final Exception e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, tickContext);
        }
    }

    @SuppressWarnings({"unused", "try"})
    public static void tickTileEntity(final TrackedWorldBridge mixinWorldServer, final ITickableTileEntity tile) {
        checkArgument(tile instanceof BlockEntity, "ITickable %s is not a TileEntity!", tile);
        checkNotNull(tile, "Cannot capture on a null ticking tile entity!");
        final TileEntity tileEntity = (TileEntity) tile;
        final TileEntityBridge mixinTileEntity = (TileEntityBridge) tile;
        final BlockPos pos = tileEntity.getPos();
        final ChunkBridge chunk = ((ActiveChunkReferantBridge) tile).bridge$getActiveChunk();
        if (!((TrackableBridge) tileEntity).bridge$shouldTick()) {
            return;
        }
        if (chunk == null) {
            ((ActiveChunkReferantBridge) tile).bridge$setActiveChunk((TrackedChunkBridge) tileEntity.getWorld().getChunkAt(tileEntity.getPos()));
        }

        final TileEntityTickContext context = TickPhase.Tick.TILE_ENTITY.createPhaseContext(PhaseTracker.SERVER).source(mixinTileEntity);
        try (final PhaseContext<?> phaseContext = context) {

            if (tile instanceof CreatorTrackedBridge) {
                // Add notifier and owner so we don't have to perform lookups during the phases and other processing
                ((CreatorTrackedBridge) tile).tracked$getNotifierReference().ifPresent(phaseContext::notifier);
                // Allow the tile entity to validate the owner of itself. As long as the tile entity
                // chunk is already loaded and activated, and the tile entity has already loaded
                // the owner of itself.
                ((CreatorTrackedBridge) tile).tracked$getCreatorReference().ifPresent(phaseContext::creator);
            }

            // Finally, switch the context now that we have the owner and notifier
            phaseContext.buildAndSwitch();

            try (final Timing timing = ((TimingBridge) tileEntity).bridge$getTimingsHandler().startTiming()) {
                PhaseTracker.LOGGER.trace(TrackingUtil.TILE_ENTITY_TICK, "Wrapping Ticked Entity: " + tile.toString());
                tile.tick();
            }
        } catch (Exception e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, context);
        }
        // We delay clearing active chunk if TE is invalidated during tick so we must remove it after
        if (tileEntity.isRemoved()) {
            ((ActiveChunkReferantBridge) tileEntity).bridge$setActiveChunk(null);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void updateTickBlock(
            final TrackedWorldBridge mixinWorld, final net.minecraft.block.BlockState block, final BlockPos pos, final Random random) {
        final ServerWorld world = (ServerWorld) mixinWorld;
        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) world;

        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot snapshot = mixinWorld.bridge$createSnapshot(block, pos, BlockChangeFlags.NONE);
            final TickBlockEvent event = SpongeEventFactory.createTickBlockEventScheduled(PhaseTracker.getCauseStackManager().getCurrentCause(), snapshot);
            SpongeCommon.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(apiWorld).position(pos.getX(), pos.getY(), pos.getZ()).state((BlockState)block).build();
        final BlockTickContext phaseContext = TickPhase.Tick.BLOCK.createPhaseContext(PhaseTracker.SERVER).source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getPhaseContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(world, pos, currentContext, phaseContext);
        // Now actually switch to the new phase

        try (final PhaseContext<?> context = phaseContext;
             final Timing timing = ((TimingBridge) block.getBlock()).bridge$getTimingsHandler()) {
            timing.startTiming();
            context.buildAndSwitch();
            PhaseTracker.LOGGER.trace(TrackingUtil.BLOCK_TICK, "Wrapping Block Tick: " + block.toString());
            block.tick(world, pos, random);
        } catch (Exception | NoClassDefFoundError e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, phaseContext);

        }
    }

    public static void updateTickFluid(
        final TrackedWorldBridge mixinWorld, final IFluidState fluidState, final BlockPos pos
    ) {
        final ServerWorld world = (ServerWorld) mixinWorld;
        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) world;

        final net.minecraft.block.BlockState blockState = fluidState.getBlockState();
        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot snapshot = mixinWorld.bridge$createSnapshot(blockState, pos, BlockChangeFlags.NONE);
            final TickBlockEvent event = SpongeEventFactory.createTickBlockEventScheduled(PhaseTracker.getCauseStackManager().getCurrentCause(), snapshot);
            SpongeCommon.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(apiWorld).position(pos.getX(), pos.getY(), pos.getZ()).state((BlockState) blockState).build();
        final BlockTickContext phaseContext = TickPhase.Tick.BLOCK.createPhaseContext(PhaseTracker.SERVER).source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getPhaseContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(world, pos, currentContext, phaseContext);
        // Now actually switch to the new phase

        try (final PhaseContext<?> context = phaseContext;
            final Timing timing = ((TimingBridge) blockState.getBlock()).bridge$getTimingsHandler()) {
            timing.startTiming();
            context.buildAndSwitch();
            PhaseTracker.LOGGER.trace(TrackingUtil.BLOCK_TICK, "Wrapping Fluid Tick: " + fluidState.toString());
            fluidState.tick(world, pos);
        } catch (Exception | NoClassDefFoundError e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, phaseContext);

        }
    }

    @SuppressWarnings("rawtypes")
    public static void randomTickBlock(final TrackedWorldBridge mixinWorld,
                                       final net.minecraft.block.BlockState state, final BlockPos pos, final Random random) {
        final ServerWorld world = (ServerWorld) mixinWorld;
        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) world;

        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot currentTickBlock = mixinWorld.bridge$createSnapshot(state, pos, BlockChangeFlags.NONE);
            final TickBlockEvent
                event =
                SpongeEventFactory.createTickBlockEventRandom(PhaseTracker.getCauseStackManager().getCurrentCause(), currentTickBlock);
            SpongeCommon.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder()
                                             .world(apiWorld)
                                             .position(pos.getX(), pos.getY(), pos.getZ())
                                             .state((BlockState) state)
                                             .build();
        final BlockTickContext phaseContext = TickPhase.Tick.RANDOM_BLOCK.createPhaseContext(PhaseTracker.SERVER).source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getPhaseContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(world, pos, currentContext, phaseContext);
        // Now actually switch to the new phase
        try (final PhaseContext<?> context = phaseContext) {
            context.buildAndSwitch();
            PhaseTracker.LOGGER.trace(TrackingUtil.BLOCK_TICK, "Wrapping Random Block Tick: " + state.toString());
            state.randomTick(world, pos, random);
        } catch (final Exception | NoClassDefFoundError e) {
            PhasePrinter.printExceptionFromPhase(PhaseTracker.getInstance().stack, e, phaseContext);
        }
    }


    public static void tickWorldProvider(final ServerWorldBridge worldServer) {
        final Dimension worldProvider = ((ServerWorld) worldServer).dimension;
        try (final DimensionContext context = TickPhase.Tick.DIMENSION.createPhaseContext(PhaseTracker.SERVER).source(worldProvider)) {
            context.buildAndSwitch();
            worldProvider.tick();
        }
    }

    public static boolean fireMinecraftBlockEvent(final ServerWorld worldIn, final BlockEventData event) {
        final net.minecraft.block.BlockState currentState = worldIn.getBlockState(event.getPosition());
        final BlockEventDataBridge blockEvent = (BlockEventDataBridge) event;
        final Object source = blockEvent.bridge$getTileEntity() != null ? blockEvent.bridge$getTileEntity() : blockEvent.bridge$getTickingLocatable();
        if (source == null) {
            // No source present which means we are ignoring the phase state
            return currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
        }
        final BlockEventTickContext phaseContext = TickPhase.Tick.BLOCK_EVENT.createPhaseContext(PhaseTracker.SERVER);
        phaseContext.source(source);

        final User user = ((BlockEventDataBridge) event).bridge$getSourceUser();
        if (user != null) {
            phaseContext.creator = user;
            phaseContext.notifier = user;
        }

        boolean result = true;
        try (final BlockEventTickContext o = phaseContext) {
            o.buildAndSwitch();
            phaseContext.setEventSucceeded(currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter()));
            // We need to grab the result here as the phase context close will trigger a reset
            result = phaseContext.wasNotCancelled();
        } // We can't return onBlockEventReceived because the phase state may have cancelled all transactions
        // at which point we want to keep track of the return value from the target, and from the block events.
        return result;
    }

    static boolean forceModify(final Block originalBlock, final Block newBlock) {
        if (originalBlock instanceof RepeaterBlock && newBlock instanceof RepeaterBlock) {
            return true;
        } else if (originalBlock instanceof RedstoneTorchBlock && newBlock instanceof RedstoneTorchBlock) {
            return true;
        } else
            return originalBlock instanceof RedstoneLampBlock && newBlock instanceof RedstoneLampBlock;
    }

    private TrackingUtil() {
    }

    @Nullable
    public static User getNotifierOrOwnerFromBlock(final ServerWorld world, final BlockPos blockPos) {
        final ChunkBridge mixinChunk = (ChunkBridge) world.getChunkAt(blockPos);
        final User notifier = mixinChunk.bridge$getBlockNotifier(blockPos).orElse(null);
        if (notifier != null) {
            return notifier;
        }

        return mixinChunk.bridge$getBlockCreator(blockPos).orElse(null);
    }

    public static Supplier<IllegalStateException> throwWithContext(final String s, final PhaseContext<?> phaseContext) {
        return () -> {
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Exception trying to process over a phase!").centre().hr();
            printer.addWrapped(TrackingUtil.WIDTH, "%s : %s", "State", phaseContext.state);
            printer.addWrapped(TrackingUtil.WIDTH, "%s :", "PhaseContext");
            PhasePrinter.CONTEXT_PRINTER.accept(printer, phaseContext);
            printer.add("Stacktrace:");
            final IllegalStateException exception = new IllegalStateException(s + " Please analyze the current phase context. ");
            printer.add(exception);
            printer.trace(System.err, SpongeCommon.getLogger(), Level.ERROR);
            return exception;
        };
    }

    public static boolean processBlockCaptures(final PhaseContext<?> context) {
        final TransactionalCaptureSupplier transactor = context.getBlockTransactor();
        // Fail fast and check if it's empty.
        if (transactor.isEmpty()) {
            return false;
        }
        // Start the hybrid depth first batched iteration of transactions
        // Some rules of the iteration:
        /*
        1) Each transaction can be potentially turned into a Transaction<BlockSnapshot>
        2) If a transaction has children side effects, stop and throw the related event(s) to verify
          - the event was not cancelled
          - the event result was the same
        3) If there are child side effects, repeat the process.

         */
        return transactor.processTransactions(context);
    }

    public static void associateTrackerToTarget(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> transaction, final User user) {
        final BlockSnapshot finalSnapshot = transaction.getFinal();
        final SpongeBlockSnapshot spongeSnapshot = (SpongeBlockSnapshot) finalSnapshot;
        final BlockPos pos = spongeSnapshot.getBlockPos();
        final Block block = ((net.minecraft.block.BlockState) spongeSnapshot.getState()).getBlock();
        spongeSnapshot.getServerWorld()
            .map(world -> world.getChunkAt(pos))
            .map(chunk -> (ChunkBridge) chunk)
            .ifPresent(spongeChunk -> {
            final PlayerTracker.Type trackerType = blockChange == BlockChange.PLACE ? PlayerTracker.Type.CREATOR : PlayerTracker.Type.NOTIFIER;
            spongeChunk.bridge$addTrackedBlockPosition(block, pos, user, trackerType);
        });
    }

    public static void addTileEntityToBuilder(@Nullable final TileEntity existing, final SpongeBlockSnapshotBuilder builder) {
        // We MUST only check to see if a TE exists to avoid creating a new one.
        final BlockEntity tile = (BlockEntity) existing;
        if (existing == null) {
            return;
        }
        // TODO - gather custom data.
        final CompoundNBT nbt = new CompoundNBT();
        // Some mods like OpenComputers assert if attempting to save robot while moving
        try {
            existing.write(nbt);
            builder.unsafeNbt(nbt);
        }
        catch(Throwable t) {
            // ignore
        }
    }

    public static String phaseStateToString(final String type, final IPhaseState<?> state) {
        return TrackingUtil.phaseStateToString(type, null, state);
    }

    public static String phaseStateToString(final String type, @Nullable final String extra, final IPhaseState<?> state) {
        String name = state.getClass().getSimpleName();
        name = name.replace("Phase", "");
        name = name.replace("State", "");
        name = name.replace(type, "");

        if (extra == null) {
            return type + "{" + name + "}";
        } else if (name.isEmpty()) {
            return type + "{" + extra + "}";
        } else {
            return type + "{" + name + ":" + extra + "}";
        }
    }

    public static SpongeBlockSnapshot createPooledSnapshot(final net.minecraft.block.BlockState state, final BlockPos pos,
        final BlockChangeFlag updateFlag, @Nullable final TileEntity existing,
        final Supplier<ServerWorld> worldSupplier,
        final Supplier<Optional<UUID>> creatorSupplier,
        final Supplier<Optional<UUID>> notifierSupplier
    ) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.reset();
        builder.blockState(state)
                .world(worldSupplier.get())
                .position(VecHelper.toVector3i(pos));
        creatorSupplier.get().ifPresent(builder::creator);
        notifierSupplier.get().ifPresent(builder::notifier);
        if (existing != null) {
            TrackingUtil.addTileEntityToBuilder(existing, builder);
        }
        builder.flag(updateFlag);
        return builder.build();
    }
}
