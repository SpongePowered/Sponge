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

import org.spongepowered.common.event.tracking.ITrackingPhaseState;

public class WorldPhase extends TrackingPhase {

    public enum State implements ITrackingPhaseState {
        TERRAIN_GENERATION,
        CHUNK_LOADING,
        TICKING_ENTITY,
        TICKING_TILE_ENTITY,
        TICKING_BLOCK,
        RANDOM_TICK_BLOCK,
        IDLE;


        @Override
        public TrackingPhase getPhase() {
            return TrackingPhases.WORLD;
        }

        @Override
        public boolean isBusy() {
            return this != IDLE;
        }

        @Override
        public boolean isManaged() {
            return false;
        }

        @Override
        public boolean canSwitchTo(ITrackingPhaseState state) {
            if (this == TERRAIN_GENERATION) {
                if (state.isTicking()) {
                    return true;
                } else if (state == BlockPhase.State.BLOCK_DECAY) {
                    return true;
                }
                // I'm sure there will be more cases.
            }
            return false;
        }

        @Override
        public boolean isTicking() {
            return this == TICKING_BLOCK || this == TICKING_ENTITY || this == TICKING_TILE_ENTITY || this == RANDOM_TICK_BLOCK;
        }
    }

    public WorldPhase(TrackingPhase parent) {
        super(parent);
    }
}
