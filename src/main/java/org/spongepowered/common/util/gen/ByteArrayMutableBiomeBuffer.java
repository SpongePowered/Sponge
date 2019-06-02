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
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.extent.worker.MutableBiomeVolumeWorker;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.common.world.extent.MutableBiomeViewDownsize;
import org.spongepowered.common.world.extent.MutableBiomeViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBiomeVolumeWrapper;
import org.spongepowered.common.world.extent.worker.SpongeMutableBiomeVolumeWorker;

import java.util.Arrays;
import java.util.Objects;

/**
 * Mutable biome volume backed by a byte array. Reusable.
 *
 * <p>Using {@link #detach()} the underlying byte array can be accessed.
 * The byte array can then be reused by calling {@link #reuse(Vector3i)}.</p>
 */
public final class ByteArrayMutableBiomeBuffer extends AbstractBiomeBuffer implements MutableBiomeVolume {

    private boolean detached;
    private final byte[] biomes;
    private final Palette<BiomeType> palette;

    private void checkOpen() {
        checkState(!this.detached, "trying to use buffer after it's closed");
    }

    public ByteArrayMutableBiomeBuffer(Palette<BiomeType> palette, Vector3i start, Vector3i size) {
        this(palette, new byte[size.getX() * size.getZ()], start, size);
    }

    public ByteArrayMutableBiomeBuffer(Palette<BiomeType> palette, byte[] biomes, Vector3i start, Vector3i size) {
        super(start, size);
        this.biomes = biomes;
        this.palette = palette;
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        checkOpen();
        checkRange(x, y, z);

        this.biomes[getIndex(x, z)] = (byte) this.palette.getOrAssign(biome);
    }

    @Override
    public BiomeType getBiome(int x, int y, int z) {
        checkOpen();
        checkRange(x, y, z);

        byte biomeId = this.biomes[getIndex(x, z)];
        return this.palette.get(biomeId & 255).orElse(BiomeTypes.OCEAN);
    }

    /**
     * Gets the internal byte array, and prevents further of it through this
     * object uses until {@link #reuse(Vector3i)} is called.
     *
     * @return The internal byte array.
     */
    public byte[] detach() {
        checkOpen();

        this.detached = true;
        return this.biomes;
    }

    /**
     * Gets whether this biome volume is currently detached. When detached, this
     * object is available for reuse using {@link #reuse(Vector3i)}.
     *
     * @return Whether this biome volume is detached
     */
    public boolean isDetached() {
        return this.detached;
    }

    /**
     * Changes the bounds of this biome volume, so that it can be reused for
     * another chunk.
     *
     * @param start New start position.
     */
    public void reuse(Vector3i start) {
        checkState(this.detached, "Cannot reuse while still in use");

        this.start = checkNotNull(start, "start");
        this.end = this.start.add(this.size).sub(Vector3i.ONE);
        Arrays.fill(this.biomes, (byte) 0);

        this.detached = false;
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
        checkOpen();
        switch (type) {
            case STANDARD:
                return new ByteArrayMutableBiomeBuffer(this.palette, this.biomes.clone(), this.start, this.size);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBiomeVolume getImmutableBiomeCopy() {
        checkOpen();
        return new ByteArrayImmutableBiomeBuffer(this.palette, this.biomes, this.start, this.size);
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
        ByteArrayMutableBiomeBuffer that = (ByteArrayMutableBiomeBuffer) o;
        return this.detached == that.detached &&
               Arrays.equals(this.biomes, that.biomes) &&
               this.palette.equals(that.palette);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), this.detached, this.palette);
        result = 31 * result + Arrays.hashCode(this.biomes);
        return result;
    }
}
