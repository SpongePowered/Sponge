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
package org.spongepowered.common.world.generation.config.noise;

import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.SamplingConfig;
import org.spongepowered.api.world.generation.config.noise.SlideConfig;

import java.util.Objects;

public final class SpongeNoiseConfig {

    private SpongeNoiseConfig() {
    }

    public static final class BuilderImpl implements NoiseConfig.Builder {

        public SamplingConfig sampling;
        public SlideConfig top, bottom;

        public int height, horizontalSize, verticalSize;
        public double densityFactor, densityOffset;
        public boolean simplexForSurface, randomizeDensityOffset, amplified;

        public BuilderImpl() {
            this.reset();
        }

        @Override
        public NoiseConfig.Builder height(final int height) {
            this.height = height;
            return this;
        }

        @Override
        public NoiseConfig.Builder sampling(final SamplingConfig sampling) {
            this.sampling = Objects.requireNonNull(sampling, "sampling");
            return this;
        }

        @Override
        public NoiseConfig.Builder top(final SlideConfig top) {
            this.top = Objects.requireNonNull(top, "top");
            return this;
        }

        @Override
        public NoiseConfig.Builder bottom(final SlideConfig bottom) {
            this.bottom = Objects.requireNonNull(bottom, "bottom");
            return this;
        }

        @Override
        public NoiseConfig.Builder horizontalSize(final int horizontal) {
            this.horizontalSize = horizontal;
            return this;
        }

        @Override
        public NoiseConfig.Builder verticalSize(final int vertical) {
            this.verticalSize = vertical;
            return this;
        }

        @Override
        public NoiseConfig.Builder densityFactor(final double densityFactor) {
            this.densityFactor = densityFactor;
            return this;
        }

        @Override
        public NoiseConfig.Builder densityOffset(final double densityOffset) {
            this.densityOffset = densityOffset;
            return this;
        }

        @Override
        public NoiseConfig.Builder simplexForSurface(boolean simplex) {
            this.simplexForSurface = simplex;
            return this;
        }

        @Override
        public NoiseConfig.Builder randomizeDensityOffset(boolean randomDensityOffset) {
            this.randomizeDensityOffset = randomDensityOffset;
            return this;
        }

        @Override
        public NoiseConfig.Builder amplified(boolean amplified) {
            this.amplified = amplified;
            return this;
        }

        @Override
        public NoiseConfig.Builder reset() {
            this.sampling = SamplingConfig.of(0.9999999814507745D, 80.0D,0.9999999814507745D, 160.0D);
            this.top = SlideConfig.of(-10, 3, 0);
            this.bottom = SlideConfig.of(-30, 0, 0);
            this.height = 256;
            this.horizontalSize = 1;
            this.verticalSize = 2;
            this.densityFactor = 1;
            this.densityOffset = -0.46875D;
            this.simplexForSurface = true;
            this.randomizeDensityOffset = true;
            this.amplified = false;
            return this;
        }

        @Override
        public NoiseConfig.Builder from(final NoiseConfig value) {
            Objects.requireNonNull(value, "value");
            this.sampling = value.samplingConfig();
            this.top = value.topConfig();
            this.bottom = value.bottomConfig();
            this.height = value.height();
            this.horizontalSize = value.horizontalSize();
            this.verticalSize = value.verticalSize();
            this.densityFactor = value.densityFactor();
            this.densityOffset = value.densityOffset();
            this.simplexForSurface = value.simplexForSurface();
            this.randomizeDensityOffset = value.randomizeDensityOffset();
            this.amplified = value.amplified();
            return this;
        }

        @Override
        public @NonNull NoiseConfig build() {
            Objects.requireNonNull(this.sampling, "sampling");
            Objects.requireNonNull(this.top, "top");
            Objects.requireNonNull(this.bottom, "bottom");

            return (NoiseConfig) new net.minecraft.world.level.levelgen.NoiseSettings(this.height, (NoiseSamplingSettings) this.sampling,
                    (net.minecraft.world.level.levelgen.NoiseSlideSettings) this.top, (net.minecraft.world.level.levelgen.NoiseSlideSettings) this.bottom,
                    this.horizontalSize, this.verticalSize, this.densityFactor, this.densityOffset, this.simplexForSurface,
                    this.randomizeDensityOffset, false, this.amplified);
        }
    }

    public static final class FactoryImpl implements NoiseConfig.Factory {

        private static final NoiseConfig OVERWORLD = (NoiseConfig) new net.minecraft.world.level.levelgen.NoiseSettings(256,
                new NoiseSamplingSettings(0.9999999814507745D, 0.9999999814507745D, 80.0D, 160.0D),
                new net.minecraft.world.level.levelgen.NoiseSlideSettings(-10, 3, 0), new net.minecraft.world.level.levelgen.NoiseSlideSettings(-30, 0, 0), 1, 2, 1.0D,
                -0.46875D, true, true, false, false);

        private static final NoiseConfig NETHER = (NoiseConfig) new net.minecraft.world.level.levelgen.NoiseSettings(128,
                new NoiseSamplingSettings(1.0D, 3.0D, 80.0D, 60.0D), new net.minecraft.world.level.levelgen.NoiseSlideSettings(120, 3, 0), new net.minecraft.world.level.levelgen.NoiseSlideSettings(320,
                4, -1), 1, 2, 0.0D, 0.019921875D, false,
                false, false, false);

        private static final NoiseConfig END = (NoiseConfig) new net.minecraft.world.level.levelgen.NoiseSettings(128,
                new NoiseSamplingSettings(2.0D, 1.0D, 80.0D, 160.0D), new net.minecraft.world.level.levelgen.NoiseSlideSettings(-3000, 64, -46), new net.minecraft.world.level.levelgen.NoiseSlideSettings(
                -30, 7, 1), 2, 1, 0.0D, 0.0D,
                true, false, false, false);

        @Override
        public NoiseConfig overworld() {
            return FactoryImpl.OVERWORLD;
        }

        @Override
        public NoiseConfig nether() {
            return FactoryImpl.NETHER;
        }

        @Override
        public NoiseConfig end() {
            return FactoryImpl.END;
        }
    }
}
