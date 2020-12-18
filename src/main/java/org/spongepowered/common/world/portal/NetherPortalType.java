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
package org.spongepowered.common.world.portal;

import net.minecraft.block.BlockState;
import net.minecraft.block.PortalInfo;
import net.minecraft.block.PortalSize;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.util.AxisUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class NetherPortalType extends VanillaPortalType {

    static Optional<TeleportationRepositioner.Result> findPortalInternal(final ServerLocation location) {
        final ServerWorld serverWorld = (ServerWorld) location.getWorld();
        final BlockPos position = VecHelper.toBlockPos(location.getBlockPosition());
        return serverWorld.getPortalForcer()
                .findPortalAround(position, serverWorld.dimension() == World.NETHER);
    }

    @Override
    public void generatePortal(final ServerLocation location, final Axis axis) {
        Objects.requireNonNull(location);
        Direction.Axis mcAxis = AxisUtil.getFor(axis);
        if (mcAxis == Direction.Axis.Y) {
            mcAxis = Direction.Axis.X;
        }
        PortalHelper.generateNetherPortal((ServerWorld) location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), mcAxis, true);
    }

    @Override
    public Optional<Portal> findPortal(final ServerLocation location) {
        Objects.requireNonNull(location);
        return NetherPortalType.findPortalInternal(location).map(x -> new VanillaPortal(this, location.withPosition(VecHelper.toVector3d(x.minCorner)), null));
    }

    @Override
    public boolean teleport(final Entity entity, final ServerLocation destination, final boolean generateDestinationPortal) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(destination);

        final net.minecraft.entity.Entity mEntity = (net.minecraft.entity.Entity) entity;

        // Nether Portal Block Collision Rules
        if (mEntity.isPassenger() || mEntity.isVehicle() || !mEntity.canChangeDimensions()) {
            return false;
        }

        final PlatformTeleporter teleporter = new Teleporter(destination, generateDestinationPortal, this);

        ((EntityAccessor) entity).accessor$portalEntrancePos(VecHelper.toBlockPos(entity.getBlockPosition()));
        return ((EntityBridge) entity).bridge$changeDimension((ServerWorld) destination.getWorld(), teleporter) != null;

    }

    static final class Teleporter implements PlatformTeleporter {

        private final ServerLocation originalDestination;
        private final boolean generateDestinationPortal;
        private final PortalType portalType;

        public Teleporter(final ServerLocation originalDestination, final boolean generateDestinationPortal, final PortalType type) {
            this.originalDestination = originalDestination;
            this.generateDestinationPortal = generateDestinationPortal;
            this.portalType = type;
        }

        @Override
        @Nullable
        public PortalInfo getPortalInfo(final net.minecraft.entity.Entity entity,
                final ServerWorld currentWorld,
                final ServerWorld targetWorld,
                final Vector3d currentPosition) {
            Optional<PortalInfo> portal = NetherPortalType.findPortalInternal(this.originalDestination)
                    .map(x -> this.createNetherPortalInfo(entity, targetWorld, x.minCorner, x));

            final Vector3d originalDestination = portal.map(x -> VecHelper.toVector3d(x.pos)).orElseGet(this.originalDestination::getPosition);
            final ChangeEntityWorldEvent.Reposition reposition = ((EntityBridge) entity).bridge$fireRepositionEvent(
                    this.originalDestination.getWorld(),
                    (org.spongepowered.api.world.server.ServerWorld) targetWorld,
                    originalDestination
            );
            if (!reposition.isCancelled() && reposition.getDestinationPosition() != originalDestination) {
                // find another portal
                portal = NetherPortalType.findPortalInternal(this.originalDestination.withPosition(reposition.getDestinationPosition()))
                        .map(x -> this.createNetherPortalInfo(entity, targetWorld, x.minCorner, x));
            }

            if (this.generateDestinationPortal && !portal.isPresent()) {
                return targetWorld.getPortalForcer().createPortal(VecHelper.toBlockPos(this.originalDestination),
                            Direction.from2DDataValue(entity.getDirection().get2DDataValue()).getAxis())
                        .map(x -> this.createNetherPortalInfo(entity, targetWorld, x.minCorner, x))
                        .orElse(null);
            }

            return portal.orElse(null);
        }

        @Override
        public net.minecraft.entity.Entity performTeleport(final net.minecraft.entity.Entity entity, final ServerWorld currentWorld,
                final ServerWorld targetWorld, final float xRot, final Function<Boolean, net.minecraft.entity.Entity> teleportLogic) {
            return teleportLogic.apply(false);
        }

        @Override
        public boolean isVanilla() {
            return false;
        }

        @Override
        public MovementType getMovementType() {
            return MovementTypes.PORTAL.get();
        }

        @Override
        public PortalType getPortalType() {
            return this.portalType;
        }

        private PortalInfo createNetherPortalInfo(
                final net.minecraft.entity.Entity entity,
                final ServerWorld serverWorld,
                final BlockPos portalLocation,
                final TeleportationRepositioner.Result result) {
            final BlockState blockstate = serverWorld.getBlockState(portalLocation);
            final Direction.Axis axis;
            final net.minecraft.util.math.vector.Vector3d vector3d;
            if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                final TeleportationRepositioner.Result res = TeleportationRepositioner.getLargestRectangleAround(portalLocation, axis, 21,
                        Direction.Axis.Y, 21, (pos) -> serverWorld.getBlockState(pos) == blockstate);
                vector3d = PortalSize.getRelativePosition(res, axis, entity.position(), entity.getDimensions(entity.getPose()));
            } else {
                axis = Direction.Axis.X;
                vector3d = new net.minecraft.util.math.vector.Vector3d(0.5D, 0.0D, 0.0D);
            }
            return PortalSize.createPortalInfo(serverWorld, result, axis, vector3d,
                    entity.getDimensions(entity.getPose()), entity.getDeltaMovement(), entity.yRot, entity.xRot);
        }

    }

}
