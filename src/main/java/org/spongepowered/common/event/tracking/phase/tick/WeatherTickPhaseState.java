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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;

class WeatherTickPhaseState extends TickPhaseState {

    WeatherTickPhaseState() {
    }

    @Override
    public void processPostTick(PhaseContext phaseContext) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.WEATHER);
            phaseContext.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                final SpawnEntityEvent spawnEntityEvent =
                        SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
                SpongeImpl.postEvent(spawnEntityEvent);
                for (Entity entity : spawnEntityEvent.getEntities()) {
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                }
            });
            phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blockSnapshots -> {
                TrackingUtil.processBlockCaptures(blockSnapshots, this, phaseContext);
            });
        }
    }

    @Override
    public boolean spawnEntityOrCapture(PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.WEATHER);
            final ArrayList<Entity> capturedEntities = new ArrayList<>();
            capturedEntities.add(entity);
            final SpawnEntityEvent spawnEntityEvent =
                    SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), capturedEntities);
            SpongeImpl.postEvent(spawnEntityEvent);
            Sponge.getCauseStackManager().popCauseFrame(frame);
            if (!spawnEntityEvent.isCancelled()) {
                for (Entity anEntity : spawnEntityEvent.getEntities()) {
                    EntityUtil.getMixinWorld(anEntity).forceSpawnEntity(anEntity);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "WeatherTickPhase";
    }
}
