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
package org.spongepowered.common.world.volume.buffer.archetype.entity;

import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.AbstractVolumeBuffer;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectArrayMutableEntityArchetypeBuffer extends AbstractVolumeBuffer implements EntityArchetypeVolume.Mutable {

    private final ArrayList<Tuple<Vector3d, EntityArchetype>> entities;

    public ObjectArrayMutableEntityArchetypeBuffer(final Vector3i start, final Vector3i size) {
        super(start, size);
        this.entities = new ArrayList<>();
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes() {
        return this.entities.stream()
            .map(Tuple::second)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<EntityArchetypeEntry> entityArchetypesByPosition() {
        return this.entities.stream()
            .map(tuple -> EntityArchetypeEntry.of(tuple.second(), tuple.first()))
            .collect(Collectors.toList());
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes(final Predicate<EntityArchetype> filter) {
        return this.entities.stream()
            .map(Tuple::second)
            .filter(filter)
            .collect(Collectors.toList());
    }

    @Override
    public VolumeStream<EntityArchetypeVolume.Mutable, EntityArchetype> entityArchetypeStream(final Vector3i min, final Vector3i max,
        final StreamOptions options
    ) {
        VolumeStreamUtils.validateStreamArgs(min, max, this.blockMin(), this.blockMax(), options);
        final Stream<Tuple<Vector3d, EntityArchetype>> entryStream;
        if (options.carbonCopy()) {
            entryStream = new ArrayList<>(this.entities).stream();
        } else {
            entryStream = this.entities.stream();
        }
        final Stream<VolumeElement<EntityArchetypeVolume.Mutable, EntityArchetype>> archetypeStream = entryStream
            .filter(VolumeStreamUtils.entityArchetypePositionFilter(min, max))
            .map(tuple -> VolumeElement.of(this, tuple.second(), tuple.first()));
        return new SpongeVolumeStream<>(archetypeStream, () -> this);
    }

    @Override
    public Stream<EntityArchetypeEntry> entitiesByPosition() {
        return this.entities.stream()
            .map(tuple -> EntityArchetypeEntry.of(tuple.second(), tuple.first()));
    }

    @Override
    public void addEntity(final EntityArchetypeEntry entry) {
        if (!this.containsBlock(Objects.requireNonNull(entry, "EntityArchetype cannot be null").position().toInt())) {
            final String message = String.format(
                "EntityArchetype position is out of bounds: Found %s but is outside bounds (%s, %s)",
                entry.position(),
                this.blockMin(),
                this.blockMax()
            );
            throw new IllegalArgumentException(message);
        }
        this.entities.add(Tuple.of(entry.position(), entry.archetype()));
    }
}
