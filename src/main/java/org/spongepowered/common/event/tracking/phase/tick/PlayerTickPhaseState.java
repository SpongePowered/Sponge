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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;

class PlayerTickPhaseState extends TickPhaseState {

    PlayerTickPhaseState() {
    }

    @Override
    public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
        blockEvent.setSourceUser(context.getSource(Player.class).get());
    }

    @Override
    public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
        final Player player = phaseContext.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Player!", phaseContext));
        phaseContext.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
            final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                    .entity(player)
                    .type(InternalSpawnTypes.PASSIVE)
                    .build());
            final SpawnEntityEvent
                    spawnEntityEvent =
                    SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, causeTracker.getWorld());
            SpongeImpl.postEvent(spawnEntityEvent);
            for (Entity entity : spawnEntityEvent.getEntities()) {
                EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                causeTracker.getMixinWorld().forceSpawnEntity(entity);
            }
        });
        phaseContext.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
            final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                    .entity(player)
                    .type(InternalSpawnTypes.DROPPED_ITEM)
                    .build());
            final ArrayList<Entity> capturedEntities = new ArrayList<>();
            for (EntityItem entity : entities) {
                capturedEntities.add(EntityUtil.fromNative(entity));
            }

            final SpawnEntityEvent
                    spawnEntityEvent =
                    SpongeEventFactory.createSpawnEntityEvent(builder.build(), capturedEntities, causeTracker.getWorld());
            SpongeImpl.postEvent(spawnEntityEvent);
            for (Entity entity : spawnEntityEvent.getEntities()) {
                EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                causeTracker.getMixinWorld().forceSpawnEntity(entity);
            }
        });
        phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blockSnapshots -> {
            TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
        });
    }

    @Override
    public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
        builder.named(NamedCause.OWNER, context.getSource(Player.class).get());
    }

    @Override
    public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {
        final Player player = context.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
        explosionContext.owner(player);
        explosionContext.notifier(player);
        explosionContext.add(NamedCause.source(player));
    }

    @Override
    public String toString() {
        return "PlayerTickPhase";
    }
}
