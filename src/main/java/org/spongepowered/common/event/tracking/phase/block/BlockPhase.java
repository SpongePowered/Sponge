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
package org.spongepowered.common.event.tracking.phase.block;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;

public final class BlockPhase extends TrackingPhase {

    public static final class State {
        public static final IPhaseState BLOCK_DECAY = new BlockDecayPhaseState();
        public static final IPhaseState RESTORING_BLOCKS = new RestoringBlockPhaseState();
        public static final IPhaseState DISPENSE = new DispensePhaseState();
        public static final IPhaseState BLOCK_DROP_ITEMS = new BlockDropItemsPhaseState();
        public static final IPhaseState BLOCK_ADDED = null;
        public static final IPhaseState BLOCK_BREAK = null;
        public static final IPhaseState PISTON_MOVING = new PistonMovingPhaseState();

        private State() {
        }

    }

    public static BlockPhase getInstance() {
        return Holder.INSTANCE;
    }

    private BlockPhase() {
    }

    private static final class Holder {
        static final BlockPhase INSTANCE = new BlockPhase();
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return currentState != State.RESTORING_BLOCKS;
    }

    @Override
    public boolean allowEntitySpawns(IPhaseState currentState) {
        return ((BlockPhaseState) currentState).allowsSpawns();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(IPhaseState state, PhaseContext phaseContext) {
        ((BlockPhaseState) state).unwind(phaseContext);
    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX,
            int chunkZ) {
        return this.allowEntitySpawns(phaseState)
               ? context.getCapturedEntities().add(entity)
               : super.spawnEntityOrCapture(phaseState, context, entity, chunkX, chunkZ);
    }

    @Override
    public boolean isRestoring(IPhaseState state, PhaseContext phaseContext, int updateFlag) {
        return state == State.RESTORING_BLOCKS && (updateFlag & 1) == 0;
    }

}
