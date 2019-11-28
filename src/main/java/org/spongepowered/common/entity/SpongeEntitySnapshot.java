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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.Constants;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;

public class SpongeEntitySnapshot implements EntitySnapshot {

    @Nullable private final UUID entityUuid;
    private final UUID worldUuid;
    private final EntityType entityType;
    private final Vector3d position;
    private final Vector3d rotation;
    private final Vector3d scale;
    private final ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;
    private final ImmutableSet<Key<?>> keys;
    private final ImmutableSet<ImmutableValue<?>> values;
    @Nullable private final CompoundNBT compound;
    @Nullable private final WeakReference<Entity> entityReference;
    // TODO write optimization to lazy load and evaluate all of the manipulators
    // for entities during events.
    private boolean isDirty = true;

    SpongeEntitySnapshot(SpongeEntitySnapshotBuilder builder) {
        this.entityType = builder.entityType;
        this.entityUuid = builder.entityId;
        if (builder.manipulators == null) {
            this.manipulators = ImmutableList.of();
        } else {
            this.manipulators = ImmutableList.copyOf(builder.manipulators);
        }
        if (this.manipulators.isEmpty()) {
            this.keys = ImmutableSet.of();
            this.values = ImmutableSet.of();
        } else {
            final ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
            final ImmutableSet.Builder<ImmutableValue<?>> valueBuilder = ImmutableSet.builder();
            for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
                for (ImmutableValue<?> value : manipulator.getValues()) {
                    keyBuilder.add(value.getKey());
                    valueBuilder.add(value);
                }
            }
            this.keys = keyBuilder.build();
            this.values = valueBuilder.build();
        }
        this.compound = builder.compound == null ? null : builder.compound.func_74737_b();
        this.worldUuid = builder.worldId;
        this.position = builder.position;
        this.rotation = builder.rotation;
        this.scale = builder.scale;
        this.entityReference = builder.entityReference;
        if (this.compound != null) {
            this.compound.func_74782_a("Pos", Constants.NBT.newDoubleNBTList(this.position.getX(), this.position.getY(), this.position.getZ()));
            // TODO should ensure other elements are within the compound as well
        }
    }

    // internal use only
    public WeakReference<Entity> getEntityReference() {
        return this.entityReference;
    }

    @Override
    public Optional<UUID> getUniqueId() {
        return Optional.ofNullable(this.entityUuid);
    }

    @Override
    public Optional<Transform<World>> getTransform() {
        Optional<World> optional = SpongeImpl.getGame().getServer().getWorld(this.worldUuid);
        if (optional.isPresent()) {
            final Transform<World> transform = new Transform<>(optional.get(), this.position, this.rotation);
            return Optional.of(transform);
        }
        return Optional.empty();
    }

    @Override
    public EntityType getType() {
        return this.entityType;
    }

    @Override
    public Optional<Location<World>> getLocation() {
        Optional<World> optional = SpongeImpl.getGame().getServer().getWorld(this.worldUuid);
        if (optional.isPresent()) {
            final Location<World> location = new Location<>(optional.get(), this.position);
            return Optional.of(location);
        }
        return Optional.empty();
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return this.manipulators;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final List<DataView> dataList = DataUtil.getSerializedImmutableManipulatorList(this.manipulators);
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(Queries.WORLD_ID, this.worldUuid.toString())
                .set(Constants.Entity.TYPE, this.entityType.getId())
                .createView(Constants.Sponge.SNAPSHOT_WORLD_POSITION)
                .set(Queries.POSITION_X, this.position.getX())
                .set(Queries.POSITION_Y, this.position.getY())
                .set(Queries.POSITION_Z, this.position.getZ())
                .getContainer()
                .createView(Constants.Entity.ROTATION)
                .set(Queries.POSITION_X, this.rotation.getX())
                .set(Queries.POSITION_Y, this.rotation.getY())
                .set(Queries.POSITION_Z, this.rotation.getZ())
                .getContainer()
                .createView(Constants.Entity.SCALE)
                .set(Queries.POSITION_X, this.scale.getX())
                .set(Queries.POSITION_Y, this.scale.getY())
                .set(Queries.POSITION_Z, this.scale.getZ())
                .getContainer()
                .set(Constants.Sponge.DATA_MANIPULATORS, dataList);

        if (this.entityUuid != null) {
            container.set(Constants.Entity.UUID, this.entityUuid.toString());
        }
        if (this.compound != null) {
            container.set(Constants.Sponge.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(this.compound));
        }

        return container;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of((T) manipulator);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        final Optional<T> optional = get(containerClass);
        if (optional.isPresent()) {
            return optional;
        }
        // try harder
        final Optional<DataProcessor> processorOptional = DataUtil.getWildImmutableProcessor(containerClass);
        if (processorOptional.isPresent()) {
            if (processorOptional.get().supports(this.entityType)) {
                return Optional
                        .of((T) SpongeDataManager.getInstance().getWildBuilderForImmutable(containerClass).get().create().asImmutable());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            if (containerClass.isInstance(manipulator)) {
                return true;
            }
        }
        final Optional<DataProcessor> processorOptional = DataUtil.getWildImmutableProcessor(containerClass);
        return processorOptional.isPresent() && processorOptional.get().supports(this.entityType);
    }

    @Override
    public <E> Optional<EntitySnapshot> transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        checkNotNull(key);
        checkNotNull(function);
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        boolean createNew = false;
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            if (manipulator.supports(key)) {
                createNew = true;
                builder.add(manipulator.with(key, checkNotNull(function.apply(manipulator.get(key).orElse(null)))).get());
            } else {
                builder.add(manipulator);
            }
        }
        if (createNew) {
            final SpongeEntitySnapshotBuilder snapshotBuilder = createBuilder();
            snapshotBuilder.manipulators = builder.build();
            return Optional.of(snapshotBuilder.build());
        }
        return Optional.empty();
    }

    @Override
    public <E> Optional<EntitySnapshot> with(Key<? extends BaseValue<E>> key, E value) {
        return transform(key, input -> value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<EntitySnapshot> with(BaseValue<?> value) {
        return with((Key<? extends BaseValue<Object>>) value.getKey(), value.get());
    }

    @Override
    public Optional<EntitySnapshot> with(ImmutableDataManipulator<?, ?> valueContainer) {
        return Optional.of(createBuilder().add(valueContainer).build());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Optional<EntitySnapshot> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        final Builder builder = createBuilder();
        for (ImmutableDataManipulator manipulator : valueContainers) {
            builder.add(manipulator);
        }
        return Optional.of(builder.build());
    }

    @Override
    public Optional<EntitySnapshot> without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        if (!supports(containerClass)) {
            return Optional.empty();
        }
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        for (ImmutableDataManipulator<?, ?> manipulator : this.manipulators) {
            if (!containerClass.isAssignableFrom(manipulator.getClass())) {
                builder.add(manipulator);
            }
        }
        final SpongeEntitySnapshotBuilder snapshotBuilder = createBuilder();
        snapshotBuilder.manipulators = builder.build();
        return Optional.of(snapshotBuilder.build());
    }

    @Override
    public EntitySnapshot merge(EntitySnapshot that) {
        return this;
    }

    @Override
    public EntitySnapshot merge(EntitySnapshot that, MergeFunction function) {
        return this;
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return this.getManipulators();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        checkNotNull(key);
        for (ImmutableValue<?> value : this.values) {
            if (value.getKey().equals(key)) {
                return Optional.of((E) value.get());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        checkNotNull(key);
        for (ImmutableValue<?> value : this.values) {
            if (value.getKey().equals(key)) {
                return Optional.of((V) value.asMutable());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return this.keys.contains(key);
    }

    @Override
    public EntitySnapshot copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return this.keys;
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return this.values;
    }

    @Override
    public UUID getWorldUniqueId() {
        return this.worldUuid;
    }

    @Override
    public Vector3i getPosition() {
        return this.position.toInt();
    }

    @Override
    public EntitySnapshot withLocation(Location<World> location) {
        checkNotNull(location, "location");
        final SpongeEntitySnapshotBuilder builder = createBuilder();
        builder.position = location.getPosition();
        builder.worldId = location.getExtent().getUniqueId();
        CompoundNBT newCompound = this.compound.func_74737_b();
        newCompound.func_74768_a("Dimension", ((WorldInfoBridge) location.getExtent().getProperties()).bridge$getDimensionId());
        builder.compound = newCompound;
        return builder.build();
    }

    private SpongeEntitySnapshotBuilder createBuilder() {
        return new SpongeEntitySnapshotBuilder()
                .type(this.getType())
                .rotation(this.rotation)
                .scale(this.scale);
    }

    public Optional<CompoundNBT> getCompound() {
        if (this.compound == null) {
            return Optional.empty();
        }
        return Optional.of(this.compound.func_74737_b());
    }

    @Override
    public Optional<Entity> restore() {
        if (this.entityReference != null) {
            Entity entity = this.entityReference.get();
            if (entity != null) {
                return Optional.of(entity);
            }
        }
        Optional<World> world = SpongeImpl.getGame().getServer().getWorld(this.worldUuid);
        if (!world.isPresent()) {
            return Optional.empty();
        }
        if (this.entityUuid != null) {
            Optional<Entity> entity = world.get().getEntity(this.entityUuid);
            if (entity.isPresent()) {
                return entity;
            }
        }
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
            Entity newEntity = world.get().createEntity(getType(), this.position);
            if (newEntity != null) {
                net.minecraft.entity.Entity nmsEntity = (net.minecraft.entity.Entity) newEntity;
                if (this.compound != null) {
                    nmsEntity.func_70020_e(this.compound);
                }
    
                boolean spawnResult = world.get().spawnEntity((Entity) nmsEntity);
                if (spawnResult) {
                    return Optional.of((Entity) nmsEntity);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return ImmutableList.of();
    }

    @Override
    public EntityArchetype createArchetype() {
        EntityArchetype.Builder builder = new SpongeEntityArchetypeBuilder();
        builder.type(this.entityType);
        if (this.compound != null) {
            builder.entityData(NbtTranslator.getInstance().translate(this.compound));
        }
        return builder.build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entityUuid, this.worldUuid, this.entityType, this.position, this.rotation, this.scale, this.manipulators, this.keys, this.values);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final SpongeEntitySnapshot other = (SpongeEntitySnapshot) obj;
        return Objects.equals(this.entityUuid, other.entityUuid)
                && Objects.equals(this.worldUuid, other.worldUuid)
                && Objects.equals(this.entityType, other.entityType)
                && Objects.equals(this.position, other.position)
                && Objects.equals(this.rotation, other.rotation)
                && Objects.equals(this.scale, other.scale)
                && Objects.equals(this.manipulators, other.manipulators)
                && Objects.equals(this.keys, other.keys)
                && Objects.equals(this.values, other.values);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uniqueId", this.entityUuid)
                .add("entityType", this.entityType)
                .add("position", this.position)
                .add("rotation", this.rotation)
                .add("scale", this.scale)
                .toString();
    }
}
