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
package org.spongepowered.common.world.volume.buffer.entity;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.volume.entity.EntityVolume;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ObjectArrayMutableEntityBuffer extends AbstractVolumeBuffer implements EntityVolume.Mutable {
    private final List<Entity> entities;

    public ObjectArrayMutableEntityBuffer(final Vector3i start, final Vector3i size) {
        super(start, size);
        this.entities = new ArrayList<>();
    }

    @Override
    public <E extends Entity> E createEntity(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException("Cannot create entities without a world, can only add to a volume");
    }

    @Override
    public <E extends Entity> E createEntityNaturally(final EntityType<E> type, final Vector3d position
    ) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException("Cannot create entities without a world, can only add to a volume");
    }

    @Override
    public Optional<Entity> createEntity(final DataContainer entityContainer) {
        return Optional.empty();
    }

    @Override
    public Optional<Entity> createEntity(final DataContainer entityContainer, final Vector3d position) {
        return Optional.empty();
    }

    @Override
    public boolean spawnEntity(final Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null!");
        }
        if (!this.contains(entity.position().toInt())) {
            throw new IllegalArgumentException(String.format("Entity is out of bounds! {min: %s, max: %s} does not contain %s", this.min(), this.max(), entity.position()));
        }
        return this.entities.add(entity);
    }

    @Override
    public Collection<Entity> spawnEntities(final Iterable<? extends Entity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
            .filter(this::spawnEntity)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends Player> players() {
        return this.entities.stream()
            .filter(entity -> entity instanceof Player)
            .map(entity -> (Player) entity)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Entity> entity(final UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null!");
        }
        return this.entities.stream()
            .filter(entity -> uuid.equals(entity.uniqueId()))
            .findFirst();
    }

    @Override
    public Collection<? extends Entity> entities() {
        return ImmutableList.copyOf(this.entities);
    }

    @Override
    public <T extends Entity> Collection<? extends T> entities(final Class<? extends T> entityClass, final AABB box,
        final @Nullable Predicate<? super T> predicate
    ) {
        Objects.requireNonNull(entityClass);
        Objects.requireNonNull(box);
        if (!this.contains(box.min().toInt())) {
            throw new IllegalArgumentException("Box is larger than volume allowed");
        }
        if (!this.contains(box.max().toInt())) {
            throw new IllegalArgumentException("Box is larger than volume allowed!");
        }
        Stream<T> tStream = this.entities.stream()
            .filter(entityClass::isInstance)
            .map(entity -> (T) entity)
            .filter(entity -> box.contains(entity.position()));
        if (predicate != null) {
            tStream = tStream.filter(predicate);
        }
        return tStream
            .collect(Collectors.toList());

    }

    @Override
    public Collection<? extends Entity> entities(final AABB box, final Predicate<? super Entity> filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(box, "Bounding box cannot be null");
        if (!this.contains(box.min().toInt())) {
            throw new IllegalArgumentException("Box is larger than volume allowed");
        }
        if (!this.contains(box.max().toInt())) {
            throw new IllegalArgumentException("Box is larger than volume allowed!");
        }
        return this.entities.stream()
            .filter(entity -> box.contains(entity.position()))
            .filter(filter)
            .collect(Collectors.toList());
    }

    @Override
    public VolumeStream<EntityVolume.Mutable, Entity> entityStream(final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        VolumeStreamUtils.validateStreamArgs(min, max, this.min(), this.max(), options);
        // Normally, we'd be able to shadow-copy, but we can't copy entities, and we're only using a list, so we can iterate only on the list.
        final Stream<VolumeElement<EntityVolume.Mutable, Entity>> backingStream = this.entities.stream()
            .map(entity -> VolumeElement.of(this, entity, entity.position()));
        return new SpongeVolumeStream<>(backingStream, () -> this);
    }

}
