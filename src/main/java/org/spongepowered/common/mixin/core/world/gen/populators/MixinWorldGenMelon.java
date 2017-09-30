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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMelon;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Melon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenMelon.class)
public class MixinWorldGenMelon implements Melon {

    private VariableAmount count;

    @Inject(method = "<init>()V", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.count = VariableAmount.fixed(10);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.MELON;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World world = (World) worldIn;
        BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        int n = this.count.getFlooredAmount(random);
        int x = random.nextInt(size.getX());
        int z = random.nextInt(size.getZ());
        int y = nextInt(random, Math.min(world.getHeight(position.add(x, 0, z)).getY() * 2, 255));
        position = position.add(x, y, z);
        for (int i = 0; i < n; ++i) {
            BlockPos blockpos1 = position.add(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));

            if (Blocks.MELON_BLOCK.canPlaceBlockAt(world, blockpos1) && world.getBlockState(blockpos1.down()).getBlock() == Blocks.GRASS) {
                world.setBlockState(blockpos1, Blocks.MELON_BLOCK.getDefaultState(), 2);
            }
        }
    }

    private int nextInt(Random rand, int i) {
        if (i <= 1)
            return 0;
        return rand.nextInt(i);
    }

    @Override
    public VariableAmount getMelonsPerChunk() {
        return this.count;
    }

    @Override
    public void setMelonsPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Melon")
                .add("PerChunk", this.count)
                .toString();
    }

}
