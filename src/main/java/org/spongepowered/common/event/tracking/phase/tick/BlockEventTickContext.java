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

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class BlockEventTickContext extends TickContext<BlockEventTickContext> {

    @Nullable private List<EventTransaction> transactions;
    @Nullable private EventTransaction pendingNeig

    BlockEventTickContext() {
        super(TickPhase.Tick.BLOCK_EVENT);
    }

    /*
    Ok, so basically put: BlockEvents can cause all sorts of havoc, especially on clients if they're not cancelled properly.

    In the case of pistons:
    When a piston extends, it's performed during a BlockEvent transaction. The issue lies when the
    piston logic is being done, the following happens:
    all blocks being destroyed are destroyed with a notice for clients not to re-render the block position
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

    void captureNeighborNotification(IMixinWorldServer mixinWorldServer, BlockPos notifyPos, Block sourceBlock, BlockPos sourcePos) {
        final EventTransaction transaction = new EventTransaction();
        final NeighborNotification notification = new NeighborNotification();
        notification.worldServer = mixinWorldServer;
        notification.source = sourcePos;
        notification.sourceBlock = sourceBlock;
        notification.notification = notifyPos;
        transaction.notification = notification;
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }

    boolean hasNeighborNotifications() {

    }

    void logBlockChange(SpongeBlockSnapshot originalBlockSnapshot) {
        final EventTransaction transaction = new EventTransaction();
        transaction.changedSnapshot = originalBlockSnapshot;
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }


    static class EventTransaction {

        @Nullable private SpongeBlockSnapshot changedSnapshot;
        @Nullable private TileEntity removedTileEntity;
        @Nullable private TileEntity addedTileEntity;
        @Nullable private NeighborNotification notification;
    }

    static class NeighborNotification {
        IMixinWorldServer worldServer;
        BlockPos source;
        Block sourceBlock;
        BlockPos notification;
    }
}
