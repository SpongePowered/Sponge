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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;

import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;

public class ExperienceLevelValueProcessor extends AbstractSpongeValueProcessor<PlayerEntity, Integer, MutableBoundedValue<Integer>> {

    public ExperienceLevelValueProcessor() {
        super(PlayerEntity.class, Keys.EXPERIENCE_LEVEL);
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Integer value) {
        final ImmutableBoundedValue<Integer> newValue = constructImmutableValue(value);
        if (supports(container)) {
            final PlayerEntity player = (PlayerEntity) container;
            final Integer oldValue = player.field_71068_ca;
            player.field_71067_cb = ExperienceHolderUtils.xpAtLevel(value);
            player.field_71106_cc = 0;
            player.field_71068_ca = value;
            ((EntityPlayerMPBridge) container).bridge$refreshExp();
            final ImmutableBoundedValue<Integer> oldImmutableValue = constructImmutableValue(oldValue);
            return DataTransactionResult.successReplaceResult(newValue, oldImmutableValue);
        }

        return DataTransactionResult.failResult(newValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public MutableBoundedValue<Integer> constructValue(Integer defaultValue) {
        return SpongeValueFactory.boundedBuilder(Keys.EXPERIENCE_LEVEL)
            .defaultValue(0)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .actualValue(defaultValue)
            .build();
    }

    @Override
    protected boolean set(PlayerEntity container, Integer value) {
        return false;
    }

    @Override
    protected Optional<Integer> getVal(PlayerEntity container) {
        return Optional.of(container.field_71068_ca);
    }

    @Override
    protected ImmutableBoundedValue<Integer> constructImmutableValue(Integer value) {
        return constructValue(value).asImmutable();
    }
}
