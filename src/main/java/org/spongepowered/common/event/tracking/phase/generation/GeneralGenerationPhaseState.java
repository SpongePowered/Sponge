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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.tick.BlockTickContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A generalized generation phase state. Used for entering populator world generation,
 * new chunk generation, and world spawner entity spawning (since it is used as a populator).
 * Generally does not capture or throw events unless necessary.
 */
@SuppressWarnings("rawtypes")
abstract class GeneralGenerationPhaseState<G extends GenerationContext<G>> extends PooledPhaseState<G> implements IPhaseState<G> {

    private Set<IPhaseState<?>> compatibleStates = new HashSet<>();
    private boolean isBaked = false;
    private final String id;
    private final String desc;

    GeneralGenerationPhaseState(final String id) {
        this.id = id;
        this.desc = TrackingUtil.phaseStateToString("Generation", id, this);
    }

    final GeneralGenerationPhaseState addCompatibleState(final IPhaseState<?> state) {
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
    public boolean requiresPost() {
        return false;
    }

    @Override
    public final boolean isNotReEntrant() {
        return false;
    }

    @Override
    public boolean ignoresBlockEvent() {
        return true;
    }

    @Override
    public boolean ignoresBlockUpdateTick(final G context) {
        return true;
    }

    @Override
    public boolean ignoresEntityCollisions() {
        return true;
    }

    @Override
    public boolean doesBulkBlockCapture(final G context) {
        return false;
    }

    @Override
    public boolean alreadyProcessingBlockItemDrops() {
        return true;
    }

    @Override
    public boolean isWorldGeneration() {
        return true;
    }

    @Override
    public boolean includesDecays() {
        return true;
    }

    @Override
    public boolean allowsEventListener() {
        return false;
    }

    @Override
    public boolean shouldCaptureBlockChangeOrSkip(final G phaseContext, final BlockPos pos, final BlockState currentState,
        final BlockState newState, final BlockChangeFlag flags) {
        return false;
    }

    @Override
    public void appendNotifierPreBlockTick(ServerWorld mixinWorld, BlockPos pos, G context, BlockTickContext phaseContext) {
    }

    @Override
    public boolean doesBlockEventTracking(final G context) {
        return false;
    }

    @Override
    public void unwind(final G context) {

    }

    @Override
    public boolean spawnEntityOrCapture(final G context, final Entity entity) {
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity.getLocation().getWorld());
            return SpongeCommonEventFactory.callSpawnEntitySpawner(entities, context);
        }
    }

    @Override
    public boolean tracksCreatorsAndNotifiers() {
        return false;
    }

    @Override
    public boolean shouldProvideModifiers(final G phaseContext) {
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final GeneralGenerationPhaseState that = (GeneralGenerationPhaseState) o;
        return Objects.equal(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
