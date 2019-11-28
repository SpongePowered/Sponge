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
import net.minecraft.world.gen.feature.PumpkinFeature;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Pumpkin;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(PumpkinFeature.class)
public abstract class WorldGenPumpkinMixin_API extends Feature implements Pumpkin {

    private VariableAmount api$count = VariableAmount.fixed(10);
    private double api$chance = 0.1;

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.PUMPKIN;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        if (random.nextDouble() < this.api$chance) {
            final int x = min.getX() + random.nextInt(size.getX());
            final int z = min.getZ() + random.nextInt(size.getZ());
            final int height = world.getHeight(new BlockPos(x, 0, z)).getY();
            final int y = min.getY() + height < 1 ? 0 : random.nextInt(height * 2);
            generate(world, random, new BlockPos(x, y, z));
        }
    }


    @Override
    public VariableAmount getPumpkinsPerChunk() {
        return this.api$count;
    }

    @Override
    public void setPumpkinsPerChunk(final VariableAmount count) {
        this.api$count = count;
    }

    @Override
    public double getPumpkinChance() {
        return this.api$chance;
    }

    @Override
    public void setPumpkinChance(final double p) {
        this.api$chance = p;
    }

}
