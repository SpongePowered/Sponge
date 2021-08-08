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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.portal.PortalTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.util.AxisUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;

public final class NetherPortalType extends VanillaPortalType {

    static Optional<BlockUtil.FoundRectangle> findPortalInternal(final ServerLocation location) {
        final ServerLevel serverWorld = (ServerLevel) location.world();
        final BlockPos position = VecHelper.toBlockPos(location.blockPosition());
        return serverWorld.getPortalForcer()
                .findPortalAround(position, serverWorld.dimension() == Level.NETHER);
    }

    public static Portal portalObjectFromRectangle(final ServerWorld world, final BlockUtil.FoundRectangle x) {
        final Vector3d minCornerVec = VecHelper.toVector3d(x.minCorner);
        final ServerLocation minCorner = world.location(minCornerVec);
        final @Nullable Axis axis =  minCorner.block().getOrNull(Keys.AXIS);
        if (axis == null) {
            return new VanillaPortal(PortalTypes.NETHER.get(), minCorner, null);
        }
        final ServerLocation maxCorner;
        if (axis == Axis.X) {
            maxCorner = minCorner.withPosition(minCornerVec.add(x.axis1Size, x.axis2Size, 0));
        } else {
            // it's z
            maxCorner = minCorner.withPosition(minCornerVec.add(0, x.axis2Size, x.axis1Size));
        }
        return new VanillaTwoDimensionalPortal(PortalTypes.NETHER.get(), minCorner, maxCorner, null);
    }

    @Override
    public boolean generatePortal(final ServerLocation location, final Axis axis) {
        Objects.requireNonNull(location);
        Direction.Axis mcAxis = AxisUtil.getFor(axis);
        if (mcAxis == Direction.Axis.Y) {
            mcAxis = Direction.Axis.X;
        }
        PortalHelper.generateNetherPortal((ServerLevel) location.world(), location.blockX(), location.blockY(), location.blockZ(), mcAxis, true);
        return true;
    }

    @Override
    public Optional<Portal> findPortal(final ServerLocation location) {
        Objects.requireNonNull(location);
        return NetherPortalType.findPortalInternal(location).map(x -> NetherPortalType.portalObjectFromRectangle(location.world(), x));
    }

    @Override
    public boolean teleport(final Entity entity, final ServerLocation destination, final boolean generateDestinationPortal) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(destination);

        final net.minecraft.world.entity.Entity mEntity = (net.minecraft.world.entity.Entity) entity;

        // Nether Portal Block Collision Rules
        if (mEntity.isPassenger() || mEntity.isVehicle() || !mEntity.canChangeDimensions()) {
            return false;
        }

        final PortalLogic teleporter = new Teleporter(destination, generateDestinationPortal, this);

        ((EntityAccessor) entity).accessor$portalEntrancePos(VecHelper.toBlockPos(entity.blockPosition()));
        return ((EntityBridge) entity).bridge$changeDimension((ServerLevel) destination.world(), teleporter) != null;

    }

    static final class Teleporter implements PortalLogic {

        private final ServerLocation originalDestination;
        private final boolean generateDestinationPortal;
        private final PortalType portalType;

        public Teleporter(final ServerLocation originalDestination, final boolean generateDestinationPortal, final PortalType type) {
            this.originalDestination = originalDestination;
            this.generateDestinationPortal = generateDestinationPortal;
            this.portalType = type;
        }

        @Override
        public @Nullable PortalInfo getPortalInfo(final net.minecraft.world.entity.Entity entity,
                final ServerLevel targetWorld,
                final Function<ServerLevel, PortalInfo> defaultPortalInfo) {
            Optional<PortalInfo> portal = NetherPortalType.findPortalInternal(this.originalDestination)
                    .map(x -> this.createNetherPortalInfo(entity, targetWorld, x.minCorner, x));

            final Vector3d originalDestination = portal.map(x -> VecHelper.toVector3d(x.pos)).orElseGet(this.originalDestination::position);
            final ChangeEntityWorldEvent.Reposition reposition = ((EntityBridge) entity).bridge$fireRepositionEvent(
                    this.originalDestination.world(),
                    (org.spongepowered.api.world.server.ServerWorld) targetWorld,
                    originalDestination
            );
            if (!reposition.isCancelled() && reposition.destinationPosition() != originalDestination) {
                // find another portal
                portal = NetherPortalType.findPortalInternal(this.originalDestination.withPosition(reposition.destinationPosition()))
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
        public net.minecraft.world.entity.Entity placeEntity(final net.minecraft.world.entity.Entity entity, final ServerLevel currentWorld,
                final ServerLevel targetWorld, final float yRot, final Function<Boolean, net.minecraft.world.entity.Entity> teleportLogic) {
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
                final net.minecraft.world.entity.Entity entity,
                final ServerLevel serverWorld,
                final BlockPos portalLocation,
                final BlockUtil.FoundRectangle result) {
            final BlockState blockstate = serverWorld.getBlockState(portalLocation);
            final Direction.Axis axis;
            final net.minecraft.world.phys.Vec3 vector3d;
            if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                final BlockUtil.FoundRectangle res = BlockUtil.getLargestRectangleAround(portalLocation, axis, 21,
                        Direction.Axis.Y, 21, (pos) -> serverWorld.getBlockState(pos) == blockstate);
                vector3d = PortalShape.getRelativePosition(res, axis, entity.position(), entity.getDimensions(entity.getPose()));
            } else {
                axis = Direction.Axis.X;
                vector3d = new net.minecraft.world.phys.Vec3(0.5D, 0.0D, 0.0D);
            }
            return PortalShape.createPortalInfo(serverWorld, result, axis, vector3d,
                    entity.getDimensions(entity.getPose()), entity.getDeltaMovement(), entity.yRot, entity.xRot);
        }

    }

}
