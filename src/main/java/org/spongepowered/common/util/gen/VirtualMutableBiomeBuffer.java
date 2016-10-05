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
 * Mutable biome volume backed by a byte array. Reusable.
 *
 * <p>Using {@link #detach()} the underlying byte array can be accessed. The
 * byte array can then be reused by calling {@link #reuse(Vector3i)}.</p>
 */
public final class VirtualMutableBiomeBuffer extends AbstractBiomeBuffer implements MutableBiomeVolume {

    private final BiomeType[] biomes;

    public VirtualMutableBiomeBuffer(Vector3i start, Vector3i size) {
        super(start, size);
        this.biomes = new BiomeType[size.getX() * size.getZ()];
        Arrays.fill(this.biomes, BiomeTypes.OCEAN);
    }

    public VirtualMutableBiomeBuffer(BiomeType[] biomes, Vector3i start, Vector3i size) {
        super(start, size);
        this.biomes = biomes;
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        checkRange(x, y, z);

        this.biomes[getIndex(x, z)] = biome;
    }

    @Override
    public BiomeType getBiome(int x, int y, int z) {
        checkRange(x, y, z);

        return this.biomes[getIndex(x, z)];
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
                return new VirtualMutableBiomeBuffer(this.biomes.clone(), this.start, this.size);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBiomeVolume getImmutableBiomeCopy() {
        return new VirtualImmutableBiomeBuffer(this.biomes, this.start, this.size);
    }

}
