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

import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.nbt.validation.DelegateDataValidator;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.Optional;

public class SpongeEntityArchetypeBuilder extends AbstractDataBuilder<EntityArchetype> implements EntityArchetype.Builder {

    EntityType entityType = null;
    DataContainer entityData;
    CompoundNBT compound;
    DataManipulator.Mutable manipulator;

    public SpongeEntityArchetypeBuilder() {
        super(EntityArchetype.class, Constants.Sponge.EntityArchetype.BASE_VERSION);
    }

    @Override
    public EntityArchetype.Builder reset() {
        this.entityType = null;
        this.entityData = null;
        this.manipulator = null;
        return this;
    }

    @Override
    public EntityArchetype.Builder from(final EntityArchetype value) {
        this.entityType = value.getType();
        this.entityData = value.getEntityData();
        this.manipulator = null;
        return this;
    }

    @Override
    protected Optional<EntityArchetype> buildContent(final DataView container) throws InvalidDataException {
        final SpongeEntityArchetypeBuilder builder = new SpongeEntityArchetypeBuilder();
        if (container.contains(Constants.Sponge.EntityArchetype.ENTITY_TYPE)) {
            builder.type(container.getCatalogType(Constants.Sponge.EntityArchetype.ENTITY_TYPE, EntityType.class)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize a TileEntityType!")));
        } else {
            throw new InvalidDataException("Missing the TileEntityType and BlockState! Cannot re-construct a TileEntityArchetype!");
        }

        if (container.contains(Constants.Sponge.EntityArchetype.ENTITY_DATA)) {
            builder.entityData(container.getView(Constants.Sponge.EntityArchetype.ENTITY_DATA)
                    .orElseThrow(() -> new InvalidDataException("No DataView found for the TileEntity data tag!"))
            );
        }
        return Optional.of(builder.build());
    }

    @Override
    public EntityArchetype.Builder type(final EntityType type) {
        Objects.requireNonNull(type, "EntityType cannot be null!");
        if (this.entityType != type) {
            this.entityData = null;
        }
        this.entityType = type;
        return this;
    }

    @Override
    public EntityArchetype.Builder from(final Entity entity) {
        Objects.requireNonNull(entity, "Cannot build an EntityArchetype for a null entity!");
        this.entityType = Objects.requireNonNull(entity.getType(), "Entity is returning a null EntityType!");
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final CompoundNBT compound = new CompoundNBT();
        minecraftEntity.writeWithoutTypeId(compound);
        compound.putString(Constants.Sponge.EntityArchetype.ENTITY_ID, entity.getType().getKey().toString());
        compound.remove(Constants.UUID);
        compound.remove(Constants.UUID_MOST);
        compound.remove(Constants.UUID_LEAST);
        compound.putBoolean(Constants.Sponge.EntityArchetype.REQUIRES_EXTRA_INITIAL_SPAWN, true);
        this.compound = compound;
        return this;
    }

    @Override
    public EntityArchetype.Builder entityData(final DataView view) {
        Objects.requireNonNull(view, "Provided DataView cannot be null!");
        final DataContainer copy = view.copy();
        new DelegateDataValidator(SpongeEntityArchetype.VALIDATORS, Validations.ENTITY).validate(copy);
        this.entityData = copy;
        this.compound = null;
        return this;
    }

    @Override
    public <V> EntityArchetype.Builder add(Key<? extends Value<V>> key, V value) {
        if (this.manipulator == null) {
            this.manipulator = DataManipulator.mutableOf();
        }
        this.manipulator.set(key, value);
        return this;
    }

    @Override
    public EntityArchetype build() {
        Objects.requireNonNull(this.entityType);
        if (this.entityData != null) {
            this.entityData.remove(Constants.Entity.Player.UUID);
        }
        final SpongeEntityArchetype archetype = new SpongeEntityArchetype(this);
        if (this.manipulator != null) {
            archetype.copyFrom(this.manipulator);
        }
        return archetype;
    }
}
