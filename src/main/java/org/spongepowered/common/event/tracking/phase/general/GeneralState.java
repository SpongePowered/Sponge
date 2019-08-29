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
package org.spongepowered.common.event.tracking.phase.general;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;

abstract class GeneralState<G extends PhaseContext<G>> extends PooledPhaseState<G> implements IPhaseState<G> {

    @Override
    public abstract void unwind(G context);

    /**
     * A duplicate of {@link IPhaseState#spawnEntityOrCapture(PhaseContext, Entity, int, int)}
     * such that the general states will not know what to do for entity spawns. Eventually, this is going to be centralized
     * so that it's not always delegated between the phases and phase states.
     *
     * Basically, for this method, this is included only for the {@link GeneralPhase.State#COMPLETE}, all other
     * will capture or spawn appropriately. In the case of explosions for example, the entities must be mapped
     * according to the blocks broken so that the blocks themselves can be cancelled and the entities spawned
     * are dropped from the game entirely before throwing additional events.
     *
     * @param context
     * @param entity
     * @param chunkX
     * @param chunkZ
     * @return
     */
    @Override
    public boolean spawnEntityOrCapture(final G context, final Entity entity, final int chunkX, final int chunkZ) {
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PASSIVE);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    private final String desc = TrackingUtil.phaseStateToString("General", this);

    @Override
    public String toString() {
        return this.desc;
    }

}
