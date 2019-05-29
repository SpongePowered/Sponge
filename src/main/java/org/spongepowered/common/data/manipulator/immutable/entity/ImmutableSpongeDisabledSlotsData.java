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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDisabledSlotsData;
import org.spongepowered.api.data.manipulator.mutable.entity.DisabledSlotsData;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeDisabledSlotsData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;

import java.util.Collections;
import java.util.Set;

public class ImmutableSpongeDisabledSlotsData extends AbstractImmutableData<ImmutableDisabledSlotsData, DisabledSlotsData> implements ImmutableDisabledSlotsData {

    private Set<EquipmentType> takingDisabled;
    private Set<EquipmentType> placingDisabled;
    private ImmutableSetValue<EquipmentType> takingDisabledValue;
    private ImmutableSetValue<EquipmentType> placingDisabledValue;

    public ImmutableSpongeDisabledSlotsData() {
        this(Collections.emptySet(), Collections.emptySet());
    }

    public ImmutableSpongeDisabledSlotsData(Set<EquipmentType> takingDisabled, Set<EquipmentType> placingDisabled) {
        super(ImmutableDisabledSlotsData.class);

        this.takingDisabled = takingDisabled;
        this.placingDisabled = placingDisabled;

        this.takingDisabledValue = new ImmutableSpongeSetValue<>(Keys.ARMOR_STAND_TAKING_DISABLED, this.takingDisabled);
        this.placingDisabledValue = new ImmutableSpongeSetValue<>(Keys.ARMOR_STAND_PLACING_DISABLED, this.placingDisabled);

        registerGetters();
    }


    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.ARMOR_STAND_TAKING_DISABLED, () -> this.takingDisabled);
        registerKeyValue(Keys.ARMOR_STAND_TAKING_DISABLED, () -> this.takingDisabledValue);

        registerFieldGetter(Keys.ARMOR_STAND_PLACING_DISABLED, () -> this.placingDisabled);
        registerKeyValue(Keys.ARMOR_STAND_PLACING_DISABLED, () -> this.placingDisabledValue);
    }

    @Override
    public SpongeDisabledSlotsData asMutable() {
        return new SpongeDisabledSlotsData(this.takingDisabled, this.placingDisabled);
    }
    @Override
    public ImmutableSetValue<EquipmentType> takingDisabled() {
        return this.takingDisabledValue;
    }

    @Override
    public ImmutableSetValue<EquipmentType> placingDisabled() {
        return this.placingDisabledValue;
    }
}
