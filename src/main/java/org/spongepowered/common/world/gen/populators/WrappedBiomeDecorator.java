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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.registry.type.world.gen.PopulatorTypeRegistryModule;
import org.spongepowered.common.util.VecHelper;

import java.util.Random;

public class WrappedBiomeDecorator implements Populator {

    private BiomeDecorator wrapped;
    private Biome biome;
    private final PopulatorType type;

    public WrappedBiomeDecorator(Biome dec) {
        this.biome = dec;
        this.type = PopulatorTypeRegistryModule.getInstance().replaceFromForge(this.biome);
    }

    public WrappedBiomeDecorator(BiomeDecorator dec) {
        this.wrapped = dec;
        this.type = PopulatorTypeRegistryModule.getInstance().getOrCreateForType(this.wrapped.getClass());
    }

    @Override
    public PopulatorType getType() {
        return this.type;
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        Vector3i size = extent.getBlockSize();
        World worldIn = (World) world;
        if (this.biome != null) {
            this.biome.func_180624_a(worldIn, random, VecHelper.toBlockPos(min.sub(8, 0, 8)));
        } else {
            Biome biome = (Biome) extent.getBiome(size.getX() / 2 + min.getX(), 0, size.getZ() / 2 + min.getZ());
            this.wrapped.func_180292_a(worldIn, random, biome, VecHelper.toBlockPos(min.sub(8, 0, 8)));
        }
    }

}
