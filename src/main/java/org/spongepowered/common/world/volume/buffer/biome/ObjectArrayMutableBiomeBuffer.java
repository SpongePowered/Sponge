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


import net.minecraft.core.BlockPos;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.Biomes;
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
 * Mutable view of a {@link net.minecraft.world.level.biome.Biome} array.
 *
 * <p>Normally, the {@link ByteArrayMutableBiomeBuffer} class uses memory more
 * efficiently, but when the {@link net.minecraft.world.level.biome.Biome} array is already created (for
 * example for a contract specified by Minecraft) this implementation becomes
 * more efficient.</p>
 */
public final class ObjectArrayMutableBiomeBuffer extends AbstractBiomeBuffer implements BiomeVolume.Modifiable<ObjectArrayMutableBiomeBuffer> {

    private final RegistryKey<Biome>[] biomes;
    private final Registry<Biome> registry;

    @SuppressWarnings("unchecked")
    public ObjectArrayMutableBiomeBuffer(final Vector3i start, final Vector3i size, final Registry<Biome> biomeRegistry) {
        super(start, size);
        this.biomes = new RegistryKey[size.x() * size.y() * size.z()];
        this.registry = biomeRegistry;
        // for now, biomes are still global to the world/gen register, but we're passing in a registry
        Arrays.fill(this.biomes, Biomes.OCEAN);
    }

    @Override
    public Biome biome(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        final RegistryKey<Biome> key = this.biomes[this.getIndex(x, y, z)];
        return this.registry.value(key);
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
    @SuppressWarnings("ConstantConditions")
    public net.minecraft.world.level.biome.Biome getNativeBiome(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        final RegistryKey<Biome> key = this.biomes[this.getIndex(x, y, z)];
        return this.registry.value(key);
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        Objects.requireNonNull(biome, "biome");
        this.checkRange(x, y, z);
        return this.registry.findValueKey(biome)
            .map(key -> {
                this.biomes[this.getIndex(x, y, z)] =  RegistryKey.of(this.registry.type(), key);
                return true;
            }).orElse(false)
       ;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean setBiome(final BlockPos pos, final net.minecraft.world.level.biome.Biome biome) {
        Objects.requireNonNull(biome, "biome");
        Objects.requireNonNull(pos, "pos");
        this.checkRange(pos.getX(), pos.getY(), pos.getZ());
        return this.registry.findValueKey((Biome) (Object) biome)
            .map(key -> {
                this.biomes[this.getIndex(pos.getX(), pos.getY(), pos.getZ())] =  RegistryKey.of(this.registry.type(), key);
                return true;
            }).orElse(false);
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
    public VolumeStream<ObjectArrayMutableBiomeBuffer, Biome> biomeStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options
    ) {
        VolumeStreamUtils.validateStreamArgs(min, max, this.min(), this.max(), options);
        final RegistryKey<Biome>[] buffer;
        if (options.carbonCopy()) {
            buffer = Arrays.copyOf(this.biomes, this.biomes.length);
        } else {
            buffer = this.biomes;
        }
        final Stream<VolumeElement<ObjectArrayMutableBiomeBuffer, Biome>> stateStream = IntStream.rangeClosed(min.x(), max.x())
            .mapToObj(x -> IntStream.rangeClosed(min.z(), max.z())
                .mapToObj(z -> IntStream.rangeClosed(min.y(), max.y())
                    .mapToObj(y -> VolumeElement.of(this, () ->  {
                        final RegistryKey<Biome> key = buffer[this.getIndex(x, y, z)];
                        final Biome biome = this.registry.value(key);
                        return biome;
                    }, new Vector3d(x, y, z)))
                ).flatMap(Function.identity())
            ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }
}
