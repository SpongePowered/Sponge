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
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.context.MultiBlockCaptureSupplier;
import org.spongepowered.common.event.tracking.phase.tick.BlockEventTickContext;
import org.spongepowered.common.event.tracking.phase.tick.BlockTickContext;
import org.spongepowered.common.event.tracking.phase.tick.DimensionContext;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.event.tracking.phase.tick.TileEntityTickContext;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * A simple utility for aiding in tracking, either with resolving notifiers
 * and owners, or proxying out the logic for ticking a block, entity, etc.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class TrackingUtil {

    public static final int BREAK_BLOCK_INDEX = BlockChange.BREAK.ordinal();
    public static final int PLACE_BLOCK_INDEX = BlockChange.PLACE.ordinal();
    public static final int DECAY_BLOCK_INDEX = BlockChange.DECAY.ordinal();
    public static final int CHANGE_BLOCK_INDEX = BlockChange.MODIFY.ordinal();
    private static final int MULTI_CHANGE_INDEX = BlockChange.values().length;
    private static final int EVENT_COUNT = BlockChange.values().length + 1;
    private static final Function<ImmutableList.Builder<Transaction<BlockSnapshot>>[], Consumer<Transaction<BlockSnapshot>>> TRANSACTION_PROCESSOR =
            builders ->
                    transaction -> {
                        final BlockChange blockChange = ((SpongeBlockSnapshot) transaction.getOriginal()).blockChange;
                        builders[blockChange.ordinal()].add(transaction);
                        builders[MULTI_CHANGE_INDEX].add(transaction);
                    }
            ;
    static final Function<SpongeBlockSnapshot, Optional<Transaction<BlockSnapshot>>> TRANSACTION_CREATION =
        (blockSnapshot) -> blockSnapshot.getWorldServer().map(worldServer -> {
            final BlockPos targetPos = blockSnapshot.getBlockPos();
            final SpongeBlockSnapshot replacement = ((WorldServerBridge) worldServer).bridge$createSnapshot(targetPos, BlockChangeFlags.NONE);
            return new Transaction<>(blockSnapshot, replacement);
        });
    public static final int WIDTH = 40;

    public static void tickEntity(final net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        final EntityBridge mixinEntity = (EntityBridge) entity;
        if (!mixinEntity.bridge$shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext().source(entity);
        try (final EntityTickContext context = tickContext;
             final Timing entityTiming = ((TimingBridge) entity).bridge$getTimingsHandler()
        ) {
            if (entity instanceof OwnershipTrackedBridge) {
                ((OwnershipTrackedBridge) entity).tracked$getNotifierReference()
                    .ifPresent(context::notifier);
                ((OwnershipTrackedBridge) entity).tracked$getOwnerReference()
                    .ifPresent(context::owner);
            }
            context.buildAndSwitch();
            entityTiming.startTiming();
            entity.func_70071_h_();
            if (ShouldFire.MOVE_ENTITY_EVENT_POSITION || ShouldFire.ROTATE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity, context);
            }
        } catch (Exception | NoClassDefFoundError e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, tickContext);
        }
    }

    public static void tickRidingEntity(final net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        final EntityBridge mixinEntity = (EntityBridge) entity;
        if (!mixinEntity.bridge$shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext().source(entity);
        try (
             final EntityTickContext context = tickContext;
             final Timing entityTiming = ((TimingBridge) entity).bridge$getTimingsHandler()
             ) {
            entityTiming.startTiming();
            if (entity instanceof OwnershipTrackedBridge) {
                ((OwnershipTrackedBridge) entity).tracked$getNotifierReference()
                    .ifPresent(context::notifier);
                ((OwnershipTrackedBridge) entity).tracked$getOwnerReference()
                    .ifPresent(context::owner);
            }
            context.buildAndSwitch();
            entity.func_70098_U();
            if (ShouldFire.MOVE_ENTITY_EVENT_POSITION || ShouldFire.ROTATE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity, context);
            }
        } catch (Exception | NoClassDefFoundError e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, tickContext);
        }
    }

    @SuppressWarnings({"unused", "try"})
    public static void tickTileEntity(final WorldServerBridge mixinWorldServer, final ITickable tile) {
        checkArgument(tile instanceof TileEntity, "ITickable %s is not a TileEntity!", tile);
        checkNotNull(tile, "Cannot capture on a null ticking tile entity!");
        final net.minecraft.tileentity.TileEntity tileEntity = (net.minecraft.tileentity.TileEntity) tile;
        final TileEntityBridge mixinTileEntity = (TileEntityBridge) tile;
        final BlockPos pos = tileEntity.func_174877_v();
        final ChunkBridge chunk = ((ActiveChunkReferantBridge) tile).bridge$getActiveChunk();
        if (!mixinTileEntity.bridge$shouldTick()) {
            return;
        }
        if (chunk == null) {
            ((ActiveChunkReferantBridge) tile).bridge$setActiveChunk((ChunkBridge) tileEntity.func_145831_w().func_175726_f(tileEntity.func_174877_v()));
        }

        final TileEntityTickContext context = TickPhase.Tick.TILE_ENTITY.createPhaseContext().source(mixinTileEntity);
        try (final PhaseContext<?> phaseContext = context) {

            if (tile instanceof OwnershipTrackedBridge) {
                // Add notifier and owner so we don't have to perform lookups during the phases and other processing
                ((OwnershipTrackedBridge) tile).tracked$getNotifierReference().ifPresent(phaseContext::notifier);
                // Allow the tile entity to validate the owner of itself. As long as the tile entity
                // chunk is already loaded and activated, and the tile entity has already loaded
                // the owner of itself.
                ((OwnershipTrackedBridge) tile).tracked$getOwnerReference().ifPresent(phaseContext::owner);
            }

            // Finally, switch the context now that we have the owner and notifier
            phaseContext.buildAndSwitch();

            try (final Timing timing = ((TimingBridge) tileEntity).bridge$getTimingsHandler().startTiming()) {
                tile.func_73660_a();
            }
        } catch (Exception e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, context);
        }
        // We delay clearing active chunk if TE is invalidated during tick so we must remove it after
        if (tileEntity.func_145837_r()) {
            ((ActiveChunkReferantBridge) tileEntity).bridge$setActiveChunk(null);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void updateTickBlock(
            final WorldServerBridge mixinWorld, final Block block, final BlockPos pos, final net.minecraft.block.BlockState state, final Random random) {
        final ServerWorld world = (ServerWorld) mixinWorld;
        final World apiWorld = (World) world;

        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot snapshot = mixinWorld.bridge$createSnapshot(state, state, pos, BlockChangeFlags.NONE);
            final TickBlockEvent event = SpongeEventFactory.createTickBlockEventScheduled(Sponge.getCauseStackManager().getCurrentCause(), snapshot);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(apiWorld).position(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p()).state((BlockState)state).build();
        final BlockTickContext phaseContext = TickPhase.Tick.BLOCK.createPhaseContext().source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(mixinWorld, pos, currentContext, phaseContext);
        // Now actually switch to the new phase

        try (final PhaseContext<?> context = phaseContext;
             final Timing timing = ((TimingBridge) state.func_177230_c()).bridge$getTimingsHandler()) {
            timing.startTiming();
            context.buildAndSwitch();
            block.func_180650_b(world, pos, state, random);
        } catch (Exception | NoClassDefFoundError e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, phaseContext);

        }
    }

    @SuppressWarnings("rawtypes")
    public static void randomTickBlock(final WorldServerBridge mixinWorld, final Block block,
                                       final BlockPos pos, final net.minecraft.block.BlockState state, final Random random) {
        final ServerWorld world = (ServerWorld) mixinWorld;
        final World apiWorld = (World) world;

        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot currentTickBlock = mixinWorld.bridge$createSnapshot(state, state, pos, BlockChangeFlags.NONE);
            final TickBlockEvent
                event =
                SpongeEventFactory.createTickBlockEventRandom(Sponge.getCauseStackManager().getCurrentCause(), currentTickBlock);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(apiWorld).position(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p()).state((BlockState) state).build();
        final BlockTickContext phaseContext = TickPhase.Tick.RANDOM_BLOCK.createPhaseContext().source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(mixinWorld, pos, currentContext, phaseContext);
        // Now actually switch to the new phase
        try (final PhaseContext<?> context = phaseContext) {
            context.buildAndSwitch();
            block.func_180645_a(world, pos, state, random);
        } catch (Exception | NoClassDefFoundError e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, phaseContext);
        }
    }


    public static void tickWorldProvider(final WorldServerBridge worldServer) {
        final Dimension worldProvider = ((ServerWorld) worldServer).field_73011_w;
        try (final DimensionContext context = TickPhase.Tick.DIMENSION.createPhaseContext().source(worldProvider)) {
            context.buildAndSwitch();
            worldProvider.func_186059_r();
        }
    }

    public static boolean fireMinecraftBlockEvent(final ServerWorld worldIn, final BlockEventData event) {
        final net.minecraft.block.BlockState currentState = worldIn.func_180495_p(event.func_180328_a());
        final BlockEventDataBridge blockEvent = (BlockEventDataBridge) event;
        final Object source = blockEvent.bridge$getTileEntity() != null ? blockEvent.bridge$getTileEntity() : blockEvent.bridge$getTickingLocatable();
        if (source == null) {
            // No source present which means we are ignoring the phase state
            return currentState.func_189547_a(worldIn, event.func_180328_a(), event.func_151339_d(), event.func_151338_e());
        }
        final BlockEventTickContext phaseContext = TickPhase.Tick.BLOCK_EVENT.createPhaseContext();
        phaseContext.source(source);

        final User user = ((BlockEventDataBridge) event).bridge$getSourceUser();
        if (user != null) {
            phaseContext.owner = user;
            phaseContext.notifier = user;
        }

        try (final BlockEventTickContext o = phaseContext) {
            o.buildAndSwitch();
            phaseContext.setEventSucceeded(currentState.func_189547_a(worldIn, event.func_180328_a(), event.func_151339_d(), event.func_151338_e()));
        } // We can't return onBlockEventReceived because the phase state may have cancelled all transactions
        // at which point we want to keep track of the return value from the target, and from the block events.
        return phaseContext.wasNotCancelled();
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
        final ChunkBridge mixinChunk = (ChunkBridge) world.func_175726_f(blockPos);
        final User notifier = mixinChunk.bridge$getBlockNotifier(blockPos).orElse(null);
        if (notifier != null) {
            return notifier;
        }

        return mixinChunk.bridge$getBlockOwner(blockPos).orElse(null);
    }

    public static Supplier<IllegalStateException> throwWithContext(final String s, final PhaseContext<?> phaseContext) {
        return () -> {
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Exception trying to process over a phase!").centre().hr();
            printer.addWrapped(WIDTH, "%s : %s", "State", phaseContext.state);
            printer.addWrapped(WIDTH, "%s :", "PhaseContext");
            PhaseTracker.CONTEXT_PRINTER.accept(printer, phaseContext);
            printer.add("Stacktrace:");
            final IllegalStateException exception = new IllegalStateException(s + " Please analyze the current phase context. ");
            printer.add(exception);
            printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
            return exception;
        };
    }

    public static boolean processBlockCaptures(final PhaseContext<?> context) {
        return processBlockCaptures(context, 0, context.getCapturedBlockSupplier());
    }

    /**
     * Processes the given list of {@link BlockSnapshot}s and creates and throws and processes
     * the {@link ChangeBlockEvent}s as appropriately determined based on the {@link BlockChange}
     * for each snapshot. If any transactions are invalid or events cancelled, this event
     * returns {@code false} to signify a transaction was cancelled. This return value
     * is used for portal creation.
     *
     * @param context The phase context, only used by the phase for handling processes.
     * @param supplier
     * @return True if no events or transactions were cancelled
     */
    @SuppressWarnings({"unchecked"})
    static boolean processBlockCaptures(final PhaseContext<?> context, final int currentDepth, final MultiBlockCaptureSupplier supplier) {
        // Fail fast and check if it's empty.
        if (!supplier.hasBlocksCaptured()) {
            if (((IPhaseState) context.state).hasSpecificBlockProcess(context) && supplier.hasTransactions()) {
                // Then we just need to process the transactions, there may be things that are not
                // specifically block captured
                final ListMultimap<BlockPos, BlockEventData> scheduledEvents = supplier.getScheduledEvents();
                // Clear captured snapshots after processing them
                supplier.clear();
                return supplier.processTransactions(ImmutableList.of(), context, true, scheduledEvents, currentDepth);

            }
            return false;
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();

        final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[EVENT_COUNT];
        final ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[EVENT_COUNT];
        for (int i = 0; i < EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }

        createTransactionLists(context, supplier, transactionArrays, transactionBuilders);
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents = supplier.getScheduledEvents();

        // Clear captured snapshots after processing them
        supplier.clear();

        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];

        // Creates the block events accordingly to the transaction arrays
        iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents); // Needs to throw events
        // We create the post event and of course post it in the method, regardless whether any transactions are invalidated or not
        final ChangeBlockEvent.Post postEvent = throwMultiEventsAndCreatePost(context, transactionArrays, blockEvents, mainEvents);

        if (postEvent == null) { // Means that we have had no actual block changes apparently?
            return false;
        }

        final List<Transaction<BlockSnapshot>> invalid = new ArrayList<>();

        // Iterate through the block events to mark any transactions as invalid to accumilate after (since the post event contains all
        // transactions of the preceding block events)
        boolean noCancelledTransactions = checkCancelledEvents(blockEvents, postEvent, scheduledEvents, context, invalid);

        // Now we can gather the invalid transactions that either were marked as invalid from an event listener - OR - cancelled.
        // Because after, we will restore all the invalid transactions in reverse order.
        clearInvalidTransactionDrops(context, postEvent);

        if (!invalid.isEmpty()) {
            // We need to set this value and return it to signify that some transactions were cancelled
            noCancelledTransactions = false;
            rollBackTransactions(context, invalid);
            invalid.clear(); // Clear because we might re-enter for some reasons yet to be determined.

        }
        return performBlockAdditions(postEvent.getTransactions(), context, noCancelledTransactions, scheduledEvents, currentDepth);

    }

    private static void createTransactionLists(final PhaseContext<?> context, final MultiBlockCaptureSupplier supplier,
        final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, final ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders) {
        final List<SpongeBlockSnapshot> snapshots = supplier.get();
        for (final SpongeBlockSnapshot snapshot : snapshots) {
            // This processes each snapshot to assign them to the correct event in the next area, with the
            // correct builder array entry.
            context.getCapturedBlockSupplier().createTransaction(snapshot)
                .ifPresent(transaction -> TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(transaction));
        }
        for (int i = 0; i < EVENT_COUNT; i++) {
            // Build each event array
            transactionArrays[i] = transactionBuilders[i].build();
        }
    }

    private static boolean checkCancelledEvents(final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent.Post postEvent,
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents, final PhaseContext<?> context, final List<Transaction<BlockSnapshot>> invalid) {
        boolean noCancelledTransactions = true;
        for (final ChangeBlockEvent blockEvent : blockEvents) { // Need to only check if the event is cancelled, If it is, restore
            if (blockEvent.isCancelled()) {
                noCancelledTransactions = false;
                // Don't restore the transactions just yet, since we're just marking them as invalid for now
                for (final Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    if (!scheduledEvents.isEmpty()) {
                        scheduledEvents.removeAll(VecHelper.toBlockPos(transaction.getOriginal().getPosition()));
                    }
                    transaction.setValid(false);
                }
            }
        }
        if (postEvent.isCancelled()) {
            // Of course, if post is cancelled, just mark all transactions as invalid.
            noCancelledTransactions = false;
            for (final Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                if (!scheduledEvents.isEmpty()) {
                    scheduledEvents.removeAll(VecHelper.toBlockPos(transaction.getOriginal().getPosition()));
                }
                transaction.setValid(false);
            }
        }
        for (final Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                noCancelledTransactions = false;
            }
        }
        // Now to check, if any of the transactions being cancelled means cancelling the entire event.
        if (!noCancelledTransactions) {
            // This is available to verify only when necessary that a state
            // absolutely needs to cancel the entire transaction chain, this is mostly for more fasts
            // since we don't want to iterate over the transaction list multiple times.
            final boolean cancelAll = ((IPhaseState) context.state).getShouldCancelAllTransactions(context, blockEvents, postEvent, scheduledEvents, false);

            for (final Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                if (cancelAll) {
                    if (!scheduledEvents.isEmpty()) {
                        scheduledEvents.removeAll(VecHelper.toBlockPos(transaction.getOriginal().getPosition()));
                    }
                    transaction.setValid(false);
                    noCancelledTransactions = false;
                }
                if (!transaction.isValid()) {
                    invalid.add(transaction);
                }
            }
        }

        return noCancelledTransactions;
    }

    private static void clearInvalidTransactionDrops(final PhaseContext<?> context, final ChangeBlockEvent.Post postEvent) {
        for (final Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                // Cancel any block drops performed, avoids any item drops, regardless
                if (transaction.getOriginal() instanceof SpongeBlockSnapshot) {
                    final BlockPos pos = ((SpongeBlockSnapshot) transaction.getOriginal()).getBlockPos();
                    context.getBlockItemDropSupplier().removeAllIfNotEmpty(pos);
                    context.getPerBlockEntitySpawnSuppplier().removeAllIfNotEmpty(pos);
                }
            }
        }
    }

    private static void rollBackTransactions(final PhaseContext<?> context, final List<Transaction<BlockSnapshot>> invalid) {
        // NOW we restore the invalid transactions (remember invalid transactions are from either plugins marking them as invalid
        // or the events were cancelled), again in reverse order of which they were received.
        for (final Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
            transaction.getOriginal().restore(true, BlockChangeFlags.NONE);
            ((IPhaseState) context.state).processCancelledTransaction(context, transaction, transaction.getOriginal());
        }
    }

    private static void iterateChangeBlockEvents(final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, final List<ChangeBlockEvent> blockEvents,
        final ChangeBlockEvent[] mainEvents) {
        for (final BlockChange blockChange : BlockChange.values()) {
            if (blockChange == BlockChange.DECAY) { // Decay takes place after.
                continue;
            }
            if (!transactionArrays[blockChange.ordinal()].isEmpty()) {
                final ChangeBlockEvent event = blockChange.createEvent(Sponge.getCauseStackManager().getCurrentCause(), transactionArrays[blockChange.ordinal()]);
                mainEvents[blockChange.ordinal()] = event;
                SpongeImpl.postEvent(event);
                blockEvents.add(event);
            }
        }
        if (!transactionArrays[BlockChange.DECAY.ordinal()].isEmpty()) { // Needs to be placed into iterateChangeBlockEvents
            final ChangeBlockEvent event = BlockChange.DECAY.createEvent(Sponge.getCauseStackManager().getCurrentCause(), transactionArrays[BlockChange.DECAY.ordinal()]);
            mainEvents[BlockChange.DECAY.ordinal()] = event;
            SpongeImpl.postEvent(event);
            blockEvents.add(event);
        }
    }

    private static boolean performBlockAdditions(final List<Transaction<BlockSnapshot>> transactions,
        final PhaseContext<?> phaseContext, final boolean noCancelledTransactions,
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents,
        final int currentDepth) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        boolean hasEvents = false;
        if (!scheduledEvents.isEmpty()) {
            hasEvents = true;
        }
        if (((IPhaseState) phaseContext.state).hasSpecificBlockProcess(phaseContext)) {
            // In some states, we need to be taking advantage of processing the transactions in the order in which they
            // were processed. This means recycling some usage of how transactions are processed, but at the same time
            // processing the ORDER of them differently (since some notifications or block events can be thrown around
            // from time to time
            return phaseContext.getCapturedBlockSupplier().processTransactions(transactions, phaseContext, noCancelledTransactions, scheduledEvents, currentDepth);
        }
        for (final Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue;
            }
            performTransactionProcess(transaction, phaseContext, currentDepth);
        }
        phaseContext.getCapturedBlockSupplier().clearProxies();
        return noCancelledTransactions;
    }

    /**
     * The heart of all that is chaos. If you're reading this... well.. Let me explain it to you..
     * Based on the provided transaction, pulling from the original block and new {@link IBlockState},
     * we can perform physics such as {@link Block#onBlockAdded(net.minecraft.world.World, BlockPos, IBlockState)}
     * and notify neighbors. It is important that this method is replicated based on a combination of
     * {@link net.minecraft.world.World#setBlockState(BlockPos, IBlockState, int)} and
     * {@link Chunk#setBlockState(BlockPos, IBlockState)} as various "physics" and "notification" operations
     * are performed in precise order. This method is utilized in both bulk and non-bulk captures when
     * an event is required to be thrown. The deterministic requirement to know whether a bulk capture
     * is being performed or not is with the provided {@link IPhaseState} itself.
     * @param transaction The transaction to perform
     * @param phaseContext The currently working phase context
     * @param currentDepth The current processing depth, to avoid stack overflows
     */
    public static void performTransactionProcess(final Transaction<BlockSnapshot> transaction, final PhaseContext<?> phaseContext, final int currentDepth) {
        // Handle custom replacements - these need to get actually set onto the chunk, but ignored as far as tracking
        // goes.
        if (transaction.getCustom().isPresent()) {
            // Custom replacements should not trigger any physics or notifications, except for sending the notification to
            // a client. Meaning the intermediary block changes will also be ignored.
            transaction.getFinal().restore(true, BlockChangeFlags.NONE);
        }

        final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
        final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();

        // Handle item drops captured
        final Optional<ServerWorld> worldServer = oldBlockSnapshot.getWorldServer();
        if (!worldServer.isPresent()) {
            // Emit a log warning about a missing world
            final String transactionForLogging = MoreObjects.toStringHelper("Transaction")
                .add("World", oldBlockSnapshot.getWorldUniqueId())
                .add("Position", oldBlockSnapshot.getBlockPos())
                .add("Original State", oldBlockSnapshot.getState())
                .add("Changed State", newBlockSnapshot.getState())
                .toString();
            SpongeImpl.getLogger().warn("Unloaded/Missing World for a captured block change! Skipping change: " + transactionForLogging);
            return;
        }
        final WorldServerBridge mixinWorld = (WorldServerBridge) worldServer.get();
        // Reset any previously set transactions
        final BlockPos pos = oldBlockSnapshot.getBlockPos();
        performBlockEntitySpawns( phaseContext.state, phaseContext, oldBlockSnapshot, pos);

        final ServerWorld world = (ServerWorld) mixinWorld;
        SpongeHooks.logBlockAction(world, oldBlockSnapshot.blockChange, transaction);
        final SpongeBlockChangeFlag originalChangeFlag = oldBlockSnapshot.getChangeFlag();
        final net.minecraft.block.BlockState originalState = (net.minecraft.block.BlockState) oldBlockSnapshot.getState();
        final net.minecraft.block.BlockState newState = (net.minecraft.block.BlockState) newBlockSnapshot.getState();
        // So basically, the gist is this: If we have intermediary states during captures, we want to process the states
        // in the order in which they were applied. The issue is that since some changes end up having "don't tell clients about this, but tell clients about that"
        // flags, we have to abide by the changes accordingly. Likewise, this interacts with neighbor notifications being performed.
        if (transaction.getIntermediary().isEmpty()) {
            // We call onBlockAdded here for blocks without a TileEntity.
            // ChunkMixin#bridge$setBlockState will call onBlockAdded for blocks
            // with a TileEntity or when capturing is not being done.
            performOnBlockAdded(phaseContext, currentDepth, pos, world, originalChangeFlag, originalState, newState);

            ((IPhaseState)  phaseContext.state).postBlockTransactionApplication(oldBlockSnapshot.blockChange, transaction, phaseContext);

            if (originalChangeFlag.isNotifyClients()) { // Always try to notify clients of the change.
                world.func_184138_a(pos, originalState, newState, originalChangeFlag.getRawFlag());
            }

            performNeighborAndClientNotifications(phaseContext, currentDepth, newBlockSnapshot, mixinWorld, pos, newState, originalChangeFlag);
        }
        net.minecraft.block.BlockState previousIntermediary = originalState;
        boolean processedOriginal = false;
        for (final Iterator<? extends BlockSnapshot> iterator = transaction.getIntermediary().iterator(); iterator.hasNext();) {
            final SpongeBlockSnapshot intermediary = (SpongeBlockSnapshot) iterator.next();
            final SpongeBlockChangeFlag intermediaryChangeFlag = intermediary.getChangeFlag();
            final net.minecraft.block.BlockState intermediaryState = (net.minecraft.block.BlockState) intermediary.getState();
            // We have to process the original block change (since it's not part of the intermediary changes)
            // as a original -> intermediary
            if (!processedOriginal) {
                performOnBlockAdded(phaseContext, currentDepth, pos, world, originalChangeFlag, originalState, intermediaryState);
                if (originalChangeFlag.isNotifyClients()) {
                    world.func_184138_a(pos, originalState, intermediaryState, originalChangeFlag.getRawFlag());
                }
                performNeighborAndClientNotifications(phaseContext, currentDepth, intermediary, mixinWorld, pos, intermediaryState, originalChangeFlag);
                processedOriginal = true;
            }
            // Then, we can process the intermediary to final potentially if there is only the original -> intermediary -> final,
            // whereas if there's more than one intermediary, the intermediary will refer to the previous intermediary
            // block state for appropriate physics.
            final boolean isFinal = !iterator.hasNext();
            performOnBlockAdded(phaseContext, currentDepth, pos, world, intermediaryChangeFlag, isFinal ? intermediaryState : previousIntermediary, isFinal ? newState : intermediaryState);
            if (intermediaryChangeFlag.isNotifyClients()) {
                world.func_184138_a(pos, isFinal ? intermediaryState :  previousIntermediary, isFinal ? newState : intermediaryState, intermediaryChangeFlag.getRawFlag());
            }
            performNeighborAndClientNotifications(phaseContext, currentDepth, isFinal ? newBlockSnapshot : intermediary, mixinWorld, pos, isFinal ? newState : intermediaryState, intermediaryChangeFlag);
            if (isFinal) {
                return;
            }
            previousIntermediary = intermediaryState;
        }
    }

    private static void performOnBlockAdded(final PhaseContext<?> phaseContext, final int currentDepth, final BlockPos pos, final ServerWorld world,
        final SpongeBlockChangeFlag changeFlag, final net.minecraft.block.BlockState originalState, final net.minecraft.block.BlockState newState) {
        final Block newBlock = newState.func_177230_c();
        if (originalState.func_177230_c() != newBlock && changeFlag.performBlockPhysics()
            && (!SpongeImplHooks.hasBlockTileEntity(newBlock, newState))) {
            newBlock.func_176213_c(world, pos, newState);
            ((IPhaseState) phaseContext.state).performOnBlockAddedSpawns(phaseContext, currentDepth + 1);
        }
    }

    public static void performNeighborAndClientNotifications(final PhaseContext<?> phaseContext, final int currentDepth,
                                                             final SpongeBlockSnapshot newBlockSnapshot, final WorldServerBridge mixinWorld, final BlockPos pos,
                                                             final net.minecraft.block.BlockState newState, final SpongeBlockChangeFlag changeFlag) {
        final Block newBlock = newState.func_177230_c();
        final IPhaseState phaseState = phaseContext.state;
        if (changeFlag.updateNeighbors()) { // Notify neighbors only if the change flag allowed it.
            // Append the snapshot being applied that is allowing us to keep track of which source is
            // performing the notification, it's quick and dirty.
            // TODO - somehow make this more functional so we're not relying on fields.
            final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
            final BlockSnapshot previousNeighbor = context.neighborNotificationSource;
            context.neighborNotificationSource = newBlockSnapshot;
            if (changeFlag.updateNeighbors()) {
                ((ServerWorld) mixinWorld).func_175722_b(pos, newState.func_177230_c(), changeFlag.notifyObservers());

                if (newState.func_185912_n()) {
                    ((ServerWorld) mixinWorld).func_175666_e(pos, newState.func_177230_c());
                }
            }
            context.neighborNotificationSource = previousNeighbor;
        } else if (changeFlag.notifyObservers()) {
            ((net.minecraft.world.World) mixinWorld).func_190522_c(pos, newBlock);
        }

        phaseState.performPostBlockNotificationsAndNeighborUpdates(phaseContext, newState, changeFlag, currentDepth + 1);
    }

    public static void performBlockEntitySpawns(final IPhaseState<?> state, final PhaseContext<?> phaseContext, final SpongeBlockSnapshot oldBlockSnapshot,
        final BlockPos pos) {
        // This is for pre-merged items
        if (state.doesCaptureEntitySpawns() || ((IPhaseState) state).doesCaptureEntityDrops(phaseContext)) {
            phaseContext.getBlockDropSupplier().acceptAndRemoveIfPresent(pos, items ->
                spawnItemDataForBlockDrops(items, oldBlockSnapshot, phaseContext));
            // And this is for un-pre-merged items, these will be EntityItems, not ItemDropDatas.
            phaseContext.getBlockItemDropSupplier().acceptAndRemoveIfPresent(pos, items ->
                spawnItemEntitiesForBlockDrops(items, oldBlockSnapshot, phaseContext));
            // This is for entities actually spawned
            phaseContext.getPerBlockEntitySpawnSuppplier().acceptAndRemoveIfPresent(pos, items ->
                spawnEntitiesForBlock(items, phaseContext));
        }
    }

    private static void spawnItemEntitiesForBlockDrops(final Collection<ItemEntity> entityItems, final BlockSnapshot newBlockSnapshot,
        final PhaseContext<?> phaseContext) {
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = entityItems.stream()
                .map(entity -> (Entity) entity)
                .collect(Collectors.toList());
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(newBlockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            phaseContext.applyNotifierIfAvailable(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
            SpongeCommonEventFactory.callDropItemDestruct(itemDrops, phaseContext);
        }
    }

    public static void spawnItemDataForBlockDrops(final Collection<ItemDropData> itemStacks, final BlockSnapshot oldBlockSnapshot,
        final PhaseContext<?> phaseContext) {
        final Vector3i position = oldBlockSnapshot.getPosition();
        final List<ItemStackSnapshot> itemSnapshots = itemStacks.stream()
                .map(ItemDropData::getStack)
                .map(ItemStackUtil::snapshotOf)
                .collect(Collectors.toList());
        final ImmutableList<ItemStackSnapshot> originalSnapshots = ImmutableList.copyOf(itemSnapshots);
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(oldBlockSnapshot);
            final DropItemEvent.Pre
                dropItemEventPre =
                SpongeEventFactory.createDropItemEventPre(frame.getCurrentCause(), originalSnapshots, itemSnapshots);
            SpongeImpl.postEvent(dropItemEventPre);
            if (dropItemEventPre.isCancelled()) {
                return;
            }
        }
        final Location<World> worldLocation = oldBlockSnapshot.getLocation().get();
        final World world = worldLocation.getExtent();
        final ServerWorld worldServer = (ServerWorld) world;
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = itemStacks.stream().map(itemStack -> {
                    final net.minecraft.item.ItemStack minecraftStack = itemStack.getStack();
                    float f = 0.5F;
                    double offsetX = worldServer.field_73012_v.nextFloat() * f + (1.0F - f) * 0.5D;
                    double offsetY = worldServer.field_73012_v.nextFloat() * f + (1.0F - f) * 0.5D;
                    double offsetZ = worldServer.field_73012_v.nextFloat() * f + (1.0F - f) * 0.5D;
                    final double x = position.getX() + offsetX;
                    final double y = position.getY() + offsetY;
                    final double z = position.getZ() + offsetZ;
                    ItemEntity entityitem = new ItemEntity(worldServer, x, y, z, minecraftStack);
                    entityitem.func_174869_p();
                    return entityitem;
                })
                .map(entity -> (Entity) entity)
                .collect(Collectors.toList());
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(oldBlockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            phaseContext.applyNotifierIfAvailable(notifier ->  frame.addContext(EventContextKeys.NOTIFIER, notifier));
            SpongeCommonEventFactory.callDropItemDestruct(itemDrops, phaseContext);
        }
    }

    private static void spawnEntitiesForBlock(final Collection<net.minecraft.entity.Entity> entities, final PhaseContext<?> phaseContext) {
        // Now we can spawn the entity items appropriately
        final List<Entity> entitiesSpawned = entities.stream()
            .map(entity -> (Entity) entity)
            .collect(Collectors.toList());
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            SpongeCommonEventFactory.callSpawnEntity(entitiesSpawned, phaseContext);
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    private static ChangeBlockEvent.Post throwMultiEventsAndCreatePost(final PhaseContext<?> context,
        final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays,
        final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent[] mainEvents) {
        if (!blockEvents.isEmpty()) {
            final ImmutableList<Transaction<BlockSnapshot>> transactions = transactionArrays[MULTI_CHANGE_INDEX];
            // We suffix the cause with the extra events, without modifying the cause stack manager to avoid adding extra
            // contexts or resetting the caches, this allows us to avoid adding extra frames when unnecessary.
            final Cause currentCause = Sponge.getCauseStackManager().getCurrentCause();
            final Cause causeToUse;
            if (((IPhaseState) context.state).shouldProvideModifiers(context)) {
                final Cause.Builder builder = Cause.builder().from(currentCause);
                final EventContext.Builder modified = EventContext.builder();
                modified.from(currentCause.getContext());
                for (final BlockChange blockChange : BlockChange.values()) {
                    final ChangeBlockEvent mainEvent = mainEvents[blockChange.ordinal()];
                    if (mainEvent != null) {
                        builder.append(mainEvent);
                        modified.add((EventContextKey<? super ChangeBlockEvent>) blockChange.getKey(), mainEvent);
                    }
                }
                causeToUse = builder.build(modified.build());
            } else {
                causeToUse = currentCause;
            }
            final ChangeBlockEvent.Post post = ((IPhaseState) context.state).createChangeBlockPostEvent(context, transactions, causeToUse);
            SpongeImpl.postEvent(post);
            return post;
        }
        return null;
    }

    public static void associateTrackerToTarget(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> transaction, final User user) {
        final BlockSnapshot finalSnapshot = transaction.getFinal();
        final SpongeBlockSnapshot spongeSnapshot = (SpongeBlockSnapshot) finalSnapshot;
        final BlockPos pos = spongeSnapshot.getBlockPos();
        final Block block = ((net.minecraft.block.BlockState) spongeSnapshot.getState()).func_177230_c();
        spongeSnapshot.getWorldServer()
            .map(world -> world.func_175726_f(pos))
            .map(chunk -> (ChunkBridge) chunk)
            .ifPresent(spongeChunk -> {
            final PlayerTracker.Type trackerType = blockChange == BlockChange.PLACE ? PlayerTracker.Type.OWNER : PlayerTracker.Type.NOTIFIER;
            spongeChunk.bridge$addTrackedBlockPosition(block, pos, user, trackerType);
        });
    }

    public static void addTileEntityToBuilder(@Nullable final net.minecraft.tileentity.TileEntity existing, final SpongeBlockSnapshotBuilder builder) {
        // We MUST only check to see if a TE exists to avoid creating a new one.
        final TileEntity tile = (TileEntity) existing;
        for (final DataManipulator<?, ?> manipulator : ((CustomDataHolderBridge) tile).bridge$getCustomManipulators()) {
            builder.add(manipulator);
        }
        final CompoundNBT nbt = new CompoundNBT();
        // Some mods like OpenComputers assert if attempting to save robot while moving
        try {
            existing.func_189515_b(nbt);
            builder.unsafeNbt(nbt);
        }
        catch(Throwable t) {
            // ignore
        }
    }

    public static String phaseStateToString(final String type, final IPhaseState<?> state) {
        return phaseStateToString(type, null, state);
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
}
