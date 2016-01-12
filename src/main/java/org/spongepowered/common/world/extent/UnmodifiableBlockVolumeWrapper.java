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

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.BlockVolumeWorker;
import org.spongepowered.common.world.extent.worker.SpongeBlockVolumeWorker;

public class UnmodifiableBlockVolumeWrapper implements UnmodifiableBlockVolume {

    private final MutableBlockVolume volume;

    public UnmodifiableBlockVolumeWrapper(MutableBlockVolume volume) {
        this.volume = volume;
    }

    @Override
    public Vector3i getBlockMin() {
        return this.volume.getBlockMin();
    }

    @Override
    public Vector3i getBlockMax() {
        return this.volume.getBlockMax();
    }

    @Override
    public Vector3i getBlockSize() {
        return this.volume.getBlockSize();
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return this.volume.containsBlock(x, y, z);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        return this.volume.getBlockType(x, y, z);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return this.volume.getBlock(x, y, z);
    }

    @Override
    public UnmodifiableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        return new UnmodifiableBlockVolumeWrapper(this.volume.getBlockView(newMin, newMax));
    }

    @Override
    public UnmodifiableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return new UnmodifiableBlockVolumeWrapper(this.volume.getBlockView(transform));
    }

    @Override
    public BlockVolumeWorker<? extends UnmodifiableBlockVolume> getBlockWorker() {
        return new SpongeBlockVolumeWorker<>(this);
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        return this.volume.getBlockCopy(type);
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return this.volume.getImmutableBlockCopy();
    }

}
