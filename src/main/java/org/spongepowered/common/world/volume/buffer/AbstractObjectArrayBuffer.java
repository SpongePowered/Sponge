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
package org.spongepowered.common.world.volume.buffer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;
import java.util.StringJoiner;

public class AbstractObjectArrayBuffer implements Volume {

    protected final Vector3i start;
    protected final Vector3i size;
    protected final Vector3i end;
    private final int yLine;
    private final int yzSlice;

    public AbstractObjectArrayBuffer(final Vector3i start, final Vector3i size) {
        this.start = start;
        this.size = size;
        this.end = this.start.add(this.size).sub(Vector3i.ONE);

        this.yLine = size.getY();
        this.yzSlice = this.yLine * size.getZ();
    }

    protected void checkRange(final int x, final int y, final int z) {
        if (!VecHelper.inBounds(x, y, z, this.start, this.end)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.start, this.end);
        }
    }

    protected int getIndex(final int x, final int y, final int z) {
        return (x - this.start.getX()) * this.yzSlice + (z - this.start.getZ()) * this.yLine + (y - this.start.getY());
    }

    @Override
    public Vector3i getBlockMax() {
        return this.end;
    }

    @Override
    public Vector3i getBlockMin() {
        return this.start;
    }

    @Override
    public Vector3i getBlockSize() {
        return this.size;
    }

    @Override
    public boolean containsBlock(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.start, this.end);
    }

    @Override
    public boolean isAreaAvailable(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.start, this.end);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final AbstractObjectArrayBuffer that = (AbstractObjectArrayBuffer) o;
        return this.yLine == that.yLine &&
            this.yzSlice == that.yzSlice &&
            this.start.equals(that.start) &&
            this.size.equals(that.size) &&
            this.end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.start, this.size, this.end, this.yLine, this.yzSlice);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractObjectArrayBuffer.class.getSimpleName() + "[", "]")
            .add("start=" + this.start)
            .add("end=" + this.end)
            .toString();
    }
}
