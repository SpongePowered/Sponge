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
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.populator.EnderCrystalPlatform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenSpikes.class)
public class MixinWorldGenSpikes implements EnderCrystalPlatform {

    @Shadow private Block baseBlockRequired;

    private double probability;
    private VariableAmount height;
    private VariableAmount radius;

    @Inject(method = "<init>(Lnet/minecraft/block/Block;)V", at = @At("RETURN") )
    public void onConstructed(Block block, CallbackInfo ci) {
        this.probability = 0.2;
        this.radius = VariableAmount.baseWithRandomAddition(1, 4);
        this.height = VariableAmount.baseWithRandomAddition(6, 32);
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        Vector3i min = chunk.getBlockMin();
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x, z;
        if (random.nextDouble() < this.probability) {
            x = random.nextInt(16) + 8;
            z = random.nextInt(16) + 8;
            generate(world, random, world.getTopSolidOrLiquidBlock(chunkPos.add(x, 0, z)));
        }
    }

    /*
     * Author: Deamon - December 12th, 2015 TODO I believe this can be done
     * purely with injections as no code needs to be removed. Or possible
     * redirects
     */
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (worldIn.isAirBlock(position) && worldIn.getBlockState(position.down()).getBlock() == this.baseBlockRequired) {
            // BEGIN sponge
//            int yHeight = rand.nextInt(32) + 6;
            int yHeight = this.height.getFlooredAmount(rand);
//            int rad = rand.nextInt(4) + 1;
            int rad = this.radius.getFlooredAmount(rand);
            // END sponge
            int k;
            int l;
            int i1;
            int j1;

            for (k = position.getX() - rad; k <= position.getX() + rad; ++k) {
                for (l = position.getZ() - rad; l <= position.getZ() + rad; ++l) {
                    i1 = k - position.getX();
                    j1 = l - position.getZ();

                    if (i1 * i1 + j1 * j1 <= rad * rad + 1
                            && worldIn.getBlockState(new BlockPos(k, position.getY() - 1, l)).getBlock() != this.baseBlockRequired) {
                        return false;
                    }
                }
            }

            for (k = position.getY(); k < position.getY() + yHeight && k < 256; ++k) {
                for (l = position.getX() - rad; l <= position.getX() + rad; ++l) {
                    for (i1 = position.getZ() - rad; i1 <= position.getZ() + rad; ++i1) {
                        j1 = l - position.getX();
                        int k1 = i1 - position.getZ();

                        if (j1 * j1 + k1 * k1 <= rad * rad + 1) {
                            worldIn.setBlockState(new BlockPos(l, k, i1), Blocks.obsidian.getDefaultState(), 2);
                        }
                    }
                }
            }

            EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(worldIn);
            // BEGIN sponge
            // add one to the height to fix MC bug if the EnderCrystal removing
            // the bedrock block
            entityendercrystal.setLocationAndAngles((double) ((float) position.getX() + 0.5F), (double) (position.getY() + yHeight + 1),
                    (double) ((float) position.getZ() + 0.5F), rand.nextFloat() * 360.0F, 0.0F);
//            entityendercrystal.setLocationAndAngles((double) ((float) position.getX() + 0.5F), (double) (position.getY() + yHeight),
//                    (double) ((float) position.getZ() + 0.5F), rand.nextFloat() * 360.0F, 0.0F);
            // END sponge
            worldIn.spawnEntityInWorld(entityendercrystal);
            worldIn.setBlockState(position.up(yHeight), Blocks.bedrock.getDefaultState(), 2);
            return true;
        }
        return false;
    }

    @Override
    public double getSpawnProbability() {
        return this.probability;
    }

    @Override
    public void setSpawnProbability(double p) {
        this.probability = p;
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
    public VariableAmount getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(VariableAmount radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("Type", "EnderCrystalPlatform")
                .add("Chance", this.probability)
                .add("Height", this.height)
                .add("Radius", this.radius)
                .toString();
    }

}
