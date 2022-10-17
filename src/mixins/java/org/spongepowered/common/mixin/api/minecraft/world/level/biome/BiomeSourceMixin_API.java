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
package org.spongepowered.common.mixin.api.minecraft.world.level.biome;

import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Set;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin_API implements org.spongepowered.api.world.biome.provider.BiomeProvider, BiomeManager.NoiseBiomeSource {

    // @formatter:off
    @Shadow public abstract Set<net.minecraft.world.level.biome.Biome> shadow$possibleBiomes();
    @Shadow public abstract Set<net.minecraft.world.level.biome.Biome> shadow$getBiomesWithin(int x, int y, int z, int size, Climate.Sampler sampler);
    // @formatter:on

    @Override
    public Biome find(final int x, final int y, final int z) {
        return (Biome) (Object) this.getNoiseBiome(x, y, z);
    }

    @Override
    public List<Biome> choices() {
        return (List<Biome>) (Object) this.shadow$possibleBiomes();
    }

    /* TODO sampler is not available here?
    public Set<Biome> within(final int x, final int y, final int z, final int size) {
        final Climate.Sampler sampler = ((BiomeSourceBridge) (Object) this).bridge$chunkGenerator().climateSampler();
        return (Set<Biome>) (Object) this.shadow$getBiomesWithin(x, y, z, size, sampler);
    }
    */
}
