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

import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.generation.config.noise.Noise;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.DoubleStream;

public final class SpongeNoiseBuilder implements Noise.Builder {

    @Nullable private Integer octave;
    @Nullable private List<Double> amplitudes;

    public SpongeNoiseBuilder() {
        this.reset();
    }

    @Override
    public Noise.Builder from(final Noise noise) {
        this.octave(noise.octave()).amplitudes(noise.amplitudes());
        return this;
    }

    @Override
    public Noise.Builder octave(final int octave) {
        this.octave = octave;
        return this;
    }

    @Override
    public Noise.Builder amplitudes(final double... amplitudes) {
        this.amplitudes = DoubleStream.of(amplitudes).boxed().toList();
        return this;
    }

    @Override
    public Noise.Builder amplitudes(final List<Double> amplitudes) {
        this.amplitudes = amplitudes;
        return this;
    }

    @Override
    public Noise.Builder reset() {
        this.octave = null;
        this.amplitudes = null;
        return this;
    }

    @Override
    public Noise build() {
        Objects.requireNonNull(this.octave, "octave");
        Objects.requireNonNull(this.amplitudes, "amplitudes");
        return (Noise) (Object) new NormalNoise.NoiseParameters(this.octave, new ArrayList<>(this.amplitudes));
    }
}
