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

import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.world.BlockChange;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

class FluidTickPhaseState extends LocationBasedTickPhaseState<FluidTickContext> {

    private final String desc;

    FluidTickPhaseState(final String name) {
        this.desc = TrackingUtil.phaseStateToString("Tick", name, this);
    }

    @Override
    public FluidTickContext createNewContext(final PhaseTracker tracker) {
        return new FluidTickContext(this, tracker);
    }

    @Override
    public boolean shouldProvideModifiers(final FluidTickContext phaseContext) {
        return phaseContext.providesModifier;
    }

    @Override
    public BlockChange associateBlockChangeWithSnapshot(
        final FluidTickContext phaseContext,
        final BlockState newState,
        final BlockState currentState
    ) {
        final Block newBlock = newState.getBlock();
        if (phaseContext.tickingBlock.getType() instanceof FlowingFluid) {
            if (newBlock == Blocks.AIR) {
                return BlockChange.BREAK;
            }
            if (currentState.getBlock() instanceof LiquidBlock) {
                if (newBlock instanceof LiquidBlock) {
                    return BlockChange.MODIFY;
                } else if (newState.isAir()) {
                    return BlockChange.DECAY;
                } else {
                    return BlockChange.PLACE;
                }
            }

            if (currentState.isAir() && newBlock instanceof LiquidBlock) {
                return BlockChange.PLACE;
            }
        }
        return super.associateBlockChangeWithSnapshot(phaseContext, newState, currentState);
    }

    @Override
    public Operation getBlockOperation(
        final FluidTickContext phaseContext,
        final SpongeBlockSnapshot original, final SpongeBlockSnapshot result
    ) {
        final FluidState fluidState = original.state().fluidState();
        if (!fluidState.isEmpty() && result.blockChange == BlockChange.DECAY) {
            return Operations.LIQUID_DECAY.get();
        }
        if (fluidState.isEmpty() && result.blockChange == BlockChange.PLACE) {
            return Operations.LIQUID_SPREAD.get();
        }
        if (!fluidState.isEmpty() && result.blockChange == BlockChange.MODIFY) {
            return Operations.LIQUID_SPREAD.get();
        }
        return super.getBlockOperation(phaseContext, original, result);
    }

    @Override
    public String toString() {
        return this.desc;
    }

}
