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

import net.minecraft.world.gen.settings.ScalingSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.world.generation.settings.noise.NoiseSettings;
import org.spongepowered.api.world.generation.settings.noise.SamplingSettings;
import org.spongepowered.api.world.generation.settings.noise.SlideSettings;

import java.util.Objects;

public final class SpongeNoiseSettings {

    private SpongeNoiseSettings() {
    }

    public static final class BuilderImpl implements NoiseSettings.Builder {

        public SamplingSettings sampling;
        public SlideSettings top, bottom;
        
        public int height, horizontalSize, verticalSize;
        public double densityFactor, densityOffset;
        public boolean simplexForSurface, randomizeDensityOffset, amplified;

        BuilderImpl() {
            this.reset();
        }

        @Override
        public NoiseSettings.Builder height(final int height) {
            this.height = height;
            return this;
        }

        @Override
        public NoiseSettings.Builder sampling(final SamplingSettings sampling) {
            this.sampling = Objects.requireNonNull(sampling, "sampling");
            return this;
        }

        @Override
        public NoiseSettings.Builder top(final SlideSettings top) {
            this.top = Objects.requireNonNull(top, "top");
            return this;
        }

        @Override
        public NoiseSettings.Builder bottom(final SlideSettings bottom) {
            this.bottom = Objects.requireNonNull(bottom, "bottom");
            return this;
        }

        @Override
        public NoiseSettings.Builder horizontalSize(final int horizontal) {
            this.horizontalSize = horizontal;
            return this;
        }

        @Override
        public NoiseSettings.Builder verticalSize(final int vertical) {
            this.verticalSize = vertical;
            return this;
        }

        @Override
        public NoiseSettings.Builder densityFactor(final double densityFactor) {
            this.densityFactor = densityFactor;
            return this;
        }

        @Override
        public NoiseSettings.Builder densityOffset(final double densityOffset) {
            this.densityOffset = densityOffset;
            return this;
        }

        @Override
        public NoiseSettings.Builder simplexForSurface(boolean simplex) {
            this.simplexForSurface = simplex;
            return this;
        }

        @Override
        public NoiseSettings.Builder randomizeDensityOffset(boolean randomDensityOffset) {
            this.randomizeDensityOffset = randomDensityOffset;
            return this;
        }

        @Override
        public NoiseSettings.Builder amplified(boolean amplified) {
            this.amplified = amplified;
            return this;
        }

        @Override
        public NoiseSettings.Builder reset() {
            this.sampling = SamplingSettings.of(0.9999999814507745D, 80.0D,0.9999999814507745D, 160.0D);
            this.top = SlideSettings.of(-10, 3, 0);
            this.bottom = SlideSettings.of(-30, 0, 0);
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
        public NoiseSettings.Builder from(final NoiseSettings value) {
            Objects.requireNonNull(value, "value");
            this.sampling = value.samplingSettings();
            this.top = value.topSettings();
            this.bottom = value.bottomSettings();
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
        public @NonNull NoiseSettings build() {
            Objects.requireNonNull(this.sampling, "sampling");
            Objects.requireNonNull(this.top, "top");
            Objects.requireNonNull(this.bottom, "bottom");

            return (NoiseSettings) new net.minecraft.world.gen.settings.NoiseSettings(this.height, (ScalingSettings) this.sampling,
                    (net.minecraft.world.gen.settings.SlideSettings) this.top, (net.minecraft.world.gen.settings.SlideSettings) this.bottom,
                    this.horizontalSize, this.verticalSize, this.densityFactor, this.densityOffset, this.simplexForSurface,
                    this.randomizeDensityOffset, false, this.amplified);
        }
    }

    public static final class FactoryImpl implements NoiseSettings.Factory {

        private static final NoiseSettings OVERWORLD = (NoiseSettings) new net.minecraft.world.gen.settings.NoiseSettings(256,
                new ScalingSettings(0.9999999814507745D, 0.9999999814507745D, 80.0D, 160.0D),
                new net.minecraft.world.gen.settings.SlideSettings(-10, 3, 0), new net.minecraft.world.gen
                .settings.SlideSettings(-30, 0, 0), 1, 2, 1.0D,
                -0.46875D, true, true, false, false);

        private static final NoiseSettings END = (NoiseSettings) new net.minecraft.world.gen.settings.NoiseSettings(128,
                new ScalingSettings(2.0D, 1.0D, 80.0D, 160.0D), new net.minecraft.world.gen
                .settings.SlideSettings(-3000, 64, -46), new net.minecraft.world.gen.settings.SlideSettings(
                        -30, 7, 1), 2, 1, 0.0D, 0.0D,
                true, false, false, false);

        private static final NoiseSettings NETHER = (NoiseSettings) new net.minecraft.world.gen.settings.NoiseSettings(128,
                new ScalingSettings(1.0D, 3.0D, 80.0D, 60.0D), new net.minecraft.world.gen.settings
                .SlideSettings(120, 3, 0), new net.minecraft.world.gen.settings.SlideSettings(320,
                4, -1), 1, 2, 0.0D, 0.019921875D, false,
                false, false, false);

        @Override
        public NoiseSettings overworld() {
            return FactoryImpl.OVERWORLD;
        }

        @Override
        public NoiseSettings nether() {
            return FactoryImpl.NETHER;
        }

        @Override
        public NoiseSettings end() {
            return FactoryImpl.END;
        }
    }
}
