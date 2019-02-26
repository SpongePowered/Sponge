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
package org.spongepowered.common.event.tracking.phase.tick;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.BlockChange;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import javax.annotation.Nullable;

public class EnhancedBlockTrackingContext<T extends EnhancedBlockTrackingContext<T>> extends LocationBasedTickContext<T> {
    @Nullable private LinkedList<EventTransaction> transactions;
    @Nullable private Set<BlockPos> usedPositions;
    @Nullable private EventTransaction pendingNeighborPosition;

    protected EnhancedBlockTrackingContext(IPhaseState<T> phaseState) {
        super(phaseState);
    }

    /*    Ok, so basically put: BlockEvents can cause all sorts of havoc, especially on clients if they're not cancelled properly.

    In the case of pistons:
    When a piston extends, it's performed during a BlockEvent transaction. The issue lies when the
    piston logic is being done, the following happens:
    all blocks being destroyed are destroyed with a notice for clients not to re-render the block position, no neighbor notifications
    all blocks being moved are replaced with air and a notification to neighbors (should have 6)
    all blocks being moved are then replaced with a new tile entity and telling clients not to render the block change
    The piston block and it's extension are then set, with a new tile entity for the piston extension position
    All blocks destroyed have a notification sent out to neighbors in reverse order
    all blocks moved have a notification sent out to neighbors in reverse order
    the piston head (extension) has a neighbor notification sent out

    The same is the case for pistons retracting (except no piston head creation) To save time and expenses, pistons have their
    block states cached in the order of which the blocks have been modified and played back in the reverse order in which they
    were modified.

    So, with that case explained: The context needs to be able to track all of these interactions,
    throwing events for all the block changes after captures, and especially to suppress the neighbor notifications
    and replay them in the order in which they were received. The added complexity is that tile entities are also being set and removed in this time.
    If all of the events are cancelled, all the tile entities would need to be unloaded, invalidated, and removed before they are sent to the clients.
     */

    void captureNeighborNotification(IMixinWorldServer mixinWorldServer, BlockPos notifyPos, IBlockState iblockstate,
        Block sourceBlock, BlockPos sourcePos) {
        final EventTransaction transaction = new EventTransaction(null);
        final NeighborNotification
            notification = new NeighborNotification(mixinWorldServer, iblockstate, notifyPos, sourceBlock, sourcePos);
        // Finally, set the notification to the transaction
        transaction.notification = notification;
        if (this.transactions == null) {
            this.transactions = new LinkedList<>();
        }
        this.transactions.add(transaction);
    }

    void logBlockChange(SpongeBlockSnapshot originalBlockSnapshot, BlockPos pos, @Nullable TileEntity tileEntity) {
        final EventTransaction
            transaction = new EventTransaction(tileEntity != null ? BlockChange.BREAK : null);
        transaction.changedSnapshot = originalBlockSnapshot;
        if (tileEntity != null && ((IMixinTileEntity) tileEntity).isCaptured()) {
            transaction.removedTileEntity = tileEntity; // The tile entity is going to be removed by the transaction if successfully handled
        }
        if (this.transactions == null) {
            this.transactions = new LinkedList<>();
        }
        this.transactions.add(transaction);
    }

    void logTileChange(IMixinWorldServer mixinWorldServer, BlockPos pos, @Nullable TileEntity currenTile, @Nullable TileEntity tileEntity) {
        final WorldServer world = (WorldServer) mixinWorldServer;
        final IBlockState current = world.getBlockState(pos);

        final SpongeBlockSnapshot snapshot = mixinWorldServer.createSpongeSnapshotForTileEntity(current, pos, BlockChangeFlags.NONE, currenTile);
        TrackingUtil.associateBlockChangeWithSnapshot(TickPhase.Tick.BLOCK_EVENT, current.getBlock(), current, snapshot);
        final BlockChange flag = currenTile != null
                                 ? tileEntity != null
                                   ? BlockChange.MODIFY // If both new and old are not null, it's a modification
                                   : BlockChange.BREAK  // If new is null and old is not, we're breaking the tile entity
                                 : tileEntity != null
                                   ? BlockChange.PLACE // If the replacing tile entity si not null AND the original is null, well, we're placing
                                   : null; // If both are null (why are we even here?) it's null.
        final EventTransaction transaction = new EventTransaction(flag);
        transaction.removedTileEntity = currenTile;
        transaction.addedTileEntity = tileEntity;
        if (this.transactions == null) {
            this.transactions = new LinkedList<>();
        }
        this.transactions.add(transaction);
        getCapturedBlockSupplier().put(snapshot, current);
    }

    public void processTransactionsUpTo(SpongeBlockSnapshot oldBlockSnapshot, Transaction<BlockSnapshot> transaction,
        IBlockState newState, int currentDepth) {
        if (this.transactions == null || this.transactions.isEmpty()) {
            return;
        }
        boolean hasBeenFound = false;
        for (Iterator<EventTransaction> iterator = this.transactions.iterator(); iterator.hasNext();) {
            final EventTransaction next = iterator.next();
            iterator.remove();
            if (next.changedSnapshot == oldBlockSnapshot) {
                hasBeenFound = true;
            }
            final NeighborNotification notification = next.notification;
            if (notification != null) {
                final IMixinWorldServer worldServer = notification.worldServer;
                final IBlockState sourceState = notification.source;
                final BlockPos notifyPos = notification.notifyPos;
                final Block sourceBlock = notification.sourceBlock;
                final BlockPos sourcePos = notification.sourcePos;

                PhaseTracker.getInstance().performNeighborNotificationOnTarget(worldServer, notifyPos, sourceBlock, sourcePos, sourceState);
            }
            if (hasBeenFound) {
                break;
            }

        }
        if (!hasBeenFound) {
            // uh oh....
            System.err.println("Uhoh...");
        }
        if (this.pendingNeighborPosition == null) {
            this.pendingNeighborPosition = this.transactions.get(0);
        }
        if (this.pendingNeighborPosition.changedSnapshot == oldBlockSnapshot) {

        }
    }

    public void cancelTransaction(Transaction<BlockSnapshot> transaction, BlockSnapshot original) {
        if (this.transactions == null || this.transactions.isEmpty()) {
            return;
        }
        if (this.usedPositions == null) {
            this.usedPositions = new HashSet<>(this.transactions.size());
        }

        final BlockPos blockPos = ((SpongeBlockSnapshot) original).getBlockPos();

        final boolean checked = this.usedPositions.contains(blockPos);
        this.usedPositions.add(blockPos);
        final WorldServer worldServer = ((SpongeBlockSnapshot) original).getWorldServer();
        for (ListIterator<EventTransaction> iterator = this.transactions.listIterator(this.transactions.size() - 1); iterator.hasPrevious();) {
            final EventTransaction next = iterator.previous(); // We have to iterate in reverse order due to rollbacks
            if (next.changeFlag == BlockChange.BREAK) {
                final TileEntity tileToAdd = next.removedTileEntity;
                final BlockPos pos = tileToAdd.getPos();
                if (blockPos.equals(pos) && !checked) {
                    recreateTileEntity(worldServer, tileToAdd, pos);
                }
            } else if (next.changeFlag == BlockChange.PLACE) {
                final TileEntity tileToRemove = next.addedTileEntity;
                // Just set the tile entity to null.
                final BlockPos pos = tileToRemove.getPos();
                if (blockPos.equals(pos) && !checked) {
                    worldServer.removeTileEntity(pos);
                }
            } else if (next.changeFlag == BlockChange.MODIFY) {
                final TileEntity tileToReplace = next.removedTileEntity;
                final BlockPos pos = tileToReplace.getPos();
                if (blockPos.equals(pos) && !checked) {
                    worldServer.removeTileEntity(pos);
                    recreateTileEntity(worldServer, tileToReplace, pos);
                }
            }
            if (next.changedSnapshot != null) {
                if (next.changedSnapshot.getBlockPos().equals(blockPos)) {
                    next.changedSnapshot.restore(true, BlockChangeFlags.NONE);
                }
            }
        }
    }

    private void recreateTileEntity(WorldServer worldServer, TileEntity tileToReplace, BlockPos pos) {
        tileToReplace.validate(); // Need to mark the tile entity valid before it's being set back onto the world
        final NBTTagCompound compound = new NBTTagCompound();
        tileToReplace.writeToNBT(compound);
        final TileEntity tileEntity = TileEntity.create(worldServer, compound);
        if (tileEntity != null) {
            worldServer.getChunk(pos).addTileEntity(tileEntity);
        }
    }


    static class EventTransaction {

        @Nullable SpongeBlockSnapshot changedSnapshot;
        @Nullable TileEntity removedTileEntity;
        @Nullable TileEntity addedTileEntity;
        @Nullable NeighborNotification notification;
        @Nullable final BlockChange changeFlag;

        EventTransaction(@Nullable BlockChange changeFlag) {
            this.changeFlag = changeFlag;
        }


        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("changedSnapshot", changedSnapshot)
                .add("removedTileEntity", removedTileEntity)
                .add("addedTileEntity", addedTileEntity)
                .add("notification", notification)
                .add("changeFlag", changeFlag)
                .toString();
        }
    }

    static class NeighborNotification {
        final IMixinWorldServer worldServer;
        final IBlockState source;
        final BlockPos notifyPos;
        final Block sourceBlock;
        final BlockPos sourcePos;

        public NeighborNotification(IMixinWorldServer worldServer, IBlockState source, BlockPos notifyPos, Block sourceBlock,
            BlockPos sourcePos) {
            this.worldServer = worldServer;
            this.source = source;
            this.notifyPos = notifyPos;
            this.sourceBlock = sourceBlock;
            this.sourcePos = sourcePos;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("worldServer", ((World) worldServer).getProperties().getWorldName())
                .add("source", source)
                .add("notifyPos", notifyPos)
                .add("sourceBlock", sourceBlock)
                .add("sourcePos", sourcePos)
                .toString();
        }
    }

}
