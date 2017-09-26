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
package org.spongepowered.common.event.tracking.phase.general;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.UnwindingPhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;

import java.util.ArrayList;
import java.util.List;

final class PostState extends GeneralState<UnwindingPhaseContext> {

    @Override
    public UnwindingPhaseContext createPhaseContext() {
        return null;
    }

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return state.getPhase() == TrackingPhases.GENERATION
                || state.getPhase() == TrackingPhases.PLUGIN
                || state == BlockPhase.State.RESTORING_BLOCKS;
    }

    @Override
    public boolean tracksBlockRestores() {
        return false; // TODO - check that this really is needed.
    }

    @Override
    public boolean requiresPost() {
        return false;
    }

    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return true;
    }

    @Override
    public boolean ignoresScheduledUpdates() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(UnwindingPhaseContext context) {
        final IPhaseState unwindingState = context.getUnwindingState();
        final PhaseContext<?> unwindingContext = context.getUnwindingContext();
        unwindingState.unwind(unwindingContext);
        this.postDispatch(unwindingState, unwindingContext, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postDispatch(IPhaseState<?> unwindingState, PhaseContext<?> unwindingContext, UnwindingPhaseContext postContext) {
        final List<BlockSnapshot> contextBlocks = unwindingContext.getCapturedBlockSupplier().orEmptyList();
        final List<Entity> contextEntities = unwindingContext.getCapturedEntitySupplier().orEmptyList();
        final List<Entity> contextItems = (List<Entity>) (List<?>) unwindingContext.getCapturedItemsSupplier().orEmptyList();
        if (contextBlocks.isEmpty() && contextEntities.isEmpty() && contextItems.isEmpty()) {
            return;
        }
        if (!contextBlocks.isEmpty()) {
            final List<BlockSnapshot> blockSnapshots = new ArrayList<>(contextBlocks);
            contextBlocks.clear();
            GeneralPhase.processBlockTransactionListsPost(postContext, blockSnapshots, this, unwindingContext);
        }
        if (!contextEntities.isEmpty()) {
            final ArrayList<Entity> entities = new ArrayList<>(contextEntities);
            contextEntities.clear();
            ((IPhaseState) unwindingState).postProcessSpawns(unwindingContext, entities);
        }
        if (!contextItems.isEmpty()) {
            final ArrayList<Entity> items = new ArrayList<>(contextItems);
            contextItems.clear();
            TrackingUtil.splitAndSpawnEntities(items);
        }
    }

    public void appendContextPreExplosion(PhaseContext<?> phaseContext, PhaseData currentPhaseData) {
        final IPhaseState phaseState = ((UnwindingPhaseContext) currentPhaseData.context).getUnwindingState();
        final PhaseContext<?> unwinding = ((UnwindingPhaseContext) currentPhaseData.context).getUnwindingContext();
        final PhaseData phaseData = new PhaseData(unwinding, phaseState);
        phaseState.getPhase().appendContextPreExplosion(phaseContext, phaseData);

    }

    @Override
    public boolean spawnEntityOrCapture(UnwindingPhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return context.getCapturedEntities().add(entity);
    }
}
