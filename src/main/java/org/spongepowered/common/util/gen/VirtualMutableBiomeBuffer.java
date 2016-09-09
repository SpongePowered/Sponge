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

import com.flowpowered.math.vector.Vector2i;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.util.DiscreteTransform2;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBiomeArea;
import org.spongepowered.api.world.extent.worker.MutableBiomeAreaWorker;
import org.spongepowered.common.world.extent.MutableBiomeViewDownsize;
import org.spongepowered.common.world.extent.MutableBiomeViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBiomeAreaWrapper;
import org.spongepowered.common.world.extent.worker.SpongeMutableBiomeAreaWorker;

import java.util.Arrays;

/**
 * Mutable biome area backed by a byte array. Reusable.
 *
 * <p>Using {@link #detach()} the underlying byte array can be accessed. The
 * byte array can then be reused by calling {@link #reuse(Vector2i)}.</p>
 */
public final class VirtualMutableBiomeBuffer extends AbstractBiomeBuffer implements MutableBiomeArea {

    private final BiomeType[] biomes;

    public VirtualMutableBiomeBuffer(Vector2i start, Vector2i size) {
        super(start, size);
        this.biomes = new BiomeType[size.getX() * size.getY()];
        Arrays.fill(this.biomes, BiomeTypes.OCEAN);
    }

    public VirtualMutableBiomeBuffer(BiomeType[] biomes, Vector2i start, Vector2i size) {
        super(start, size);
        this.biomes = biomes;
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        checkRange(x, z);

        this.biomes[getIndex(x, z)] = biome;
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkRange(x, z);

        return this.biomes[getIndex(x, z)];
    }

    /**
     * Changes the bounds of this biome area, so that it can be reused for
     * another chunk.
     *
     * @param start New start position.
     */
    public void reuse(Vector2i start) {
        this.start = checkNotNull(start, "start");
        this.end = this.start.add(this.size).sub(Vector2i.ONE);
        Arrays.fill(this.biomes, BiomeTypes.OCEAN);
    }

    public void fill(byte[] biomes) {
        for (int x = 0; x < this.size.getX(); x++) {
            for (int y = 0; y < this.size.getY(); y++) {
                BiomeType type = this.biomes[x + y * this.size.getX()];
                if (type instanceof VirtualBiomeType) {
                    type = ((VirtualBiomeType) type).getPersistedType();
                }
                biomes[x + y * this.size.getX()] = (byte) Biome.getIdForBiome((Biome) type);
            }
        }
    }

    @Override
    public MutableBiomeArea getBiomeView(Vector2i newMin, Vector2i newMax) {
        checkRange(newMin.getX(), newMin.getY());
        checkRange(newMax.getX(), newMax.getY());
        return new MutableBiomeViewDownsize(this, newMin, newMax);
    }

    @Override
    public MutableBiomeArea getBiomeView(DiscreteTransform2 transform) {
        return new MutableBiomeViewTransform(this, transform);
    }

    @Override
    public MutableBiomeAreaWorker<? extends MutableBiomeArea> getBiomeWorker() {
        return new SpongeMutableBiomeAreaWorker<>(this);
    }

    @Override
    public UnmodifiableBiomeArea getUnmodifiableBiomeView() {
        return new UnmodifiableBiomeAreaWrapper(this);
    }

    @Override
    public MutableBiomeArea getBiomeCopy(StorageType type) {
        switch (type) {
            case STANDARD:
                return new VirtualMutableBiomeBuffer(this.biomes.clone(), this.start, this.size);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBiomeArea getImmutableBiomeCopy() {
        return new VirtualImmutableBiomeBuffer(this.biomes, this.start, this.size);
    }

}
