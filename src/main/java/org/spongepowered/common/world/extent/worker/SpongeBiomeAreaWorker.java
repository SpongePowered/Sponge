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
package org.spongepowered.common.world.extent.worker;

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.BiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.UnmodifiableBiomeArea;
import org.spongepowered.api.world.extent.worker.BiomeAreaWorker;
import org.spongepowered.api.world.extent.worker.procedure.BiomeAreaMapper;
import org.spongepowered.api.world.extent.worker.procedure.BiomeAreaMerger;
import org.spongepowered.api.world.extent.worker.procedure.BiomeAreaReducer;
import org.spongepowered.api.world.extent.worker.procedure.BiomeAreaVisitor;

import java.util.function.BiFunction;

/**
 *
 */
public class SpongeBiomeAreaWorker<A extends BiomeArea> implements BiomeAreaWorker<A> {

    protected final A area;

    public SpongeBiomeAreaWorker(A area) {
        this.area = area;
    }

    @Override
    public A getArea() {
        return this.area;
    }

    @Override
    public void map(BiomeAreaMapper mapper, MutableBiomeArea destination) {
        final Vector2i offset = align(destination);
        final int xOffset = offset.getX();
        final int zOffset = offset.getY();
        final UnmodifiableBiomeArea unmodifiableArea = this.area.getUnmodifiableBiomeView();
        final int xMin = unmodifiableArea.getBiomeMin().getX();
        final int zMin = unmodifiableArea.getBiomeMin().getY();
        final int xMax = unmodifiableArea.getBiomeMax().getX();
        final int zMax = unmodifiableArea.getBiomeMax().getY();
        for (int z = zMin; z <= zMax; z++) {
            for (int x = xMin; x <= xMax; x++) {
                final BiomeType biome = mapper.map(unmodifiableArea, x, z);
                destination.setBiome(x + xOffset, z + zOffset, biome);
            }
        }
    }

    @Override
    public void merge(BiomeArea second, BiomeAreaMerger merger, MutableBiomeArea destination) {
        final Vector2i offsetSecond = align(second);
        final int xOffsetSecond = offsetSecond.getX();
        final int zOffsetSecond = offsetSecond.getY();
        final Vector2i offsetDestination = align(destination);
        final int xOffsetDestination = offsetDestination.getX();
        final int zOffsetDestination = offsetDestination.getY();
        final UnmodifiableBiomeArea firstUnmodifiableArea = this.area.getUnmodifiableBiomeView();
        final int xMin = firstUnmodifiableArea.getBiomeMin().getX();
        final int zMin = firstUnmodifiableArea.getBiomeMin().getY();
        final int xMax = firstUnmodifiableArea.getBiomeMax().getX();
        final int zMax = firstUnmodifiableArea.getBiomeMax().getY();
        final UnmodifiableBiomeArea secondUnmodifiableArea = second.getUnmodifiableBiomeView();
        for (int z = zMin; z <= zMax; z++) {
            for (int x = xMin; x <= xMax; x++) {
                final BiomeType biome = merger.merge(firstUnmodifiableArea, x, z, secondUnmodifiableArea, x + xOffsetSecond, z + zOffsetSecond);
                destination.setBiome(x + xOffsetDestination, z + zOffsetDestination, biome);
            }
        }
    }

    @Override
    public void iterate(BiomeAreaVisitor<A> visitor) {
        final int xMin = this.area.getBiomeMin().getX();
        final int zMin = this.area.getBiomeMin().getY();
        final int xMax = this.area.getBiomeMax().getX();
        final int zMax = this.area.getBiomeMax().getY();
        for (int z = zMin; z <= zMax; z++) {
            for (int x = xMin; x <= xMax; x++) {
                visitor.visit(this.area, x, z);
            }
        }
    }

    @Override
    public <T> T reduce(BiomeAreaReducer<T> reducer, BiFunction<T, T, T> merge, T identity) {
        final UnmodifiableBiomeArea unmodifiableArea = this.area.getUnmodifiableBiomeView();
        final int xMin = unmodifiableArea.getBiomeMin().getX();
        final int zMin = unmodifiableArea.getBiomeMin().getY();
        final int xMax = unmodifiableArea.getBiomeMax().getX();
        final int zMax = unmodifiableArea.getBiomeMax().getY();
        T reduction = identity;
        for (int z = zMin; z <= zMax; z++) {
            for (int x = xMin; x <= xMax; x++) {
                reduction = reducer.reduce(unmodifiableArea, x, z, reduction);
            }
        }
        return reduction;
    }

    private Vector2i align(BiomeArea other) {
        final Vector2i thisSize = this.area.getBiomeSize();
        final Vector2i otherSize = other.getBiomeSize();
        checkArgument(otherSize.getX() >= thisSize.getX() && otherSize.getY() >= thisSize.getY(), "Other area is smaller than work area");
        return other.getBiomeMin().sub(this.area.getBiomeMin());
    }

}
