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
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.BlockBlobFeature;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

import javax.annotation.Nullable;

@Mixin(BlockBlobFeature.class)
public abstract class WorldGenBlockBlobMixin_API extends Feature implements BlockBlob {

    @Shadow @Final private Block block;
    @Shadow @Final private int startRadius;

    @Nullable private BlockState api$blockState;
    @Nullable private VariableAmount api$radius;
    private VariableAmount api$count = VariableAmount.baseWithRandomAddition(0, 3);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.BLOCK_BLOB;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x;
        int z;
        final int n = this.api$count.getFlooredAmount(random);

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(size.getX());
            z = random.nextInt(size.getZ());
            func_180709_b(world, random, world.func_175645_m(chunkPos.add(x, 0, z)));
        }
    }

    @Override
    public BlockState getBlock() {
        if (this.api$blockState == null) {
            this.api$blockState = (BlockState) this.block.getDefaultState();
        }
        return this.api$blockState;
    }

    @Override
    public void setBlock(final BlockState state) {
        this.api$blockState = state;
    }

    @Override
    public VariableAmount getRadius() {
        if (this.api$radius == null) {
            this.api$radius = VariableAmount.fixed(this.startRadius);
        }
        return this.api$radius;
    }

    @Override
    public void setRadius(final VariableAmount radius) {
        this.api$radius = radius;
    }

    @Override
    public VariableAmount getCount() {
        return this.api$count;
    }

    @Override
    public void setCount(final VariableAmount count) {
        this.api$count = count;
    }


}
