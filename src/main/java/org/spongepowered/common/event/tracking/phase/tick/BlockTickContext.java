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
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockState;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.event.tracking.IPhaseState;

public class BlockTickContext extends LocationBasedTickContext<BlockTickContext> {

    BlockBridge tickingBlock;
    boolean providesModifier;
    World world;

    protected BlockTickContext(IPhaseState<BlockTickContext> phaseState) {
        super(phaseState);
    }

    @Override
    public BlockTickContext source(Object owner) {
        super.source(owner);
        if (owner instanceof LocatableBlock) {
            final LocatableBlock locatableBlock = (LocatableBlock) owner;
            final Block block = ((BlockState) locatableBlock.getBlockState()).getBlock();
            this.tickingBlock = (BlockBridge) block;
            this.providesModifier = !(block instanceof BlockDynamicLiquid);
            this.world = locatableBlock.getWorld();
            if (block instanceof TrackableBridge) {
                final TrackableBridge trackable = (TrackableBridge) block;
                this.setBlockEvents(trackable.bridge$allowsBlockEventCreation())
                    .setBulkBlockCaptures(trackable.bridge$allowsBlockBulkCapture())
                    .setEntitySpawnEvents(trackable.bridge$allowsEntityEventCreation())
                    .setBulkEntityCaptures(trackable.bridge$allowsEntityBulkCapture());
            }
        }
        return this;
    }

    @Override
    protected void reset() {
        super.reset();
        this.tickingBlock = null;
        this.providesModifier = true;
        this.world = null;
    }
}
