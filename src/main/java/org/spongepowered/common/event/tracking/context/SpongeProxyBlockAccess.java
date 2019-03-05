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

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.LinkedHashMap;

public final class SpongeProxyBlockAccess implements IBlockAccess {

    private final LinkedHashMap<BlockPos, IBlockState> processed = new LinkedHashMap<>();
    private int index;
    private WorldServer processingWorld;
    private BlockTransaction processingTransaction;

    public SpongeProxyBlockAccess(IMixinWorldServer worldServer) {
        this.index = 0;
        this.processingWorld = ((WorldServer) worldServer);
    }

    public void proceed(BlockTransaction transaction, BlockPos pos, IBlockState state) {
        this.processed.put(pos, state);
        this.processingTransaction = transaction;
        this.index++;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if (this.processingTransaction instanceof BlockTransaction.TileEntityAdd) {
            final TileEntity added = ((BlockTransaction.TileEntityAdd) this.processingTransaction).added;
            if (added.getPos().equals(pos)) {
                return added;
            }
        } else if (this.processingTransaction instanceof BlockTransaction.ReplaceTileEntity) {
            final TileEntity added = ((BlockTransaction.ReplaceTileEntity) this.processingTransaction).added;
            if (added.getPos().equals(pos)) {
                return added;
            }
        }
        return this.processingWorld != null ? this.processingWorld.getTileEntity(pos) : null;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (this.processed.containsKey(pos)) { // first just check if there's already a pos list built.
            return this.processed.get(pos);
        }
        if (this.processingTransaction instanceof BlockTransaction.ChangeBlock) {
            final BlockTransaction.ChangeBlock changeBlock = (BlockTransaction.ChangeBlock) this.processingTransaction;
            if (changeBlock.original.getBlockPos().equals(pos)) {
                return changeBlock.newState;
            }
        } else if (this.processingTransaction instanceof BlockTransaction.RemoveTileEntity) {
            final BlockTransaction.RemoveTileEntity removeTile = (BlockTransaction.RemoveTileEntity) this.processingTransaction;
            if (removeTile.removed.getPos().equals(pos)) {
                return removeTile.newState;
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
