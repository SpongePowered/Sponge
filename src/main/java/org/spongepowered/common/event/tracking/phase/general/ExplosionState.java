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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.accessor.world.level.ExplosionAccessor;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.ChangeBlock;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.context.transaction.SpawnEntityTransaction;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;

final class ExplosionState extends GeneralState<ExplosionContext> {

    private final BiConsumer<CauseStackManager.StackFrame, ExplosionContext> EXPLOSION_MODIFIER =
        super.getFrameModifier().andThen((frame, context) -> {
            final Explosion explosion = context.getExplosion();
            final @Nullable LivingEntity placedBy = explosion.getSourceMob();
            if (placedBy != null) {
                if (placedBy instanceof CreatorTrackedBridge) {
                    ((CreatorTrackedBridge) placedBy).tracked$getCreatorReference()
                        .ifPresent(creator -> frame.addContext(EventContextKeys.CREATOR, creator));
                    ((CreatorTrackedBridge) placedBy).tracked$getNotifierReference()
                        .ifPresent(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
                }
                frame.addContext(EventContextKeys.IGNITER, (Living) placedBy);
            }
            final @Nullable Entity exploder = ((ExplosionAccessor) explosion).accessor$source();
            if (exploder != null) {
                frame.pushCause(exploder);
            }
            frame.pushCause(explosion);
        });

    @Override
    public ExplosionContext createNewContext(final PhaseTracker tracker) {
        return new ExplosionContext(tracker)
            .populateFromCurrentState();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, ExplosionContext> getFrameModifier() {
        return this.EXPLOSION_MODIFIER;
    }

    @Override
    public void populateLootContext(final ExplosionContext phaseContext, final LootContext.Builder lootBuilder) {
        final Explosion explosion = phaseContext.getExplosion();
        lootBuilder.withOptionalParameter(LootContextParams.THIS_ENTITY, ((ExplosionAccessor) explosion).accessor$source());

        if (((ExplosionAccessor) explosion).accessor$blockInteraction() == net.minecraft.world.level.Explosion.BlockInteraction.DESTROY) {
            lootBuilder.withParameter(LootContextParams.EXPLOSION_RADIUS, ((ExplosionAccessor) explosion).accessor$radius());
        }
    }

    @Override
    public void unwind(final ExplosionContext context) {
        TrackingUtil.processBlockCaptures(context);
    }

    @Override
    public SpawnEntityEvent createSpawnEvent(
        final ExplosionContext context,
        final GameTransaction<@NonNull ?> parent,
        final ImmutableList<Tuple<Entity, SpawnEntityTransaction.DummySnapshot>> collect,
        final Cause currentCause
    ) {
        if (parent instanceof ChangeBlock) {
            return SpongeEventFactory.createDropItemEventDestruct(currentCause,
                collect.stream()
                    .map(t -> (org.spongepowered.api.entity.Entity) t.first())
                    .collect(Collectors.toList()));
        }
        return super.createSpawnEvent(context, parent, collect, currentCause);
    }

}
