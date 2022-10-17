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
package org.spongepowered.common.entity;

import net.minecraft.nbt.CompoundTag;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.data.holder.SimpleNBTDataHolder;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DataUtil;
import org.spongepowered.math.vector.Vector3d;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SpongeEntitySnapshotBuilder extends AbstractDataBuilder<EntitySnapshot> implements EntitySnapshot.Builder {

    ResourceKey worldKey;
    Vector3d position;
    Vector3d rotation;
    Vector3d scale;
    EntityType<?> entityType;

    @Nullable UUID uniqueId;
    DataManipulator.@Nullable Mutable manipulator;
    @Nullable CompoundTag compound;
    @Nullable WeakReference<Entity> entityReference;

    public SpongeEntitySnapshotBuilder() {
        super(EntitySnapshot.class, 1);
    }

    @Override
    public SpongeEntitySnapshotBuilder world(final ServerWorldProperties worldProperties) {
        this.worldKey = Objects.requireNonNull(worldProperties).key();
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder type(final EntityType<?> entityType) {
        this.entityType = Objects.requireNonNull(entityType);
        this.manipulator = null;
        this.compound = null;
        this.uniqueId = null;
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder position(final Vector3d position) {
        this.position = Objects.requireNonNull(position);
        return this;
    }

    public SpongeEntitySnapshotBuilder rotation(final Vector3d rotation) {
        this.rotation = Objects.requireNonNull(rotation);
        return this;
    }

    public SpongeEntitySnapshotBuilder scale(final Vector3d scale) {
        this.scale = Objects.requireNonNull(scale);
        return this;
    }

    public SpongeEntitySnapshotBuilder id(final UUID entityId) {
        this.uniqueId = Objects.requireNonNull(entityId);
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder from(final Entity entity) {
        this.reset();
        this.entityReference = new WeakReference<>(entity);
        this.worldKey = entity.serverLocation().worldKey();
        this.position = entity.transform().position();
        this.rotation = entity.transform().rotation();
        this.scale = entity.transform().scale();
        this.entityType = entity.type();
        this.uniqueId = entity.uniqueId();
        this.manipulator = ((SpongeDataHolderBridge) entity).bridge$getManipulator().copy();
        this.compound = new CompoundTag();
        ((net.minecraft.world.entity.Entity) entity).saveWithoutId(this.compound);
        return this;
    }

    @Override
    public <V> EntitySnapshot.Builder add(final Key<? extends Value<V>> key, final V value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(this.entityType);

        if (this.manipulator == null) {
            this.manipulator = DataManipulator.mutableOf();
        }
        this.manipulator.set(key, value);
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder from(final EntitySnapshot holder) {
        this.reset();
        this.entityType = holder.type();
        this.worldKey = holder.world();
        if (holder.uniqueId().isPresent()) {
            this.uniqueId = holder.uniqueId().get();
        }
        this.position = holder.position().toDouble();
        final Optional<Transform> optional = holder.transform();
        if (optional.isPresent()) {
            this.position = optional.get().position();
            this.rotation = optional.get().rotation();
            this.scale = optional.get().scale();
        }
        this.manipulator = DataManipulator.mutableOf(holder);
        if (holder instanceof SpongeEntitySnapshot) {
            this.compound = ((SpongeEntitySnapshot) holder).getCompound().orElse(null);
        }
        return this;
    }

    public SpongeEntitySnapshotBuilder from(final net.minecraft.world.entity.Entity minecraftEntity) {
        this.entityType = ((Entity) minecraftEntity).type();
        this.worldKey = ((Entity) minecraftEntity).serverLocation().worldKey();
        this.uniqueId = minecraftEntity.getUUID();
        final Transform transform = ((Entity) minecraftEntity).transform();
        this.position = transform.position();
        this.rotation = transform.rotation();
        this.scale = transform.scale();
        this.manipulator = DataManipulator.mutableOf((Entity) minecraftEntity);
        this.compound = new CompoundTag();
        minecraftEntity.saveWithoutId(this.compound);
        return this;
    }

    public SpongeEntitySnapshotBuilder unsafeCompound(final CompoundTag compound) {
        this.compound = Objects.requireNonNull(compound).copy();
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder reset() {
        this.worldKey = null;
        this.uniqueId = null;
        this.position = null;
        this.rotation = null;
        this.scale = null;
        this.entityType = null;
        this.manipulator = null;
        this.compound = null;
        this.entityReference = null;
        return this;
    }

    @Override
    public EntitySnapshot build() {
        Objects.requireNonNull(this.worldKey);
        Objects.requireNonNull(this.position);
        Objects.requireNonNull(this.rotation);
        Objects.requireNonNull(this.scale);
        Objects.requireNonNull(this.entityType);

        final EntitySnapshot snapshot = new SpongeEntitySnapshot(this);

        // Write the the manipulator values to NBT
        if (this.manipulator != null && !this.manipulator.getKeys().isEmpty()) {
            if (this.compound == null) {
                this.compound = new CompoundTag();
            }

            final SimpleNBTDataHolder dataHolder = new SimpleNBTDataHolder(this.compound, NBTDataTypes.ENTITY);
            dataHolder.copyFrom(this.manipulator);
            this.compound = dataHolder.data$getCompound();

            // If there was no data remove the compound again
            if (this.compound.isEmpty()) {
                this.compound = null;
            }
        }

        return snapshot;
    }

    @Override
    protected Optional<EntitySnapshot> buildContent(final DataView container) throws InvalidDataException {
        if (!container.contains(Queries.WORLD_KEY, Constants.Entity.TYPE, Constants.Entity.ROTATION, Constants.Entity.SCALE, Constants.Sponge.SNAPSHOT_WORLD_POSITION)) {
            return Optional.empty();
        }
        this.worldKey = ResourceKey.resolve(container.getString(Queries.WORLD_KEY).get());
        this.position = DataUtil.getPosition3d(container, Constants.Sponge.SNAPSHOT_WORLD_POSITION);
        this.rotation = DataUtil.getPosition3d(container, Constants.Entity.ROTATION);
        this.scale = DataUtil.getPosition3d(container, Constants.Entity.SCALE);
        final String entityTypeId = container.getString(Constants.Entity.TYPE).get();
        this.entityType = Sponge.game().registry(RegistryTypes.ENTITY_TYPE).value(ResourceKey.resolve(entityTypeId));
        this.manipulator = null; // lazy read from nbt
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.compound = NBTTranslator.INSTANCE.translate(container.getView(Constants.Sponge.UNSAFE_NBT).get());
        }
        if (container.contains(Constants.Entity.UUID)) {
            this.uniqueId = UUID.fromString(container.getString(Constants.Entity.UUID).get());
        }
        return Optional.of(this.build());
    }
}
