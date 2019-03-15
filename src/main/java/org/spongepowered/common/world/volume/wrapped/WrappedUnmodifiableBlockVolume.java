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
package org.spongepowered.common.world.volume.wrapped;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.volume.UnmodifiableVolume;
import org.spongepowered.api.world.volume.block.ImmutableBlockVolume;
import org.spongepowered.api.world.volume.block.ReadableBlockVolume;
import org.spongepowered.api.world.volume.block.UnmodifiableBlockVolume;
import org.spongepowered.api.world.volume.block.worker.BlockVolumeStream;

import java.lang.ref.WeakReference;

public class WrappedUnmodifiableBlockVolume<B extends ReadableBlockVolume> implements UnmodifiableBlockVolume<WrappedUnmodifiableBlockVolume<B>> {

    private final WeakReference<B> volumeRef;

    public WrappedUnmodifiableBlockVolume(B volumeRef) {
        this.volumeRef = new WeakReference<>(volumeRef);
    }

    private B getReferenceVolume() {
        return checkNotNull(this.volumeRef.get(), "Volume Reference lost!");
    }

    @Override
    public Vector3i getBlockMin() {
        return getReferenceVolume().getBlockMin();
    }

    @Override
    public Vector3i getBlockMax() {
        return getReferenceVolume().getBlockMax();
    }

    @Override
    public Vector3i getBlockSize() {
        return getReferenceVolume().getBlockSize();
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return getReferenceVolume().containsBlock(x, y, z);
    }

    @Override
    public boolean isAreaAvailable(int x, int y, int z) {
        return getReferenceVolume().isAreaAvailable(x, y, z);
    }

    @Override
    public UnmodifiableVolume getView(Vector3i newMin, Vector3i newMax) {
        return null;
    }

    @Override
    public BlockVolumeStream<WrappedUnmodifiableBlockVolume<B>, ?> toBlockStream() {
        return null;
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return null;
    }

    @Override
    public FluidState getFluid(int x, int y, int z) {
        return null;
    }

    @Override
    public ImmutableBlockVolume asImmutableBlockVolume() {
        return null;
    }

    @Override
    public int getHighestYAt(int x, int z) {
        return 0;
    }

    @Override
    public WrappedUnmodifiableBlockVolume<B> asUnmodifiableBlockVolume() {
        return this;
    }
}
