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
package org.spongepowered.common.mixin.optimization.world;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.util.math.IMixinBlockPos;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(World.class)
public abstract class MixinWorld_Inline_Valid_BlockPos {

    @Shadow public boolean processingLoadedTiles;


    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);
    @Shadow public abstract Chunk getChunkFromBlockCoords(BlockPos pos);
    @Shadow public abstract void notifyLightSet(BlockPos pos);
    @Shadow public abstract IChunkProvider getChunkProvider();

    @Shadow @Nullable public abstract TileEntity getPendingTileEntityAt(BlockPos p_189508_1_);

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return The block state at the desired position
     */
    @Overwrite
    public IBlockState getBlockState(BlockPos pos) {
        // Sponge - Replace with inlined method
        // if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((IMixinBlockPos) pos).isInvalidYPosition()) {
            // Sponge end
            return Blocks.AIR.getDefaultState();
        } else {
            Chunk chunk = this.getChunkFromBlockCoords(pos);
            return chunk.getBlockState(pos);
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return The tile entity at the desired position, or else null
     */
    @Overwrite
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        // Sponge - Replace with inlined method
        //  if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((IMixinBlockPos) pos).isInvalidYPosition()) {
            // Sponge End
            return null;
        } else {
            TileEntity tileentity = null;

            if (this.processingLoadedTiles) {
                tileentity = this.getPendingTileEntityAt(pos);
            }

            if (tileentity == null) {
                tileentity = this.getChunkFromBlockCoords(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
            }

            if (tileentity == null) {
                tileentity = this.getPendingTileEntityAt(pos);
            }

            return tileentity;
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return True if the block position is valid
     */
    @Overwrite
    protected boolean isValid(BlockPos pos) {
        return ((IMixinBlockPos) pos).isValidPosition();
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return True if the block position is outside build height
     */
    @Overwrite
    private boolean isOutsideBuildHeight(BlockPos pos) {
        return ((IMixinBlockPos) pos).isInvalidYPosition();
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param type The type of sky lighting
     * @param pos The position
     * @return The light for the defined sky type and block position
     */
    @Overwrite
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        if (pos.getY() < 0) {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());
        }

        // Sponge Start - Replace with inlined method to check
        // if (!this.isValid(pos)) // vanilla
        if (!((IMixinBlockPos) pos).isValidPosition()) {
            // Sponge End
            return type.defaultLightValue;
        } else {
            Chunk chunk = ((IMixinChunkProviderServer) this.getChunkProvider()).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null) {
                return type.defaultLightValue;
            }
            return chunk.getLightFor(type, pos);
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Inlines the isValid check to BlockPos.
     *
     * @param type The type of sky lighting
     * @param pos The block position
     * @param lightValue The light value to set to
     */
    @Overwrite
    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
        // Sponge Start - Replace with inlined Valid position check
        // if (this.isValid(pos)) // Vanilla
        if (((IMixinBlockPos) pos).isValidPosition()) { // Sponge - Replace with inlined method to check
            // Sponge End
            if (this.isBlockLoaded(pos)) {
                Chunk chunk = this.getChunkFromBlockCoords(pos);
                chunk.setLightFor(type, pos, lightValue);
                this.notifyLightSet(pos);
            }
        }
    }


    /**
     * @author gabizou - August 4th, 2016
     * @reason Inlines the isValidXZPosition check to BlockPos.
     *
     * @param bbox The AABB to check
     * @return True if the AABB collides with a block
     */
    @Overwrite
    public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
        List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
        int i = MathHelper.floor_double(bbox.minX) - 1;
        int j = MathHelper.ceiling_double_int(bbox.maxX) + 1;
        int k = MathHelper.floor_double(bbox.minY) - 1;
        int l = MathHelper.ceiling_double_int(bbox.maxY) + 1;
        int i1 = MathHelper.floor_double(bbox.minZ) - 1;
        int j1 = MathHelper.ceiling_double_int(bbox.maxZ) + 1;
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        try {
            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = i1; l1 < j1; ++l1) {
                    int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);

                    if (i2 != 2 && this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
                        for (int j2 = k; j2 < l; ++j2) {
                            if (i2 <= 0 || j2 != k && j2 != l - 1) {
                                blockpos$pooledmutableblockpos.setPos(k1, j2, l1);

                                // Sponge - Replace with inlined method
                                // if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) // Vanilla
                                if (!((IMixinBlockPos) (Object) blockpos$pooledmutableblockpos).isValidXZPosition()) {
                                    // Sponge End
                                    boolean flag1 = true;
                                    return flag1;
                                }

                                IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos);
                                iblockstate.addCollisionBoxToList((World) (Object) this, blockpos$pooledmutableblockpos, bbox, list, (Entity) null);

                                if (!list.isEmpty()) {
                                    boolean flag = true;
                                    return flag;
                                }
                            }
                        }
                    }
                }
            }

            return false;
        } finally {
            blockpos$pooledmutableblockpos.release();
        }
    }

}
