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
package org.spongepowered.common.data.processor.data;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableWetData;
import org.spongepowered.api.data.manipulator.mutable.WetData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.data.manipulator.mutable.SpongeWetData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import com.google.common.base.Optional;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;

public class WetDataProcessor extends AbstractSpongeDataProcessor<WetData, ImmutableWetData> {

	@Override
	public boolean supports(DataHolder dataHolder) {
		return dataHolder instanceof EntityWolf || dataHolder instanceof ItemStack;
	}

	@Override
	public Optional<WetData> from(DataHolder dataHolder) {
		if (this.supports(dataHolder)) {
			// incomplete
			SpongeWetData wetData = new SpongeWetData(dataHolder.get(Keys.IS_WET).get());
			
			return Optional.<WetData>of(wetData);
		}
		return Optional.absent();
	}

	@Override
	public Optional<WetData> createFrom(DataHolder dataHolder) {
		return Optional.absent();
	}

	@Override
	public Optional<WetData> fill(DataHolder dataHolder, WetData manipulator, MergeFunction overlap) {
		if (this.supports(dataHolder)) {
			WetData merged = overlap.merge(checkNotNull(manipulator.copy()), this.from(dataHolder).get());
            return Optional.of(manipulator.set(Keys.IS_WET, merged.wet().get()));
        }
        return Optional.absent();
	}

	@Override
	public Optional<WetData> fill(DataContainer container, WetData m) {
		return Optional.absent();
	}

	@Override
	public DataTransactionResult set(DataHolder dataHolder, WetData manipulator, MergeFunction function) {
		return null;
	}

	@Override
	public Optional<ImmutableWetData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableWetData immutable) {
		return Optional.absent();
	}

	@Override
	public DataTransactionResult remove(DataHolder dataHolder) {
		return null;
	}

}
