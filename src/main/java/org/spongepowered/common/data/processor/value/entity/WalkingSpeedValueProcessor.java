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

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.entity.player.PlayerCapabilitiesAccessor;

import java.util.Optional;

public class WalkingSpeedValueProcessor extends AbstractSpongeValueProcessor<EntityPlayer, Double, Value<Double>> {

    public WalkingSpeedValueProcessor() {
        super(EntityPlayer.class, Keys.WALKING_SPEED);
    }

    @Override
    protected Value<Double> constructValue(final Double defaultValue) {
        return new SpongeValue<>(Keys.WALKING_SPEED, 0.7D);
    }

    @Override
    protected ImmutableValue<Double> constructImmutableValue(final Double value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected boolean set(final EntityPlayer container, final Double value) {
        setWalkSpeed(container, value);
        container.func_71016_p();
        return true;
    }

    @Override
    protected Optional<Double> getVal(final EntityPlayer container) {
        return Optional.of(((double) container.field_71075_bZ.func_75094_b()));
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    public static void setWalkSpeed(final EntityPlayer container, final double value) {
        ((PlayerCapabilitiesAccessor) container.field_71075_bZ).accessor$setWalkSpeed((float) value);
        final IAttributeInstance attribute = container.func_110148_a(SharedMonsterAttributes.field_111263_d);
        attribute.func_111128_a(value);
    }
}
