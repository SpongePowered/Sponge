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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.data.nbt.validation.DelegateDataValidator;
import org.spongepowered.common.data.nbt.validation.ValidationTypes;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;

import java.util.Objects;
import java.util.Optional;

public class SpongeEntityArchetypeBuilder extends AbstractDataBuilder<EntityArchetype> implements EntityArchetype.Builder {

    @Nullable EntityType<@NonNull ?> entityType = null;
    @Nullable CompoundTag compound;
    DataManipulator.@Nullable Mutable manipulator;
    @Nullable Vector3d position;

    public SpongeEntityArchetypeBuilder() {
        super(EntityArchetype.class, Constants.Sponge.EntityArchetype.BASE_VERSION);
    }

    @Override
    public EntityArchetype.Builder reset() {
        this.entityType = null;
        this.manipulator = null;
        this.compound = null;
        return this;
    }

    @Override
    public EntityArchetype.Builder from(final EntityArchetype value) {
        this.entityType = value.type();
        this.compound = NBTTranslator.INSTANCE.translate(value.entityData());
        this.stripCompound(this.compound);
        // TODO Copy over the pending manipulator data?
        this.manipulator = null;
        return this;
    }

    @Override
    protected Optional<EntityArchetype> buildContent(final DataView container) throws InvalidDataException {
        if (container.contains(Constants.Sponge.EntityArchetype.ENTITY_TYPE)) {
            this.type(container.getRegistryValue(Constants.Sponge.EntityArchetype.ENTITY_TYPE, RegistryTypes.ENTITY_TYPE,
                Sponge.game().registries()).orElseThrow(() -> new InvalidDataException("Could not deserialize an EntityType!")));
        } else {
            throw new InvalidDataException("Missing the EntityType! Cannot re-construct an EntityArchetype!");
        }

        if (container.contains(Constants.Sponge.EntityArchetype.ENTITY_DATA)) {
            this.entityData(container.getView(Constants.Sponge.EntityArchetype.ENTITY_DATA)
                    .orElseThrow(() -> new InvalidDataException("No DataView found for the 'EntityData' data tag!")));
        }
        return Optional.of(this.build());
    }

    @Override
    public EntityArchetype.Builder type(final EntityType<@NonNull ?> type) {
        if (this.entityType != Objects.requireNonNull(type, "EntityType cannot be null!")) {
            // TODO Type changed, should we also clear the pending manipulator data?
            this.compound = null;
        }
        this.entityType = type;
        return this;
    }

    @Override
    public EntityArchetype.Builder from(final Entity entity) {
        this.entityType = Objects.requireNonNull(Objects.requireNonNull(entity, "Cannot build an EntityArchetype for a null entity!").type(),
            "Entity is returning a null EntityType!");
        final CompoundTag compound = new CompoundTag();
        ((net.minecraft.world.entity.Entity) entity).saveAsPassenger(compound);
        this.stripCompound(compound);
        compound.putBoolean(Constants.Sponge.EntityArchetype.REQUIRES_EXTRA_INITIAL_SPAWN, true);
        this.position = entity.position();
        this.compound = compound;
        return this;
    }

    @Override
    public EntityArchetype.Builder entityData(final DataView view) {
        final DataContainer container = Objects.requireNonNull(view, "Provided DataView cannot be null!").copy();
        new DelegateDataValidator(SpongeEntityArchetype.VALIDATORS, ValidationTypes.ENTITY.get()).validate(container);
        this.compound = NBTTranslator.INSTANCE.translate(container);
        this.stripCompound(this.compound);
        return this;
    }

    @Override
    public <V> EntityArchetype.Builder add(final Key<? extends Value<V>> key, final V value) {
        if (this.manipulator == null) {
            this.manipulator = DataManipulator.mutableOf();
        }
        this.manipulator.set(Objects.requireNonNull(key, "Key cannot be null!"), Objects.requireNonNull(value, "Value cannot be null!"));
        return this;
    }

    @Override
    public EntityArchetype build() {
        Objects.requireNonNull(this.entityType, "Entity type cannot be nulL!");
        final SpongeEntityArchetype archetype = new SpongeEntityArchetype(this);
        if (this.manipulator != null) {
            archetype.copyFrom(this.manipulator);
        }
        return archetype;
    }

    private void stripCompound(final CompoundTag compound) {
        compound.remove(Constants.UUID);
        compound.remove(Constants.UUID_MOST);
        compound.remove(Constants.UUID_LEAST);
        compound.remove(Constants.Entity.ENTITY_POSITION);
    }
}
