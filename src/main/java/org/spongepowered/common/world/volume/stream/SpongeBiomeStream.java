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
package org.spongepowered.common.world.volume.stream;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.volume.biome.MutableBiomeVolume;
import org.spongepowered.api.world.volume.biome.StreamableBiomeVolume;
import org.spongepowered.api.world.volume.biome.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.volume.biome.worker.BiomeVolumeStream;
import org.spongepowered.api.world.volume.worker.VolumeResult;
import org.spongepowered.api.world.volume.worker.function.VolumePredicate;

import java.util.function.Predicate;

public class SpongeBiomeStream<V extends StreamableBiomeVolume<V>,
    M extends MutableBiomeVolume<M>> extends AbstractVolumeStream<V, UnmodifiableBiomeVolume<?>, BiomeType, M> implements BiomeVolumeStream<V, M> {


    public SpongeBiomeStream(V target) {
        super(target);
    }

    @Override
    public BiomeVolumeStream<V, M> filter(VolumePredicate<V, UnmodifiableBiomeVolume<?>, BiomeType, M> predicate) {
        return null; // todo - will need to basically recreate Stream implementation pipelines.
    }

    @Override
    public BiomeVolumeStream<V, M> filter(Predicate<VolumeResult<V, ? super BiomeType>> predicate) {
        return filter(((volume, element, x, y, z) -> predicate.test(VolumeResult.of(volume, element, new Vector3i(x, y, z)))));
    }

    @Override
    public long count() {
        return 0; // TODO - do something here?
    }
}


