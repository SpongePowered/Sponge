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
package org.spongepowered.common.world.volume.biome;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.volume.biome.ImmutableBiomeVolume;
import org.spongepowered.api.world.volume.biome.MutableBiomeVolume;
import org.spongepowered.api.world.volume.biome.ReadableBiomeVolume;
import org.spongepowered.api.world.volume.biome.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.volume.biome.worker.BiomeVolumeStream;
import org.spongepowered.common.world.volume.stream.SpongeBiomeStream;

public class UnmodifiableDownsizedBiomeVolume extends AbstractDownsizedBiomeVolume<ReadableBiomeVolume> implements UnmodifiableBiomeVolume<UnmodifiableDownsizedBiomeVolume> {

    public UnmodifiableDownsizedBiomeVolume(ReadableBiomeVolume volume, Vector3i min, Vector3i max) {
        super(volume, min, max);
    }

    @Override
    public BiomeVolumeStream<UnmodifiableDownsizedBiomeVolume, ?> toBiomeStream() {
        return new SpongeBiomeStream<UnmodifiableDownsizedBiomeVolume, MutableBiomeVolume>(this);
    }

    @Override
    public UnmodifiableDownsizedBiomeVolume getView(Vector3i newMin, Vector3i newMax) {
        checkRange(newMin.getX(), newMin.getY(), newMin.getZ());
        checkRange(newMax.getX(), newMax.getY(), newMax.getZ());
        return new UnmodifiableDownsizedBiomeVolume(this, newMin, newMax);
    }

    @Override
    public ImmutableBiomeVolume asImmutableBiomeVolume() {
        return null; // todo - implement basic immutable biome buffer via palettes or something.
    }
}
