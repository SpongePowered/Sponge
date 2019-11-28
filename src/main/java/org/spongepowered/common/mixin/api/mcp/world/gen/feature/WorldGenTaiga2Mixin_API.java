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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.TallTaigaTreeFeature;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.gen.feature.WorldGeneratorBridge;

import java.util.Random;

//tall_taiga
@Mixin(TallTaigaTreeFeature.class)
public abstract class WorldGenTaiga2Mixin_API extends AbstractTreeFeature implements PopulatorObject {

    public WorldGenTaiga2Mixin_API(final boolean notify) {
        super(notify);
    }

    @Override
    public String getId() {
        return "minecraft:tall_taiga";
    }

    @Override
    public String getName() {
        return "Tall taiga tree";
    }

    @Override
    public boolean canPlaceAt(final World world, final int x, final int y, final int z) {
        final net.minecraft.world.World worldIn = (net.minecraft.world.World) world;
        final int i = 6;
        final int j = 1;
        final int l = 2;
        boolean flag = true;
        if (y >= 1 && y + i + 1 <= 256) {
            int j1;
            int i3;
            for (int i1 = y; i1 <= y + 1 + i && flag; ++i1) {
                if (i1 - y < j) {
                    i3 = 0;
                } else {
                    i3 = l;
                }
                for (j1 = x - i3; j1 <= x + i3 && flag; ++j1) {
                    for (int k1 = z - i3; k1 <= z + i3 && flag; ++k1) {
                        if (i1 >= 0 && i1 < 256) {
                            final BlockPos pos = new BlockPos(j1, i1, k1);
                            final BlockState state = worldIn.getBlockState(pos);
                            if (!((WorldGeneratorBridge) this).bridge$isAir(state, worldIn, pos) && !((WorldGeneratorBridge) this).bridge$isLeaves(state, worldIn, pos)) {
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
            final Block block = worldIn.getBlockState(down).getBlock();
            if (((WorldGeneratorBridge) this).bridge$canSustainPlant(block, worldIn, down, Direction.UP, Blocks.field_150345_g) && y < 256 - i - 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void placeObject(final World world, final Random random, final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        func_175904_e();
        if (func_180709_b((net.minecraft.world.World) world, random, pos)) {
            generateSaplings((net.minecraft.world.World) world, random, pos);
        }
    }

}
