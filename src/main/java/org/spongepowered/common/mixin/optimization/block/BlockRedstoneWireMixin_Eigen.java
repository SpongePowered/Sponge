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
package org.spongepowered.common.mixin.optimization.block;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.mixin.plugin.RedstoneWireTurbo;

import java.util.List;
import java.util.Set;

@Mixin(value = RedstoneWireBlock.class, priority = 1001)
public abstract class BlockRedstoneWireMixin_Eigen extends Block {

    @Shadow @Final private Set<BlockPos> blocksNeedingUpdate;
    @Shadow private boolean canProvidePower;
    @Shadow protected abstract int getMaxCurrentStrength(World worldIn, BlockPos pos, int strength);

    protected BlockRedstoneWireMixin_Eigen(Material materialIn) {
        super(materialIn);
    }

    /******* [theosib] Beginning of modified code *******/
    // Note:  Search for [theosib] to find isolated lines that were modified.

    // Set to true to restore the vanilla algorithm for propagating redstone wire changed.
    private boolean old_search = SpongeImpl.getGlobalConfigAdapter().getConfig().getOptimizations().getEigenRedstoneCategory().vanillaSearch();
    
    // Set to true to restore the vanilla algorithm for computing wire power levels when powering off.
    private boolean old_decrement = SpongeImpl.getGlobalConfigAdapter().getConfig().getOptimizations().getEigenRedstoneCategory().vanillaDecrement();

    // IMPORTANT:  It intended that these two flags be both true (for vanilla behavior) or both false
    // (for optimized behavior).  They are separated for testing purposes and to allow my earlier
    // 45% improvement to be enabled by itself.

    // The bulk of the new functionality is found in RedstoneWireTurbo.java
    private RedstoneWireTurbo turbo = new RedstoneWireTurbo((RedstoneWireBlock)(Object) this);

    @Inject(method = "updateSurroundingRedstone", at = @At("HEAD"), cancellable = true)
    private void onUpdateSurroundingRedstone(World worldIn, BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir) {
        if (!worldIn.isRemote) {
            this.updateSurroundingRedstone(worldIn, pos, state, null);
            cir.setReturnValue(state);
        }
    }

    /**
     * @author unknown
     * @reason eigen
     */
    @Override
    @SuppressWarnings("deprecation")
    @Overwrite
    public void func_189540_a(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (!worldIn.isRemote)
        {
            if (this.func_176196_c(worldIn, pos))
            {
                // [theosib] Added fourth argument: fromPos is the block position that updated the redstone
                // wire block.  We use this to help determine the direction of information flow.
                this.updateSurroundingRedstone(worldIn, pos, state, fromPos);
            }
            else
            {
                this.func_176226_b(worldIn, pos, state, 0);
                worldIn.func_175698_g(pos);
            }
        }
    }

    /**
     * @author unknown
     * @reason eigen
     */
    /*
     * Modified version of pre-existing updateSurroundingRedstone, which is called from
     * this.neighborChanged and a few other methods in this class.
     * Note: Added 'source' argument so to help determine direction of information flow
     */
    private BlockState updateSurroundingRedstone(World worldIn, BlockPos pos, BlockState state, BlockPos source)
    {
        if (this.old_search) {
            state = this.calculateCurrentChanges(worldIn, pos, pos, state);
            List<BlockPos> list = Lists.newArrayList(this.blocksNeedingUpdate);
            this.blocksNeedingUpdate.clear();
    
            for (BlockPos blockpos : list)
            {   
                worldIn.func_175685_c(blockpos, this, false);
            }
    
            return state;
        } else {
            return this.turbo.updateSurroundingRedstone(worldIn, pos, state, source);
        }
    }

    /**
     * @author unknown
     * @reason eigen
     */
    /*
     * Slightly modified method to compute redstone wire power levels from neighboring blocks.
     * Modifications cut the number of power level changes by about 45% from vanilla, and this 
     * optimization synergizes well with the breadth-first search implemented in 
     * RedstoneWireTurbo.
     * Note:  RedstoneWireTurbo contains a faster version of this code.
     * Note:  Made this public so that RedstoneWireTurbo can access it.
     */
    @Overwrite
    private BlockState calculateCurrentChanges(World worldIn, BlockPos pos1, BlockPos pos2, BlockState state)
    {
        BlockState iblockstate = state;
        int i = state.get(RedstoneWireBlock.POWER).intValue();
        int j = 0;
        j = this.getMaxCurrentStrength(worldIn, pos2, j);
        this.canProvidePower = false;
        int k = worldIn.getRedstonePowerFromNeighbors(pos1);
        this.canProvidePower = true;

        if (this.old_decrement) {
            // This code is totally redundant to if statements just below the loop.
            if (k > 0 && k > j - 1)
            {
                j = k;
            }
        }

        int l = 0;

        // The variable 'k' holds the maximum redstone power value of any adjacent blocks.
        // If 'k' has the highest level of all neighbors, then the power level of this 
        // redstone wire will be set to 'k'.  If 'k' is already 15, then nothing inside the 
        // following loop can affect the power level of the wire.  Therefore, the loop is 
        // skipped if k is already 15. 
        if (this.old_search || k < 15)
        for (Direction enumfacing : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = pos1.offset(enumfacing);
            boolean flag = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();

            if (flag)
            {
                l = this.getMaxCurrentStrength(worldIn, blockpos, l);
            }

            if (worldIn.getBlockState(blockpos).func_185915_l() && !worldIn.getBlockState(pos1.up()).func_185915_l())
            {
                if (flag && pos1.getY() >= pos2.getY())
                {
                    l = this.getMaxCurrentStrength(worldIn, blockpos.up(), l);
                }
            }
            else if (!worldIn.getBlockState(blockpos).func_185915_l() && flag && pos1.getY() <= pos2.getY())
            {
                l = this.getMaxCurrentStrength(worldIn, blockpos.down(), l);
            }
        }

        if (this.old_decrement) {
            // The old code would decrement the wire value only by 1 at a time.
            if (l > j)
            {
                j = l - 1;
            }
            else if (j > 0)
            {
                --j;
            }
            else
            {
                j = 0;
            }
    
            if (k > j - 1)
            {
                j = k;
            }
        } else {
            // The new code sets this redstonewire block's power level to the highest neighbor
            // minus 1.  This usually results in wire power levels dropping by 2 at a time.
            // This optimization alone has no impact on opdate order, only the number of updates.
            j = l-1;
            
            // If 'l' turns out to be zero, then j will be set to -1, but then since 'k' will
            // always be in the range of 0 to 15, the following if will correct that.
            if (k>j) j=k;
        }

        if (i != j)
        {
            state = state.func_177226_a(RedstoneWireBlock.POWER, Integer.valueOf(j));
            
            if (worldIn.getBlockState(pos1) == iblockstate)
            {
                worldIn.setBlockState(pos1, state, 2);
            }

            if (this.old_search) {
                // The new search algorithm keeps track of blocks needing updates in its own data structures,
                // so only add anything to blocksNeedingUpdate if we're using the vanilla update algorithm.
                this.blocksNeedingUpdate.add(pos1);

                for (Direction enumfacing1 : Direction.values())
                {   
                    this.blocksNeedingUpdate.add(pos1.offset(enumfacing1));
                }
            }
        }

        return state;
    }
}
