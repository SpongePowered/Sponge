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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * A generalized
     */
    static class GeneralGenerationPhaseState implements IPhaseState {

        private Set<IPhaseState> compatibleStates = new HashSet<>();
        private boolean isBaked = false;
        private final String id;

        GeneralGenerationPhaseState(String id) {
            this.id = id;
        }

        final GeneralGenerationPhaseState addCompatibleState(IPhaseState state) {
            if (this.isBaked) {
                throw new IllegalStateException("This state is already baked! " + this.id);
            }
            this.compatibleStates.add(state);
            return this;
        }

        final GeneralGenerationPhaseState addCompatibleStates(IPhaseState... states) {
            if (this.isBaked) {
                throw new IllegalStateException("This state is already baked! " + this.id);
            }
            Collections.addAll(this.compatibleStates, states);
            return this;
        }

        final GeneralGenerationPhaseState bake() {
            if (this.isBaked) {
                throw new IllegalStateException("This state is already baked! " + this.id);
            }
            this.compatibleStates = ImmutableSet.copyOf(this.compatibleStates);
            this.isBaked = true;
            return this;
        }

        @Override
        public final TrackingPhase getPhase() {
            return TrackingPhases.GENERATION;
        }

        @Override
        public final boolean canSwitchTo(IPhaseState state) {
            return this.compatibleStates.contains(state);
        }

        @Override
        public final boolean isExpectedForReEntrance() {
            return true;
        }


        Cause provideSpawnCause(CauseTracker causeTracker, PhaseContext context) {
            return Cause.source(InternalSpawnTypes.SpawnCauses.WORLD_SPAWNER_CAUSE).named("World", causeTracker.getWorld()).build();
        }

        @SuppressWarnings("unchecked")
        public final void unwind(CauseTracker causeTracker, PhaseContext context) {
            final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
            if (spawnedEntities.isEmpty()) {
                return;
            }
            final Cause cause = provideSpawnCause(causeTracker, context);

            final SpawnEntityEvent.Spawner
                    event =
                    SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, causeTracker.getWorld());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity entity : event.getEntities()) {
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            }

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            GeneralGenerationPhaseState that = (GeneralGenerationPhaseState) o;
            return Objects.equal(this.id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.id);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("id", this.id)
                    .toString();
        }
    }

    private static class PopulatorGenerationPhaseState extends GeneralGenerationPhaseState {

        PopulatorGenerationPhaseState(String id) {
            super(id);
        }

        @Override
        Cause provideSpawnCause(CauseTracker causeTracker, PhaseContext context) {
            final PopulatorType runningGenerator = context.firstNamed(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, PopulatorType.class)
                    .orElse(null);
            final Cause.Builder causeBuilder = Cause.builder();
            Cause.source(InternalSpawnTypes.SpawnCauses.WORLD_SPAWNER_CAUSE).named("World",  causeTracker.getWorld());
            if (InternalPopulatorTypes.ANIMAL.equals(runningGenerator)) {
                causeBuilder.named(NamedCause.source(InternalSpawnTypes.SpawnCauses.WORLD_SPAWNER_CAUSE))
                        .named(NamedCause.of(InternalNamedCauses.General.ANIMAL_SPAWNER, runningGenerator));
            } else if (runningGenerator != null) {
                causeBuilder.named(NamedCause.source(InternalSpawnTypes.SpawnCauses.STRUCTURE_SPAWNING))
                        .named(NamedCause.of(InternalNamedCauses.WorldGeneration.STRUCTURE, runningGenerator));
            } else {
                causeBuilder.named(NamedCause.source(InternalSpawnTypes.SpawnCauses.STRUCTURE_SPAWNING));
            }
            return causeBuilder.build();
        }

    }

    GenerationPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public GenerationPhase addChild(TrackingPhase child) {
        super.addChild(child);
        return this;
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
    protected void processPostEntitySpawns(CauseTracker causeTracker, IPhaseState unwindingState, ArrayList<Entity> entities) {
        super.processPostEntitySpawns(causeTracker, unwindingState, entities);
    }

    @Override
    public boolean isWorldGeneration(IPhaseState state) {
        return true;
    }

    @Override
    public void appendPreBlockProtectedCheck(Cause.Builder builder, IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker) {
    }

}
