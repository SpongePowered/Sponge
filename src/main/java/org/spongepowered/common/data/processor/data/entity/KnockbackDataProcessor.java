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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableKnockbackData;
import org.spongepowered.api.data.manipulator.mutable.entity.KnockbackData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeKnockbackData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.mixin.core.entity.projectile.EntityArrowAccessor;

import java.util.Optional;
import net.minecraft.entity.projectile.AbstractArrowEntity;

public class KnockbackDataProcessor
        extends AbstractEntitySingleDataProcessor<AbstractArrowEntity, Integer, MutableBoundedValue<Integer>, KnockbackData, ImmutableKnockbackData> {

    public KnockbackDataProcessor() {
        super(AbstractArrowEntity.class, Keys.KNOCKBACK_STRENGTH);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(final Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.KNOCKBACK_STRENGTH)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(0)
                .actualValue(actualValue)
                .build();
    }

    @Override
    protected boolean set(final AbstractArrowEntity entity, final Integer value) {
        entity.func_70240_a(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final AbstractArrowEntity entity) {
        return Optional.of(((EntityArrowAccessor) entity).accessor$getKnockbackStrength());
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return new ImmutableSpongeValue<>(Keys.KNOCKBACK_STRENGTH, value);
    }

    @Override
    protected KnockbackData createManipulator() {
        return new SpongeKnockbackData();
    }

}
