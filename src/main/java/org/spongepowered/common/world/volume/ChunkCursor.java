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
package org.spongepowered.common.world.volume;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;
import java.util.StringJoiner;

final class ChunkCursor {
    final int chunkX;
    final int xOffset;
    final int chunkZ;
    final int zOffset;
    final int ySection;
    final int yOffset;

    ChunkCursor(final Vector3i pos) {
        this.chunkX = pos.getX() >> 4;
        this.xOffset = pos.getX() & 15;
        this.chunkZ = pos.getZ() >> 4;
        this.zOffset = pos.getZ() & 15;
        this.ySection = pos.getY() >> 4 << 4;
        this.yOffset = pos.getY() & 15;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ChunkCursor that = (ChunkCursor) o;
        return this.chunkX == that.chunkX
            && this.xOffset == that.xOffset
            && this.chunkZ == that.chunkZ
            && this.zOffset == that.zOffset
            && this.ySection == that.ySection
            && this.yOffset == that.yOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.chunkX, this.xOffset, this.chunkZ, this.zOffset, this.ySection, this.yOffset);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "ChunkCursor[", "]")
            .add("chunkX=" + this.chunkX)
            .add("xOffset=" + this.xOffset)
            .add("chunkZ=" + this.chunkZ)
            .add("zOffset=" + this.zOffset)
            .add("ySection=" + this.ySection)
            .add("yOffset=" + this.yOffset)
            .toString();
    }
}
