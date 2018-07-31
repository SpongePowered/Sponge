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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class SpongeEntitySnapshotBuilder extends AbstractDataBuilder<EntitySnapshot> implements EntitySnapshot.Builder {

    UUID worldId;
    Vector3d position;
    Vector3d rotation;
    Vector3d scale;
    EntityType entityType;

    @Nullable UUID entityId;
    @Nullable Set<ImmutableDataManipulator<?, ?>> customManipulators;
    @Nullable Set<ImmutableDataManipulator<?, ?>> vanillaManipulators;
    @Nullable NBTTagCompound compound;
    @Nullable Set<ImmutableValue<?>> values;
    @Nullable WeakReference<Entity> entityReference;

    public SpongeEntitySnapshotBuilder() {
        super(EntitySnapshot.class, 1);
    }

    @Override
    public SpongeEntitySnapshotBuilder world(WorldProperties worldProperties) {
        this.worldId = checkNotNull(worldProperties).getUniqueId();
        return this;
    }

    public SpongeEntitySnapshotBuilder worldId(UUID worldUuid) {
        this.worldId = checkNotNull(worldUuid);
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder type(EntityType entityType) {
        this.entityType = checkNotNull(entityType);
        this.compound = null;
        this.vanillaManipulators = null;
        this.customManipulators = null;
        this.entityId = null;
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder position(Vector3d position) {
        this.position = checkNotNull(position);
        return this;
    }

    public SpongeEntitySnapshotBuilder rotation(Vector3d rotation) {
        this.rotation = checkNotNull(rotation);
        return this;
    }

    public SpongeEntitySnapshotBuilder scale(Vector3d scale) {
        this.scale = checkNotNull(scale);
        return this;
    }

    public SpongeEntitySnapshotBuilder id(UUID entityId) {
        this.entityId = checkNotNull(entityId);
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder from(Entity entity) {
        reset();
        this.entityReference = new WeakReference<>(entity);
        this.worldId = entity.getWorld().getUniqueId();
        this.position = entity.getTransform().getPosition();
        this.rotation = entity.getTransform().getRotation();
        this.scale = entity.getTransform().getScale();
        this.entityType = entity.getType();
        this.entityId = entity.getUniqueId();
        this.vanillaManipulators = ((IMixinEntity) entity).getVanillaManipulators().stream().map(DataManipulator::asImmutable).collect(Collectors.toSet());
        this.customManipulators = ((IMixinCustomDataHolder) entity).getCustomManipulators().stream().map(DataManipulator::asImmutable).collect(Collectors.toSet());
        this.compound = new NBTTagCompound();
        ((net.minecraft.entity.Entity) entity).writeToNBT(this.compound);
        return this;
    }

    @Override
    public SpongeEntitySnapshotBuilder add(DataManipulator<?, ?> manipulator) {
        checkState(this.entityType != null, "Must have a valid entity type before applying data!");
        return add(manipulator.asImmutable());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SpongeEntitySnapshotBuilder add(ImmutableDataManipulator<?, ?> manipulator) {
        checkState(this.entityType != null, "Must have a valid entity type before applying data!");
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getImmutableProcessor((Class) manipulator.getClass());
        if (optional.isPresent() && optional.get().supports(this.entityType)) {
            addManipulator(manipulator);
        }
        return this;
    }

    @Override
    public <V> EntitySnapshot.Builder add(Key<? extends BaseValue<V>> key, V value) {
        checkNotNull(key, "key");
        checkState(this.entityType != null, "Must have a valid entity type before applying data!");
        if (this.values == null) {
            this.values = new HashSet<>();
        }
        this.values.add(new ImmutableSpongeValue<>(key, value));
        return this;
    }

    private void addManipulator(ImmutableDataManipulator<?, ?> manipulator) {
        if (this.customManipulators == null) {
            this.customManipulators = new HashSet<>();
        } else {
            this.customManipulators.remove(manipulator);
        }
        this.customManipulators.add(manipulator);
    }

    @Override
    public SpongeEntitySnapshotBuilder from(EntitySnapshot holder) {
        reset();
        this.entityType = holder.getType();
        this.worldId = holder.getWorldUniqueId();
        if (holder.getUniqueId().isPresent()) {
            this.entityId = holder.getUniqueId().get();
        }
        final Optional<Transform<World>> optional = holder.getTransform();
        if (optional.isPresent()) {
            this.position = optional.get().getPosition();
            this.rotation = optional.get().getRotation();
            this.scale = optional.get().getScale();
        } else {
            this.position = holder.getPosition().toDouble();
        }
        if (!holder.getContainers().isEmpty()) {
            if (this.customManipulators == null) {
                this.customManipulators = new HashSet<>();
            }
            this.customManipulators.addAll(holder.getContainers());
        }
        if (holder instanceof SpongeEntitySnapshot) {
            this.compound = ((SpongeEntitySnapshot) holder).getCompound().orElse(null);
            this.vanillaManipulators = ((SpongeEntitySnapshot) holder).getVanillaManipulators().orElse(null);
        }
        return this;
    }

    public SpongeEntitySnapshotBuilder from(net.minecraft.entity.Entity minecraftEntity) {
        return from(EntityUtil.fromNative(minecraftEntity));
    }

    public SpongeEntitySnapshotBuilder unsafeCompound(NBTTagCompound compound) {
        this.compound = checkNotNull(compound).copy();
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
        this.vanillaManipulators = null;
        this.customManipulators = null;
        this.compound = null;
        this.entityReference = null;
        return this;
    }

    @Override
    public EntitySnapshot build() {
        EntitySnapshot snapshot = new SpongeEntitySnapshot(this);
        if (this.values != null) {
            for (ImmutableValue<?> value : this.values) {
                snapshot = snapshot.with(value).orElse(snapshot);
            }
        }
        return snapshot;
    }

    @Override
    protected Optional<EntitySnapshot> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(Queries.WORLD_ID, DataQueries.ENTITY_TYPE, DataQueries.ENTITY_ROTATION, DataQueries.ENTITY_SCALE, DataQueries.SNAPSHOT_WORLD_POSITION)) {
            return Optional.empty();
        }
        this.worldId = UUID.fromString(container.getString(Queries.WORLD_ID).get());
        this.position = DataUtil.getPosition3d(container);
        this.rotation = DataUtil.getPosition3d(container, DataQueries.ENTITY_ROTATION);
        this.scale = DataUtil.getPosition3d(container, DataQueries.ENTITY_SCALE);
        final String entityTypeId = container.getString(DataQueries.ENTITY_TYPE).get();
        this.entityType = SpongeImpl.getRegistry().getType(EntityType.class, entityTypeId).get();
        if (container.contains(DataQueries.DATA_MANIPULATORS)) {
            this.customManipulators = new HashSet<>(DataUtil.deserializeImmutableManipulatorList(container.getViewList(DataQueries.DATA_MANIPULATORS).get()));
        }
        if (container.contains(DataQueries.UNSAFE_NBT)) {
            this.compound = NbtTranslator.getInstance().translateData(container.getView(DataQueries.UNSAFE_NBT).get());
        }
        if (container.contains(DataQueries.ENTITY_ID)) {
            this.entityId = UUID.fromString(container.getString(DataQueries.ENTITY_ID).get());
        }

        ImmutableSet.Builder<ImmutableDataManipulator<?, ?>> set = ImmutableSet.builder();
        World world = Sponge.getServer().getWorld(this.worldId).get();
        Entity entity = null;
        if (this.entityId != null) {
            entity = world.getEntity(this.entityId).orElse(null);
        }
        if (entity == null) {
            entity = world.createEntity(this.entityType, this.position);
        }
        final Set<DataManipulator<?, ?>> vanilla = ((IMixinEntity) entity).getVanillaManipulators();
        DataContainer cont = container.copy();
        for (DataManipulator<?, ?> manipulator : vanilla) {
            manipulator.from(cont).ifPresent(m -> set.add(m.asImmutable()));
        }
        this.vanillaManipulators = set.build();

        return Optional.of(build());
    }
}
