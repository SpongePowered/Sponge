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
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.DataContainerHolder;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.data.AbstractArchetype;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.nbt.validation.RawDataValidator;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.provider.DataProviderLookup;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeEntityArchetype extends AbstractArchetype<EntityType, EntitySnapshot, org.spongepowered.api.entity.Entity> implements EntityArchetype,
        DataContainerHolder.Mutable {

    // TODO actually validate stuff
    public static final ImmutableList<RawDataValidator> VALIDATORS = ImmutableList.of();

    private static final DataProviderLookup lookup = SpongeDataManager.getProviderRegistry().getProviderLookup(SpongeEntityArchetype.class);

    @Nullable
    private Vector3d position;

    SpongeEntityArchetype(SpongeEntityArchetypeBuilder builder) {
        super(builder.entityType, builder.compound != null ? builder.compound : builder.entityData == null ? new CompoundNBT() : NbtTranslator.getInstance().translate(builder.entityData));
    }

    @Override
    public EntityType<?> getType() {
        return this.type;
    }

    @Nullable
    public CompoundNBT getData() {
        return this.data;
    }

    @Override
    public DataProviderLookup getLookup() {
        return SpongeEntityArchetype.lookup;
    }

    public Optional<Vector3d> getPosition() {
        if (this.position != null) {
            return Optional.of(this.position);
        }
        if (!this.data.contains(Constants.Entity.ENTITY_POSITION, Constants.NBT.TAG_LIST)) {
            return Optional.empty();
        }
        try {
            ListNBT pos = this.data.getList(Constants.Entity.ENTITY_POSITION, Constants.NBT.TAG_DOUBLE);
            double x = pos.getDouble(0);
            double y = pos.getDouble(1);
            double z = pos.getDouble(2);
            this.position = new Vector3d(x, y, z);
            return Optional.of(this.position);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public DataContainer data$getDataContainer() {
        return this.getEntityData();
    }

    @Override
    public void data$setDataContainer(DataContainer container) {
        this.data = NbtTranslator.getInstance().translate(container);
    }

    @Override
    public DataContainer getEntityData() {
        return NbtTranslator.getInstance().translateFrom(this.data);
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> apply(ServerLocation location) {
        if (!SpongeImplHooks.onServerThread()) {
            return Optional.empty();
        }
        final org.spongepowered.api.world.server.ServerWorld spongeWorld = location.getWorld();
        final ServerWorld worldServer = (ServerWorld) spongeWorld;

        final Entity entity = ((net.minecraft.entity.EntityType<?>) this.type).create(worldServer);
        if (entity == null) {
            return Optional.empty();
        }
        entity.setPosition(location.getX(), location.getY(), location.getZ()); // Set initial position

        final boolean requiresInitialSpawn;
        if (this.data.contains(Constants.Sponge.EntityArchetype.REQUIRES_EXTRA_INITIAL_SPAWN)) {
            requiresInitialSpawn = !this.data.getBoolean(Constants.Sponge.EntityArchetype.REQUIRES_EXTRA_INITIAL_SPAWN);
            this.data.remove(Constants.Sponge.EntityArchetype.REQUIRES_EXTRA_INITIAL_SPAWN);
        } else {
            requiresInitialSpawn = true;
        }

        if (entity instanceof MobEntity) {
            MobEntity mobentity = (MobEntity) entity;
            mobentity.rotationYawHead = mobentity.rotationYaw;
            mobentity.renderYawOffset = mobentity.rotationYaw;
            if (requiresInitialSpawn) {
                // TODO null reason?
                mobentity.onInitialSpawn(worldServer, worldServer.getDifficultyForLocation(new BlockPos(mobentity)), null, null, null);
            }
        }

        // like applyItemNBT
        final CompoundNBT mergedNbt = entity.writeWithoutTypeId(new CompoundNBT());
        final UUID uniqueID = entity.getUniqueID();

        mergedNbt.merge(this.data);
        mergedNbt.remove(Constants.Sponge.EntityArchetype.REQUIRES_EXTRA_INITIAL_SPAWN);
        final Integer dimensionId = ((WorldInfoBridge) location.getWorld().getProperties()).bridge$getDimensionId();
        // TODO null dimensionId possible?
        mergedNbt.putInt(Constants.Entity.ENTITY_DIMENSION, dimensionId); // Fix dimension
        mergedNbt.putUniqueId(Constants.Entity.ENTITY_UUID, uniqueID); // TODO can we avoid this when the entity is only spawned once?
        entity.read(mergedNbt); // Read in all data
        entity.setPosition(location.getX(), location.getY(), location.getZ());

        // Finished building the entity. Now spawn it if not cancelled.
        final org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) entity;
        final List<org.spongepowered.api.entity.Entity> entities = new ArrayList<>();
        entities.add(spongeEntity);

        // We require spawn types. This is more of a sanity check to throw an IllegalStateException otherwise for the plugin developer to properly associate the type.
        final SpawnType require = PhaseTracker.getCauseStackManager().getCurrentContext().require(EventContextKeys.SPAWN_TYPE);
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(PhaseTracker.getCauseStackManager().getCurrentCause(), entities);
        if (!event.isCancelled()) {
            worldServer.addEntity(entity);
            return Optional.of(spongeEntity);
        }
        return Optional.empty();
    }

    @Override
    public EntitySnapshot toSnapshot(ServerLocation location) {
        final SpongeEntitySnapshotBuilder builder = new SpongeEntitySnapshotBuilder();
        builder.entityType = this.type;
        CompoundNBT newCompound = this.data.copy();
        final Vector3d pos = location.getPosition();
        newCompound.put(Constants.Entity.ENTITY_POSITION, Constants.NBT.newDoubleNBTList(pos.getX(), pos.getY(), pos.getZ()));
        newCompound.putInt(Constants.Entity.ENTITY_DIMENSION, ((WorldInfoBridge) location.getWorld().getProperties()).bridge$getDimensionId());
        builder.compound = newCompound;
        builder.worldKey = location.getWorld().getProperties().getKey();
        builder.position = pos;
        builder.rotation = this.getRotation();
        builder.scale = Vector3d.ONE;
        return builder.build();
    }

    private Vector3d getRotation() {
        final ListNBT listnbt3 = this.data.getList("Rotation", 5);
        float rotationYaw = listnbt3.getFloat(0);
        float rotationPitch = listnbt3.getFloat(1);
        return new Vector3d(rotationPitch, rotationYaw, 0);
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.EntityArchetype.BASE_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Constants.Sponge.EntityArchetype.ENTITY_TYPE, this.type)
                .set(Constants.Sponge.EntityArchetype.ENTITY_DATA, this.getEntityData());
    }

    @Override
    protected ValidationType getValidationType() {
        return Validations.ENTITY;
    }

    @Override
    public EntityArchetype copy() {
        final SpongeEntityArchetypeBuilder builder = new SpongeEntityArchetypeBuilder();
        builder.entityType = this.type;
        builder.entityData = NbtTranslator.getInstance().translate(this.data);
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SpongeEntityArchetype that = (SpongeEntityArchetype) o;
        return Objects.equals(this.position, that.position);
    }

    @Override
    protected ImmutableList<RawDataValidator> getValidators() {
        return SpongeEntityArchetype.VALIDATORS;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.position);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("position", this.position)
                .add("type", this.type)
                .toString();
    }
}
