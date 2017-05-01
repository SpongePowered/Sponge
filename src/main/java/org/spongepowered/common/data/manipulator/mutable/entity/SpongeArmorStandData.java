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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableArmorStandData;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeArmorStandData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeArmorStandData extends AbstractData<ArmorStandData, ImmutableArmorStandData> implements ArmorStandData {

    private boolean marker;
    private boolean small;
    private boolean arms;
    private boolean basePlate;

    public SpongeArmorStandData() {
        this(false, false, false, true);
    }

    public SpongeArmorStandData(boolean marker, boolean small, boolean arms, boolean basePlate) {
        super(ArmorStandData.class);
        this.marker = marker;
        this.small = small;
        this.arms = arms;
        this.basePlate = basePlate;
        registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.ARMOR_STAND_HAS_ARMS, () -> this.arms);
        registerFieldSetter(Keys.ARMOR_STAND_HAS_ARMS, (hasArms) -> this.arms = hasArms);
        registerKeyValue(Keys.ARMOR_STAND_HAS_ARMS, this::arms);


        registerFieldGetter(Keys.ARMOR_STAND_IS_SMALL, () -> this.small);
        registerFieldSetter(Keys.ARMOR_STAND_IS_SMALL, (small) -> this.small = small);
        registerKeyValue(Keys.ARMOR_STAND_IS_SMALL, this::small);

        registerFieldGetter(Keys.ARMOR_STAND_HAS_BASE_PLATE, () -> this.basePlate);
        registerFieldSetter(Keys.ARMOR_STAND_HAS_BASE_PLATE, (basePlate) -> this.basePlate = basePlate);
        registerKeyValue(Keys.ARMOR_STAND_HAS_BASE_PLATE, this::basePlate);

        registerFieldGetter(Keys.ARMOR_STAND_MARKER, () -> this.marker);
        registerFieldSetter(Keys.ARMOR_STAND_MARKER, (marker) -> this.marker = marker);
        registerKeyValue(Keys.ARMOR_STAND_MARKER, this::marker);
    }

    @Override
    public Value<Boolean> marker() {
        return new SpongeValue<>(Keys.ARMOR_STAND_MARKER, false, this.marker);
    }

    @Override
    public Value<Boolean> small() {
        return new SpongeValue<>(Keys.ARMOR_STAND_IS_SMALL, false, this.small);
    }

    @Override
    public Value<Boolean> arms() {
        return new SpongeValue<>(Keys.ARMOR_STAND_HAS_ARMS, false, this.arms);
    }

    @Override
    public Value<Boolean> basePlate() {
        return new SpongeValue<>(Keys.ARMOR_STAND_HAS_BASE_PLATE, true, this.basePlate);
    }


    @Override
    public ArmorStandData copy() {
        return new SpongeArmorStandData(this.marker, this.small, this.arms, this.basePlate);
    }

    @Override
    public ImmutableArmorStandData asImmutable() {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeArmorStandData.class, this.marker, this.small, this.arms, this.basePlate);
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
