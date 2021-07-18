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

import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.world.BlockChange;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

class BlockTickPhaseState extends LocationBasedTickPhaseState<BlockTickContext> {

    private final String desc;

    BlockTickPhaseState(final String name) {
        this.desc = TrackingUtil.phaseStateToString("Tick", name, this);
    }

    @Override
    public BlockTickContext createNewContext(final PhaseTracker tracker) {
        return new BlockTickContext(this, tracker);
    }

    @Override
    public boolean shouldProvideModifiers(final BlockTickContext phaseContext) {
        return phaseContext.providesModifier;
    }

    @Override
    public BlockChange associateBlockChangeWithSnapshot(
        final BlockTickContext phaseContext, final BlockState newState,
        final BlockState currentState
    ) {
        final Block newBlock = newState.getBlock();
        if (phaseContext.tickingBlock instanceof BonemealableBlock) {
            if (newBlock == Blocks.AIR) {
                return BlockChange.BREAK;
            }
            if (newBlock instanceof BonemealableBlock || newState.getMaterial().isFlammable()) {
                return BlockChange.GROW;
            }
        }
        return super.associateBlockChangeWithSnapshot(phaseContext, newState, currentState);
    }

    @Override
    public String toString() {
        return this.desc;
    }

}
