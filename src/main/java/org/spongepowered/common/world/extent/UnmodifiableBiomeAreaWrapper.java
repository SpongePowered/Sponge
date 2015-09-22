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
package org.spongepowered.common.world.extent;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.util.DiscreteTransform2;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBiomeArea;

public class UnmodifiableBiomeAreaWrapper implements UnmodifiableBiomeArea {

    private final MutableBiomeArea area;

    public UnmodifiableBiomeAreaWrapper(MutableBiomeArea area) {
        this.area = area;
    }

    @Override
    public Vector2i getBiomeMin() {
        return this.area.getBiomeMin();
    }

    @Override
    public Vector2i getBiomeMax() {
        return this.area.getBiomeMax();
    }

    @Override
    public Vector2i getBiomeSize() {
        return this.area.getBiomeSize();
    }

    @Override
    public boolean containsBiome(Vector2i position) {
        return this.area.containsBiome(position);
    }

    @Override
    public boolean containsBiome(int x, int z) {
        return this.area.containsBiome(x, z);
    }

    @Override
    public BiomeType getBiome(Vector2i position) {
        return this.area.getBiome(position);
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        return this.area.getBiome(x, z);
    }

    @Override
    public UnmodifiableBiomeArea getBiomeView(Vector2i newMin, Vector2i newMax) {
        return new UnmodifiableBiomeAreaWrapper(this.area.getBiomeView(newMin, newMax));
    }

    @Override
    public UnmodifiableBiomeArea getBiomeView(DiscreteTransform2 transform) {
        return new UnmodifiableBiomeAreaWrapper(this.area.getBiomeView(transform));
    }

    @Override
    public UnmodifiableBiomeArea getRelativeBiomeView() {
        return new UnmodifiableBiomeAreaWrapper(this.area.getRelativeBiomeView());
    }

    @Override
    public UnmodifiableBiomeArea getUnmodifiableBiomeView() {
        return this;
    }

    @Override
    public MutableBiomeArea getBiomeCopy() {
        return this.area.getBiomeCopy();
    }

    @Override
    public MutableBiomeArea getBiomeCopy(StorageType type) {
        return this.area.getBiomeCopy(type);
    }

    @Override
    public ImmutableBiomeArea getImmutableBiomeCopy() {
        return this.area.getImmutableBiomeCopy();
    }
}
