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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpirableData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpirableData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpirableData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.mixin.core.entity.monster.EntityEndermiteAccessor;

import java.util.Optional;
import net.minecraft.entity.monster.EndermiteEntity;

public class EndermiteExpirableDataProcessor extends
        AbstractEntitySingleDataProcessor<EndermiteEntity, Integer, MutableBoundedValue<Integer>, ExpirableData, ImmutableExpirableData> {

    public EndermiteExpirableDataProcessor() {
        super(EndermiteEntity.class, Keys.EXPIRATION_TICKS);
    }

    @Override
    protected ExpirableData createManipulator() {
        return new SpongeExpirableData(0, 2400);
    }

    @Override
    protected boolean set(final EndermiteEntity entity, final Integer value) {
        if (entity.isNoDespawnRequired()) {
            return false;
        }
        checkArgument(value >= 0);
        checkArgument(value <= 2400);
        ((EntityEndermiteAccessor) entity).accessor$setLifetime(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final EndermiteEntity entity) {
        return entity.isNoDespawnRequired() ? Optional.empty() : Optional.of(((EntityEndermiteAccessor) entity).accessor$getLifetime());
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(final Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.EXPIRATION_TICKS)
                .minimum(0)
                .maximum(2400)
                .defaultValue(0)
                .actualValue(actualValue)
                .build();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
