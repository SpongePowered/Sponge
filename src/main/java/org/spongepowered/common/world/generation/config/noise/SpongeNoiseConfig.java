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

import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.NoiseSlider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.SamplingConfig;
import org.spongepowered.api.world.generation.config.noise.Shaper;
import org.spongepowered.api.world.generation.config.noise.SlideConfig;

import java.util.Objects;

public final class SpongeNoiseConfig {

    private SpongeNoiseConfig() {
    }

    public static final class BuilderImpl implements NoiseConfig.Builder {

        public SamplingConfig sampling;
        public SlideConfig top, bottom;
        
        public int minY, height, horizontalSize, verticalSize;
        public boolean largeBiomes, islandNoiseOverride;

        private Shaper terrainShaper;

        public BuilderImpl() {
            this.reset();
        }

        @Override
        public NoiseConfig.Builder minY(final int minY) {
            this.minY = minY;
            return this;
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
        public NoiseConfig.Builder islandNoiseOverride(final boolean islandNoiseOverride) {
            this.islandNoiseOverride = islandNoiseOverride;
            return this;
        }

        @Override
        public NoiseConfig.Builder largeBiomes(final boolean largeBiomes) {
            this.largeBiomes = largeBiomes;
            return this;
        }

        @Override
        public NoiseConfig.Builder terrainShaper(Shaper terrainShaper) {
            this.terrainShaper = terrainShaper;
            return this;
        }

        @Override
        public NoiseConfig.Builder reset() {

            // defaults like overworld
            this.minY = -64;
            this.height = 384;
            this.sampling = SamplingConfig.of(1D, 80.0D,1D, 160.0D);
            this.top = SlideConfig.of(-0.078125D, 2, 8);
            this.bottom = SlideConfig.of(0.1171875D, 3, 0);
            this.horizontalSize = 1;
            this.verticalSize = 2;
            this.islandNoiseOverride = false;
            this.largeBiomes = false;
            this.terrainShaper = Shaper.overworld();
            return this;
        }

        @Override
        public NoiseConfig.Builder from(final NoiseConfig value) {
            Objects.requireNonNull(value, "value");
            this.minY = value.minY();
            this.height = value.height();
            this.sampling = value.samplingConfig();
            this.top = value.topConfig();
            this.bottom = value.bottomConfig();
            this.horizontalSize = value.horizontalSize();
            this.verticalSize = value.verticalSize();
            this.islandNoiseOverride = value.islandNoiseOverride();
            this.largeBiomes = value.largeBiomes();
            this.terrainShaper = value.terrainShaper();
            return this;
        }

        @Override
        public @NonNull NoiseConfig build() {
            Objects.requireNonNull(this.sampling, "sampling");
            Objects.requireNonNull(this.top, "top");
            Objects.requireNonNull(this.bottom, "bottom");

            return (NoiseConfig) (Object) NoiseSettings.create(this.minY, this.height, (NoiseSamplingSettings) this.sampling,
                    (NoiseSlider) this.top, (NoiseSlider) this.bottom,
                    this.horizontalSize, this.verticalSize, this.islandNoiseOverride, false, this.largeBiomes,
                    (TerrainShaper) (Object) this.terrainShaper);
        }
    }

    public static final class FactoryImpl implements NoiseConfig.Factory {

        private static final class Holder {

            // See NoiseGeneratorSettings#overworld
            private static final NoiseConfig OVERWORLD = (NoiseConfig) (Object) NoiseSettings.create(-64, 384,
                    new NoiseSamplingSettings(1.0D, 1.0D, 80.0D, 160.0D), new NoiseSlider(-0.078125D, 2, 8),
                    new NoiseSlider(0.1171875D, 3, 0), 1, 2, false, false, false, TerrainProvider.overworld(false));
            // See NoiseGeneratorSettings#nether
            private static final NoiseConfig NETHER = (NoiseConfig) (Object) NoiseSettings.create(0, 128,
                    new NoiseSamplingSettings(1.0D, 3.0D, 80.0D, 60.0D), new NoiseSlider(0.9375D, 3, 0),
                    new NoiseSlider(2.5D, 4, -1), 1, 2, false, false, false, TerrainProvider.nether());
            // See NoiseGeneratorSettings#end
            private static final NoiseConfig END = (NoiseConfig) (Object) NoiseSettings.create(0, 128,
                    new NoiseSamplingSettings(2.0D, 1.0D, 80.0D, 160.0D), new NoiseSlider(-23.4375D, 64, -46),
                    new NoiseSlider(-0.234375D, 7, 1), 2, 1, true, false, false, TerrainProvider.end());
        }

        @Override
        public NoiseConfig overworld() {
            return FactoryImpl.Holder.OVERWORLD;
        }

        @Override
        public NoiseConfig nether() {
            return FactoryImpl.Holder.NETHER;
        }

        @Override
        public NoiseConfig end() {
            return FactoryImpl.Holder.END;
        }
    }
}
