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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;

class PlayerTickPhaseState extends TickPhaseState<PlayerTickContext> {

    PlayerTickPhaseState() {
    }

    @Override
    public PlayerTickContext createPhaseContext() {
        return new PlayerTickContext()
                .addCaptures()
                .addEntityDropCaptures()
                ;
    }

    @Override
    public void unwind(PlayerTickContext phaseContext) {
        final Player player = phaseContext.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Player!", phaseContext));
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PASSIVE);
            phaseContext.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                final SpawnEntityEvent spawnEntityEvent =
                        SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
                SpongeImpl.postEvent(spawnEntityEvent);
                for (Entity entity : spawnEntityEvent.getEntities()) {
                    EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                }
            });
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);
            phaseContext.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                final ArrayList<Entity> capturedEntities = new ArrayList<>();
                for (EntityItem entity : entities) {
                    capturedEntities.add(EntityUtil.fromNative(entity));
                }

                final SpawnEntityEvent spawnEntityEvent =
                        SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), capturedEntities);
                SpongeImpl.postEvent(spawnEntityEvent);
                for (Entity entity : spawnEntityEvent.getEntities()) {
                    EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                }
            });
            phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blockSnapshots -> {
                TrackingUtil.processBlockCaptures(blockSnapshots, this, phaseContext);
            });
        }
    }

    @Override
    public void appendExplosionContext(PhaseContext<?> explosionContext, PhaseContext<?> context) {
        final Player player = context.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
        explosionContext.owner(player);
        explosionContext.notifier(player);
        explosionContext.source(player);
    }

    @Override
    public boolean spawnEntityOrCapture(PlayerTickContext context, Entity entity, int chunkX, int chunkZ) {
        final Player player = context.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Player!", context));
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PASSIVE);
            final List<Entity> entities = new ArrayList<>(1);
            entities.add(entity);
            final SpawnEntityEvent spawnEntityEvent =
                    SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
            SpongeImpl.postEvent(spawnEntityEvent);
            if (!spawnEntityEvent.isCancelled()) {
                for (Entity anEntity : spawnEntityEvent.getEntities()) {
                    EntityUtil.toMixin(anEntity).setCreator(player.getUniqueId());
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(anEntity);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "PlayerTickPhase";
    }
}
