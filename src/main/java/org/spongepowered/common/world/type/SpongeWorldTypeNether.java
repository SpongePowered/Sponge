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
package org.spongepowered.common.world.type;

import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NetherGenSettings;

public class SpongeWorldTypeNether extends SpongeWorldType {

    public SpongeWorldTypeNether() {
        super("nether");
        this.enableInfoNotice();
    }

    @Override
    public BiomeProvider createBiomeProvider(final World world) {
        final SingleBiomeProviderSettings settings = BiomeProviderType.FIXED.createSettings();
        settings.setBiome(Biomes.NETHER);
        return BiomeProviderType.FIXED.create(settings);
    }

    @Override
    public IChunkGenerator createChunkGenerator(final World world, final String generatorOptions) {
        final NetherGenSettings settings = ChunkGeneratorType.CAVES.createSettings();
        settings.setDefautBlock(Blocks.NETHERRACK.getDefaultState());
        settings.setDefaultFluid(Blocks.LAVA.getDefaultState());
        return ChunkGeneratorType.CAVES.create(world, this.createBiomeProvider(world), settings);
    }
}
