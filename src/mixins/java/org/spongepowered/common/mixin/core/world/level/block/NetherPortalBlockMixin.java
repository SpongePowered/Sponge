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
package org.spongepowered.common.mixin.core.world.level.block;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.level.block.PortalBlockBridge;
import org.spongepowered.common.util.AxisUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.SpongePortal;
import org.spongepowered.math.vector.Vector3i;

import java.util.Comparator;
import java.util.Optional;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin implements PortalBlockBridge {

    @Override
    public Optional<ServerLocation> bridge$calculatePortalExit(final ServerWorld from, final Vector3i fromPos, final org.spongepowered.api.entity.Entity entity) {
        final var fromLevel = (ServerLevel) from.world();
        final var toLevelKey = fromLevel.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
        final var toLevel = fromLevel.getServer().getLevel(toLevelKey);
        if (toLevel == null) {
            return Optional.empty();
        }
        final var scale = DimensionType.getTeleportationScale(fromLevel.dimensionType(), toLevel.dimensionType());
        final var exitPosition = toLevel.getWorldBorder().clampToBounds(fromPos.x() * scale, fromPos.y(), fromPos.z() * scale);
        return Optional.of(ServerLocation.of((ServerWorld) toLevel, VecHelper.toVector3i(exitPosition)));
    }

    @Override
    public Optional<Portal> bridge$findPortal(final ServerLocation at, final int searchRange) {
        final var level = ((ServerLevel) at.world());
        final var worldBorder = level.getWorldBorder();
        final var blockPos = VecHelper.toBlockPos(at.position());
        final var poiManager = level.getPoiManager();
        final int range = Math.clamp(searchRange, 1, 128);

        poiManager.ensureLoadedAndValid(level, blockPos, range);
        final var foundPortalPos = poiManager.getInSquare(poi -> poi.is(PoiTypes.NETHER_PORTAL), blockPos, range, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(worldBorder::isWithinBounds)
                .filter(pos -> level.getBlockState(pos).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
                .min(Comparator.<BlockPos>comparingDouble($$1x -> $$1x.distSqr(blockPos)).thenComparingInt(Vec3i::getY));
        return foundPortalPos.map(pos -> {
            final var portalBlockState = level.getBlockState(pos);
            final var axis = portalBlockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            final var foundRectangle = BlockUtil.getLargestRectangleAround(pos, axis, 21, Direction.Axis.Y, 21, pos2 -> level.getBlockState(pos2) == portalBlockState);
            return new SpongePortal(at.world().location(VecHelper.toVector3i(pos)), (PortalLogic) this, NetherPortalBlockMixin.impl$portalAABB(foundRectangle, axis));
        });
    }

    private static AABB impl$portalAABB(final BlockUtil.FoundRectangle portal, final Direction.Axis axis) {
        final var minCorner = VecHelper.toVector3i(portal.minCorner);
        if (axis == Direction.Axis.X) {
            return AABB.of(minCorner, minCorner.add(portal.axis1Size, portal.axis2Size, 1));
        }
        // it's z
        return AABB.of(minCorner, minCorner.add(1, portal.axis2Size, portal.axis1Size));
    }

    @Override
    public Optional<Portal> bridge$generatePortal(final ServerLocation at, final Axis axis) {
        final var level = ((ServerLevel) at.world());
        final var blockPos = VecHelper.toBlockPos(at.position());

        final var portal = level.getPortalForcer().createPortal(blockPos, AxisUtil.getFor(axis));
        return portal.map(p -> new SpongePortal(at, (PortalLogic) this, NetherPortalBlockMixin.impl$portalAABB(p, AxisUtil.getFor(axis))));
    }

    @Override
    public boolean bridge$teleport(final org.spongepowered.api.entity.Entity entity, final ServerLocation destination, final boolean generateDestinationPortal) {
        final var toLevel = (ServerLevel) destination.world();
        boolean toSmallerScaleLevel = toLevel.dimension() == Level.NETHER;
        var found = this.bridge$findPortal(destination, toSmallerScaleLevel ? 16 : 128);
        var portalDestination = found.map(Portal::position);
        if (portalDestination.isPresent()) {
            entity.setLocation(portalDestination.get());
            return true;
        }
        if (!generateDestinationPortal) {
            return false;
        }
        final Axis axis = AxisUtil.getFor(Direction.Axis.X);
        var generated = this.bridge$generatePortal(destination, axis);
        portalDestination = generated.map(Portal::position);
        if (portalDestination.isPresent()) {
            entity.setLocation(portalDestination.get());
            return true;
        }
        return false;
    }


}
