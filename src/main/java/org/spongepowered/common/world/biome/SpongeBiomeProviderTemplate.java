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
package org.spongepowered.common.world.biome;

import net.minecraft.world.biome.provider.BiomeProvider;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.BiomeProviderTemplate;

import java.util.List;

public final class SpongeBiomeProviderTemplate implements BiomeProviderTemplate {

    public SpongeBiomeProviderTemplate(final BiomeProvider provider) {
    }

    @Override
    public List<RegistryReference<Biome>> choices() {
        return null;
    }

    public static final class FactoryImpl implements BiomeProviderTemplate.Factory {

        @Override
        public BiomeProviderTemplate overworld() {
            return null;
        }

        @Override
        public BiomeProviderTemplate nether() {
            return null;
        }

        @Override
        public BiomeProviderTemplate end() {
            return null;
        }

        @Override
        public BiomeProviderTemplate checkerboard(final List<RegistryReference<Biome>> biomes, final int scale) {
            return null;
        }

        @Override
        public BiomeProviderTemplate single(final RegistryReference<Biome> biome) {
            return null;
        }
    }
}
