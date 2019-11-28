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
import net.minecraft.world.gen.feature.DeadBushFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.DeadBush;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(DeadBushFeature.class)
public abstract class WorldGenDeadBushMixin_API extends Feature implements DeadBush {

    private VariableAmount api$count = VariableAmount.fixed(128);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.DEAD_BUSH;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        int n = (int) Math.ceil(this.api$count.getFlooredAmount(random) / 16f);
        for (int i = 0; i < n; i++) {
            BlockPos pos = position.add(random.nextInt(size.getX()), 0, random.nextInt(size.getZ()));
            pos = world.getTopSolidOrLiquidBlock(pos).add(0, 1, 0);
            generate(world, random, pos);
        }
    }
    
    @Override
    public VariableAmount getShrubsPerChunk() {
        return this.api$count;
    }

    @Override
    public void setShrubsPerChunk(VariableAmount count) {
        this.api$count = count;
    }

}
