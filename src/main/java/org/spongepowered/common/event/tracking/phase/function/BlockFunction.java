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
package org.spongepowered.common.event.tracking.phase.function;

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;

public final class BlockFunction {

    public interface Drops {

        Drops DECAY_ITEMS = (blockSnapshot, causeTracker, phaseContext, items) -> {
            // Currently nothing happens here.
        };
        Drops DROP_ITEMS =   (blockSnapshot, causeTracker, phaseContext, items) -> {
            final Cause cause = Cause.source(BlockSpawnCause.builder()
                    .block(blockSnapshot)
                    .type(InternalSpawnTypes.DROPPED_ITEM)
                    .build())
                .build();
            final ArrayList<Entity> entities = new ArrayList<>();
            for (EntityItem item : items) {
                entities.add(EntityUtil.fromNative(item));
            }
            final DropItemEvent.Destruct
                    event =
                    SpongeEventFactory.createDropItemEventDestruct(cause, entities, causeTracker.getWorld());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity entity : event.getEntities()) {
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            }
        };
        Drops DISPENSE = (blockSnapshot, causeTracker, phaseContext, items) -> {
            final Cause cause = Cause.source(BlockSpawnCause.builder()
                        .block(blockSnapshot)
                        .type(InternalSpawnTypes.DISPENSE)
                        .build())
                    .build();
            final ArrayList<Entity> entities = new ArrayList<>();
            for (EntityItem item : items) {
                entities.add(EntityUtil.fromNative(item));
            }
            final DropItemEvent.Dispense
                    event =
                    SpongeEventFactory.createDropItemEventDispense(cause, entities, causeTracker.getWorld());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity entity : event.getEntities()) {
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            }
        };

        void processItemSpawns(BlockSnapshot blockSnapshot, CauseTracker causeTracker, PhaseContext phaseContext, List<EntityItem> items);

    }

    public interface Entities {

        Entities DROP_ITEMS_RANDOM = (blockSnapshot, causeTracker, phaseContext, entities) -> {
            final Cause cause = Cause.source(BlockSpawnCause.builder()
                        .block(blockSnapshot)
                        .type(InternalSpawnTypes.BLOCK_SPAWNING)
                        .build())
                    .build();
            final SpawnEntityEvent
                    event =
                    SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity entity : event.getEntities()) {
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            }
        };

        void processEntities(BlockSnapshot blockSnapshot, CauseTracker causeTracker, PhaseContext phaseContext, List<Entity> entities);
    }

}
