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

import static org.spongepowered.common.util.Constants.Functional.doubleComparator;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.bridge.entity.LivingEntityBaseBridge;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public class HealthValueProcessor extends AbstractSpongeValueProcessor<LivingEntity, Double, MutableBoundedValue<Double>> {

    public HealthValueProcessor() {
        super(LivingEntity.class, Keys.HEALTH);
    }

    @Override
    public MutableBoundedValue<Double> constructValue(Double health) {
        return SpongeValueFactory.boundedBuilder(Keys.HEALTH)
            .comparator(doubleComparator())
            .minimum(0D)
            .maximum(((Float) Float.MAX_VALUE).doubleValue())
            .defaultValue(20D)
            .actualValue(health)
            .build();
    }

    @Override
    protected boolean set(LivingEntity container, Double value) {
        return false;
    }

    @Override
    protected Optional<Double> getVal(LivingEntity container) {
        return Optional.of((double) container.getHealth());
    }

    @Override
    protected ImmutableBoundedValue<Double> constructImmutableValue(Double value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public Optional<MutableBoundedValue<Double>> getApiValueFromContainer(ValueContainer<?> container) {
        if (container instanceof LivingEntity) {
            final double health = ((LivingEntity) container).getHealth();
            final double maxHealth = ((LivingEntity) container).getMaxHealth();
            return Optional.of(SpongeValueFactory.boundedBuilder(Keys.HEALTH)
                .minimum(0D)
                .maximum(maxHealth)
                .defaultValue(maxHealth)
                .actualValue(health)
                .build());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof LivingEntity;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Double value) {
        final ImmutableBoundedValue<Double> proposedValue = constructImmutableValue(value);
        if (container instanceof LivingEntity) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final LivingEntity livingbase = (LivingEntity) container;
            final double maxHealth = livingbase.getMaxHealth();
            final ImmutableBoundedValue<Double> newHealthValue = SpongeValueFactory.boundedBuilder(Keys.HEALTH)
                .defaultValue(maxHealth)
                .minimum(0D)
                .maximum(maxHealth)
                .actualValue(value)
                .build()
                .asImmutable();
            final ImmutableBoundedValue<Double> oldHealthValue = getApiValueFromContainer(container).get().asImmutable();
            if (value > maxHealth) {
                return DataTransactionResult.errorResult(newHealthValue);
            }
            try {
                livingbase.setHealth(value.floatValue());
            } catch (Exception e) {
                return DataTransactionResult.errorResult(newHealthValue);
            }
            if (value.floatValue() <= 0.0F) {
                livingbase.attackEntityFrom(DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE, 1000F);
            } else {
                ((LivingEntityBaseBridge) livingbase).bridge$resetDeathEventsPosted();
            }
            return builder.success(newHealthValue).replace(oldHealthValue).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failResult(proposedValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
