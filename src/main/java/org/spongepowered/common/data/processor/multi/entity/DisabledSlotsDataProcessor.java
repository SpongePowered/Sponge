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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDisabledSlotsData;
import org.spongepowered.api.data.manipulator.mutable.entity.DisabledSlotsData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeDisabledSlotsData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;
import org.spongepowered.common.mixin.core.entity.item.EntityArmorStandAccessor;

import java.util.*;

public class DisabledSlotsDataProcessor extends
    AbstractMultiDataSingleTargetProcessor<EntityArmorStandAccessor, DisabledSlotsData, ImmutableDisabledSlotsData> {

    public DisabledSlotsDataProcessor() {
        super(EntityArmorStandAccessor.class);
    }

    private int populateChunkFromSet(final Set<EquipmentType> value) {
        int chunk = 0;

        if (value.contains(EquipmentTypes.BOOTS)) chunk |= 1 << 1;
        if (value.contains(EquipmentTypes.LEGGINGS)) chunk |= 1 << 2;
        if (value.contains(EquipmentTypes.CHESTPLATE)) chunk |= 1 << 3;
        if (value.contains(EquipmentTypes.HEADWEAR)) chunk |= 1 << 4;
        return chunk;
    }

    @Override
    protected boolean doesDataExist(final EntityArmorStandAccessor dataHolder) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(final EntityArmorStandAccessor dataHolder, final Map<Key<?>, Object> keyValues) {
        int disabledSlots = 0;

        if (keyValues.containsKey(Keys.ARMOR_STAND_TAKING_DISABLED)) {
            final Set<EquipmentType> takingDisabled = (Set<EquipmentType>) keyValues.get(Keys.ARMOR_STAND_TAKING_DISABLED);
            disabledSlots |= (populateChunkFromSet(takingDisabled) << 8);
        }

        if (keyValues.containsKey(Keys.ARMOR_STAND_PLACING_DISABLED)) {
            final Set<EquipmentType> placingDisabled = (Set<EquipmentType>) keyValues.get(Keys.ARMOR_STAND_PLACING_DISABLED);
            disabledSlots |= (populateChunkFromSet(placingDisabled) << 16);
        }

        dataHolder.accessor$setDisabledSlots(disabledSlots);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final EntityArmorStandAccessor dataHolder) {
        // try and keep the all chunk empty
        int disabledSlotsValue = dataHolder.accessor$getDisabledSlots();
        final int allChunk = disabledSlotsValue & 0b1111_1111;
        if (allChunk != 0) {
            disabledSlotsValue |= (allChunk << 8);
            disabledSlotsValue |= (allChunk << 16);
            disabledSlotsValue ^= 0b1111_1111;
            dataHolder.accessor$setDisabledSlots(disabledSlotsValue);
        }

        final int disabledSlots = dataHolder.accessor$getDisabledSlots();

        final Set<EquipmentType> takingDisabled = new HashSet<>();
        if (((disabledSlots >> 1 + 8) & 1) != 0) takingDisabled.add(EquipmentTypes.BOOTS);
        if (((disabledSlots >> 2 + 8) & 1) != 0) takingDisabled.add(EquipmentTypes.LEGGINGS);
        if (((disabledSlots >> 3 + 8) & 1) != 0) takingDisabled.add(EquipmentTypes.CHESTPLATE);
        if (((disabledSlots >> 4 + 8) & 1) != 0) takingDisabled.add(EquipmentTypes.HEADWEAR);

        final Set<EquipmentType> placingDisabled = new HashSet<>();
        if (((disabledSlots >> 1 + 16) & 1) != 0) placingDisabled.add(EquipmentTypes.BOOTS);
        if (((disabledSlots >> 2 + 16) & 1) != 0) placingDisabled.add(EquipmentTypes.LEGGINGS);
        if (((disabledSlots >> 3 + 16) & 1) != 0) placingDisabled.add(EquipmentTypes.CHESTPLATE);
        if (((disabledSlots >> 4 + 16) & 1) != 0) placingDisabled.add(EquipmentTypes.HEADWEAR);

        return ImmutableMap.<Key<?>, Object>builder()
                .put(Keys.ARMOR_STAND_PLACING_DISABLED, placingDisabled)
                .put(Keys.ARMOR_STAND_TAKING_DISABLED, takingDisabled)
                .build();
    }

    @Override
    protected DisabledSlotsData createManipulator() {
        return new SpongeDisabledSlotsData();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<DisabledSlotsData> fill(final DataContainer container, final DisabledSlotsData disabledSlotsData) {
        if (container.contains(Keys.ARMOR_STAND_TAKING_DISABLED)) {
            disabledSlotsData.set(Keys.ARMOR_STAND_TAKING_DISABLED,
                new HashSet<>((Collection<EquipmentType>) container.get(Keys.ARMOR_STAND_TAKING_DISABLED.getQuery()).get()));
        }
        if (container.contains(Keys.ARMOR_STAND_PLACING_DISABLED)) {
            disabledSlotsData.set(Keys.ARMOR_STAND_PLACING_DISABLED,
                new HashSet<>((Collection<EquipmentType>) container.get(Keys.ARMOR_STAND_PLACING_DISABLED.getQuery()).get()));
        }

        return Optional.of(disabledSlotsData);
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
