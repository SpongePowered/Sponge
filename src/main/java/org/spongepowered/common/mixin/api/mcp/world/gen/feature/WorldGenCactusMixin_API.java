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
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(WorldGenCactus.class)
public abstract class WorldGenCactusMixin_API extends WorldGenerator implements Cactus {

    private VariableAmount api$cactiPerChunk = VariableAmount.fixed(10);
    private VariableAmount api$height = VariableAmount.baseWithRandomAddition(1, VariableAmount.baseWithRandomAddition(1, 3));

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.CACTUS;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x;
        int z;
        final int n = this.api$cactiPerChunk.getFlooredAmount(random);

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            func_180709_b(world, random, world.func_175672_r(chunkPos.func_177982_a(x, 0, z)));
        }
    }

    @Override
    public VariableAmount getCactiPerChunk() {
        return this.api$cactiPerChunk;
    }

    @Override
    public void setCactiPerChunk(final VariableAmount count) {
        this.api$cactiPerChunk = count;
    }

    @Override
    public VariableAmount getHeight() {
        return this.api$height;
    }

    @Override
    public void setHeight(final VariableAmount height) {
        this.api$height = height;
    }

}
