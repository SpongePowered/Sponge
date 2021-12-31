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
package org.spongepowered.common.registry.provider;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.NearestNeighborBiomeZoomer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.biome.BiomeSampler;

public final class BiomeSamplerProvider {

    public static BiomeSamplerProvider INSTANCE = new BiomeSamplerProvider();

    private final BiMap<ResourceKey, BiomeSampler> mappings;

    private BiomeSamplerProvider() {
        this.mappings = HashBiMap.create();

        this.mappings.put(ResourceKey.sponge("column_fuzzed"), (BiomeSampler) (Object) FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE);
        this.mappings.put(ResourceKey.sponge("fuzzy"), (BiomeSampler) (Object) FuzzyOffsetBiomeZoomer.INSTANCE);
        this.mappings.put(ResourceKey.sponge("default"), (BiomeSampler) (Object) NearestNeighborBiomeZoomer.INSTANCE);
    }

    public @Nullable BiomeSampler get(final ResourceKey key) {
        return this.mappings.get(key);
    }

    public @Nullable ResourceKey get(final BiomeSampler biomeSampler) {
        return this.mappings.inverse().get(biomeSampler);
    }
}
