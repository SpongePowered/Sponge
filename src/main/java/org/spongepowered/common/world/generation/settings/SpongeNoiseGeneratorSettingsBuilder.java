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
package org.spongepowered.common.world.generation.settings;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.generation.settings.NoiseGeneratorSettings;
import org.spongepowered.api.world.generation.settings.noise.NoiseSettings;
import org.spongepowered.api.world.generation.settings.structure.StructureGenerationSettings;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

public final class SpongeNoiseGeneratorSettingsBuilder extends AbstractResourceKeyedBuilder<NoiseGeneratorSettings, NoiseGeneratorSettings.Builder> implements NoiseGeneratorSettings.Builder {

    @Override
    public NoiseGeneratorSettings.Builder structureSettings(final StructureGenerationSettings settings) {
        return null;
    }

    @Override
    public NoiseGeneratorSettings.Builder noiseSettings(final NoiseSettings settings) {
        return null;
    }

    @Override
    public NoiseGeneratorSettings.Builder defaultBlock(final BlockState block) {
        return null;
    }

    @Override
    public NoiseGeneratorSettings.Builder defaultFluid(final BlockState fluid) {
        return null;
    }

    @Override
    public NoiseGeneratorSettings.Builder bedrockRoofY(final int y) {
        return null;
    }

    @Override
    public NoiseGeneratorSettings.Builder bedrockFloorY(final int y) {
        return null;
    }

    @Override
    public NoiseGeneratorSettings.Builder seaLevel(final int y) {
        return null;
    }

    @Override
    public NoiseGeneratorSettings.Builder reset() {
        super.reset();

        return null;
    }

    @Override
    public NoiseGeneratorSettings.Builder from(final NoiseGeneratorSettings value) {
        return null;
    }

    @Override
    protected NoiseGeneratorSettings build0() {
        return null;
    }
}
