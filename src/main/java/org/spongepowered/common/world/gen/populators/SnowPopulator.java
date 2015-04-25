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
package org.spongepowered.common.world.gen.populators;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.Populator;

import java.util.Random;

public class SnowPopulator implements Populator {

    @Override
    public void populate(Chunk chunk, Random random) {
        World worldObj = (World) chunk.getWorld();
        BlockPos blockpos = new BlockPos(chunk.getBlockMin().getX() + 8, chunk.getBlockMin().getY(), chunk.getBlockMin().getZ() + 8);
        for (int k1 = 0; k1 < 16; ++k1) {
            for (int l1 = 0; l1 < 16; ++l1) {
                BlockPos blockpos1 = worldObj.getPrecipitationHeight(blockpos.add(k1, 0, l1));
                BlockPos blockpos2 = blockpos1.down();

                if (worldObj.canBlockFreezeWater(blockpos2)) {
                    worldObj.setBlockState(blockpos2, Blocks.ice.getDefaultState(), 2);
                }

                if (worldObj.canSnowAt(blockpos1, true)) {
                    worldObj.setBlockState(blockpos1, Blocks.snow_layer.getDefaultState(), 2);
                }
            }
        }
    }

}
