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
package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.SimplexNoiseGenerator;
import net.minecraft.world.gen.feature.EndIslandFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.EndIsland;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

import javax.annotation.Nullable;

@Mixin(EndIslandFeature.class)
public abstract class WorldGenEndIslandMixin_API extends Feature implements EndIsland {

    @Nullable private SimplexNoiseGenerator api$noise;
    private VariableAmount api$initial = VariableAmount.baseWithRandomAddition(4, 3);
    private VariableAmount api$decrement = VariableAmount.baseWithRandomAddition(0.5, 2);
    private BlockState api$state = BlockTypes.END_STONE.getDefaultState();
    private long api$lastSeed = -1;
    private int api$exclusion = 1024;

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.END_ISLAND;
    }

    @Override
    public VariableAmount getStartingRadius() {
        return this.api$initial;
    }

    @Override
    public void setStartingRadius(final VariableAmount radius) {
        this.api$initial = checkNotNull(radius);
    }

    @Override
    public VariableAmount getRadiusDecrement() {
        return this.api$decrement;
    }

    @Override
    public void setRadiusDecrement(final VariableAmount decrement) {
        this.api$decrement = checkNotNull(decrement);
    }

    @Override
    public BlockState getIslandBlock() {
        return this.api$state;
    }

    @Override
    public void setIslandBlock(final BlockState state) {
        this.api$state = checkNotNull(state);
    }
    
    @Override
    public int getExclusionRadius() {
        return this.api$exclusion;
    }
    
    @Override
    public void setExclusionRadius(final int radius) {
        checkArgument(radius >= 0);
        this.api$exclusion = radius;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random rand) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        if (this.api$noise == null || worldIn.getProperties().getSeed() != this.api$lastSeed) {
            this.api$lastSeed = worldIn.getProperties().getSeed();
            this.api$noise = new SimplexNoiseGenerator(new Random(this.api$lastSeed));
        }
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        final int chunkX = min.getX() / 16;
        final int chunkZ = min.getZ() / 16;
        if ((long) min.getX() * (long) min.getX() + (long) min.getZ() * (long) min.getZ() > this.api$exclusion * this.api$exclusion) {
            final float f = this.implClone$chunkGeneratorEnd$getIslandHeightValue(chunkX, chunkZ, 1, 1);

            if (f < -20.0F && rand.nextInt(14) == 0) {
                func_180709_b(world, rand, chunkPos.add(rand.nextInt(size.getX()), 55 + rand.nextInt(16), rand.nextInt(size.getZ())));
                if (rand.nextInt(4) == 0) {
                    func_180709_b((World) worldIn, rand, chunkPos.add(rand.nextInt(size.getX()), 55 + rand.nextInt(16), rand.nextInt(size.getZ())));
                }
            }
        }
    }

    private float implClone$chunkGeneratorEnd$getIslandHeightValue(final int p_185960_1_, final int p_185960_2_, final int p_185960_3_, final int p_185960_4_) {
        float f = p_185960_1_ * 2 + p_185960_3_;
        float f1 = p_185960_2_ * 2 + p_185960_4_;
        float f2 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * 8.0F;

        if (f2 > 80.0F) {
            f2 = 80.0F;
        }

        if (f2 < -100.0F) {
            f2 = -100.0F;
        }

        for (int i = -12; i <= 12; ++i) {
            for (int j = -12; j <= 12; ++j) {
                final long k = p_185960_1_ + i;
                final long l = p_185960_2_ + j;

                if (k * k + l * l > 4096L && this.api$noise.getValue(k, l) < -0.8999999761581421D) {
                    final float f3 = (MathHelper.abs(k) * 3439.0F + MathHelper.abs(l) * 147.0F) % 13.0F + 9.0F;
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
