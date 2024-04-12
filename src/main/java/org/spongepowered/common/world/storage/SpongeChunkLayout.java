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
package org.spongepowered.common.world.storage;

import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3i;

public final class SpongeChunkLayout implements ChunkLayout {

    public static final SpongeChunkLayout INSTANCE = new SpongeChunkLayout();

    private final Vector3i size;
    private final Vector3i mask;
    private final Vector3i spaceMin;
    private final Vector3i spaceMax;
    private final Vector3i spaceSize;

    public SpongeChunkLayout(final int minY, final int height) {
        this.size = new Vector3i(16, height, 16);
        this.mask = this.size.sub(1, 1, 1);
        this.spaceMax = new Vector3i(Constants.World.BLOCK_MAX.x(), minY + height, Constants.World.BLOCK_MAX.z()).sub(1, 1, 1);
        this.spaceMin = new Vector3i(Constants.World.BLOCK_MIN.x(), minY, Constants.World.BLOCK_MIN.z());
        this.spaceSize = this.spaceMax.sub(this.spaceMin).add(1, 1, 1);
    }

    private SpongeChunkLayout() {
        this(-64, 384);
    }

    public Vector3i getMask() {
        return this.mask;
    }

    @Override
    public Vector3i chunkSize() {
        return this.size;
    }

    @Override
    public Vector3i spaceMax() {
        return this.spaceMax;
    }

    @Override
    public Vector3i spaceMin() {
        return this.spaceMin;
    }

    @Override
    public Vector3i spaceSize() {
        return this.spaceSize;
    }

    @Override
    public Vector3i spaceOrigin() {
        return Vector3i.ZERO;
    }

    @Override
    public boolean isInChunk(final int x, final int y, final int z) {
        // no bits allowed outside the mask!
        return (x & ~this.mask.x()) == 0 && (y & ~this.mask.y()) == 0 && (z & ~this.mask.z()) == 0;
    }

    @Override
    public boolean isInChunk(final int wx, final int wy, final int wz, final int cx, final int cy, final int cz) {
        return this.isInChunk(wx - (cx << 4), wy - Math.floorDiv(cy - this.spaceMin.y(), this.size.y()), wz - (cz << 4));
    }

    @Override
    public Vector3i forceToChunk(final int x, final int y, final int z) {
        return new Vector3i(x >> 4, Math.floorDiv(y - this.spaceMin.y(), this.size.y()), z >> 4);
    }

    @Override
    public Vector3i forceToWorld(final int x, final int y, final int z) {
        return new Vector3i(x << 4, (y * this.size.y()) + this.spaceMin.y(), z << 4);
    }

}
