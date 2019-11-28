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
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.IceSpikeFeature;
import org.spongepowered.api.world.gen.populator.IceSpike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(IceSpikeFeature.class)
public abstract class WorldGenIceSpikeMixin extends WorldGeneratorMixin {

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason overwritten to make use of populator parameters
     */
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, BlockPos position) {
        while (worldIn.func_175623_d(position) && position.func_177956_o() > 2) {
            position = position.func_177977_b();
        }

        if (worldIn.func_180495_p(position).func_177230_c() != Blocks.field_150433_aE) {
            return false;
        }
        position = position.func_177981_b(rand.nextInt(4));
        // Sponge Start
        final int height =  ((IceSpike) this).getHeight().getFlooredAmount(rand);
        final int width = height / 4 + rand.nextInt(2);
        if (width > 1 && rand.nextDouble() < ((IceSpike) this).getExtremeSpikeProbability()) {
            // position.up(10 + rand.nextInt(30));
            position = position.func_177981_b( ((IceSpike) this).getExtremeSpikeIncrease().getFlooredAmount(rand));
        }
        // Sponge End
        if (width > 1 && rand.nextInt(60) == 0) {
            position = position.func_177981_b(10 + rand.nextInt(30));
        }

        for (int k = 0; k < height; ++k) {
            final float f = (1.0F - (float) k / (float) height) * width;
            final int l = MathHelper.func_76123_f(f);

            for (int i1 = -l; i1 <= l; ++i1) {
                final float f1 = MathHelper.func_76130_a(i1) - 0.25F;

                for (int j1 = -l; j1 <= l; ++j1) {
                    final float f2 = MathHelper.func_76130_a(j1) - 0.25F;

                    if ((i1 == 0 && j1 == 0 || f1 * f1 + f2 * f2 <= f * f)
                            && (i1 != -l && i1 != l && j1 != -l && j1 != l || rand.nextFloat() <= 0.75F)) {
                        IBlockState iblockstate = worldIn.func_180495_p(position.func_177982_a(i1, k, j1));
                        Block block = iblockstate.func_177230_c();

                        if (iblockstate.func_185904_a() == Material.field_151579_a || block == Blocks.field_150346_d || block == Blocks.field_150433_aE || block == Blocks.field_150432_aD) {
                            this.setBlockAndNotifyAdequately(worldIn, position.func_177982_a(i1, k, j1), Blocks.field_150403_cj.func_176223_P());
                        }

                        if (k != 0 && l > 1) {
                            iblockstate = worldIn.func_180495_p(position.func_177982_a(i1, -k, j1));
                            block = iblockstate.func_177230_c();

                            if (iblockstate.func_185904_a() == Material.field_151579_a || block == Blocks.field_150346_d || block == Blocks.field_150433_aE || block == Blocks.field_150432_aD) {
                                this.setBlockAndNotifyAdequately(worldIn, position.func_177982_a(i1, -k, j1), Blocks.field_150403_cj.func_176223_P());
                            }
                        }
                    }
                }
            }
        }

        int k1 = width - 1;

        if (k1 < 0) {
            k1 = 0;
        } else if (k1 > 1) {
            k1 = 1;
        }

        for (int l1 = -k1; l1 <= k1; ++l1) {
            for (int i2 = -k1; i2 <= k1; ++i2) {
                BlockPos blockpos = position.func_177982_a(l1, -1, i2);
                int j2 = 50;

                if (Math.abs(l1) == 1 && Math.abs(i2) == 1) {
                    j2 = rand.nextInt(5);
                }

                while (blockpos.func_177956_o() > 50) {
                    final IBlockState iblockstate1 = worldIn.func_180495_p(blockpos);
                    final Block block1 = iblockstate1.func_177230_c();

                    if (iblockstate1.func_185904_a() != Material.field_151579_a && block1 != Blocks.field_150346_d && block1 != Blocks.field_150433_aE && block1 != Blocks.field_150432_aD
                            && block1 != Blocks.field_150403_cj) {
                        break;
                    }

                    this.setBlockAndNotifyAdequately(worldIn, blockpos, Blocks.field_150403_cj.func_176223_P());
                    blockpos = blockpos.func_177977_b();
                    --j2;

                    if (j2 <= 0) {
                        blockpos = blockpos.func_177979_c(rand.nextInt(5) + 1);
                        j2 = rand.nextInt(5);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Type", "IceSpike")
            .add("ExtremeChance", ((IceSpike) this).getExtremeSpikeProbability())
            .add("ExtremeIncrease",  ((IceSpike) this).getExtremeSpikeIncrease())
            .add("PerChunk", ((IceSpike) this).getSpikesPerChunk())
            .toString();
    }

}
