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
package org.spongepowered.common.world.volume.stream.biome;

import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.volume.biome.MutableBiomeVolume;
import org.spongepowered.api.world.volume.biome.ReadableBiomeVolume;
import org.spongepowered.api.world.volume.biome.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.volume.worker.VolumeResult;
import org.spongepowered.api.world.volume.worker.VolumeStream;
import org.spongepowered.common.world.volume.stream.EmptyVolumeStream;

import java.util.function.Function;

public class EmptyBiomeStream extends EmptyVolumeStream<ReadableBiomeVolume, UnmodifiableBiomeVolume, BiomeType, MutableBiomeVolume> {

    public EmptyBiomeStream(ReadableBiomeVolume target) {
        super(target);
    }

    @Override
    public <T> VolumeStream<ReadableBiomeVolume, UnmodifiableBiomeVolume, T, MutableBiomeVolume> map(
        Function<VolumeResult<UnmodifiableBiomeVolume, BiomeType>, T> mapper) {
        return null;
    }
}
