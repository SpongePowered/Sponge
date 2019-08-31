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
import org.spongepowered.common.mixin.core.entity.EntityLivingAccessor;

import java.util.List;
import java.util.Optional;

public class EntityLivingAIFlagsConverter extends DataParameterConverter<Byte> {

    public EntityLivingAIFlagsConverter() {
        super(EntityLivingAccessor.accessor$getAiFlagsParameter());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final Byte currentValue, final Byte value) {

        final boolean oldHasAi = (currentValue.byteValue() & 1) == 0;
        final boolean newHasAi = (value.byteValue() & 1) == 0;
        return Optional.of(DataTransactionResult.builder()
            .replace(ImmutableSpongeValue.cachedOf(Keys.AI_ENABLED, true, oldHasAi))
            .success(ImmutableSpongeValue.cachedOf(Keys.AI_ENABLED, true, newHasAi))
            .result(DataTransactionResult.Type.SUCCESS)
            .build());
    }

    @Override
    public Byte getValueFromEvent(Byte originalValue, final List<ImmutableValue<?>> immutableValues) {
        for (final ImmutableValue<?> immutableValue : immutableValues) {
            if (immutableValue.getKey() == Keys.AI_ENABLED) {
                final Boolean hasAi = (Boolean) immutableValue.get();
                originalValue = Byte.valueOf(hasAi ? (byte) (originalValue.byteValue() & -2) : (byte) (originalValue.byteValue() | 1));
            }
        }
        return originalValue;
    }
}
