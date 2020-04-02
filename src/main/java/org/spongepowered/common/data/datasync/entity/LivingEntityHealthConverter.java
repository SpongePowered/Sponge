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
import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;

import java.util.List;
import java.util.Optional;

public class LivingEntityHealthConverter extends DataParameterConverter<Float> {

    public LivingEntityHealthConverter() {
        super(LivingEntityAccessor.accessor$getHealth());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final Float currentValue, final Float value) {
        final float maxHealth = entity instanceof ServerPlayerEntityBridge
                                ? (float) ((ServerPlayerEntityBridge) entity).bridge$getHealthScale()
                                // Players scale their health and send it through the
                                // data provider, only the server knows their actual max health
                                : ((LivingEntity) entity).getMaxHealth();
        return Optional.of(DataTransactionResult.builder()
                .replace(BoundedValue.immutableOf(Keys.HEALTH, currentValue.doubleValue(), 0.0, (double) maxHealth))
                .success(BoundedValue.immutableOf(Keys.HEALTH, value.doubleValue(), 0.0, (double) maxHealth))
                .result(DataTransactionResult.Type.SUCCESS)
                .build());
    }

    @Override
    public Float getValueFromEvent(final Float originalValue, final List<Immutable<?>> immutableValues) {
        for (final Immutable<?> immutableValue : immutableValues) {
            if (immutableValue.getKey() == Keys.HEALTH.get()) {
                return (Float) immutableValue.get();
            }
        }
        return originalValue;
    }
}
