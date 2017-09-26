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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;

import javax.annotation.Nullable;

public final class TickPhase extends TrackingPhase {

    public static final class Tick {

        public static final IPhaseState<BlockTickContext> NO_CAPTURE_BLOCK = new NoCaptureBlockTickPhaseState("NoCaptureBlockTickPhase");
        public static final IPhaseState<BlockTickContext> BLOCK = new BlockTickPhaseState("BlockTickPhase");
        public static final IPhaseState<BlockTickContext> RANDOM_BLOCK = new BlockTickPhaseState("RandomBlockTickPhase");

        public static final IPhaseState<EntityTickContext> ENTITY = new EntityTickPhaseState();

        public static final IPhaseState<DimensionContext> DIMENSION = new DimensionTickPhaseState();
        public static final IPhaseState<TileEntityTickContext> TILE_ENTITY = new TileEntityTickPhaseState();
        public static final IPhaseState<BlockEventTickContext> BLOCK_EVENT = new BlockEventTickPhaseState();
        public static final IPhaseState<PlayerTickContext> PLAYER = new PlayerTickPhaseState();
        public static final IPhaseState<?> WEATHER = new WeatherTickPhaseState();

        private Tick() { // No instances for you!
        }
    }

    public static TickPhase getInstance() {
        return Holder.INSTANCE;
    }

    private TickPhase() {
    }

    private static final class Holder {
        static final TickPhase INSTANCE = new TickPhase();
    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState<?> phaseState, PhaseContext<?> context, Entity entity, int chunkX, int chunkZ) {
        return ((TickPhaseState<?>) phaseState).spawnEntityOrCapture(context, entity, chunkX, chunkZ);
    }

    @Override
    public void addNotifierToBlockEvent(IPhaseState<?> phaseState, PhaseContext<?> context, IMixinWorldServer mixinWorld, BlockPos pos, IMixinBlockEventData blockEvent) {
    }

    @Override
    public void appendNotifierPreBlockTick(IMixinWorldServer mixinWorld, BlockPos pos, IPhaseState<?> currentState, PhaseContext<?> context, PhaseContext<?> newContext) {
        if (currentState == Tick.BLOCK || currentState == Tick.RANDOM_BLOCK) {

        }
    }

    @Override
    public void associateNeighborStateNotifier(IPhaseState<?> state, PhaseContext<?> context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                                               WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        ((TickPhaseState<?>) state).associateNeighborBlockNotifier(context, sourcePos, block, notifyPos, minecraftWorld, notifier);
    }


    @Override
    public boolean alreadyCapturingItemSpawns(IPhaseState<?> currentState) {
        return currentState != Tick.NO_CAPTURE_BLOCK;
    }

    @Override
    public void appendContextPreExplosion(PhaseContext<?> phaseContext, PhaseData currentPhaseData) {
        ((TickPhaseState<?>) currentPhaseData.state).appendExplosionContext(phaseContext, currentPhaseData.context);
    }
}
