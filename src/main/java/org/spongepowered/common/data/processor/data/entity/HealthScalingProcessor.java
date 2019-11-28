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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHealthScalingData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthScalingData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthScaleData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.entity.player.ServerPlayerEntity;

public class HealthScalingProcessor extends AbstractEntitySingleDataProcessor<ServerPlayerEntity, Double, MutableBoundedValue<Double>, HealthScalingData, ImmutableHealthScalingData> {

    public HealthScalingProcessor() {
        super(ServerPlayerEntity.class, Keys.HEALTH_SCALE);
    }

    @Override
    protected HealthScalingData createManipulator() {
        return new SpongeHealthScaleData();
    }

    @Override
    protected boolean set(ServerPlayerEntity dataHolder, Double value) {
        if (value < 1D) {
            return false;
        }
        if (value > Float.MAX_VALUE) {
            return false;
        }
        final EntityPlayerMPBridge mixinPlayer = (EntityPlayerMPBridge) dataHolder;
        mixinPlayer.bridge$setHealthScale(value);
        return true;
    }

    @Override
    protected Optional<Double> getVal(ServerPlayerEntity dataHolder) {
        final EntityPlayerMPBridge mixinPlayer = (EntityPlayerMPBridge) dataHolder;
        return Optional.ofNullable(mixinPlayer.bridge$isHealthScaled() ? mixinPlayer.bridge$getHealthScale() : null);
    }

    @Override
    protected ImmutableValue<Double> constructImmutableValue(Double value) {
        return SpongeValueFactory.boundedBuilder(Keys.HEALTH_SCALE)
                .minimum(1D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(Constants.Entity.Player.DEFAULT_HEALTH_SCALE)
                .actualValue(value)
                .build()
                .asImmutable();
    }

    @Override
    protected MutableBoundedValue<Double> constructValue(Double actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.HEALTH_SCALE)
                .minimum(1D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(Constants.Entity.Player.DEFAULT_HEALTH_SCALE)
                .actualValue(actualValue)
                .build();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof EntityPlayerMPBridge)) {
            return DataTransactionResult.failNoData();
        }
        final ImmutableValue<Double> current = constructImmutableValue(((EntityPlayerMPBridge) container).bridge$getHealthScale());
        ((EntityPlayerMPBridge) container).bridge$setHealthScale(Constants.Entity.Player.DEFAULT_HEALTH_SCALE);
        return DataTransactionResult.successRemove(current);
    }
}
