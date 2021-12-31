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
package org.spongepowered.common.world.biome.provider.multinoise;

import org.spongepowered.api.world.biome.provider.multinoise.MultiNoiseConfig;
import org.spongepowered.common.accessor.world.level.biome.MultiNoiseBiomeSourceAccessor;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public final class SpongeMultiNoiseConfigFactory implements MultiNoiseConfig.Factory {

    @Override
    public MultiNoiseConfig nether() {
        return (MultiNoiseConfig) MultiNoiseBiomeSourceAccessor.accessor$DEFAULT_NOISE_PARAMETERS();
    }

    @Override
    public MultiNoiseConfig of(final int firstOctave, final List<Double> amplitudes) {
        if (Objects.requireNonNull(amplitudes, "amplitudes").isEmpty()) {
            throw new IllegalArgumentException("Amplitudes must have at least 1 value!");
        }
        return (MultiNoiseConfig) new MultiNoiseBiomeSource.NoiseParameters(firstOctave, amplitudes);
    }
}
