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

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class FireTicksValueProcessor extends AbstractSpongeValueProcessor<Entity, Integer, MutableBoundedValue<Integer>> {

    public FireTicksValueProcessor() {
        super(Entity.class, Keys.FIRE_TICKS);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (container instanceof Entity) {
            if (((EntityAccessor) container).accessor$getFire() >= Constants.Entity.MINIMUM_FIRE_TICKS) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                builder.replace(getApiValueFromContainer(container).get().asImmutable());
                builder.replace(container.getValue(Keys.FIRE_DAMAGE_DELAY).get().asImmutable());
                ((Entity) container).func_70066_B();
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(final Integer defaultValue) {
        return SpongeValueFactory.boundedBuilder(Keys.FIRE_TICKS)
            .defaultValue(Constants.Entity.DEFAULT_FIRE_TICKS)
            .minimum(Constants.Entity.MINIMUM_FIRE_TICKS)
            .maximum(Integer.MAX_VALUE)
            .actualValue(defaultValue)
            .build();
    }

    @Override
    protected boolean set(final Entity container, final Integer value) {
        ((EntityAccessor) container).accessor$setFire( value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final Entity container) {
        if (((EntityAccessor) container).accessor$getFire() > 0) {
            return Optional.of(((EntityAccessor) container).accessor$getFire());
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return constructValue(value).asImmutable();
    }

}
