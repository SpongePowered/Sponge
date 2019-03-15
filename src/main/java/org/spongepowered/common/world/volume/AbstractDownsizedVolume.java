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

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.common.util.VecHelper;

public abstract class AbstractDownsizedVolume<V extends Volume> implements Volume {

    protected final V volume;
    protected final Vector3i min;
    protected final Vector3i max;
    protected final Vector3i size;

    public AbstractDownsizedVolume(V volume, Vector3i min, Vector3i max) {
        this.volume = volume;
        this.min = min;
        this.max = max;
        this.size = max.sub(min).add(Vector3i.ONE);
    }

    protected final void checkRange(int x, int y, int z) {
        if (!VecHelper.inBounds(x, y, z, this.min, this.max)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.min, this.max);
        }
    }

    @Override
    public final Vector3i getBlockMin() {
        return this.min;
    }

    @Override
    public final Vector3i getBlockMax() {
        return this.max;
    }

    @Override
    public final Vector3i getBlockSize() {
        return this.size;
    }

    @Override
    public final boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.min, this.max);
    }

    /**
     * {@inheritDoc}
     *
     * Allows for further overridding in the event we are processing on an area
     * that uses chunks.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    @Override
    public boolean isAreaAvailable(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.min, this.max);
    }

}
