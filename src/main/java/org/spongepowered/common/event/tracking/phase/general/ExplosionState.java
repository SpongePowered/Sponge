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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

final class ExplosionState extends GeneralState<ExplosionContext> {

    private final BiConsumer<CauseStackManager.StackFrame, ExplosionContext> EXPLOSION_MODIFIER =
        super.getFrameModifier().andThen((frame, context) -> frame.pushCause(context.getExplosion()));

    @Override
    public ExplosionContext createNewContext() {
        return new ExplosionContext()
            .addEntityCaptures()
            .addEntityDropCaptures()
            .addBlockCaptures()
            .populateFromCurrentState();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, ExplosionContext> getFrameModifier() {
        return this.EXPLOSION_MODIFIER;
    }

    @Override
    public boolean tracksBlockSpecificDrops(final ExplosionContext context) {
        return true;
    }

    @Override
    public boolean requiresBlockPosTracking() {
        return true;
    }

    @Override
    public boolean alreadyCapturingEntitySpawns() {
        return true;
    }

    @Override
    public boolean alreadyProcessingBlockItemDrops() {
        return true;
    }

    @Override
    public boolean ignoresEntityCollisions() {
        return true;
    }

    @Override
    public void unwind(final ExplosionContext context) {
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(context);
        context.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> {
                try (final CauseStackManager.StackFrame smaller = Sponge.getCauseStackManager().pushCauseFrame()) {
                    smaller.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.TNT_IGNITE);
                    SpongeCommonEventFactory.callSpawnEntity(entities, context);
                }
            });
        context.getBlockItemDropSupplier().acceptAndClearIfNotEmpty(drops -> drops.asMap()
            .forEach((pos, items) -> {
                if (ShouldFire.DROP_ITEM_EVENT_DESTRUCT) {
                    final List<Entity> itemEntities = items.stream().map(entity -> (Entity) entity).collect(Collectors.toList());
                    final DropItemEvent.Destruct event =
                        SpongeEventFactory.createDropItemEventDestruct(Sponge.getCauseStackManager().getCurrentCause(), itemEntities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        EntityUtil.processEntitySpawnsFromEvent(context, event);
                    }
                } else {
                    items
                        .forEach(item -> EntityUtil.processEntitySpawn((Entity) item, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context)));
                }
            })
        );

    }

    @Override
    public ChangeBlockEvent.Post createChangeBlockPostEvent(final ExplosionContext context, final ImmutableList<Transaction<BlockSnapshot>> transactions,
        final Cause cause) {
        return SpongeEventFactory.createExplosionEventPost(cause, context.getSpongeExplosion(), transactions);
    }

    @Override
    public boolean spawnEntityOrCapture(final ExplosionContext context, final Entity entity, final int chunkX, final int chunkZ) {
        return context.getBlockPosition().map(blockPos -> {
            // TODO - this needs to be guaranteed. can't be bothered to figure out why it isn't
            final Multimap<BlockPos, net.minecraft.entity.Entity> blockPosEntityMultimap = context.getPerBlockEntitySpawnSuppplier().get();
            final Multimap<BlockPos, ItemEntity> blockPosEntityItemMultimap = context.getBlockItemDropSupplier().get();
            if (entity instanceof ItemEntity) {
                blockPosEntityItemMultimap.put(blockPos, (ItemEntity) entity);
            } else {
                blockPosEntityMultimap.put(blockPos, (net.minecraft.entity.Entity) entity);
            }
            return true;
        }).orElseGet(() -> {
            final ArrayList<Entity> entities = new ArrayList<>(1);
            entities.add(entity);
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()){
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                return SpongeCommonEventFactory.callSpawnEntity(entities, context);
            }
        });

    }


    @Override
    public boolean doesCaptureEntitySpawns() {
        return true;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

}
