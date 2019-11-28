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
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenIcePath;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.IcePath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenIcePath.class)
public abstract class WorldGenIcePathMixin extends WorldGeneratorMixin {

    @Shadow @Final private Block block;

    @Inject(method = "<init>(I)V", at = @At("RETURN") )
    private void impl$setRadiusOnCtor(final int radius, final CallbackInfo ci) {
        ((IcePath) this).setRadius(VariableAmount.baseWithRandomAddition(2, radius > 2 ? radius - 2 : 1));
    }

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason Overwritten to replace the path radius with one dependent on
     * our variable amount.
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
        // Sponge start
        final int i = ((IcePath) this).getRadius().getFlooredAmount(rand);
        // Sponge end
        final byte b0 = 1;

        for (int j = position.func_177958_n() - i; j <= position.func_177958_n() + i; ++j) {
            for (int k = position.func_177952_p() - i; k <= position.func_177952_p() + i; ++k) {
                final int l = j - position.func_177958_n();
                final int i1 = k - position.func_177952_p();

                if (l * l + i1 * i1 <= i * i) {
                    for (int j1 = position.func_177956_o() - b0; j1 <= position.func_177956_o() + b0; ++j1) {
                        final BlockPos blockpos1 = new BlockPos(j, j1, k);
                        final Block block = worldIn.func_180495_p(blockpos1).func_177230_c();

                        if (block == Blocks.field_150346_d || block == Blocks.field_150433_aE || block == Blocks.field_150432_aD) {
                            worldIn.func_180501_a(blockpos1, this.block.func_176223_P(), 2);
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
                .add("Type", "IcePath")
                .add("PerChunk", ((IcePath) this).getSectionsPerChunk())
                .add("Radius", ((IcePath) this).getRadius())
                .toString();
    }

}
