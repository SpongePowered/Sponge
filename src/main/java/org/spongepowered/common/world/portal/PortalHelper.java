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
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

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

}
