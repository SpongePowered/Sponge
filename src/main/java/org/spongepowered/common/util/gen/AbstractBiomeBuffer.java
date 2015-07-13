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
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Objects;
import org.spongepowered.api.world.extent.BiomeArea;
import org.spongepowered.common.util.VecHelper;

/**
 * Base class for biome areas. This class provides methods for retrieving the
 * size and for range checking.
 */
public abstract class AbstractBiomeBuffer implements BiomeArea {

    protected Vector2i start;
    protected Vector2i size;
    protected Vector2i end;

    protected AbstractBiomeBuffer(Vector2i start, Vector2i size) {
        this.start = checkNotNull(start, "start");
        this.size = checkNotNull(size, "size");

        checkArgument(size.getX() > 0);
        checkArgument(size.getY() > 0);

        this.end = this.start.add(this.size).sub(Vector2i.ONE);
    }

    protected final void checkRange(int x, int z) {
        if (x < this.start.getX() || x > this.end.getX()
                || z < this.start.getY() || z > this.end.getY()) {
            throw new IndexOutOfBoundsException("Position (" + new Vector2i(x, z) + " out of bounds for " + this);
        }
    }

    @Override
    public Vector2i getBiomeMin() {
        return this.start;
    }

    @Override
    public Vector2i getBiomeMax() {
        return this.end;
    }

    @Override
    public Vector2i getBiomeSize() {
        return this.size;
    }

    @Override
    public boolean containsBiome(Vector2i position) {
        return containsBiome(position.getX(), position.getY());
    }

    @Override
    public boolean containsBiome(int x, int z) {
        return VecHelper.inBounds(x, z, this.start, this.end);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("min", this.getBiomeMin())
                .add("max", this.getBiomeMax())
                .toString();
    }
}
