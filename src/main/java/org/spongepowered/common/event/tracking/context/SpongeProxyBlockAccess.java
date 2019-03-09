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
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public final class SpongeProxyBlockAccess implements IBlockAccess {
    private static final boolean DEBUG_PROXY = Boolean.valueOf(System.getProperty("sponge.debugProxyChanges", "false"));

    private final LinkedHashMap<BlockPos, IBlockState> processed = new LinkedHashMap<>();
    private final LinkedHashMap<BlockPos, TileEntity> processedTiles = new LinkedHashMap<>();
    private final ListMultimap<BlockPos, TileEntity> queuedTiles = LinkedListMultimap.create();
    private final Set<BlockPos> markedRemoved = new HashSet<>();
    private final Deque<Proxy> proxies = Queues.newArrayDeque();
    private WorldServer processingWorld;

    public SpongeProxyBlockAccess(IMixinWorldServer worldServer) {
        this.processingWorld = ((WorldServer) worldServer);
    }

    public Proxy pushProxy() {
        final Proxy proxy = new Proxy(this);
        this.proxies.push(proxy);
        if (DEBUG_PROXY) {
            proxy.stack_debug = new Exception();
        }
        return proxy;
    }

    SpongeProxyBlockAccess proceed(BlockPos pos, IBlockState state) {
        IBlockState existing = this.processed.put(pos, state);

        if (!this.proxies.isEmpty()) {
            final Proxy proxy = this.proxies.peek();
            if (existing == null) {
                proxy.markNew(pos);
            } else if (!proxy.isNew(pos) && !proxy.isStored(pos)) {
                proxy.store(pos, state);
            }
            if (proxy.toBeRemoved != null) {
                this.processedTiles.remove(proxy.tileEntityChange);
            }
            if (proxy.toBeAdded != null) {
                this.processedTiles.put(proxy.tileEntityChange, proxy.toBeAdded);
            }
        }
        return this;
    }

    private void popProxy(Proxy oldProxy) {
        if (oldProxy == null) {
            throw new IllegalArgumentException("Cannot pop a null proxy!");
        }
        Proxy proxy = this.proxies.peek();
        if (proxy != oldProxy) {
            int offset = -1;
            int i = 0;
            for (Proxy f : this.proxies) {
                if (f == oldProxy) {
                    offset = i;
                    break;
                }
                i++;
            }
            if (!DEBUG_PROXY && offset == -1) {
                // if we're not debugging the cause proxies then throw an error
                // immediately otherwise let the pretty printer output the proxy
                // that was erroneously popped.
                throw new IllegalStateException("Block Change Proxy corruption! Attempted to pop a proxy that was not on the stack.");
            }
            final PrettyPrinter printer = new PrettyPrinter(100).add("Block Change Proxy Corruption!").centre().hr()
                .add("Found %n proxies left on the stack. Clearing them all.", new Object[]{offset + 1});
            if (!DEBUG_PROXY) {
                printer.add()
                    .add("Please add -Dsponge.debugProxyChanges=true to your startup flags to enable further debugging output.");
                SpongeImpl.getLogger().warn("  Add -Dsponge.debugProxyChanges to your startup flags to enable further debugging output.");
            } else {
                printer.add()
                    .add("Attempting to pop proxy:")
                    .add(proxy.stack_debug)
                    .add()
                    .add("Frames being popped are:")
                    .add(oldProxy.stack_debug);
            }
            while (offset >= 0) {
                Proxy f = this.proxies.peek();
                if (DEBUG_PROXY && offset > 0) {
                    printer.add("   Stack proxy in position %n :", offset);
                    printer.add(f.stack_debug);
                }
                popProxy(f);
                offset--;
            }
            printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
            if (offset == -1) {
                // Popping a proxy that was not on the stack is not recoverable
                // so we throw an exception.
                throw new IllegalStateException("Cause Stack Proxy Corruption! Attempted to pop a proxy that was not on the stack.");
            }
            return;
        }
        this.proxies.pop();
        if (proxy.hasNew()) {
            for (BlockPos pos : proxy.newBlocks) {
                this.processed.remove(pos);
                unmarkRemoval(pos);
            }
        }
        if (proxy.hasStored()) {
            for (Map.Entry<BlockPos, IBlockState> entry : proxy.processed.entrySet()) {
                this.processed.put(entry.getKey(), entry.getValue());
            }
        }
        if (proxy.tileEntityChange != null) {
            if (proxy.toBeRemoved != null) {
                this.processedTiles.remove(proxy.tileEntityChange);
                if (proxy.toBeAdded == null) {
                    this.markedRemoved.add(proxy.tileEntityChange);
                }
            }
            if (proxy.toBeAdded != null) {
                this.queuedTiles.remove(proxy.tileEntityChange, proxy.toBeAdded);
                this.processedTiles.put(proxy.tileEntityChange, proxy.toBeAdded);
            }
        }


    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return this.processedTiles.get(pos);
    }

    public boolean hasTileEntity(BlockPos pos) {
        return this.processedTiles.containsKey(pos);
    }

    public boolean hasTileEntity(BlockPos pos, TileEntity tileEntity) {
        return this.processedTiles.get(pos) == tileEntity;
    }

    public boolean isTileEntityRemoved(BlockPos pos) {
        return this.markedRemoved.contains(pos);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return this.processed.get(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return this.processingWorld.isAirBlock(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return this.processingWorld.getStrongPower(pos, direction);
    }

    public void onChunkChanged(BlockPos pos) {
        final IBlockState existing = this.processed.remove(pos);
        if (existing != null && !this.proxies.isEmpty()) {
            final Proxy peek = this.proxies.peek();
            if (!peek.isNew(pos)) {
                peek.store(pos, existing);
            }
        }
    }

    private void unmarkRemoval(BlockPos pos) {
        final boolean removed = this.markedRemoved.remove(pos);
        if (removed && !this.proxies.isEmpty()) {
            final Proxy peek = this.proxies.peek();
            if (peek.toBeRemoved != null && pos.equals(peek.toBeRemoved)) {
                peek.toBeRemoved = null;
            }
        }
    }

    private void storeRemovedTile(BlockPos pos, TileEntity tileEntity) {
        if (tileEntity != null && !this.proxies.isEmpty()) {
            final Proxy peek = this.proxies.peek();
            if (peek.toBeRemoved == tileEntity) {
                peek.toBeRemoved = null;
                peek.tileEntityChange = null;
            } else {
                peek.toBeRemoved = tileEntity;
                peek.tileEntityChange = pos;
            }
        }
    }

    public void onTileReplace(BlockPos pos, TileEntity replacing) {
        unmarkRemoval(pos);
        if (this.queuedTiles.containsEntry(pos, replacing)) {
            proceedWithAdd(pos, replacing);
            return;
        }
        final TileEntity removedTile = this.processedTiles.remove(pos);
        storeRemovedTile(pos, removedTile);
    }

    void proceedWithRemoval(BlockPos targetPosition, TileEntity removed) {
        final TileEntity existing = this.processedTiles.remove(targetPosition);
        this.markedRemoved.add(targetPosition);
        storeRemovedTile(targetPosition, existing);
    }

    void proceedWithAdd(BlockPos targetPos, TileEntity added) {
        final boolean removed = this.queuedTiles.remove(targetPos, added);
        if (!removed) {
            // someone else popped for us?
            System.err.println("Unknown removal for: " + targetPos + " with tile entity: " + added);
        }
        unmarkRemoval(targetPos);
        final TileEntity existing = this.processedTiles.put(targetPos, added);
        if (existing != null && existing != added) {
            existing.invalidate();
        }
    }

    public List<TileEntity> getQueuedTiles(BlockPos pos) {
        return this.queuedTiles.get(pos);
    }

    public boolean isTileQueued(BlockPos pos, TileEntity tileEntity) {
        return this.queuedTiles.containsEntry(pos, tileEntity);
    }

    public void queueTileAddition(BlockPos pos, TileEntity added) {
        final boolean existing = this.queuedTiles.put(pos, added);
        if (!existing) {
            if (!this.proxies.isEmpty()) {
                final Proxy peek = this.proxies.peek();
                peek.tileEntityChange = pos;
                peek.toBeAdded = added;
            }
        }
    }

    public void queueRemoval(TileEntity removed) {
        if (!this.proxies.isEmpty()) {
            final Proxy peek = this.proxies.peek();
            if (peek.toBeRemoved == null) {
                peek.toBeRemoved = removed;
                peek.tileEntityChange = removed.getPos();
            }
        }
    }

    public void queueReplacement(TileEntity added, TileEntity removed) {
        final TileEntity existing = this.processedTiles.get(removed.getPos());
        if (existing != removed) {
            // ok, looks like the tile entity to be removed will be queued for placement later.
            final boolean exists = this.queuedTiles.put(removed.getPos(), removed);
            if (!exists && !this.proxies.isEmpty()) {
                final Proxy peek = this.proxies.peek();
                peek.toBeRemoved = removed;
                peek.toBeAdded = added;
                peek.tileEntityChange = removed.getPos();
            }
        } else {
            // Otherwise, it's just queued to be added/replaced later.
            this.queuedTiles.put(added.getPos(), added);
        }
    }

    public boolean succeededInAdding(BlockPos pos, TileEntity tileEntity) {
        final TileEntity removed = this.processedTiles.remove(pos);
        if (removed != tileEntity) {
            System.err.println("Removed a tile entity that wasn't expected to be removed: " + removed);
            return false;
        }
        return true;
    }

    public static final class Proxy implements AutoCloseable {

        private final SpongeProxyBlockAccess proxyAccess;
        @Nullable Exception stack_debug;
        @Nullable private LinkedHashMap<BlockPos, IBlockState> processed;
        @Nullable private Set<BlockPos> newBlocks;
        @Nullable BlockPos tileEntityChange;
        @Nullable TileEntity toBeRemoved;
        @Nullable TileEntity toBeAdded;

        Proxy(SpongeProxyBlockAccess spongeProxyBlockAccess) {
            proxyAccess = spongeProxyBlockAccess;
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

        void markNew(BlockPos pos) {
            if (this.newBlocks == null) {
                this.newBlocks = new HashSet<>();
            }
            this.newBlocks.add(pos);
        }

        boolean isNew(BlockPos pos) {
            return this.newBlocks != null && this.newBlocks.contains(pos);
        }

        boolean isStored(BlockPos pos) {
            return this.processed != null && this.processed.containsKey(pos);
        }


        void store(BlockPos pos, IBlockState state) {
            if (this.processed == null) {
                this.processed = new LinkedHashMap<>();
            }
            this.processed.put(pos, state);
        }

    }
}
