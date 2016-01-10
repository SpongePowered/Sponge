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
import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Random;

@Mixin(WorldGenBlockBlob.class)
public abstract class MixinWorldGenBlockBlob implements BlockBlob {

    private BlockState block;
    private VariableAmount radius;
    private VariableAmount count;

    @Inject(method = "<init>(Lnet/minecraft/block/Block;I)V", at = @At("RETURN") , require = 1)
    public void onConstructed(Block block, int radius, CallbackInfo ci) {
        this.block = (BlockState) block.getDefaultState();
        this.radius = VariableAmount.fixed(radius);
        this.count = VariableAmount.baseWithRandomAddition(0, 3);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.BLOCK_BLOB;
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        Vector3i min = chunk.getBlockMin();
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x, z;
        int n = this.count.getFlooredAmount(random);

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(16) + 8;
            z = random.nextInt(16) + 8;
            generate(world, random, world.getHeight(chunkPos.add(x, 0, z)));
        }
    }

    /*
     * Author: Deamon - December 12th, 2015
     *
     * Purpose: This overwrite is to replace the usages of Block with BlockState as well
     * as to modify the radii calculations to use our VariableAmount.
     */
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        // TODO cleanup this decompiler spaghetti
        while (true) {
            if (position.getY() > 3) {
                label47: {
                    if (!worldIn.isAirBlock(position.down())) {
                        Block block = worldIn.getBlockState(position.down()).getBlock();

                        if (block == Blocks.grass || block == Blocks.dirt || block == Blocks.stone) {
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
                int j = getRadiusInstance(rand);
                int k = getRadiusInstance(rand);
                int l = getRadiusInstance(rand);
                float f = (j + k + l) * 0.333F + 0.5F;
                Iterator iterator = BlockPos.getAllInBox(position.add(-j, -k, -l), position.add(j, k, l)).iterator();

                while (iterator.hasNext()) {
                    BlockPos blockpos1 = (BlockPos) iterator.next();

                    if (blockpos1.distanceSq(position) <= f * f) {
//                        worldIn.setBlockState(blockpos1, this.field_150545_a.getDefaultState(), 4);
                        worldIn.setBlockState(blockpos1, (IBlockState) this.block, 4);
                    }
                }

//                position = position.add(-(i1 + 1) + rand.nextInt(2 + i1 * 2), 0 - rand.nextInt(2), -(i1 + 1) + rand.nextInt(2 + i1 * 2));
                position = position.add(-(j + 1) + rand.nextInt(2 + j * 2), 0 - rand.nextInt(2), -(l + 1) + rand.nextInt(2 + l * 2));
                // SPONGE end
            }

            return true;
        }
    }

    private int getRadiusInstance(Random rand) {
        int i = this.radius.getFlooredAmount(rand);
        return i < 0 ? 0 : i;
    }

    @Override
    public BlockState getBlock() {
        return this.block;
    }

    @Override
    public void setBlock(BlockState state) {
        this.block = state;
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
    public VariableAmount getCount() {
        return this.count;
    }

    @Override
    public void setCount(VariableAmount count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("Type", "BlockBlob")
                .add("Block", this.block)
                .add("Radius", this.radius)
                .add("Count", this.count)
                .toString();
    }

}
