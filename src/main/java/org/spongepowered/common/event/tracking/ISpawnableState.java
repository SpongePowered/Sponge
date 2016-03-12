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
package org.spongepowered.common.event.tracking;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.World;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A state that is known to spawn entities. Upon completion of the
 * state, it should be considered that all entities spawned are
 * therefor processed as bulk events. Examples of said events
 * include {@link SpawnEntityEvent}.
 */
public interface ISpawnableState extends IPhaseState {

    @Nullable
    SpawnEntityEvent createSpawnEventPostProcess(Cause cause, CauseTracker causeTracker, PhaseContext phaseContext,
            List<EntitySnapshot> entitySnapshots);

    @Nullable
    default SpawnEntityEvent createEntityEvent(Cause cause, CauseTracker causeTracker, PhaseContext phaseContext) {
        final PhaseContext.CapturedSupplier<Entity> capturedEntitiesSupplier = phaseContext.getCapturedEntitySupplier().get();
        if (capturedEntitiesSupplier.isEmpty()) {
            return null;
        }
        List<Transaction<BlockSnapshot>> invalidTransactions = phaseContext.getInvalidTransactions().get();
        final Tuple<List<EntitySnapshot>, Cause> listCauseTuple =
                TrackingHelper.processSnapshotsForSpawning(cause, capturedEntitiesSupplier.get(), invalidTransactions);
        List<EntitySnapshot> entitySnapshots = listCauseTuple.getFirst();
        cause = listCauseTuple.getSecond();
        if (entitySnapshots.isEmpty()) {
            return null;
        }
        return createSpawnEventPostProcess(cause, causeTracker, phaseContext, entitySnapshots);
    }
}
