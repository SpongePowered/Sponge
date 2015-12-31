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

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.common.registry.type.world.gen.PopulatorTypeRegistryModule;
import org.spongepowered.common.util.VecHelper;

import java.util.Random;

public class WrappedBiomeDecorator implements Populator {

    private BiomeDecorator wrapped;
    private BiomeGenBase biome;
    private final PopulatorType type;

    public WrappedBiomeDecorator(BiomeGenBase dec) {
        this.biome = dec;
        this.type = PopulatorTypeRegistryModule.getInstance().getOrCreateForType(this.biome.getClass());
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
    public void populate(Chunk chunk, Random random) {
        World worldIn = (World) chunk.getWorld();
        if (this.biome != null) {
            this.biome.decorate(worldIn, random, VecHelper.toBlockPos(chunk.getBlockMin()));
        } else {
            BiomeGenBase biome = (BiomeGenBase) chunk.getBiome(chunk.getBiomeMin().add(16, 16));
            this.wrapped.decorate(worldIn, random, biome, VecHelper.toBlockPos(chunk.getBlockMin()));
        }
    }

}
