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
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetedEntityData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetedEntityData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTargetedEntityData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.interfaces.IEntityTargetingEntity;

import java.util.Optional;

public final class EntityTargetedEntityDataProcessor extends AbstractEntitySingleDataProcessor<Entity, Optional<org.spongepowered.api.entity.Entity>,
        OptionalValue<org.spongepowered.api.entity.Entity>, TargetedEntityData, ImmutableTargetedEntityData> {

    public EntityTargetedEntityDataProcessor() {
        super(Entity.class, Keys.TARGETED_ENTITY);
    }

    @Override
    protected boolean set(Entity dataHolder, Optional<org.spongepowered.api.entity.Entity> value) {
        if (!(dataHolder instanceof IEntityTargetingEntity)) {
            return false;
        }

        ((IEntityTargetingEntity) dataHolder).setTargetedEntity((Entity) value.orElse(null));

        return true;
    }

    @Override
    protected Optional<Optional<org.spongepowered.api.entity.Entity>> getVal(Entity dataHolder) {
        if (!(dataHolder instanceof IEntityTargetingEntity)) {
            return Optional.empty();
        }

        return Optional.of(Optional.ofNullable((org.spongepowered.api.entity.Entity) ((IEntityTargetingEntity) dataHolder).getTargetedEntity()));
    }

    @Override
    protected ImmutableOptionalValue<org.spongepowered.api.entity.Entity> constructImmutableValue(
            Optional<org.spongepowered.api.entity.Entity> value) {
        return new ImmutableSpongeOptionalValue<>(this.key, value);
    }

    @Override
    protected OptionalValue<org.spongepowered.api.entity.Entity> constructValue(Optional<org.spongepowered.api.entity.Entity> actualValue) {
        return new SpongeOptionalValue<>(this.key, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected TargetedEntityData createManipulator() {
        return new SpongeTargetedEntityData();
    }
}
