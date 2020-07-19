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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Queues;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.bridge.tileentity.TrackableTileEntityBridge;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.TrackedChunkBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.accessor.world.WorldAccessor;
import org.spongepowered.common.world.BlockChange;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SpongeProxyBlockAccess implements IBlockReader, AutoCloseable {
    private static final boolean DEBUG_PROXY = Boolean.parseBoolean(System.getProperty("sponge.debugProxyChanges", "false"));

    private final LinkedHashMap<BlockPos, BlockState> processed = new LinkedHashMap<>();
    private final LinkedHashMap<BlockPos, TileEntity> affectedTileEntities = new LinkedHashMap<>();
    private final ListMultimap<BlockPos, TileEntity> queuedTiles = LinkedListMultimap.create();
    private final ListMultimap<BlockPos, TileEntity> queuedRemovals = LinkedListMultimap.create();
    private final Set<BlockPos> markedRemoved = new HashSet<>();
    private final Deque<Proxy> proxies = Queues.newArrayDeque();
    private ServerWorld processingWorld;
    @Nullable private BlockTransaction processingTransaction;
    @Nullable private Deque<BlockTransaction> processingStack;
    private boolean isNeighbor = false;

    public SpongeProxyBlockAccess(final TrackedWorldBridge worldServer) {
        this.processingWorld = ((ServerWorld) worldServer);
    }

    Proxy pushProxy() {
        final Proxy proxy = new Proxy(this);
        this.proxies.push(proxy);
        if (SpongeProxyBlockAccess.DEBUG_PROXY) {
            proxy.stack_debug = new Exception();
        }
        return proxy;
    }

    SpongeProxyBlockAccess proceed(final BlockPos pos, final BlockState state, final boolean setStateForProcessing) {
        if (this.proxies.isEmpty()) {
            throw new IllegalStateException("Cannot push a new block change without having proxies!");
        }
        final BlockState existing = this.processed.put(pos, state);

        if (!this.proxies.isEmpty()) {
            final Proxy proxy = this.proxies.peek();
            if (existing == null) {
                proxy.markNew(pos);
            } else if ((this.processingTransaction != null || !proxy.isStored(pos)) && !proxy.isNew(pos)) {
                proxy.store(pos, state);
            }
        }
        if (setStateForProcessing && this.processingTransaction != null) {
            PhaseTracker.getInstance().setBlockState((TrackedWorldBridge) this.processingWorld, pos, state, BlockChangeFlags.NONE);
        }
        return this;
    }

    private boolean hasTile = false;

    private void popProxy(final Proxy oldProxy) {
        if (oldProxy == null) {
            throw new IllegalArgumentException("Cannot pop a null proxy!");
        }
        final Proxy proxy = this.proxies.peek();
        if (proxy == null || proxy != oldProxy) {
            int offset = -1;
            int i = 0;
            for (final Proxy f : this.proxies) {
                if (f == oldProxy) {
                    offset = i;
                    break;
                }
                i++;
            }
            if (!SpongeProxyBlockAccess.DEBUG_PROXY && offset == -1) {
                // if we're not debugging the cause proxies then throw an error
                // immediately otherwise let the pretty printer output the proxy
                // that was erroneously popped.
                throw new IllegalStateException("Block Change Proxy corruption! Attempted to pop a proxy that was not on the stack.");
            }
            final PrettyPrinter printer = new PrettyPrinter(100).add("Block Change Proxy Corruption!").centre().hr()
                .add("Found %n proxies left on the stack. Clearing them all.", new Object[]{offset + 1});
            if (!SpongeProxyBlockAccess.DEBUG_PROXY) {
                printer.add()
                    .add("Please add -Dsponge.debugProxyChanges=true to your startup flags to enable further debugging output.");
                SpongeCommon.getLogger().warn("  Add -Dsponge.debugProxyChanges to your startup flags to enable further debugging output.");
            } else {
                printer.add()
                    .add("Attempting to pop proxy:")
                    .add(proxy.stack_debug)
                    .add()
                    .add("Frames being popped are:")
                    .add(oldProxy.stack_debug);
            }
            while (offset >= 0) {
                final Proxy f = this.proxies.peek();
                if (SpongeProxyBlockAccess.DEBUG_PROXY && offset > 0) {
                    printer.add("   Stack proxy in position %n :", offset);
                    printer.add(f.stack_debug);
                }
                this.popProxy(f);
                offset--;
            }
            printer.trace(System.err, SpongeCommon.getLogger(), Level.ERROR);
            if (offset == -1) {
                // Popping a proxy that was not on the stack is not recoverable
                // so we throw an exception.
                throw new IllegalStateException("Cause Stack Proxy Corruption! Attempted to pop a proxy that was not on the stack.");
            }
            return;
        }
        this.proxies.pop();
        if (proxy.hasNew()) {
            for (final BlockPos pos : proxy.newBlocks) {
                this.processed.remove(pos);
            }
        }
        if (proxy.hasStored()) {
            if (!this.proxies.isEmpty()) {
                for (final Map.Entry<BlockPos, BlockState> entry : proxy.processed.entrySet()) {
                    this.processed.put(entry.getKey(), entry.getValue());
                }
            } else {
                for (final BlockPos blockPos : proxy.processed.keySet()) {
                    this.processed.remove(blockPos);
                }
            }
        }
        if (proxy.hasRemovals()) {
            for (final BlockPos removedTile : proxy.markedRemovedTiles) {
                this.markedRemoved.remove(removedTile);
            }
        }
        if (this.proxies.isEmpty()) {
            PrettyPrinter pretty = null;
            if (!this.processed.isEmpty()) {
                pretty = new PrettyPrinter(60)
                    .add("%s : %s", "Remaining", this.processed.size());
                final PrettyPrinter printer = pretty;
                this.processed.forEach(((pos, state) -> printer.add("- %s : %s", "Pos", pos).addWrapped(60, "  %s : %s", "State", state)));
                this.processed.clear();
            }
            if (!this.markedRemoved.isEmpty()) {
                if (pretty == null) {
                    pretty = new PrettyPrinter(60);
                }
                pretty.add("Unclaimed Removed Tile Positions");
                final PrettyPrinter printer = pretty;
                this.markedRemoved.forEach(pos -> printer.add("  -%s", pos));
                pretty.add();
                this.markedRemoved.clear();
            }
            if (!this.queuedTiles.isEmpty()) {
                if (pretty == null) {
                    pretty = new PrettyPrinter(60);
                }
                pretty.add("Unadded TileEntities queued for addition");
                final PrettyPrinter printer = pretty;
                this.queuedTiles.forEach((pos, tile) -> printer.add(" - %s : %s", pos, tile == null ? "null" : ((TileEntityBridge) tile).bridge$getPrettyPrinterString()));
                this.queuedTiles.clear();
            }
            if (!this.queuedRemovals.isEmpty()) {
                if (pretty == null) {
                    pretty = new PrettyPrinter(60);
                }
                pretty.add("Unremoved TileEntities queued for removal!");
                final PrettyPrinter printer = pretty;
                this.queuedRemovals.forEach((pos, tile) -> printer.add(" - %s : %s", pos, tile == null ? "null" : ((TileEntityBridge) tile).bridge$getPrettyPrinterString()));
                this.queuedRemovals.clear();
            }
            if (!this.affectedTileEntities.isEmpty()) {
                if (pretty == null) {
                    pretty = new PrettyPrinter(60);
                }
                final PrettyPrinter printer = pretty;
                this.affectedTileEntities.forEach(((pos, tileEntity) -> {
                    if (tileEntity == null) {
                        return;
                    }
                    if (!this.hasTile) {
                        printer.add("Unremoved TileEntities affected by the proxy, likely will cause issues if these are meant to be added to the world!");
                    }
                    this.hasTile = true;
                    printer.add(" - %s : %s", pos, ((TileEntityBridge) tileEntity).bridge$getPrettyPrinterString());
                }));
                this.affectedTileEntities.clear();
            }

            if (pretty != null) {
                if (this.hasTile) {
                    pretty.add("Following the necessary steps to have removed the above entries, the proxy is now being cleared.");
                    this.hasTile = false;
                    pretty.trace(System.err);
                }
            }
        }
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos) {
        return this.affectedTileEntities.get(pos);
    }

    public boolean hasTileEntity(final BlockPos pos) {
        return this.affectedTileEntities.containsKey(pos);
    }

    public boolean hasTileEntity(final BlockPos pos, final TileEntity tileEntity) {
        return this.affectedTileEntities.get(pos) == tileEntity;
    }

    public boolean isTileEntityRemoved(final BlockPos pos) {
        return this.markedRemoved.contains(pos);
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        return this.processed.get(pos);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return null;
    }


    public void onChunkChanged(final BlockPos pos, final BlockState newState) {
        // We can prune the existing block state.
        if (this.proxies.isEmpty()) {
            // Don't push any changes to the proxy when we're not actually
            // capturing changes or using proxies.
            if (!this.processed.isEmpty()) {
                this.processed.clear();
            }
            return;
        }
        if (this.processingTransaction != null) {
            for (BlockTransaction transaction = this.processingTransaction; transaction != null; transaction = transaction.next) {
                if (transaction.acceptChunkChange(pos, newState)) {
                    final BlockState existing = transaction.blocksNotAffected.put(pos, newState);
                }
            }
        }
        this.proceed(pos, newState, false);
    }

    private void unmarkRemoval(final BlockPos pos) {
        this.markedRemoved.remove(pos);
        if (!this.proxies.isEmpty()) {
            final Proxy proxy = this.proxies.peek();
            if (proxy.isMarkedForRemoval(pos)) {
                proxy.unmarkRemoval(pos);
            }
        }
    }

    void unmarkRemoval(final BlockPos pos, final TileEntity tileEntity) {
        this.unmarkRemoval(pos);
        if (tileEntity != null) {
            this.queuedRemovals.remove(pos, tileEntity);
            final TileEntity removed = this.affectedTileEntities.remove(pos);
            if (removed != null) {
                this.affectedTileEntities.put(pos, tileEntity);
            }
        }
    }

    void proceedWithRemoval(final BlockPos targetPosition, final TileEntity removed) {
        this.markedRemoved.remove(targetPosition);
        final TileEntity existing = this.affectedTileEntities.remove(targetPosition);
        // existing should be removed
        // Always remove the tile entity from various lists.
        if (removed != null) {
            this.queuedRemovals.remove(targetPosition, removed);
            if (this.queuedTiles.containsEntry(targetPosition, removed)) {
                this.markRemovedTile(targetPosition);
            } else {
                this.removeTileEntityFromWorldAndChunk(removed);
            }
        }
    }

    private void removeTileEntityFromWorldAndChunk(final TileEntity removed) {
        if (((WorldAccessor) this.processingWorld).accessor$getProcessingLoadedTiles()) {
            ((WorldAccessor) this.processingWorld).accessor$getAddedTileEntityList().remove(removed);
            if (!(removed instanceof ITickableTileEntity)) { //Forge: If they are not tickable they wont be removed in the update loop.
                this.processingWorld.loadedTileEntityList.remove(removed);
            }
        } else {
            ((WorldAccessor) this.processingWorld).accessor$getAddedTileEntityList().remove(removed);
            this.processingWorld.loadedTileEntityList.remove(removed);
            this.processingWorld.tickableTileEntities.remove(removed);
        }
        final TrackedChunkBridge activeChunk = (TrackedChunkBridge) ((ActiveChunkReferantBridge) removed).bridge$getActiveChunk();
        if (activeChunk != null) {
            activeChunk.bridge$removeTileEntity(removed);
        }
    }

    void proceedWithAdd(final BlockPos targetPos, final TileEntity added) {
        final boolean removed = this.queuedTiles.remove(targetPos, added);
        if (!removed) {
            // someone else popped for us?
            System.err.println("Unknown removal for: " + targetPos + " with tile entity: " + added);
        }
        this.unmarkRemoval(targetPos, added);
        final TileEntity existing = this.affectedTileEntities.remove(targetPos);
        if (existing != null && existing != added) {
            ((TrackableTileEntityBridge) existing).bridge$setCaptured(false);
            existing.remove();
        }
        ((TrackableTileEntityBridge) added).bridge$setCaptured(false);
        if (((WorldAccessor) this.processingWorld).accessor$getProcessingLoadedTiles()) {
            added.setPos(targetPos);
            if (added.getWorld() != this.processingWorld) {
                added.setWorld(this.processingWorld);
            }
            ((WorldAccessor) this.processingWorld).accessor$getAddedTileEntityList().add(added);
        } else {
            final Chunk chunk = this.processingWorld.getChunkAt(targetPos);
            if (!chunk.isEmpty()) {
                ((TrackedChunkBridge) chunk).bridge$setTileEntity(targetPos, added);
            }
            this.processingWorld.addTileEntity(added);
        }
    }

    public List<TileEntity> getQueuedTiles(final BlockPos pos) {
        return this.queuedTiles.get(pos);
    }

    public boolean isTileQueued(final BlockPos pos, final TileEntity tileEntity) {
        return this.queuedTiles.containsEntry(pos, tileEntity);
    }

    public boolean isTileQueuedForRemoval(final BlockPos pos, final TileEntity tileEntity) {
        return this.queuedRemovals.containsEntry(pos, tileEntity);
    }

    void queueTileAddition(final BlockPos pos, final TileEntity added) {
        // We want to provide the "added tile entity" to the proxy so any requests for this
        // new tile entity will succeed in returning the appropriate one.
        this.affectedTileEntities.put(pos, added);
        // Also, remove the position from being marked as removed.
        this.markedRemoved.remove(pos);
        if (added != null && added.getWorld() != this.processingWorld) {
            added.setWorld(this.processingWorld);
        }
        this.queuedTiles.put(pos, added);
    }

    void unQueueTileAddition(final BlockPos pos, final TileEntity added) {
        final TileEntity remove = this.affectedTileEntities.remove(pos);
        if (remove != added) {
            this.affectedTileEntities.put(pos, remove);
        }
        this.queuedTiles.remove(pos, added);
    }

    void queueRemoval(final TileEntity removed) {
        if (removed != null) {
            // Set the tile entity to the affected tile entities so it is retrieved
            // by the hooks in WorldServerMixin for getting tiles for removal.
            final BlockPos pos = removed.getPos();
            this.affectedTileEntities.put(pos, null);
            this.markRemovedTile(pos);
            if (!this.queuedRemovals.containsEntry(pos, removed)) {
                this.queuedRemovals.put(pos, removed);
            }
        }
    }

    void queueReplacement(final TileEntity added, final TileEntity removed) {
        // Go ahead and remove the "removed" tile entity, it will be invalidated
        // later. Likewise, it will be removed from the world's ticking list
        // later, once processed. The goal here is that the "existing" tile entity
        // retrieved by the target world will return the new added tile entity
        // without it actually being added yet to the world/chunk. Likewise, it will
        // not be removed from the world/chunk until the BlockTransaction is processed.
        final TileEntity existing = this.affectedTileEntities.put(removed.getPos(), added);
        this.markedRemoved.remove(removed.getPos());
        if (existing != null && existing != removed) {
            // Someone went and changed? Maybe it's already removed?
            this.queuedRemovals.put(existing.getPos(), existing);
        }
        this.queuedTiles.put(added.getPos(), added);
    }

    public boolean succeededInAdding(final BlockPos pos, final TileEntity tileEntity) {
        final TileEntity removed = this.affectedTileEntities.remove(pos);
        if (removed != null && removed != tileEntity) {
            System.err.println("Removed a tile entity that wasn't expected to be removed: " + removed);
            return false;
        }
        return true;
    }

    void pushTile(final BlockPos pos, final TileEntity tile) {
        this.affectedTileEntities.put(pos, tile);
        if (tile == null) {
            this.markRemovedTile(pos);
        } else {
            this.unmarkRemoval(pos);
        }
    }

    private void markRemovedTile(final BlockPos pos) {
        final boolean added = this.markedRemoved.add(pos);
        if (added) {
            // We want the tile entity to be null at the position, without being able to retrieve it
            // because if there's a queued tile being added, well, then it's marked for addition later,
            // but we do not want to be showing that tile entity if there's supposed to be an "empty"
            // or "null" tile entity at the processing time.
            this.affectedTileEntities.put(pos, null);
        }
        if (!this.proxies.isEmpty()) {
            final Proxy proxy = this.proxies.peek();
            if (!proxy.isMarkedForRemoval(pos)) {
                proxy.storeMarkedRemoval(pos);
            }
        }
    }

    public TrackedWorldBridge getWorld() {
        return (TrackedWorldBridge) this.processingWorld;
    }

    public void addToPrinter(final PrettyPrinter printer) {
        printer.add(" BlockStates");
        this.processed.forEach((pos, state) -> printer.add("  %s : %s", pos, state));
        printer.add()
            .add(" MarkedRemoved");
        this.markedRemoved.forEach(pos -> printer.add("  - %s", pos));
        printer.add()
            .add(" Affected Tiles");
        this.affectedTileEntities.forEach((pos, tileEntity) -> printer.add("  - %s : %s", pos, tileEntity == null ? "null" : ((TileEntityBridge) tileEntity).bridge$getPrettyPrinterString()));
        printer.add()
            .add(" QueuedTiles");
        this.queuedTiles.forEach((pos, tileEntity) -> printer.add("  - %s : %s", pos, tileEntity == null ? "null" : ((TileEntityBridge) tileEntity).bridge$getPrettyPrinterString()));
        printer.add().add(" QueuedRemovals");
        this.queuedRemovals.forEach(((pos, tileEntity) -> printer.add("  - %s: %s", pos, tileEntity == null ? "null" :  ((TileEntityBridge) tileEntity).bridge$getPrettyPrinterString())));
    }

    @Override
    public void close() {
        if (this.processingStack == null) {
            this.processingTransaction = null;
            return;
        }
        final BlockTransaction peek = this.processingStack.peek();
        if (this.processingTransaction != peek) {
            // error... pop them all?
            return;
        }
        this.processingStack.pop();
        if (!this.processingStack.isEmpty()) {
            this.processingTransaction = this.processingStack.peek();
            if (this.processingTransaction instanceof BlockTransaction.NeighborNotification) {
                this.isNeighbor = true;
            }
        }
        this.processingTransaction = null;
        this.isNeighbor = false;
    }

    public SpongeProxyBlockAccess switchTo(final BlockTransaction transaction) {
        if (this.processingTransaction != null) {
            if (this.processingStack == null) {
                this.processingStack = new ArrayDeque<>();
            }
            this.processingStack.push(this.processingTransaction);
            // Basically will push the previous and new one, so we can pop the old one to verify we're popping the right one.
            this.processingStack.push(transaction);
        }
        this.processingTransaction = transaction;
        if (transaction instanceof BlockTransaction.NeighborNotification) {
            this.isNeighbor = true;
        }
        return this;
    }

    public boolean isProcessingNeighbors() {
        return this.isNeighbor;
    }

    public boolean hasProxy() {
        return !this.proxies.isEmpty();
    }

    public TileEntity getQueuedTileForRemoval(final BlockPos pos) {
        if (this.queuedRemovals.isEmpty()) {
            return null;
        }
        final List<TileEntity> tiles = this.queuedRemovals.get(pos);
        if (tiles.isEmpty()) {
            return null;
        }
        // We always want to return the first tile that was queued, because when it's actually processed
        // for removal, it'll push the next queued removed tile entity forward.
        return tiles.get(0);
    }

    public boolean isProcessingTransactionWithNextHavingBreak(final BlockPos pos, final BlockState state) {
        if (this.processingTransaction == null) {
            return false;
        }
        for (BlockTransaction transaction = this.processingTransaction; transaction != null;) {
            if (transaction.next == null) {
                return false;
            }
            if (transaction.next.affectedPosition.equals(pos) && transaction.next instanceof BlockTransaction.ChangeBlock) {
                final BlockTransaction.ChangeBlock change = (BlockTransaction.ChangeBlock) transaction.next;
                if (change.queueBreak && change.original.getState() == state && change.original.blockChange == BlockChange.BREAK) {
                    return true;
                }
            }
            transaction = transaction.next;
        }
        return false;
    }

    public static final class Proxy implements AutoCloseable {

        private final SpongeProxyBlockAccess proxyAccess;
        @Nullable Exception stack_debug;
        @Nullable private LinkedHashMap<BlockPos, BlockState> processed;
        @Nullable private Set<BlockPos> newBlocks;
        @Nullable private Set<BlockPos> markedRemovedTiles;
        @Nullable private LinkedHashMap<BlockPos, TileEntity> removedTiles;

        Proxy(final SpongeProxyBlockAccess spongeProxyBlockAccess) {
            this.proxyAccess = spongeProxyBlockAccess;
        }

        @Override
        public void close() {
            this.proxyAccess.popProxy(this);
        }


        boolean hasNew() {
            return this.newBlocks != null && !this.newBlocks.isEmpty();
        }

        boolean hasStored() {
            return this.processed != null && !this.processed.isEmpty();
        }

        void markNew(final BlockPos pos) {
            if (this.newBlocks == null) {
                this.newBlocks = new HashSet<>();
            }
            this.newBlocks.add(pos);
        }

        boolean isNew(final BlockPos pos) {
            return this.newBlocks != null && this.newBlocks.contains(pos);
        }

        boolean isStored(final BlockPos pos) {
            return this.processed != null && this.processed.containsKey(pos);
        }


        void store(final BlockPos pos, final BlockState state) {
            if (this.processed == null) {
                this.processed = new LinkedHashMap<>();
            }
            this.processed.put(pos, state);
        }

        boolean isMarkedForRemoval(final BlockPos pos) {
            return this.markedRemovedTiles != null && this.markedRemovedTiles.contains(pos);
        }

        public boolean isStoredRemoval(final BlockPos pos) {
            return this.removedTiles != null && this.removedTiles.containsKey(pos);
        }

        void storeMarkedRemoval(final BlockPos pos) {
            if (this.markedRemovedTiles == null) {
                this.markedRemovedTiles = new HashSet<>();
            }
            this.markedRemovedTiles.add(pos);
        }

        boolean hasRemovals() {
            return this.markedRemovedTiles != null && !this.markedRemovedTiles.isEmpty();
        }

        void unmarkRemoval(final BlockPos pos) {
            this.markedRemovedTiles.remove(pos);
        }
    }

}
