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
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBiomeArea;
import org.spongepowered.api.world.extent.worker.MutableBiomeAreaWorker;
import org.spongepowered.common.world.extent.MutableBiomeViewDownsize;
import org.spongepowered.common.world.extent.MutableBiomeViewTransform;
import org.spongepowered.common.world.extent.UnmodifiableBiomeAreaWrapper;
import org.spongepowered.common.world.extent.worker.SpongeMutableBiomeAreaWorker;

/**
 * Mutable view of a {@link Biome} array.
 *
 * <p>Normally, the {@link ByteArrayMutableBiomeBuffer} class uses memory more
 * efficiently, but when the {@link Biome} array is already created (for
 * example for a contract specified by Minecraft) this implementation becomes
 * more efficient.</p>
 */
public final class ObjectArrayMutableBiomeBuffer extends AbstractBiomeBuffer implements MutableBiomeArea {

    private final Biome[] biomes;

    /**
     * Creates a new instance.
     *
     * @param biomes The biome array. The array is not copied, so changes made
     * by this object will write through.
     * @param start The start position
     * @param size The size
     */
    public ObjectArrayMutableBiomeBuffer(Biome[] biomes, Vector2i start, Vector2i size) {
        super(start, size);
        this.biomes = biomes;
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkRange(x, z);
        return (BiomeType) this.biomes[getIndex(x, z)];
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        checkNotNull(biome, "biome");
        checkRange(x, z);
        this.biomes[getIndex(x, z)] = (Biome) biome;
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
                return new ObjectArrayMutableBiomeBuffer(this.biomes.clone(), this.start, this.size);
            case THREAD_SAFE:
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }

    @Override
    public ImmutableBiomeArea getImmutableBiomeCopy() {
        return new ObjectArrayImmutableBiomeBuffer(this.biomes, this.start, this.size);
    }

}
