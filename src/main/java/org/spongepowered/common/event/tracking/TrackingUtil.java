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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.plugin.blockcapturing.IModData_BlockCapturing;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A simple utility for aiding in tracking, either with resolving notifiers
 * and owners, or proxying out the logic for ticking a block, entity, etc.
 */
public final class TrackingUtil {

    public static final int BREAK_BLOCK_INDEX = 0;
    public static final int PLACE_BLOCK_INDEX = 1;
    public static final int DECAY_BLOCK_INDEX = 2;
    public static final int CHANGE_BLOCK_INDEX = 3;
    public static final int MULTI_CHANGE_INDEX = 4;
    public static final Function<ImmutableList.Builder<Transaction<BlockSnapshot>>[], Consumer<Transaction<BlockSnapshot>>> TRANSACTION_PROCESSOR =
            builders ->
                    transaction -> {
                        final BlockChange blockChange = ((SpongeBlockSnapshot) transaction.getOriginal()).blockChange;
                        builders[blockChange.ordinal()].add(transaction);
                        builders[MULTI_CHANGE_INDEX].add(transaction);
                    }
            ;
    public static final int EVENT_COUNT = 5;
    public static final Function<BlockSnapshot, Transaction<BlockSnapshot>> TRANSACTION_CREATION = (blockSnapshot) -> {
        final Location<World> originalLocation = blockSnapshot.getLocation().get();
        final WorldServer worldServer = (WorldServer) originalLocation.getExtent();
        final BlockPos blockPos = ((IMixinLocation) (Object) originalLocation).getBlockPos();
        final IBlockState newState = worldServer.getBlockState(blockPos);
        final IBlockState newActualState = newState.getActualState(worldServer, blockPos);
        final BlockSnapshot newSnapshot = ((IMixinWorldServer) worldServer).createSpongeBlockSnapshot(newState, newActualState, blockPos, 0);
        return new Transaction<>(blockSnapshot, newSnapshot);
    };

    public static void tickEntity(net.minecraft.entity.Entity entityIn) {
        checkArgument(entityIn instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entityIn);
        checkNotNull(entityIn, "Cannot capture on a null ticking entity!");
        final IMixinChunk chunk = ((IMixinEntity) entityIn).getActiveChunk();
        if (chunk == null || (chunk.isQueuedForUnload() && !chunk.isPersistedChunk())) {
            // Don't tick entities in chunks queued for unload
            return;
        }

        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(entityIn))
                .addEntityCaptures()
                .addBlockCaptures();
        final IMixinEntity mixinEntity = EntityUtil.toMixin(entityIn);
        mixinEntity.getNotifierUser()
                .ifPresent(phaseContext::notifier);
        mixinEntity.getCreatorUser()
                .ifPresent(phaseContext::owner);

        CauseTracker.getInstance().switchToPhase(TickPhase.Tick.ENTITY, phaseContext
                .complete());
        final Timing entityTiming = mixinEntity.getTimingsHandler();
        entityTiming.startTiming();
        try {
            entityIn.onUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            entityTiming.stopTiming();
            CauseTracker.getInstance().completePhase(TickPhase.Tick.ENTITY);
        }
    }

    public static void tickRidingEntity(net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        final IMixinChunk chunk = ((IMixinEntity) entity).getActiveChunk();
        if (chunk == null || (chunk.isQueuedForUnload() && !chunk.isPersistedChunk())) {
            // Don't tick entity in chunks queued for unload
            return;
        }
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(entity))
                .addEntityCaptures()
                .addBlockCaptures();
        final IMixinEntity mixinEntity = EntityUtil.toMixin(entity);
        mixinEntity.getNotifierUser()
                .ifPresent(phaseContext::notifier);
        mixinEntity.getCreatorUser()
                .ifPresent(phaseContext::owner);
        CauseTracker.getInstance().switchToPhase(TickPhase.Tick.ENTITY, phaseContext
                .complete());
        final Timing entityTiming = mixinEntity.getTimingsHandler();
        entityTiming.startTiming();
        entity.updateRidden();
        entityTiming.stopTiming();
        CauseTracker.getInstance().completePhase(TickPhase.Tick.ENTITY);
    }

    public static void tickTileEntity(IMixinWorldServer mixinWorldServer, ITickable tile) {
        checkArgument(tile instanceof TileEntity, "ITickable %s is not a TileEntity!", tile);
        checkNotNull(tile, "Cannot capture on a null ticking tile entity!");
        final net.minecraft.tileentity.TileEntity tileEntity = (net.minecraft.tileentity.TileEntity) tile;
        final BlockPos pos = tileEntity.getPos();
        final IMixinChunk chunk = ((IMixinTileEntity) tile).getActiveChunk();
        if (chunk == null || (chunk.isQueuedForUnload() && !chunk.isPersistedChunk())) {
            // Don't tick TE's in chunks queued for unload
            return;
        }
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(tile))
                .addEntityCaptures()
                .addBlockCaptures();

        final IMixinChunk mixinChunk = (IMixinChunk) chunk;
        // Add notifier and owner so we don't have to perform lookups during the phases and other processing
        mixinChunk.getBlockNotifier(pos)
                .ifPresent(phaseContext::notifier);

        final IMixinTileEntity mixinTileEntity = (IMixinTileEntity) tile;
        User blockOwner = mixinTileEntity.getSpongeOwner();
        if (!mixinTileEntity.hasSetOwner()) {
            blockOwner = mixinChunk.getBlockOwner(pos).orElse(null);
            mixinTileEntity.setSpongeOwner(blockOwner);
        }

        phaseContext.owner = blockOwner;
        // Add the block snapshot of the tile entity for caches to avoid creating multiple snapshots during processing
        // This is a lazy evaluating snapshot to avoid the overhead of snapshot creation
        final CauseTracker causeTracker = CauseTracker.getInstance();
        causeTracker.switchToPhase(TickPhase.Tick.TILE_ENTITY, phaseContext
                .complete());

        mixinTileEntity.getTimingsHandler().startTiming();
        try {
            tile.update();
        } finally {
            mixinTileEntity.getTimingsHandler().stopTiming();
            causeTracker.completePhase(TickPhase.Tick.TILE_ENTITY);
        }
    }

    public static void updateTickBlock(IMixinWorldServer mixinWorld, Block block, BlockPos pos, IBlockState state, Random random) {
        final WorldServer minecraftWorld = mixinWorld.asMinecraftWorld();
        if (ShouldFire.TICK_BLOCK_EVENT) {
            BlockSnapshot snapshot = mixinWorld.createSpongeBlockSnapshot(state, state, pos, 0);
            final TickBlockEvent event = SpongeEventFactory.createTickBlockEventScheduled(Cause.of(NamedCause.source(minecraftWorld)), snapshot);
            SpongeImpl.postEvent(event);
            if(event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = LocatableBlock.builder()
                .location(new Location<>(mixinWorld.asSpongeWorld(), pos.getX(), pos.getY(), pos.getZ()))
                .state((BlockState) state)
                .build();
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(locatable))
                .addBlockCaptures()
                .addEntityCaptures();

        checkAndAssignBlockTickConfig(block, minecraftWorld, phaseContext);
        final CauseTracker causeTracker = CauseTracker.getInstance();

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseData current = causeTracker.getCurrentPhaseData();
        final IPhaseState currentState = current.state;
        currentState.getPhase().appendNotifierPreBlockTick(mixinWorld, pos, currentState, current.context, phaseContext);
        // Now actually switch to the new phase
        IPhaseState phase = ((IMixinBlock) block).requiresBlockCapture() ? TickPhase.Tick.BLOCK : TickPhase.Tick.NO_CAPTURE_BLOCK;

        causeTracker.switchToPhase(phase, phaseContext.complete());
        block.updateTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase(phase);
    }

    public static void randomTickBlock(CauseTracker causeTracker, IMixinWorldServer mixinWorld, Block block,
        BlockPos pos, IBlockState state, Random random) {
        final WorldServer minecraftWorld = mixinWorld.asMinecraftWorld();
        if (ShouldFire.TICK_BLOCK_EVENT) {
            final BlockSnapshot currentTickBlock = mixinWorld.createSpongeBlockSnapshot(state, state, pos, 0);
            final TickBlockEvent event = SpongeEventFactory.createTickBlockEventRandom(Cause.of(NamedCause.source(minecraftWorld)), currentTickBlock);
            SpongeImpl.postEvent(event);
            if(event.isCancelled()) {
                return;
            }
        }

        final LocatableBlock locatable = LocatableBlock.builder()
                .location(new Location<>(mixinWorld.asSpongeWorld(), pos.getX(), pos.getY(), pos.getZ()))
                .state((BlockState) state)
                .build();
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(locatable))
                .addEntityCaptures()
                .addBlockCaptures();

        checkAndAssignBlockTickConfig(block, minecraftWorld, phaseContext);

        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseData current = causeTracker.getCurrentPhaseData();
        final IPhaseState currentState = current.state;
        currentState.getPhase().appendNotifierPreBlockTick(mixinWorld, pos, currentState, current.context, phaseContext);
        // Now actually switch to the new phase
        IPhaseState phase = ((IMixinBlock) block).requiresBlockCapture() ? TickPhase.Tick.RANDOM_BLOCK : TickPhase.Tick.NO_CAPTURE_BLOCK;
        causeTracker.switchToPhase(phase, phaseContext.complete());
        block.randomTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase(phase);
    }

    private static void checkAndAssignBlockTickConfig(Block block, WorldServer minecraftWorld, PhaseContext phaseContext) {
        if (block instanceof IModData_BlockCapturing) {
            IModData_BlockCapturing capturingBlock = (IModData_BlockCapturing) block;
            if (capturingBlock.requiresBlockCapturingRefresh()) {
                capturingBlock.initializeBlockCapturingState(minecraftWorld);
                capturingBlock.requiresBlockCapturingRefresh(false);
            }
            phaseContext.add(NamedCause.of(InternalNamedCauses.Tracker.PROCESS_IMMEDIATELY, ((IModData_BlockCapturing) block).processTickChangesImmediately()));
        } else {
            phaseContext.add(NamedCause.of(InternalNamedCauses.Tracker.PROCESS_IMMEDIATELY, false));
        }
    }

    public static void tickWorldProvider(IMixinWorldServer worldServer) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final WorldProvider worldProvider = ((WorldServer) worldServer).provider;
        causeTracker.switchToPhase(TickPhase.Tick.DIMENSION, PhaseContext.start()
                .add(NamedCause.source(worldProvider))
                .addBlockCaptures()
                .addEntityCaptures()
                .addEntityDropCaptures()
                .complete());
        worldProvider.onWorldUpdateEntities();
        causeTracker.completePhase(TickPhase.Tick.DIMENSION);
    }

    public static boolean fireMinecraftBlockEvent(CauseTracker causeTracker, WorldServer worldIn, BlockEventData event) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        final IMixinBlockEventData blockEvent = (IMixinBlockEventData) event;
        final PhaseContext phaseContext = PhaseContext.start()
                .addBlockCaptures()
                .addEntityCaptures();

        Object source = blockEvent.getTickBlock() != null ? blockEvent.getTickBlock() : blockEvent.getTickTileEntity();
        if (source != null) {
            phaseContext.add(NamedCause.source(source));
        } else {
            // No source present which means we are ignoring the phase state
            boolean result = currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
            return result;
        }

        if (blockEvent.getSourceUser() != null) {
            phaseContext.add(NamedCause.notifier(blockEvent.getSourceUser()));
        }

        IPhaseState phase = blockEvent.getCaptureBlocks() ? TickPhase.Tick.BLOCK_EVENT : TickPhase.Tick.NO_CAPTURE_BLOCK;
        causeTracker.switchToPhase(phase, phaseContext.complete());
        boolean result = currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
        causeTracker.completePhase(phase);
        return result;
    }

    public static void performBlockDrop(Block block, IMixinWorldServer mixinWorld, BlockPos pos, IBlockState state, float chance, int fortune) {
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final IPhaseState currentState = causeTracker.getCurrentState();
        final boolean shouldEnterBlockDropPhase = !currentState.getPhase().alreadyCapturingItemSpawns(currentState) && !currentState.getPhase().isWorldGeneration(currentState);
        if (shouldEnterBlockDropPhase) {
            PhaseContext context = PhaseContext.start()
                    .add(NamedCause.source(mixinWorld.createSpongeBlockSnapshot(state, state, pos, 4)))
                    .addBlockCaptures()
                    .addEntityCaptures();
                    // unused, to be removed and re-located when phase context is cleaned up
                    //.add(NamedCause.of(InternalNamedCauses.General.BLOCK_BREAK_FORTUNE, fortune))
                    //.add(NamedCause.of(InternalNamedCauses.General.BLOCK_BREAK_POSITION, pos));
            // use current notifier and owner if available
            context.notifier = causeTracker.getCurrentContext().notifier;
            context.owner = causeTracker.getCurrentContext().owner;
            context.complete();
            causeTracker.switchToPhase(BlockPhase.State.BLOCK_DROP_ITEMS, context);
        }
        block.dropBlockAsItemWithChance((WorldServer) mixinWorld, pos, state, chance, fortune);
        if (shouldEnterBlockDropPhase) {
            causeTracker.completePhase(BlockPhase.State.BLOCK_DROP_ITEMS);
        }
    }

    static boolean trackBlockChange(CauseTracker causeTracker, IMixinWorldServer mixinWorld, Chunk chunk, IBlockState currentState, IBlockState newState, BlockPos pos, int flags,
            PhaseContext phaseContext, IPhaseState phaseState) {
        final SpongeBlockSnapshot originalBlockSnapshot;
        final WorldServer minecraftWorld = mixinWorld.asMinecraftWorld();
        if (phaseState.shouldCaptureBlockChangeOrSkip(phaseContext, pos)) {
            //final IBlockState actualState = currentState.getActualState(minecraftWorld, pos);
            originalBlockSnapshot = mixinWorld.createSpongeBlockSnapshot(currentState, currentState, pos, flags);
            final List<BlockSnapshot> capturedSnapshots = phaseContext.getCapturedBlocks();
            final Block newBlock = newState.getBlock();

            associateBlockChangeWithSnapshot(phaseState, newBlock, currentState, originalBlockSnapshot, capturedSnapshots);
            final IMixinChunk mixinChunk = (IMixinChunk) chunk;
            final IBlockState originalBlockState = mixinChunk.setBlockState(pos, newState, currentState, originalBlockSnapshot);
            if (originalBlockState == null) {
                capturedSnapshots.remove(originalBlockSnapshot);
                return false;
            }
            phaseState.postTrackBlock(originalBlockSnapshot, causeTracker, phaseContext);
        } else {
            originalBlockSnapshot = (SpongeBlockSnapshot) BlockSnapshot.NONE;
            final IMixinChunk mixinChunk = (IMixinChunk) chunk;
            final IBlockState originalBlockState = mixinChunk.setBlockState(pos, newState, currentState, originalBlockSnapshot);
            if (originalBlockState == null) {
                return false;
            }
        }


        if (newState.getLightOpacity() != currentState.getLightOpacity() || newState.getLightValue() != currentState.getLightValue()) {
            minecraftWorld.profiler.startSection("checkLight");
            minecraftWorld.checkLight(pos);
            minecraftWorld.profiler.endSection();
        }

        return true;
    }

    private static void associateBlockChangeWithSnapshot(IPhaseState phaseState, Block newBlock, IBlockState currentState, SpongeBlockSnapshot snapshot, List<BlockSnapshot> capturedSnapshots) {
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
        }
        if (originalBlock instanceof BlockRedstoneTorch && newBlock instanceof BlockRedstoneTorch) {
            return true;
        }
        if (originalBlock instanceof BlockRedstoneLight && newBlock instanceof BlockRedstoneLight) {
            return true;
        }

        return false;
    }

    private TrackingUtil() {
    }

    public static User getNotifierOrOwnerFromBlock(Location<World> location) {
        final BlockPos blockPos = ((IMixinLocation) (Object) location).getBlockPos();
        return getNotifierOrOwnerFromBlock((WorldServer) location.getExtent(), blockPos);
    }

    public static User getNotifierOrOwnerFromBlock(WorldServer world, BlockPos blockPos) {
        final IMixinChunk mixinChunk = (IMixinChunk) world.getChunkFromBlockCoords(blockPos);
        User notifier = mixinChunk.getBlockNotifier(blockPos).orElse(null);
        if (notifier != null) {
            return notifier;
        }

        User owner = mixinChunk.getBlockOwner(blockPos).orElse(null);
        return owner;
    }

    public static Supplier<IllegalStateException> throwWithContext(String s, PhaseContext phaseContext) {
        return () -> {
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Exception trying to process over a phase!").centre().hr();
            printer.addWrapped(40, "%s :", "PhaseContext");
            CauseTracker.CONTEXT_PRINTER.accept(printer, phaseContext);
            printer.add("Stacktrace:");
            final IllegalStateException exception = new IllegalStateException(s + " Please analyze the current phase context. ");
            printer.add(exception);
            printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
            return exception;
        };
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean processBlockCaptures(List<BlockSnapshot> snapshots, IPhaseState state, PhaseContext context) {
        if (snapshots.isEmpty()) {
            return false;
        }
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[EVENT_COUNT];
        for (int i = 0; i < EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();

        for (BlockSnapshot snapshot : snapshots) {
            // This processes each snapshot to assign them to the correct event in the next area, with the
            // correct builder array entry.
            TRANSACTION_PROCESSOR.apply(transactionBuilders).accept(TRANSACTION_CREATION.apply(snapshot));
        }

        for (int i = 0; i < EVENT_COUNT; i++) {
            // Build each event array
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
        // This likely needs to delegate to the phase in the event we don't use the source object as the main object causing the block changes
        // case in point for WorldTick event listeners since the players are captured non-deterministically
        final Cause.Builder builder = Cause.source(context.getSource(Object.class)
                .orElseThrow(throwWithContext("There was no root source object for this phase!", context))
        );
        context.getNotifier().ifPresent(builder::notifier);
        context.getOwner().ifPresent(builder::owner);
        try {
            state.getPhase().associateAdditionalCauses(state, context, builder);
        } catch (Exception e) {
            // TODO - this should be a thing to associate additional objects in the cause, or context, but for now it's just a simple
            // try catch to avoid bombing on performing block changes.
        }
        // Creates the block events accordingly to the transaction arrays
        iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents, builder); // Needs to throw events
        // We create the post event and of course post it in the method, regardless whether any transactions are invalidated or not
        final ChangeBlockEvent.Post postEvent = throwMultiEventsAndCreatePost(transactionArrays, blockEvents, mainEvents, builder);

        if (postEvent == null) { // Means that we have had no actual block changes apparently?
            return false;
        }

        final List<Transaction<BlockSnapshot>> invalid = new ArrayList<>();

        boolean noCancelledTransactions = true;

        // Iterate through the block events to mark any transactions as invalid to accumilate after (since the post event contains all
        // transactions of the preceeding block events)
        for (ChangeBlockEvent blockEvent : blockEvents) { // Need to only check if the event is cancelled, If it is, restore
            if (blockEvent.isCancelled()) {
                noCancelledTransactions = false;
                // Don't restore the transactions just yet, since we're just marking them as invalid for now
                for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    transaction.setValid(false);
                }
            }
        }

        // Finally check the post event
        if (postEvent.isCancelled()) {
            // Of course, if post is cancelled, just mark all transactions as invalid.
            noCancelledTransactions = false;
            for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                transaction.setValid(false);
            }
        }

        // Now we can gather the invalid transactions that either were marked as invalid from an event listener - OR - cancelled.
        // Because after, we will restore all the invalid transactions in reverse order.
        for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                invalid.add(transaction);
                // Cancel any block drops performed, avoids any item drops, regardless
                final BlockPos blockPos = ((IMixinLocation) (Object) transaction.getOriginal().getLocation().get()).getBlockPos();

                context.getBlockItemDropSupplier().ifPresentAndNotEmpty(map -> {
                    if (map.containsKey(blockPos)) {
                        map.get(blockPos).clear();
                    }
                });
                context.getBlockEntitySpawnSupplier().ifPresentAndNotEmpty(map -> {
                    if (map.containsKey(blockPos)) {
                        map.get(blockPos).clear();
                    }
                });
                context.getBlockEntitySpawnSupplier().ifPresentAndNotEmpty(blockPosEntityMultimap -> {
                    if (blockPosEntityMultimap.containsKey(blockPos)) {
                        blockPosEntityMultimap.get(blockPos).clear();
                    }
                });
            }
        }

        if (!invalid.isEmpty()) {
            // We need to set this value and return it to signify that some transactions were cancelled
            noCancelledTransactions = false;
            // NOW we restore the invalid transactions (remember invalid transactions are from either plugins marking them as invalid
            // or the events were cancelled), again in reverse order of which they were received.
            for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                transaction.getOriginal().restore(true, BlockChangeFlag.NONE);
                if (state.tracksBlockSpecificDrops()) {
                    // Cancel any block drops or harvests for the block change.
                    // This prevents unnecessary spawns.
                    final BlockPos position = ((IMixinLocation) (Object) transaction.getOriginal().getLocation().get()).getBlockPos();
                    context.getBlockDropSupplier().ifPresentAndNotEmpty(map -> {
                        // Check if the mapping actually has the position to avoid unnecessary
                        // collection creation
                        if (map.containsKey(position)) {
                            map.get(position).clear();
                        }
                    });
                }
            }
        }
        return performBlockAdditions(postEvent.getTransactions(), builder, state, context, noCancelledTransactions);
    }

    public static void iterateChangeBlockEvents(ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays, List<ChangeBlockEvent> blockEvents,
        ChangeBlockEvent[] mainEvents, Cause.Builder builder) {
        for (BlockChange blockChange : BlockChange.values()) {
            if (blockChange == BlockChange.DECAY) { // Decay takes place after.
                continue;
            }
            if (!transactionArrays[blockChange.ordinal()].isEmpty()) {
                final ChangeBlockEvent event = blockChange.createEvent(builder.build(), transactionArrays[blockChange.ordinal()]);
                mainEvents[blockChange.ordinal()] = event;
                if (event != null) {
                    SpongeImpl.postEvent(event);
                    blockEvents.add(event);
                }
            }
        }
        if (!transactionArrays[BlockChange.DECAY.ordinal()].isEmpty()) { // Needs to be placed into iterateChangeBlockEvents
            final ChangeBlockEvent event = BlockChange.DECAY.createEvent(builder.build(), transactionArrays[BlockChange.DECAY.ordinal()]);
            mainEvents[BlockChange.DECAY.ordinal()] = event;
            if (event != null) {
                SpongeImpl.postEvent(event);
                blockEvents.add(event);
            }
        }
    }

    public static boolean performBlockAdditions(List<Transaction<BlockSnapshot>> transactions, Cause.Builder builder, IPhaseState phaseState,
        PhaseContext phaseContext, boolean noCancelledTransactions) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        final SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(transactions);
        final CapturedMultiMapSupplier<BlockPos, ItemDropData> capturedBlockDrops = phaseContext.getBlockDropSupplier();
        final CapturedMultiMapSupplier<BlockPos, EntityItem> capturedBlockItemEntityDrops = phaseContext.getBlockItemDropSupplier();
        final CapturedMultiMapSupplier<BlockPos, net.minecraft.entity.Entity> capturedBlockEntitySpawns = phaseContext.getBlockEntitySpawnSupplier();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                // Rememver that this value needs to be set to false to return because of the fact that
                // a transaction was marked as invalid or cancelled. This is used primarily for
                // things like portal creation, and if false, removes the portal from the cache
                noCancelledTransactions = false;
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                transaction.getFinal().restore(true, BlockChangeFlag.NONE);
            }

            final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();

            final Location<World> worldLocation = oldBlockSnapshot.getLocation().get();
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldLocation.getExtent();
            // Handle item drops captured
            final BlockPos pos = ((IMixinLocation) (Object) oldBlockSnapshot.getLocation().get()).getBlockPos();
            // This is for pre-merged items
            capturedBlockDrops.ifPresentAndNotEmpty(map -> spawnItemDataForBlockDrops(map.containsKey(pos) ? map.removeAll(pos) : Collections.emptyList(), newBlockSnapshot,
                phaseContext, phaseState));
            // And this is for un-pre-merged items, these will be EntityItems, not ItemDropDatas.
            capturedBlockItemEntityDrops.ifPresentAndNotEmpty(map -> spawnItemEntitiesForBlockDrops(map.containsKey(pos) ? map.removeAll(pos) : Collections.emptyList(), newBlockSnapshot,
                phaseContext, phaseState));
            // This is for entities actually spawned
            capturedBlockEntitySpawns.ifPresentAndNotEmpty(map -> spawnEntitiesForBlock(map.containsKey(pos) ? map.removeAll(pos) : Collections.emptyList(), newBlockSnapshot,
                phaseContext, phaseState));

            SpongeHooks.logBlockAction(builder, mixinWorldServer.asMinecraftWorld(), oldBlockSnapshot.blockChange, transaction);
            final BlockChangeFlag changeFlag = oldBlockSnapshot.getChangeFlag();
            final IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
            final IBlockState newState = (IBlockState) newBlockSnapshot.getState();
            // We call onBlockAdded here for both TE blocks (BlockContainer's) and other blocks.
            // MixinChunk#setBlockState will only call onBlockAdded for BlockContainers when it's passed a null newBlockSnapshot,
            // which only happens when capturing is not being done.
            final CauseTracker causeTracker = CauseTracker.getInstance();
            if (changeFlag.performBlockPhysics() && originalState.getBlock() != newState.getBlock()) {
                newState.getBlock().onBlockAdded(mixinWorldServer.asMinecraftWorld(), pos, newState);
                final PhaseData peek = causeTracker.getCurrentPhaseData();
                if (peek.state == GeneralPhase.Post.UNWINDING) {
                    peek.state.getPhase().unwind(peek.state, peek.context);
                }
            }

            proxyBlockAccess.proceed();
            phaseState.handleBlockChangeWithUser(oldBlockSnapshot.blockChange, transaction, phaseContext);

            final int minecraftChangeFlag = oldBlockSnapshot.getUpdateFlag();
            if (((minecraftChangeFlag & 2) != 0)) { // Always try to notify clients of the change.
                mixinWorldServer.asMinecraftWorld().notifyBlockUpdate(pos, originalState, newState, minecraftChangeFlag);
            }

            if (changeFlag.updateNeighbors()) { // Notify neighbors only if the change flag allowed it.
                mixinWorldServer.spongeNotifyNeighborsPostBlockChange(pos, originalState, newState, oldBlockSnapshot.getUpdateFlag());
            } else if ((minecraftChangeFlag & 16) == 0) {
                mixinWorldServer.asMinecraftWorld().updateObservingBlocksAt(pos, newState.getBlock());
            }

            final PhaseData peek = causeTracker.getCurrentPhaseData();
            if (peek.state == GeneralPhase.Post.UNWINDING) {
                peek.state.getPhase().unwind(peek.state, peek.context);
            }
        }
        return noCancelledTransactions;
    }

    public static void spawnItemEntitiesForBlockDrops(Collection<EntityItem> entityItems, SpongeBlockSnapshot newBlockSnapshot,
        PhaseContext phaseContext, IPhaseState phaseState) {
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = entityItems.stream()
                .map(EntityUtil::fromNative)
                .collect(Collectors.toList());
        final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                .block(newBlockSnapshot)
                .type(InternalSpawnTypes.DROPPED_ITEM)
                .build());
        final Optional<User> owner = phaseContext.getOwner();
        final Optional<User> notifier = phaseContext.getNotifier();
        notifier.ifPresent(builder::notifier);
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        final Cause spawnCauses = builder.build();
        final DropItemEvent.Destruct destruct = SpongeEventFactory.createDropItemEventDestruct(spawnCauses, itemDrops);
        SpongeImpl.postEvent(destruct);
        if (!destruct.isCancelled()) {
            for (Entity entity : destruct.getEntities()) {
                if (entityCreator != null) {
                    EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                }
                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
            }
        }
    }

    public static void spawnItemDataForBlockDrops(Collection<ItemDropData> itemStacks, SpongeBlockSnapshot oldBlockSnapshot,
        PhaseContext phaseContext, IPhaseState state) {
        final Vector3i position = oldBlockSnapshot.getPosition();
        final List<ItemStackSnapshot> itemSnapshots = itemStacks.stream()
                .map(ItemDropData::getStack)
                .map(ItemStackUtil::snapshotOf)
                .collect(Collectors.toList());
        final ImmutableList<ItemStackSnapshot> originalSnapshots = ImmutableList.copyOf(itemSnapshots);
        final Cause cause = Cause.source(oldBlockSnapshot).build();
        final DropItemEvent.Pre dropItemEventPre = SpongeEventFactory.createDropItemEventPre(cause, originalSnapshots, itemSnapshots);
        SpongeImpl.postEvent(dropItemEventPre);
        if (dropItemEventPre.isCancelled()) {
            itemStacks.clear();
        }
        if (itemStacks.isEmpty()) {
            return;
        }
        final World world = oldBlockSnapshot.getLocation().get().getExtent();
        final WorldServer worldServer = (WorldServer) world;
        // Now we can spawn the entity items appropriately
        final List<Entity> itemDrops = itemStacks.stream().map(itemStack -> {
                    final net.minecraft.item.ItemStack minecraftStack = itemStack.getStack();
                    float f = 0.5F;
                    double offsetX = (double) (worldServer.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double offsetY = (double) (worldServer.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double offsetZ = (double) (worldServer.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final double x = (double) position.getX() + offsetX;
                    final double y = (double) position.getY() + offsetY;
                    final double z = (double) position.getZ() + offsetZ;
                    EntityItem entityitem = new EntityItem(worldServer, x, y, z, minecraftStack);
                    entityitem.setDefaultPickupDelay();
                    return entityitem;
                })
                .map(EntityUtil::fromNative)
                .collect(Collectors.toList());
        final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                .block(oldBlockSnapshot)
                .type(InternalSpawnTypes.DROPPED_ITEM)
                .build());
        phaseContext.getNotifier().ifPresent(builder::notifier);
        final User entityCreator = phaseContext.getNotifier().orElseGet(() -> phaseContext.getOwner().orElse(null));
        final Cause spawnCauses = builder.build();
        final DropItemEvent.Destruct destruct = SpongeEventFactory.createDropItemEventDestruct(spawnCauses, itemDrops);
        SpongeImpl.postEvent(destruct);
        if (!destruct.isCancelled()) {
            for (Entity entity : destruct.getEntities()) {
                if (entityCreator != null) {
                    EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                }
                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
            }
        }
    }

    public static void spawnEntitiesForBlock(Collection<net.minecraft.entity.Entity> entities, SpongeBlockSnapshot newBlockSnapshot,
        PhaseContext phaseContext, IPhaseState phaseState) {
        // Now we can spawn the entity items appropriately
        final List<Entity> entitiesSpawned = entities.stream()
            .map(EntityUtil::fromNative)
            .collect(Collectors.toList());
        final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
            .block(newBlockSnapshot)
            .type(InternalSpawnTypes.BLOCK_SPAWNING)
            .build());
        final Optional<User> owner = phaseContext.getOwner();
        final Optional<User> notifier = phaseContext.getNotifier();
        notifier.ifPresent(builder::notifier);
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        final Cause spawnCauses = builder.build();
        final SpawnEntityEvent destruct = SpongeEventFactory.createSpawnEntityEvent(spawnCauses, entitiesSpawned);
        SpongeImpl.postEvent(destruct);
        if (!destruct.isCancelled()) {
            for (Entity entity : destruct.getEntities()) {
                if (entityCreator != null) {
                    EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                }
                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
            }
        }
    }
    public static ChangeBlockEvent.Post throwMultiEventsAndCreatePost(ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays,
        List<ChangeBlockEvent> blockEvents,
        ChangeBlockEvent[] mainEvents, Cause.Builder builder) {
        if (!blockEvents.isEmpty()) {
            for (BlockChange blockChange : BlockChange.values()) {
                final ChangeBlockEvent mainEvent = mainEvents[blockChange.ordinal()];
                if (mainEvent != null) {
                    blockChange.suggestNamed(builder, mainEvent);
                }
            }
            final ImmutableList<Transaction<BlockSnapshot>> transactions = transactionArrays[MULTI_CHANGE_INDEX];
            final ChangeBlockEvent.Post post = SpongeEventFactory.createChangeBlockEventPost(builder.build(), transactions);
            SpongeImpl.postEvent(post);
            return post;
        }
        return null;
    }

    public static void splitAndSpawnEntities(Cause cause, List<Entity> entities) {
        splitAndSpawnEntities(cause, entities, (entity) -> {});
    }

    public static void splitAndSpawnEntities(Cause cause, List<Entity> entities, Consumer<IMixinEntity> mixinEntityConsumer) {

        if (entities.size() > 1) {
            final HashMultimap<World, Entity> entityListMap = HashMultimap.create();
            for (Entity entity : entities) {
                entityListMap.put(entity.getWorld(), entity);
            }
            for (Map.Entry<World, Collection<Entity>> entry : entityListMap.asMap().entrySet()) {
                final World world = entry.getKey();
                final ArrayList<Entity> worldEntities = new ArrayList<>(entry.getValue());
                final SpawnEntityEvent event =
                    SpongeEventFactory.createSpawnEntityEvent(cause, worldEntities);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity entity : event.getEntities()) {
                        mixinEntityConsumer.accept(EntityUtil.toMixin(entity));
                        ((IMixinWorldServer) world).forceSpawnEntity(entity);
                    }
                }
            }
            return;
        }

        final Entity singleEntity = entities.get(0);

        final World world = singleEntity.getWorld();

        final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, entities);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            for (Entity entity : event.getEntities()) {
                mixinEntityConsumer.accept(EntityUtil.toMixin(entity));
                ((IMixinWorldServer) world).forceSpawnEntity(entity);
            }
        }
    }
}
