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
package org.spongepowered.common.data.datasync.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class EntityAirConverter extends DataParameterConverter<Integer> {

    public EntityAirConverter() {
        super(EntityAccessor.accessor$getAirParameter());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(Entity entity, Integer currentValue, Integer value) {
        return Optional.of(DataTransactionResult.builder()
            .replace(new ImmutableSpongeBoundedValue<Integer>(Keys.REMAINING_AIR, Constants.Sponge.Entity.DEFAULT_MAX_AIR, currentValue, Integer::compareTo, 0, Constants.Sponge.Entity.DEFAULT_MAX_AIR))
            .success(new ImmutableSpongeBoundedValue<Integer>(Keys.REMAINING_AIR, Constants.Sponge.Entity.DEFAULT_MAX_AIR, value, Integer::compareTo, 0, Constants.Sponge.Entity.DEFAULT_MAX_AIR))
            .result(DataTransactionResult.Type.SUCCESS)
            .build()
            );
    }

    @Override
    public Integer getValueFromEvent(Integer originalValue, List<ImmutableValue<?>> immutableValues) {
        for (ImmutableValue<?> value : immutableValues) {
            if (value.getKey() == Keys.REMAINING_AIR) {
                return (Integer) value.get();
            }
        }
        return originalValue;
    }
}
