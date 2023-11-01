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
package org.spongepowered.common.world.volume.buffer.biome;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.volume.biome.BiomeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Mutable biome volume backed by a byte array.
 */
public final class ByteArrayMutableBiomeBuffer extends AbstractBiomeBuffer implements BiomeVolume.Mutable {

    private final byte[] biomes;
    private final Palette.Mutable<Biome, Biome> palette;

    public ByteArrayMutableBiomeBuffer(final Palette<Biome, Biome> palette, final Vector3i start, final Vector3i size) {
        this(palette, new byte[size.x() * size.y() * size.z()], start, size);
    }

    public ByteArrayMutableBiomeBuffer(final Palette<Biome, Biome> palette, final byte[] biomes, final Vector3i start, final Vector3i size) {
        super(start, size);
        this.biomes = biomes;
        this.palette = palette.asMutable(Sponge.game());
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        this.checkRange(x, y, z);

        this.biomes[this.getIndex(x, y, z)] = (byte) this.palette.orAssign(biome);
        return true;
    }

    @Override
    public Biome biome(final int x, final int y, final int z) {
        this.checkRange(x, y, z);

        final byte biomeId = this.biomes[this.getIndex(x, y, z)];
        return this.palette.get(biomeId & 255, Sponge.server())
            .orElseGet(() -> Biomes.OCEAN.get(Sponge.server())
            );
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ByteArrayMutableBiomeBuffer that = (ByteArrayMutableBiomeBuffer) o;
        return Arrays.equals(this.biomes, that.biomes) &&
               this.palette.equals(that.palette);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), this.palette);
        result = 31 * result + Arrays.hashCode(this.biomes);
        return result;
    }

    @Override
    public VolumeStream<BiomeVolume.Mutable, Biome> biomeStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options
    ) {
        final Vector3i blockMin = this.min();
        final Vector3i blockMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final byte[] biomes;
        if (options.carbonCopy()) {
            biomes = Arrays.copyOf(this.biomes, this.biomes.length);
        } else {
            biomes = this.biomes;
        }
        final Stream<VolumeElement<BiomeVolume.Mutable, Biome>> stateStream = IntStream.rangeClosed(min.x(), max.x())
            .mapToObj(x -> IntStream.rangeClosed(min.z(), max.z())
                .mapToObj(z -> IntStream.rangeClosed(min.y(), max.y())
                    .mapToObj(y -> VolumeElement.of((BiomeVolume.Mutable) this, () -> {
                        final byte biomeId = biomes[this.getIndex(x, y, z)];
                        return this.palette.get(biomeId & 255, Sponge.server())
                            .orElseGet(() -> Sponge.server()
                                .registry(RegistryTypes.BIOME)
                                .value(Biomes.OCEAN)
                            );
                    }, new Vector3d(x, y, z)))
                ).flatMap(Function.identity())
            ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    public Palette.Mutable<Biome, Biome> getPalette() {
        return this.palette;
    }
}
