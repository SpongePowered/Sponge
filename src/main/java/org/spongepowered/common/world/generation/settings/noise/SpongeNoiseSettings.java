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
package org.spongepowered.common.world.generation.settings.noise;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.world.generation.settings.noise.NoiseSettings;
import org.spongepowered.api.world.generation.settings.noise.SamplingSettings;
import org.spongepowered.api.world.generation.settings.noise.SlideSettings;

public final class SpongeNoiseSettings {

    private SpongeNoiseSettings() {
    }

    public static final class BuilderImpl implements NoiseSettings.Builder {

        @Override
        public NoiseSettings.Builder height(final int height) {
            return null;
        }

        @Override
        public NoiseSettings.Builder sampling(final SamplingSettings sampling) {
            return null;
        }

        @Override
        public NoiseSettings.Builder top(final SlideSettings top) {
            return null;
        }

        @Override
        public NoiseSettings.Builder bottom(final SlideSettings bottom) {
            return null;
        }

        @Override
        public NoiseSettings.Builder horizontalSize(final int horizontal) {
            return null;
        }

        @Override
        public NoiseSettings.Builder verticalSize(final int vertical) {
            return null;
        }

        @Override
        public NoiseSettings.Builder densityFactor(final double densityFactor) {
            return null;
        }

        @Override
        public NoiseSettings.Builder densityOffset(final double densityOffset) {
            return null;
        }

        @Override
        public NoiseSettings.Builder from(final NoiseSettings value) {
            return null;
        }

        @Override
        public @NonNull NoiseSettings build() {
            return null;
        }
    }

    public static final class FactoryImpl implements NoiseSettings.Factory {

        @Override
        public NoiseSettings amplified() {
            return null;
        }

        @Override
        public NoiseSettings overworld() {
            return null;
        }

        @Override
        public NoiseSettings nether() {
            return null;
        }

        @Override
        public NoiseSettings caves() {
            return null;
        }

        @Override
        public NoiseSettings end() {
            return null;
        }

        @Override
        public NoiseSettings floatingIslands() {
            return null;
        }
    }
}
