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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.MutableWrapper;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;

final class PistonMovingPhaseState extends BlockPhaseState {

    PistonMovingPhaseState() {
    }

    @SuppressWarnings("unchecked")
    @Override
    void unwind(CauseTracker causeTracker, PhaseContext phaseContext) {
        final List<BlockSnapshot> capturedBlocks = phaseContext.getCapturedBlocks();
        if (!TrackingUtil.processBlockCaptures(capturedBlocks, causeTracker, this, phaseContext)) {
            phaseContext.firstNamed(InternalNamedCauses.Piston.DUMMY_CALLBACK, MutableWrapper.class)
                    .map(wrapper -> ((MutableWrapper<CallbackInfoReturnable<Boolean>>) wrapper).getObj())
                    .ifPresent(callback -> callback.setReturnValue(false));
        }
        final BlockSnapshot blockSnapshot = phaseContext.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Could not find a piston!", phaseContext));
        phaseContext.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(items -> {
                    final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                            .block(blockSnapshot)
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build());
                    phaseContext.getNotifier()
                            .ifPresent(builder::notifier);
                    phaseContext.getOwner()
                            .ifPresent(builder::owner);

                    final Cause cause = builder
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
                });
        phaseContext.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                            .block(blockSnapshot)
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build());
                    phaseContext.getNotifier()
                            .ifPresent(builder::notifier);
                    phaseContext.getOwner()
                            .ifPresent(builder::owner);

                    final Cause cause = builder
                            .build();
                    final SpawnEntityEvent
                            event =
                            SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld());
                    SpongeImpl.postEvent(event);
                    final User user = phaseContext.getNotifier().orElseGet(() -> phaseContext.getOwner().orElse(null));
                    if (!event.isCancelled()) {
                        for (Entity entity : event.getEntities()) {
                            if (user != null) {
                                EntityUtil.toMixin(entity).setCreator(user.getUniqueId());
                            }
                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                        }
                    }
                });
    }
}
