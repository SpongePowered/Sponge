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

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.extent.BiomeVolume;
import org.spongepowered.common.util.VecHelper;

import java.util.Objects;

/**
 * Base class for biome areas. This class provides methods for retrieving the
 * size and for range checking.
 */
public abstract class AbstractBiomeBuffer implements BiomeVolume {

    protected Vector3i start;
    protected Vector3i size;
    protected Vector3i end;
    private final int xLine;

    protected AbstractBiomeBuffer(Vector3i start, Vector3i size) {
        checkArgument(size.getY() == 1, "Size y coordinate should be 1");

        this.start = start;
        this.size = size;
        this.end = this.start.add(this.size).sub(Vector3i.ONE);

        this.xLine = size.getX();
    }

    protected final void checkRange(int x, int y, int z) {
        if (!VecHelper.inBounds(x, y, z, this.start, this.end)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.start, this.end);
        }
    }

    protected int getIndex(int x, int z) {
        return (z - this.start.getZ()) * this.xLine + (x - this.start.getX());
    }

    @Override
    public Vector3i getBiomeMin() {
        return this.start;
    }

    @Override
    public Vector3i getBiomeMax() {
        return this.end;
    }

    @Override
    public Vector3i getBiomeSize() {
        return this.size;
    }

    @Override
    public boolean containsBiome(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.start, this.end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractBiomeBuffer that = (AbstractBiomeBuffer) o;
        return this.xLine == that.xLine &&
               this.start.equals(that.start) &&
               this.size.equals(that.size) &&
               this.end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.start, this.size, this.end, this.xLine);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("min", this.getBiomeMin())
            .add("max", this.getBiomeMax())
            .toString();
    }

}
