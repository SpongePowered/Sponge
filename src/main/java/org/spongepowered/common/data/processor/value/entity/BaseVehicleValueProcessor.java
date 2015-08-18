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
package org.spongepowered.common.data.processor.value.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class BaseVehicleValueProcessor implements ValueProcessor<Entity, Value<Entity>> {

    @Override
    public Key<? extends BaseValue<Entity>> getKey() {
        return Keys.BASE_VEHICLE;
    }

    @Override
    public Optional<Entity> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof net.minecraft.entity.Entity) {
            net.minecraft.entity.Entity currentEntity = ((net.minecraft.entity.Entity) container).ridingEntity;
            while (currentEntity != null && currentEntity.isRiding()) {
                currentEntity = currentEntity.ridingEntity;
            }
            final Entity baseVehicle = (Entity) currentEntity;
            return Optional.of(baseVehicle);
        }
        return Optional.absent();
    }

    @Override
    public Optional<Value<Entity>> getApiValueFromContainer(ValueContainer<?> container) {
        if (container instanceof net.minecraft.entity.Entity) {
            net.minecraft.entity.Entity currentEntity = ((net.minecraft.entity.Entity) container).ridingEntity;
            while (currentEntity != null && currentEntity.isRiding()) {
                currentEntity = currentEntity.ridingEntity;
            }
            final Entity baseVehicle = (Entity) currentEntity;
            return Optional.<Value<Entity>>of(new SpongeValue<Entity>(Keys.BASE_VEHICLE, baseVehicle));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof net.minecraft.entity.Entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
        final BaseValue<Entity> actualValue = (BaseValue<Entity>) value;
        final ImmutableValue<Entity> proposedValue = new ImmutableSpongeValue<Entity>(Keys.BASE_VEHICLE, actualValue.get());
        if (container instanceof net.minecraft.entity.Entity) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final ImmutableValue<Entity> newVehicleValue = new ImmutableSpongeValue<Entity>(Keys.BASE_VEHICLE, actualValue.get());
            final ImmutableValue<Entity> oldVehicleValue = getApiValueFromContainer(container).get().asImmutable();
            if (actualValue.get() == null) {
                return DataTransactionBuilder.errorResult(newVehicleValue);
            }
            try {
                net.minecraft.entity.Entity currentEntity = ((net.minecraft.entity.Entity) container).ridingEntity;
                while (currentEntity != null && currentEntity.isRiding()) {
                    currentEntity = currentEntity.ridingEntity;
                }
                final net.minecraft.entity.Entity baseVehicle = currentEntity;
                if (baseVehicle != null) {
                    baseVehicle.riddenByEntity.ridingEntity = (net.minecraft.entity.Entity) actualValue.get();
                } else {
                    return DataTransactionBuilder.errorResult(newVehicleValue);
                }
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newVehicleValue);
            }
            return builder.success(newVehicleValue).replace(oldVehicleValue).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionBuilder.failResult(proposedValue);
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Entity value) {
        return offerToStore(container, new SpongeValue<Entity>(Keys.BASE_VEHICLE, value, value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container, Function<Entity, Entity> function) {
        if (container instanceof net.minecraft.entity.Entity) {
            final Entity oldVehicle = getValueFromContainer(container).get();
            final Entity newVehicle = checkNotNull(function.apply(oldVehicle));
            return offerToStore(container, newVehicle);
        } else {
            return DataTransactionBuilder.failNoData();
        }
    }
}
