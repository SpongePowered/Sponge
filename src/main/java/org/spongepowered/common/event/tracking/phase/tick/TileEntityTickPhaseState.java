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
import org.spongepowered.api.block.tileentity.TileEntity;
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
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class TileEntityTickPhaseState extends LocationBasedTickPhaseState {

    TileEntityTickPhaseState() {
    }

    @Override
    Location<World> getLocationSourceFromContext(PhaseContext context) {
        return context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over a TileEntity!", context))
                .getLocation();
    }

    @Override
    public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
        final TileEntity tickingTile = phaseContext.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", phaseContext));
        final PhaseContext.CaptureBlockSnapshotForTile capturedSnapshot = phaseContext.getTileSnapshot();
        final Optional<User> notifier = phaseContext.getNotifier();
        final Optional<User> owner = phaseContext.getOwner();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));

        try {

            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blockSnapshots -> {
                        TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                    });
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final Cause cause = Cause.source(BlockSpawnCause.builder()
                                .block(capturedSnapshot.getSnapshot())
                                .type(InternalSpawnTypes.BLOCK_SPAWNING)
                                .build())
                                .build();
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : entities) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        final SpawnEntityEvent
                                spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(spawnEntityEvent);
                        if (!spawnEntityEvent.isCancelled()) {
                            for (Entity entity : spawnEntityEvent.getEntities()) {
                                if (entityCreator != null) {
                                    EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                                }
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception occured while processing tile entity %s at %s", tickingTile, tickingTile.getLocation()), e);
        }
    }

    @Override
    public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
        final TileEntity tickingTile = context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
        builder.named(NamedCause.notifier(tickingTile));
    }

    @Override
    public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
        final TileEntity tickingTile = context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking a block, but found none!", context));
        blockEvent.setCurrentTickTileEntity(tickingTile);
        final Location<World> blockLocation = tickingTile.getLocation();
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
        final TileEntity tickingTile = context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
        explosionContext.add(NamedCause.source(tickingTile));
    }

    @Override
    public boolean spawnEntityOrCapture(CauseTracker causeTracker, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final TileEntity tickingTile = context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
        final PhaseContext.CaptureBlockSnapshotForTile capturedSnapshot = context.getTileSnapshot();
        final Optional<User> notifier = context.getNotifier();
        final Optional<User> owner = context.getOwner();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        final IMixinTileEntity mixinTileEntity = (IMixinTileEntity) tickingTile;
        // Separate experience from other entities
        if (entity instanceof EntityXPOrb) {
            final Cause.Builder builder = Cause.builder();
            builder.named(NamedCause.source(
                    BlockSpawnCause.builder()
                            .block(capturedSnapshot.getSnapshot())
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build()
                    )
            );
            notifier.ifPresent(builder::notifier);
            owner.ifPresent(builder::owner);
            final ArrayList<Entity> exp = new ArrayList<>();
            exp.add(entity);
            final SpawnEntityEvent event =
                    SpongeEventFactory.createSpawnEntityEvent(builder.build(), exp, causeTracker.getWorld());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity anEntity : event.getEntities()) {
                    if (entityCreator != null) {
                        EntityUtil.toMixin(anEntity).setCreator(entityCreator.getUniqueId());
                    }
                    causeTracker.getMixinWorld().forceSpawnEntity(anEntity);
                }
                return true;
            }
            return false;
        }
        final List<Entity> nonExpEntities = new ArrayList<>(1);
        nonExpEntities.add(entity);

        final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                .block(capturedSnapshot.getSnapshot())
                .type(mixinTileEntity.getTickedSpawnType())
                .build());
        notifier.ifPresent(builder::notifier);
        owner.ifPresent(builder::owner);
        final Cause cause = builder.build();
        final SpawnEntityEvent
                spawnEntityEvent =
                SpongeEventFactory.createSpawnEntityEvent(cause, nonExpEntities, causeTracker.getWorld());
        SpongeImpl.postEvent(spawnEntityEvent);
        if (!spawnEntityEvent.isCancelled()) {
            for (Entity anEntity : spawnEntityEvent.getEntities()) {
                if (entityCreator != null) {
                    EntityUtil.toMixin(anEntity).setCreator(entityCreator.getUniqueId());
                }
                causeTracker.getMixinWorld().forceSpawnEntity(anEntity);
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TileEntityTickPhase";
    }
}
