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
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.PumpkinFeature;
import org.spongepowered.api.world.gen.populator.Pumpkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(PumpkinFeature.class)
public abstract class WorldGenPumpkinMixin extends WorldGeneratorMixin {

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason This is overwritten in order to use our custom patch size.
     */
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, final BlockPos p_180709_3_) {
        final int n = ((Pumpkin) this).getPumpkinsPerChunk().getFlooredAmount(rand);
        for (int i = 0; i < n; ++i) {
            final BlockPos blockpos1 =
                    p_180709_3_.func_177982_a(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.func_175623_d(blockpos1) && worldIn.func_180495_p(blockpos1.func_177977_b()).func_177230_c() == Blocks.field_150349_c
                    && Blocks.field_150423_aK.func_176196_c(worldIn, blockpos1)) {
                worldIn.func_180501_a(blockpos1,
                        Blocks.field_150423_aK.func_176223_P().func_177226_a(BlockHorizontal.field_185512_D, EnumFacing.Plane.HORIZONTAL.func_179518_a(rand)), 2);
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Pumpkin")
                .add("PerChunk", ((Pumpkin) this).getPumpkinsPerChunk())
                .add("Chance", ((Pumpkin) this).getPumpkinChance())
                .toString();
    }

}
