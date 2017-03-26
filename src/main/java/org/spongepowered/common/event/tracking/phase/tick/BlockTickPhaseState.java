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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.LocatableBlockSpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class BlockTickPhaseState extends LocationBasedTickPhaseState {

    private final String name;

    BlockTickPhaseState(String name) {
        this.name = name;
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(PhaseContext context) {
        return context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
    }

    @Override
    Location<World> getLocationSourceFromContext(PhaseContext context) {
        return context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context)).getLocation();
    }

    @Override
    public void processPostTick(PhaseContext context) {
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        final Optional<User> owner = context.getOwner();
        final Optional<User> notifier = context.getNotifier();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, this, context));
        context.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(items -> {
                    final Cause cause = Cause.source(LocatableBlockSpawnCause.builder()
                            .locatableBlock(locatableBlock)
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build())
                            .build();
                    final ArrayList<Entity> capturedEntities = new ArrayList<>();
                    for (EntityItem entity : items) {
                        capturedEntities.add(EntityUtil.fromNative(entity));
                    }
                    final SpawnEntityEvent
                            spawnEntityEvent =
                            SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, locatableBlock.getWorld());
                    SpongeImpl.postEvent(spawnEntityEvent);
                    for (Entity entity : spawnEntityEvent.getEntities()) {
                        if (entityCreator != null) {
                            EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                        }
                        EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                    }
                });
    }

    @Override
    public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder) {
        builder.named(NamedCause.notifier(getLocatableBlockSourceFromContext(context)));
    }

    @Override
    public void associateBlockEventNotifier(PhaseContext context, BlockPos pos, IMixinBlockEventData blockEvent) {
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        blockEvent.setTickBlock(locatableBlock);
        final Location<World> location = locatableBlock.getLocation();
        final WorldServer worldServer = (WorldServer)  location.getExtent();
        final Vector3d blockPosition =  location.getPosition();
        final BlockPos blockPos = VecHelper.toBlockPos(blockPosition);
        final IMixinChunk mixinChunk = (IMixinChunk) worldServer.getChunkFromBlockCoords(blockPos);
        mixinChunk.getBlockNotifier(blockPos).ifPresent(blockEvent::setSourceUser);
        context.firstNamed(NamedCause.NOTIFIER, User.class).ifPresent(blockEvent::setSourceUser);
    }

    @Override
    public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {
        context.getOwner().ifPresent(explosionContext::owner);
        context.getNotifier().ifPresent(explosionContext::notifier);
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        explosionContext.add(NamedCause.source(locatableBlock));
    }

    @Override
    public boolean spawnEntityOrCapture(PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        final Optional<User> owner = context.getOwner();
        final Optional<User> notifier = context.getNotifier();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        if (entity instanceof EntityXPOrb) {
            final ArrayList<Entity> entities = new ArrayList<>(1);
            entities.add(entity);
            final Cause.Builder builder = Cause.builder();
            builder.named(NamedCause.source(LocatableBlockSpawnCause.builder()
                    .locatableBlock(locatableBlock)
                    .type(InternalSpawnTypes.EXPERIENCE)
                    .build()));
            notifier.ifPresent(builder::notifier);
            owner.ifPresent(builder::owner);
            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, locatableBlock.getWorld());
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
        final Cause.Builder builder = Cause.source(LocatableBlockSpawnCause.builder()
                .locatableBlock(locatableBlock)
                .type(InternalSpawnTypes.BLOCK_SPAWNING)
                .build());
        notifier.ifPresent(builder::notifier);
        owner.ifPresent(builder::owner);
        final Cause cause = builder.build();
        final SpawnEntityEvent
                spawnEntityEvent =
                SpongeEventFactory.createSpawnEntityEvent(cause, nonExpEntities, locatableBlock.getWorld());
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
        return false;
    }

    @Override
    public void postTrackBlock(BlockSnapshot snapshot, CauseTracker tracker, PhaseContext context) {
        boolean processImmediately = context.firstNamed(InternalNamedCauses.Tracker.PROCESS_IMMEDIATELY, Boolean.class).get();
        if (processImmediately) {
            TrackingUtil.processBlockCaptures(context.getCapturedBlocks(), this, context);
            context.getCapturedBlockSupplier().get().remove(snapshot);
        }

    }

    @Override
    public String toString() {
        return this.name;
    }
}
