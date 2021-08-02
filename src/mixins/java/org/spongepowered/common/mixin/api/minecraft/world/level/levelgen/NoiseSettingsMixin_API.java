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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import org.spongepowered.api.world.generation.config.noise.NoiseConfig;
import org.spongepowered.api.world.generation.config.noise.SamplingConfig;
import org.spongepowered.api.world.generation.config.noise.SlideConfig;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseSettings.class)
@Implements(@Interface(iface = NoiseConfig.class, prefix = "noiseConfig$", remap = Remap.NONE))
public abstract class NoiseSettingsMixin_API implements NoiseConfig {

    // @formatter:off
    @Shadow public abstract int shadow$height();
    @Shadow public abstract NoiseSamplingSettings shadow$noiseSamplingSettings();
    @Shadow public abstract net.minecraft.world.level.levelgen.NoiseSlideSettings shadow$topSlideSettings();
    @Shadow public abstract net.minecraft.world.level.levelgen.NoiseSlideSettings shadow$bottomSlideSettings();
    @Shadow public abstract int shadow$noiseSizeHorizontal();
    @Shadow public abstract int shadow$noiseSizeVertical();
    @Shadow public abstract double shadow$densityFactor();
    @Shadow public abstract double shadow$densityOffset();
    @Shadow @Deprecated public abstract boolean shadow$useSimplexSurfaceNoise();
    @Shadow @Deprecated public abstract boolean shadow$randomDensityOffset();
    @Shadow @Deprecated public abstract boolean shadow$isAmplified();
    // @formatter:on

    @Intrinsic
    public int noiseConfig$height() {
        return this.shadow$height();
    }

    @Override
    public SamplingConfig samplingConfig() {
        return (SamplingConfig) this.shadow$noiseSamplingSettings();
    }

    @Override
    public SlideConfig topConfig() {
        return (SlideConfig) this.shadow$topSlideSettings();
    }

    @Override
    public SlideConfig bottomConfig() {
        return (SlideConfig) this.shadow$bottomSlideSettings();
    }

    @Override
    public int horizontalSize() {
        return this.shadow$noiseSizeHorizontal();
    }

    @Override
    public int verticalSize() {
        return this.shadow$noiseSizeVertical();
    }

    @Intrinsic
    public double noiseConfig$densityFactor() {
        return this.shadow$densityFactor();
    }

    @Intrinsic
    public double noiseConfig$densityOffset() {
        return this.shadow$densityOffset();
    }

    @Override
    public boolean simplexForSurface() {
        return this.shadow$useSimplexSurfaceNoise();
    }

    @Override
    public boolean randomizeDensityOffset() {
        return this.shadow$randomDensityOffset();
    }

    @Override
    public boolean amplified() {
        return this.shadow$isAmplified();
    }
}
