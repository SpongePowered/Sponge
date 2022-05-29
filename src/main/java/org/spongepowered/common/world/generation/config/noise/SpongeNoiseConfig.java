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

import net.minecraft.world.level.levelgen.NoiseSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;

import java.util.Objects;

public final class SpongeNoiseConfig {

    private SpongeNoiseConfig() {
    }

    public static final class BuilderImpl implements NoiseConfig.Builder {

        public int minY, height, horizontalSize, verticalSize;

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
        public NoiseConfig.Builder reset() {

            // defaults like overworld
            this.minY = -64;
            this.height = 384;
            this.horizontalSize = 1;
            this.verticalSize = 2;
            return this;
        }

        @Override
        public NoiseConfig.Builder from(final NoiseConfig value) {
            Objects.requireNonNull(value, "value");
            this.minY = value.minY();
            this.height = value.height();
            this.horizontalSize = value.horizontalSize();
            this.verticalSize = value.verticalSize();
            return this;
        }

        @Override
        public @NonNull NoiseConfig build() {
            return (NoiseConfig) (Object) NoiseSettings.create(this.minY, this.height, this.horizontalSize, this.verticalSize);
        }
    }

    public static final class FactoryImpl implements NoiseConfig.Factory {

        private static final class Holder {

            // See NoiseGeneratorSettings#overworld
            private static final NoiseConfig OVERWORLD = (NoiseConfig) (Object) NoiseSettings.create(-64, 384, 1, 2);
            // See NoiseGeneratorSettings#nether
            private static final NoiseConfig NETHER = (NoiseConfig) (Object) NoiseSettings.create(0, 128, 1, 2);
            // See NoiseGeneratorSettings#end
            private static final NoiseConfig END = (NoiseConfig) (Object) NoiseSettings.create(0, 128, 2, 1);
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
