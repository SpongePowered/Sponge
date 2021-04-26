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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public abstract class AbstractSpongeRayTrace<T extends Locatable> implements RayTrace<@NonNull T> {

    private final Predicate<T> defaultFilter;

    int limit = 30;
    @Nullable Vector3d start;
    @Nullable Vector3d direction;
    @Nullable Vector3d end;
    @Nullable ResourceKey world;
    @Nullable Predicate<T> select;
    @Nullable Predicate<LocatableBlock> continueWhileBlock = null;
    @Nullable Predicate<Entity> continueWhileEntity = null;
    @Nullable Predicate<ServerLocation> continueWhileLocation = null;

    AbstractSpongeRayTrace(final Predicate<T> defaultFilter) {
        this.defaultFilter = defaultFilter;
        this.select = defaultFilter;
    }

    @Override
    public final @NonNull RayTrace<@NonNull T> world(final @NonNull ServerWorld serverWorld) {
        this.world = serverWorld.key();
        return this;
    }

    @Override
    public @NonNull RayTrace<@NonNull T> sourceEyePosition(final @NonNull Living entity) {
        this.sourcePosition(entity.get(Keys.EYE_POSITION)
                .orElseThrow(() -> new IllegalArgumentException("Entity does not have an eye position key")));
        return this.world(entity.serverLocation().world());
    }

    @Override
    public final @NonNull RayTrace<@NonNull T> sourcePosition(final @NonNull Vector3d sourcePosition) {
        this.start = sourcePosition;
        return this;
    }

    @Override
    public @NonNull RayTrace<@NonNull T> direction(final @NonNull Vector3d direction) {
        this.end = null;
        this.direction = direction.normalize();
        return this;
    }

    @Override
    public @NonNull RayTrace<@NonNull T> limit(final int distance) {
        if (distance < 1) {
            throw new IllegalArgumentException("distance limit must be positive");
        }
        this.limit = distance;
        return this;
    }

    @Override
    public final @NonNull RayTrace<@NonNull T> continueUntil(final @NonNull Vector3d endPosition) {
        this.end = endPosition;
        this.direction = null;
        return this;
    }

    @Override
    public @NonNull RayTrace<@NonNull T> continueWhileLocation(final @NonNull Predicate<ServerLocation> continueWhileLocation) {
        if (this.continueWhileLocation == null) {
            this.continueWhileLocation = continueWhileLocation;
        } else {
            this.continueWhileLocation = this.continueWhileLocation.and(continueWhileLocation);
        }
        return this;
    }

    @Override
    public @NonNull RayTrace<@NonNull T> continueWhileBlock(final @NonNull Predicate<LocatableBlock> continueWhileBlock) {
        if (this.continueWhileBlock == null) {
            this.continueWhileBlock = continueWhileBlock;
        } else {
            this.continueWhileBlock = this.continueWhileBlock.and(continueWhileBlock);
        }
        return this;
    }

    @Override
    public @NonNull RayTrace<@NonNull T> continueWhileEntity(final @NonNull Predicate<Entity> continueWhileEntity) {
        if (this.continueWhileEntity == null) {
            this.continueWhileEntity = continueWhileEntity;
        } else {
            this.continueWhileEntity = this.continueWhileEntity.and(continueWhileEntity);
        }
        return this;
    }

    @Override
    public final @NonNull RayTrace<@NonNull T> select(final @NonNull Predicate<T> filter) {
        if (this.select == this.defaultFilter) {
            this.select = filter;
        } else {
            this.select = this.select.or(filter);
        }
        return this;
    }

    @Override
    public @NonNull Optional<RayTraceResult<@NonNull T>> execute() {
        this.setupEnd();

        // get the direction
        final Vector3d directionWithLength = this.end.sub(this.start);
        final double length = directionWithLength.length();
        final Vector3d direction = directionWithLength.normalize();
        if (direction.lengthSquared() == 0) {
            throw new IllegalStateException("The start and end must be two different vectors");
        }

        final ServerWorld serverWorld = Sponge.server().worldManager().world(this.world)
                .orElseThrow(() -> new IllegalStateException("World with key " + this.world.formatted() + " is not loaded!"));

        Vector3i currentBlock = this.initialBlock(direction);
        final Vector3i steps = this.createSteps(direction);

        // The ray equation is, vec(u) + t vec(d). From a point (x, y), there is a t
        // that we need to traverse to get to a boundary. We work that out now...
        TData tData = this.createInitialTData(direction);
        Vector3d currentLocation = new Vector3d(this.start.getX(), this.start.getY(), this.start.getZ());
        final boolean requiresEntityTracking = this.requiresEntityTracking();

        boolean requireAdvancement = true;
        while (requireAdvancement) {
            final net.minecraft.world.phys.Vec3 vec3dstart = VecHelper.toVanillaVector3d(currentLocation);
            // As this iteration is for the CURRENT block location, we need to check where we are with the filter.
            if (this.continueWhileLocation != null && !this.continueWhileLocation.test(ServerLocation.of(serverWorld, currentBlock))) {
                return Optional.empty();
            }
            final Vector3d nextLocation;
            final net.minecraft.world.phys.Vec3 vec3dend;
            if (tData.getTotalTWithNextStep() > length) {
                // This is the last step, we break out of the loop after this set of checks.
                requireAdvancement = false;
                nextLocation = this.end;
                vec3dend = VecHelper.toVanillaVector3d(this.end);
            } else {
                nextLocation = currentLocation.add(
                        direction.getX() * tData.getNextStep(),
                        direction.getY() * tData.getNextStep(),
                        direction.getZ() * tData.getNextStep()
                );
                vec3dend = VecHelper.toVanillaVector3d(nextLocation);
            }

            // Get the selection result.
            final Optional<RayTraceResult<@NonNull T>> result = this.testSelectLocation(serverWorld, vec3dstart, vec3dend);
            if (result.isPresent() && !this.shouldCheckFailures()) {
                // either this is a block ray, so no failures need to be checked, else
                // we return the entity later if there isn't an entity in front of it
                // that's blocking the view.
                return result;
            }

            // Ensure that the block can be travelled through.
            if (!this.shouldAdvanceThroughBlock(serverWorld, vec3dstart, vec3dend)) {
                return Optional.empty();
            }

            // Ensure that the entities in the block can be travelled through.
            if (requiresEntityTracking && this.continueWhileEntity != null) {
                final double resultDistance;
                if (result.isPresent()) {
                    resultDistance = result.get().hitPosition().distanceSquared(currentLocation);
                } else {
                    resultDistance = Double.MAX_VALUE;
                }
                final AABB targetAABB = this.getBlockAABB(currentBlock);
                for (final net.minecraft.world.entity.Entity entity : this.getFailingEntities(serverWorld, targetAABB)) {
                    final Optional<net.minecraft.world.phys.Vec3> vec3d = entity.getBoundingBox().clip(vec3dstart, vec3dend);
                    if (vec3d.isPresent()) {
                        final net.minecraft.world.phys.Vec3 hitPosition = vec3d.get();
                        final double sqdist = hitPosition.distanceToSqr(vec3dstart);
                        if (sqdist < resultDistance) {
                            // We have a failure, so at this point we just bail out and end the trace.
                            return Optional.empty();
                        }
                    }
                }
            }

            // If we still have a result at this point, return it.
            if (result.isPresent()) {
                return result;
            }

            if (requireAdvancement) {
                currentLocation = nextLocation;
                currentBlock = this.getNextBlock(currentBlock, tData, steps);
                tData = this.advance(tData, steps, direction);
            }
        }

        return Optional.empty();
    }

    @Override
    public @NonNull RayTrace<@NonNull T> reset() {
        this.select = this.defaultFilter;
        this.world = null;
        this.start = null;
        this.end = null;
        this.continueWhileBlock = null;
        this.continueWhileEntity = null;
        this.continueWhileLocation = null;
        return this;
    }

    final Vector3i getNextBlock(final Vector3i current, final TData data, final Vector3i steps) {
        return current.add(
                data.nextStepWillAdvanceX() ? steps.getX() : 0,
                data.nextStepWillAdvanceY() ? steps.getY() : 0,
                data.nextStepWillAdvanceZ() ? steps.getX() : 0
        );
    }

    final Vector3i createSteps(final Vector3d direction) {
        return new Vector3i(
                Math.signum(direction.getX()),
                Math.signum(direction.getY()),
                Math.signum(direction.getZ())
        );
    }

    final AABB getBlockAABB(final Vector3i currentBlock) {
        return new AABB(currentBlock.getX(),
                currentBlock.getY(), currentBlock.getZ(), currentBlock.getX() + 1, currentBlock.getY() + 1, currentBlock.getZ() + 1);
    }

    private List<net.minecraft.world.entity.Entity> getFailingEntities(final ServerWorld serverWorld, final AABB targetAABB) {
        return ((Level) serverWorld).getEntities((net.minecraft.world.entity.Entity) null, targetAABB, (Predicate) this.continueWhileEntity.negate());
    }

    boolean requiresEntityTracking() {
        return this.continueWhileEntity != null;
    }

    List<net.minecraft.world.entity.Entity> selectEntities(final ServerWorld serverWorld, final AABB targetAABB) {
        return Collections.emptyList();
    }

    abstract Optional<RayTraceResult<@NonNull T>> testSelectLocation(final ServerWorld serverWorld,
            final net.minecraft.world.phys.Vec3 location,
            final net.minecraft.world.phys.Vec3 exitLocation);

    final LocatableBlock getBlock(final ServerWorld world, final net.minecraft.world.phys.Vec3 in, final net.minecraft.world.phys.Vec3 out) {
        final Vector3i coord = new Vector3i(
                Math.min(in.x, out.x),
                Math.min(in.y, out.y),
                Math.min(in.z, out.z)
        );
        return world.locatableBlock(coord);
    }

    private boolean shouldAdvanceThroughBlock(final ServerWorld serverWorld,
            final net.minecraft.world.phys.Vec3 location,
            final net.minecraft.world.phys.Vec3 exitLocation) {
        if (this.continueWhileBlock == null) {
            return true;
        }

        return this.continueWhileBlock.test(this.getBlock(serverWorld, location, exitLocation));
    }

    boolean shouldCheckFailures() {
        return false;
    }

    final void setupEnd() {
        if (this.start == null) {
            throw new IllegalStateException("start cannot be null");
        }
        if (this.end == null && this.direction == null) {
            throw new IllegalStateException("end cannot be null");
        }
        if (this.world == null) {
            throw new IllegalStateException("world cannot be null");
        }
        if (this.select == null) {
            throw new IllegalStateException("select filter cannot be null");
        }

        if (this.direction != null) {
            this.continueUntil(this.start.add(this.direction.mul(this.limit)));
        }
    }

    final Vector3i initialBlock(final Vector3d direction) {
        return new Vector3i(
                this.start.getX() - (direction.getX() < 0 && this.start.getX() == 0 ? 1 : 0),
                this.start.getY() - (direction.getY() < 0 && this.start.getY() == 0 ? 1 : 0),
                this.start.getZ() - (direction.getZ() < 0 && this.start.getZ() == 0 ? 1 : 0)
        );
    }

    final TData createInitialTData(final Vector3d direction) {
        return new TData(
                0,
                this.getT(this.start.getX(), direction.getX(), this.end.getX()),
                this.getT(this.start.getY(), direction.getY(), this.end.getY()),
                this.getT(this.start.getZ(), direction.getZ(), this.end.getZ())
        );
    }

    final TData advance(final TData data, final Vector3i steps, final Vector3d direction) {
        final double nextStep = data.getNextStep();
        return new TData(
                data.getTotalTWithNextStep(),
                data.nextStepWillAdvanceX() ? steps.getX() / direction.getX() : data.gettToX() - nextStep,
                data.nextStepWillAdvanceY() ? steps.getY() / direction.getY() : data.gettToY() - nextStep,
                data.nextStepWillAdvanceZ() ? steps.getZ() / direction.getZ() : data.gettToZ() - nextStep
        );
    }

    private double getT(final double start, final double direction, final double end) {
        if (direction > 0) {
            return (Math.min(end, Math.ceil(start)) - start) / direction;
        } else if (direction < 0) {
            return (Math.max(end, Math.floor(start)) - start) / direction;
        } else {
            // Infinity - indicates we never reach a boundary.
            return Double.POSITIVE_INFINITY;
        }
    }

    static final class TData {

        private final double totalT;
        private final double tToX;
        private final double tToY;
        private final double tToZ;
        private final double nextStep;

        TData(final double totalT, final double tToX, final double tToY, final double tToZ) {
            this.totalT = totalT;
            this.tToX = tToX;
            this.tToY = tToY;
            this.tToZ = tToZ;
            this.nextStep = Math.min(tToX, Math.min(tToY, tToZ));
        }

        public double getTotalT() {
            return this.totalT;
        }

        public double gettToX() {
            return this.tToX;
        }

        public double gettToY() {
            return this.tToY;
        }

        public double gettToZ() {
            return this.tToZ;
        }

        public boolean nextStepWillAdvanceX() {
            return this.tToX <= this.nextStep;
        }

        public boolean nextStepWillAdvanceY() {
            return this.tToY <= this.nextStep;
        }

        public boolean nextStepWillAdvanceZ() {
            return this.tToZ <= this.nextStep;
        }

        public double getNextStep() {
            return this.nextStep;
        }

        public double getTotalTWithNextStep() {
            return this.nextStep + this.totalT;
        }
    }

}
