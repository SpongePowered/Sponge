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
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

class BlockEventTickPhaseState extends TickPhaseState {

    BlockEventTickPhaseState() {
    }

    @Override
    public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
        WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        if (sourcePos == null) {
            LocatableBlock locatableBlock =  context.getSource(LocatableBlock.class).orElse(null);
            if (locatableBlock == null) {
                TileEntity tileEntity = context.getSource(TileEntity.class).orElseThrow(TrackingUtil.throwWithContext(
                        "Expected to be ticking over at a TileEntity!", context));
                locatableBlock = tileEntity.getLocatableBlock();
            }
            sourcePos = ((IMixinLocation)(Object) locatableBlock.getLocation()).getBlockPos();
        }
        final User user = context.getNotifier().orElse(TrackingUtil.getNotifierOrOwnerFromBlock(minecraftWorld, sourcePos));
        if (user != null) {
            final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos);
            mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
        }
    }

    @Override
    public boolean spawnEntityOrCapture(PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final Optional<User> notifier = context.getNotifier();
        final Optional<User> owner = context.getOwner();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        final Cause cause = Cause.source(InternalSpawnTypes.SpawnCauses.CUSTOM_SPAWN)
                .build();

        final List<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        final SpawnEntityEvent
                spawnEntityEvent =
                SpongeEventFactory.createSpawnEntityEvent(cause, entities);
        SpongeImpl.postEvent(spawnEntityEvent);
        if (!spawnEntityEvent.isCancelled()) {
            for (Entity anEntity : spawnEntityEvent.getEntities()) {
                if (entityCreator != null) {
                    EntityUtil.toMixin(anEntity).setCreator(entityCreator.getUniqueId());
                }
                EntityUtil.getMixinWorld(anEntity).forceSpawnEntity(anEntity);
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleBlockChangeWithUser(@Nullable BlockChange blockChange,
        Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {
        final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
        final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
        final Vector3d changedPosition = changedLocation.getPosition();
        final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
        final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
        final User user = TrackingUtil.getNotifierOrOwnerFromBlock(changedLocation);
        if (user != null) {
            changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER);
        }
    }

    @Override
    public void processPostTick(PhaseContext phaseContext) {
        final Optional<User> notifier = phaseContext.getNotifier();
        final Optional<User> owner = phaseContext.getOwner();
        final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
        phaseContext.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, this, phaseContext));
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
                            SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities);
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
        LocatableBlock locatable =  context.getSource(LocatableBlock.class).orElse(null);
        if (locatable == null) {
            TileEntity tileEntity = context.getSource(TileEntity.class).orElseThrow(TrackingUtil.throwWithContext(
                    "Expected to be ticking over at a TileEntity!", context));
            locatable = tileEntity.getLocatableBlock();
        }
        builder.named(NamedCause.notifier(locatable));
    }

    @Override
    public String toString() {
        return "BlockEventTickPhase";
    }
}
