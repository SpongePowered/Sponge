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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class SneakingValueProcessor implements ValueProcessor<Boolean, Value<Boolean>> {

	@Override
	public Key<? extends BaseValue<Boolean>> getKey() {
		return Keys.IS_SNEAKING;
	}

	@Override
	public Optional<Boolean> getValueFromContainer(ValueContainer<?> container) {
		if (container instanceof Entity) {
            return Optional.of(Boolean.valueOf(((Entity) container).isSneaking()));
        }
        return Optional.absent();
	}

	@Override
	public Optional<Value<Boolean>> getApiValueFromContainer(ValueContainer<?> container) {
		if (container instanceof Entity) {
            final boolean sneaking = ((Entity) container).isSneaking();
            return Optional.<Value<Boolean>>of(new SpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, Boolean.valueOf(sneaking)));
        }
        return Optional.absent();
	}

	@Override
	public boolean supports(ValueContainer<?> container) {
		return container instanceof Entity;
	}

	@Override
	public DataTransactionResult transform(ValueContainer<?> container, Function<Boolean, Boolean> function) {
		if (container instanceof Entity) {
            final Boolean old = getValueFromContainer(container).get();
            final ImmutableValue<Boolean> oldValue = new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, old);
            final Boolean sneaking = checkNotNull(checkNotNull(function, "function").apply(old), "The function returned a null value!");
            final ImmutableValue<Boolean> newVal = new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, sneaking);
            try {
                ((Entity) container).setSneaking(sneaking);
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newVal);
            }
            return DataTransactionBuilder.successReplaceResult(newVal, oldValue);
        }
        return DataTransactionBuilder.failNoData();
	}

	@Override
	public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
		return offerToStore(container, ((Boolean) value.get()));
	}

	@Override
	public DataTransactionResult offerToStore(ValueContainer<?> container, Boolean value) {
		final ImmutableValue<Boolean> newValue = new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, value);
        if (container instanceof Entity) {
            final Boolean old = getValueFromContainer(container).get();
            final ImmutableValue<Boolean> oldValue = new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, old);
            try {
                ((Entity) container).setSneaking(value);
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failResult(newValue);
	}

	@Override
	public DataTransactionResult removeFrom(ValueContainer<?> container) {
		return DataTransactionBuilder.failNoData();
	}

}
