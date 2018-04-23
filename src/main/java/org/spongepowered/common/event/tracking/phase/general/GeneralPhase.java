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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.context.CapturedMultiMapSupplier;
import org.spongepowered.common.event.tracking.context.CapturedSupplier;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.List;

public final class GeneralPhase extends TrackingPhase {

    public static final class State {
        public static final IPhaseState<CommandPhaseContext> COMMAND = new CommandState();
        public static final IPhaseState<ExplosionContext> EXPLOSION = new ExplosionState();
        public static final IPhaseState<GeneralizedContext> COMPLETE = new CompletePhase();
        public static final IPhaseState<?> WORLD_UNLOAD = new WorldUnload();

        private State() { }
    }

    public static final class Post {
        public static final IPhaseState<UnwindingPhaseContext> UNWINDING = new PostState();

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


    /**
     *  @param snapshotsToProcess
     * @param unwindingState
     * @param unwinding
     */
    @SuppressWarnings({"unchecked"})
    public static void processBlockTransactionListsPost(PhaseContext<?> postContext, List<BlockSnapshot> snapshotsToProcess,
                                                        IPhaseState<?> unwindingState, PhaseContext<?> unwinding) {
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

        // Clear captured snapshots after processing them
        postContext.getCapturedBlocksOrEmptyList().clear();

        final ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
        // This likely needs to delegate to the phase in the event we don't use the source object as the main object causing the block changes
        // case in point for WorldTick event listeners since the players are captured non-deterministically
        // Creates the block events accordingly to the transaction arrays
        TrackingUtil.iterateChangeBlockEvents(transactionArrays, blockEvents, mainEvents);
        // We create the post event and of course post it in the method, regardless whether any transactions are invalidated or not
        final ChangeBlockEvent.Post
                postEvent =
                TrackingUtil.throwMultiEventsAndCreatePost(transactionArrays, blockEvents, mainEvents);

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
                transaction.getOriginal().restore(true, BlockChangeFlags.NONE);
                if (unwindingState.tracksBlockSpecificDrops()) {
                    // Cancel any block drops or harvests for the block change.
                    // This prevents unnecessary spawns.
                    final Location<World> location = transaction.getOriginal().getLocation().orElse(null);
                    if (location != null) {
                        // Cancel any block drops performed, avoids any item drops, regardless
                        final BlockPos pos = ((IMixinLocation) (Object) location).getBlockPos();
                        postContext.getBlockDropSupplier().removeAllIfNotEmpty(pos);
                    }
                }
            }
            invalidTransactions.clear();
        }
        performPostBlockAdditions(postContext, postEvent.getTransactions(), unwindingState, unwinding);
    }

    @SuppressWarnings("unchecked")
    private static void performPostBlockAdditions(PhaseContext<?> postContext, List<Transaction<BlockSnapshot>> transactions,
                                                  IPhaseState<?> unwindingState, PhaseContext<?> unwindingPhaseContext) {
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
                transaction.getFinal().restore(true, BlockChangeFlags.ALL);
            }

            final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();

            // Handle item drops captured
            final Location<World> worldLocation = oldBlockSnapshot.getLocation().get();
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldLocation.getExtent();
            final BlockPos pos = ((IMixinLocation) (Object) worldLocation).getBlockPos();
            capturedBlockDrops.acceptAndRemoveIfPresent(pos, items -> TrackingUtil
                    .spawnItemDataForBlockDrops(items, oldBlockSnapshot, unwindingPhaseContext));
            capturedBlockItemEntityDrops.acceptAndRemoveIfPresent(pos, items -> TrackingUtil
                    .spawnItemEntitiesForBlockDrops(items, oldBlockSnapshot, unwindingPhaseContext));

            final WorldServer worldServer = mixinWorldServer.asMinecraftWorld();
            SpongeHooks.logBlockAction(worldServer, oldBlockSnapshot.blockChange, transaction);
            final SpongeBlockChangeFlag spongeFlag = oldBlockSnapshot.getChangeFlag();
            final int updateFlag =  spongeFlag.getRawFlag();
            final IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
            final IBlockState newState = (IBlockState) newBlockSnapshot.getState();
            // Containers get placed automatically
            final CapturedSupplier<BlockSnapshot> capturedBlockSupplier = postContext.getCapturedBlockSupplier();
            if (spongeFlag.performBlockPhysics() && originalState.getBlock() != newState.getBlock() && !SpongeImplHooks.hasBlockTileEntity(newState.getBlock(),
                    newState)) {
                newState.getBlock().onBlockAdded(worldServer, pos, newState);
                postContext.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {
                    final ArrayList<Entity> capturedEntities = new ArrayList<>(entities);
                    ((IPhaseState) unwindingState).postProcessSpawns(unwindingPhaseContext, capturedEntities);
                });
                capturedBlockSupplier.acceptAndClearIfNotEmpty(blocks -> {
                    final List<BlockSnapshot> blockSnapshots = new ArrayList<>(blocks);
                    processBlockTransactionListsPost(postContext, blockSnapshots, unwindingState, unwindingPhaseContext);
                });
            }

            proxyBlockAccess.proceed();

            ((IPhaseState) unwindingState).handleBlockChangeWithUser(oldBlockSnapshot.blockChange, transaction, unwindingPhaseContext);

            if (spongeFlag.isNotifyClients()) {
                // Since notifyBlockUpdate is basically to tell clients that the block position has changed,
                // we need to respect that flag
                worldServer.notifyBlockUpdate(pos, originalState, newState, updateFlag);
            }

            if (spongeFlag.updateNeighbors()) { // Notify neighbors only if the change flag allowed it.
                mixinWorldServer.spongeNotifyNeighborsPostBlockChange(pos, originalState, newState, spongeFlag);
            } else if (spongeFlag.notifyObservers()) {
                worldServer.updateObservingBlocksAt(pos, newState.getBlock());
            }

            capturedBlockSupplier.acceptAndClearIfNotEmpty(blocks -> {
                final List<BlockSnapshot> blockSnapshots = new ArrayList<>(blocks);
                blocks.clear();
                processBlockTransactionListsPost(postContext, blockSnapshots, unwindingState, unwindingPhaseContext);
            });
        }
    }

}
