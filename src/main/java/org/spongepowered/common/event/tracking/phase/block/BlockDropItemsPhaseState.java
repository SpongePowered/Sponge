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
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.context.ItemDropData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.WorldUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

final class BlockDropItemsPhaseState extends BlockPhaseState {

    private final BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> BLOCK_DROP_MODIFIER = super.getFrameModifier()
        .andThen((frame, ctx) -> {
            final BlockSnapshot blockSnapshot = ctx.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Could not find a block dropping items!", ctx));
            frame.pushCause(blockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
        });

    BlockDropItemsPhaseState() {
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> getFrameModifier() {
        return this.BLOCK_DROP_MODIFIER;
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

        context.getCapturedItemsSupplier()
            .acceptAndClearIfNotEmpty(items -> {
                final ArrayList<Entity> entities = new ArrayList<>();
                for (EntityItem item : items) {
                    entities.add(EntityUtil.fromNative(item));
                }
                SpongeCommonEventFactory.callDropItemDestruct(entities, context);
            });
        context.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> SpongeCommonEventFactory.callSpawnEntity(entities, context));
        final BlockSnapshot blockSnapshot = context.getSource(BlockSnapshot.class)
            .orElseThrow(TrackingUtil.throwWithContext("Could not find a block dropping items!", context));
        final Location<World> worldLocation = blockSnapshot.getLocation().get();
        final IMixinWorldServer mixinWorld = ((IMixinWorldServer) worldLocation.getExtent());

        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context);
        context.getCapturedItemStackSupplier()
            .acceptAndClearIfNotEmpty(drops -> {
                final List<EntityItem> items = drops.stream()
                    .map(drop -> drop.create(WorldUtil.asNative(mixinWorld)))
                    .collect(Collectors.toList());
                final List<Entity> entities = (List<Entity>) (List<?>) items;
                if (!entities.isEmpty()) {
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                    SpongeCommonEventFactory.callDropItemCustom(entities, context);
                }
                drops.clear();

            });
        context.getBlockDropSupplier()
            .acceptAndClearIfNotEmpty(drops -> {
                for (BlockPos key : drops.asMap().keySet()) {
                    final List<ItemDropData> values = drops.get(key);
                    if (!values.isEmpty()) {
                        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                        TrackingUtil.spawnItemDataForBlockDrops(values, blockSnapshot, context);
                    }
                }
            });

    }

    @Override
    public boolean tracksBlockSpecificDrops(GeneralizedContext context) {
        return true;
    }
}
