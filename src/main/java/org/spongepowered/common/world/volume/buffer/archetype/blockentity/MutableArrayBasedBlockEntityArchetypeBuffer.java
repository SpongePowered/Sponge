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
package org.spongepowered.common.world.volume.buffer.archetype.blockentity;

import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.world.volume.archetype.block.entity.BlockEntityArchetypeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.volume.buffer.block.ArrayMutableBlockBuffer;
import org.spongepowered.math.vector.Vector3i;

import java.util.Map;
import java.util.Optional;

public class MutableArrayBasedBlockEntityArchetypeBuffer extends AbstractMutableBlockEntityArchetypeBuffer {

    protected MutableArrayBasedBlockEntityArchetypeBuffer(Vector3i start, Vector3i size) {
        super(start, size);
    }

    protected MutableArrayBasedBlockEntityArchetypeBuffer(
        ArrayMutableBlockBuffer buffer
    ) {
        super(buffer);
    }

    @Override
    public void addBlockEntity(int x, int y, int z, BlockEntityArchetype archetype) {

    }

    @Override
    public void removeBlockEntity(int x, int y, int z) {

    }

    @Override
    public Optional<BlockEntityArchetype> blockEntityArchetype(int x, int y, int z) {
        return Optional.empty();
    }

    @Override
    public Map<Vector3i, BlockEntityArchetype> blockEntityArchetypes() {
        return null;
    }

    @Override
    public VolumeStream<BlockEntityArchetypeVolume.Mutable, BlockEntityArchetype> blockEntityArchetypeStream(Vector3i min,
        Vector3i max, StreamOptions options
    ) {
        return null;
    }
}
