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

import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.block.ArrayMutableBlockBuffer;
import org.spongepowered.math.vector.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class MutableMapBlockEntityArchetypeBuffer extends AbstractMutableBlockEntityArchetypeBuffer<MutableMapBlockEntityArchetypeBuffer> {

    private final Map<Vector3i, BlockEntityArchetype> blockEntities;

    public MutableMapBlockEntityArchetypeBuffer(final Vector3i start, final Vector3i size) {
        super(start, size);
        this.blockEntities = new HashMap<>();
    }

    public MutableMapBlockEntityArchetypeBuffer(final ArrayMutableBlockBuffer buffer) {
        super(buffer);
        this.blockEntities = new HashMap<>();
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntityArchetype archetype) {
        this.checkRange(x, y, z);
        this.blockEntities.put(new Vector3i(x, y, z), Objects.requireNonNull(archetype, "Archetype cannot be null"));
    }

    @Override
    public void addBlockEntity(final Vector3i pos, final BlockEntity blockEntity) {
        this.checkRange(pos.x(), pos.y(), pos.z());
        this.blockEntities.put(Objects.requireNonNull(pos, "Position cannot be null"), Objects.requireNonNull(blockEntity, "BlockEntity cannot be null").createArchetype());
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntity blockEntity) {
        this.checkRange(x, y, z);
        this.blockEntities.put(new Vector3i(x, y, z), Objects.requireNonNull(blockEntity, "BlockEntity cannot be null").createArchetype());
    }

    @Override
    public void addBlockEntity(final Vector3i pos, final BlockEntityArchetype archetype) {
        this.checkRange(pos.x(), pos.y(), pos.z());
        this.blockEntities.put(Objects.requireNonNull(pos, "Position cannot be null"), Objects.requireNonNull(archetype, "Archetype cannot be null"));
    }

    @Override
    public void removeBlockEntity(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        this.blockEntities.remove(new Vector3i(x, y, z));
    }

    @Override
    public Optional<BlockEntityArchetype> blockEntityArchetype(final int x, final int y, final int z) {
        return Optional.ofNullable(this.blockEntities.get(new Vector3i(x, y, z)));
    }

    @Override
    public Map<Vector3i, BlockEntityArchetype> blockEntityArchetypes() {
        return this.blockEntities;
    }

    @Override
    public VolumeStream<MutableMapBlockEntityArchetypeBuffer, BlockEntityArchetype> blockEntityArchetypeStream(final Vector3i min,
        final Vector3i max, final StreamOptions options
    ) {
        VolumeStreamUtils.validateStreamArgs(min, max, this.blockMin(), this.blockMax(), options);
        final Stream<Map.Entry<Vector3i, BlockEntityArchetype>> entryStream;
        if (options.carbonCopy()) {
            final Map<Vector3i, BlockEntityArchetype> copy = new HashMap<>();
            this.blockEntities.forEach((vector3i, archetype) -> copy.put(vector3i, archetype.copy()));
            entryStream = copy.entrySet().stream();
        } else {
            entryStream = this.blockEntities.entrySet().stream();
        }
        final Stream<VolumeElement<MutableMapBlockEntityArchetypeBuffer, BlockEntityArchetype>> volumeElementStream = entryStream
            .filter(VolumeStreamUtils.blockEntityArchetypePositionFilter(min, max))
            .map(entry -> VolumeElement.of(this, entry.getValue(), entry.getKey()));

        return new SpongeVolumeStream<>(volumeElementStream, () -> this);
    }
}
