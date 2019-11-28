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
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.IcePathFeature;
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

@Mixin(IcePathFeature.class)
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
        while (worldIn.isAirBlock(position) && position.getY() > 2) {
            position = position.down();
        }

        if (worldIn.getBlockState(position).getBlock() != Blocks.SNOW) {
            return false;
        }
        // Sponge start
        final int i = ((IcePath) this).getRadius().getFlooredAmount(rand);
        // Sponge end
        final byte b0 = 1;

        for (int j = position.getX() - i; j <= position.getX() + i; ++j) {
            for (int k = position.getZ() - i; k <= position.getZ() + i; ++k) {
                final int l = j - position.getX();
                final int i1 = k - position.getZ();

                if (l * l + i1 * i1 <= i * i) {
                    for (int j1 = position.getY() - b0; j1 <= position.getY() + b0; ++j1) {
                        final BlockPos blockpos1 = new BlockPos(j, j1, k);
                        final Block block = worldIn.getBlockState(blockpos1).getBlock();

                        if (block == Blocks.DIRT || block == Blocks.SNOW || block == Blocks.ICE) {
                            worldIn.setBlockState(blockpos1, this.block.getDefaultState(), 2);
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
