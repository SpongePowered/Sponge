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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenFire;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.populator.NetherFire;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenFire.class)
public class MixinWorldGenFire implements NetherFire {

    private VariableAmount count;
    private VariableAmount cluster;

    @Inject(method = "<init>()V", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.count = VariableAmount.fixed(10);
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        int n = this.count.getFlooredAmount(random);
        for (int i = 0; i < n; i++) {
            int x = chunk.getBlockMin().getX() + 8 + random.nextInt(16);
            int z = chunk.getBlockMin().getZ() + 8 + random.nextInt(16);
            int y = chunk.getBlockMin().getY() + 4 + random.nextInt(120);
            generate(world, random, new BlockPos(x, y, z));
        }
    }

    /*
     * Author: Deamon - December 12th, 2015
     * 
     * Purpose: Overwritten to use our custom cluster size.
     */
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos pos) {
    	//BEGIN sponge
        int n = this.cluster.getFlooredAmount(rand);
        //END sponge
        for (int i = 0; i < n; ++i) {
            BlockPos blockpos1 =
                    pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            if (worldIn.isAirBlock(blockpos1) && worldIn.getBlockState(blockpos1.down()).getBlock() == Blocks.netherrack) {
                worldIn.setBlockState(blockpos1, Blocks.fire.getDefaultState(), 2);
            }
        }

        return true;
    }

    @Override
    public VariableAmount getFirePerCluster() {
        return this.cluster;
    }

    @Override
    public void setFirePerCluster(VariableAmount count) {
        this.cluster = checkNotNull(count);
    }

    @Override
    public VariableAmount getClustersPerChunk() {
        return this.count;
    }

    @Override
    public void setClustersPerChunk(VariableAmount count) {
        this.count = checkNotNull(count);
    }
    
    @Override
    public String toString() {
    	return Objects.toStringHelper(this)
    			.add("Type", "NetherFire")
    			.add("PerChunk", this.count)
    			.add("PerCluster", this.cluster)
    			.toString();
    }

}
