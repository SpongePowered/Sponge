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
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.entity.item.ExperienceOrbEntity;

@SuppressWarnings("unchecked")
class NeighborNotificationState extends LocationBasedTickPhaseState<NeighborNotificationContext> {

    private final BiConsumer<CauseStackManager.StackFrame, NeighborNotificationContext> FRAME_MODIFIER =
        ((BiConsumer<CauseStackManager.StackFrame, NeighborNotificationContext>) IPhaseState.DEFAULT_OWNER_NOTIFIER)
            .andThen((frame, context) -> {
                if (context.notificationSnapshot != null) {
                    frame.addContext(EventContextKeys.NEIGHBOR_NOTIFY_SOURCE, context.notificationSnapshot);
                }
            });

    @Override
    public NeighborNotificationContext createNewContext() {
        return new NeighborNotificationContext(this)
                .addCaptures();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, NeighborNotificationContext> getFrameModifier() {
        return this.FRAME_MODIFIER;
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(final PhaseContext<?> context) {
        return context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
    }

    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final NeighborNotificationContext context) {
        context.applyNotifierIfAvailable(explosionContext::notifier);
        context.applyOwnerIfAvailable(explosionContext::owner);
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        explosionContext.source(locatableBlock);
    }

    @Override
    public boolean spawnEntityOrCapture(final NeighborNotificationContext context, final Entity entity, final int chunkX, final int chunkZ) {
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        if (!context.allowsEntityEvents() || !ShouldFire.SPAWN_ENTITY_EVENT) { // We don't want to throw an event if we don't need to.
            return EntityUtil.processEntitySpawn(entity, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(locatableBlock);
            if (entity instanceof ExperienceOrbEntity) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                final ArrayList<Entity> entities = new ArrayList<>(1);
                entities.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(entities, context);
            }
            final List<Entity> nonExpEntities = new ArrayList<>(1);
            nonExpEntities.add(entity);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            return SpongeCommonEventFactory.callSpawnEntity(nonExpEntities, context);
        }
    }

    @Override
    public void provideNotifierForNeighbors(final NeighborNotificationContext context, final NeighborNotificationContext notification) {
        super.provideNotifierForNeighbors(context, notification);
        notification.setDepth(context.getDepth() + 1);
    }


    @Override
    public boolean isNotReEntrant() {
        return false;
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if bulk block captures are usable for this entity type (default true)
     */
    @Override
    public boolean doesBulkBlockCapture(final NeighborNotificationContext context) {
        return false;
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if block events are to be tracked by the specific type of entity (default is true)
     */
    @Override
    public boolean doesBlockEventTracking(final NeighborNotificationContext context) {
        return context.allowsBlockEvents();
    }


    @Override
    public boolean doesCaptureEntityDrops(final NeighborNotificationContext context) {
        return false; // Maybe make this configurable as well.
    }
}
