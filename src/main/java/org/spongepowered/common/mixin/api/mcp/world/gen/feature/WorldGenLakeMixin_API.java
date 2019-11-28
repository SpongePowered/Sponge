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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LakesFeature;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.Random;

@Mixin(LakesFeature.class)
public abstract class WorldGenLakeMixin_API extends Feature implements Lake {

    @Shadow @Final @Mutable private Block block;

    private double api$prob = 0.25D;
    private VariableAmount api$height = VariableAmount.baseWithRandomAddition(0, 256);

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.LAKE;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World worldIn, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final World world = (World) worldIn;
        if (random.nextDouble() < this.api$prob) {
            final int x = random.nextInt(size.getX());
            final int y = this.api$height.getFlooredAmount(random);
            final int z = random.nextInt(size.getZ());
            generate(world, random, new BlockPos(x + min.getX(), y + min.getY(), z + min.getZ()));
        }
    }

    // Once we can replace lines with @Injects we can inject into the generate
    // method and change the block to our state, but for now I'm just using the
    // Block field which already exists which means that the default state of
    // the given block type is used rather than the exact blockstate passed in.
    // Since we enforce that it must be a liquid this is almost certainly fine.
    // If its not we can simply add an Overwrite for the generate method and
    // change the part that places down the liquid to use our state rather than
    // the block field.

    @Override
    public BlockState getLiquidType() {
        return (BlockState) this.block.getDefaultState();
    }

    @Override
    public void setLiquidType(final BlockState liquid) {
        checkNotNull(liquid, "Must provide a non-null BlockState!");
        final Optional<MatterProperty> matter = liquid.getType().getProperty(MatterProperty.class);
        checkArgument(matter.isPresent(), "For some reason, the property is not returning correctly!");
        checkArgument(matter.get().getValue() == MatterProperty.Matter.LIQUID, "Must use a liquid property based BlockState!");
        this.block = (Block) liquid.getType();
    }

    @Override
    public double getLakeProbability() {
        return this.api$prob;
    }

    @Override
    public void setLakeProbability(final double p) {
        this.api$prob = p;
    }

    @Override
    public VariableAmount getHeight() {
        return this.api$height;
    }

    @Override
    public void setHeight(final VariableAmount height) {
        this.api$height = height;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Lake")
                .add("Chance", this.api$prob)
                .add("Height", this.api$height)
                .add("LiquidType", this.block)
                .toString();
    }

}
