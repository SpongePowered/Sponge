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
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
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
    Location<World> getLocationSourceFromContext(PhaseContext context) {
        return context.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over a block!", context))
                .getLocation()
                .get();
    }

    @Override
    public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
        final BlockSnapshot tickingBlock = phaseContext.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Block!", phaseContext));
        final Optional<User> owner = phaseContext.getOwner();
        final Optional<User> notifier = phaseContext.getNotifier();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        phaseContext.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
        phaseContext.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    // Separate experience from other entities
                    final List<Entity> experience = new ArrayList<>(entities.size());
                    final List<Entity> nonExpEntities = new ArrayList<>(entities.size());
                    for (Entity entity : entities) {
                        if (entity instanceof EntityXPOrb) {
                            experience.add(entity);
                            continue;
                        }
                        nonExpEntities.add(entity);
                    }
                    if (!experience.isEmpty()) {
                        final Cause.Builder builder = Cause.builder();
                        builder.named(NamedCause.source(BlockSpawnCause.builder()
                                .block(tickingBlock)
                                .type(InternalSpawnTypes.EXPERIENCE)
                                .build()));
                        notifier.ifPresent(builder::notifier);
                        owner.ifPresent(builder::owner);
                        SpongeEventFactory.createSpawnEntityEvent(builder.build(), experience, causeTracker.getWorld());
                    }
                    final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                            .block(tickingBlock)
                            .type(InternalSpawnTypes.BLOCK_SPAWNING)
                            .build());
                    notifier.ifPresent(builder::notifier);
                    owner.ifPresent(builder::owner);
                    final Cause cause = builder.build();
                    final SpawnEntityEvent
                            spawnEntityEvent =
                            SpongeEventFactory.createSpawnEntityEvent(cause, nonExpEntities, causeTracker.getWorld());
                    SpongeImpl.postEvent(spawnEntityEvent);
                    for (Entity entity : spawnEntityEvent.getEntities()) {
                        if (entityCreator != null) {
                            EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                        }
                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                    }
                });
        phaseContext.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(items -> {
                    final Cause cause = Cause.source(BlockSpawnCause.builder()
                            .block(tickingBlock)
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build())
                            .build();
                    final ArrayList<Entity> capturedEntities = new ArrayList<>();
                    for (EntityItem entity : items) {
                        capturedEntities.add(EntityUtil.fromNative(entity));
                    }
                    final SpawnEntityEvent
                            spawnEntityEvent =
                            SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld());
                    SpongeImpl.postEvent(spawnEntityEvent);
                    for (Entity entity : spawnEntityEvent.getEntities()) {
                        if (entityCreator != null) {
                            EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                        }
                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                    }
                });
    }

    @Override
    public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
        final BlockSnapshot tickingBlock = context.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Block!", context));
        builder.named(NamedCause.notifier(tickingBlock));
    }

    @Override
    public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
        final BlockSnapshot tickingBlock = context.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking a block, but found none!", context));
        blockEvent.setCurrentTickBlock(tickingBlock);
        final Location<World> blockLocation = tickingBlock.getLocation().get();
        final WorldServer worldServer = (WorldServer) blockLocation.getExtent();
        final Vector3d blockPosition = blockLocation.getPosition();
        final BlockPos blockPos = VecHelper.toBlockPos(blockPosition);
        final IMixinChunk mixinChunk = (IMixinChunk) worldServer.getChunkFromBlockCoords(blockPos);
        mixinChunk.getBlockNotifier(blockPos).ifPresent(blockEvent::setSourceUser);
        context.firstNamed(NamedCause.NOTIFIER, User.class).ifPresent(blockEvent::setSourceUser);
    }

    @Override
    public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {
        context.getOwner().ifPresent(explosionContext::owner);
        context.getNotifier().ifPresent(explosionContext::notifier);
        final BlockSnapshot blockSnapshot = context.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking a block", context));
        explosionContext.add(NamedCause.source(blockSnapshot));
    }

    @Override
    public String toString() {
        return this.name;
    }
}
