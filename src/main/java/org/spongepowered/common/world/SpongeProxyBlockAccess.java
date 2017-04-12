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
package org.spongepowered.common.world;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.util.VecHelper;

import java.util.LinkedHashMap;
import java.util.List;

public final class SpongeProxyBlockAccess implements IBlockAccess {

    private final List<Transaction<BlockSnapshot>> transactions;
    private final List<BlockPos> poses;
    private final LinkedHashMap<BlockPos, IBlockState> processed = new LinkedHashMap<>();
    private int index;
    private WorldServer processingWorld;

    public SpongeProxyBlockAccess(List<Transaction<BlockSnapshot>> snapshotTransaction) {
        this.transactions = snapshotTransaction;
        this.poses = this.transactions.stream()
            .map(transaction -> ((IMixinLocation) (Object) transaction.getOriginal().getLocation().get()).getBlockPos())
            .collect(ImmutableList.toImmutableList());
        this.index = 0;
        this.processingWorld = ((WorldServer) snapshotTransaction.get(0).getOriginal().getLocation().get().getExtent());
    }

    public void proceed() {
        this.processed.put(this.poses.get(this.index), ((IBlockState) this.transactions.get(this.index).getFinal().getState()));
        this.index++;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return this.processingWorld != null ? this.processingWorld.getTileEntity(pos) : null;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (this.processed.containsKey(pos)) { // first just check if there's already a pos list built.
            return this.processed.get(pos);
        }
        Transaction<BlockSnapshot> unknown = this.transactions.get(this.index);
        if (unknown != null) {
            final BlockPos actualPos = this.poses.get(this.index);
            if (pos.equals(actualPos)) {
                return (IBlockState) unknown.getFinal().getState();
            }
        }

        return this.processingWorld.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return this.processingWorld.isAirBlock(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return this.processingWorld.getStrongPower(pos, direction);
    }
}
