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

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;

public class FoodLevelValueProcessor extends AbstractSpongeValueProcessor<Integer, MutableBoundedValue<Integer>> {

    public FoodLevelValueProcessor() {
        super(Keys.FOOD_LEVEL);
    }

    @Override
    public Optional<Integer> getValueFromContainer(ValueContainer<?> container) {
        if (supports(container)) {
            final EntityPlayer player = (EntityPlayer) container;
            if (player.getFoodStats() != null) {
                return Optional.of(player.getFoodStats().getFoodLevel());
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<MutableBoundedValue<Integer>> getApiValueFromContainer(ValueContainer<?> container) {
        if (supports(container)) {
            final EntityPlayer player = (EntityPlayer) container;
            if (player.getFoodStats() != null) {
                return Optional.<MutableBoundedValue<Integer>>of(new SpongeBoundedValue<Integer>(Keys.FOOD_LEVEL, 20, intComparator(), 0,
                        Integer.MAX_VALUE, player.getFoodStats().getFoodLevel()));
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityPlayer;
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container, Function<Integer, Integer> function) {
        if (supports(container)) {
            final EntityPlayer player = (EntityPlayer) container;
            if (player.getFoodStats() != null) {
                final Integer oldValue = player.getFoodStats().getFoodLevel();
                final Integer newValue = function.apply(oldValue);
                player.getFoodStats().setFoodLevel(newValue);
                return DataTransactionBuilder.successReplaceResult(new ImmutableSpongeValue<Integer>(Keys.FOOD_LEVEL, newValue),
                        new ImmutableSpongeValue<Integer>(Keys.FOOD_LEVEL, oldValue));
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
        final Object object = value.get();
        if (object instanceof Number) {
            return offerToStore(container, ((Number) object).intValue());
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Integer value) {
        if (supports(container)) {
            final EntityPlayer player = (EntityPlayer) container;
            if (player.getFoodStats() != null) {
                final Integer oldValue = player.getFoodStats().getFoodLevel();
                player.getFoodStats().setFoodLevel(value);
                return DataTransactionBuilder.successReplaceResult(new ImmutableSpongeValue<Integer>(Keys.FOOD_LEVEL, value),
                        new ImmutableSpongeValue<Integer>(Keys.FOOD_LEVEL, oldValue));
            }
        }
        return DataTransactionBuilder.failResult(new ImmutableSpongeValue<Integer>(Keys.FOOD_LEVEL, value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

}
