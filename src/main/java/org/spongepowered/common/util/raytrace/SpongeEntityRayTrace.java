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
package org.spongepowered.common.util.raytrace;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SpongeEntityRayTrace extends AbstractSpongeRayTrace<@NonNull Entity> {

    private static final Predicate<Entity> DEFAULT_FILTER = entity -> true;

    public SpongeEntityRayTrace() {
        super(SpongeEntityRayTrace.DEFAULT_FILTER);
    }

    @Override
    boolean requiresEntityTracking() {
        return true;
    }

    @Override
    List<net.minecraft.world.entity.Entity> selectEntities(final ServerWorld serverWorld, final AABB targetAABB) {
        return ((Level) serverWorld).getEntities((net.minecraft.world.entity.Entity) null, targetAABB, (Predicate) this.select);
    }

    @Override
    final Optional<RayTraceResult<@NonNull Entity>> testSelectLocation(final ServerWorld serverWorld, final Vec3 vec3din, final Vec3 vec3dend) {
        double currentSqDist = Double.MAX_VALUE;
        RayTraceResult<@NonNull Entity> returnedEntity = null;
        final LocatableBlock locatableBlock = this.getBlock(serverWorld, vec3din, vec3dend);
        for (final net.minecraft.world.entity.Entity entity : this.selectEntities(serverWorld, this.getBlockAABB(locatableBlock.getBlockPosition()))) {
            final Optional<Vec3> vec3d = entity.getBoundingBox().clip(vec3din, vec3dend);
            if (vec3d.isPresent()) {
                final Vec3 hitPosition = vec3d.get();
                final double sqdist = hitPosition.distanceToSqr(vec3din);
                if (sqdist < currentSqDist) {
                    currentSqDist = sqdist;
                    returnedEntity = new SpongeRayTraceResult<>((Entity) entity, VecHelper.toVector3d(hitPosition));
                }
            }
        }
        return Optional.ofNullable(returnedEntity);
    }

    @Override final boolean shouldCheckFailures() {
        return true;
    }

}
