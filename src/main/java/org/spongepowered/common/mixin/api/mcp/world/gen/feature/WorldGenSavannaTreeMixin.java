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
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.SavannaTreeFeature;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.gen.feature.WorldGeneratorBridge;

import java.util.Random;

@Mixin(SavannaTreeFeature.class)
public abstract class WorldGenSavannaTreeMixin extends AbstractTreeFeature implements PopulatorObject {

    public WorldGenSavannaTreeMixin(final boolean notify) {
        super(notify);
    }

    @Override
    public String getId() {
        return "minecraft:savanna";
    }

    @Override
    public String getName() {
        return "Savanna tree";
    }

    @Override
    public boolean canPlaceAt(final World world, final int x, final int y, final int z) {
        final net.minecraft.world.World worldIn = (net.minecraft.world.World) world;
        final int i = 5;
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
                            if (!this.impl$isReplaceable(worldIn, new BlockPos(k, j, l))) {
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
            if (((WorldGeneratorBridge) this).bridge$canSustainPlant(block, worldIn, down, Direction.UP, Blocks.SAPLING) && y < 256 - i - 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void placeObject(final World world, final Random random, final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        setDecorationDefaults();
        if (generate((net.minecraft.world.World) world, random, pos)) {
            generateSaplings((net.minecraft.world.World) world, random, pos);
        }
    }
    private boolean impl$isReplaceable(final net.minecraft.world.World world, final BlockPos pos) {
        final net.minecraft.block.BlockState state = world.getBlockState(pos);
        return ((WorldGeneratorBridge) this).bridge$isAir(state, world, pos)
               || ((WorldGeneratorBridge) this).bridge$isLeaves(state, world, pos)
               || ((WorldGeneratorBridge) this).birdge$isWood(state, world, pos)
               || canGrowInto(state.getBlock());
    }

}
