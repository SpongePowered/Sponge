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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableArmorStandData;
import org.spongepowered.api.data.manipulator.mutable.ArmorStandData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArmorStandData;
import org.spongepowered.common.data.value.SpongeImmutableValue;

public class ImmutableSpongeArmorStandData extends AbstractImmutableData<ImmutableArmorStandData, ArmorStandData> implements ImmutableArmorStandData {

    private final boolean marker;
    private final boolean small;
    private final boolean arms;
    private final boolean basePlate;
    private final Value.Immutable<Boolean> markerValue;
    private final Value.Immutable<Boolean> smallValue;
    private final Value.Immutable<Boolean> armsValue;
    private final Value.Immutable<Boolean> basePlateValue;

    public ImmutableSpongeArmorStandData() {
        this(false, false, false, true);
    }

    public ImmutableSpongeArmorStandData(boolean marker, boolean small, boolean arms, boolean basePlate) {
        super(ImmutableArmorStandData.class);
        this.marker = marker;
        this.small = small;
        this.arms = arms;
        this.basePlate = basePlate;
        this.markerValue = SpongeImmutableValue.cachedOf(Keys.ARMOR_STAND_MARKER, this.marker);
        this.smallValue = SpongeImmutableValue.cachedOf(Keys.ARMOR_STAND_IS_SMALL, this.small);
        this.armsValue = SpongeImmutableValue.cachedOf(Keys.ARMOR_STAND_HAS_ARMS, this.arms);
        this.basePlateValue = SpongeImmutableValue.cachedOf(Keys.ARMOR_STAND_HAS_BASE_PLATE, this.basePlate);
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.ARMOR_STAND_HAS_ARMS, () -> this.arms);
        registerKeyValue(Keys.ARMOR_STAND_HAS_ARMS, () -> this.armsValue);

        registerFieldGetter(Keys.ARMOR_STAND_IS_SMALL, () -> this.small);
        registerKeyValue(Keys.ARMOR_STAND_IS_SMALL, () -> this.smallValue);

        registerFieldGetter(Keys.ARMOR_STAND_HAS_BASE_PLATE, () -> this.basePlate);
        registerKeyValue(Keys.ARMOR_STAND_HAS_BASE_PLATE, () -> this.basePlateValue);

        registerFieldGetter(Keys.ARMOR_STAND_MARKER, () -> this.marker);
        registerKeyValue(Keys.ARMOR_STAND_MARKER, () -> this.markerValue);

    }

    @Override
    public Value.Immutable<Boolean> marker() {
        return this.markerValue;
    }

    @Override
    public Value.Immutable<Boolean> small() {
        return this.smallValue;
    }

    @Override
    public Value.Immutable<Boolean> arms() {
        return this.armsValue;
    }

    @Override
    public Value.Immutable<Boolean> basePlate() {
        return this.basePlateValue;
    }

    @Override
    public ArmorStandData asMutable() {
        return new SpongeArmorStandData(this.marker, this.small, this.arms, this.basePlate);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.ARMOR_STAND_HAS_ARMS, this.arms)
                .set(Keys.ARMOR_STAND_HAS_BASE_PLATE, this.basePlate)
                .set(Keys.ARMOR_STAND_IS_SMALL, this.small)
                .set(Keys.ARMOR_STAND_MARKER, this.marker);
    }
}
