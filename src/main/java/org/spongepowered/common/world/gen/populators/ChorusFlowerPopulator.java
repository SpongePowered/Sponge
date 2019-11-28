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

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.SimplexNoiseGenerator;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.ChorusFlower;

import java.util.Random;

public class ChorusFlowerPopulator implements ChorusFlower {

    private SimplexNoiseGenerator noise;
    private long lastSeed = -1;
    private int exclusion = 1024;

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.CHORUS_FLOWER;
    }

    @Override
    public int getExclusionRadius() {
        return this.exclusion;
    }

    @Override
    public void setExclusionRadius(int radius) {
        checkArgument(radius >= 0, "Exclusion radius must be positive or zero");
        this.exclusion = radius;
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, Extent extent, Random rand) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        if (this.noise == null || world.getProperties().getSeed() != this.lastSeed) {
            this.lastSeed = world.getProperties().getSeed();
            this.noise = new SimplexNoiseGenerator(new Random(this.lastSeed));
        }

        World worldObj = (World) world;
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int chunkX = min.getX() / 16;
        int chunkZ = min.getZ() / 16;
        if ((long) min.getX() * (long) min.getX() + (long) min.getZ() * (long) min.getZ() > this.exclusion * this.exclusion) {
            if (this.func_185960_a(chunkX, chunkZ, 1, 1) > 40.0F) {
                int count = rand.nextInt(5);

                for (int n = 0; n < count; ++n) {
                    int x = rand.nextInt(size.getX());
                    int y = rand.nextInt(size.getZ());
                    int z = worldObj.getHeight(chunkPos.add(x, 0, y)).getY();

                    if (z > 0) {
                        if (worldObj.isAirBlock(chunkPos.add(x, z, y))
                                && worldObj.getBlockState(chunkPos.add(x, z - 1, y)).getBlock() == Blocks.END_STONE) {
                            ChorusFlowerBlock.generatePlant(worldObj, chunkPos.add(x, z, y), rand, 8);
                        }
                    }
                }
            }
        }
    }

    private float func_185960_a(int x, int z, int p_185960_3_, int p_185960_4_) {
        float f = x * 2 + p_185960_3_;
        float f1 = z * 2 + p_185960_4_;
        float f2 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * 8.0F;

        if (f2 > 80.0F) {
            f2 = 80.0F;
        }

        if (f2 < -100.0F) {
            f2 = -100.0F;
        }

        for (int i = -12; i <= 12; ++i) {
            for (int j = -12; j <= 12; ++j) {
                long k = x + i;
                long l = z + j;

                if (k * k + l * l > 4096L && this.noise.getValue(k, l) < -0.8999999761581421D) {
                    float f3 = (MathHelper.abs(k) * 3439.0F + MathHelper.abs(l) * 147.0F) % 13.0F + 9.0F;
                    f = p_185960_3_ - i * 2;
                    f1 = p_185960_4_ - j * 2;
                    float f4 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * f3;

                    if (f4 > 80.0F) {
                        f4 = 80.0F;
                    }

                    if (f4 < -100.0F) {
                        f4 = -100.0F;
                    }

                    if (f4 > f2) {
                        f2 = f4;
                    }
                }
            }
        }

        return f2;
    }
}
