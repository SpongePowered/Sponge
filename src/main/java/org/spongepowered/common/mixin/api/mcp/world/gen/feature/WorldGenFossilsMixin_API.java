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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FossilsFeature;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.Fossil;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(FossilsFeature.class)
public abstract class WorldGenFossilsMixin_API extends Feature implements Fossil {

    private double api$chance = 1 / 64.0;

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.FOSSIL;
    }

    @Override
    public void populate(World world, Extent volume, Random random) {
        if (random.nextDouble() > this.api$chance) {
            return;
        }
        int x = volume.getBlockMin().getX();
        int z = volume.getBlockMin().getZ();
        // Here, we want to reset the coordinates to the origin point or (0,0) in relation to the
        // chunk being generated.
        generate((net.minecraft.world.World) world, random, new BlockPos((x >> 4) << 4, 0, (z >> 4) << 4));
    }

    @Override
    public double getSpawnProbability() {
        return this.api$chance;
    }

    @Override
    public void setSpawnProbability(double chance) {
        this.api$chance = chance;
    }

}
