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

import net.minecraft.entity.monster.EntityCreeper;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class ChargedValueProcessor extends AbstractSpongeValueProcessor<EntityCreeper, Boolean, Value<Boolean>> {

	public ChargedValueProcessor() {
		super(EntityCreeper.class, Keys.CREEPER_CHARGED);
	}

	@Override
	protected Value<Boolean> constructValue(Boolean defaultValue) {
		return new SpongeValue<>(this.getKey(), defaultValue);
	}

	@Override
	protected boolean set(EntityCreeper container, Boolean value) {
		container.getDataWatcher().updateObject(17, (value ? (byte)1 : (byte)0));
		return true;
	}

	@Override
	protected Optional<Boolean> getVal(EntityCreeper container) {
		return Optional.of(container.getDataWatcher().getWatchableObjectByte(17) == 1);
	}

	@Override
	protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
		return new ImmutableSpongeValue<>(this.getKey(), value);
	}

	@Override
	public DataTransactionResult removeFrom(ValueContainer<?> container) {
		return DataTransactionBuilder.failNoData();
	}

}
