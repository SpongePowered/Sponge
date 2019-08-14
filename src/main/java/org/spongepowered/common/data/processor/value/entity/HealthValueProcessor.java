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

import static org.spongepowered.common.data.util.ComparatorUtil.doubleComparator;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.health.HealingTypes;
import org.spongepowered.api.event.entity.RegainHealthEvent;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.bridge.entity.LivingEntityBaseBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;

import java.util.Optional;

public class HealthValueProcessor extends AbstractSpongeValueProcessor<EntityLivingBase, Double, MutableBoundedValue<Double>> {

    public HealthValueProcessor() {
        super(EntityLivingBase.class, Keys.HEALTH);
    }

    @Override
    public MutableBoundedValue<Double> constructValue(final Double health) {
        return SpongeValueFactory.boundedBuilder(Keys.HEALTH)
            .comparator(doubleComparator())
            .minimum(0D)
            .maximum(((Float) Float.MAX_VALUE).doubleValue())
            .defaultValue(20D)
            .actualValue(health)
            .build();
    }

    @Override
    protected boolean set(final EntityLivingBase container, final Double value) {
        return false;
    }

    @Override
    protected Optional<Double> getVal(final EntityLivingBase container) {
        return Optional.of((double) container.getHealth());
    }

    @Override
    protected ImmutableBoundedValue<Double> constructImmutableValue(final Double value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public Optional<MutableBoundedValue<Double>> getApiValueFromContainer(final ValueContainer<?> container) {
        if (container instanceof EntityLivingBase) {
            final double health = ((EntityLivingBase) container).getHealth();
            final double maxHealth = ((EntityLivingBase) container).getMaxHealth();
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
    public boolean supports(final ValueContainer<?> container) {
        return container instanceof EntityLivingBase;
    }

    @Override
    public DataTransactionResult offerToStore(final ValueContainer<?> container, Double value) {
        final ImmutableBoundedValue<Double> proposedValue = constructImmutableValue(value);
        if (!(container instanceof EntityLivingBase)) {
            return DataTransactionResult.failResult(proposedValue);
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        final EntityLivingBase livingbase = (EntityLivingBase) container;
        final double maxHealth = livingbase.getMaxHealth();
        final float current = livingbase.getHealth();
        if (ShouldFire.REGAIN_HEALTH_EVENT && current < value) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                if (!frame.getCurrentContext().containsKey(EventContextKeys.HEALING_TYPE)) {
                    frame.addContext(EventContextKeys.HEALING_TYPE, HealingTypes.PLUGIN);
                }
                final RegainHealthEvent event =
                    SpongeEventFactory.createRegainHealthEvent(frame.getCurrentCause(), (Living) container, value - current);
                if (event.isCancelled()) {
                    final ImmutableBoundedValue<Double> rejected = SpongeValueFactory.boundedBuilder(Keys.HEALTH)
                        .defaultValue(maxHealth)
                        .minimum(0D)
                        .maximum(maxHealth)
                        .actualValue(value)
                        .build().asImmutable();
                    return DataTransactionResult.builder()
                        .reject(rejected)
                        .result(DataTransactionResult.Type.CANCELLED)
                        .build();
                }
                value = current + event.getAmountToRegain();
            }
        }
        final ImmutableBoundedValue<Double> newHealthValue = SpongeValueFactory.boundedBuilder(Keys.HEALTH)
            .defaultValue(maxHealth)
            .minimum(0D)
            .maximum(maxHealth)
            .actualValue(value)
            .build()
            .asImmutable();
        final ImmutableBoundedValue<Double> oldHealthValue = SpongeValueFactory.boundedBuilder(Keys.HEALTH)
            .defaultValue(maxHealth)
            .minimum(0D)
            .maximum(maxHealth)
            .actualValue((double) current)
            .build()
            .asImmutable();
        if (value > maxHealth) {
            return DataTransactionResult.errorResult(newHealthValue);
        }
        try {
            livingbase.setHealth(value.floatValue());
        } catch (final Exception e) {
            return DataTransactionResult.errorResult(newHealthValue);
        }
        if (value.floatValue() <= 0.0F) {
            livingbase.attackEntityFrom(DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE, 1000F);
        } else {
            ((LivingEntityBaseBridge) livingbase).bridge$resetDeathEventsPosted();
        }
        return builder.success(newHealthValue).replace(oldHealthValue).result(DataTransactionResult.Type.SUCCESS).build();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
