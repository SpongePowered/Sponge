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
package org.spongepowered.common.world.level.chunk.storage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.chunk.EntityChunk;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.entity.ObjectArrayMutableEntityBuffer;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public final class SpongeEntityChunk implements EntityChunk {

    private final ServerLevel level;
    private final Vector3i chunkPosition;
    private final List<net.minecraft.world.entity.Entity> entities;

    private @MonotonicNonNull SpongeChunkLayout chunkLayout;
    private @MonotonicNonNull Vector3i blockMin;
    private @MonotonicNonNull Vector3i blockMax;

    public SpongeEntityChunk(final ServerLevel level, final Vector3i chunkPosition, final List<net.minecraft.world.entity.Entity> entities) {
        this.level = level;
        this.chunkPosition = chunkPosition;
        this.entities = entities;
    }

    @Override
    public Vector3i min() {
        if (this.blockMin == null) {
            if (this.chunkLayout == null) {
                this.chunkLayout = new SpongeChunkLayout(this.level.getMinBuildHeight(), this.level.getHeight());
            }
            this.blockMin = this.chunkLayout.forceToWorld(this.chunkPosition);
        }
        return this.blockMin;
    }

    @Override
    public Vector3i max() {
        if (this.blockMax == null) {
            if (this.chunkLayout == null) {
                this.chunkLayout = new SpongeChunkLayout(this.level.getMinBuildHeight(), this.level.getHeight());
            }
            this.blockMax = this.min().add(this.chunkLayout.chunkSize()).sub(1, 1, 1);
        }
        return this.blockMax;
    }

    @Override
    public boolean contains(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    @Override
    public boolean isAreaAvailable(final int x, final int y, final int z) {
        return VecHelper.inBounds(x, y, z, this.min(), this.max());
    }

    @Override
    public Collection<? extends Player> players() {
        return this.entities.stream().filter(Player.class::isInstance).map(Player.class::cast).toList();
    }

    @Override
    public Optional<Entity> entity(final UUID uuid) {
        return this.entities.stream().filter(e -> e.getUUID().equals(uuid)).map(Entity.class::cast).findFirst();
    }

    @Override
    public Collection<? extends Entity> entities() {
        return this.entities.stream().map(Entity.class::cast).toList();
    }

    @Override
    public <T extends Entity> Collection<? extends T> entities(final Class<? extends T> entityClass, final AABB box, final @Nullable Predicate<? super T> predicate) {
        final net.minecraft.world.phys.AABB mcAabb = VecHelper.toMinecraftAABB(box);
        return this.entities.stream()
                .filter(e -> entityClass.isInstance(e) && e.getBoundingBox().intersects(mcAabb))
                .map(entityClass::cast)
                .filter(e -> predicate.test(e))
                .toList();
    }

    @Override
    public Collection<? extends Entity> entities(final AABB box, final Predicate<? super Entity> filter) {
        final net.minecraft.world.phys.AABB mcAabb = VecHelper.toMinecraftAABB(box);
        return this.entities.stream().map(Entity.class::cast)
                .filter(e -> ((net.minecraft.world.entity.Entity) e).getBoundingBox().intersects(mcAabb) && filter.test(e))
                .toList();
    }

    @Override
    public VolumeStream<EntityChunk, Entity> entityStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
        VolumeStreamUtils.validateStreamArgs(
                Objects.requireNonNull(min, "min"), Objects.requireNonNull(max, "max"),
                Objects.requireNonNull(options, "options"));

        final boolean shouldCarbonCopy = options.carbonCopy();
        final Vector3i size = max.sub(min).add(1, 1 ,1);
        final @MonotonicNonNull ObjectArrayMutableEntityBuffer backingVolume;
        if (shouldCarbonCopy) {
            backingVolume = new ObjectArrayMutableEntityBuffer(min, size);
        } else {
            backingVolume = null;
        }

        return VolumeStreamUtils.generateStream(options,
                this,
                this,
                // Entity Accessor
                (chunk) -> chunk.entities.stream().filter(entity -> VecHelper.inBounds(entity.blockPosition(), min, max))
                        .map(entity -> new AbstractMap.SimpleEntry<>(entity.blockPosition(), entity)),
                // Entity Identity Function
                VolumeStreamUtils.getOrCloneEntityWithVolume(shouldCarbonCopy, backingVolume, this.level),
                (key, entity) -> entity.getUUID(),
                // Filtered Position Entity Accessor
                (entityUuid, chunk) -> {
                    final net.minecraft.world.entity.@Nullable Entity entity = shouldCarbonCopy
                            ? (net.minecraft.world.entity.Entity) backingVolume.entity(entityUuid).orElse(null)
                            : (net.minecraft.world.entity.Entity) chunk.entity(entityUuid).orElse(null);
                    if (entity == null) {
                        return null;
                    }
                    return new Tuple<>(entity.blockPosition(), entity);
                }
        );
    }

    @Override
    public <E extends Entity> E createEntity(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        this.checkPositionInChunk(position);
        return ((LevelBridge) this.level).bridge$createEntity(type, position, false);
    }

    @Override
    public <E extends Entity> E createEntityNaturally(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        this.checkPositionInChunk(position);
        return ((LevelBridge) this.level).bridge$createEntity(type, position, true);
    }

    @Override
    public Optional<Entity> createEntity(final DataContainer container) {
        return Optional.ofNullable(((LevelBridge) this.level).bridge$createEntity(container, null,
                position -> VecHelper.inBounds(position.toInt(), this.min(), this.max())));
    }

    @Override
    public Optional<Entity> createEntity(final DataContainer container, final Vector3d position) {
        this.checkPositionInChunk(position);
        return Optional.ofNullable(((LevelBridge) this.level).bridge$createEntity(container, position, null));
    }

    @Override
    public boolean spawnEntity(final Entity entity) {
        this.entities.add((net.minecraft.world.entity.Entity) entity);
        return true;
    }

    @Override
    public Collection<Entity> spawnEntities(final Iterable<? extends Entity> entities) {
        final List<Entity> list = new ArrayList<>();
        for (final Entity entity : entities) {
            this.spawnEntity(entity);
            list.add(entity);
        }
        return list;
    }

    @Override
    public void filterEntities(final Predicate<? super Entity> predicate) {
        this.entities.removeIf(e -> !predicate.test((Entity) e));
    }

    @Override
    public void filterEntities(final AABB box, final Predicate<? super Entity> predicate) {
        final net.minecraft.world.phys.AABB mcAabb = VecHelper.toMinecraftAABB(box);
        this.entities.removeIf(e -> e.getBoundingBox().intersects(mcAabb) && !predicate.test((Entity) e));
    }

    @Override
    public <T extends Entity> void filterEntities(final Class<? extends T> entityClass, final AABB box, final @Nullable Predicate<? super T> predicate) {
        final net.minecraft.world.phys.AABB mcAabb = VecHelper.toMinecraftAABB(box);
        this.entities.removeIf(e -> entityClass.isInstance(e) && e.getBoundingBox().intersects(mcAabb)
            && (predicate == null || !predicate.test(entityClass.cast(e))));
    }

    private void checkPositionInChunk(final Vector3d position) {
        if (!VecHelper.inBounds(position.toInt(), this.min(), this.max())) {
            throw new IllegalArgumentException("Supplied bounds are not within this chunk.");
        }
    }

    public List<net.minecraft.world.entity.Entity> build() {
        return Collections.unmodifiableList(this.entities);
    }
}
