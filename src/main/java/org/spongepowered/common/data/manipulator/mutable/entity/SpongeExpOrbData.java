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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpOrbData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpOrbData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExpOrbData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

public class SpongeExpOrbData extends AbstractSingleData<Integer, ExpOrbData, ImmutableExpOrbData> implements ExpOrbData {

	public SpongeExpOrbData(Integer value) {
		super(ExpOrbData.class, value, Keys.CONTAINED_EXPERIENCE);
	}

	public SpongeExpOrbData() {
		this(0);
	}

	@Override
	protected Value<?> getValueGetter() {
		return experience();
	}

	@Override
	public ExpOrbData copy() {
		return new SpongeExpOrbData(this.getValue());
	}

	@Override
	public ImmutableExpOrbData asImmutable() {
		return new ImmutableSpongeExpOrbData(this.getValue());
	}

	@Override
	public int compareTo(ExpOrbData o) {
		return o.experience().get().compareTo(this.getValue());
	}

	@Override
	public Value<Integer> experience() {
		return new SpongeBoundedValue<>(Keys.CONTAINED_EXPERIENCE, 0, intComparator(), 0, Integer.MAX_VALUE, this.getValue());
	}

	@Override
	public DataContainer toContainer() {
		return new MemoryDataContainer().set(Keys.CONTAINED_EXPERIENCE, this.getValue());
	}

}
