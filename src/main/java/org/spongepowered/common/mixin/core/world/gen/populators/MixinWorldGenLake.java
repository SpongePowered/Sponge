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
package org.spongepowered.common.mixin.core.world.gen.populators;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenLakes;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.property.block.MatterProperty.Matter;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Random;

@Mixin(WorldGenLakes.class)
public abstract class MixinWorldGenLake implements Lake {

    private double prob;
    private VariableAmount height;

    @Shadow private Block block;

    @Shadow
    public abstract boolean generate(World worldIn, Random rand, BlockPos position);

    @Inject(method = "<init>(Lnet/minecraft/block/Block;)V", at = @At("RETURN") , require = 1)
    public void onConstructed(Block block, CallbackInfo ci) {
        this.prob = 0.25D;
        this.height = VariableAmount.baseWithRandomAddition(0, 256);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.LAKE;
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        if (random.nextDouble() < this.prob) {
            int x = random.nextInt(16) + 8;
            int y = this.height.getFlooredAmount(random);
            int z = random.nextInt(16) + 8;
            generate(world, random, new BlockPos(x + chunk.getBlockMin().getX(), y + chunk.getBlockMin().getY(),
                    z + chunk.getBlockMin().getZ()));
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
    public void setLiquidType(BlockState liquid) {
        Optional<MatterProperty> matter = liquid.getType().getProperty(MatterProperty.class);
        checkArgument(matter.isPresent() && matter.get().getValue() == Matter.LIQUID);
        this.block = (Block) liquid.getType();
    }

    @Override
    public double getLakeProbability() {
        return this.prob;
    }

    @Override
    public void setLakeProbability(double p) {
        this.prob = p;
    }

    @Override
    public VariableAmount getHeight() {
        return this.height;
    }

    @Override
    public void setHeight(VariableAmount height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("Type", "Lake")
                .add("Chance", this.prob)
                .add("Height", this.height)
                .add("LiquidType", this.block)
                .toString();
    }

}
