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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.api.entity.EntityTypes.UNKNOWN;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.nbt.NbtDataTypes;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataVersions;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeEntityArchetypeBuilder extends AbstractDataBuilder<EntityArchetype> implements EntityArchetype.Builder {

    EntityType entityType = UNKNOWN; // -These two fields can never be null
    DataContainer entityData;                    // This can be empty, but cannot be null.

    public SpongeEntityArchetypeBuilder() {
        super(EntityArchetype.class, DataVersions.EntityArchetype.BASE_VERSION);
    }

    @Override
    public EntityArchetype.Builder reset() {
        this.entityType = UNKNOWN;
        this.entityData = new MemoryDataContainer();
        return this;
    }

    @Override
    public EntityArchetype.Builder from(EntityArchetype value) {
        this.entityType = value.getType();
        this.entityData = value.getEntityData();
        return this;
    }

    @Override
    protected Optional<EntityArchetype> buildContent(DataView container) throws InvalidDataException {
        final SpongeEntityArchetypeBuilder builder = new SpongeEntityArchetypeBuilder();
        if (container.contains(DataQueries.EntityArchetype.ENTITY_TYPE)) {
            builder.type(container.getCatalogType(DataQueries.EntityArchetype.ENTITY_TYPE, EntityType.class)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize a TileEntityType!"))
            );
        } else {
            throw new InvalidDataException("Missing the TileEntityType and BlockState! Cannot re-construct a TileEntityArchetype!");
        }

        if (container.contains(DataQueries.EntityArchetype.ENTITY_DATA)) {
            builder.entityData(container.getView(DataQueries.EntityArchetype.ENTITY_DATA)
                    .orElseThrow(() -> new InvalidDataException("No DataView found for the TileEntity data tag!"))
            );
        } else {
            builder.entityData(new MemoryDataContainer());
        }
        return Optional.of(builder.build());
    }

    @Override
    public EntityArchetype.Builder type(EntityType type) {
        checkNotNull(type, "EntityType cannot be null!");
        checkArgument(type != UNKNOWN, "EntityType cannot be set to UNKNOWN!");
        if (this.entityType != type) {
            this.entityData = new MemoryDataContainer();
        }
        this.entityType = type;
        return this;
    }

    @Override
    public EntityArchetype.Builder from(Entity entity) {
        checkNotNull(entity, "Cannot build an EntityArchetype for a null entity!");
        this.entityType = checkNotNull(entity.getType(), "Entity is returning a null EntityType!");
        final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(entity);
        final NBTTagCompound compound = new NBTTagCompound();
        minecraftEntity.writeToNBT(compound);
        compound.removeTag(NbtDataUtil.UUID);
        this.entityData = NbtTranslator.getInstance().translate(compound);
        return this;
    }

    @Override
    public EntityArchetype.Builder entityData(DataView view) {
        checkNotNull(view, "Provided DataView cannot be null!");
        final DataContainer copy = view.copy();
        SpongeDataManager.getInstance().getValidators(Validations.ENTITY).validate(copy);
        this.entityData = copy;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityArchetype.Builder setData(DataManipulator<?, ?> manipulator) {
        if (this.entityData == null) {
            this.entityData = new MemoryDataContainer();
        }
        SpongeDataManager.getInstance().getRawNbtProcessor(NbtDataTypes.ENTITY, manipulator.getClass())
                .ifPresent(processor -> processor.storeToView(this.entityData, manipulator));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> EntityArchetype.Builder set(V value) {
        if (this.entityData == null) {
            this.entityData = new MemoryDataContainer();
        }
        SpongeDataManager.getInstance().getRawNbtProcessor(NbtDataTypes.TILE_ENTITY, value.getKey())
                .ifPresent(processor -> processor.offer(this.entityData, value));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> EntityArchetype.Builder set(Key<V> key, E value) {
        if (this.entityData == null) {
            this.entityData = new MemoryDataContainer();
        }
        SpongeDataManager.getInstance().getRawNbtProcessor(NbtDataTypes.TILE_ENTITY, key)
                .ifPresent(processor -> processor.offer(this.entityData, value));
        return this;
    }

    @Override
    public EntityArchetype build() {
        checkState(!this.entityData.isEmpty());
        checkNotNull(this.entityType);
        checkState(this.entityType != UNKNOWN);
        return new SpongeEntityArchetype(this);
    }
}
