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
import net.minecraft.block.PortalInfo;
import net.minecraft.block.PortalSize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.accessor.entity.player.ServerPlayerEntityAccessor;
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;
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
        final BlockPos portalPos = new BlockPos(x, y, z);
        final Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double d0 = -1.0D;
        BlockPos blockpos = null;
        double d1 = -1.0D;
        BlockPos blockpos1 = null;
        final WorldBorder worldborder = world.getWorldBorder();
        final int i = world.getHeight() - 1;
        final BlockPos.Mutable blockpos$mutable = portalPos.mutable();

        for(final BlockPos.Mutable blockpos$mutable1 : BlockPos.spiralAround(portalPos, 16, Direction.EAST, Direction.SOUTH)) {
            final int j = Math.min(i, world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable1.getX(), blockpos$mutable1.getZ()));
            final int k = 1;
            if (worldborder.isWithinBounds(blockpos$mutable1) && worldborder.isWithinBounds(blockpos$mutable1.move(direction, 1))) {
                blockpos$mutable1.move(direction.getOpposite(), 1);

                for(int l = j; l >= 0; --l) {
                    blockpos$mutable1.setY(l);
                    if (world.isEmptyBlock(blockpos$mutable1)) {
                        final int i1;
                        for(i1 = l; l > 0 && world.isEmptyBlock(blockpos$mutable1.move(Direction.DOWN)); --l) {
                        }

                        if (l + 4 <= i) {
                            final int j1 = i1 - l;
                            if (j1 <= 0 || j1 >= 3) {
                                blockpos$mutable1.setY(l);
                                if (PortalHelper.canHostFrame(world, blockpos$mutable1, blockpos$mutable, direction, 0)) {
                                    final double d2 = portalPos.distSqr(blockpos$mutable1);
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
            final Direction direction1 = direction.getClockWise();
            if (!worldborder.isWithinBounds(blockpos)) {
                return;
            }

            for(int l1 = -1; l1 < 2; ++l1) {
                for(int k2 = 0; k2 < 2; ++k2) {
                    for(int i3 = -1; i3 < 3; ++i3) {
                        final BlockState blockstate1 = i3 < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
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
            final BlockState blockstate = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);

            for(int j2 = 0; j2 < 2; ++j2) {
                for(int l2 = 0; l2 < 3; ++l2) {
                    blockpos$mutable.setWithOffset(blockpos, j2 * direction.getStepX(), l2, j2 * direction.getStepZ());
                    world.setBlock(blockpos$mutable, blockstate, 18);
                }
            }
        }

    }

    private static boolean canHostFrame(final ServerWorld world, final BlockPos p_242955_1_, final BlockPos.Mutable p_242955_2_, final Direction p_242955_3_, final int p_242955_4_) {
        final Direction direction = p_242955_3_.getClockWise();

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
                    final BlockState blockstate = k == -1 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    world.setBlockAndUpdate(blockpos$mutable.set(x, y, z).move(j, k, i), blockstate);
                }
            }
        }
    }

    public static Function<Boolean, Entity> createVanillaEntityPortalLogic(final Entity entity, final net.minecraft.util.math.vector.Vector3d destination,
            final ServerWorld fromWorld, final ServerWorld toWorld, final PortalType portal) {

        return spawnInPortal -> {
            final RegistryKey<World> fromWorldLocation = fromWorld.dimension();
            final RegistryKey<World> toWorldLocation = toWorld.dimension();
            double x = destination.x;
            double y = destination.y;
            double z = destination.z;
            float xRot = entity.xRot;
            float yRot = entity.yRot;
            net.minecraft.util.math.vector.Vector3d deltaMovement = entity.getDeltaMovement();

            fromWorld.getProfiler().push("moving");

            final BlockPos toSpawnPos = toWorld.getSharedSpawnPos();
            final boolean fromEndToOverworld = World.END.equals(fromWorldLocation) && World.OVERWORLD.equals(toWorldLocation);

            // Entering an end portal from THE_END to OVERWORLD after exiting
            if (!(portal instanceof NetherPortalType) && fromEndToOverworld) {
                x = toSpawnPos.getX();
                y = toWorld.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, toSpawnPos.getX(), toSpawnPos.getZ());
                z = toSpawnPos.getZ();
            } else if ((!(portal instanceof NetherPortalType) && World.NETHER.equals(toWorldLocation))) {
                // Sponge End
                x = toSpawnPos.getX();
                y = toSpawnPos.getY();
                z = toSpawnPos.getZ();
                yRot = 90.0F;
                xRot = 0.0F;
            } else {
                // Use platform move factor instead of Vanilla
                // TODO: PlatformDimensionTypeBridge
                final double moveFactor = DimensionType.getTeleportationScale(fromWorld.dimensionType(), toWorld.dimensionType());
                x *= moveFactor;
                z *= moveFactor;
            }

            toWorld.getProfiler().pop();
            toWorld.getProfiler().push("placing");
            final double d7 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().getMinX() + 16.0D);
            final double d4 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().getMinZ() + 16.0D);
            final double d5 = Math.min(2.9999872E7D, toWorld.getWorldBorder().getMaxX() - 16.0D);
            final double d6 = Math.min(2.9999872E7D, toWorld.getWorldBorder().getMaxZ() - 16.0D);
            x = MathHelper.clamp(x, d7, d5);
            z = MathHelper.clamp(z, d4, d6);

            // THE_END -> OVERWORLD is a straight port, no actual portal detection. Turn off the spawn flag
            // <ANY> -> THE_END is a straight port onto the obsidian slab. Turn off spawn flag
            if (fromEndToOverworld || World.END.equals(toWorldLocation)) {
                spawnInPortal = false;
            }

            boolean isPortalThere = false;

            // If the portal is our API re-implementation of the Nether portal, allow that to veto and handle repositioning (as someone specified
            // intent to have it handle placing the nether portal/etc regardless of where we're coming and going)
            if (portal instanceof NetherPortalType || spawnInPortal) {
                final Optional<TeleportationRepositioner.Result> findResult = toWorld.getPortalForcer()
                        .findPortalAround(new BlockPos(x, y, z), World.NETHER.equals(toWorldLocation));

                isPortalThere = findResult.isPresent();
                if (isPortalThere) {
                    final TeleportationRepositioner.Result result = findResult.get();
                    final PortalInfo portalInfo = PortalHelper.getPortalInfo(
                            toWorld,
                            result,
                            entity,
                            entity.yRot,
                            entity.xRot
                    );

                    x = portalInfo.pos.x;
                    y = portalInfo.pos.y;
                    z = portalInfo.pos.z;

                    yRot = portalInfo.yRot;
                    xRot = portalInfo.xRot;

                    deltaMovement = portalInfo.speed;
                }
            }

            if (spawnInPortal && !isPortalThere) {
                return entity;
            }

            final MoveEntityEvent event = SpongeEventFactory.createChangeEntityWorldEventReposition(
                    PhaseTracker.getCauseStackManager().getCurrentCause(),
                    (org.spongepowered.api.entity.Entity) entity,
                    (org.spongepowered.api.world.server.ServerWorld) fromWorld,
                    ((org.spongepowered.api.entity.Entity) entity).getPosition(),
                    new Vector3d(x, y, z),
                    (org.spongepowered.api.world.server.ServerWorld) toWorld,
                    new Vector3d(x, y, z),
                    (org.spongepowered.api.world.server.ServerWorld) toWorld);

            if (SpongeCommon.postEvent(event)) {
                entity.setDeltaMovement(net.minecraft.util.math.vector.Vector3d.ZERO);
                return entity;
            }

            x = event.getDestinationPosition().getX();
            y = event.getDestinationPosition().getY();
            z = event.getDestinationPosition().getZ();

            ((PlatformEntityBridge) entity).bridge$remove(true);
            final Entity result = entity.getType().create(toWorld);
            if (result != null) {
                result.copyPosition(entity);
                result.moveTo(new BlockPos(x, y, z), yRot, xRot);
                result.setDeltaMovement(deltaMovement);
                toWorld.addFromAnotherDimension(result);
            }

            return result;
        };
    }

    public static Function<Boolean, @Nullable Entity> createVanillaPlayerPortalLogic(final ServerPlayerEntity player,
            final net.minecraft.util.math.vector.Vector3d destination,
            final ServerWorld fromWorld,
            final ServerWorld toWorld,
            final PortalType portal) {

        return spawnInPortal -> {
            final RegistryKey<World> fromWorldLocation = fromWorld.dimension();
            final RegistryKey<World> toWorldLocation = toWorld.dimension();
            double x = destination.x;
            double y = destination.y;
            double z = destination.z;
            net.minecraft.util.math.vector.Vector3d deltaMovement = player.getDeltaMovement();
            float xRot = player.xRot;
            float yRot = player.yRot;

            fromWorld.getProfiler().push("moving");

            final boolean isSameWorld = fromWorld == toWorld;

            final BlockPos toSpawnPos = toWorld.getSharedSpawnPos();
            final boolean fromEndToOverworld = World.END.equals(fromWorldLocation) && World.OVERWORLD.equals(toWorldLocation);

            // Entering an end portal from THE_END to OVERWORLD after beating the Dragon/etc
            if (!(portal instanceof NetherPortalType) && fromEndToOverworld) {
                x = toSpawnPos.getX();
                y = toWorld.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, toSpawnPos.getX(), toSpawnPos.getZ());
                z = toSpawnPos.getZ();
            } else if (!(portal instanceof NetherPortalType) && World.END.equals(toWorldLocation)) {
                // Sponge End
                x = toSpawnPos.getX();
                y = toSpawnPos.getY();
                z = toSpawnPos.getZ();
                yRot = 90.0F;
                xRot = 0.0F;
            } else {
                // Use platform move factor instead of Vanilla
                // TODO: PlatformDimensionTypeBridge
                final double moveFactor = DimensionType.getTeleportationScale(fromWorld.dimensionType(), toWorld.dimensionType());
                x *= moveFactor;
                z *= moveFactor;
            }

            fromWorld.getProfiler().pop();
            fromWorld.getProfiler().push("placing");
            final double d7 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().getMinX() + 16.0D);
            final double d4 = Math.min(-2.9999872E7D, toWorld.getWorldBorder().getMinZ() + 16.0D);
            final double d5 = Math.min(2.9999872E7D, toWorld.getWorldBorder().getMaxX() - 16.0D);
            final double d6 = Math.min(2.9999872E7D, toWorld.getWorldBorder().getMaxZ() - 16.0D);
            x = MathHelper.clamp(x, d7, d5);
            z = MathHelper.clamp(z, d4, d6);

            // THE_END -> OVERWORLD is a straight port, no actual portal detection. Turn off the spawn flag
            // <ANY> -> THE_END is a straight port onto the obsidian slab. Turn off spawn flag
            if (fromEndToOverworld || World.NETHER.equals(toWorldLocation)) {
                spawnInPortal = false;
            }

            // If the portal is our API re-implementation of the Nether portal, allow that to veto and handle repositioning (as someone specified
            // intent to have it handle placing the nether portal/etc regardless of where we're coming and going)-
            if (portal instanceof NetherPortalType) {
                spawnInPortal = true;
            }

            ((ServerPlayerEntityBridge) player).bridge$sendChangeDimension(toWorld.dimensionType(),
                    toWorldLocation,
                    BiomeManager.obfuscateSeed(toWorld.getSeed()),
                    player.gameMode.getGameModeForPlayer(),
                    player.gameMode.getPreviousGameModeForPlayer(),
                    false,
                    toWorld.isFlat(),
                    true);

            boolean isPortalThere = false;

            if (spawnInPortal) {
                final Optional<TeleportationRepositioner.Result> findResult = toWorld.getPortalForcer()
                        .findPortalAround(new BlockPos(x, y, z), World.NETHER.equals(toWorldLocation));

                isPortalThere = findResult.isPresent();
                if (isPortalThere) {
                    final TeleportationRepositioner.Result result = findResult.get();
                    final PortalInfo portalInfo = PortalHelper.getPortalInfo(
                            toWorld,
                            result,
                            player,
                            player.yRot,
                            player.xRot
                    );

                    x = portalInfo.pos.x;
                    y = portalInfo.pos.y;
                    z = portalInfo.pos.z;

                    yRot = portalInfo.yRot;
                    xRot = portalInfo.xRot;

                    deltaMovement = portalInfo.speed;
                }
            }

            final MoveEntityEvent event = SpongeEventFactory.createChangeEntityWorldEventReposition(
                    PhaseTracker.getCauseStackManager().getCurrentCause(),
                    (ServerPlayer) player,
                    (org.spongepowered.api.world.server.ServerWorld) fromWorld,
                    ((ServerPlayer) player).getPosition(),
                    new Vector3d(x, y, z),
                    (org.spongepowered.api.world.server.ServerWorld) toWorld,
                    new Vector3d(x, y, z),
                    (org.spongepowered.api.world.server.ServerWorld) toWorld);

            if (SpongeCommon.postEvent(event)) {
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot);
                player.connection.resetPosition();
                player.setDeltaMovement(net.minecraft.util.math.vector.Vector3d.ZERO);
                return player;
            }

            x = event.getDestinationPosition().getX();
            y = event.getDestinationPosition().getY();
            z = event.getDestinationPosition().getZ();

            // Only create the obsidian platform if this not inter-world, not the API nether portal, and we're going to Vanilla's The End
            if (!isSameWorld && (!(portal instanceof NetherPortalType) && World.END.equals(toWorldLocation))) {
                PortalHelper.generateEndObsidianPlatform(toWorld, (int) x, (int) y, (int) z);
                player.setDeltaMovement(net.minecraft.util.math.vector.Vector3d.ZERO);
            // Only set the entered nether position if we've spawned in a portal, it isn't inter-world, not the API nether portal, and we're going
            // to Vanilla's The Nether
            } else if (spawnInPortal && !isSameWorld && (!(portal instanceof NetherPortalType) && World.NETHER.equals(toWorldLocation))) {
                ((ServerPlayerEntityAccessor) player).accessor$enteredNetherPosition(VecHelper.toVanillaVector3d(((ServerPlayer) player).getPosition()));
            }

            if (spawnInPortal && !isPortalThere) {
                // Apply snapshot values now that event has been fired
                player.setPos(x, y, z);
                ((EntityAccessor) player).invoker$setRot(yRot, xRot);

                final Optional<TeleportationRepositioner.Result> createPortalResult = toWorld.getPortalForcer()
                        .createPortal(new BlockPos(x, y, z), Direction.fromYRot(yRot).getAxis());
                if (!createPortalResult.isPresent()) {
                    SpongeCommon.getLogger().error("Unable to create a portal, likely target out of worldborder");
                    return null;
                }
                final PortalInfo portalInfo = PortalHelper.getPortalInfo(
                        toWorld,
                        createPortalResult.get(),
                        player,
                        yRot,
                        xRot
                );

                // Grab values one last time, only to allow us to call setLocationAndAngles below
                x = portalInfo.pos.x;
                y = portalInfo.pos.y;
                z = portalInfo.pos.z;

                yRot = portalInfo.yRot;
                xRot = portalInfo.xRot;

                deltaMovement = portalInfo.speed;
            }

            player.setDeltaMovement(deltaMovement);
            player.connection.teleport(x, y, z, yRot, xRot);
            player.connection.resetPosition();
            fromWorld.getProfiler().pop();
            return player;
        };
    }

    private static PortalInfo getPortalInfo(final ServerWorld targetWorld,
            final TeleportationRepositioner.Result result,
            final Entity player,
            final float yRot,
            final float xRot) {
        final Direction.Axis direction =
                targetWorld.getBlockState(result.minCorner).getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
        return PortalSize.createPortalInfo(
                targetWorld,
                result,
                direction,
                ((EntityAccessor) player).invoker$getRelativePortalPosition(direction, result),
                player.getDimensions(player.getPose()),
                player.getDeltaMovement(),
                yRot,
                xRot
        );
    }

}
