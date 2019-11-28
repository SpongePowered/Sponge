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
import net.minecraft.world.gen.feature.FireFeature;
import org.spongepowered.api.world.gen.populator.NetherFire;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(FireFeature.class)
public abstract class WorldGenFireMixin extends WorldGeneratorMixin {

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason Overwritten to use our custom cluster size.
     */
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, final BlockPos pos) {
    	//BEGIN sponge
        final int n = ((NetherFire) this).getClustersPerChunk().getFlooredAmount(rand);
        //END sponge
        for (int i = 0; i < n; ++i) {
            final BlockPos blockpos1 =
                    pos.func_177982_a(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            if (worldIn.func_175623_d(blockpos1) && worldIn.func_180495_p(blockpos1.func_177977_b()).func_177230_c() == Blocks.field_150424_aL) {
                worldIn.func_180501_a(blockpos1, Blocks.field_150480_ab.func_176223_P(), 2);
            }
        }

        return true;
    }

    @Override
    public String toString() {
    	return MoreObjects.toStringHelper(this)
    			.add("Type", "NetherFire")
    			.add("PerChunk", ((NetherFire) this).getClustersPerChunk())
    			.add("PerCluster", ((NetherFire) this).getFirePerCluster())
    			.toString();
    }

}
