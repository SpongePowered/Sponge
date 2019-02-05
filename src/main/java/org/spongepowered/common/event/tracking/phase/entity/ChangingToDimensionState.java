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
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import javax.annotation.Nullable;

final class ChangingToDimensionState extends EntityPhaseState<TeleportingContext> {

    ChangingToDimensionState() {
    }

    @Override
    public TeleportingContext createPhaseContext() {
        return new TeleportingContext(this)
            .addBlockCaptures()
            .addEntityCaptures();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(TeleportingContext context) {
    }

    @Override
    public boolean tracksBlockSpecificDrops(TeleportingContext context) {
        return true;
    }

    @Override
    public boolean spawnEntityOrCapture(TeleportingContext context, Entity entity, int chunkX, int chunkZ) {
        final WorldServer worldServer = context.getTargetWorld();
        // Allowed to use the force spawn because it's the same "entity"
        ((IMixinWorldServer) worldServer).forceSpawnEntity(entity);
        return true;
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Nullable
    @Override
    public net.minecraft.entity.Entity returnTeleportResult(PhaseContext<?> context, MoveEntityEvent.Teleport.Portal event) {
        final net.minecraft.entity.Entity teleportingEntity = context.getSource(net.minecraft.entity.Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be teleporting an entity!", context));
        // The rest of this is to be handled in the phase.
        if (event.isCancelled()) {
            return null;
        }

        teleportingEntity.world.profiler.startSection("changeDimension");

        WorldServer toWorld = (WorldServer) event.getToTransform().getWorld();

        teleportingEntity.world.removeEntity(teleportingEntity);
        teleportingEntity.removed = false;
        teleportingEntity.world.profiler.startSection("reposition");
        final Vector3d position = event.getToTransform().getPosition();
        teleportingEntity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(),
                (float) event.getToTransform().getPitch());
        toWorld.spawnEntity(teleportingEntity);
        teleportingEntity.world = toWorld;

        toWorld.tickEntity(teleportingEntity, false);
        teleportingEntity.world.profiler.endStartSection("reloading");

        teleportingEntity.world.profiler.endSection();
        teleportingEntity.world.profiler.endSection();
        return teleportingEntity;
    }
}
