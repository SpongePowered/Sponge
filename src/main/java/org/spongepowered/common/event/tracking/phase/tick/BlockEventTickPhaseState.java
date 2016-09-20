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
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

class BlockEventTickPhaseState extends TickPhaseState {

    BlockEventTickPhaseState() {
    }

    @Override
    public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        context.getSource(BlockSnapshot.class).ifPresent(snapshot -> {
            final Location<World> location = snapshot.getLocation().get();
            TrackingUtil.getNotifierOrOwnerFromBlock(location)
                    .ifPresent(user -> {
                        final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos);
                        mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
                    });
        });
    }

    @Override
    public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {
        final Location<World> location = Stream.<Supplier<Optional<Location<World>>>>
                of(
                () -> context.getSource(BlockSnapshot.class).map(snapshot -> snapshot.getLocation().get()),
                () -> context.getSource(TileEntity.class).map(Locatable::getLocation),
                () -> context.getSource(IMixinBlockEventData.class).map(data ->
                        new Location<>((World) minecraftWorld, VecHelper.toVector3d(data.getEventBlockPosition())))
        )
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be throwing a block event for a tile entity or a snapshot but got none!",
                        context));
        final Vector3d position = location.getPosition();
        final BlockPos sourcePos = VecHelper.toBlockPos(position);
        final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
        final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
        final Vector3d changedPosition = changedLocation.getPosition();
        final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
        final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
        TrackingUtil.getNotifierOrOwnerFromBlock(changedLocation)
                .ifPresent(user -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
    }

    @Override
    public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
        final Optional<User> notifier = phaseContext.getNotifier();
        final Optional<User> owner = phaseContext.getOwner();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        phaseContext.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
        phaseContext.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final Cause cause = Cause.source(InternalSpawnTypes.SpawnCauses.CUSTOM_SPAWN)
                            .build();
                    final SpawnEntityEvent
                            spawnEntityEvent =
                            SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld());
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
                    final Cause cause = Cause.source(InternalSpawnTypes.SpawnCauses.CUSTOM_SPAWN)
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
        final Optional<BlockSnapshot> blockSnapshot = context.getSource(BlockSnapshot.class);
        final Optional<TileEntity> tileEntity = context.getSource(TileEntity.class);
        if (blockSnapshot.isPresent()) {
            builder.named(NamedCause.notifier(blockSnapshot.get()));
        } else if (tileEntity.isPresent()) {
            builder.named(NamedCause.notifier(tileEntity.get()));
        }

    }

    @Override
    public String toString() {
        return "BlockEventTickPhase";
    }
}
