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
package org.spongepowered.common.event.tracking.phase.general;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CapturedMultiMapSupplier;
import org.spongepowered.common.event.tracking.CapturedSupplier;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public final class GeneralPhase extends TrackingPhase {

    public static final class State {
        public static final IPhaseState COMMAND = new CommandState();
        public static final IPhaseState EXPLOSION = new ExplosionState();
        public static final IPhaseState COMPLETE = new CompletePhase();
        public static final IPhaseState MARKER_CROSS_WORLD = new MarkerCrossWorld();
        public static final IPhaseState GAME_STATE_EVENTS = new GameStateEventState();
        public static final IPhaseState TILE_ENTITY_UNLOAD = new TileEntityUnloadState();
        public static final IPhaseState WORLD_UNLOAD = new WorldUnload();

        private State() { }
    }

    public static final class Post {
        public static final IPhaseState UNWINDING = new PostState();

        private Post() { }
    }


    public static GeneralPhase getInstance() {
        return Holder.INSTANCE;
    }

    private GeneralPhase() {
    }

    private static final class Holder {
        static final GeneralPhase INSTANCE = new GeneralPhase();
    }

    @Override
    public void unwind(IPhaseState state, PhaseContext phaseContext) {
        ((GeneralState) state).unwind(phaseContext);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postDispatch(IPhaseState unwindingState, PhaseContext unwindingContext, PhaseContext postContext) {
        final List<BlockSnapshot> contextBlocks = postContext.getCapturedBlockSupplier().orEmptyList();
        final List<Entity> contextEntities = postContext.getCapturedEntitySupplier().orEmptyList();
        final List<Entity> contextItems = (List<Entity>) (List<?>) postContext.getCapturedItemsSupplier().orEmptyList();
        if (contextBlocks.isEmpty() && contextEntities.isEmpty() && contextItems.isEmpty()) {
            return;
        }
        if (!contextBlocks.isEmpty()) {
            final List<BlockSnapshot> blockSnapshots = new ArrayList<>(contextBlocks);
            contextBlocks.clear();
            processBlockTransactionListsPost(postContext, blockSnapshots, unwindingState, unwindingContext);
        }
        if (!contextEntities.isEmpty()) {
            final ArrayList<Entity> entities = new ArrayList<>(contextEntities);
            contextEntities.clear();
            unwindingState.getPhase().processPostEntitySpawns(unwindingState, unwindingContext, entities);
        }
        if (!contextItems.isEmpty()) {
            final ArrayList<Entity> items = new ArrayList<>(contextItems);
            contextItems.clear();
            unwindingState.getPhase().processPostItemSpawns(unwindingState, items);
        }

    }

    /**
     *  @param snapshotsToProcess
     * @param unwindingState
     * @param unwinding
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void processBlockTransactionListsPost(PhaseContext postContext, List<BlockSnapshot> snapshotsToProcess,
        IPhaseState unwindingState, PhaseContext unwinding) {
        final List<Transaction<BlockSnapshot>> invalidTransactions = new ArrayList<>();
        ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[TrackingUtil.EVENT_COUNT];
        ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[TrackingUtil.EVENT_COUNT];
        for (int i = 0; i < TrackingUtil.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        final List<ChangeBlockEvent> blockEvents = new ArrayList<>();

        for (BlockSnapshot snapshot : snapshotsToProcess) {
            // This processes each snapshot to assign them to the correct event in the next area, with the
            // correct builder array entry.
            TrackingUtil.TRANSACTION_PROCESSOR.apply(transactionBuilders)
                    .accept(TrackingUtil.TRANSACTION_CREATION.apply(snapshot));
        }

        for (int i = 0; i < TrackingUtil.EVENT_COUNT; i++) {
            // Build each event array
            transactionArrays[i] = transactionBuilders[i].build();
        }
        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
        // This likely needs to delegate to the phase in the event we don't use the source object as the main object causing the block changes
        // case in point for WorldTick event listeners since the players are captured non-deterministically
        final Cause.Builder builder = Cause.source(unwinding.getSource(Object.class).get());
        // Creates the block events accordingly to the transaction arrays
        TrackingUtil.iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents, builder);
        // We create the post event and of course post it in the method, regardless whether any transactions are invalidated or not
        final ChangeBlockEvent.Post
                postEvent =
                TrackingUtil.throwMultiEventsAndCreatePost(transactionArrays, blockEvents, mainEvents, builder);

        if (postEvent == null) { // Means that we have had no actual block changes apparently?
            return;
        }

        // Iterate through the block events to mark any transactions as invalid to accumilate after (since the post event contains all
        // transactions of the preceeding block events)
        for (ChangeBlockEvent blockEvent : blockEvents) { // Need to only check if the event is cancelled, If it is, restore
            if (blockEvent.isCancelled()) {
                // Don't restore the transactions just yet, since we're just marking them as invalid for now
                for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                    transaction.setValid(false);
                }
            }
        }

        // Finally check the post event
        if (postEvent.isCancelled()) {
            // Of course, if post is cancelled, just mark all transactions as invalid.
            for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
                transaction.setValid(false);
            }
        }

        // Now we can gather the invalid transactions that either were marked as invalid from an event listener - OR - cancelled.
        // Because after, we will restore all the invalid transactions in reverse order.
        for (Transaction<BlockSnapshot> transaction : postEvent.getTransactions()) {
            if (!transaction.isValid()) {
                invalidTransactions.add(transaction);
            }
        }

        if (!invalidTransactions.isEmpty()) {
            // NOW we restore the invalid transactions (remember invalid transactions are from either plugins marking them as invalid
            // or the events were cancelled), again in reverse order of which they were received.
            for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalidTransactions)) {
                transaction.getOriginal().restore(true, BlockChangeFlag.NONE);
                if (unwindingState.tracksBlockSpecificDrops()) {
                    // Cancel any block drops or harvests for the block change.
                    // This prevents unnecessary spawns.
                    final BlockPos position = ((IMixinLocation) (Object) transaction.getOriginal().getLocation().get()).getBlockPos();
                    postContext.getBlockDropSupplier().ifPresentAndNotEmpty(map -> {
                        if (map.containsKey(position)) {
                            map.get(position).clear();
                        }
                    });
                }
            }
            invalidTransactions.clear();
        }
        performPostBlockAdditions(postContext, postEvent.getTransactions(), builder, unwindingState, unwinding);
    }

    private static void performPostBlockAdditions(PhaseContext postContext, List<Transaction<BlockSnapshot>> transactions,
        Cause.Builder builder, IPhaseState unwindingState, PhaseContext unwindingPhaseContext) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        final SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(transactions);
        final CapturedMultiMapSupplier<BlockPos, ItemDropData> capturedBlockDrops = postContext.getBlockDropSupplier();
        final CapturedMultiMapSupplier<BlockPos, EntityItem> capturedBlockItemEntityDrops = postContext.getBlockItemDropSupplier();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                transaction.getFinal().restore(true, BlockChangeFlag.ALL);
            }

            final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();

            // Handle item drops captured
            final Location<World> worldLocation = oldBlockSnapshot.getLocation().get();
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldLocation.getExtent();
            final BlockPos pos = ((IMixinLocation) (Object) worldLocation).getBlockPos();
            capturedBlockDrops.ifPresentAndNotEmpty(map -> TrackingUtil
                    .spawnItemDataForBlockDrops(map.containsKey(pos) ? map.get(pos) : Collections.emptyList(), newBlockSnapshot, unwindingPhaseContext, unwindingState));
            capturedBlockItemEntityDrops.ifPresentAndNotEmpty(map -> TrackingUtil
                    .spawnItemEntitiesForBlockDrops(map.containsKey(pos) ? map.get(pos) : Collections.emptyList(), newBlockSnapshot,
                        unwindingPhaseContext, unwindingState));

            final WorldServer worldServer = mixinWorldServer.asMinecraftWorld();
            SpongeHooks.logBlockAction(builder, worldServer, oldBlockSnapshot.blockChange, transaction);
            final BlockChangeFlag changeFlag = oldBlockSnapshot.getChangeFlag();
            final int updateFlag = oldBlockSnapshot.getUpdateFlag();
            final IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
            final IBlockState newState = (IBlockState) newBlockSnapshot.getState();
            // Containers get placed automatically
            final CapturedSupplier<BlockSnapshot> capturedBlockSupplier = postContext.getCapturedBlockSupplier();
            if (changeFlag.performBlockPhysics() && originalState.getBlock() != newState.getBlock() && !SpongeImplHooks.hasBlockTileEntity(newState.getBlock(),
                    newState)) {
                newState.getBlock().onBlockAdded(worldServer, pos, newState);
                postContext.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {

                });
                capturedBlockSupplier.ifPresentAndNotEmpty(blocks -> {
                    final List<BlockSnapshot> blockSnapshots = new ArrayList<>(blocks);
                    blocks.clear();
                    processBlockTransactionListsPost(postContext, blockSnapshots, unwindingState, unwindingPhaseContext);
                });
            }

            proxyBlockAccess.proceed();

            unwindingState.handleBlockChangeWithUser(oldBlockSnapshot.blockChange, transaction, unwindingPhaseContext);

            if (((updateFlag & 2) != 0)) {
                // Since notifyBlockUpdate is basically to tell clients that the block position has changed,
                // we need to respect that flag
                worldServer.notifyBlockUpdate(pos, originalState, newState, updateFlag);
            }

            if (changeFlag.updateNeighbors()) { // Notify neighbors only if the change flag allowed it.
                mixinWorldServer.spongeNotifyNeighborsPostBlockChange(pos, originalState, newState, oldBlockSnapshot.getUpdateFlag());
            } else if ((updateFlag & 16) == 0) {
                worldServer.updateObservingBlocksAt(pos, newState.getBlock());
            }

            capturedBlockSupplier.ifPresentAndNotEmpty(blocks -> {
                final List<BlockSnapshot> blockSnapshots = new ArrayList<>(blocks);
                blocks.clear();
                processBlockTransactionListsPost(postContext, blockSnapshots, unwindingState, unwindingPhaseContext);
            });
        }
    }

    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return phaseData.state == Post.UNWINDING;
    }

    @Override
    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return false;
    }

    @Override
    public boolean alreadyCapturingEntitySpawns(IPhaseState state) {
        return state == Post.UNWINDING || state == State.EXPLOSION;
    }

    @Override
    public boolean alreadyCapturingEntityTicks(IPhaseState state) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingTileTicks(IPhaseState state) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingItemSpawns(IPhaseState currentState) {
        return currentState == Post.UNWINDING || currentState == State.EXPLOSION;
    }

    @Override
    public boolean ignoresItemPreMerging(IPhaseState currentState) {
        return currentState == State.COMMAND || currentState == State.COMPLETE || super.ignoresItemPreMerging(currentState);
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return currentState != State.COMPLETE && currentState != State.MARKER_CROSS_WORLD;
    }

    @Override
    public boolean requiresPost(IPhaseState state) {
        return ((GeneralState) state).requiresPost();
    }

    @Override
    public void associateNeighborStateNotifier(IPhaseState state, PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        if (state == Post.UNWINDING) {
            final IPhaseState unwindingState = context.firstNamed(InternalNamedCauses.Tracker.UNWINDING_STATE, IPhaseState.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Intended to be unwinding a phase but no phase unwinding found!", context));
            final PhaseContext unwindingContext = context.firstNamed(InternalNamedCauses.Tracker.UNWINDING_CONTEXT, PhaseContext.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Intended to be unwinding a phase with a context, but no context found!", context));
            unwindingState.getPhase()
                    .associateNeighborStateNotifier(unwindingState, unwindingContext, sourcePos, block, notifyPos, minecraftWorld, notifier);
        } else if (state == State.COMMAND) {
            context.getSource(Player.class)
                    .ifPresent(player -> ((IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos))
                            .setBlockNotifier(notifyPos, player.getUniqueId()));

        }
    }

    @Override
    public boolean ignoresScheduledUpdates(IPhaseState phaseState) {
        return phaseState == Post.UNWINDING;
    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return ((GeneralState) phaseState).spawnEntityOrCapture(context, entity, chunkX, chunkZ);
    }

    @Override
    public void appendContextPreExplosion(PhaseContext phaseContext, PhaseData currentPhaseData) {
        if (currentPhaseData.state == Post.UNWINDING) {
            ((PostState) currentPhaseData.state).appendContextPreExplosion(phaseContext, currentPhaseData);
            return;
        }
        super.appendContextPreExplosion(phaseContext, currentPhaseData);
    }

    @Override
    public Cause generateTeleportCause(IPhaseState state, PhaseContext context) {
        return ((GeneralState) state).generateTeleportCause(context);
    }
}
