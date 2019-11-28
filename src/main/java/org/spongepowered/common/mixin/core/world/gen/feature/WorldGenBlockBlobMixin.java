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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.BlockBlobFeature;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(BlockBlobFeature.class)
public abstract class WorldGenBlockBlobMixin extends WorldGeneratorMixin {

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason This overwrite is to replace the usages of Block with BlockState as well
     * as to modify the radii calculations to use our VariableAmount.
     */
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, BlockPos position) {
        // TODO cleanup this decompiler spaghetti
        while (true) {
            if (position.getY() > 3) {
                label47: {
                    if (!worldIn.isAirBlock(position.down())) {
                        final Block block = worldIn.getBlockState(position.down()).getBlock();

                        if (block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.STONE) {
                            break label47;
                        }
                    }

                    position = position.down();
                    continue;
                }
            }

            if (position.getY() <= 3) {
                return false;
            }

            // SPONGE start
//            int i1 = this.field_150544_b;

//            for (int i = 0; i1 >= 0 && i < 3; ++i)
            for (int i = 0; i < 3; ++i) {
//                int j = i1 + rand.nextInt(2);
//                int k = i1 + rand.nextInt(2);
//                int l = i1 + rand.nextInt(2);
                final int j = impl$getRadiusInstance(rand);
                final int k = impl$getRadiusInstance(rand);
                final int l = impl$getRadiusInstance(rand);
                final float f = (j + k + l) * 0.333F + 0.5F;

                for (final BlockPos blockpos1 : BlockPos.getAllInBox(position.add(-j, -k, -l), position.add(j, k, l))) {
                    if (blockpos1.distanceSq(position) <= f * f) {
//                        worldIn.setBlockState(blockpos1, this.field_150545_a.getDefaultState(), 4);
                        worldIn.setBlockState(blockpos1, (BlockState) ((BlockBlob) this).getBlock(), 4);
                    }
                }

//                position = position.add(-(i1 + 1) + rand.nextInt(2 + i1 * 2), 0 - rand.nextInt(2), -(i1 + 1) + rand.nextInt(2 + i1 * 2));
                position = position.add(-(j + 1) + rand.nextInt(2 + j * 2), 0 - rand.nextInt(2), -(l + 1) + rand.nextInt(2 + l * 2));
                // SPONGE end
            }

            return true;
        }
    }

    private int impl$getRadiusInstance(final Random rand) {
        final int i = ((BlockBlob) this).getRadius().getFlooredAmount(rand);
        return i < 0 ? 0 : i;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "BlockBlob")
                .add("Block", ((BlockBlob) this).getBlock())
                .add("Radius", ((BlockBlob) this).getRadius())
                .add("Count", ((BlockBlob) this).getCount())
                .toString();
    }

}
