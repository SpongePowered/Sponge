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
package org.spongepowered.common.util.gen;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.worker.BiomeVolumeWorker;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.common.world.extent.ImmutableBiomeViewDownsize;
import org.spongepowered.common.world.extent.ImmutableBiomeViewTransform;
import org.spongepowered.common.world.extent.worker.SpongeBiomeVolumeWorker;
import org.spongepowered.common.world.schematic.GlobalPalette;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable biome volume, backed by a byte array. The array passed to the
 * constructor is copied to ensure that the instance is immutable.
 */
public final class ByteArrayImmutableBiomeBuffer extends AbstractBiomeBuffer implements ImmutableBiomeVolume {

    private final byte[] biomes;
    private final Palette<BiomeType> palette;

    public ByteArrayImmutableBiomeBuffer(Palette<BiomeType> palette, byte[] biomes, Vector3i start, Vector3i size) {
        super(start, size);
        this.biomes = biomes.clone();
        this.palette = palette;
    }

    private ByteArrayImmutableBiomeBuffer(Palette<BiomeType> palette, Vector3i start, Vector3i size, byte[] biomes) {
        super(start, size);
        this.biomes = biomes;
        this.palette = palette;
    }

    @Override
    public BiomeType getBiome(int x, int y, int z) {
        checkRange(x, y, z);
        BiomeType biomeType = (BiomeType) Biome.func_185357_a(this.biomes[getIndex(x, z)] & 255);
        return biomeType == null ? BiomeTypes.OCEAN : biomeType;
    }

    @Override
    public ImmutableBiomeVolume getBiomeView(Vector3i newMin, Vector3i newMax) {
        checkRange(newMin.getX(), newMin.getY(), newMin.getZ());
        checkRange(newMax.getX(), newMax.getY(), newMax.getZ());
        return new ImmutableBiomeViewDownsize(this, newMin, newMax);
    }

    @Override
    public ImmutableBiomeVolume getBiomeView(DiscreteTransform3 transform) {
        return new ImmutableBiomeViewTransform(this, transform);
    }

    @Override
    public BiomeVolumeWorker<? extends ImmutableBiomeVolume> getBiomeWorker() {
        return new SpongeBiomeVolumeWorker<>(this);
    }

    @Override
    public MutableBiomeVolume getBiomeCopy(StorageType type) {
        switch (type) {
            case STANDARD:
                return new ByteArrayMutableBiomeBuffer(this.palette, this.biomes.clone(), this.start, this.size);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
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
    public static ImmutableBiomeVolume newWithoutArrayClone(byte[] biomes, Vector3i start, Vector3i size) {
        return new ByteArrayImmutableBiomeBuffer(GlobalPalette.getBiomePalette(), start, size, biomes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ByteArrayImmutableBiomeBuffer that = (ByteArrayImmutableBiomeBuffer) o;
        return Arrays.equals(this.biomes, that.biomes) &&
               this.palette.equals(that.palette);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), this.palette);
        result = 31 * result + Arrays.hashCode(this.biomes);
        return result;
    }
}
