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
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockRedstoneLamp;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlag;
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
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
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
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.WorldUtil;

import java.util.ArrayList;
import java.util.Collection;
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
@SuppressWarnings("unchecked")
public final class TrackingUtil {

    public static final int BREAK_BLOCK_INDEX = 0;
    public static final int PLACE_BLOCK_INDEX = 1;
    public static final int DECAY_BLOCK_INDEX = 2;
    public static final int CHANGE_BLOCK_INDEX = 3;
    private static final int MULTI_CHANGE_INDEX = 4;
    private static final Function<ImmutableList.Builder<Transaction<BlockSnapshot>>[], Consumer<Transaction<BlockSnapshot>>> TRANSACTION_PROCESSOR =
            builders ->
                    transaction -> {
                        final BlockChange blockChange = ((SpongeBlockSnapshot) transaction.getOriginal()).blockChange;
                        builders[blockChange.ordinal()].add(transaction);
                        builders[MULTI_CHANGE_INDEX].add(transaction);
                    }
            ;
    private static final int EVENT_COUNT = 5;
    static final Function<BlockSnapshot, Transaction<BlockSnapshot>> TRANSACTION_CREATION = (blockSnapshot) -> {
        final Location originalLocation = blockSnapshot.getLocation().get();
        final WorldServer worldServer = (WorldServer) originalLocation.getWorld();
        final BlockPos blockPos = VecHelper.toBlockPos(originalLocation);
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
            entity.tick();
            if (ShouldFire.MOVE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity);
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
            if (ShouldFire.MOVE_ENTITY_EVENT) {
                SpongeCommonEventFactory.callMoveEntityEvent(entity);
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
                tile.tick();
            }
            // We delay clearing active chunk if TE is invalidated during tick so we must remove it after
            if (tileEntity.isRemoved()) {
                mixinTileEntity.setActiveChunk(null);
            }
        } catch (Exception e) {
            PhaseTracker.getInstance().printExceptionFromPhase(e, context);
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

        final LocatableBlock locatable = LocatableBlock.builder()
            .location(new Location(apiWorld, pos.getX(), pos.getY(), pos.getZ()))
            .state((BlockState) state)
            .build();
        final BlockTickContext phaseContext = TickPhase.Tick.BLOCK.createPhaseContext().source(locatable);

        final PhaseTracker phaseTracker = PhaseTracker.getInstance();

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseData current = phaseTracker.getCurrentPhaseData();
        final IPhaseState<?> currentState = current.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(mixinWorld, pos, current.context, phaseContext);
        // Now actually switch to the new phase

        try (final PhaseContext<?> context = phaseContext;
             final Timing timing = BlockUtil.toMixin(state).getTimingsHandler()) {
            timing.startTiming();
            context.buildAndSwitch();
            state.tick(world, pos, random);
        } catch (Exception | NoClassDefFoundError e) {
            phaseTracker.printExceptionFromPhase(e, phaseContext);

        }
    }

    @SuppressWarnings("rawtypes")
    public static void randomTickBlock(PhaseTracker phaseTracker, IMixinWorldServer mixinWorld, Block block,
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

        final LocatableBlock locatable = LocatableBlock.builder()
            .location(new Location(apiWorld, pos.getX(), pos.getY(), pos.getZ()))
            .state((BlockState) state)
            .build();
        final BlockTickContext phaseContext = TickPhase.Tick.RANDOM_BLOCK.createPhaseContext().source(locatable);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseData current = phaseTracker.getCurrentPhaseData();
        final IPhaseState<?> currentState = current.state;
        ((IPhaseState) currentState).appendNotifierPreBlockTick(mixinWorld, pos, current.context, phaseContext);
        // Now actually switch to the new phase
        try (PhaseContext<?> context = phaseContext) {
            context.buildAndSwitch();
            state.randomTick(world, pos, random);
        } catch (Exception | NoClassDefFoundError e) {
            phaseTracker.printExceptionFromPhase(e, phaseContext);
        }
    }


    public static void tickDimension(IMixinWorldServer worldServer) {
        final Dimension dimension = ((WorldServer) worldServer).dimension;
        try (DimensionContext context = TickPhase.Tick.DIMENSION.createPhaseContext().source(dimension)) {
            context.buildAndSwitch();
            dimension.tick();
        }
    }

    public static boolean fireMinecraftBlockEvent(WorldServer worldIn, BlockEventData event) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        final IMixinBlockEventData blockEvent = (IMixinBlockEventData) event;
        IPhaseState<?> phase = TickPhase.Tick.BLOCK_EVENT;
        final PhaseContext<?> phaseContext = phase.createPhaseContext();

        Object source = blockEvent.getTickBlock() != null ? blockEvent.getTickBlock() : blockEvent.getTickTileEntity();
        if (source != null) {
            phaseContext.source(source);
        } else {
            // No source present which means we are ignoring the phase state
            return currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
        }

        if (blockEvent.getSourceUser() != null) {
            phaseContext.notifier(blockEvent.getSourceUser());
        }

        try (PhaseContext<?> o = phaseContext) {
            o.buildAndSwitch();
            return currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
        }
    }

    @SuppressWarnings("rawtypes")
    static boolean captureBulkBlockChange(IMixinWorldServer mixinWorld, Chunk chunk, IBlockState currentState,
        IBlockState newState, BlockPos pos, BlockChangeFlag flags, PhaseContext<?> phaseContext, IPhaseState<?> phaseState) {
        final SpongeBlockSnapshot originalBlockSnapshot;
        final WorldServer world = WorldUtil.asNative(mixinWorld);
        if (((IPhaseState) phaseState).shouldCaptureBlockChangeOrSkip(phaseContext, pos)) {
            //final IBlockState actualState = currentState.getActualState(world, pos);
            originalBlockSnapshot = mixinWorld.createSpongeBlockSnapshot(currentState, currentState, pos, flags);
            final List<BlockSnapshot> capturedSnapshots = phaseContext.getCapturedBlocks();
            final Block newBlock = newState.getBlock();

            associateBlockChangeWithSnapshot(phaseState, newBlock, currentState, originalBlockSnapshot, capturedSnapshots);
            final IMixinChunk mixinChunk = (IMixinChunk) chunk;
            final IBlockState originalBlockState = mixinChunk.setBlockState(pos, newState, currentState, originalBlockSnapshot, BlockChangeFlags.ALL);
            if (originalBlockState == null) {
                capturedSnapshots.remove(originalBlockSnapshot);
                return false;
            }
            ((IPhaseState) phaseState).postTrackBlock(originalBlockSnapshot, phaseContext);
        } else {
            originalBlockSnapshot = (SpongeBlockSnapshot) BlockSnapshot.NONE;
            final IMixinChunk mixinChunk = (IMixinChunk) chunk;
            final IBlockState originalBlockState = mixinChunk.setBlockState(pos, newState, currentState, originalBlockSnapshot, BlockChangeFlags.ALL);
            if (originalBlockState == null) {
                return false;
            }
        }


        if (newState.getOpacity(world, pos) != currentState.getOpacity(world, pos) || newState.getLightValue() != currentState.getLightValue()) {
            world.profiler.startSection("checkLight");
            world.checkLight(pos);
            world.profiler.endSection();
        }

        return true;
    }

    static void associateBlockChangeWithSnapshot(IPhaseState<?> phaseState, Block newBlock, IBlockState currentState, SpongeBlockSnapshot snapshot,
        List<BlockSnapshot> capturedSnapshots) {
        Block originalBlock = currentState.getBlock();
        if (phaseState == BlockPhase.State.BLOCK_DECAY) {
            if (newBlock == Blocks.AIR) {
                snapshot.blockChange = BlockChange.DECAY;
                capturedSnapshots.add(snapshot);
            }
        } else if (newBlock == Blocks.AIR) {
            snapshot.blockChange = BlockChange.BREAK;
            capturedSnapshots.add(snapshot);
        } else if (newBlock != originalBlock && !forceModify(originalBlock, newBlock)) {
            snapshot.blockChange = BlockChange.PLACE;
            capturedSnapshots.add(snapshot);
        } else {
            snapshot.blockChange = BlockChange.MODIFY;
            capturedSnapshots.add(snapshot);
        }
    }

    private static boolean forceModify(Block originalBlock, Block newBlock) {
        if (originalBlock instanceof BlockRedstoneRepeater && newBlock instanceof BlockRedstoneRepeater) {
            return true;
        } else if (originalBlock instanceof BlockRedstoneTorch && newBlock instanceof BlockRedstoneTorch) {
            return true;
        } else
            return originalBlock instanceof BlockRedstoneLamp && newBlock instanceof BlockRedstoneLamp;
    }

    private TrackingUtil() {
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static User getNotifierOrOwnerFromBlock(Location location) {
        final BlockPos blockPos = VecHelper.toBlockPos(location);
        return getNotifierOrOwnerFromBlock((WorldServer) location.getWorld(), blockPos);
    }

    @Nullable
    private static User getNotifierOrOwnerFromBlock(WorldServer world, BlockPos blockPos) {
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

    public static boolean processBlockCaptures(List<BlockSnapshot> snapshots, IPhaseState<?> state, PhaseContext<?> context) {
        return processBlockCaptures(snapshots, state, context, 0);
    }

    /**
     * Processes the given list of {@link BlockSnapshot}s and creates and throws and processes
     * the {@link ChangeBlockEvent}s as appropriately determined based on the {@link BlockChange}
     * for each snapshot. If any transactions are invalid or events cancelled, this event
     * returns {@code false} to signify a transaction was cancelled. This return value
     * is used for portal creation.
     *
     * @param snapshots The snapshots to process
     * @param state The phase state that is being processed, used to handle marking notifiers
     *  and block owners
     * @param context The phase context, only used by the phase for handling processes.
     * @return True if no events or transactions were cancelled
     */
    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    public static boolean processBlockCaptures(List<BlockSnapshot> snapshots, IPhaseState<?> state, PhaseContext<?> context, int currentDepth) {
        if (snapshots.isEmpty()) {
            return false;
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();

        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[EVENT_COUNT];
        for (int i = 0; i < EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }

        createTransactionLists(snapshots, transactionArrays, transactionBuilders);

        // Clear captured snapshots after processing them
        context.getCapturedBlocksOrEmptyList().clear();

        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
        // This likely needs to delegate to the phase in the event we don't use the source object as the main object causing the block changes
        // case in point for WorldTick event listeners since the players are captured non-deterministically
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            try {
                state.associateAdditionalCauses(context, frame);
            } catch (Exception e) {
                // TODO - this should be a thing to associate additional objects in the cause, or context, but for now it's just a simple
                // try catch to avoid bombing on performing block changes.
            }
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
            boolean noCancelledTransactions = checkCancelledEvents(blockEvents, postEvent);

            // Now we can gather the invalid transactions that either were marked as invalid from an event listener - OR - cancelled.
            // Because after, we will restore all the invalid transactions in reverse order.
            clearInvalidTransactionDrops(context, postEvent, invalid);

            if (!invalid.isEmpty()) {
                // We need to set this value and return it to signify that some transactions were cancelled
                noCancelledTransactions = false;
                rollBackTransactions(state, context, invalid);
                invalid.clear(); // Clear because we might re-enter for some reasons yet to be determined.

            }
            return performBlockAdditions(postEvent.getTransactions(), state, context, noCancelledTransactions, currentDepth);
        }
    }

    private static void createTransactionLists(List<BlockSnapshot> snapshots, ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays,
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders) {
        for (BlockSnapshot snapshot : snapshots) {
            // This processes each snapshot to assign them to the correct event in the next area, with the
            // correct builder array entry.
            TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(TRANSACTION_CREATION.apply(snapshot));
        }
        for (int i = 0; i < EVENT_COUNT; i++) {
            // Build each event array
            transactionArrays[i] = transactionBuilders[i].build();
        }
    }

    private static boolean checkCancelledEvents(List<ChangeBlockEvent> blockEvents, ChangeBlockEvent.Post postEvent) {
        boolean noCancelledTransactions = true;
        for (ChangeBlockEvent blockEvent : blockEvents) { // Need to only check if the event is cancelled, If it is, restore
            if (blockEvent.isCancelled()) {
                noCancelledTransactions = false;
                // Don't restore the transactions just yet, since we're just marking them as invalid for now
                for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    transaction.setValid(false);
                }
            }
        }
        if (postEvent.isCancelled()) {
            // Of course, if post is cancelled, just mark all transactions as invalid.
            noCancelledTransactions = false;
            for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                transaction.setValid(false);
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
                final Location location = transaction.getOriginal().getLocation().orElse(null);
                if (location != null) {
                    final BlockPos pos = VecHelper.toBlockPos(location);
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
            if (((IPhaseState) state).tracksBlockSpecificDrops(context)) {
                // Cancel any block drops or harvests for the block change.
                // This prevents unnecessary spawns.
                final Location location = transaction.getOriginal().getLocation().orElse(null);
                if (location != null) {
                    final BlockPos pos = VecHelper.toBlockPos(location);
                    context.getBlockDropSupplier().removeAllIfNotEmpty(pos);
                }
            }
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
                                                PhaseContext<?> phaseContext, boolean noCancelledTransactions, int currentDepth) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        for (Transaction<BlockSnapshot> transaction : transactions) {
            noCancelledTransactions = performTransactionProcess(transaction, phaseState, phaseContext, noCancelledTransactions, currentDepth);
        }
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
     * @param noCancelledTransactions Whether there's any cancelled transactions
     * @param currentDepth The current processing depth, to avoid stack overflows
     * @return True if the block transaction was successful
     */
    @SuppressWarnings("rawtypes")
    static boolean performTransactionProcess(Transaction<BlockSnapshot> transaction, IPhaseState<?> phaseState, PhaseContext<?> phaseContext,
        boolean noCancelledTransactions, int currentDepth) {
        // Handle custom replacements - these need to get actually set onto the chunk, but ignored as far as tracking
        // goes.
        if (transaction.getCustom().isPresent()) {
            transaction.getFinal().restore(true, BlockChangeFlags.NONE);
        }

        final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
        final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();

        // Handle item drops captured
        final Location worldLocation = oldBlockSnapshot.getLocation().orElseThrow(() -> {
            final IllegalStateException exception = new IllegalStateException("BlockSnapshot with Invalid Location");
            PhaseTracker.getInstance().printMessageWithCaughtException("BlockSnapshot does not have a valid location object, usually because the world is unloaded!", "", exception);
            return exception;
        });
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) worldLocation.getWorld();
        final BlockPos pos = VecHelper.toBlockPos(worldLocation);
        performBlockEntitySpawns(phaseState, phaseContext, oldBlockSnapshot, pos);

        final WorldServer world = WorldUtil.asNative(mixinWorld);
        SpongeHooks.logBlockAction(world, oldBlockSnapshot.blockChange, transaction);
        final SpongeBlockChangeFlag changeFlag = oldBlockSnapshot.getChangeFlag();
        final IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
        final IBlockState newState = (IBlockState) newBlockSnapshot.getState();

        // We call onBlockAdded here for blocks without a TileEntity.
        // MixinChunk#setBlockState will call onBlockAdded for blocks
        // with a TileEntity or when capturing is not being done.
        final Block newBlock = newState.getBlock();
        if (originalState.getBlock() != newBlock && changeFlag.performBlockPhysics() && !SpongeImplHooks.hasBlockTileEntity(newBlock, newState)) {
            newBlock.onBlockAdded(world, pos, newState);
            ((IPhaseState) phaseState).performOnBlockAddedSpawns(phaseContext);
        }

        ((IPhaseState) phaseState).postBlockTransactionApplication(oldBlockSnapshot.blockChange, transaction, phaseContext);

        if (changeFlag.isNotifyClients()) { // Always try to notify clients of the change.
            world.notifyBlockUpdate(pos, originalState, newState, changeFlag.getRawFlag());
        }

        if (changeFlag.updateNeighbors()) { // Notify neighbors only if the change flag allowed it.
            mixinWorld.spongeNotifyNeighborsPostBlockChange(pos, originalState, newState, changeFlag);
        } else if (changeFlag.notifyObservers()) {
            world.updateObservingBlocksAt(pos, newBlock);
        }

        ((IPhaseState) phaseState).performPostBlockNotificationsAndNeighborUpdates(phaseContext, currentDepth + 1);
        return noCancelledTransactions;
    }

    private static void performBlockEntitySpawns(IPhaseState<?> state, PhaseContext<?> phaseContext, SpongeBlockSnapshot oldBlockSnapshot, BlockPos pos) {
        // This is for pre-merged items
        if (state.doesCaptureEntitySpawns() || ((IPhaseState) state).doesCaptureEntityDrops(phaseContext)) {
            phaseContext.getBlockDropSupplier().acceptAndRemoveIfPresent(pos, items -> spawnItemDataForBlockDrops(items, oldBlockSnapshot,
                phaseContext));
            // And this is for un-pre-merged items, these will be EntityItems, not ItemDropDatas.
            phaseContext.getBlockItemDropSupplier().acceptAndRemoveIfPresent(pos, items -> spawnItemEntitiesForBlockDrops(items, oldBlockSnapshot,
                phaseContext));
            // This is for entities actually spawned
            phaseContext.getPerBlockEntitySpawnSuppplier().acceptAndRemoveIfPresent(pos, items -> spawnEntitiesForBlock(items,
                phaseContext));
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
        Location worldLocation = oldBlockSnapshot.getLocation().get();
        final World world = worldLocation.getWorld();
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
        List<ChangeBlockEvent> blockEvents, ChangeBlockEvent[] mainEvents) {
        if (!blockEvents.isEmpty()) {
            final ImmutableList<Transaction<BlockSnapshot>> transactions = transactionArrays[MULTI_CHANGE_INDEX];
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                for (BlockChange blockChange : BlockChange.values()) {
                    final ChangeBlockEvent mainEvent = mainEvents[blockChange.ordinal()];
                    if (mainEvent != null) {
                        frame.pushCause(mainEvent);
                    }
                }
                final ChangeBlockEvent.Post post = ((IPhaseState) state).createChangeBlockPostEvent(context, transactions);
                SpongeImpl.postEvent(post);
                return post;
            }
        }
        return null;
    }

}
