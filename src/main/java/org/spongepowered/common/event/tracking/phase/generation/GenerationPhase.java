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
package org.spongepowered.common.event.tracking.phase.generation;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;

import java.util.ArrayList;

/**
 * A specific {@link TrackingPhase} to handle any point in which world or chunk
 * terrain generation/population is taking place. Note that while {@link State#CHUNK_LOADING}
 * is included here, it is still treated as terrain generation due to the fact that
 * a chunk load should not capture any block changes and populators may be re-ran.
 * Virtually, no capturing takes place during generation, withstanding capturing
 * entities as those can be captured safely to allow plugins final say in whether
 * the entities spawn. Likewise, Forge requires the entities to still throw
 * spawn events, so we must oblige in both Forge's requirements and Plugins requirements.
 * Note that {@link State#TERRAIN_GENERATION} and {@link State#POPULATOR_RUNNING} are
 * re-entrant and should allow for re-entrance. Re-entrance is defined in this case when
 * while a chunk is being generated/populated, the generator and/or populator require
 * a neighboring chunk to be generated/populated, which requires a re-entrance of the
 * very same state, with different chunk coordinates.
 */
public final class GenerationPhase extends TrackingPhase {

    public static final class State {

        public static final IPhaseState CHUNK_LOADING = new GeneralGenerationPhaseState("CHUNK_LOADING").bake();

        public static final IPhaseState WORLD_SPAWNER_SPAWNING = new GeneralGenerationPhaseState("WORLD_SPAWNER_SPAWNING").bake();

        public static final IPhaseState POPULATOR_RUNNING = new PopulatorGenerationPhaseState("POPULATOR_RUNNING");

        public static final IPhaseState TERRAIN_GENERATION = new GeneralGenerationPhaseState("TERRAIN_GENERATION");

        static {
            ((GeneralGenerationPhaseState) POPULATOR_RUNNING)
                    .addCompatibleState(BlockPhase.State.BLOCK_DECAY)
                    .addCompatibleState(BlockPhase.State.BLOCK_DROP_ITEMS)
                    .addCompatibleState(BlockPhase.State.RESTORING_BLOCKS)
                    .addCompatibleState(GenerationPhase.State.WORLD_SPAWNER_SPAWNING)
                    .addCompatibleState(GeneralPhase.Post.UNWINDING)
                    .addCompatibleState(GenerationPhase.State.POPULATOR_RUNNING)
                    .bake();
            ((GeneralGenerationPhaseState) TERRAIN_GENERATION)
                    .addCompatibleState(BlockPhase.State.BLOCK_DECAY)
                    .addCompatibleState(BlockPhase.State.BLOCK_DROP_ITEMS)
                    .addCompatibleState(BlockPhase.State.RESTORING_BLOCKS)
                    .addCompatibleState(GenerationPhase.State.POPULATOR_RUNNING)
                    .addCompatibleState(GenerationPhase.State.WORLD_SPAWNER_SPAWNING)
                    .addCompatibleState(GeneralPhase.Post.UNWINDING)
                    .bake();
        }
    }

    public static GenerationPhase getInstance() {
        return Holder.INSTANCE;
    }

    private GenerationPhase() {
    }

    private static final class Holder {
        static final GenerationPhase INSTANCE = new GenerationPhase();
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        ((GeneralGenerationPhaseState) state).unwind(causeTracker, phaseContext);
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return false;
    }

    @Override
    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return true;
    }

    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return phaseData.state != GenerationPhase.State.WORLD_SPAWNER_SPAWNING;
    }

    @Override
    public void appendNotifierPreBlockTick(CauseTracker causeTracker, BlockPos pos, IPhaseState currentState, PhaseContext context, PhaseContext newContext) {

    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return context.getCapturedEntities().add(entity);
    }

    @Override
    public void processPostEntitySpawns(CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext phaseContext,
            ArrayList<Entity> entities) {
        super.processPostEntitySpawns(causeTracker, unwindingState, phaseContext, entities);
    }

    @Override
    public boolean isWorldGeneration(IPhaseState state) {
        return true;
    }

    @Override
    public boolean appendPreBlockProtectedCheck(Cause.Builder builder, IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker) {
        return false;
    }

}
