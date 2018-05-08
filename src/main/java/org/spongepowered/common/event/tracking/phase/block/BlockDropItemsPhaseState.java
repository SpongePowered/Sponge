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
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.world.WorldUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class BlockDropItemsPhaseState extends BlockPhaseState {

    BlockDropItemsPhaseState() {
    }

    @Override
    public GeneralizedContext createPhaseContext() {
        return super.createPhaseContext()
                .addBlockCaptures()
                .addEntityCaptures();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(GeneralizedContext context) {
        final BlockSnapshot blockSnapshot = context.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Could not find a block dropping items!", context));
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(blockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);
            context.addNotifierAndOwnerToCauseStack(frame);
            context.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(items -> {
                        final ArrayList<Entity> entities = new ArrayList<>();
                        for (EntityItem item : items) {
                            entities.add(EntityUtil.fromNative(item));
                        }
                        SpongeCommonEventFactory.callDropItemDestruct(entities, context);
                    });
            context.getCapturedEntitySupplier()
                    .acceptAndClearIfNotEmpty(entities -> {
                        final SpawnEntityEvent event =
                                SpongeEventFactory.createSpawnEntityEvent(frame.getCurrentCause(), entities);
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            EntityUtil.processEntitySpawnsFromEvent(context, event);
                        }
                        entities.clear();
                    });
            final Location<World> worldLocation = blockSnapshot.getLocation().get();
            final IMixinWorldServer mixinWorld = ((IMixinWorldServer) worldLocation.getExtent());

            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            context.getCapturedBlockSupplier()
                    .acceptAndClearIfNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, this, context));
            context.getCapturedItemStackSupplier()
                    .acceptAndClearIfNotEmpty(drops -> {
                        final List<EntityItem> items = drops.stream()
                                .map(drop -> drop.create(WorldUtil.asNative(mixinWorld)))
                                .collect(Collectors.toList());
                        final List<Entity> entities = (List<Entity>) (List<?>) items;
                        if (!entities.isEmpty()) {
                            SpongeCommonEventFactory.callDropItemCustom(entities, context);
                        }
                        drops.clear();

                    });
            context.getBlockDropSupplier()
                .acceptAndClearIfNotEmpty(drops -> {
                    for (BlockPos key : drops.asMap().keySet()) {
                        final List<ItemDropData> values = drops.get(key);
                        if (!values.isEmpty()) {
                            TrackingUtil.spawnItemDataForBlockDrops(values, blockSnapshot, context);
                        }
                    }
                });
        }
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return true;
    }
}
