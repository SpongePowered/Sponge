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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;

public class FluidTickContext extends LocationBasedTickContext<FluidTickContext> {

    FluidState tickingBlock;
    boolean providesModifier;
    World<?, ?> world;

    protected FluidTickContext(final IPhaseState<FluidTickContext> phaseState, final PhaseTracker tracker) {
        super(phaseState, tracker);
    }

    @Override
    public FluidTickContext source(final Object owner) {
        super.source(owner);
        if (owner instanceof LocatableBlock) {
            final LocatableBlock locatableBlock = (LocatableBlock) owner;
            final Block block = ((BlockState) locatableBlock.blockState()).getBlock();
            this.providesModifier = !(block instanceof  LiquidBlock);
            this.world = locatableBlock.world();
            if (block instanceof TrackableBridge) {
                final TrackableBridge trackable = (TrackableBridge) block;
                this.setBlockEvents(trackable.bridge$allowsBlockEventCreation())
                    .setBulkBlockCaptures(trackable.bridge$allowsBlockBulkCaptures())
                    .setEntitySpawnEvents(trackable.bridge$allowsEntityEventCreation())
                    .setBulkEntityCaptures(trackable.bridge$allowsEntityBulkCaptures());
            }
        }
        return this;
    }

    public FluidTickContext fluid(final FluidState fluidState) {
        this.tickingBlock = fluidState;
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
