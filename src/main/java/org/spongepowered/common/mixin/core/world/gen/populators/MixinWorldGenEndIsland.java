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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.world.gen.feature.WorldGenEndIsland;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.EndIsland;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenEndIsland.class)
public abstract class MixinWorldGenEndIsland extends WorldGenerator implements EndIsland {

    private NoiseGeneratorSimplex noise;
    private long lastSeed = -1;

    private VariableAmount initial;
    private VariableAmount decrement;
    private BlockState state;
    private int exclusion = 1024;

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.initial = VariableAmount.baseWithRandomAddition(4, 3);
        this.decrement = VariableAmount.baseWithRandomAddition(0.5, 2);
        this.state = BlockTypes.END_STONE.getDefaultState();
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.END_ISLAND;
    }

    @Override
    public VariableAmount getStartingRadius() {
        return this.initial;
    }

    @Override
    public void setStartingRadius(VariableAmount radius) {
        this.initial = checkNotNull(radius);
    }

    @Override
    public VariableAmount getRadiusDecrement() {
        return this.decrement;
    }

    @Override
    public void setRadiusDecrement(VariableAmount decrement) {
        this.decrement = checkNotNull(decrement);
    }

    @Override
    public BlockState getIslandBlock() {
        return this.state;
    }

    @Override
    public void setIslandBlock(BlockState state) {
        this.state = checkNotNull(state);
    }
    
    @Override
    public int getExclusionRadius() {
        return this.exclusion;
    }
    
    @Override
    public void setExclusionRadius(int radius) {
        checkArgument(radius >= 0);
        this.exclusion = radius;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random rand) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        if (this.noise == null || worldIn.getProperties().getSeed() != this.lastSeed) {
            this.lastSeed = worldIn.getProperties().getSeed();
            this.noise = new NoiseGeneratorSimplex(new Random(this.lastSeed));
        }
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int chunkX = min.getX() / 16;
        int chunkZ = min.getZ() / 16;
        if ((long) min.getX() * (long) min.getX() + (long) min.getZ() * (long) min.getZ() > this.exclusion * this.exclusion) {
            float f = this.func_185960_a(chunkX, chunkZ, 1, 1);

            if (f < -20.0F && rand.nextInt(14) == 0) {
                generate(world, rand, chunkPos.add(rand.nextInt(size.getX()), 55 + rand.nextInt(16), rand.nextInt(size.getZ())));
                if (rand.nextInt(4) == 0) {
                    generate((World) worldIn, rand, chunkPos.add(rand.nextInt(size.getX()), 55 + rand.nextInt(16), rand.nextInt(size.getZ())));
                }
            }
        }
    }

    /**
     * @author Deamon
     * @reason Make it use the initial radius, radius decrement, and
     *     block type fields
     */
    @Override
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        // int radius = rand.nextInt(3) + 4;
        double radius = this.initial.getFlooredAmount(rand);

        for (int y = 0; radius > 0.5F; --y) {
            for (int x = MathHelper.floor(-radius); x <= MathHelper.ceil(radius); ++x) {
                for (int z = MathHelper.floor(-radius); z <= MathHelper.ceil(radius); ++z) {
                    if (x * x + z * z <= (radius + 1.0F) * (radius + 1.0F)) {
                        // this.setBlockAndNotifyAdequately(worldIn,
                        // position.add(k, j, l),
                        // Blocks.end_stone.getDefaultState());
                        this.setBlockAndNotifyAdequately(worldIn, position.add(x, y, z), (IBlockState) this.state);
                    }
                }
            }

            radius = (float) (radius - this.decrement.getAmount(rand));
            // radius = (float)(radius - (rand.nextInt(2) + 0.5D));
        }

        return true;
    }

    private float func_185960_a(int p_185960_1_, int p_185960_2_, int p_185960_3_, int p_185960_4_) {
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
                long k = p_185960_1_ + i;
                long l = p_185960_2_ + j;

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
