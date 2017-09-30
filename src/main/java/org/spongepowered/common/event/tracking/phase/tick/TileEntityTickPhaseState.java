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
import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class TileEntityTickPhaseState extends LocationBasedTickPhaseState<TileEntityTickContext> {

    TileEntityTickPhaseState() {
    }

    @Override
    public TileEntityTickContext createPhaseContext() {
        return new TileEntityTickContext()
                .addEntityCaptures()
                .addBlockCaptures();
    }

    @Override
    Location<World> getLocationSourceFromContext(PhaseContext<?> context) {
        return context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over a TileEntity!", context))
                .getLocation();
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(PhaseContext<?> context) {
        return context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over a TileEntity!", context))
                .getLocatableBlock();
    }

    @Override
    public void unwind(TileEntityTickContext phaseContext) {
        final TileEntity tickingTile = phaseContext.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", phaseContext));
        final Optional<User> notifier = phaseContext.getNotifier();
        final Optional<User> owner = phaseContext.getOwner();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blockSnapshots -> {
                        TrackingUtil.processBlockCaptures(blockSnapshots, this, phaseContext);
                    });
            Sponge.getCauseStackManager().pushCause(tickingTile.getLocatableBlock());
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.BLOCK_SPAWNING);
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : entities) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        final SpawnEntityEvent
                                spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), capturedEntities);
                        SpongeImpl.postEvent(spawnEntityEvent);
                        if (!spawnEntityEvent.isCancelled()) {
                            for (Entity entity : spawnEntityEvent.getEntities()) {
                                if (entityCreator != null) {
                                    EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                                }
                                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                            }
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception occured while processing tile entity %s at %s", tickingTile, tickingTile.getLocation()), e);
        }
    }

    @Override
    public void appendExplosionContext(PhaseContext<?> explosionContext, PhaseContext<?> context) {
        context.getOwner().ifPresent(explosionContext::owner);
        context.getNotifier().ifPresent(explosionContext::notifier);
        final TileEntity tickingTile = context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
        explosionContext.source(tickingTile);
    }

    @Override
    public boolean spawnEntityOrCapture(TileEntityTickContext context, Entity entity, int chunkX, int chunkZ) {
        final TileEntity tickingTile = context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
        final Optional<User> notifier = context.getNotifier();
        final Optional<User> owner = context.getOwner();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        final IMixinTileEntity mixinTileEntity = (IMixinTileEntity) tickingTile;
        // Separate experience from other entities
        if (entity instanceof EntityXPOrb) {
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                Sponge.getCauseStackManager().pushCause(tickingTile.getLocatableBlock());
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.EXPERIENCE);
                context.addNotifierAndOwnerToCauseStack();
                final ArrayList<Entity> exp = new ArrayList<>();
                exp.add(entity);
                final SpawnEntityEvent event =
                        SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), exp);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity anEntity : event.getEntities()) {
                        if (entityCreator != null) {
                            EntityUtil.toMixin(anEntity).setCreator(entityCreator.getUniqueId());
                        }
                        EntityUtil.getMixinWorld(entity).forceSpawnEntity(anEntity);
                    }
                    return true;
                }
            }
            return false;
        }
        final List<Entity> nonExpEntities = new ArrayList<>(1);
        nonExpEntities.add(entity);
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(tickingTile.getLocatableBlock());
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, mixinTileEntity.getTickedSpawnType());
            context.addNotifierAndOwnerToCauseStack();
            final SpawnEntityEvent
                    spawnEntityEvent =
                    SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), nonExpEntities);
            SpongeImpl.postEvent(spawnEntityEvent);
            if (!spawnEntityEvent.isCancelled()) {
                for (Entity anEntity : spawnEntityEvent.getEntities()) {
                    if (entityCreator != null) {
                        EntityUtil.toMixin(anEntity).setCreator(entityCreator.getUniqueId());
                    }
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(anEntity);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TileEntityTickPhase";
    }
}
