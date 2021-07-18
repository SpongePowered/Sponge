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

import org.spongepowered.common.event.tracking.IPhaseState;

/**
 * A specific tracking phase to handle any point in which world or chunk
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
public final class GenerationPhase {

    @SuppressWarnings("unchecked")
    public static final class State {

        public static final IPhaseState<ChunkLoadContext> CHUNK_LOADING = new ChunkLoadPhaseState();

        public static final IPhaseState<DeferredScheduledUpdatePhaseState.Context> DEFERRED_SCHEDULED_UPDATE = new DeferredScheduledUpdatePhaseState();

        public static final IPhaseState<GenericGenerationContext> CHUNK_REGENERATING_LOAD_EXISTING = new ChunkRegeneratingLoadExistingPhaseState();

        public static final IPhaseState<ChunkRegenerateContext> CHUNK_REGENERATING = new ChunkRegeneratePhaseState();

        public static final IPhaseState<GenericGenerationContext> WORLD_SPAWNER_SPAWNING = new WorldSpawnerPhaseState();

        public static final IPhaseState<PopulatorPhaseContext> POPULATOR_RUNNING = new PopulatorGenerationPhaseState("POPULATOR_RUNNING");

        public static final IPhaseState<FeaturePhaseContext> FEATURE_PLACEMENT = new FeatureGenerationPhaseState("FEATURE_PLACEMENT");

        public static final IPhaseState<GenericGenerationContext> TERRAIN_GENERATION = new TerrainGenerationState();

        public static final IPhaseState<GenerationCompatibileContext> GENERATION_COMPATIBILITY = new GenerationCompatibilityState();

    }

    private GenerationPhase() {
    }
}
