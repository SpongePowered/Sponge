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
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ReedFeature;
import org.spongepowered.api.world.gen.populator.Reed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(ReedFeature.class)
public abstract class WorldGenReedMixin extends WorldGeneratorMixin {

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason This is overwritten to use our custom attempt counts and reed
     * heights.
     */
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, final BlockPos position) {
        // Sponge start
        final int n = ((Reed) this).getReedsPerChunk().getFlooredAmount(rand);
        // Sponge end
        for (int i = 0; i < n; ++i) {
            final BlockPos blockpos1 = position.func_177982_a(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4));

            if (worldIn.func_175623_d(blockpos1)) {
                final BlockPos blockpos2 = blockpos1.func_177977_b();
                
                if (worldIn.func_180495_p(blockpos2.func_177976_e()).func_185904_a() == Material.field_151586_h
                        || worldIn.func_180495_p(blockpos2.func_177974_f()).func_185904_a() == Material.field_151586_h
                        || worldIn.func_180495_p(blockpos2.func_177978_c()).func_185904_a() == Material.field_151586_h
                        || worldIn.func_180495_p(blockpos2.func_177968_d()).func_185904_a() == Material.field_151586_h) {
                    // Sponge start
                    final int height = ((Reed) this).getReedHeight().getFlooredAmount(rand);
                    // Sponge end
                    for (int y = 0; y < height; ++y) {
                        if (Blocks.field_150436_aH.func_176354_d(worldIn, blockpos1)) {
                            worldIn.func_180501_a(blockpos1.func_177981_b(y), Blocks.field_150436_aH.func_176223_P(), 2);
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Reed")
                .add("PerChunk", ((Reed) this).getReedsPerChunk())
                .add("Height", ((Reed) this).getReedHeight())
                .toString();
    }

}
