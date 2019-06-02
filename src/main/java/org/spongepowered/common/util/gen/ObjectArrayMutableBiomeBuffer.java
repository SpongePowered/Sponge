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

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.extent.worker.MutableBiomeVolumeWorker;
import org.spongepowered.common.world.extent.MutableBiomeViewDownsize;
import org.spongepowered.common.world.extent.MutableBiomeViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBiomeVolumeWrapper;
import org.spongepowered.common.world.extent.worker.SpongeMutableBiomeVolumeWorker;

import java.util.Arrays;

/**
 * Mutable view of a {@link Biome} array.
 *
 * <p>Normally, the {@link ByteArrayMutableBiomeBuffer} class uses memory more
 * efficiently, but when the {@link Biome} array is already created (for
 * example for a contract specified by Minecraft) this implementation becomes
 * more efficient.</p>
 */
public final class ObjectArrayMutableBiomeBuffer extends AbstractBiomeBuffer implements MutableBiomeVolume {

    private final BiomeType[] biomes;

    public ObjectArrayMutableBiomeBuffer(Vector3i start, Vector3i size) {
        super(start, size);
        this.biomes = new BiomeType[size.getX() * size.getZ()];
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
    public ObjectArrayMutableBiomeBuffer(BiomeType[] biomes, Vector3i start, Vector3i size) {
        super(start, size);
        this.biomes = biomes;
    }

    @Override
    public BiomeType getBiome(int x, int y, int z) {
        checkRange(x, y, z);
        return this.biomes[getIndex(x, z)];
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
    public Biome getNativeBiome(int x, int y, int z) {
        checkRange(x, y, z);
        BiomeType type = this.biomes[getIndex(x, z)];
        if (type instanceof VirtualBiomeType) {
            type = ((VirtualBiomeType) type).getPersistedType();
        }
        return (Biome) type;
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        checkNotNull(biome, "biome");
        checkRange(x, y, z);
        this.biomes[getIndex(x, z)] = biome;
    }

    /**
     * Changes the bounds of this biome volume, so that it can be reused for
     * another chunk.
     *
     * @param start New start position.
     */
    public void reuse(Vector3i start) {
        this.start = checkNotNull(start, "start");
        this.end = this.start.add(this.size).sub(Vector3i.ONE);
        Arrays.fill(this.biomes, BiomeTypes.OCEAN);
    }

    public void fill(byte[] biomes) {
        for (int x = 0; x < this.size.getX(); x++) {
            for (int z = 0; z < this.size.getZ(); z++) {
                BiomeType type = this.biomes[x + z * this.size.getX()];
                if (type instanceof VirtualBiomeType) {
                    type = ((VirtualBiomeType) type).getPersistedType();
                }
                biomes[x + z * this.size.getX()] = (byte) Biome.getIdForBiome((Biome) type);
            }
        }
    }

    public void fill(Biome[] biomes) {
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
    public MutableBiomeVolume getBiomeView(Vector3i newMin, Vector3i newMax) {
        checkRange(newMin.getX(), newMin.getY(), newMin.getZ());
        checkRange(newMax.getX(), newMax.getY(), newMax.getZ());
        return new MutableBiomeViewDownsize(this, newMin, newMax);
    }

    @Override
    public MutableBiomeVolume getBiomeView(DiscreteTransform3 transform) {
        return new MutableBiomeViewTransform(this, transform);
    }

    @Override
    public MutableBiomeVolumeWorker<? extends MutableBiomeVolume> getBiomeWorker() {
        return new SpongeMutableBiomeVolumeWorker<>(this);
    }

    @Override
    public UnmodifiableBiomeVolume getUnmodifiableBiomeView() {
        return new UnmodifiableBiomeVolumeWrapper(this);
    }

    @Override
    public MutableBiomeVolume getBiomeCopy(StorageType type) {
        switch (type) {
            case STANDARD:
                return new ObjectArrayMutableBiomeBuffer(this.biomes.clone(), this.start, this.size);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBiomeVolume getImmutableBiomeCopy() {
        return new ObjectArrayImmutableBiomeBuffer(this.biomes, this.start, this.size);
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
        ObjectArrayMutableBiomeBuffer that = (ObjectArrayMutableBiomeBuffer) o;
        return Arrays.equals(this.biomes, that.biomes);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(this.biomes);
        return result;
    }

}
