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
package org.spongepowered.common.data.provider.entity.armorstand;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.accessor.entity.item.ArmorStandEntityAccessor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArmorStandEntityPlacingDisabledProvider extends GenericMutableDataProvider<ArmorStandEntity, Set<EquipmentType>> {

    public ArmorStandEntityPlacingDisabledProvider() {
        super(Keys.ARMOR_STAND_PLACING_DISABLED);
    }

    @Override
    protected Optional<Set<EquipmentType>> getFrom(ArmorStandEntity dataHolder) {
        // include all chunk
        final int disabled = ((ArmorStandEntityAccessor) dataHolder).accessor$getDisabledSlots();
        final int resultantChunk = ((disabled >> 16) & 0b1111_1111) | (disabled & 0b1111_1111);

        final Set<EquipmentType> val = new HashSet<>();

        if ((resultantChunk & (1 << 1)) != 0) val.add(EquipmentTypes.BOOTS.get());
        if ((resultantChunk & (1 << 2)) != 0) val.add(EquipmentTypes.LEGGINGS.get());
        if ((resultantChunk & (1 << 3)) != 0) val.add(EquipmentTypes.CHESTPLATE.get());
        if ((resultantChunk & (1 << 4)) != 0) val.add(EquipmentTypes.HEADWEAR.get());

        return Optional.of(val);
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Set<EquipmentType> value) {
        int chunk = 0;

        int disabledSlots = ((ArmorStandEntityAccessor) dataHolder).accessor$getDisabledSlots();
        // try and keep the all chunk empty
        final int allChunk = disabledSlots & 0b1111_1111;
        if (allChunk != 0) {
            disabledSlots |= (allChunk << 16);
            disabledSlots ^= 0b1111_1111;

        }

        if (value.contains(EquipmentTypes.BOOTS.get())) chunk |= 1 << 1;
        if (value.contains(EquipmentTypes.LEGGINGS.get())) chunk |= 1 << 2;
        if (value.contains(EquipmentTypes.CHESTPLATE.get())) chunk |= 1 << 3;
        if (value.contains(EquipmentTypes.HEADWEAR.get())) chunk |= 1 << 4;

        disabledSlots |= (chunk << 16);
        ((ArmorStandEntityAccessor) dataHolder).accessor$setDisabledSlots(disabledSlots);

        return true;
    }
}
