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
package org.spongepowered.common.world.generation;

import net.minecraft.world.gen.ChunkGenerator;
import org.spongepowered.api.world.biome.BiomeProviderTemplate;
import org.spongepowered.api.world.generation.ChunkGeneratorTemplate;
import org.spongepowered.api.world.generation.ConfigurableChunkGeneratorTemplate;
import org.spongepowered.api.world.generation.settings.FlatGeneratorSettings;
import org.spongepowered.api.world.generation.settings.NoiseGeneratorSettings;
import org.spongepowered.api.world.generation.settings.structure.StructureGenerationSettings;

public final class SpongeChunkGeneratorTemplate implements ChunkGeneratorTemplate {

    public SpongeChunkGeneratorTemplate(ChunkGenerator generator) {
    }

    @Override
    public BiomeProviderTemplate biomeProvider() {
        return null;
    }

    @Override
    public StructureGenerationSettings structureSettings() {
        return null;
    }

    public ChunkGenerator asType() {
        return null;
    }

    public static final class FactoryImpl implements Factory {

        @Override
        public <T extends FlatGeneratorSettings> ConfigurableChunkGeneratorTemplate<T> flat(final BiomeProviderTemplate provider, final T settings) {
            return null;
        }

        @Override
        public <T extends NoiseGeneratorSettings> ConfigurableChunkGeneratorTemplate<T> noise(final BiomeProviderTemplate provider, final T settings) {
            return null;
        }
    }
}
