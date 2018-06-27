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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.tick.BlockTickContext;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A generalized
 */
@SuppressWarnings("rawtypes")
abstract class GeneralGenerationPhaseState<G extends GenerationContext<G>> implements IPhaseState<G> {

    private Set<IPhaseState<?>> compatibleStates = new HashSet<>();
    private boolean isBaked = false;
    private final String id;

    GeneralGenerationPhaseState(String id) {
        this.id = id;
    }

    final GeneralGenerationPhaseState addCompatibleState(IPhaseState<?> state) {
        if (this.isBaked) {
            throw new IllegalStateException("This state is already baked! " + this.id);
        }
        this.compatibleStates.add(state);
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
    public final boolean canSwitchTo(IPhaseState<?> state) {
        return this.compatibleStates.contains(state);
    }

    @Override
    public final boolean isExpectedForReEntrance() {
        return true;
    }

    @Override
    public boolean ignoresBlockEvent() {
        return true;
    }

    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return true;
    }

    @Override
    public boolean requiresBlockBulkCaptures() {
        return false;
    }

    @Override
    public boolean alreadyCapturingItemSpawns() {
        return true;
    }

    @Override
    public boolean isWorldGeneration() {
        return true;
    }

    @Override
    public void appendNotifierPreBlockTick(IMixinWorldServer mixinWorld, BlockPos pos, G context, BlockTickContext phaseContext) {

    }

    @Override
    public boolean requiresBlockTracking() {
        return false;
    }

    @Override
    public final void unwind(G context) {
        final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
        if (spawnedEntities.isEmpty()) {
            return;
        }
        SpongeCommonEventFactory.callSpawnEntitySpawner(spawnedEntities, context);
    }

    @Override
    public boolean spawnEntityOrCapture(G context, Entity entity, int chunkX, int chunkZ) {
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity.getLocation().getExtent());
            return SpongeCommonEventFactory.callSpawnEntitySpawner(entities, context);
        }
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
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

    private final String className = this.getClass().getSimpleName();

    @Override
    public String toString() {
        return this.getPhase() + "{" + this.className + ":" + this.id +  "}";
    }
}
