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
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;
import org.spongepowered.common.mixin.core.entity.item.EntityArmorStandAccessor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.item.ArmorStandEntity;

public class PlacingDisabledValueProcessor
		extends AbstractSpongeValueProcessor<ArmorStandEntity, Set<EquipmentType>, SetValue<EquipmentType>> {

	public PlacingDisabledValueProcessor() {
		super(ArmorStandEntity.class, Keys.ARMOR_STAND_TAKING_DISABLED);
	}

	@Override
	public DataTransactionResult offerToStore(final ValueContainer<?> container, final Set<EquipmentType> value) {
		try {
			set((ArmorStandEntity) container, value);
			return DataTransactionResult.successNoData();
		} catch (Exception e) {
			return DataTransactionResult.failNoData();
		}
	}

	@Override
	public DataTransactionResult removeFrom(final ValueContainer<?> container) {
		return DataTransactionResult.failNoData();
	}

	@Override
	protected SetValue<EquipmentType> constructValue(final Set<EquipmentType> actualValue) {
		return new SpongeSetValue<EquipmentType>(this.key, actualValue);
	}

	@Override
	protected boolean set(final ArmorStandEntity container, final Set<EquipmentType> value) {
		int chunk = 0;

		int disabledSlots = ((EntityArmorStandAccessor) container).accessor$getDisabledSlots();
		// try and keep the all chunk empty
		final int allChunk = disabledSlots & 0b1111_1111;
		if (allChunk != 0) {
			disabledSlots |= (allChunk << 16);
			disabledSlots ^= 0b1111_1111;

		}
		
		if (value.contains(EquipmentTypes.BOOTS)) chunk |= 1 << 1;
		if (value.contains(EquipmentTypes.LEGGINGS)) chunk |= 1 << 2;
		if (value.contains(EquipmentTypes.CHESTPLATE)) chunk |= 1 << 3;
		if (value.contains(EquipmentTypes.HEADWEAR)) chunk |= 1 << 4;
		
		disabledSlots |= (chunk << 816);

		((EntityArmorStandAccessor) container).accessor$setDisabledSlots(disabledSlots);
		
		return true;
	}

	@Override
	protected Optional<Set<EquipmentType>> getVal(final ArmorStandEntity container) {
		// include all chunk
		final int disabled = ((EntityArmorStandAccessor) container).accessor$getDisabledSlots();
		final int resultantChunk = ((disabled >> 16) & 0b1111_1111) | (disabled & 0b1111_1111);
		
		final HashSet<EquipmentType> val = new HashSet<EquipmentType>();
		
		if ((resultantChunk & (1 << 1)) != 0) val.add(EquipmentTypes.BOOTS);
		if ((resultantChunk & (1 << 2)) != 0) val.add(EquipmentTypes.LEGGINGS);
		if ((resultantChunk & (1 << 3)) != 0) val.add(EquipmentTypes.CHESTPLATE);
		if ((resultantChunk & (1 << 4)) != 0) val.add(EquipmentTypes.HEADWEAR);
		
		return Optional.of(val);
	}

	@Override
	protected ImmutableValue<Set<EquipmentType>> constructImmutableValue(final Set<EquipmentType> value) {
		return ImmutableSpongeValue.cachedOf(this.key, Collections.emptySet(), value);
	}

}
