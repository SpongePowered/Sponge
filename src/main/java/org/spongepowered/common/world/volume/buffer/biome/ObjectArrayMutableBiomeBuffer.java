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


import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.volume.biome.MutableBiomeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3i;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Mutable view of a {@link Biome} array.
 *
 * <p>Normally, the {@link ByteArrayMutableBiomeBuffer} class uses memory more
 * efficiently, but when the {@link Biome} array is already created (for
 * example for a contract specified by Minecraft) this implementation becomes
 * more efficient.</p>
 */
public final class ObjectArrayMutableBiomeBuffer extends AbstractBiomeBuffer implements MutableBiomeVolume<ObjectArrayMutableBiomeBuffer> {

    private final BiomeType[] biomes;

    public ObjectArrayMutableBiomeBuffer(final Vector3i start, final Vector3i size) {
        super(start, size);
        this.biomes = new BiomeType[size.getX() * size.getY() * size.getZ()];
        Arrays.fill(this.biomes, BiomeTypes.OCEAN);
    }

    /**
     * Creates a new instance.
     *
     * @param biomes The biome array. The array is not copied, so changes made
     * by this object will write through.
     * @param start The start position
     * @param size The size
     */
    public ObjectArrayMutableBiomeBuffer(final BiomeType[] biomes, final Vector3i start, final Vector3i size) {
        super(start, size);
        this.biomes = biomes;
    }

    @Override
    public BiomeType getBiome(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        return this.biomes[this.getIndex(x, y, z)];
    }

    /**
     * Gets the native biome for the position, resolving virtual biomes to
     * persisted types if needed.
     * 
     * @param x The X position
     * @param y The Y position
     * @param z The X position
     * @return The native biome
     */
    public Biome getNativeBiome(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        BiomeType type = this.biomes[this.getIndex(x, y, z)];
        if (type instanceof VirtualBiomeType) {
            type = ((VirtualBiomeType) type).getPersistedType();
        }
        return (Biome) type;
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final BiomeType biome) {
        Objects.requireNonNull(biome, "biome");
        this.checkRange(x, y, z);
        this.biomes[this.getIndex(x, y, z)] = biome;
        return true;
    }

    public boolean setBiome(final BlockPos pos, final Biome biome) {
        Objects.requireNonNull(biome, "biome");
        Objects.requireNonNull(pos, "pos");
        this.checkRange(pos.getX(), pos.getY(), pos.getZ());
        this.biomes[this.getIndex(pos.getX(), pos.getY(), pos.getZ())] = (BiomeType) biome;
        return true;
    }

    public void fill(final int[] biomes) {
        for (int x = 0; x < this.size.getX(); x++) {
            for (int z = 0; z < this.size.getZ(); z++) {
                BiomeType type = this.biomes[x + z * this.size.getX()];
                if (type instanceof VirtualBiomeType) {
                    type = ((VirtualBiomeType) type).getPersistedType();
                }
                biomes[x + z * this.size.getX()] = Registry.BIOME.getId((Biome) type);
            }
        }
    }

    public void fill(final Biome[] biomes) {
        for (int x = 0; x < this.size.getX(); x++) {
            for (int z = 0; z < this.size.getZ(); z++) {
                BiomeType type = this.biomes[x + z * this.size.getX()];
                if (type instanceof VirtualBiomeType) {
                    type = ((VirtualBiomeType) type).getPersistedType();
                }
                biomes[x + z * this.size.getX()] = (Biome) type;
            }
        }
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
        final ObjectArrayMutableBiomeBuffer that = (ObjectArrayMutableBiomeBuffer) o;
        return Arrays.equals(this.biomes, that.biomes);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(this.biomes);
        return result;
    }

    @Override
    public VolumeStream<ObjectArrayMutableBiomeBuffer, BiomeType> getBiomeStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options
    ) {
        final Vector3i blockMin = this.getBlockMin();
        final Vector3i blockMax = this.getBlockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final BiomeType[] buffer;
        if (options.carbonCopy()) {
            buffer = Arrays.copyOf(this.biomes, this.biomes.length);
        } else {
            buffer = this.biomes;
        }
        final Stream<VolumeElement<ObjectArrayMutableBiomeBuffer, BiomeType>> stateStream = IntStream.range(blockMin.getX(), blockMax.getX() + 1)
            .mapToObj(x -> IntStream.range(blockMin.getZ(), blockMax.getZ() + 1)
                .mapToObj(z -> IntStream.range(blockMin.getY(), blockMax.getY() + 1)
                    .mapToObj(y -> VolumeElement.of(this, () -> buffer[this.getIndex(x, y, z)], new Vector3i(x, y, z)))
                ).flatMap(Function.identity())
            ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }
}
