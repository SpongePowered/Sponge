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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.WaterlilyFeature;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.WaterLily;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(WaterlilyFeature.class)
public abstract class WorldGenWaterLilyMixin_API extends Feature implements WaterLily {

    private VariableAmount api$count = VariableAmount.fixed(4);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.WATER_LILY;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        int n = this.api$count.getFlooredAmount(random);
        final BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x, z;

        // The generate method makes 10 attempts, so divide the count by 10
        n /= 10;
        if (n < 0) {
            n++;
        }

        for (int i = 0; i < n; i++) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            BlockPos pos = position.func_177982_a(x, 0, z);
            pos = world.func_175672_r(pos);
            func_180709_b(world, random, pos);
        }
    }

    @Override
    public VariableAmount getWaterLilyPerChunk() {
        return this.api$count;
    }

    @Override
    public void setWaterLilyPerChunk(final VariableAmount count) {
        this.api$count = count;
    }

}
