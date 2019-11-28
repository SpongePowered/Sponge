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
package org.spongepowered.common.world.gen.populators;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.SeaFloor;

import java.util.Random;
import java.util.function.Predicate;

public class SeaFloorPopulator implements SeaFloor {

    private BlockState block;
    private VariableAmount radius;
    private VariableAmount count;
    private Predicate<BlockState> check;
    private VariableAmount depth;

    public SeaFloorPopulator(BlockState block, VariableAmount radius, VariableAmount count, VariableAmount depth, Predicate<BlockState> check) {
        this.block = checkNotNull(block, "block");
        this.radius = checkNotNull(radius, "radius");
        this.count = checkNotNull(count, "count");
        this.check = checkNotNull(check, "check");
        this.depth = checkNotNull(depth, "depth");
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.SEA_FLOOR;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        int n = this.count.getFlooredAmount(random);
        BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        for (int i = 0; i < n; i++) {
            BlockPos pos = position.add(random.nextInt(size.getX()), 0, random.nextInt(size.getZ()));
            // This method is incorrectly named, it simply gets the top block
            // that blocks movement and isn't leaves
            pos = world.getTopSolidOrLiquidBlock(pos);
            if (world.getBlockState(pos).getMaterial() != Material.WATER) {
                continue;
            }
            int radius = this.radius.getFlooredAmount(random);
            int depth = this.depth.getFlooredAmount(random);
            for (int x = pos.getX() - radius; x <= pos.getX() + radius; ++x) {
                int x0 = x - pos.getX();
                for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; ++z) {
                    int z0 = z - pos.getZ();
                    if (x0 * x0 + z0 * z0 > radius * radius) {
                        continue;
                    }
                    for (int y = pos.getY() - depth; y <= pos.getY() + depth; ++y) {
                        BlockPos blockpos1 = new BlockPos(x, y, z);
                        if (this.check.test((BlockState) world.getBlockState(blockpos1))) {
                            world.setBlockState(blockpos1, (net.minecraft.block.BlockState) this.block, 2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public BlockState getBlock() {
        return this.block;
    }

    @Override
    public void setBlock(BlockState block) {
        this.block = block;
    }

    @Override
    public VariableAmount getDiscsPerChunk() {
        return this.count;
    }

    @Override
    public void setDiscsPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public VariableAmount getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(VariableAmount radius) {
        this.radius = radius;
    }

    @Override
    public Predicate<BlockState> getValidBlocksToReplace() {
        return this.check;
    }

    @Override
    public void setValidBlocksToReplace(Predicate<BlockState> check) {
        this.check = check;
    }

    @Override
    public VariableAmount getDepth() {
        return this.depth;
    }

    @Override
    public void setDepth(VariableAmount depth) {
        this.depth = depth;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("depth", this.depth)
                .add("radius", this.radius)
                .add("block", this.block.getType().getName())
                .add("count", this.count)
                .toString();
    }

}
