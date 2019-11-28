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
package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(WorldGenCanopyTree.class)
public abstract class WorldGenCanopyTreeMixin extends WorldGenAbstractTree implements PopulatorObject {

    public WorldGenCanopyTreeMixin(boolean notify) { // Shadow mixin requirement
        super(notify);
    }

    @Override
    public String getId() {
        return "minecraft:canopy";
    }

    @Override
    public String getName() {
        return "Canopy tree";
    }

    @Override
    public boolean canPlaceAt(World world, int x, int y, int z) {
        net.minecraft.world.World worldIn = (net.minecraft.world.World) world;
        int i = 6;
        boolean flag = true;

        if (y >= 1 && y + i + 1 <= 256) {
            int k;
            int l;

            for (int j = y; j <= y + 1 + i; ++j) {
                byte b0 = 1;

                if (j == y) {
                    b0 = 0;
                }

                if (j >= y + 1 + i - 2) {
                    b0 = 2;
                }

                for (k = x - b0; k <= x + b0 && flag; ++k) {
                    for (l = z - b0; l <= z + b0 && flag; ++l) {
                        if (j >= 0 && j < 256) {
                            if (!this.func_150523_a(worldIn.func_180495_p(new BlockPos(k, j, l)).func_177230_c())) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }

            if (!flag) {
                return false;
            }
            Block block = worldIn.func_180495_p(new BlockPos(x, y - 1, z)).func_177230_c();

            if ((block == Blocks.field_150349_c || block == Blocks.field_150346_d) && y < 256 - i - 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void placeObject(World world, Random random, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        func_175904_e();
        if (func_180709_b((net.minecraft.world.World) world, random, pos)) {
            func_180711_a((net.minecraft.world.World) world, random, pos);
        }
    }

}
