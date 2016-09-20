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
package org.spongepowered.common.event.tracking.phase.entity;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;

import javax.annotation.Nullable;

final class ChangingToDimensionState extends EntityPhaseState {

    ChangingToDimensionState() {
    }

    @SuppressWarnings("unchecked")
    @Override
    void unwind(CauseTracker causeTracker, PhaseContext context) {
//                final MoveEntityEvent.Teleport.Portal portalEvent = context.firstNamed(InternalNamedCauses.Teleporting.TELEPORT_EVENT, MoveEntityEvent.Teleport.Portal.class)
//                                .orElseThrow(PhaseUtil.throwWithContext("Expected to capture a portal event!", context));
//
//                // Throw our event now
//                SpongeImpl.postEvent(portalEvent);
//
//                final IMixinEntity mixinEntity = context.getSource(IMixinEntity.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be teleporting an entity!", context));
//                final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(mixinEntity);
//
//                // Reset the player connection to allow position update packets
//                if (minecraftEntity instanceof EntityPlayerMP) {
//                    ((IMixinNetHandlerPlayServer) ((EntityPlayerMP) minecraftEntity).connection).setAllowClientLocationUpdate(true);
//                }
//
//                final Vector3i chunkPosition = mixinEntity.getLocation().getChunkPosition();
//
//
//                final Teleporter targetTeleporter = context.firstNamed(InternalNamedCauses.Teleporting.TARGET_TELEPORTER, Teleporter.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a targetTeleporter for a teleportation!", context));
//
//                final Transform<World> fromTransform = (Transform<World>) context.firstNamed(InternalNamedCauses.Teleporting.FROM_TRANSFORM, Transform.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing an origination Transform!", context));
//
//                final IMixinTeleporter targetMixinTeleporter = (IMixinTeleporter) targetTeleporter;
//                if (portalEvent.isCancelled()) {
//                    targetMixinTeleporter.removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
//                    mixinEntity.setLocationAndAngles(fromTransform);
//                    return;
//                }
//
//                final Transform targetTransform = context.firstNamed(InternalNamedCauses.Teleporting.TARGET_TRANSFORM, Transform.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a target Transform!", context));
//
//                final WorldServer targetWorldServer = context.firstNamed(InternalNamedCauses.Teleporting.TARGET_WORLD, WorldServer.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a target WorldServer!", context));
//                final WorldServer fromWorldServer = context.firstNamed(InternalNamedCauses.Teleporting.FROM_WORLD, WorldServer.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing an origination WorldServer!", context));
//                // Plugins may change transforms on us. Gotta reset the targetTeleporter cache
//                final Transform<World> eventTargetTransform = portalEvent.getToTransform();
//                if (!targetTransform.equals(eventTargetTransform)) {
//
//                    if (fromWorldServer == eventTargetTransform.getExtent()) {
//                        portalEvent.setCancelled(true);
//
//                        targetMixinTeleporter.removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
//                        mixinEntity.setLocationAndAngles(eventTargetTransform);
//                        if (minecraftEntity instanceof EntityPlayerMP) {
//                            final EntityPlayerMP minecraftPlayer = (EntityPlayerMP) minecraftEntity;
//                            // close any open inventory
//                            minecraftPlayer.closeScreen();
//                            // notify client
//                            minecraftPlayer.connection.setPlayerLocation(minecraftPlayer.posX, minecraftPlayer.posY, minecraftPlayer.posZ,
//                                    minecraftPlayer.rotationYaw, minecraftPlayer.rotationPitch);
//                        }
//                        return;
//                    }
//                } else {
//                    if (targetWorldServer.provider instanceof WorldProviderEnd) {
//                        final BlockPos blockpos = minecraftEntity.worldObj.getTopSolidOrLiquidBlock(targetWorldServer.getSpawnPoint());
//                        minecraftEntity.moveToBlockPosAndAngles(blockpos, minecraftEntity.rotationYaw, minecraftEntity.rotationPitch);
//                    }
//                }
//
//                final IMixinWorldServer targetMixinWorldServer = (IMixinWorldServer) targetWorldServer;
//                final List<BlockSnapshot> capturedBlocks = context.getCapturedBlocks();
//                final CauseTracker targetCauseTracker = targetMixinWorldServer.getCauseTracker();
//                if (capturedBlocks.isEmpty()
//                    || !GeneralFunctions.processBlockCaptures(capturedBlocks, targetCauseTracker, State.CHANGING_TO_DIMENSION, context)) {
//                    targetMixinTeleporter.removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
//                }
//
//                if (!portalEvent.getKeepsVelocity()) {
//                    minecraftEntity.motionX = 0;
//                    minecraftEntity.motionY = 0;
//                    minecraftEntity.motionZ = 0;
//                }
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return true;
    }

    @Nullable
    @Override
    public net.minecraft.entity.Entity returnTeleportResult(PhaseContext context, MoveEntityEvent.Teleport.Portal event) {
        final net.minecraft.entity.Entity teleportingEntity = context.getSource(net.minecraft.entity.Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be teleporting an entity!", context));
        // The rest of this is to be handled in the phase.
        if (event.isCancelled()) {
            return null;
        }

        teleportingEntity.worldObj.theProfiler.startSection("changeDimension");

        WorldServer toWorld = (WorldServer) event.getToTransform().getExtent();

        teleportingEntity.worldObj.removeEntity(teleportingEntity);
        teleportingEntity.isDead = false;
        teleportingEntity.worldObj.theProfiler.startSection("reposition");
        final Vector3d position = event.getToTransform().getPosition();
        teleportingEntity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(),
                (float) event.getToTransform().getPitch());
        toWorld.spawnEntityInWorld(teleportingEntity);
        teleportingEntity.worldObj = toWorld;

        toWorld.updateEntityWithOptionalForce(teleportingEntity, false);
        teleportingEntity.worldObj.theProfiler.endStartSection("reloading");

        teleportingEntity.worldObj.theProfiler.endSection();
        teleportingEntity.worldObj.theProfiler.endSection();
        return teleportingEntity;
    }
}
