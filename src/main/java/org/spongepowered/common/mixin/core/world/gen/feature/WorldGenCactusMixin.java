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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.CactusFeature;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(CactusFeature.class)
public abstract class WorldGenCactusMixin extends WorldGeneratorMixin {

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason Overwritten to be less random. This method was completely rewritten.
     */
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, final BlockPos position) {
//        for (int i = 0; i < 10; ++i)
//        {
//            BlockPos blockpos1 = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
//
//            if (worldIn.isAirBlock(blockpos1))
//            {
//                int j = 1 + rand.nextInt(rand.nextInt(3) + 1);
//
//                for (int k = 0; k < j; ++k)
//                {
//                    if (Blocks.cactus.canBlockStay(worldIn, blockpos1))
//                    {
//                        worldIn.setBlockState(blockpos1.up(k), Blocks.cactus.getDefaultState(), 2);
//                    }
//                }
//            }
//        }
        if (worldIn.func_175623_d(position)) {
            final int height = ((Cactus) this).getHeight().getFlooredAmount(rand);
            for (int k = 0; k < height; ++k) {
                if (Blocks.field_150434_aF.func_176586_d(worldIn, position)) {
                    worldIn.func_180501_a(position.func_177981_b(k), Blocks.field_150434_aF.func_176223_P(), 2);
                } else {
                    break;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Cactus")
                .add("PerChunk", ((Cactus) this).getCactiPerChunk())
                .add("Height", ((Cactus) this).getHeight())
                .toString();
    }

}
