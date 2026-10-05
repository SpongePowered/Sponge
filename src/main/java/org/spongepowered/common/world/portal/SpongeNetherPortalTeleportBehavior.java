package org.spongepowered.common.world.portal;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.util.VecHelper;

public enum SpongeNetherPortalTeleportBehavior implements PortalLogic.TeleportBehavior {
    INSTANCE;

    @Override
    public ServerLocation spawnLocation(ServerLocation from, ServerLocation to, Entity spongeEntity) {
        final var entity = (net.minecraft.world.entity.Entity) spongeEntity;
        final var level = ((ServerLevel) to.world());
        final var originBlockPos = VecHelper.toBlockPos(from);
        final var destBlockPos = VecHelper.toBlockPos(to);
        final var portalBlockState = level.getBlockState(destBlockPos);
        if (!portalBlockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            return to;
        }
        final var axis = portalBlockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        final var foundRectangle = BlockUtil.getLargestRectangleAround(destBlockPos, axis, 21, Direction.Axis.Y, 21, pos2 -> level.getBlockState(pos2) == portalBlockState);
        BlockState $$5 = entity.level().getBlockState(originBlockPos);

        Vec3 relativePortalPosition;
        Direction.Axis originAxis = $$5.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        BlockUtil.FoundRectangle $$7 = BlockUtil.getLargestRectangleAround(originBlockPos, originAxis, 21, Direction.Axis.Y, 21, ($$2x) -> entity.level().getBlockState($$2x) == $$5);
        relativePortalPosition = entity.getRelativePortalPosition(originAxis, $$7);

        BlockPos minCorner = foundRectangle.minCorner;
        BlockState blockState = level.getBlockState(minCorner);
        Direction.Axis $$11 = blockState.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        double $$12 = foundRectangle.axis1Size;
        double $$13 = foundRectangle.axis2Size;
        EntityDimensions $$14 = entity.getDimensions(entity.getPose());
        double $$17 = (double) $$14.width() / (double) 2.0F + ($$12 - (double) $$14.width()) * relativePortalPosition.x();
        double $$18 = ($$13 - (double) $$14.height()) * relativePortalPosition.y();
        double $$19 = (double) 0.5F + relativePortalPosition.z();
        boolean $$20 = $$11 == Direction.Axis.X;
        Vec3 $$21 = new Vec3((double) minCorner.getX() + ($$20 ? $$17 : $$19), (double) minCorner.getY() + $$18, (double) minCorner.getZ() + ($$20 ? $$19 : $$17));
        Vec3 collisionFreePosition = PortalShape.findCollisionFreePosition($$21, level, entity, $$14);
        return to.world().location(VecHelper.toVector3d(collisionFreePosition));
    }

}
