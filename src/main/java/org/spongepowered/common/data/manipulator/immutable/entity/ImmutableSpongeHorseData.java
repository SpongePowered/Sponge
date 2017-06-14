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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeHorseData extends AbstractImmutableData<ImmutableHorseData, HorseData> implements ImmutableHorseData {

    private final HorseColor horseColor;
    private final HorseStyle horseStyle;

    private final ImmutableValue<HorseColor> colorValue;
    private final ImmutableValue<HorseStyle> styleValue;

    public ImmutableSpongeHorseData(HorseColor horseColor, HorseStyle horseStyle) {
        super(ImmutableHorseData.class);
        this.horseColor = horseColor;
        this.horseStyle = horseStyle;

        this.colorValue = ImmutableSpongeValue.cachedOf(Keys.HORSE_COLOR, HorseColors.BLACK, this.horseColor);
        this.styleValue = ImmutableSpongeValue.cachedOf(Keys.HORSE_STYLE, HorseStyles.NONE, this.horseStyle);

        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.HORSE_COLOR, ImmutableSpongeHorseData.this::getHorseColor);
        registerKeyValue(Keys.HORSE_COLOR, ImmutableSpongeHorseData.this::color);

        registerFieldGetter(Keys.HORSE_STYLE, ImmutableSpongeHorseData.this::getHorseStyle);
        registerKeyValue(Keys.HORSE_STYLE, ImmutableSpongeHorseData.this::style);

    }

    @Override
    public ImmutableValue<HorseColor> color() {
        return this.colorValue;
    }

    @Override
    public ImmutableValue<HorseStyle> style() {
        return this.styleValue;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.HORSE_COLOR.getQuery(), this.horseColor.getId())
                .set(Keys.HORSE_STYLE.getQuery(), this.horseStyle.getId());
    }

    @Override
    public HorseData asMutable() {
        return new SpongeHorseData(this.horseColor, this.horseStyle);
    }

    private HorseColor getHorseColor() {
        return this.horseColor;
    }

    private HorseStyle getHorseStyle() {
        return this.horseStyle;
    }

}
