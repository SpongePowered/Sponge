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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpongeEntitySnapshotBuilder extends AbstractDataBuilder<EntitySnapshot> implements EntitySnapshot.Builder {

    UUID worldId;
    Vector3d position;
    Vector3d rotation;
    Vector3d scale;
    EntityType entityType;

    @Nullable UUID entityId;
    @Nullable List<Immutable<?, ?>> manipulators;
    @Nullable CompoundNBT compound;
    @Nullable List<org.spongepowered.api.data.value.Value.Immutable<?>> values;
    @Nullable WeakReference<Entity> entityReference;

    public SpongeEntitySnapshotBuilder() {
        super(EntitySnapshot.class, 1);
    }

    @Override
    public SpongeEntitySnapshotBuilder world(WorldProperties worldProperties) {
        this.worldId = Preconditions.checkNotNull(worldProperties).getUniqueId();
        return this;
    }

    public SpongeEntitySnapshotBuilder worldId(UUID worldUuid) {
        this.worldId = Preconditions.checkNotNull(worldUuid);
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder type(EntityType entityType) {
        this.entityType = Preconditions.checkNotNull(entityType);
        this.compound = null;
        this.manipulators = null;
        this.entityId = null;
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder position(Vector3d position) {
        this.position = Preconditions.checkNotNull(position);
        return this;
    }

    public SpongeEntitySnapshotBuilder rotation(Vector3d rotation) {
        this.rotation = Preconditions.checkNotNull(rotation);
        return this;
    }

    public SpongeEntitySnapshotBuilder scale(Vector3d scale) {
        this.scale = Preconditions.checkNotNull(scale);
        return this;
    }

    public SpongeEntitySnapshotBuilder id(UUID entityId) {
        this.entityId = Preconditions.checkNotNull(entityId);
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder from(Entity entity) {
        this.reset();
        this.entityReference = new WeakReference<>(entity);
        this.worldId = entity.getWorld().getUniqueId();
        this.position = entity.getTransform().getPosition();
        this.rotation = entity.getTransform().getRotation();
        this.scale = entity.getTransform().getScale();
        this.entityType = entity.getType();
        this.entityId = entity.getUniqueId();
        this.manipulators = Lists.newArrayList();
        for (Mutable<?, ?> manipulator : ((CustomDataHolderBridge) entity).bridge$getCustomManipulators()) {
            this.addManipulator(manipulator.asImmutable());
        }
        this.compound = new CompoundNBT();
        ((net.minecraft.entity.Entity) entity).writeWithoutTypeId(this.compound);
        return this;
    }


    @Override
    public <V> EntitySnapshot.Builder add(Key<? extends Value<V>> key, V value) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkState(this.entityType != null, "Must have a valid entity type before applying data!");
        if (this.values == null) {
            this.values = Lists.newArrayList();
        }
        this.values.add(new ImmutableSpongeValue<>(key, value));
        return this;
    }

    private void addManipulator(Immutable<?, ?> manipulator) {
        if (this.manipulators == null) {
            this.manipulators = Lists.newArrayList();
        }
        int replaceIndex = -1;
        for (Immutable<?, ?> existing : this.manipulators) {
            replaceIndex++;
            if (existing.getClass().equals(manipulator.getClass())) {
                break;
            }
        }
        if (replaceIndex != -1) {
            this.manipulators.remove(replaceIndex);
        }
        this.manipulators.add(manipulator);
    }

    @Override
    public SpongeEntitySnapshotBuilder from(EntitySnapshot holder) {
        this.entityType = holder.getType();
        this.worldId = holder.getWorldUniqueId();
        if (holder.getUniqueId().isPresent()) {
            this.entityId = holder.getUniqueId().get();
        }
        this.position = holder.getPosition().toDouble();
        final Optional<Transform> optional = holder.getTransform();
        if (optional.isPresent()) {
            this.position = optional.get().getPosition();
            this.rotation = optional.get().getRotation();
            this.scale = optional.get().getScale();
        }
        this.manipulators = Lists.newArrayList();
        for (Immutable<?, ?> manipulator : holder.getContainers()) {
            this.add(manipulator);
        }
        if (holder instanceof SpongeEntitySnapshot) {
            this.compound = ((SpongeEntitySnapshot) holder).getCompound().orElse(null);
        }
        return this;
    }

    public SpongeEntitySnapshotBuilder from(net.minecraft.entity.Entity minecraftEntity) {
        this.entityType = ((Entity) minecraftEntity).getType();
        this.worldId = ((Entity) minecraftEntity).getWorld().getUniqueId();
        this.entityId = minecraftEntity.getUniqueID();
        final Transform transform = ((Entity) minecraftEntity).getTransform();
        this.position = transform.getPosition();
        this.rotation = transform.getRotation();
        this.scale = transform.getScale();
        this.manipulators = Lists.newArrayList();
        for (Mutable<?, ?> manipulator : ((CustomDataHolderBridge) minecraftEntity).bridge$getCustomManipulators()) {
            this.addManipulator(manipulator.asImmutable());
        }
        this.compound = new CompoundNBT();
        minecraftEntity.writeWithoutTypeId(this.compound);
        return this;
    }

    public SpongeEntitySnapshotBuilder unsafeCompound(CompoundNBT compound) {
        this.compound = Preconditions.checkNotNull(compound).copy();
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder reset() {
        this.worldId = null;
        this.entityId = null;
        this.position = null;
        this.rotation = null;
        this.scale = null;
        this.entityType = null;
        this.entityId = null;
        this.manipulators = null;
        this.compound = null;
        this.entityReference = null;
        return this;
    }

    @Override
    public EntitySnapshot build() {
        Preconditions.checkNotNull(this.worldId);
        Preconditions.checkNotNull(this.position);
        Preconditions.checkNotNull(this.rotation);
        Preconditions.checkNotNull(this.scale);
        Preconditions.checkNotNull(this.entityType);
        EntitySnapshot snapshot = new SpongeEntitySnapshot(this);
        if(this.values != null) {
            for (org.spongepowered.api.data.value.Value.Immutable<?> value : this.values) {
                snapshot = snapshot.with(value).orElse(snapshot);
            }
        }
        return snapshot;
    }

    @Override
    protected Optional<EntitySnapshot> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(Queries.WORLD_ID, Constants.Entity.TYPE, Constants.Entity.ROTATION, Constants.Entity.SCALE, Constants.Sponge.SNAPSHOT_WORLD_POSITION)) {
            return Optional.empty();
        }
        this.worldId = UUID.fromString(container.getString(Queries.WORLD_ID).get());
        this.position = DataUtil.getPosition3d(container, Constants.Sponge.SNAPSHOT_WORLD_POSITION);
        this.rotation = DataUtil.getPosition3d(container, Constants.Entity.ROTATION);
        this.scale = DataUtil.getPosition3d(container, Constants.Entity.SCALE);
        final String entityTypeId = container.getString(Constants.Entity.TYPE).get();
        this.entityType = SpongeCommon.getRegistry().getType(EntityType.class, entityTypeId).get();

        if (container.contains(Constants.Sponge.DATA_MANIPULATORS)) {
            this.manipulators = DataUtil.deserializeImmutableManipulatorList(container.getViewList(Constants.Sponge.DATA_MANIPULATORS).get());
        } else {
            this.manipulators = ImmutableList.of();
        }
        if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
            this.compound = NbtTranslator.getInstance().translateData(container.getView(Constants.Sponge.UNSAFE_NBT).get());
        }
        if (container.contains(Constants.Entity.UUID)) {
            this.entityId = UUID.fromString(container.getString(Constants.Entity.UUID).get());
        }
        return Optional.of(this.build());
    }
}
