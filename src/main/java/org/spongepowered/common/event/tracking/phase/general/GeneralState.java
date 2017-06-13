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

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;

abstract class GeneralState implements IPhaseState {

    abstract void unwind(PhaseContext context);

    @Override
    public final TrackingPhase getPhase() {
        return TrackingPhases.GENERAL;
    }

    /**
     * A duplicate of {@link TrackingPhase#spawnEntityOrCapture(IPhaseState, PhaseContext, Entity, int, int)}
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
    public boolean spawnEntityOrCapture(PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final User user = context.getNotifier().orElseGet(() -> context.getOwner().orElse(null));
        if (user != null) {
            entity.setCreator(user.getUniqueId());
        }
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(InternalSpawnTypes.UNKNOWN_CAUSE,
                entities);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled() && event.getEntities().size() > 0) {
            for (Entity item: event.getEntities()) {
                ((IMixinWorldServer) item.getWorld()).forceSpawnEntity(item);
            }
            return true;
        }
        return false;
    }

    public Cause generateTeleportCause(PhaseContext context) {
        return Cause.of(NamedCause.source(TeleportCause.builder().type(TeleportTypes.UNKNOWN).build()));
    }

    public boolean requiresPost() {
        return true;
    }
}
