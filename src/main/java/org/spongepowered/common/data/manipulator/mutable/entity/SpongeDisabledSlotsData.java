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
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDisabledSlotsData;
import org.spongepowered.api.data.manipulator.mutable.entity.DisabledSlotsData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeDisabledSlotsData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;

import java.util.HashSet;
import java.util.Set;

public class SpongeDisabledSlotsData extends AbstractData<DisabledSlotsData, ImmutableDisabledSlotsData> implements DisabledSlotsData {

    private Set<EquipmentType> takingDisabled;
    private Set<EquipmentType> placingDisabled;

    public SpongeDisabledSlotsData() {
        this(new HashSet<>(), new HashSet<>());
    }

    public SpongeDisabledSlotsData(Set<EquipmentType> takingDisabled, Set<EquipmentType> placingDisabled) {
        super(DisabledSlotsData.class);

        this.takingDisabled = takingDisabled;
        this.placingDisabled = placingDisabled;

        registerGettersAndSetters();
    }


    @Override
    public SetValue<EquipmentType> takingDisabled() {
        return new SpongeSetValue<>(Keys.ARMOR_STAND_TAKING_DISABLED, new HashSet<>(), this.takingDisabled);
    }

    @Override
    public SetValue<EquipmentType> placingDisabled() {
        return new SpongeSetValue<>(Keys.ARMOR_STAND_PLACING_DISABLED, new HashSet<>(), this.placingDisabled);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.ARMOR_STAND_TAKING_DISABLED, () -> this.takingDisabled);
        registerFieldSetter(Keys.ARMOR_STAND_TAKING_DISABLED, (takingDisabled) -> this.takingDisabled = takingDisabled);
        registerKeyValue(Keys.ARMOR_STAND_TAKING_DISABLED, this::takingDisabled);

        registerFieldGetter(Keys.ARMOR_STAND_PLACING_DISABLED, () -> this.placingDisabled);
        registerFieldSetter(Keys.ARMOR_STAND_PLACING_DISABLED, (placingDisabled) -> this.placingDisabled = placingDisabled);
        registerKeyValue(Keys.ARMOR_STAND_PLACING_DISABLED, this::placingDisabled);
    }

    @Override
    public DisabledSlotsData copy() {
        return new SpongeDisabledSlotsData(this.takingDisabled, this.placingDisabled);
    }
    
    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.ARMOR_STAND_TAKING_DISABLED, this.takingDisabled)
                .set(Keys.ARMOR_STAND_PLACING_DISABLED, this.placingDisabled);
    }

    @Override
    public ImmutableDisabledSlotsData asImmutable() {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDisabledSlotsData.class, this.takingDisabled, this.placingDisabled);
    }
}
