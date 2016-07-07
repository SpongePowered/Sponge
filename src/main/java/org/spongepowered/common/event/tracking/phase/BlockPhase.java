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
package org.spongepowered.common.event.tracking.phase;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.function.BlockFunction;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;

public final class BlockPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        BLOCK_DECAY,
        RESTORING_BLOCKS,
        DISPENSE,
        BLOCK_DROP_ITEMS,
        BLOCK_ADDED, BLOCK_BREAK;

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return false;
        }

        @Override
        public void assignEntityCreator(PhaseContext context, Entity entity) {
            context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Not processing over a block!", context));
        }

        @Override
        public BlockPhase getPhase() {
            return TrackingPhases.BLOCK;
        }

    }

    BlockPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return currentState != State.RESTORING_BLOCKS;
    }

    @Override
    public boolean allowEntitySpawns(IPhaseState currentState) {
        return currentState != State.RESTORING_BLOCKS;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.BLOCK_DECAY) {
            final BlockSnapshot blockSnapshot = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Could not find a decaying block snapshot!", phaseContext));
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> BlockFunction.Drops.DECAY_ITEMS.processItemSpawns(blockSnapshot, causeTracker, phaseContext, items));
            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> BlockFunction.Entities.DROP_ITEMS_RANDOM.processEntities(blockSnapshot, causeTracker, phaseContext, entities));
        } else if (state == State.BLOCK_DROP_ITEMS) {
            final BlockSnapshot blockSnapshot = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Could not find a block dropping items!", phaseContext));
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> BlockFunction.Drops.DROP_ITEMS.processItemSpawns(blockSnapshot, causeTracker, phaseContext, items));
            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> BlockFunction.Entities.DROP_ITEMS_RANDOM.processEntities(blockSnapshot, causeTracker, phaseContext, entities));
        } else if (state == State.RESTORING_BLOCKS) {
            // do nothing for now.
        } else if (state == State.DISPENSE) {
            final BlockSnapshot blockSnapshot = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Could not find a block dispensing items!", phaseContext));
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> BlockFunction.Drops.DISPENSE.processItemSpawns(blockSnapshot, causeTracker, phaseContext, items));
            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> BlockFunction.Entities.DROP_ITEMS_RANDOM.processEntities(blockSnapshot, causeTracker, phaseContext, entities));
        }

    }


    @Override
    public boolean isRestoring(IPhaseState state, PhaseContext phaseContext, int updateFlag) {
        return state == State.RESTORING_BLOCKS && (updateFlag & 1) == 0;
    }
}
