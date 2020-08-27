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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.player.ServerPlayerEntityAccessor;
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.PlatformServerWorldBridge;
import org.spongepowered.common.bridge.world.dimension.PlatformDimensionBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.function.Function;

// Because Vanilla doesn't move this stuff out...
public final class PortalHelper {

    private static final BlockState northEndFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
    private static final BlockState southEndFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
    private static final BlockState eastEndFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
    private static final BlockState westEndFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);
    private static final BlockState endPortal = Blocks.END_PORTAL.getDefaultState();

    public static void generateEndPortal(final ServerWorld world, final int x, final int y, final int z, final boolean placePortalBlocks) {
        // Sponge Start - Recreate logic for making an end portal frame since Vanilla assumes you are in a stronghold

        final BlockPos.Mutable origin = new BlockPos.Mutable(x, y, z); // 2

        for (int bx = 0; bx < 5; bx++) {
            for (int by = 0; by < 5; by++) {
                origin.setPos(x + bx, y, z + by);

                if (bx == 0 && (by > 0 && by < 4)) {
                    world.setBlockState(origin, PortalHelper.southEndFrame.with(EndPortalFrameBlock.EYE, world.rand.nextFloat() > 0.9F), 2);
                    continue;
                }

                if (bx == 1 || bx == 2 || bx == 3) {
                    if (by == 0) {
                        world.setBlockState(origin, PortalHelper.eastEndFrame.with(EndPortalFrameBlock.EYE, world.rand.nextFloat() > 0.9F), 2);
                    } else if (by == 4) {
                        world.setBlockState(origin, PortalHelper.westEndFrame.with(EndPortalFrameBlock.EYE, world.rand.nextFloat() > 0.9F), 2);
                    } else if (placePortalBlocks) {
                        world.setBlockState(origin, PortalHelper.endPortal, 2);
                    }

                    continue;
                }

                if (bx == 4 && (by > 0 && by < 4)) {
                    world.setBlockState(origin, PortalHelper.northEndFrame.with(EndPortalFrameBlock.EYE, world.rand.nextFloat() > 0.9F), 2);
                }
            }
        }
    }

    public static void generateNetherPortal(final ServerWorld world, final int x, final int y, final int z, final boolean placePortalBlocks) {
        int i = 16;
        double d0 = -1.0D;
        int j = x;
        int k = y;
        int l = z;
        int i1 = j;
        int j1 = k;
        int k1 = l;
        int l1 = 0;
        int i2 = world.rand.nextInt(4);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(int j2 = j - 16; j2 <= j + 16; ++j2) {
            double d1 = (double)j2 + 0.5D - x;

            for(int l2 = l - 16; l2 <= l + 16; ++l2) {
                double d2 = (double)l2 + 0.5D - z;

                label276:
                for(int j3 = world.getActualHeight() - 1; j3 >= 0; --j3) {
                    if (world.isAirBlock(blockpos$mutable.setPos(j2, j3, l2))) {
                        while(j3 > 0 && world.isAirBlock(blockpos$mutable.setPos(j2, j3 - 1, l2))) {
                            --j3;
                        }

                        for(int k3 = i2; k3 < i2 + 4; ++k3) {
                            int l3 = k3 % 2;
                            int i4 = 1 - l3;
                            if (k3 % 4 >= 2) {
                                l3 = -l3;
                                i4 = -i4;
                            }

                            for(int j4 = 0; j4 < 3; ++j4) {
                                for(int k4 = 0; k4 < 4; ++k4) {
                                    for(int l4 = -1; l4 < 4; ++l4) {
                                        int i5 = j2 + (k4 - 1) * l3 + j4 * i4;
                                        int j5 = j3 + l4;
                                        int k5 = l2 + (k4 - 1) * i4 - j4 * l3;
                                        blockpos$mutable.setPos(i5, j5, k5);
                                        if (l4 < 0 && !world.getBlockState(blockpos$mutable).getMaterial().isSolid() || l4 >= 0 && !world.isAirBlock(blockpos$mutable)) {
                                            continue label276;
                                        }
                                    }
                                }
                            }

                            double d5 = (double)j3 + 0.5D - y;
                            double d7 = d1 * d1 + d5 * d5 + d2 * d2;
                            if (d0 < 0.0D || d7 < d0) {
                                d0 = d7;
                                i1 = j2;
                                j1 = j3;
                                k1 = l2;
                                l1 = k3 % 4;
                            }
                        }
                    }
                }
            }
        }

        if (d0 < 0.0D) {
            for(int l5 = j - 16; l5 <= j + 16; ++l5) {
                double d3 = (double)l5 + 0.5D - x;

                for(int j6 = l - 16; j6 <= l + 16; ++j6) {
                    double d4 = (double)j6 + 0.5D - z;

                    label214:
                    for(int i7 = world.getActualHeight() - 1; i7 >= 0; --i7) {
                        if (world.isAirBlock(blockpos$mutable.setPos(l5, i7, j6))) {
                            while(i7 > 0 && world.isAirBlock(blockpos$mutable.setPos(l5, i7 - 1, j6))) {
                                --i7;
                            }

                            for(int l7 = i2; l7 < i2 + 2; ++l7) {
                                int l8 = l7 % 2;
                                int k9 = 1 - l8;

                                for(int i10 = 0; i10 < 4; ++i10) {
                                    for(int k10 = -1; k10 < 4; ++k10) {
                                        int i11 = l5 + (i10 - 1) * l8;
                                        int j11 = i7 + k10;
                                        int k11 = j6 + (i10 - 1) * k9;
                                        blockpos$mutable.setPos(i11, j11, k11);
                                        if (k10 < 0 && !world.getBlockState(blockpos$mutable).getMaterial().isSolid() || k10 >= 0 && !world.isAirBlock(blockpos$mutable)) {
                                            continue label214;
                                        }
                                    }
                                }

                                double d6 = (double)i7 + 0.5D - y;
                                double d8 = d3 * d3 + d6 * d6 + d4 * d4;
                                if (d0 < 0.0D || d8 < d0) {
                                    d0 = d8;
                                    i1 = l5;
                                    j1 = i7;
                                    k1 = j6;
                                    l1 = l7 % 2;
                                }
                            }
                        }
                    }
                }
            }
        }

        int i6 = i1;
        int k2 = j1;
        int k6 = k1;
        int l6 = l1 % 2;
        int i3 = 1 - l6;
        if (l1 % 4 >= 2) {
            l6 = -l6;
            i3 = -i3;
        }

        if (d0 < 0.0D) {
            j1 = MathHelper.clamp(j1, 70, world.getActualHeight() - 10);
            k2 = j1;

            for(int j7 = -1; j7 <= 1; ++j7) {
                for(int i8 = 1; i8 < 3; ++i8) {
                    for(int i9 = -1; i9 < 3; ++i9) {
                        int l9 = i6 + (i8 - 1) * l6 + j7 * i3;
                        int j10 = k2 + i9;
                        int l10 = k6 + (i8 - 1) * i3 - j7 * l6;
                        boolean flag = i9 < 0;
                        blockpos$mutable.setPos(l9, j10, l10);
                        world.setBlockState(blockpos$mutable, flag ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        for(int k7 = -1; k7 < 3; ++k7) {
            for(int j8 = -1; j8 < 4; ++j8) {
                if (k7 == -1 || k7 == 2 || j8 == -1 || j8 == 3) {
                    blockpos$mutable.setPos(i6 + k7 * l6, k2 + j8, k6 + k7 * i3);
                    world.setBlockState(blockpos$mutable, Blocks.OBSIDIAN.getDefaultState(), 3);
                }
            }
        }

        if (placePortalBlocks) {
            BlockState blockstate =
                    Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, l6 == 0 ? Direction.Axis.Z : Direction.Axis.X);

            for (int k8 = 0; k8 < 2; ++k8) {
                for (int j9 = 0; j9 < 3; ++j9) {
                    blockpos$mutable.setPos(i6 + k8 * l6, k2 + j9, k6 + k8 * i3);
                    world.setBlockState(blockpos$mutable, blockstate, 18);
                }
            }
        }
    }

    public static void generateEndObsidianPlatform(final ServerWorld world, final int x, final int y, final int z) {
        for (int j1 = -2; j1 <= 2; ++j1) {
            for (int k1 = -2; k1 <= 2; ++k1) {
                for (int l1 = -1; l1 < 3; ++l1) {
                    int i2 = x + k1 * 1 + j1 * 0;
                    int j2 = (y - 1) + l1;
                    int k2 = z + k1 * 0 - j1 * 1;
                    final boolean flag = l1 < 0;
                    world.setBlockState(new BlockPos(i2, j2, k2), flag ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    public static Function<Boolean, Entity> createVanillaEntityPortalLogic(final Entity entity, final Vec3d destination,
            final ServerWorld fromWorld, final ServerWorld toWorld, final PortalType portal) {

        return spawnInPortal -> {
            double x = destination.x;
            double y = destination.y;
            double z = destination.z;
            float pitch = entity.rotationPitch;
            float yaw = entity.rotationYaw;
            Vec3d motion = entity.getMotion();

            fromWorld.getProfiler().startSection("moving");

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

    public static Function<Boolean, Entity> createVanillaPlayerPortalLogic(final ServerPlayerEntity player, final Vec3d destination,
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
            if (!isSameWorld && (!(portal instanceof NetherPortalType) && toWorld.getDimension().getType() == DimensionType.THE_END)) {
                PortalHelper.generateEndObsidianPlatform(toWorld, (int) x, (int) y, (int) z);
                player.setMotion(Vec3d.ZERO);
            // Only set the entered nether position if we've spawned in a portal, it isn't inter-world, not the API nether portal, and we're going
            // to Vanilla's The Nether
            } else if (spawnInPortal && !isSameWorld && (!(portal instanceof NetherPortalType) && toWorld.getDimension().getType() == DimensionType.THE_NETHER)) {
                ((ServerPlayerEntityAccessor) player).accessor$setEnteredNetherPosition(player.getPositionVec());
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
