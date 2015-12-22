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

import com.flowpowered.math.GenericMath;
import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenIceSpike;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.populator.IceSpike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenIceSpike.class)
public abstract class MixinWorldGenIceSpike extends WorldGenerator implements IceSpike {

    private VariableAmount height;
    private VariableAmount increase;
    private VariableAmount count;
    private double prob;

    @Inject(method = "<init>()V", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.height = VariableAmount.baseWithRandomAddition(7, 4);
        this.increase = VariableAmount.baseWithRandomAddition(10, 30);
        this.prob = 0.0166667D; // 1 in 60
        this.count = VariableAmount.fixed(2);
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        int x;
        int z;
        BlockPos chunkPos = new BlockPos(chunk.getBlockMin().getX(), chunk.getBlockMin().getY(), chunk.getBlockMin().getZ());
        int n = this.count.getFlooredAmount(random);
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(16) + 8;
            z = random.nextInt(16) + 8;
            generate(world, random, world.getHeight(chunkPos.add(x, 0, z)));
        }
    }

    /*
     * Author: Deamon - December 12th, 2015
     * 
     * Purpose: overwritten to make use of populator parameters
     */
    @Override
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        while (worldIn.isAirBlock(position) && position.getY() > 2) {
            position = position.down();
        }

        if (worldIn.getBlockState(position).getBlock() != Blocks.snow) {
            return false;
        } else {
            position = position.up(rand.nextInt(4));
            // Sponge start
            // rand.nextInt(4) + 7;
            int height = this.height.getFlooredAmount(rand);
            int width = height / 4 + rand.nextInt(2);
         
            // rand.nextInt(60) == 0)
            if (width > 1 && rand.nextDouble() < this.prob) 
            {
                // position.up(10 + rand.nextInt(30));
                position = position.up(this.increase.getFlooredAmount(rand));
            }
            // Sponge end
            int k;
            int l;

            for (k = 0; k < height; ++k) {
                float f = (1.0F - (float) k / (float) height) * (float) width;
                l = MathHelper.ceiling_float_int(f);

                for (int i1 = -l; i1 <= l; ++i1) {
                    float f1 = (float) MathHelper.abs_int(i1) - 0.25F;

                    for (int j1 = -l; j1 <= l; ++j1) {
                        float f2 = (float) MathHelper.abs_int(j1) - 0.25F;

                        if ((i1 == 0 && j1 == 0 || f1 * f1 + f2 * f2 <= f * f)
                                && (i1 != -l && i1 != l && j1 != -l && j1 != l || rand.nextFloat() <= 0.75F)) {
                            Block block = worldIn.getBlockState(position.add(i1, k, j1)).getBlock();

                            if (block.getMaterial() == Material.air || block == Blocks.dirt || block == Blocks.snow || block == Blocks.ice) {
                                this.setBlock(worldIn, position.add(i1, k, j1), Blocks.packed_ice);
                            }

                            if (k != 0 && l > 1) {
                                block = worldIn.getBlockState(position.add(i1, -k, j1)).getBlock();

                                if (block.getMaterial() == Material.air || block == Blocks.dirt || block == Blocks.snow || block == Blocks.ice) {
                                    this.setBlock(worldIn, position.add(i1, -k, j1), Blocks.packed_ice);
                                }
                            }
                        }
                    }
                }
            }

            k = width - 1;

            if (k < 0) {
                k = 0;
            } else if (k > 1) {
                k = 1;
            }

            for (int k1 = -k; k1 <= k; ++k1) {
                l = -k;

                while (l <= k) {
                    BlockPos blockpos1 = position.add(k1, -1, l);
                    int l1 = 50;

                    if (Math.abs(k1) == 1 && Math.abs(l) == 1) {
                        l1 = rand.nextInt(5);
                    }

                    while (true) {
                        if (blockpos1.getY() > 50) {
                            Block block1 = worldIn.getBlockState(blockpos1).getBlock();

                            if (block1.getMaterial() == Material.air || block1 == Blocks.dirt || block1 == Blocks.snow || block1 == Blocks.ice
                                    || block1 == Blocks.packed_ice) {
                                this.setBlock(worldIn, blockpos1, Blocks.packed_ice);
                                blockpos1 = blockpos1.down();
                                --l1;

                                if (l1 <= 0) {
                                    blockpos1 = blockpos1.down(rand.nextInt(5) + 1);
                                    l1 = rand.nextInt(5);
                                }

                                continue;
                            }
                        }

                        ++l;
                        break;
                    }
                }
            }

            return true;
        }
    }

    @Override
    public VariableAmount getHeight() {
        return this.height;
    }

    @Override
    public void setHeight(VariableAmount height) {
        this.height = height;
    }

    @Override
    public double getExtremeSpikeProbability() {
        return this.prob;
    }

    @Override
    public void setExtremeSpikeProbability(double p) {
        this.prob = GenericMath.clamp(p, 0, 1);
    }

    @Override
    public VariableAmount getExtremeSpikeIncrease() {
        return this.increase;
    }

    @Override
    public void setExtremeSpikeIncrease(VariableAmount increase) {
        this.increase = increase;
    }

    @Override
    public VariableAmount getSpikesPerChunk() {
        return this.count;
    }

    @Override
    public void setSpikesPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("Type", "IceSpike")
                .add("ExtremeChance", this.prob)
                .add("ExtremeIncrease", this.increase)
                .add("PerChunk", this.count)
                .toString();
    }

}
