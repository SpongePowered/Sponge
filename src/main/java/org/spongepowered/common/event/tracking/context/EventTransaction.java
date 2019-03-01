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

import com.google.common.base.MoreObjects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.world.BlockChange;

import javax.annotation.Nullable;

final class EventTransaction {

    @Nullable BlockSnapshot notificationSnapshot;
    @Nullable SpongeBlockSnapshot changedSnapshot;
    @Nullable TileEntity removedTileEntity;
    @Nullable TileEntity addedTileEntity;
    @Nullable NeighborNotification notification;
    @Nullable final BlockChange changeFlag;
    boolean isCancelled = false;
    final int transactionIndex;
    final int snapshotIndex;

    EventTransaction(int i, int snapshotIndex, SpongeBlockSnapshot attachedSnapshot, @Nullable BlockChange changeFlag) {
        this.changeFlag = changeFlag;
        this.transactionIndex = i;
        this.snapshotIndex = snapshotIndex;
        this.changedSnapshot = attachedSnapshot;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("changedSnapshot", this.changedSnapshot)
            .add("removedTileEntity", this.removedTileEntity)
            .add("addedTileEntity", this.addedTileEntity)
            .add("notification", this.notification)
            .add("changeFlag", this.changeFlag)
            .toString();
    }

    void cancel(WorldServer worldServer, BlockPos blockPos) {
        if (this.changeFlag == BlockChange.BREAK || this.changeFlag == BlockChange.MODIFY) {
            final TileEntity tileToAdd = this.removedTileEntity;
            final BlockPos pos = tileToAdd.getPos();
            if (blockPos.equals(pos)) {
                this.isCancelled = true; // All we need to do is allow the restore to have taken place.
            }
        } else if (this.changeFlag == BlockChange.PLACE) {
            final TileEntity tileToRemove = this.addedTileEntity;
            // Just set the tile entity to null.
            final BlockPos pos = tileToRemove.getPos();
            if (blockPos.equals(pos)) {
                this.isCancelled = true;
            }
        }
        if (this.changedSnapshot != null && !this.isCancelled) {
            if (this.changedSnapshot.getBlockPos().equals(blockPos)) {
                this.isCancelled = true;
            }
        }
    }
}
