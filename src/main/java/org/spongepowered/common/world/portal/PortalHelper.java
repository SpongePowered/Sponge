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
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.player.ServerPlayerEntityAccessor;
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.function.Function;

// Because Vanilla doesn't move this stuff out...
public final class PortalHelper {

    private static final BlockState northEndFrame = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
    private static final BlockState southEndFrame = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
    private static final BlockState eastEndFrame = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
    private static final BlockState westEndFrame = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
    private static final BlockState endPortal = Blocks.END_PORTAL.defaultBlockState();

    public static void generateEndPortal(final ServerWorld world, final int x, final int y, final int z, final boolean placePortalBlocks) {
        // Sponge Start - Recreate logic for making an end portal frame since Vanilla assumes you are in a stronghold

        final BlockPos.Mutable origin = new BlockPos.Mutable(x, y, z); // 2

        for (int bx = 0; bx < 5; bx++) {
            for (int by = 0; by < 5; by++) {
                origin.set(x + bx, y, z + by);

                if (bx == 0 && (by > 0 && by < 4)) {
                    world.setBlock(origin, PortalHelper.southEndFrame.setValue(EndPortalFrameBlock.HAS_EYE, world.random.nextFloat() > 0.9F), 2);
                    continue;
                }

                if (bx == 1 || bx == 2 || bx == 3) {
                    if (by == 0) {
                        world.setBlock(origin, PortalHelper.eastEndFrame.setValue(EndPortalFrameBlock.HAS_EYE, world.random.nextFloat() > 0.9F), 2);
                    } else if (by == 4) {
                        world.setBlock(origin, PortalHelper.westEndFrame.setValue(EndPortalFrameBlock.HAS_EYE, world.random.nextFloat() > 0.9F), 2);
                    } else if (placePortalBlocks) {
                        world.setBlock(origin, PortalHelper.endPortal, 2);
                    }

                    continue;
                }

                if (bx == 4 && (by > 0 && by < 4)) {
                    world.setBlock(origin, PortalHelper.northEndFrame.setValue(EndPortalFrameBlock.HAS_EYE, world.random.nextFloat() > 0.9F), 2);
                }
            }
        }
    }

    // see Teleporter#createPortal
    public static void generateNetherPortal(final ServerWorld world, final int x, final int y, final int z, final Direction.Axis axis, final boolean placePortalBlocks) {
        BlockPos portalPos = new BlockPos(x, y, z);
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double d0 = -1.0D;
        BlockPos blockpos = null;
        double d1 = -1.0D;
        BlockPos blockpos1 = null;
        WorldBorder worldborder = world.getWorldBorder();
        int i = world.getHeight() - 1;
        BlockPos.Mutable blockpos$mutable = portalPos.mutable();

        for(BlockPos.Mutable blockpos$mutable1 : BlockPos.spiralAround(portalPos, 16, Direction.EAST, Direction.SOUTH)) {
            int j = Math.min(i, world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable1.getX(), blockpos$mutable1.getZ()));
            int k = 1;
            if (worldborder.isWithinBounds(blockpos$mutable1) && worldborder.isWithinBounds(blockpos$mutable1.move(direction, 1))) {
                blockpos$mutable1.move(direction.getOpposite(), 1);

                for(int l = j; l >= 0; --l) {
                    blockpos$mutable1.setY(l);
                    if (world.isEmptyBlock(blockpos$mutable1)) {
                        int i1;
                        for(i1 = l; l > 0 && world.isEmptyBlock(blockpos$mutable1.move(Direction.DOWN)); --l) {
                        }

                        if (l + 4 <= i) {
                            int j1 = i1 - l;
                            if (j1 <= 0 || j1 >= 3) {
                                blockpos$mutable1.setY(l);
                                if (PortalHelper.canHostFrame(world, blockpos$mutable1, blockpos$mutable, direction, 0)) {
                                    double d2 = portalPos.distSqr(blockpos$mutable1);
                                    if (PortalHelper.canHostFrame(world, blockpos$mutable1, blockpos$mutable, direction, -1) && PortalHelper.canHostFrame(world, blockpos$mutable1, blockpos$mutable, direction, 1) && (d0 == -1.0D || d0 > d2)) {
                                        d0 = d2;
                                        blockpos = blockpos$mutable1.immutable();
                                    }

                                    if (d0 == -1.0D && (d1 == -1.0D || d1 > d2)) {
                                        d1 = d2;
                                        blockpos1 = blockpos$mutable1.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (d0 == -1.0D && d1 != -1.0D) {
            blockpos = blockpos1;
            d0 = d1;
        }

        if (d0 == -1.0D) {
            blockpos = (new BlockPos(portalPos.getX(), MathHelper.clamp(portalPos.getY(), 70, world.getHeight() - 10), portalPos.getZ())).immutable();
            Direction direction1 = direction.getClockWise();
            if (!worldborder.isWithinBounds(blockpos)) {
                return;
            }

            for(int l1 = -1; l1 < 2; ++l1) {
                for(int k2 = 0; k2 < 2; ++k2) {
                    for(int i3 = -1; i3 < 3; ++i3) {
                        BlockState blockstate1 = i3 < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        blockpos$mutable.setWithOffset(blockpos, k2 * direction.getStepX() + l1 * direction1.getStepX(), i3, k2 * direction.getStepZ() + l1 * direction1.getStepZ());
                        world.setBlockAndUpdate(blockpos$mutable, blockstate1);
                    }
                }
            }
        }

        for(int k1 = -1; k1 < 3; ++k1) {
            for(int i2 = -1; i2 < 4; ++i2) {
                if (k1 == -1 || k1 == 2 || i2 == -1 || i2 == 3) {
                    blockpos$mutable.setWithOffset(blockpos, k1 * direction.getStepX(), i2, k1 * direction.getStepZ());
                    world.setBlock(blockpos$mutable, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
        }

        if (placePortalBlocks) {
            BlockState blockstate = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);

            for(int j2 = 0; j2 < 2; ++j2) {
                for(int l2 = 0; l2 < 3; ++l2) {
                    blockpos$mutable.setWithOffset(blockpos, j2 * direction.getStepX(), l2, j2 * direction.getStepZ());
                    world.setBlock(blockpos$mutable, blockstate, 18);
                }
            }
        }

    }

    private static boolean canHostFrame(ServerWorld world, BlockPos p_242955_1_, BlockPos.Mutable p_242955_2_, Direction p_242955_3_, int p_242955_4_) {
        Direction direction = p_242955_3_.getClockWise();

        for(int i = -1; i < 3; ++i) {
            for(int j = -1; j < 4; ++j) {
                p_242955_2_.setWithOffset(p_242955_1_, p_242955_3_.getStepX() * i + direction.getStepX() * p_242955_4_, j, p_242955_3_.getStepZ() * i + direction.getStepZ() * p_242955_4_);
                if (j < 0 && !world.getBlockState(p_242955_2_).getMaterial().isSolid()) {
                    return false;
                }

                if (j >= 0 && !world.isEmptyBlock(p_242955_2_)) {
                    return false;
                }
            }
        }

        return true;
    }

    // see ServerPlayerEntity#createEndPlatform
    public static void generateEndObsidianPlatform(final ServerWorld world, final int x, final int y, final int z) {
        final BlockPos.Mutable blockpos$mutable = new BlockPos(x, y, z).mutable();

        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                for(int k = -1; k < 3; ++k) {
                    BlockState blockstate = k == -1 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    world.setBlockAndUpdate(blockpos$mutable.set(x, y, z).move(j, k, i), blockstate);
                }
            }
        }
    }

    public static Function<Boolean, Entity> createVanillaEntityPortalLogic(final Entity entity, final net.minecraft.util.math.vector.Vector3d destination,
            final ServerWorld fromWorld, final ServerWorld toWorld, final PortalType portal) {

        return spawnInPortal -> {
            double x = destination.x;
            double y = destination.y;
            double z = destination.z;
            float pitch = entity.xRot;
            float yaw = entity.yRot;
            net.minecraft.util.math.vector.Vector3d motion = entity.getDeltaMovement();

            fromWorld.getProfiler().push("moving");

            // Entering an end portal from THE_END to OVERWORLD after exiting
            if (!(portal instanceof NetherPortalType) && fromWorld.getDimension().getType() == DimensionType.THE_END && toWorld.getDimension().getType() == DimensionType.OVERWORLD) {
                final BlockPos pos = toWorld.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, toWorld.getSpawnPoint());
                x = pos.getX();
                y = pos.getY();
                z = pos.getZ();
            } else if ((!(portal instanceof NetherPortalType) && toWorld.getDimension() instanceof EndDimension)) {
                // Sponge End
                final BlockPos blockpos = toWorld.getSpawnCoordinate();
                x = blockpos.getX();
                y = blockpos.getY();
                z = blockpos.getZ();
                yaw = 90.0F;
                pitch = 0.0F;
            } else {
                // Use platform move factor instead of Vanilla
                final double moveFactor =
                        ((PlatformDimensionBridge) fromWorld.getDimension()).bridge$getMovementFactor() / ((PlatformDimensionBridge)
                                toWorld.getDimension()).bridge$getMovementFactor();
                x *= moveFactor;
                z *= moveFactor;
            }

            toWorld.getProfiler().endSection();
            toWorld.getProfiler().startSection("placing");
            double d7 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().minX() + 16.0D);
            double d4 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().minZ() + 16.0D);
            double d5 = Math.min(2.9999872E7D, toWorld.getWorldBorder().maxX() - 16.0D);
            double d6 = Math.min(2.9999872E7D, toWorld.getWorldBorder().maxZ() - 16.0D);
            x = MathHelper.clamp(x, d7, d5);
            z = MathHelper.clamp(z, d4, d6);

            // THE_END -> OVERWORLD is a straight port, no actual portal detection. Turn off the spawn flag
            if (fromWorld.getDimension().getType() == DimensionType.THE_END && toWorld.getDimension().getType() == DimensionType.OVERWORLD) {
                spawnInPortal = false;
            }

            // <ANY> -> THE_END is a straight port onto the obsidian slab. Turn off spawn flag
            if (toWorld.getDimension().getType() == DimensionType.THE_END) {
                spawnInPortal = false;
            }

            boolean isPortalThere = false;

            // If the portal is our API re-implementation of the Nether portal, allow that to veto and handle repositioning (as someone specified
            // intent to have it handle placing the nether portal/etc regardless of where we're coming and going)
            if (portal instanceof NetherPortalType || spawnInPortal) {
                final BlockPattern.PortalInfo result = toWorld.getDefaultTeleporter().placeInExistingPortal(new BlockPos(x, y, z), motion,
                        entity.getTeleportDirection(), entity.getLastPortalVec().x, entity.getLastPortalVec().y, false);

                if (result == null) {
                    isPortalThere = false;
                } else {
                    isPortalThere = true;
                    x = result.pos.x;
                    y = result.pos.y;
                    z = result.pos.z;

                    motion = result.motion;
                    yaw = result.rotation;
                }
            }

            if (spawnInPortal && !isPortalThere) {
                return entity;
            }

            final MoveEntityEvent event = SpongeEventFactory.createChangeEntityWorldEventReposition(PhaseTracker.getCauseStackManager()
                            .getCurrentCause(), (org.spongepowered.api.entity.Entity) entity, (org.spongepowered.api.world.server.ServerWorld) fromWorld, VecHelper
                            .toVector3d(entity.getPositionVector()),  new Vector3d(x, y, z), (org.spongepowered.api.world.server.ServerWorld) toWorld,
                    new Vector3d(x, y, z), (org.spongepowered.api.world.server.ServerWorld) toWorld);

            if (SpongeCommon.postEvent(event)) {
                entity.setMotion(Vec3d.ZERO);
                return entity;
            }

            x = event.getDestinationPosition().getX();
            y = event.getDestinationPosition().getY();
            z = event.getDestinationPosition().getZ();

            ((PlatformEntityBridge) entity).bridge$remove(true);
            final Entity result = entity.getType().create(toWorld);
            if (result != null) {
                result.copyDataFromOld(entity);
                result.moveToBlockPosAndAngles(new BlockPos(x, y, z), yaw, pitch);
                result.setMotion(motion);
                toWorld.addFromAnotherDimension(result);
            }

            return result;
        };
    }

    public static Function<Boolean, Entity> createVanillaPlayerPortalLogic(final ServerPlayerEntity player, final net.minecraft.util.math.vector.Vector3d destination,
        final ServerWorld fromWorld, final ServerWorld toWorld, final PortalType portal) {

        return spawnInPortal -> {
            double x = destination.x;
            double y = destination.y;
            double z = destination.z;
            float pitch = player.rotationPitch;
            final float originalYaw = player.rotationYaw;
            float yaw = originalYaw;

            fromWorld.getProfiler().startSection("moving");

            final boolean isSameWorld = fromWorld == toWorld;

            // Entering an end portal from THE_END to OVERWORLD after beating the Dragon/etc
            if (!(portal instanceof NetherPortalType) && fromWorld.getDimension().getType() == DimensionType.THE_END && toWorld.getDimension().getType() == DimensionType.OVERWORLD) {
                final BlockPos pos = toWorld.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, toWorld.getSpawnPoint());
                x = pos.getX();
                y = pos.getY();
                z = pos.getZ();
            } else if ((!(portal instanceof NetherPortalType) && toWorld.getDimension() instanceof EndDimension)) {
                // Sponge End
                final BlockPos blockpos = toWorld.getSpawnCoordinate();
                x = blockpos.getX();
                y = blockpos.getY();
                z = blockpos.getZ();
                yaw = 90.0F;
                pitch = 0.0F;
            } else {
                // Use platform move factor instead of Vanilla
                final double moveFactor =
                        ((PlatformDimensionBridge) fromWorld.getDimension()).bridge$getMovementFactor() / ((PlatformDimensionBridge)
                                toWorld.getDimension()).bridge$getMovementFactor();
                x *= moveFactor;
                z *= moveFactor;
            }

            fromWorld.getProfiler().endSection();
            fromWorld.getProfiler().startSection("placing");
            double d7 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().minX() + 16.0D);
            double d4 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().minZ() + 16.0D);
            double d5 = Math.min(2.9999872E7D, toWorld.getWorldBorder().maxX() - 16.0D);
            double d6 = Math.min(2.9999872E7D, toWorld.getWorldBorder().maxZ() - 16.0D);
            x = MathHelper.clamp(x, d7, d5);
            z = MathHelper.clamp(z, d4, d6);

            // THE_END -> OVERWORLD is a straight port, no actual portal detection. Turn off the spawn flag
            if (fromWorld.getDimension().getType() == DimensionType.THE_END && toWorld.getDimension().getType() == DimensionType.OVERWORLD) {
                spawnInPortal = false;
            }

            // <ANY> -> THE_END is a straight port onto the obsidian slab. Turn off spawn flag
            if (toWorld.getDimension().getType() == DimensionType.THE_END) {
                spawnInPortal = false;
            }

            // If the portal is our API re-implementation of the Nether portal, allow that to veto and handle repositioning (as someone specified
            // intent to have it handle placing the nether portal/etc regardless of where we're coming and going)-
            if (portal instanceof NetherPortalType) {
                spawnInPortal = true;
            }

            ((ServerPlayerEntityBridge) player).bridge$sendChangeDimension(toWorld.dimension.getType(),
                    WorldInfo.byHashing(toWorld.getSeed()), toWorld.getWorldType(), player.interactionManager.getGameType());

            boolean isPortalThere = false;

            if (spawnInPortal) {
                final BlockPattern.PortalInfo result = toWorld.getDefaultTeleporter().placeInExistingPortal(new BlockPos(x, y, z), player.getMotion(),
                        player.getTeleportDirection(), player.getLastPortalVec().x, player.getLastPortalVec().y, true);

                if (result == null) {
                    isPortalThere = false;
                } else {
                    isPortalThere = true;
                    x = result.pos.x;
                    y = result.pos.y;
                    z = result.pos.z;

                    yaw = result.rotation;
                }
            }

            final MoveEntityEvent event = SpongeEventFactory.createChangeEntityWorldEventReposition(PhaseTracker.getCauseStackManager()
                    .getCurrentCause(), (ServerPlayer) player, (org.spongepowered.api.world.server.ServerWorld) fromWorld, VecHelper
                    .toVector3d(player.getPositionVector()),  new Vector3d(x, y, z), (org.spongepowered.api.world.server.ServerWorld) toWorld,
                    new Vector3d(x, y, z), (org.spongepowered.api.world.server.ServerWorld) toWorld);

            if (SpongeCommon.postEvent(event)) {
                player.connection.setPlayerLocation(player.getPosX(), player.getPosY(), player.getPosZ(), player.rotationYaw, player.rotationPitch);
                player.connection.captureCurrentPosition();
                player.setMotion(Vec3d.ZERO);
                return player;
            }

            x = event.getDestinationPosition().getX();
            y = event.getDestinationPosition().getY();
            z = event.getDestinationPosition().getZ();

            // Only create the obsidian platform if this not inter-world, not the API nether portal, and we're going to Vanilla's The End
            if (!isSameWorld && (!(portal instanceof NetherPortalType) && toWorld.getDimension() instanceof EndDimension)) {
                PortalHelper.generateEndObsidianPlatform(toWorld, (int) x, (int) y, (int) z);
                player.setMotion(Vec3d.ZERO);
            // Only set the entered nether position if we've spawned in a portal, it isn't inter-world, not the API nether portal, and we're going
            // to Vanilla's The Nether
            } else if (spawnInPortal && !isSameWorld && (!(portal instanceof NetherPortalType) && toWorld.getDimension().getType() == DimensionType.THE_NETHER)) {
                ((ServerPlayerEntityAccessor) player).accessor$enteredNetherPosition(player.getPositionVec());
            }

            if (spawnInPortal && !isPortalThere) {
                // Apply snapshot values now that event has been fired
                player.setLocationAndAngles(x, y, z, originalYaw, pitch);

                toWorld.getDefaultTeleporter().makePortal(player);
                toWorld.getDefaultTeleporter().placeInPortal(player, yaw);

                // Grab values one last time, only to allow us to call setLocationAndAngles below
                x = player.getPosX();
                y = player.getPosY();
                z = player.getPosZ();
                yaw = player.rotationYaw;
                pitch = player.rotationPitch;
            }

            player.connection.setPlayerLocation(x, y, z, yaw, pitch);
            player.connection.captureCurrentPosition();
            fromWorld.getProfiler().endSection();
            return player;
        };
    }
}
