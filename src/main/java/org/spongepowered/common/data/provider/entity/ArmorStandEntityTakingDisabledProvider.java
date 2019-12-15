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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArmorStandEntityTakingDisabledProvider extends GenericMutableDataProvider<ArmorStandEntity, Set<EquipmentType>> {

    public ArmorStandEntityTakingDisabledProvider() {
        super(Keys.ARMOR_STAND_TAKING_DISABLED);
    }

    @Override
    protected Optional<Set<EquipmentType>> getFrom(ArmorStandEntity dataHolder) {
        // include all chunk
        final int disabled = ((ArmorStandEntityAccessor) dataHolder).accessor$getDisabledSlots();
        final int resultantChunk = ((disabled >> 16) & 0b1111_1111) | (disabled & 0b1111_1111);

        final Set<EquipmentType> val = new HashSet<>();

        if ((resultantChunk & (1 << 1)) != 0) val.add(EquipmentTypes.BOOTS);
        if ((resultantChunk & (1 << 2)) != 0) val.add(EquipmentTypes.LEGGINGS);
        if ((resultantChunk & (1 << 3)) != 0) val.add(EquipmentTypes.CHESTPLATE);
        if ((resultantChunk & (1 << 4)) != 0) val.add(EquipmentTypes.HEADWEAR);

        return Optional.of(val);
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Set<EquipmentType> value) {
        int chunk = 0;

        // try and keep the all chunk empty
        int disabledSlots = ((ArmorStandEntityAccessor) dataHolder).accessor$getDisabledSlots();
        final int allChunk = disabledSlots & 0b1111_1111;
        if (allChunk != 0) {
            disabledSlots |= (allChunk << 16);
            disabledSlots ^= 0b1111_1111;
            ((ArmorStandEntityAccessor) dataHolder).accessor$setDisabledSlots(disabledSlots);
        }

        if (value.contains(EquipmentTypes.BOOTS)) chunk |= 1 << 1;
        if (value.contains(EquipmentTypes.LEGGINGS)) chunk |= 1 << 2;
        if (value.contains(EquipmentTypes.CHESTPLATE)) chunk |= 1 << 3;
        if (value.contains(EquipmentTypes.HEADWEAR)) chunk |= 1 << 4;

        disabledSlots |= (chunk << 8);
        ((ArmorStandEntityAccessor) dataHolder).accessor$setDisabledSlots(disabledSlots);

        return true;
    }
}
