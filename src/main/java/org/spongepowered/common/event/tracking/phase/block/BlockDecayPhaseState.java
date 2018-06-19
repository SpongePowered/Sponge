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
package org.spongepowered.common.event.tracking.phase.block;

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.WorldUtil;

import java.util.List;
import java.util.stream.Collectors;

final class BlockDecayPhaseState extends BlockPhaseState {

    BlockDecayPhaseState() {
    }

    @Override
    public GeneralizedContext createPhaseContext() {
        return super.createPhaseContext()
            .addCaptures();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(GeneralizedContext context) {
        final LocatableBlock locatable = context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
        final Location<World> worldLocation = locatable.getLocation();
        final IMixinWorldServer mixinWorld = ((IMixinWorldServer) worldLocation.getExtent());

        Sponge.getCauseStackManager().pushCause(locatable);
        context.addNotifierAndOwnerToCauseStack(PhaseTracker.getInstance().getCurrentFrame());

        context.getCapturedBlockSupplier()
            .acceptAndClearIfNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, this, context));

        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
        context.getCapturedItemsSupplier()
            .acceptAndClearIfNotEmpty(items -> {
                final List<Entity> entities = items.stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
                SpongeCommonEventFactory.callSpawnEntity(entities, context);
            });
        context.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> SpongeCommonEventFactory.callSpawnEntity(entities, context));
        context.getCapturedItemStackSupplier()
            .acceptAndClearIfNotEmpty(drops -> {
                final List<EntityItem> items = drops.stream()
                    .map(drop -> drop.create(WorldUtil.asNative(mixinWorld)))
                    .collect(Collectors.toList());
                final List<Entity> entities = (List<Entity>) (List<?>) items;
                if (!entities.isEmpty()) {
                    DropItemEvent.Custom
                        event =
                        SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        EntityUtil.processEntitySpawnsFromEvent(context, event);
                    }
                }
            });
    }
}
