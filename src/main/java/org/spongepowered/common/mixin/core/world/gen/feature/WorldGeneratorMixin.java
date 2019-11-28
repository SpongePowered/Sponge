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
package org.spongepowered.common.mixin.core.world.gen.feature;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.feature.WorldGeneratorBridge;

import java.util.Random;

@Mixin(Feature.class)
public abstract class WorldGeneratorMixin implements WorldGeneratorBridge {

    @Shadow public abstract void setDecorationDefaults();
    @Shadow public abstract boolean generate(World var1, Random var2, BlockPos var3);
    @Shadow protected abstract void setBlockAndNotifyAdequately(World worldIn, BlockPos pos, BlockState state);

    //These are overridden in forge to call the forge added Block.isAir/isLeaves
    @Override
    public boolean bridge$isAir(final BlockState state, final World worldIn, final BlockPos pos) {
        return state.func_185904_a() == Material.field_151579_a;
    }

    @Override
    public boolean bridge$isLeaves(final BlockState state, final World worldIn, final BlockPos pos) {
        return state.func_185904_a() == Material.field_151584_j;
    }

    @Override
    public boolean birdge$isWood(final BlockState state, final World worldIn, final BlockPos pos) {
        return state.func_185904_a() == Material.field_151575_d;
    }
    
    @Override
    public boolean bridge$canSustainPlant(final Block block, final World worldIn, final BlockPos pos, final Direction direction, final Block plant) {
        return block == Blocks.field_150349_c || block == Blocks.field_150346_d || block == Blocks.field_150458_ak;
    }
    
}
