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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
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
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
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
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.block.state.MixinIBlockState;
import org.spongepowered.common.mixin.core.block.state.MixinStateImplementation;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.common.world.WorldUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
@SuppressWarnings("unchecked")
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
    static final Function<SpongeBlockSnapshot, Transaction<BlockSnapshot>> TRANSACTION_CREATION = (blockSnapshot) -> {
        final WorldServer worldServer =  blockSnapshot.getWorldServer();
        final BlockPos blockPos = blockSnapshot.getBlockPos();
        final IBlockState newState = worldServer.getBlockState(blockPos);
        final IBlockState newActualState = newState.getActualState(worldServer, blockPos);
        final BlockSnapshot newSnapshot = ((IMixinWorldServer) worldServer).createSpongeBlockSnapshot(newState, newActualState, blockPos, BlockChangeFlags.NONE);
        return new Transaction<>(blockSnapshot, newSnapshot);
    };

    public static void tickEntity(net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        final IMixinEntity mixinEntity = EntityUtil.toMixin(entity);
        if (!mixinEntity.shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext().source(entity);
        try (final EntityTickContext context = tickContext;
             final Timing entityTiming = mixinEntity.getTimingsHandler()
        ) {
            mixinEntity.getNotifierUser()
                    .ifPresent(context::notifier);
            mixinEntity.getCreatorUser()
                    .ifPresent(context::owner);
            context.buildAndSwitch();
            entityTiming.startTiming();
            entity.onUpdate();
            if (ShouldFire.MOVE_ENTITY_EVENT_POSITION || ShouldFire.ROTATE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity, context);
            }
        } catch (Exception | NoClassDefFoundError e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, tickContext);
        }
    }

    public static void tickRidingEntity(net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        final IMixinEntity mixinEntity = EntityUtil.toMixin(entity);
        if (!mixinEntity.shouldTick()) {
            return;
        }

        final EntityTickContext tickContext = TickPhase.Tick.ENTITY.createPhaseContext().source(entity);
        try (
             final EntityTickContext context = tickContext;
             final Timing entityTiming = mixinEntity.getTimingsHandler()
             ) {
            entityTiming.startTiming();
            mixinEntity.getNotifierUser()
                .ifPresent(context::notifier);
            mixinEntity.getCreatorUser()
                .ifPresent(context::owner);
            context.buildAndSwitch();
            entity.updateRidden();
            if (ShouldFire.MOVE_ENTITY_EVENT_POSITION || ShouldFire.ROTATE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity, context);
            }
        } catch (Exception | NoClassDefFoundError e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, tickContext);
        }
    }

    @SuppressWarnings({"unused", "try"})
    public static void tickTileEntity(IMixinWorldServer mixinWorldServer, ITickable tile) {
        checkArgument(tile instanceof TileEntity, "ITickable %s is not a TileEntity!", tile);
        checkNotNull(tile, "Cannot capture on a null ticking tile entity!");
        final net.minecraft.tileentity.TileEntity tileEntity = (net.minecraft.tileentity.TileEntity) tile;
        final IMixinTileEntity mixinTileEntity = (IMixinTileEntity) tile;
        final BlockPos pos = tileEntity.getPos();
        final IMixinChunk chunk = ((IMixinTileEntity) tile).getActiveChunk();
        if (!mixinTileEntity.shouldTick()) {
            return;
        }
        if (chunk == null) {
            mixinTileEntity.setActiveChunk((IMixinChunk) tileEntity.getWorld().getChunk(tileEntity.getPos()));
        }

        final TileEntityTickContext context = TickPhase.Tick.TILE_ENTITY.createPhaseContext().source(mixinTileEntity);
        try (final PhaseContext<?> phaseContext = context) {

            // Add notifier and owner so we don't have to perform lookups during the phases and other processing
            final User blockNotifier = mixinTileEntity.getSpongeNotifier();
            if (blockNotifier != null) {
                phaseContext.notifier(blockNotifier);
            }

            // Allow the tile entity to validate the owner of itself. As long as the tile entity
            // chunk is already loaded and activated, and the tile entity has already loaded
            // the owner of itself.
            final User blockOwner = mixinTileEntity.getSpongeOwner();
            if (blockOwner != null) {
                phaseContext.owner(blockOwner);
            }

            // Finally, switch the context now that we have the owner and notifier
            phaseContext.buildAndSwitch();

            mixinTileEntity.setIsTicking(true);
            try (Timing timing = mixinTileEntity.getTimingsHandler().startTiming()) {
                tile.update();
            }
        } catch (Exception e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, context);
        }
        // We delay clearing active chunk if TE is invalidated during tick so we must remove it after
        if (tileEntity.isInvalid()) {
            mixinTileEntity.setActiveChunk(null);
        }
        mixinTileEntity.setIsTicking(false);
    }

    @SuppressWarnings("rawtypes")
    public static void updateTickBlock(IMixinWorldServer mixinWorld, Block block, BlockPos pos, IBlockState state, Random random) {
        final WorldServer world = WorldUtil.asNative(mixinWorld);
        final World apiWorld = WorldUtil.fromNative(world);

        if (ShouldFire.TICK_BLOCK_EVENT) {
            BlockSnapshot snapshot = mixinWorld.createSpongeBlockSnapshot(state, state, pos, BlockChangeFlags.NONE);
            final TickBlockEvent event = SpongeEventFactory.createTickBlockEventScheduled(Sponge.getCauseStackManager().getCurrentCause(), snapshot);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(apiWorld).position(pos.getX(), pos.getY(), pos.getZ()).state((BlockState)state).build();
        final BlockTickContext phaseContext = TickPhase.Tick.BLOCK.createPhaseContext().source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(mixinWorld, pos, currentContext, phaseContext);
        // Now actually switch to the new phase

        try (final PhaseContext<?> context = phaseContext;
             final Timing timing = BlockUtil.toMixin(state).getTimingsHandler()) {
            timing.startTiming();
            context.buildAndSwitch();
            block.updateTick(world, pos, state, random);
        } catch (Exception | NoClassDefFoundError e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, phaseContext);

        }
    }

    @SuppressWarnings("rawtypes")
    public static void randomTickBlock(IMixinWorldServer mixinWorld, Block block,
        BlockPos pos, IBlockState state, Random random) {
        final WorldServer world = WorldUtil.asNative(mixinWorld);
        final World apiWorld = WorldUtil.fromNative(world);

        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot currentTickBlock = mixinWorld.createSpongeBlockSnapshot(state, state, pos, BlockChangeFlags.NONE);
            final TickBlockEvent
                event =
                SpongeEventFactory.createTickBlockEventRandom(Sponge.getCauseStackManager().getCurrentCause(), currentTickBlock);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world(apiWorld).position(pos.getX(), pos.getY(), pos.getZ()).state((BlockState) state).build();
        final BlockTickContext phaseContext = TickPhase.Tick.RANDOM_BLOCK.createPhaseContext().source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = currentContext.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(mixinWorld, pos, currentContext, phaseContext);
        // Now actually switch to the new phase
        try (PhaseContext<?> context = phaseContext) {
            context.buildAndSwitch();
            block.randomTick(world, pos, state, random);
        } catch (Exception | NoClassDefFoundError e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, phaseContext);
        }
    }


    public static void tickWorldProvider(IMixinWorldServer worldServer) {
        final WorldProvider worldProvider = ((WorldServer) worldServer).provider;
        try (DimensionContext context = TickPhase.Tick.DIMENSION.createPhaseContext().source(worldProvider)) {
            context.buildAndSwitch();
            worldProvider.onWorldUpdateEntities();
        }
    }

    public static boolean fireMinecraftBlockEvent(WorldServer worldIn, BlockEventData event) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        final IMixinBlockEventData blockEvent = (IMixinBlockEventData) event;
        final BlockEventTickContext phaseContext = TickPhase.Tick.BLOCK_EVENT.createPhaseContext();

        Object source = blockEvent.getTickTileEntity() != null ? blockEvent.getTickTileEntity() : blockEvent.getTickBlock();
        if (source != null) {
            phaseContext.source(source);
        } else {
            // No source present which means we are ignoring the phase state
            return currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
        }

        final User user = ((IMixinBlockEventData) event).getSourceUser();
        if (user != null) {
            phaseContext.owner = user;
            phaseContext.notifier = user;
        }

        try (BlockEventTickContext o = phaseContext) {
            o.buildAndSwitch();
            phaseContext.setEventSucceeded(currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter()));
        } // We can't return onBlockEventReceived because the phase state may have cancelled all transactions
        // at which point we want to keep track of the return value from the target, and from the block events.
        return phaseContext.wasNotCancelled();
    }

    static boolean forceModify(Block originalBlock, Block newBlock) {
        if (originalBlock instanceof BlockRedstoneRepeater && newBlock instanceof BlockRedstoneRepeater) {
            return true;
        } else if (originalBlock instanceof BlockRedstoneTorch && newBlock instanceof BlockRedstoneTorch) {
            return true;
        } else
            return originalBlock instanceof BlockRedstoneLight && newBlock instanceof BlockRedstoneLight;
    }

    private TrackingUtil() {
    }

    @Nullable
    public static User getNotifierOrOwnerFromBlock(WorldServer world, BlockPos blockPos) {
        final IMixinChunk mixinChunk = (IMixinChunk) world.getChunk(blockPos);
        User notifier = mixinChunk.getBlockNotifier(blockPos).orElse(null);
        if (notifier != null) {
            return notifier;
        }

        return mixinChunk.getBlockOwner(blockPos).orElse(null);
    }

    public static Supplier<IllegalStateException> throwWithContext(String s, PhaseContext<?> phaseContext) {
        return () -> {
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Exception trying to process over a phase!").centre().hr();
            printer.addWrapped(40, "%s : %s", "State", phaseContext.state);
            printer.addWrapped(40, "%s :", "PhaseContext");
            PhaseTracker.CONTEXT_PRINTER.accept(printer, phaseContext);
            printer.add("Stacktrace:");
            final IllegalStateException exception = new IllegalStateException(s + " Please analyze the current phase context. ");
            printer.add(exception);
            printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
            return exception;
        };
    }

    public static boolean processBlockCaptures(IPhaseState<?> state, PhaseContext<?> context) {
        return processBlockCaptures(state, context, 0);
    }

    /**
     * Processes the given list of {@link BlockSnapshot}s and creates and throws and processes
     * the {@link ChangeBlockEvent}s as appropriately determined based on the {@link BlockChange}
     * for each snapshot. If any transactions are invalid or events cancelled, this event
     * returns {@code false} to signify a transaction was cancelled. This return value
     * is used for portal creation.
     *
     * @param state The phase state that is being processed, used to handle marking notifiers
     *  and block owners
     * @param context The phase context, only used by the phase for handling processes.
     * @return True if no events or transactions were cancelled
     */
    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    static boolean processBlockCaptures(IPhaseState<?> state, PhaseContext<?> context, int currentDepth) {
        final MultiBlockCaptureSupplier snapshots = context.getCapturedBlockSupplier();
        // Fail fast and check if it's empty.
        if (!snapshots.hasBlocksCaptured()) {
            if (((IPhaseState) state).hasSpecificBlockProcess(context) && snapshots.hasTransactions()) {
                // Then we just need to process the transactions, there may be things that are not
                // specifically block captured
                ListMultimap<BlockPos, BlockEventData> scheduledEvents = snapshots.getScheduledEvents();
                // Clear captured snapshots after processing them
                snapshots.clear();
                return snapshots.processTransactions(ImmutableList.of(), context, true, scheduledEvents, currentDepth);

            }
            return false;
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();

        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[EVENT_COUNT];
        for (int i = 0; i < EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }

        createTransactionLists(state, context, transactionArrays, transactionBuilders);
        ListMultimap<BlockPos, BlockEventData> scheduledEvents = snapshots.getScheduledEvents();

        // Clear captured snapshots after processing them
        snapshots.clear();

        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
        // This likely needs to delegate to the phase in the event we don't use the source object as the main object causing the block changes
        // case in point for WorldTick event listeners since the players are captured non-deterministically
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // Creates the block events accordingly to the transaction arrays
            iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents); // Needs to throw events
            // We create the post event and of course post it in the method, regardless whether any transactions are invalidated or not
            final ChangeBlockEvent.Post postEvent = throwMultiEventsAndCreatePost(state, context, transactionArrays, blockEvents, mainEvents);

            if (postEvent == null) { // Means that we have had no actual block changes apparently?
                return false;
            }

            final List<Transaction<BlockSnapshot>> invalid = new ArrayList<>();

            // Iterate through the block events to mark any transactions as invalid to accumilate after (since the post event contains all
            // transactions of the preceeding block events)
            boolean noCancelledTransactions = checkCancelledEvents(blockEvents, postEvent, scheduledEvents, state, context, invalid);

            // Now we can gather the invalid transactions that either were marked as invalid from an event listener - OR - cancelled.
            // Because after, we will restore all the invalid transactions in reverse order.
            clearInvalidTransactionDrops(context, postEvent, invalid);

            if (!invalid.isEmpty()) {
                // We need to set this value and return it to signify that some transactions were cancelled
                noCancelledTransactions = false;
                rollBackTransactions(state, context, invalid);
                invalid.clear(); // Clear because we might re-enter for some reasons yet to be determined.

            }
            return performBlockAdditions(postEvent.getTransactions(), state, context, noCancelledTransactions, scheduledEvents, currentDepth);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void createTransactionLists(IPhaseState state, PhaseContext<?> context,
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders) {
        final List<SpongeBlockSnapshot> snapshots = context.getCapturedOriginalBlocksChanged();
        for (SpongeBlockSnapshot snapshot : snapshots) {
            // This processes each snapshot to assign them to the correct event in the next area, with the
            // correct builder array entry.
            ;
            TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(state.createTransaction(context, snapshot));
        }
        for (int i = 0; i < EVENT_COUNT; i++) {
            // Build each event array
            transactionArrays[i] = transactionBuilders[i].build();
        }
    }

    private static boolean checkCancelledEvents(List<ChangeBlockEvent> blockEvents, ChangeBlockEvent.Post postEvent,
        ListMultimap<BlockPos, BlockEventData> scheduledEvents, IPhaseState<?> state, PhaseContext<?> context, List<Transaction<BlockSnapshot>> invalid) {
        boolean noCancelledTransactions = true;
        for (ChangeBlockEvent blockEvent : blockEvents) { // Need to only check if the event is cancelled, If it is, restore
            if (blockEvent.isCancelled()) {
                noCancelledTransactions = false;
                // Don't restore the transactions just yet, since we're just marking them as invalid for now
                for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
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
            for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                scheduledEvents.removeAll(VecHelper.toBlockPos(transaction.getOriginal().getPosition()));
                transaction.setValid(false);
            }
        }
        for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                noCancelledTransactions = false;
            }
        }
        // Now to check, if any of the transactions being cancelled means cancelling the entire event.
        if (!noCancelledTransactions) {
            // This is available to verify only when necessary that a state
            // absolutely needs to cancel the entire transaction chain, this is mostly for more fasts
            // since we don't want to iterate over the transaction list multiple times.
            boolean cancelAll = ((IPhaseState) state).getShouldCancelAllTransactions(context, blockEvents, postEvent, scheduledEvents, noCancelledTransactions);

            for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
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

    private static void clearInvalidTransactionDrops(PhaseContext<?> context, ChangeBlockEvent.Post postEvent,
        List<Transaction<BlockSnapshot>> invalid) {
        for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                invalid.add(transaction);
                // Cancel any block drops performed, avoids any item drops, regardless
                if (transaction.getOriginal() instanceof SpongeBlockSnapshot) {
                    final BlockPos pos = ((SpongeBlockSnapshot) transaction.getOriginal()).getBlockPos();
                    context.getBlockItemDropSupplier().removeAllIfNotEmpty(pos);
                    context.getPerBlockEntitySpawnSuppplier().removeAllIfNotEmpty(pos);
                    context.getPerBlockEntitySpawnSuppplier().removeAllIfNotEmpty(pos);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void rollBackTransactions(IPhaseState<?> state, PhaseContext<?> context, List<Transaction<BlockSnapshot>> invalid) {
        // NOW we restore the invalid transactions (remember invalid transactions are from either plugins marking them as invalid
        // or the events were cancelled), again in reverse order of which they were received.
        for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
            transaction.getOriginal().restore(true, BlockChangeFlags.NONE);
            ((IPhaseState) state).processCancelledTransaction(context, transaction, transaction.getOriginal());
        }
    }

    private static void iterateChangeBlockEvents(ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, List<ChangeBlockEvent> blockEvents,
        ChangeBlockEvent[] mainEvents) {
        for (BlockChange blockChange : BlockChange.values()) {
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

    private static boolean performBlockAdditions(List<Transaction<BlockSnapshot>> transactions, IPhaseState<?> phaseState,
        PhaseContext<?> phaseContext, boolean noCancelledTransactions,
        ListMultimap<BlockPos, BlockEventData> scheduledEvents,
        int currentDepth) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        boolean hasEvents = false;
        if (!scheduledEvents.isEmpty()) {
            hasEvents = true;
        }
        if (((IPhaseState) phaseState).hasSpecificBlockProcess(phaseContext)) {
            // In some states, we need to be taking advantage of processing the transactions in the order in which they
            // were processed. This means recycling some usage of how transactions are processed, but at the same time
            // processing the ORDER of them differently (since some notifications or block events can be thrown around
            // from time to time
            return phaseContext.getCapturedBlockSupplier().processTransactions(transactions, phaseContext, noCancelledTransactions, scheduledEvents, currentDepth);
        }
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue;
            }
            final BlockPos pos = hasEvents ? VecHelper.toBlockPos(transaction.getOriginal().getPosition()) : null;
            final List<BlockEventData> events =  hasEvents ? scheduledEvents.get(pos) : Collections.emptyList();
            noCancelledTransactions = performTransactionProcess(transaction, phaseState, phaseContext, events, noCancelledTransactions, currentDepth);
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
     * @param phaseState The currently working phase state
     * @param phaseContext The currently working phase context
     * @param events
     * @param noCancelledTransactions Whether there's any cancelled transactions
     * @param currentDepth The current processing depth, to avoid stack overflows
     * @return True if the block transaction was successful
     */
    @SuppressWarnings("rawtypes")
    public static boolean performTransactionProcess(Transaction<BlockSnapshot> transaction, IPhaseState<?> phaseState, PhaseContext<?> phaseContext,
        List<BlockEventData> events, boolean noCancelledTransactions, int currentDepth) {
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
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) oldBlockSnapshot.getWorldServer();
        // Reset any previously set transactions
        final BlockPos pos = oldBlockSnapshot.getBlockPos();
        performBlockEntitySpawns(phaseState, phaseContext, oldBlockSnapshot, pos);

        final WorldServer world = WorldUtil.asNative(mixinWorld);
        SpongeHooks.logBlockAction(world, oldBlockSnapshot.blockChange, transaction);
        final SpongeBlockChangeFlag originalChangeFlag = oldBlockSnapshot.getChangeFlag();
        final IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
        final IBlockState newState = (IBlockState) newBlockSnapshot.getState();
        // So basically, the gist is this: If we have intermediary states during captures, we want to process the states
        // in the order in which they were applied. The issue is that since some changes end up having "don't tell clients about this, but tell clients about that"
        // flags, we have to abide by the changes accordingly. Likewise, this interacts with neighbor notifications being performed.
        if (transaction.getIntermediary().isEmpty()) {
            // We call onBlockAdded here for blocks without a TileEntity.
            // MixinChunk#setBlockState will call onBlockAdded for blocks
            // with a TileEntity or when capturing is not being done.
            performOnBlockAdded(phaseState, phaseContext, currentDepth, pos, world, originalChangeFlag, originalState, newState);

            ((IPhaseState) phaseState).postBlockTransactionApplication(oldBlockSnapshot.blockChange, transaction, phaseContext);

            if (originalChangeFlag.isNotifyClients()) { // Always try to notify clients of the change.
                world.notifyBlockUpdate(pos, originalState, newState, originalChangeFlag.getRawFlag());
            }

            performNeighborAndClientNotifications(phaseContext, currentDepth, newBlockSnapshot, mixinWorld, pos, newState, originalChangeFlag);
        }
        IBlockState previousIntermediary = originalState;
        boolean processedOriginal = false;
        for (Iterator<? extends BlockSnapshot> iterator = transaction.getIntermediary().iterator(); iterator.hasNext();) {
            final SpongeBlockSnapshot intermediary = (SpongeBlockSnapshot) iterator.next();
            final SpongeBlockChangeFlag intermediaryChangeFlag = intermediary.getChangeFlag();
            final IBlockState intermediaryState = (IBlockState) intermediary.getState();
            // We have to process the original block change (since it's not part of the intermediary changes)
            // as a original -> intermediary
            if (!processedOriginal) {
                performOnBlockAdded(phaseState, phaseContext, currentDepth, pos, world, originalChangeFlag, originalState, intermediaryState);
                if (originalChangeFlag.isNotifyClients()) {
                    world.notifyBlockUpdate(pos, originalState, intermediaryState, originalChangeFlag.getRawFlag());
                }
                performNeighborAndClientNotifications(phaseContext, currentDepth, intermediary, mixinWorld, pos, intermediaryState, originalChangeFlag);
                processedOriginal = true;
            }
            // Then, we can process the intermediary to final potentially if there is only the original -> intermediary -> final,
            // whereas if there's more than one intermediary, the intermediary will refer to the previous intermediary
            // block state for appropriate physics.
            boolean isFinal = !iterator.hasNext();
            performOnBlockAdded(phaseState, phaseContext, currentDepth, pos, world, intermediaryChangeFlag, isFinal ? intermediaryState : previousIntermediary, isFinal ? newState : intermediaryState);
            if (intermediaryChangeFlag.isNotifyClients()) {
                world.notifyBlockUpdate(pos, isFinal ? intermediaryState :  previousIntermediary, isFinal ? newState : intermediaryState, intermediaryChangeFlag.getRawFlag());
            }
            performNeighborAndClientNotifications(phaseContext, currentDepth, isFinal ? newBlockSnapshot : intermediary, mixinWorld, pos, isFinal ? newState : intermediaryState, intermediaryChangeFlag);
            if (isFinal) {
                return noCancelledTransactions;
            }
            previousIntermediary = intermediaryState;
        }

        return noCancelledTransactions;
    }

    public static void performOnBlockAdded(IPhaseState phaseState, PhaseContext<?> phaseContext, int currentDepth, BlockPos pos, WorldServer world,
        SpongeBlockChangeFlag changeFlag, IBlockState originalState, IBlockState newState) {
        final Block newBlock = newState.getBlock();
        if (originalState.getBlock() != newBlock && changeFlag.performBlockPhysics()
            && (!SpongeImplHooks.hasBlockTileEntity(newBlock, newState))) {
            newBlock.onBlockAdded(world, pos, newState);
            phaseState.performOnBlockAddedSpawns(phaseContext, currentDepth + 1);
        }
    }

    public static void performNeighborAndClientNotifications(PhaseContext<?> phaseContext, int currentDepth,
        SpongeBlockSnapshot newBlockSnapshot, IMixinWorldServer mixinWorld, BlockPos pos,
        IBlockState newState, SpongeBlockChangeFlag changeFlag) {
        final Block newBlock = newState.getBlock();
        final IPhaseState phaseState = phaseContext.state;
        if (changeFlag.updateNeighbors()) { // Notify neighbors only if the change flag allowed it.
            // Append the snapshot being applied that is allowing us to keep track of which source is
            // performing the notification, it's quick and dirty.
            // TODO - somehow make this more functional so we're not relying on fields.
            final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
            final BlockSnapshot previousNeighbor = context.neighborNotificationSource;
            context.neighborNotificationSource = newBlockSnapshot;
            mixinWorld.spongeNotifyNeighborsPostBlockChange(pos, newState, changeFlag);
            context.neighborNotificationSource = previousNeighbor;
        } else if (changeFlag.notifyObservers()) {
            ((net.minecraft.world.World) mixinWorld).updateObservingBlocksAt(pos, newBlock);
        }

        phaseState.performPostBlockNotificationsAndNeighborUpdates(phaseContext, newState, changeFlag, currentDepth + 1);
    }

    public static void performBlockEntitySpawns(IPhaseState<?> state, PhaseContext<?> phaseContext, SpongeBlockSnapshot oldBlockSnapshot,
        BlockPos pos) {
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

    private static void spawnItemEntitiesForBlockDrops(Collection<EntityItem> entityItems, BlockSnapshot newBlockSnapshot,
        PhaseContext<?> phaseContext) {
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = entityItems.stream()
                .map(EntityUtil::fromNative)
                .collect(Collectors.toList());
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(newBlockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            phaseContext.applyNotifierIfAvailable(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
            SpongeCommonEventFactory.callDropItemDestruct(itemDrops, phaseContext);
        }
    }

    public static void spawnItemDataForBlockDrops(Collection<ItemDropData> itemStacks, BlockSnapshot oldBlockSnapshot,
        PhaseContext<?> phaseContext) {
        final Vector3i position = oldBlockSnapshot.getPosition();
        final List<ItemStackSnapshot> itemSnapshots = itemStacks.stream()
                .map(ItemDropData::getStack)
                .map(ItemStackUtil::snapshotOf)
                .collect(Collectors.toList());
        final ImmutableList<ItemStackSnapshot> originalSnapshots = ImmutableList.copyOf(itemSnapshots);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(oldBlockSnapshot);
            final DropItemEvent.Pre
                dropItemEventPre =
                SpongeEventFactory.createDropItemEventPre(frame.getCurrentCause(), originalSnapshots, itemSnapshots);
            SpongeImpl.postEvent(dropItemEventPre);
            if (dropItemEventPre.isCancelled()) {
                return;
            }
        }
        Location<World> worldLocation = oldBlockSnapshot.getLocation().get();
        final World world = worldLocation.getExtent();
        final WorldServer worldServer = (WorldServer) world;
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = itemStacks.stream().map(itemStack -> {
                    final net.minecraft.item.ItemStack minecraftStack = itemStack.getStack();
                    float f = 0.5F;
                    double offsetX = worldServer.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double offsetY = worldServer.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double offsetZ = worldServer.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    final double x = position.getX() + offsetX;
                    final double y = position.getY() + offsetY;
                    final double z = position.getZ() + offsetZ;
                    EntityItem entityitem = new EntityItem(worldServer, x, y, z, minecraftStack);
                    entityitem.setDefaultPickupDelay();
                    return entityitem;
                })
                .map(EntityUtil::fromNative)
                .collect(Collectors.toList());
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(oldBlockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            phaseContext.applyNotifierIfAvailable(notifier ->  frame.addContext(EventContextKeys.NOTIFIER, notifier));
            SpongeCommonEventFactory.callDropItemDestruct(itemDrops, phaseContext);
        }
    }

    private static void spawnEntitiesForBlock(Collection<net.minecraft.entity.Entity> entities, PhaseContext<?> phaseContext) {
        // Now we can spawn the entity items appropriately
        final List<Entity> entitiesSpawned = entities.stream()
            .map(EntityUtil::fromNative)
            .collect(Collectors.toList());
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            SpongeCommonEventFactory.callSpawnEntity(entitiesSpawned, phaseContext);
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    private static ChangeBlockEvent.Post throwMultiEventsAndCreatePost(IPhaseState<?> state,
        PhaseContext<?> context, ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays,
        final List<ChangeBlockEvent> blockEvents, ChangeBlockEvent[] mainEvents) {
        if (!blockEvents.isEmpty()) {
            final ImmutableList<Transaction<BlockSnapshot>> transactions = transactionArrays[MULTI_CHANGE_INDEX];
            // We suffix the cause with the extra events, without modifying the cause stack manager to avoid adding extra
            // contexts or resetting the caches, this allows us to avoid adding extra frames when unnecessary.
            final Cause currentCause = Sponge.getCauseStackManager().getCurrentCause();
            final Cause causeToUse;
            if (((IPhaseState) state).shouldProvideModifiers(context)) {
                final Cause.Builder builder = Cause.builder().from(currentCause);
                final EventContext.Builder modified = EventContext.builder();
                modified.from(currentCause.getContext());
                for (BlockChange blockChange : BlockChange.values()) {
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
            final ChangeBlockEvent.Post post = ((IPhaseState) state).createChangeBlockPostEvent(context, transactions, causeToUse);
            SpongeImpl.postEvent(post);
            return post;
        }
        return null;
    }

    public static void associateTrackerToTarget(BlockChange blockChange, Transaction<BlockSnapshot> transaction, User user) {
        final BlockSnapshot finalSnapshot = transaction.getFinal();
        final SpongeBlockSnapshot spongeSnapshot = (SpongeBlockSnapshot) finalSnapshot;
        final BlockPos pos = spongeSnapshot.getBlockPos();
        final Block block = BlockUtil.toBlock(spongeSnapshot);
        final IMixinChunk spongeChunk = (IMixinChunk) spongeSnapshot.getWorldServer().getChunk(pos);
        final PlayerTracker.Type trackerType = blockChange == BlockChange.PLACE ? PlayerTracker.Type.OWNER : PlayerTracker.Type.NOTIFIER;
        spongeChunk.addTrackedBlockPosition(block, pos, user, trackerType);
    }
}
