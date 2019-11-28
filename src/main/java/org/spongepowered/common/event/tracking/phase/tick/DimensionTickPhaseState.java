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
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;
import net.minecraft.entity.item.ItemEntity;

class DimensionTickPhaseState extends TickPhaseState<DimensionContext> {
    DimensionTickPhaseState() {
    }

    @Override
    public DimensionContext createNewContext() {
        return new DimensionContext()
                .addBlockCaptures()
                .addEntityCaptures()
                .addEntityDropCaptures();
    }

    @Override
    public void unwind(final DimensionContext phaseContext) {
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            // TODO - Determine if we need to pass the supplier or perform some parameterized
            //  process if not empty method on the capture object.
            TrackingUtil.processBlockCaptures(phaseContext);

            phaseContext.getCapturedEntitySupplier()
                    .acceptAndClearIfNotEmpty(entities ->
                        SpongeCommonEventFactory.callSpawnEntity(entities, phaseContext)
                    );
            phaseContext.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(entities -> {
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (final ItemEntity entity : entities) {
                            capturedEntities.add((Entity) entity);
                        }
                        SpongeCommonEventFactory.callSpawnEntity(capturedEntities, phaseContext);
                    });
        }
    }

    /*
    @author - gabizou
    non-javadoc
    This is a stopgap to get dragon respawns working. Since there's 4 classes that interweave themselves
    between various states including but not withstanding: respawning endercrystals, respawning the dragon,
    locating the crystals, etc. it's best to not capture the spawns and simply spawn them in directly.
    This is a todo until the dragon phases are completely configured and correctly managed (should be able to at some point restore
    traditional ai logic to the dragon without the necessity for the dragon being summoned the manual way).

     */
    @Override
    public boolean spawnEntityOrCapture(final DimensionContext context, final Entity entity, final int chunkX, final int chunkZ) {
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public boolean doesDenyChunkRequests() {
        return false;
    }
}
