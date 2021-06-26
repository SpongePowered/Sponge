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

import com.google.common.base.MoreObjects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.data.DataContainerHolder;
import org.spongepowered.common.data.holder.SpongeImmutableDataHolder;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

public class SpongeEntitySnapshot implements EntitySnapshot, SpongeImmutableDataHolder<EntitySnapshot>, DataContainerHolder.Immutable<EntitySnapshot> {

    private final @Nullable UUID uniqueId;
    private final ResourceKey worldKey;
    private final EntityType<?> entityType;
    private final Vector3d position;
    private final Vector3d rotation;
    private final Vector3d scale;
    private final @Nullable CompoundTag compound;
    private final @Nullable WeakReference<Entity> entityReference;

    SpongeEntitySnapshot(final SpongeEntitySnapshotBuilder builder) {
        this.entityType = builder.entityType;
        this.uniqueId = builder.uniqueId;

        this.compound = builder.compound == null ? null : builder.compound.copy();
        if (builder.manipulator != null) {
            ((SpongeDataHolderBridge) this).bridge$getManipulator().copyFrom(builder.manipulator);
        }

        this.worldKey = builder.worldKey;
        this.position = builder.position;
        this.rotation = builder.rotation;
        this.scale = builder.scale;
        this.entityReference = builder.entityReference;
        if (this.compound != null) {
            this.compound.put(Constants.Entity.ENTITY_POSITION, Constants.NBT.newDoubleNBTList(this.position.x(), this.position.y(), this.position.z()));
            // TODO should ensure other elements are within the compound as well
        }
    }

    // internal use only
    public WeakReference<Entity> getEntityReference() {
        return this.entityReference;
    }

    @Override
    public Optional<UUID> uniqueId() {
        return Optional.ofNullable(this.uniqueId);
    }

    @Override
    public Optional<Transform> transform() {
        final Transform transform = Transform.of(this.position, this.rotation);
        return Optional.of(transform);
    }

    @Override
    public EntityType<?> type() {
        return this.entityType;
    }

    @Override
    public Optional<ServerLocation> location() {
        final Optional<ServerWorld> optional = SpongeCommon.game().server().worldManager().world(this.worldKey);
        if (optional.isPresent()) {
            final ServerLocation location = ServerLocation.of(optional.get(), this.position);
            return Optional.of(location);
        }
        return Optional.empty();
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer unsafeNbt = NBTTranslator.INSTANCE.translateFrom(this.compound == null ? new CompoundTag() : this.compound);
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Queries.WORLD_KEY, this.worldKey.formatted())
                .createView(Constants.Sponge.SNAPSHOT_WORLD_POSITION)
                .set(Queries.POSITION_X, this.position.x())
                .set(Queries.POSITION_Y, this.position.y())
                .set(Queries.POSITION_Z, this.position.z())
                .container()
                .createView(Constants.Entity.ROTATION)
                .set(Queries.POSITION_X, this.rotation.x())
                .set(Queries.POSITION_Y, this.rotation.y())
                .set(Queries.POSITION_Z, this.rotation.z())
                .container()
                .createView(Constants.Entity.SCALE)
                .set(Queries.POSITION_X, this.scale.x())
                .set(Queries.POSITION_Y, this.scale.y())
                .set(Queries.POSITION_Z, this.scale.z())
                .container()
                .set(Constants.Entity.TYPE, net.minecraft.world.entity.EntityType.getKey((net.minecraft.world.entity.EntityType<?>) this.entityType))
                .set(Constants.Sponge.UNSAFE_NBT, unsafeNbt);

        if (this.uniqueId != null) {
            container.set(Constants.Entity.UUID, this.uniqueId.toString());
        }
        return container;
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return new SpongeEntitySnapshotBuilder().buildContent(container).isPresent();
    }

    @Override
    public EntitySnapshot withRawData(final DataView container) throws InvalidDataException {
        final Optional<EntitySnapshot> snap = new SpongeEntitySnapshotBuilder().buildContent(container);
        return snap.orElseThrow(InvalidDataException::new);
    }

    @Override
    public EntitySnapshot copy() {
        return this;
    }

    @Override
    public ResourceKey world() {
        return this.worldKey;
    }

    @Override
    public Vector3i position() {
        return this.position.toInt();
    }

    @Override
    public EntitySnapshot withLocation(final ServerLocation location) {
        Objects.requireNonNull(location, "location");
        final SpongeEntitySnapshotBuilder builder = this.createBuilder();
        builder.position = location.position();
        builder.worldKey = location.world().key();
        builder.compound = new CompoundTag();
        return builder.build();
    }

    private SpongeEntitySnapshotBuilder createBuilder() {
        return new SpongeEntitySnapshotBuilder()
                .type(this.type())
                .rotation(this.rotation)
                .scale(this.scale);
    }

    public Optional<CompoundTag> getCompound() {
        if (this.compound == null) {
            return Optional.empty();
        }
        return Optional.of(this.compound.copy());
    }

    @Override
    public Optional<Entity> restore() {
        if (this.entityReference != null) {
            final Entity entity = this.entityReference.get();
            if (entity != null) {
                return Optional.of(entity);
            }
        }
        final Optional<ServerWorld> world = Sponge.server().worldManager().world(this.worldKey);
        if (!world.isPresent()) {
            return Optional.empty();
        }
        if (this.uniqueId != null) {
            final Optional<Entity> entity = world.get().entity(this.uniqueId);
            if (entity.isPresent()) {
                return entity;
            }
        }
        try (final StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
            final Entity newEntity = world.get().createEntity(this.type(), this.position);
            if (newEntity != null) {
                final net.minecraft.world.entity.Entity nmsEntity = (net.minecraft.world.entity.Entity) newEntity;
                if (this.compound != null) {
                    nmsEntity.load(this.compound);
                }

                final boolean spawnResult = world.get().spawnEntity((Entity) nmsEntity);
                if (spawnResult) {
                    return Optional.of((Entity) nmsEntity);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public EntityArchetype createArchetype() {
        final EntityArchetype.Builder builder = new SpongeEntityArchetypeBuilder();
        builder.type(this.entityType);
        if (this.compound != null) {
            builder.entityData(NBTTranslator.INSTANCE.translate(this.compound));
        }
        return builder.build();
    }

    @Override
    public DataContainer data$getDataContainer() {
        if (this.compound == null) {
            return DataContainer.createNew();
        }
        return NBTTranslator.INSTANCE.translate(this.compound);
    }

    @Override
    public EntitySnapshot data$withDataContainer(final DataContainer container) {
        final SpongeEntitySnapshotBuilder builder = this.createBuilder();
        builder.worldKey = this.worldKey;
        builder.position = this.position;
        builder.compound = NBTTranslator.INSTANCE.translate(container);;
        return builder.build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId, this.worldKey, this.entityType, this.position, this.rotation, this.scale);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        final SpongeEntitySnapshot other = (SpongeEntitySnapshot) obj;
        return Objects.equals(this.uniqueId, other.uniqueId)
                && Objects.equals(this.worldKey, other.worldKey)
                && Objects.equals(this.entityType, other.entityType)
                && Objects.equals(this.position, other.position)
                && Objects.equals(this.rotation, other.rotation)
                && Objects.equals(this.scale, other.scale);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uniqueId", this.uniqueId)
                .add("entityType", this.entityType)
                .add("position", this.position)
                .add("rotation", this.rotation)
                .add("scale", this.scale)
                .toString();
    }
}
