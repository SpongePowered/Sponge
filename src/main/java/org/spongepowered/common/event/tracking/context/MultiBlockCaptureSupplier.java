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
package org.spongepowered.common.event.tracking.context;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.mixin.core.world.WorldServerAccessor;
import org.spongepowered.common.mixin.core.world.chunk.ChunkMixin;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public final class MultiBlockCaptureSupplier implements ICaptureSupplier {

    public static final boolean PRINT_TRANSACTIONS = Boolean.valueOf(System.getProperty("sponge.debugBlockTransactions", "false"));

    @Nullable private LinkedListMultimap<BlockPos, SpongeBlockSnapshot> multimap;
    @Nullable private ListMultimap<BlockPos, BlockEventData> scheduledEvents;
    @Nullable private List<SpongeBlockSnapshot> snapshots;
    @Nullable private LinkedHashMap<ServerWorld, SpongeProxyBlockAccess.Proxy> processingWorlds;
    @Nullable private Set<BlockPos> usedBlocks;
    private int transactionIndex = -1; // These are used to keep track of which snapshot is being referred to as "most recent change"
    private int snapshotIndex = -1;    // so that we can appropriately cancel or discard or apply specific event transactions
    // We made BlockTransaction a Node and this is a pseudo LinkedList due to the nature of needing
    // to be able to track what block states exist at the time of the transaction while other transactions
    // are processing (because future transactions performing logic based on what exists at that state,
    // will potentially get contaminated information based on the last transaction prior to transaction
    // processing). Example: When starting to perform neighbor notifications during piston movement, one
    // can feasibly see that the block state is changed already without being able to get the appropriate
    // block state.
    @Nullable private BlockTransaction tail;
    @Nullable private BlockTransaction head;

    public MultiBlockCaptureSupplier() {
    }

    /**
     * Captures the provided {@link BlockSnapshot} into a {@link Multimap} backed collection.
     * The premise is that each {@link BlockPos} normally has a single {@link BlockChange},
     * with the exceptions of certain few cases where multiple changes can occur for the same
     * position. The larger issue is that while the multiple changes are tracked, the desired
     * flag of changes does not result in a valid {@link BlockChange}, and therefor an invalid
     * {@link ChangeBlockEvent} is generated, potentially leading to duplication bugs with
     * protection plugins. As a result, the consuming {@link BlockSnapshot} is placed into
     * a {@link ListMultimap} keyed by the {@link BlockPos}, and if there are multiple snapshots
     * per {@link BlockPos}, has multiple changes will be {@code true}, and this method
     * will return {@code true}.
     *
     * @param snapshot The snapshot being captured
     * @param newState The most current new IBlockState to calculate the BlockChange flag
     * @return True if the block position has previously not been modified or captured yet
     */
    public boolean put(final BlockSnapshot snapshot, final BlockState newState) {
        // Start by figuring out the backing snapshot. In all likelyhood, we could just cast, but we want to be safe
        final SpongeBlockSnapshot backingSnapshot = getBackingSnapshot(snapshot);
        // Get the key of the block position, we know this is a pure block pos and not a mutable one too.
        final BlockPos blockPos = backingSnapshot.getBlockPos();
        if (this.usedBlocks == null) { // Means we have a first usage. All three fields are null
            // At this point, we know we have not captured anything and
            // can just populate the normal list.
            this.usedBlocks = new HashSet<>();

            this.usedBlocks.add(blockPos);
            this.addSnapshot(backingSnapshot);
            return true;
        }
        // This isn't our first rodeo...
        final boolean added = this.usedBlocks.add(blockPos); // add it to the set of positions already used and use the boolean
        if (this.multimap != null) {
            // Means we've already got multiple changes per position once before.
            // Likewise, the used blocks, snapshots and multimap will NOT be null.
            // more fasts, we know we have multiple block positions.
            // And we can find out if this is the first time we
            if (added) {
                // If the position hasn't been captured yet, that means we need to add it as an original
                // snapshot being changed, for the list usage.
                this.addSnapshot(backingSnapshot);
            }
            // we don't have to
            this.multimap.put(blockPos, backingSnapshot);

            // If the position is duplicated, we need to update the original snapshot of the now incoming block change
            // in relation to the original state (so if a block was set to air, then afterwards set to piston head, it should go from break to modify)
            if (!added) {
                associateBlockChangeForPosition(newState, blockPos);
            }
            return added;
        }
        // We have not yet checked if this incoming snapshot is a duplicate position
        if (!added) {
            // Ok, means we have a multi change on a same position, now to use the multimap
            // for the first time.
            this.multimap = LinkedListMultimap.create(); // LinkedListMultimap is insertion order respective, so the backed lists per
            // Now to populate it from the previously used list of snapshots...
            for (final SpongeBlockSnapshot existing : this.snapshots) { // Ignore snapshots potentially being null, it will never be null at this point.
                this.multimap.put(existing.getBlockPos(), existing);
            }
            // And place the snapshot into the multimap.
            this.multimap.put(blockPos, backingSnapshot);
            // Now we can re-evaluate the modified block position
            // If the position is duplicated, we need to update the original snapshot of the now incoming block change
            // in relation to the original state (so if a block was set to air, then afterwards set to piston head, it should go from break to modify)
            associateBlockChangeForPosition(newState, blockPos);
            return false;
        }
        // At this point, we haven't captured the block position yet.
        // and we can check if the list is null.
        this.addSnapshot(backingSnapshot);
        // And this is the only time that we return true, if we have not caught multiple transactions per position before.
        return true;
    }

    private void addSnapshot(final SpongeBlockSnapshot backingSnapshot) {
        if (this.snapshots == null) {
            this.snapshots = new ArrayList<>();
        }
        this.snapshots.add(backingSnapshot);
        this.snapshotIndex++;
    }

    /**
     * Associates the desired block state {@link BlockChange} in comparison to the
     * already guaranteed original {@link SpongeBlockSnapshot} for proper event
     * creation when multiple block changes exist for the provided {@link BlockPos}.
     *
     * <p>Note: This method <strong>requires</strong> that {@link #multimap} is not
     * {@code null}, otherwise it will cause an NPE.</p>
     *
     * @param newState The incoming block change to compare to change
     * @param blockPos The block position to get the backing list from the multimap
     */
    @SuppressWarnings("unchecked")
    private void associateBlockChangeForPosition(final BlockState newState, final BlockPos blockPos) {
        final List<SpongeBlockSnapshot> list = this.multimap.get(blockPos);
        if (list != null && !list.isEmpty()) {
            final SpongeBlockSnapshot originalSnapshot = list.get(0);
            final PhaseContext<?> peek = PhaseTracker.getInstance().getCurrentContext();
            final BlockState currentState = (BlockState) originalSnapshot.getState();
            originalSnapshot.blockChange = ((IPhaseState) peek.state).associateBlockChangeWithSnapshot(peek, newState, newState.func_177230_c(), currentState, originalSnapshot, currentState.func_177230_c());
        }
    }

    /**
     * Gets an <b>unmodifiable</b> {@link List} of the original
     * {@link BlockSnapshot}s being changed for their respective
     * {@link BlockPos Block Positions}. The list is self updating
     * and the {@link BlockSnapshot}s themselves are self updating
     * based on the current processes within the PhaseTracker's
     * {@link IPhaseState} the game is processing. The reasons for
     * this list to be unmodifiable except by this object are as follows:
     * <ul>
     *     <li>Submitted {@link BlockSnapshot}s are to be added by the
     *     {@link #put(BlockSnapshot, IBlockState)} method.</li>
     *     <li>Adding multiple {@link BlockSnapshot}s per {@link BlockPos}
     *     results in an internal restructuring of storage such that a
     *     {@link Multimap} is created to keep track of intermediary
     *     {@link BlockSnapshot}s. By this nature, the list cannot be modified
     *     except by this capture object.</li>
     *     <li>Removing a {@link BlockSnapshot} is only applicable via
     *     {@link #prune(BlockSnapshot)} or {@link #clear()}. This is to
     *     allow sanity checking for multimap purposes and garbage cleanup
     *     when necessary.</li>
     *     <li>The creation of {@link ChangeBlockEvent}s requires a
     *     {@link Transaction} to be created, and plugins are only
     *     exposed the {@link Transaction#getOriginal()} as the first
     *     {@link BlockSnapshot} that would exist in this list. Intermediary
     *     {@link BlockSnapshot} changes for that postiion are internally
     *     utilized to process physics, but are not exposed to the event.</li>
     * </ul>
     *
     * @return An unmodifiable list of first block originals being changed
     */
    public final List<SpongeBlockSnapshot> get() {
        return this.snapshots == null ? Collections.emptyList() : Collections.unmodifiableList(this.snapshots);
    }

    public final void prune(final BlockSnapshot snapshot) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Unexpected pruning on an empty capture object for position " + snapshot.getPosition());
        }
        // Start by figuring out the backing snapshot. In all likelyhood, we could just cast, but we want to be safe
        final SpongeBlockSnapshot backingSnapshot = getBackingSnapshot(snapshot);
        // Get the key of the block position, we know this is a pure block pos and not a mutable one too.
        final BlockPos blockPos = backingSnapshot.getBlockPos();
        // Check if we have a multi-pos
        if (this.multimap != null) {
            pruneFromMulti(backingSnapshot, blockPos);
            return;
        }
        pruneSingle(backingSnapshot, blockPos);
        if (this.head != null) {
            pruneTransaction(getBackingSnapshot(snapshot));
        }
    }

    private void pruneSingle(final SpongeBlockSnapshot backingSnapshot, final BlockPos blockPos) {
        if (this.usedBlocks == null) {
            // means we didn't actually capture???
            throw new IllegalStateException("Expected to remove a single block change that was supposed to be captured....");
        }
        if (this.snapshots == null) {
            // also means we didn't capture.... wtf is going on at this point?
            throw new IllegalStateException("Expected to remove a single block change that was supposed to be captured....");
        }
        this.usedBlocks.remove(blockPos);
        this.snapshots.remove(backingSnapshot); // Should be the same snapshot used
    }

    private void pruneFromMulti(final SpongeBlockSnapshot backingSnapshot, final BlockPos blockPos) {
        final List<SpongeBlockSnapshot> snapshots = this.multimap.get(blockPos);
        if (snapshots != null) {
            for (final Iterator<SpongeBlockSnapshot> iterator = snapshots.iterator(); iterator.hasNext(); ) {
                final SpongeBlockSnapshot next = iterator.next();
                if (next.getState().equals(backingSnapshot.getState())) {
                    iterator.remove();
                    break;
                }
            }
            // If the list view is now empty, we need to prune the position from the multimap
            if (snapshots.isEmpty()) {
                this.multimap.removeAll(blockPos);
                // And then prune the snapshot from the list of firsts
                for (final Iterator<SpongeBlockSnapshot> firsts = this.snapshots.iterator(); firsts.hasNext(); ) {
                    final SpongeBlockSnapshot next = firsts.next();
                    if (next.equals(backingSnapshot)) {
                        firsts.remove();
                        // And if it's been found, remove the position from the used blocks as well.
                        this.usedBlocks.remove(blockPos);
                        break;
                    }
                }
                if (this.snapshots.isEmpty()) {
                    this.multimap = null;
                }
            }
        }
    }


    private SpongeBlockSnapshot getBackingSnapshot(final BlockSnapshot snapshot) {
        final SpongeBlockSnapshot backingSnapshot;
        if (!(snapshot instanceof SpongeBlockSnapshot)) {
            backingSnapshot = SpongeBlockSnapshotBuilder.pooled().from(snapshot).build();
        } else {
            backingSnapshot = (SpongeBlockSnapshot) snapshot;
        }
        return backingSnapshot;
    }

    /**
     * Returns {@code true} if there are no captured objects.
     *
     * @return {@code true} if empty
     */
    @Override
    public final boolean isEmpty() {
        return (this.snapshots == null || this.snapshots.isEmpty()) && this.head == null;
    }

    /**
     * If not empty, activates the {@link BiConsumer} then clears all captures.
     * The catch with this is that as the underlying list is guaranteed
     * to be the first {@link BlockSnapshot} change, multiple changes can exist
     * for an individual {@link BlockPos}, such that the multi-map needs to
     * provide said information to the consumer.
     *
     * <p>The first {@link List} parameter is identical to having been
     * built from the first elements of each list from the second parameter
     * of {@link Map} where {@link Map#values()} returns the insertion order
     * preserved {@link List} of {@link BlockSnapshot}s for the backed
     * {@link BlockPos}. In simpler terms, the {@link List} is built from
     * the first elements of each list from the {@link Map}s values.</p>
     *
     * @param consumer The consumer to activate
     */
    @SuppressWarnings("UnstableApiUsage") // Guava marks Multimaps.asMap as Beta features
    public final void acceptAndClearIfNotEmpty(final BiConsumer<List<? extends BlockSnapshot>, Map<BlockPos, List<BlockSnapshot>>> consumer) {
        if (this.multimap != null) {
            final List<? extends BlockSnapshot> blockSnapshots = get();
            // Since multimaps provide a view when asMap is called, we need to recreate the collection
            // of the map to pass into the consumer
            final Map<BlockPos, List<SpongeBlockSnapshot>> view = Multimaps.asMap(this.multimap);
            final Map<BlockPos, List<BlockSnapshot>> map = new LinkedHashMap<>(view.size());
            for (final Map.Entry<BlockPos, List<SpongeBlockSnapshot>> entryView : view.entrySet()) {
                map.put(entryView.getKey(), new ArrayList<>(entryView.getValue()));
            }
            this.multimap.clear(); // Clean captured lists before they get potentially contaminated by processing.
            consumer.accept(blockSnapshots, map); // Accept the list and map
        }
    }

    /*
    Begin the more enhanced block tracking. This is only used by states that absolutely need to be able to track certain changes
    that involve more "physics" related transactions, such as neighbor notification tracking, tile entity tracking, and
    normally, intermediary transaction tracking. Because of these states, we need to envelop the information relating to:
    - The most recent block change, if it has been a change that was applied
    - The most recent tile entity being captured
    - The most recent neighbor notification in the order in which it is being applied to in comparison with the most recent block change

    In some rare cases, some block changes may take place after a neighbor notification is submitted, or a tile entity is being replaced.
    To acommodate this, when such cases arise, we attempt to snapshot any potential transactions that may take place prior to their
    blocks being changed, allowing us to take full snapshots of tile entities in the event a complete restoration is required.
    This is achieved through captureNeighborNotification and logTileChange.
     */

    private void logTransaction(final BlockTransaction transaction) {
        if (this.tail != null) {
            this.tail.next = transaction;
        } else {
            this.head = transaction;
        }
        transaction.previous = this.tail;
        this.tail = transaction;
    }

    private void pruneTransaction(final SpongeBlockSnapshot snapshot) {
        if (this.head == null) {
            return;
        }
        for (BlockTransaction transaction = this.head; transaction != null; transaction = transaction.next) {
            if (transaction.equalsSnapshot(snapshot)) {
                final BlockTransaction previous = transaction.previous;
                final BlockTransaction next = transaction.next;
                if (previous == null) {
                    this.head = next;
                } else {
                    previous.next = next;
                    transaction.previous = null;
                }
                if (next == null) {
                    this.tail = previous;
                } else {
                    next.previous = previous;
                    transaction.next = null;
                }
            }
        }

    }

    public void captureNeighborNotification(
            final WorldServerBridge mixinWorldServer, final BlockState notifyState, final BlockPos notifyPos, final Block sourceBlock, final BlockPos sourcePos) {
        final int transactionIndex = ++this.transactionIndex;
        final BlockState actualSourceState = ((ServerWorld) mixinWorldServer).func_180495_p(sourcePos);
        final BlockTransaction.NeighborNotification notification = new BlockTransaction.NeighborNotification(transactionIndex, this.snapshotIndex, mixinWorldServer,
            notifyState, notifyPos, sourceBlock, sourcePos, actualSourceState);
        notification.enqueueChanges(mixinWorldServer.bridge$getProxyAccess(), this);
        logTransaction(notification);
    }

    /**
     * Specifically called by {@link ChunkMixin#bridge$setBlockState(BlockPos, IBlockState, IBlockState, BlockChangeFlag)} while it is preparing
     * various transactional aspects, such as potential tile entity removals, replacements, etc. Specifically should never be called outside
     * of that reaction since {@link BlockTransaction#enqueueChanges(SpongeProxyBlockAccess, MultiBlockCaptureSupplier)}
     * does not get called automatically, it is called prior to queueing potential tile replacements, and prior to calling to
     * {@link #logTileChange(WorldServerBridge, BlockPos, TileEntity, TileEntity)} in the event a tile entity is going to be removed.
     *
     * @param originalBlockSnapshot The original snapshot being changed
     * @param newState The new state
     * @param flags The change flags
     * @return The constructed transaction
     */
    public BlockTransaction.ChangeBlock logBlockChange(final SpongeBlockSnapshot originalBlockSnapshot, final BlockState newState,
        final BlockChangeFlag flags) {
        this.put(originalBlockSnapshot, newState); // Always update the snapshot index before the block change is tracked
        final int transactionIndex = ++this.transactionIndex;
        final BlockTransaction.ChangeBlock changeBlock = new BlockTransaction.ChangeBlock(transactionIndex, this.snapshotIndex,
            originalBlockSnapshot, newState, (SpongeBlockChangeFlag) flags);
        logTransaction(changeBlock);
        return changeBlock;
    }

    public void logTileChange(
            final WorldServerBridge mixinWorldServer, final BlockPos pos, @Nullable final TileEntity oldTile, @Nullable final TileEntity newTile) {
        final ServerWorld world = (ServerWorld) mixinWorldServer;
        final BlockState current = world.func_180495_p(pos);

        if (this.tail instanceof BlockTransaction.ChangeBlock) {
            final BlockTransaction.ChangeBlock changeBlock = (BlockTransaction.ChangeBlock) this.tail;
            if (oldTile != null && newTile == null && changeBlock.queueBreak) {
                if (changeBlock.queuedRemoval == oldTile) {
                    return; // Duplicate requests need to be silenced because multiple attempts to assure a tile is removed can be made
                    // during breaking blocks.
                }
                changeBlock.queuedRemoval = oldTile;
                if (changeBlock.queueTileSet == null) {
                    mixinWorldServer.bridge$getProxyAccess().queueRemoval(oldTile);
                } else {
                    // Make sure the new tile entity has the correct position
                    changeBlock.queueTileSet.func_174878_a(pos);
                    mixinWorldServer.bridge$getProxyAccess().queueReplacement(changeBlock.queueTileSet, changeBlock.queuedRemoval);
                    mixinWorldServer.bridge$getProxyAccess().unmarkRemoval(pos, oldTile);
                }
                return;
            }
        }
        if (newTile != null && this.tail != null) {

            // Double check previous changes, if there's a remove tile entity, and previous to that, a change block, and this is an add tile entity,
            // well, we need to flip the ChangeBlock to avoid doing a breakBlock logic
            boolean isSame = false;
            for (BlockTransaction prevChange = this.tail; prevChange != null; prevChange = prevChange.previous) {
                if (prevChange instanceof BlockTransaction.ChangeBlock) {
                    final BlockTransaction.ChangeBlock changeBlock = (BlockTransaction.ChangeBlock) prevChange;
                    isSame = changeBlock.queuedRemoval == newTile;
                    if (isSame) {
                        changeBlock.ignoreBreakBlockLogic = true;
                        changeBlock.queuedRemoval = null;
                        ((TileEntityBridge) newTile).bridge$setCaptured(false);
                        break;
                    }
                }
            }
            if (isSame) {
                if (mixinWorldServer.bridge$getProxyAccess().isTileQueuedForRemoval(pos, newTile)) {
                    mixinWorldServer.bridge$getProxyAccess().unmarkRemoval(pos, newTile);
                }
                return;
            }
        }
        final int transactionIndex = ++this.transactionIndex;
        if (oldTile != null) {

            final SpongeBlockSnapshot snapshot = mixinWorldServer.bridge$createSnapshotWithEntity(current, pos, BlockChangeFlags.NONE, oldTile);
            this.put(snapshot, current);
            if (newTile != null) {
                // replacing a tile.
                snapshot.blockChange = BlockChange.MODIFY;
                final BlockTransaction.ReplaceTileEntity transaction = new BlockTransaction.ReplaceTileEntity(transactionIndex, this.snapshotIndex, newTile, oldTile, snapshot);
                logTransaction(transaction);
                transaction.enqueueChanges(mixinWorldServer.bridge$getProxyAccess(),this);
                return;
            }
            // Removing the tile
            snapshot.blockChange = BlockChange.BREAK;
            final BlockTransaction.RemoveTileEntity transaction = new BlockTransaction.RemoveTileEntity(transactionIndex, this.snapshotIndex, oldTile, snapshot);
            transaction.enqueueChanges(mixinWorldServer.bridge$getProxyAccess(), this);
            logTransaction(transaction);
            return;
        }
        if (newTile != null) {
            final SpongeBlockSnapshot snapshot = mixinWorldServer.bridge$createSnapshotWithEntity(current, pos, BlockChangeFlags.NONE, newTile);
            snapshot.blockChange = BlockChange.PLACE;
            final BlockTransaction.AddTileEntity
                transaction = new BlockTransaction.AddTileEntity(transactionIndex, this.snapshotIndex, newTile, snapshot);
            transaction.enqueueChanges(mixinWorldServer.bridge$getProxyAccess(), this);
            logTransaction(transaction);
        }
    }

    void queuePreviousStates(final BlockTransaction transaction) {
        if (this.head != null) {
            if (transaction == this.head) {
                return;
            }
            for (BlockTransaction prevChange = this.head; prevChange != null; prevChange = prevChange.next) {
                if (transaction.appliedPreChange) {
                    // Short circuit. It will not have already applied changes to the previous
                    // changes until it at least applies them to the first entry (head).
                    return;
                }
                transaction.provideUnchangedStates(prevChange);
            }
        }
    }

    public void cancelTransaction(final BlockSnapshot original) {
        if (this.tail == null) {
            return;
        }

        final SpongeBlockSnapshot snapshot = (SpongeBlockSnapshot) original;
        final BlockPos blockPos = snapshot.getBlockPos();
        snapshot.getWorldServer().ifPresent(worldServer -> {
            for (BlockTransaction prevChange = this.tail; prevChange != null; prevChange = prevChange.previous) {
                if (!prevChange.isCancelled) {
                    prevChange.cancel(worldServer, blockPos, ((WorldServerBridge) worldServer).bridge$getProxyAccess());
                }
            }
        });
    }



    public void clear() {
        if (this.multimap != null) {
            this.multimap.clear();
            this.multimap = null;
        }
        if (this.snapshots != null) {
            this.snapshots.clear();
            this.snapshots = null;
        }
        if (this.usedBlocks != null) {
            this.usedBlocks.clear();
        }
        if (this.scheduledEvents != null) {
            this.scheduledEvents.clear();
        }
        this.snapshotIndex = -1;
        this.transactionIndex = -1;
    }

    public void restoreOriginals() {
        if (this.snapshots != null && !this.snapshots.isEmpty()) {
            for (final SpongeBlockSnapshot original : Lists.reverse(this.snapshots)) {
                original.restore(true, BlockChangeFlags.NONE);
            }
            this.clear();
        }
    }

    public Optional<Transaction<BlockSnapshot>> createTransaction(final SpongeBlockSnapshot snapshot) {
        final Optional<ServerWorld> maybeWorld = snapshot.getWorldServer();
        if (!maybeWorld.isPresent()) {
            return Optional.empty();
        }
        final ServerWorld worldServer = maybeWorld.get();
        final BlockPos blockPos = snapshot.getBlockPos();
        final BlockState newState = worldServer.func_180495_p(blockPos);
        // Because enhanced tracking requires handling very specific proxying of block states
        // so, the requests for the actual states sometimes may cause issues with mods and their
        // extended state handling logic if what the world sees is different from what our tracker
        // saw, so, we have to just provide the new state (extended states are calculated anyways).
        final BlockState newActualState = this.head != null ? newState : newState.func_185899_b(worldServer, blockPos);
        final BlockSnapshot newSnapshot =
            ((WorldServerBridge) worldServer).bridge$createSnapshot(newState, newActualState, blockPos, BlockChangeFlags.NONE);
        // Up until this point, we can create a default Transaction
        if (this.multimap != null) { // But we need to check if there's any intermediary block changes...
            // And because multi is true, we can be sure the multimap is populated at least somewhere.
            final List<SpongeBlockSnapshot> intermediary = this.multimap.get(blockPos);
            if (!intermediary.isEmpty() && intermediary.size() > 1) {
                // We need to make a carbon copy of the list since it's technically a key view list
                // within the multimap, so, if the multimap is cleared, at the very least, the list will
                // not be cleared. Likewise, we also need to skip over the first element since the snapshots
                // list will have that element anyways (we don't want to be providing duplicate snapshots
                // for plugins to witness and come to expect that they are intermediary states, when they're still the original positions
                final ImmutableList.Builder<SpongeBlockSnapshot> builder = ImmutableList.builder();
                boolean movedPastFirst = false;
                for (final Iterator<SpongeBlockSnapshot> iterator = intermediary.iterator(); iterator.hasNext(); ) {
                    if (!movedPastFirst) {
                        iterator.next();
                        movedPastFirst = true;
                        continue;
                    }
                    builder.add(iterator.next());

                }
                return Optional.of(new Transaction<>(snapshot, newSnapshot, builder.build()));
            }
        }
        return Optional.of(new Transaction<>(snapshot, newSnapshot));
    }

    public boolean trackEvent(final BlockPos pos, final BlockEventData blockEventData) {
        if (this.usedBlocks != null && this.usedBlocks.contains(pos)) {
            if (this.scheduledEvents == null) {
                this.scheduledEvents = LinkedListMultimap.create();
            }
            this.scheduledEvents.put(pos.func_185334_h(), blockEventData);
            return true;
        }
        return false;
    }

    public ListMultimap<BlockPos, BlockEventData> getScheduledEvents() {
        return this.scheduledEvents == null || this.scheduledEvents.isEmpty() ? ImmutableListMultimap.of() : ArrayListMultimap.create(this.scheduledEvents);
    }

    @SuppressWarnings("ReturnInsideFinallyBlock")
    public boolean processTransactions(final List<Transaction<BlockSnapshot>> transactions, final PhaseContext<?> phaseContext, final boolean noCancelledTransactions,
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents, final int currentDepth) {

        final IPhaseState phaseState = phaseContext.state;
        int targetIndex = 0;
        if (this.tail == null) {
            boolean hasEvents = false;
            if (!scheduledEvents.isEmpty()) {
                hasEvents = true;
            }
            for (final Transaction<BlockSnapshot> transaction : transactions) {
                if (!transaction.isValid()) {
                    continue;
                }
                TrackingUtil.performTransactionProcess(transaction, phaseContext, currentDepth);
                if (hasEvents) {
                    final SpongeBlockSnapshot original = (SpongeBlockSnapshot) transaction.getOriginal();
                    original.getWorldServer().ifPresent(worldServer -> {
                        final WorldServerAccessor accessor = (WorldServerAccessor) worldServer;
                        final ServerWorld.ServerBlockEventList queue = accessor.getBlockEventQueueForSponge()[accessor.getBlockEventCacheIndexForSponge()];
                        for (final BlockEventData blockEventData : scheduledEvents.get(original.getBlockPos())) {
                            boolean equals = false;
                            for (final BlockEventData eventData : queue) {
                                if (eventData.equals(blockEventData)) {
                                    equals = true;
                                    break;
                                }
                            }
                            if (!equals) {
                                queue.add(blockEventData);
                            }
                        }
                    });
                }

            }
            return noCancelledTransactions;
        }
        Transaction<BlockSnapshot> eventTransaction = transactions.isEmpty() ? null : transactions.get(targetIndex);
        try {
            // now to clear this suppliers information before we start proceeding
            final BlockTransaction head = this.head;
            this.head = null;
            this.tail = null;
            for (BlockTransaction transaction = head; transaction != null; ) {

                if (transaction.snapshotIndex > targetIndex) {
                    targetIndex++;
                    eventTransaction = transactions.get(targetIndex);
                }
                if (eventTransaction != null && !eventTransaction.isValid()) {
                    final BlockTransaction next = transaction.next;
                    transaction.next = null;
                    transaction.previous = null;
                    transaction = next;
                    continue;
                }
                final Optional<WorldServerBridge> maybeWorld = transaction.getWorldServer();
                final BlockTransaction derp = transaction;
                try (@SuppressWarnings("try") final SpongeProxyBlockAccess access = maybeWorld.map(
                    WorldServerBridge::bridge$getProxyAccess).map(proxy -> proxy.switchTo(derp)).orElse(null);
                     final SpongeProxyBlockAccess.Proxy ignored = maybeWorld.map(transaction::getProxy).orElse(null)){
                    final PrettyPrinter printer;
                    if (PRINT_TRANSACTIONS) {
                        printer = new PrettyPrinter(60).add("Debugging BlockTransaction").centre().hr()
                            .addWrapped(60, "This is a process printout of the information passed along from the Proxy and the world.")
                            .add()
                            .add("Proxy Container:");
                    } else { printer = null; }
                    if (transaction.blocksNotAffected != null) {
                        transaction.blocksNotAffected.forEach((pos, block) -> {
                            if (PRINT_TRANSACTIONS) {
                                printer.addWrapped(120, "  %s : %s, %s", "UnaffectedBlock", pos, block);
                            }
                            if (access != null) {
                                access.proceed(pos, block, false);
                            }
                        });
                    }
                    if (transaction.tilesAtTransaction != null) {
                        transaction.tilesAtTransaction.forEach((pos, tile) -> {
                            if (PRINT_TRANSACTIONS) {
                                printer.addWrapped(120, "  %s : %s, %s", "UnaffectedTile", pos, tile == null ? "null" : ((TileEntityBridge) tile).bridge$getPrettyPrinterString());
                            }
                            if (access != null) {
                                access.pushTile(pos, tile);
                            }
                        });
                    }
                    if (PRINT_TRANSACTIONS) {
                        if (access != null) {
                            access.addToPrinter(printer);
                        }
                        transaction.addToPrinter(printer);
                        printer.print(System.err);
                    }
                    transaction.process(eventTransaction, phaseState, phaseContext, currentDepth);
                } catch (final Exception e) {
                    final PrettyPrinter printer = new PrettyPrinter(60).add("Exception while trying to apply transaction").centre().hr()
                        .addWrapped(60,
                            "BlockTransactions failing to process can lead to unintended consequences. If the exception is *directly* coming from Sponge's code, please report to Sponge.")
                        .add();
                    maybeWorld.map(WorldServerBridge::bridge$getProxyAccess).ifPresent(access -> access.addToPrinter(printer));
                    transaction.addToPrinter(printer);
                    printer.add();
                    printer
                        .add("Exception: ")
                        .add(e)
                        .trace(System.err);
                }
                maybeWorld.map(WorldServerBridge::bridge$getProxyAccess).ifPresent(transaction::postProcessBlocksAffected);

                // Clean up
                final BlockTransaction next = transaction.next;
                transaction.next = null;
                transaction.previous = null;
                transaction = next;

            }
        } finally {
            if (this.processingWorlds == null) {
                return noCancelledTransactions;
            }
            for (final Map.Entry<ServerWorld, SpongeProxyBlockAccess.Proxy> entry : this.processingWorlds.entrySet()) {
                try {
                    entry.getValue().close();
                } catch (final Exception e) {
                    PhaseTracker.getInstance().printMessageWithCaughtException("Forcibly Closing Proxy", "Proxy Access could not be popped", e);
                }
            }
            this.processingWorlds.clear();
            for (BlockTransaction transaction = this.head; transaction != null; ) {
                final BlockTransaction next = transaction.next;
                transaction.previous = null;
                transaction.next = null;
                transaction = next;
            }
            this.head = null;
            this.tail = null;
        }
        return noCancelledTransactions;
    }

    @SuppressWarnings("RedundantCast")
    void getProxyOrCreate(final WorldServerBridge mixinWorldServer) {
        if (this.processingWorlds == null) {
            this.processingWorlds = new LinkedHashMap<>();
        }
        SpongeProxyBlockAccess.Proxy existing = this.processingWorlds.get((ServerWorld) mixinWorldServer);
        if (existing == null) {
            existing = mixinWorldServer.bridge$getProxyAccess().pushProxy();
            this.processingWorlds.put((ServerWorld) mixinWorldServer, existing);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.snapshots);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MultiBlockCaptureSupplier other = (MultiBlockCaptureSupplier) obj;
        return Objects.equals(this.multimap, other.multimap);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
            .add("Captured", this.snapshots == null ? 0 : this.snapshots.size())
            .add("Head", this.head == null ? "null" : this.head)
            .toString();
    }

    public void clearProxies() {
        if (this.processingWorlds == null || this.processingWorlds.isEmpty()) {
            return;
        }
        for (final Map.Entry<ServerWorld, SpongeProxyBlockAccess.Proxy> entry : this.processingWorlds.entrySet()) {
            try {
                entry.getValue().close();
            } catch (final Exception e) {
                PhaseTracker.getInstance().printMessageWithCaughtException("Forcibly Closing Proxy", "Proxy Access could not be popped", e);
            }
        }
    }

    public boolean hasTransactions() {
        return this.head != null;
    }

    public boolean hasBlocksCaptured() {
        return !(this.snapshots == null || this.snapshots.isEmpty());
    }

    public void reset() {
        if (this.multimap != null) {
            // shouldn't but whatever, it's the end of a phase.
            this.multimap.clear();
            this.multimap = null;
        }
        if (this.scheduledEvents != null) {
            this.scheduledEvents.clear();
        }
        if (this.snapshots != null) {
            this.snapshots.clear();
            this.snapshots = null;
        }
        if (this.usedBlocks != null) {
            this.usedBlocks.clear();
        }
        this.clearProxies();
        this.transactionIndex = -1;
        this.snapshotIndex = -1;
        if (this.head != null) {
            this.head = null;
            this.tail = null;
            for (BlockTransaction transaction = this.head; transaction != null; ) {
                final BlockTransaction next = transaction.next;
                transaction.previous = null;
                transaction.next = null;
                transaction = next;
            }

        }

    }
}
