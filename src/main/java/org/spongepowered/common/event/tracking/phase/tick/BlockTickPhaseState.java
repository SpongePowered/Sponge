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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
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
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class BlockTickPhaseState extends LocationBasedTickPhaseState<BlockTickContext> {

    private final String name;

    BlockTickPhaseState(String name) {
        this.name = name;
    }

    @Override
    public BlockTickContext createPhaseContext() {
        return new BlockTickContext(this)
                .addCaptures();
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(PhaseContext<?> context) {
        return context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
    }

    @Override
    Location<World> getLocationSourceFromContext(PhaseContext<?> context) {
        return context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context)).getLocation();
    }

    @Override
    public void unwind(BlockTickContext context) {
        final LocatableBlock locatableBlock = context.requireSource(LocatableBlock.class);
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(locatableBlock);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);
            final User entityCreator = context.getNotifier().orElseGet(() -> context.getOwner().orElse(null));
            context.getCapturedBlockSupplier()
                    .acceptAndClearIfNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, this, context));
            context.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(items -> {
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : items) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        final SpawnEntityEvent spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), capturedEntities);
                        SpongeImpl.postEvent(spawnEntityEvent);
                        for (Entity entity : spawnEntityEvent.getEntities()) {
                            if (entityCreator != null) {
                                EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                            }
                            EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                        }
                    });
        }
    }

    @Override
    public void appendContextPreExplosion(ExplosionContext explosionContext, BlockTickContext context) {
        context.getOwner().ifPresent(explosionContext::owner);
        context.getNotifier().ifPresent(explosionContext::notifier);
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        explosionContext.source(locatableBlock);
    }

    @Override
    public boolean spawnEntityOrCapture(BlockTickContext context, Entity entity, int chunkX, int chunkZ) {
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        final Optional<User> owner = context.getOwner();
        final Optional<User> notifier = context.getNotifier();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(locatableBlock);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.EXPERIENCE);
            if (notifier.isPresent()) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier.get());
            }
            if (owner.isPresent()) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner.get());
            }
            if (entity instanceof EntityXPOrb) {
                final ArrayList<Entity> entities = new ArrayList<>(1);
                entities.add(entity);
                final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
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
                return false;
            }
            final List<Entity> nonExpEntities = new ArrayList<>(1);
            nonExpEntities.add(entity);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.BLOCK_SPAWNING);
            final SpawnEntityEvent spawnEntityEvent =
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
    public void postTrackBlock(BlockSnapshot snapshot, PhaseTracker tracker, BlockTickContext context) {
        if (context.shouldProcessImmediately()) {
            TrackingUtil.processBlockCaptures(context.getCapturedBlocks(), this, context);
            context.getCapturedBlockSupplier().get().remove(snapshot);
        }

    }

    @Override
    public String toString() {
        return this.name;
    }

}
