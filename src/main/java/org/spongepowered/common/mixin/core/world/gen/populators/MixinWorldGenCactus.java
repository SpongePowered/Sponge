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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenCactus;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenCactus.class)
public abstract class MixinWorldGenCactus implements Cactus {

    private VariableAmount cactiPerChunk;
    private VariableAmount height;

    @Inject(method = "<init>()V", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.cactiPerChunk = VariableAmount.fixed(10);
        this.height = VariableAmount.baseWithRandomAddition(1, VariableAmount.baseWithRandomAddition(1, 3));
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.CACTUS;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x, z;
        int n = this.cactiPerChunk.getFlooredAmount(random);

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            generate(world, random, world.getTopSolidOrLiquidBlock(chunkPos.add(x, 0, z)));
        }
    }

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason Overwritten to be less random. This method was completely rewritten.
     */
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
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
        if (worldIn.isAirBlock(position)) {
            int height = this.height.getFlooredAmount(rand);
            for (int k = 0; k < height; ++k) {
                if (Blocks.CACTUS.canBlockStay(worldIn, position)) {
                    worldIn.setBlockState(position.up(k), Blocks.CACTUS.getDefaultState(), 2);
                } else {
                    break;
                }
            }
        }

        return true;
    }

    @Override
    public VariableAmount getCactiPerChunk() {
        return this.cactiPerChunk;
    }

    @Override
    public void setCactiPerChunk(VariableAmount count) {
        this.cactiPerChunk = count;
    }

    @Override
    public VariableAmount getHeight() {
        return this.height;
    }

    @Override
    public void setHeight(VariableAmount height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Cactus")
                .add("PerChunk", this.cactiPerChunk)
                .add("Height", this.height)
                .toString();
    }

}
