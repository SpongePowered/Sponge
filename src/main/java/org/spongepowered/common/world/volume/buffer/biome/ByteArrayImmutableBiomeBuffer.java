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
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.volume.biome.ImmutableBiomeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3i;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable biome volume, backed by a byte array. The array passed to the
 * constructor is copied to ensure that the instance is immutable.
 */
public final class ByteArrayImmutableBiomeBuffer extends AbstractBiomeBuffer implements ImmutableBiomeVolume {

    private final byte[] biomes;
    private final Palette<BiomeType> palette;

    public ByteArrayImmutableBiomeBuffer(final Palette<BiomeType> palette, final byte[] biomes, final Vector3i start, final Vector3i size) {
        super(start, size);
        this.biomes = biomes.clone();
        this.palette = palette;
    }

    private ByteArrayImmutableBiomeBuffer(final Palette<BiomeType> palette, final Vector3i start, final Vector3i size, final byte[] biomes) {
        super(start, size);
        this.biomes = biomes;
        this.palette = palette;
    }

    @Override
    public BiomeType getBiome(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        return this.palette.get(this.biomes[this.getIndex(x, y, z)]).orElseGet(BiomeTypes.OCEAN);
    }

    /**
     * This method doesn't clone the array passed into it. INTERNAL USE ONLY.
     * Make sure your code doesn't leak the reference if you're using it.
     *
     * @param biomes The biomes to store
     * @param start The start of the volume
     * @param size The size of the volume
     * @return A new buffer using the same array reference
     */
    public static ImmutableBiomeVolume newWithoutArrayClone(final byte[] biomes, final Vector3i start, final Vector3i size) {
        return new ByteArrayImmutableBiomeBuffer(GlobalPalette.getBiomePalette(), start, size, biomes);
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
        final ByteArrayImmutableBiomeBuffer that = (ByteArrayImmutableBiomeBuffer) o;
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
    public VolumeStream<ImmutableBiomeVolume, BiomeType> getBiomeStream(final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        final Vector3i blockMin = this.getBlockMin();
        final Vector3i blockMax = this.getBlockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);

        final Stream<VolumeElement<ImmutableBiomeVolume, BiomeType>> stateStream = IntStream.range(blockMin.getX(), blockMax.getX() + 1)
            .mapToObj(x -> IntStream.range(blockMin.getZ(), blockMax.getZ() + 1)
                .mapToObj(z -> IntStream.range(blockMin.getY(), blockMax.getY() + 1)
                    .mapToObj(y -> VolumeElement.<ImmutableBiomeVolume, BiomeType>of(this, () -> {
                        final byte biomeId = this.biomes[this.getIndex(x, y, z)];
                        return this.palette.get(biomeId & 255).orElseGet(BiomeTypes.OCEAN);
                    }, new Vector3i(x, y, z)))
                ).flatMap(Function.identity())
            ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }
}
