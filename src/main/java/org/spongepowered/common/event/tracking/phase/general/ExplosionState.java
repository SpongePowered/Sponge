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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.function.BiConsumer;

final class ExplosionState extends GeneralState<ExplosionContext> {

    private final BiConsumer<CauseStackManager.StackFrame, ExplosionContext> EXPLOSION_MODIFIER =
        super.getFrameModifier().andThen((frame, context) -> frame.pushCause(context.getExplosion()));

    @Override
    public ExplosionContext createNewContext(final PhaseTracker tracker) {
        return new ExplosionContext(tracker)
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
        TrackingUtil.processBlockCaptures(context);
//        context.getCapturedEntitySupplier()
//            .acceptAndClearIfNotEmpty(entities -> {
//                try (final CauseStackManager.StackFrame smaller = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
//                    smaller.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.TNT_IGNITE);
//                    SpongeCommonEventFactory.callSpawnEntity(entities, context);
//                }
//            });
//        context.getBlockItemDropSupplier().acceptAndClearIfNotEmpty(drops -> drops.asMap()
//            .forEach((pos, items) -> {
//                if (ShouldFire.DROP_ITEM_EVENT_DESTRUCT) {
//                    final List<Entity> itemEntities = items.stream().map(entity -> (Entity) entity).collect(Collectors.toList());
//                    final DropItemEvent.Destruct event =
//                        SpongeEventFactory.createDropItemEventDestruct(PhaseTracker.getCauseStackManager().getCurrentCause(), itemEntities);
//                    SpongeCommon.postEvent(event);
//                    if (!event.isCancelled()) {
//                        EntityUtil.processEntitySpawnsFromEvent(context, event);
//                    }
//                } else {
//                    items
//                        .forEach(item -> EntityUtil.processEntitySpawn((Entity) item, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context)));
//                }
//            })
//        );

    }

    @Override
    public ChangeBlockEvent.Post createChangeBlockPostEvent(final ExplosionContext context, final ImmutableList<Transaction<BlockSnapshot>> transactions,
        final Cause cause) {
        return SpongeEventFactory.createExplosionEventPost(cause, context.getSpongeExplosion(), transactions);
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

}
