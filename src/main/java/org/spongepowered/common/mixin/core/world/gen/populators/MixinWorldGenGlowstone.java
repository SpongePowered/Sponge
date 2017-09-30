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
package org.spongepowered.common.mixin.core.world.gen.populators;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenGlowStone1.class)
public abstract class MixinWorldGenGlowstone extends MixinWorldGenerator implements Glowstone {

    private VariableAmount clusterheight;
    private VariableAmount height;
    private VariableAmount count;
    private VariableAmount attempts;

    @Inject(method = "<init>()V", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.count = VariableAmount.baseWithRandomAddition(1, 10);
        this.attempts = VariableAmount.fixed(1500);
        this.clusterheight = VariableAmount.baseWithRandomAddition(0, 12);
        this.height = VariableAmount.baseWithRandomAddition(4, 120);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.GLOWSTONE;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        int n = this.count.getFlooredAmount(random);
        int x, y, z;

        for (int i = 0; i < n; i++) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            y = this.height.getFlooredAmount(random);
            generate(world, random, position.add(x, y, z));
        }
    }

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason Change the number of iterations and the height of the cluster
     * depending on the respective variable amounts.
     */
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (!worldIn.isAirBlock(position)) {
            return false;
        } else if (worldIn.getBlockState(position.up()).getBlock() != Blocks.NETHERRACK) {
            return false;
        } else {
            worldIn.setBlockState(position, Blocks.GLOWSTONE.getDefaultState(), 2);
            // Sponge start
            int a = this.attempts.getFlooredAmount(rand);
            for (int i = 0; i < a; ++i) {
                BlockPos blockpos1 =
                        position.add(rand.nextInt(8) - rand.nextInt(8), this.clusterheight.getFlooredAmount(rand), rand.nextInt(8) - rand.nextInt(8));
//                if (worldIn.getBlockState(blockpos1).getBlock().getMaterial() == Material.air) {
                if (isAir(worldIn.getBlockState(blockpos1), worldIn, blockpos1)) {
                    // Sponge end
                    int j = 0;
                    EnumFacing[] aenumfacing = EnumFacing.values();
                    int k = aenumfacing.length;

                    for (int l = 0; l < k; ++l) {
                        EnumFacing enumfacing = aenumfacing[l];

                        if (worldIn.getBlockState(blockpos1.offset(enumfacing)).getBlock() == Blocks.GLOWSTONE) {
                            ++j;
                        }

                        if (j > 1) {
                            break;
                        }
                    }

                    if (j == 1) {
                        worldIn.setBlockState(blockpos1, Blocks.GLOWSTONE.getDefaultState(), 2);
                    }
                }
            }

            return true;
        }
    }

    @Override
    public VariableAmount getClustersPerChunk() {
        return this.count;
    }

    @Override
    public void setClustersPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public VariableAmount getAttemptsPerCluster() {
        return this.attempts;
    }

    @Override
    public void setAttemptsPerCluster(VariableAmount attempts) {
        this.attempts = attempts;
    }

    @Override
    public VariableAmount getClusterHeight() {
        return this.clusterheight;
    }

    @Override
    public void setClusterHeight(VariableAmount height) {
        this.clusterheight = height;
    }

    @Override
    public VariableAmount getHeight() {
        return this.height;
    }

    @Override
    public void setHeight(VariableAmount height) {
        this.height = checkNotNull(height);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Glowstone")
                .add("PerChunk", this.count)
                .add("PerCluster", this.attempts)
                .add("Height", this.height)
                .add("ClusterHeight", this.clusterheight)
                .toString();
    }

}
