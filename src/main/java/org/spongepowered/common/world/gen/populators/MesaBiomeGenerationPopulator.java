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
package org.spongepowered.common.world.gen.populators;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;

import java.util.Arrays;
import java.util.Random;

public class MesaBiomeGenerationPopulator implements GenerationPopulator {

    private BlockState[] possibleBlocks;
    private long lastSeed;
    private NoiseGeneratorPerlin noise1;
    private NoiseGeneratorPerlin noise2;
    private NoiseGeneratorPerlin noise3;
    private boolean hasHills = false;
    private boolean hasTrees = false;

    private double[] stoneNoise;

    public MesaBiomeGenerationPopulator(boolean mesa, boolean trees) {
        this.hasHills = mesa;
        this.hasTrees = trees;
    }

    @Override
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {
        long seed = world.getProperties().getSeed();
        if (this.possibleBlocks == null || this.lastSeed != seed) {
            this.func_150619_a(seed);
        }

        if (this.noise1 == null || this.noise2 == null || this.lastSeed != seed) {
            Random random1 = new Random(seed);
            this.noise1 = new NoiseGeneratorPerlin(random1, 4);
            this.noise2 = new NoiseGeneratorPerlin(random1, 1);
        }

        this.lastSeed = seed;
        int cx = buffer.getBlockMin().getX();
        int cz = buffer.getBlockMin().getZ();
        Vector3i size = buffer.getBlockSize();

        this.stoneNoise = this.noise1.func_151599_a(this.stoneNoise, cx, cz, size.getX(), size.getZ(), 0.0625D, 0.0625D, 1.0D);

        Random rand = new Random(cx / 16 * 341873128712L + cz / 16 * 132897987541L);

        for (int x = 0; x < size.getX(); x++) {
            int xo = buffer.getBlockMin().getX() + x;
            for (int z = 0; z < size.getZ(); z++) {
                int zo = buffer.getBlockMin().getZ() + z;
                performOnColumn(rand, buffer, world, xo, zo, this.stoneNoise[x + z * 16]);
            }
        }

    }

    public void performOnColumn(Random p_180622_2_, MutableBlockVolume p_180622_3_, World world, int p_180622_4_, int p_180622_5_,
            double p_180622_6_) {
        double d5 = 0.0D;
        int k;
        int l;

        if (this.hasHills) {
            k = (p_180622_4_ & -16) + (p_180622_5_ & 15);
            l = (p_180622_5_ & -16) + (p_180622_4_ & 15);
            double d1 = Math.min(Math.abs(p_180622_6_), this.noise1.func_151601_a(k * 0.25D, l * 0.25D));

            if (d1 > 0.0D) {
                double d2 = 0.001953125D;
                double d3 = Math.abs(this.noise2.func_151601_a(k * d2, l * d2));
                d5 = d1 * d1 * 2.5D;
                double d4 = Math.ceil(d3 * 50.0D) + 14.0D;

                if (d5 > d4) {
                    d5 = d4;
                }

                d5 += 64.0D;
            }
        }

        k = p_180622_5_;
        l = p_180622_4_;
        int seaLevel = world.getSeaLevel();
        IBlockState iblockstate = Blocks.field_150406_ce.func_176223_P();
        IBlockState iblockstate3 = Blocks.field_150406_ce.func_176223_P();
        int i1 = (int) (p_180622_6_ / 3.0D + 3.0D + p_180622_2_.nextDouble() * 0.25D);
        boolean flag1 = Math.cos(p_180622_6_ / 3.0D * Math.PI) > 0.0D;
        int j1 = -1;
        boolean flag2 = false;

        for (int k1 = 255; k1 >= 0; --k1) {
            if (((IBlockState) p_180622_3_.getBlock(l, k1, k)).func_185904_a() == Material.field_151579_a && k1 < (int) d5) {
                p_180622_3_.setBlock(l, k1, k, (BlockState) Blocks.field_150348_b.func_176223_P());
            }

            if (k1 <= p_180622_2_.nextInt(5)) {
                p_180622_3_.setBlock(l, k1, k, (BlockState) Blocks.field_150357_h.func_176223_P());
            } else {
                IBlockState iblockstate1 = (IBlockState) p_180622_3_.getBlock(l, k1, k);

                if (iblockstate1.func_185904_a() == Material.field_151579_a) {
                    j1 = -1;
                } else if (iblockstate1.func_177230_c() == Blocks.field_150348_b) {
                    IBlockState iblockstate2;

                    if (j1 == -1) {
                        flag2 = false;

                        if (i1 <= 0) {
                            iblockstate = null;
                            iblockstate3 = Blocks.field_150348_b.func_176223_P();
                        } else if (k1 >= seaLevel - 4 && k1 <= seaLevel + 1) {
                            iblockstate = Blocks.field_150406_ce.func_176223_P();
                            iblockstate3 = Blocks.field_150406_ce.func_176223_P();
                        }

                        if (k1 < seaLevel && (iblockstate == null || iblockstate.func_185904_a() == Material.field_151579_a)) {
                            iblockstate = Blocks.field_150355_j.func_176223_P();
                        }

                        j1 = i1 + Math.max(0, k1 - seaLevel);

                        if (k1 >= seaLevel - 1) {
                            if (this.hasTrees && k1 > 86 + i1 * 2) {
                                if (flag1) {
                                    p_180622_3_.setBlock(l, k1, k,
                                            (BlockState) Blocks.field_150346_d.func_176223_P()
                                                    .func_177226_a(BlockDirt.field_176386_a, BlockDirt.DirtType.COARSE_DIRT));
                                } else {
                                    p_180622_3_.setBlock(l, k1, k, (BlockState) Blocks.field_150349_c.func_176223_P());
                                }
                            } else if (k1 > seaLevel + 3 + i1) {
                                if (k1 >= 64 && k1 <= 127) {
                                    if (flag1) {
                                        iblockstate2 = Blocks.field_150405_ch.func_176223_P();
                                    } else {
                                        iblockstate2 = this.func_180629_a(p_180622_4_, k1, p_180622_5_);
                                    }
                                } else {
                                    iblockstate2 =
                                            Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.ORANGE);
                                }

                                p_180622_3_.setBlock(l, k1, k, (BlockState) iblockstate2);
                            } else {
                                p_180622_3_.setBlock(l, k1, k,
                                        (BlockState) Blocks.field_150354_m.func_176223_P().func_177226_a(BlockSand.field_176504_a, BlockSand.EnumType.RED_SAND));
                                flag2 = true;
                            }
                        } else {
                            p_180622_3_.setBlock(l, k1, k, (BlockState) iblockstate3);

                            if (iblockstate3.func_177230_c() == Blocks.field_150406_ce) {
                                p_180622_3_.setBlock(l, k1, k,
                                        (BlockState) iblockstate3.func_177230_c().func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.ORANGE));
                            }
                        }
                    } else if (j1 > 0) {
                        --j1;

                        if (flag2) {
                            IBlockState clay = Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.ORANGE);
                            p_180622_3_ .setBlock(l, k1, k, (BlockState) clay);
                        } else {
                            iblockstate2 = this.func_180629_a(p_180622_4_, k1, p_180622_5_);
                            p_180622_3_.setBlock(l, k1, k, (BlockState) iblockstate2);
                        }
                    }
                }
            }
        }
    }

    public IBlockState func_180629_a(int p_180629_1_, int p_180629_2_, int p_180629_3_) {
        int l =
                (int) Math
                        .round(this.noise3.func_151601_a(p_180629_1_ * 1.0D / 512.0D, p_180629_1_ * 1.0D / 512.0D) * 2.0D);
        return (IBlockState) this.possibleBlocks[(p_180629_2_ + l + 64) % 64];
    }

    public void func_150619_a(long p_150619_1_) {
        this.possibleBlocks = new BlockState[64];
        Arrays.fill(this.possibleBlocks, Blocks.field_150405_ch.func_176223_P());
        Random random = new Random(p_150619_1_);
        this.noise3 = new NoiseGeneratorPerlin(random, 1);
        int j;

        for (j = 0; j < 64; ++j) {
            j += random.nextInt(5) + 1;

            if (j < 64) {
                this.possibleBlocks[j] =
                        (BlockState) Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.ORANGE);
            }
        }

        j = random.nextInt(4) + 2;
        int k;
        int l;
        int i1;
        int j1;

        for (k = 0; k < j; ++k) {
            l = random.nextInt(3) + 1;
            i1 = random.nextInt(64);

            for (j1 = 0; i1 + j1 < 64 && j1 < l; ++j1) {
                this.possibleBlocks[i1 + j1] =
                        (BlockState) Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.YELLOW);
            }
        }

        k = random.nextInt(4) + 2;
        int k1;

        for (l = 0; l < k; ++l) {
            i1 = random.nextInt(3) + 2;
            j1 = random.nextInt(64);

            for (k1 = 0; j1 + k1 < 64 && k1 < i1; ++k1) {
                this.possibleBlocks[j1 + k1] =
                        (BlockState) Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.BROWN);
            }
        }

        l = random.nextInt(4) + 2;

        for (i1 = 0; i1 < l; ++i1) {
            j1 = random.nextInt(3) + 1;
            k1 = random.nextInt(64);

            for (int l1 = 0; k1 + l1 < 64 && l1 < j1; ++l1) {
                this.possibleBlocks[k1 + l1] =
                        (BlockState) Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.RED);
            }
        }

        i1 = random.nextInt(3) + 3;
        j1 = 0;

        for (k1 = 0; k1 < i1; ++k1) {
            byte b0 = 1;
            j1 += random.nextInt(16) + 4;

            for (int i2 = 0; j1 + i2 < 64 && i2 < b0; ++i2) {
                this.possibleBlocks[j1 + i2] =
                        (BlockState) Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.WHITE);

                if (j1 + i2 > 1 && random.nextBoolean()) {
                    this.possibleBlocks[j1 + i2 - 1] =
                            (BlockState) Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.SILVER);
                }

                if (j1 + i2 < 63 && random.nextBoolean()) {
                    this.possibleBlocks[j1 + i2 + 1] =
                            (BlockState) Blocks.field_150406_ce.func_176223_P().func_177226_a(BlockColored.field_176581_a, EnumDyeColor.SILVER);
                }
            }
        }
    }

}
