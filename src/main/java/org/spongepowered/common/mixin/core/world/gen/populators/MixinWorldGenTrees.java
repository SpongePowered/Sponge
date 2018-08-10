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

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenTrees;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.world.gen.IWorldGenTrees;

import java.util.Random;

@Mixin(WorldGenTrees.class)
public abstract class MixinWorldGenTrees extends MixinWorldGenAbstractTree implements PopulatorObject, IWorldGenTrees {

    @Shadow private int minTreeHeight;

    private String id = "minecraft:oak";
    private String name = "Oak tree";
    private VariableAmount minHeight = VariableAmount.fixed(4);
    private CatalogKey key = CatalogKey.minecraft("oak");

    @Shadow
    public abstract boolean generate(net.minecraft.world.World worldIn, Random rand, BlockPos position);

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public void setId(String id) {
        this.key = CatalogKey.resolve(id);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean canPlaceAt(World world, int x, int y, int z) {
        net.minecraft.world.World worldIn = (net.minecraft.world.World) world;
        int i = 3;
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
                        if (!this.canGrowInto(worldIn.getBlockState(new BlockPos(k, j, l)).getBlock())) {
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
        BlockPos down = new BlockPos(x, y - 1, z);
        Block block = worldIn.getBlockState(down).getBlock();
        if (!canSustainPlant(block, worldIn, down, EnumFacing.UP, Blocks.SAPLING) || y >= 256 - i - 1) {
            return false;
        }
        return true;
    }

    @Override
    public void placeObject(World world, Random random, int x, int y, int z) {
        this.minTreeHeight = this.minHeight.getFlooredAmount(random);
        BlockPos pos = new BlockPos(x, y, z);
        setDecorationDefaults();
        if (generate((net.minecraft.world.World) world, random, pos)) {
            generateSaplings((net.minecraft.world.World) world, random, pos);
        }
    }

    @Override
    public void setMinHeight(VariableAmount height) {
        this.minHeight = height;
    }

}
