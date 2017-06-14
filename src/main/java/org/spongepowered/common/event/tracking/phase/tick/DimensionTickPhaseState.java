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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;

class DimensionTickPhaseState extends TickPhaseState {
    DimensionTickPhaseState() {
    }

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return super.canSwitchTo(state) || state.getPhase() == TrackingPhases.DRAGON;
    }

    @Override
    public void processPostTick(PhaseContext phaseContext) {
        phaseContext.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blockSnapshots -> {
                    TrackingUtil.processBlockCaptures(blockSnapshots, this, phaseContext);
                });

        phaseContext.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    // TODO the entity spawn causes are not likely valid, need to investigate further.
                    final Cause cause = Cause.source(SpawnCause.builder()
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build())
                            .build();
                    final SpawnEntityEvent event =
                            SpongeEventFactory.createSpawnEntityEvent(cause, entities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        for (Entity entity : event.getEntities()) {
                            EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                        }
                    }

                });
        phaseContext.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final Cause cause = Cause.source(SpawnCause.builder()
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build())
                            .build();
                    final ArrayList<Entity> capturedEntities = new ArrayList<>();
                    for (EntityItem entity : entities) {
                        capturedEntities.add(EntityUtil.fromNative(entity));
                    }
                    final SpawnEntityEvent event =
                            SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        for (Entity entity : event.getEntities()) {
                            EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                        }
                    }
                });
    }
    @Override
    public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder) {

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
    public boolean spawnEntityOrCapture(PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final WorldServer minecraftWorld = (WorldServer) minecraftEntity.world;
        final User user = context.getNotifier().orElseGet(() -> context.getOwner().orElse(null));
        if (user != null) {
            entity.setCreator(user.getUniqueId());
        }
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        final Cause cause = Cause.source(SpawnCause.builder()
                .type(InternalSpawnTypes.PLACEMENT)
                .build())
                .build();
        final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, entities);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled() && event.getEntities().size() > 0) {
            for (Entity item: event.getEntities()) {
                EntityUtil.getMixinWorld(entity).forceSpawnEntity(item);
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "DimensionTickPhase";
    }
}
