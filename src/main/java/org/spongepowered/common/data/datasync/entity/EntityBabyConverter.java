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
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.mixin.core.entity.EntityAgeableAccessor;

import java.util.List;
import java.util.Optional;

public final class EntityBabyConverter extends DataParameterConverter<Boolean> {

    public EntityBabyConverter() {
        super(EntityAgeableAccessor.accessor$getBabyParameter());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final Boolean currentValue, final Boolean value) {
        return Optional.of(DataTransactionResult.builder()
            .replace(ImmutableSpongeValue.cachedOf(Keys.IS_ADULT, false, !currentValue))
            .success(ImmutableSpongeValue.cachedOf(Keys.IS_ADULT, false, !value))
            .result(DataTransactionResult.Type.SUCCESS)
            .build());
    }

    @Override
    public Boolean getValueFromEvent(final Boolean originalValue, final List<ImmutableValue<?>> immutableValues) {
        for (final ImmutableValue<?> value : immutableValues) {
            if (value.getKey() == Keys.IS_ADULT) {
                return !(Boolean) value.get();
            }
        }
        return originalValue;
    }
}
