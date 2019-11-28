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
import com.google.common.base.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(OreFeature.class)
public abstract class WorldGenMinableMixin_API extends Feature implements Ore {

    @Shadow @Final @Mutable private net.minecraft.block.BlockState oreBlock;
    @Shadow @Final @Mutable private int numberOfBlocks;
    @Shadow @Final @Mutable private Predicate<net.minecraft.block.BlockState> predicate;

    private VariableAmount api$size = VariableAmount.fixed(1); // default but overridden  in WorldGenMineableMixin
    private VariableAmount api$count = VariableAmount.fixed(16);
    private VariableAmount api$height = VariableAmount.baseWithRandomAddition(0, 64);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.ORE;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        final int n = this.api$count.getFlooredAmount(random);
        // WorldGenMineable applies an 8 block offset to the blockpos
        final BlockPos position = new BlockPos(min.getX() - 8, min.getY(), min.getZ() - 8);
        for (int i = 0; i < n; i++) {
            final BlockPos pos = position.add(random.nextInt(size.getX()), this.api$height.getFlooredAmount(random), random.nextInt(size.getX()));
            this.numberOfBlocks = this.api$size.getFlooredAmount(random);
            generate(world, random, pos);
        }
    }

    @Override
    public BlockState getOreBlock() {
        return (BlockState) this.oreBlock;
    }

    @Override
    public void setOreBlock(final BlockState block) {
        this.oreBlock = (net.minecraft.block.BlockState) block;
    }

    @Override
    public VariableAmount getDepositSize() {
        return this.api$size;
    }

    @Override
    public void setDepositSize(final VariableAmount size) {
        this.api$size = size;
    }

    @Override
    public VariableAmount getDepositsPerChunk() {
        return this.api$count;
    }

    @Override
    public void setDepositsPerChunk(final VariableAmount count) {
        this.api$count = count;
    }

    @Override
    public VariableAmount getHeight() {
        return this.api$height;
    }

    @Override
    public void setHeight(final VariableAmount height) {
        this.api$height = height;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public java.util.function.Predicate<BlockState> getPlacementCondition() {
        return (Predicate) this.predicate;
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public void setPlacementCondition(final java.util.function.Predicate<BlockState> condition) {
        this.predicate = foo -> condition.test((BlockState) foo);
    }

}
