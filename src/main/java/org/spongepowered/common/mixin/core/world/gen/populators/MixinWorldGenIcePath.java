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
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenIcePath;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
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
public abstract class MixinWorldGenIcePath implements IcePath {

    private VariableAmount radius;
    private VariableAmount sections;

    @Shadow @Final private Block block;

    @Inject(method = "<init>(I)V", at = @At("RETURN") )
    public void onConstructed(int radius, CallbackInfo ci) {
        this.radius = VariableAmount.baseWithRandomAddition(2, radius > 2 ? radius - 2 : 1);
        this.sections = VariableAmount.fixed(2);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.ICE_PATH;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        int x;
        int z;
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int n = this.sections.getFlooredAmount(random);
        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            generate(world, random, world.getHeight(chunkPos.add(x, 0, z)));
        }
    }

    /**
     * @author Deamon - December 12th, 2015
     * 
     * @reason Overwritten to replace the path radius with one dependent on
     * our variable amount.
     */
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        while (worldIn.isAirBlock(position) && position.getY() > 2) {
            position = position.down();
        }

        if (worldIn.getBlockState(position).getBlock() != Blocks.SNOW) {
            return false;
        }
        // Sponge start
        int i = this.radius.getFlooredAmount(rand);
        // Sponge end
        byte b0 = 1;

        for (int j = position.getX() - i; j <= position.getX() + i; ++j) {
            for (int k = position.getZ() - i; k <= position.getZ() + i; ++k) {
                int l = j - position.getX();
                int i1 = k - position.getZ();

                if (l * l + i1 * i1 <= i * i) {
                    for (int j1 = position.getY() - b0; j1 <= position.getY() + b0; ++j1) {
                        BlockPos blockpos1 = new BlockPos(j, j1, k);
                        Block block = worldIn.getBlockState(blockpos1).getBlock();

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
    public VariableAmount getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(VariableAmount radius) {
        this.radius = radius;
    }

    @Override
    public VariableAmount getSectionsPerChunk() {
        return this.sections;
    }

    @Override
    public void setSectionsPerChunk(VariableAmount sections) {
        this.sections = sections;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "IcePath")
                .add("PerChunk", this.sections)
                .add("Radius", this.radius)
                .toString();
    }

}
