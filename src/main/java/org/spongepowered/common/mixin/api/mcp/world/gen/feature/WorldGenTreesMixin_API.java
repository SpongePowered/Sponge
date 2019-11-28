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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenTrees;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.WorldGenTreesBridge;
import org.spongepowered.common.bridge.world.gen.feature.WorldGeneratorBridge;

import java.util.Random;

@Mixin(WorldGenTrees.class)
public abstract class WorldGenTreesMixin_API extends WorldGenAbstractTree implements PopulatorObject {

    @Shadow @Final @Mutable private int minTreeHeight;

    public WorldGenTreesMixin_API(final boolean notify) {
        super(notify);
    }

    @Override
    public String getId() {
        return ((WorldGenTreesBridge) this).bridge$getId();
    }

    @Override
    public String getName() {
        return ((WorldGenTreesBridge) this).bridge$getName();
    }

    @Override
    public boolean canPlaceAt(final World world, final int x, final int y, final int z) {
        final net.minecraft.world.World worldIn = (net.minecraft.world.World) world;
        final int i = 3;
        boolean flag = true;

        if (y < 1 || y + i + 1 > 256) {
            return false;
        }
        byte b0;
        int l;

        for (int j = y; j <= y + 1 + i; ++j) {
            b0 = 1;

            if (j == y) {
                b0 = 0;
            }

            if (j >= y + 1 + i - 2) {
                b0 = 2;
            }

            for (int k = x - b0; k <= x + b0 && flag; ++k) {
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
        final BlockPos down = new BlockPos(x, y - 1, z);
        final Block block = worldIn.func_180495_p(down).func_177230_c();
        if (!((WorldGeneratorBridge) this).bridge$canSustainPlant(block, worldIn, down, EnumFacing.UP, Blocks.field_150345_g) || y >= 256 - i - 1) {
            return false;
        }
        return true;
    }

    @Override
    public void placeObject(final World world, final Random random, final int x, final int y, final int z) {
        this.minTreeHeight = ((WorldGenTreesBridge) this).bridge$getMinimumHeight().getFlooredAmount(random);
        final BlockPos pos = new BlockPos(x, y, z);
        func_175904_e();
        if (func_180709_b((net.minecraft.world.World) world, random, pos)) {
            func_180711_a((net.minecraft.world.World) world, random, pos);
        }
    }


}
