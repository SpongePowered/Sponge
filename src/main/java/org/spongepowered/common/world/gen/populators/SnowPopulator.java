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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.Random;

public class SnowPopulator implements Populator {

    @Override
    public PopulatorType getType() {
        return InternalPopulatorTypes.SNOW;
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World worldObj = (World) world;
        BlockPos blockpos = new BlockPos(min.getX(), min.getY(), min.getZ());
        for (int x = 0; x < size.getX(); ++x) {
            for (int y = 0; y < size.getZ(); ++y) {
                BlockPos blockpos1 = worldObj.func_175725_q(blockpos.add(x, 0, y));
                BlockPos blockpos2 = blockpos1.down();

                if (worldObj.func_175675_v(blockpos2)) {
                    worldObj.setBlockState(blockpos2, Blocks.ICE.getDefaultState(), 2);
                }

                if (worldObj.func_175708_f(blockpos1, true)) {
                    worldObj.setBlockState(blockpos1, Blocks.field_150431_aC.getDefaultState(), 2);
                }
            }
        }
    }

}
