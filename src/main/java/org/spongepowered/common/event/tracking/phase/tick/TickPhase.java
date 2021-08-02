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

import org.spongepowered.common.event.tracking.IPhaseState;

public final class TickPhase {

    public static final class Tick {

        public static final IPhaseState<BlockTickContext> BLOCK = new BlockTickPhaseState("BlockTickPhase");
        public static final IPhaseState<FluidTickContext> FLUID = new FluidTickPhaseState("FluidTickPhase");
        public static final IPhaseState<FluidTickContext> RANDOM_FLUID = new FluidTickPhaseState("RandomFluidTickPhase");
        public static final IPhaseState<BlockTickContext> RANDOM_BLOCK = new BlockTickPhaseState("RandomBlockTickPhase");

        public static final IPhaseState<EntityTickContext> ENTITY = new EntityTickPhaseState();

        public static final IPhaseState<TileEntityTickContext> TILE_ENTITY = new TileEntityTickPhaseState();
        public static final IPhaseState<BlockEventTickContext> BLOCK_EVENT = new BlockEventTickPhaseState();
        public static final IPhaseState<PlayerTickContext> PLAYER = new PlayerTickPhaseState();
        public static final IPhaseState<?> WEATHER = new WeatherTickPhaseState();
        public static final IPhaseState<ServerTickState.ServerTickContext> SERVER_TICK = new ServerTickState();
        public static final IPhaseState<WorldTickState.WorldTickContext> WORLD_TICK = new WorldTickState();

        private Tick() { // No instances for you!
        }
    }

    private TickPhase() {
    }

}
