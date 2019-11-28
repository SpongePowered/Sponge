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
package org.spongepowered.common.mixin.core.world.gen.feature;

import com.google.common.base.MoreObjects;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(WorldGenGlowStone1.class)
public abstract class WorldGenGlowstoneMixin extends WorldGeneratorMixin {

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason Change the number of iterations and the height of the cluster
     * depending on the respective variable amounts.
     */
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, final BlockPos position) {
        if (!worldIn.func_175623_d(position)) {
            return false;
        } else if (worldIn.func_180495_p(position.func_177984_a()).func_177230_c() != Blocks.field_150424_aL) {
            return false;
        } else {
            worldIn.func_180501_a(position, Blocks.field_150426_aN.func_176223_P(), 2);
            // Sponge start
            final int a = ((Glowstone) this).getAttemptsPerCluster().getFlooredAmount(rand);
            for (int i = 0; i < a; ++i) {
                final int xAdd = rand.nextInt(8) - rand.nextInt(8);
                final int zAdd = rand.nextInt(8) - rand.nextInt(8);
                final BlockPos blockpos1 = position.func_177982_a(xAdd, ((Glowstone) this).getClusterHeight().getFlooredAmount(rand), zAdd);
//                if (worldIn.getBlockState(blockpos1).getBlock().getMaterial() == Material.air) {
                if (bridge$isAir(worldIn.func_180495_p(blockpos1), worldIn, blockpos1)) {
                    // Sponge end
                    int j = 0;
                    final EnumFacing[] aenumfacing = EnumFacing.values();
                    final int k = aenumfacing.length;

                    for (int l = 0; l < k; ++l) {
                        final EnumFacing enumfacing = aenumfacing[l];

                        if (worldIn.func_180495_p(blockpos1.func_177972_a(enumfacing)).func_177230_c() == Blocks.field_150426_aN) {
                            ++j;
                        }

                        if (j > 1) {
                            break;
                        }
                    }

                    if (j == 1) {
                        worldIn.func_180501_a(blockpos1, Blocks.field_150426_aN.func_176223_P(), 2);
                    }
                }
            }

            return true;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Glowstone")
                .add("PerChunk", ((Glowstone) this).getClustersPerChunk())
                .add("PerCluster", ((Glowstone) this).getAttemptsPerCluster())
                .add("Height", ((Glowstone) this).getHeight())
                .add("ClusterHeight", ((Glowstone) this).getClusterHeight())
                .toString();
    }

}
