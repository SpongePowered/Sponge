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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.MelonFeature;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Melon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.core.world.gen.feature.WorldGeneratorMixin;

import java.util.Random;

@Mixin(MelonFeature.class)
public abstract class WorldGenMelonMixin_API extends WorldGeneratorMixin implements Melon {

    private VariableAmount api$count = VariableAmount.fixed(10);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.MELON;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        final int n = this.api$count.getFlooredAmount(random);
        final int x = random.nextInt(size.getX());
        final int z = random.nextInt(size.getZ());
        final int y = apiImpl$nextInt(random, Math.min(world.func_175645_m(position.func_177982_a(x, 0, z)).func_177956_o() * 2, 255));
        position = position.func_177982_a(x, y, z);
        for (int i = 0; i < n; ++i) {
            final BlockPos blockpos1 = position.func_177982_a(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));

            if (Blocks.field_150440_ba.func_176196_c(world, blockpos1) && world.func_180495_p(blockpos1.func_177977_b()).func_177230_c() == Blocks.field_150349_c) {
                world.func_180501_a(blockpos1, Blocks.field_150440_ba.func_176223_P(), 2);
            }
        }
    }

    private int apiImpl$nextInt(final Random rand, final int i) {
        if (i <= 1)
            return 0;
        return rand.nextInt(i);
    }

    @Override
    public VariableAmount getMelonsPerChunk() {
        return this.api$count;
    }

    @Override
    public void setMelonsPerChunk(final VariableAmount count) {
        this.api$count = count;
    }


}
