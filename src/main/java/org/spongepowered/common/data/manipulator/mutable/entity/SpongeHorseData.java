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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHorseData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeHorseData extends AbstractData<HorseData, ImmutableHorseData> implements HorseData {

    private HorseColor horseColor;
    private HorseStyle horseStyle;

    public SpongeHorseData(HorseColor horseColor, HorseStyle horseStyle) {
        super(HorseData.class);
        this.horseColor = checkNotNull(horseColor, "The Horse Color was null!");
        this.horseStyle = checkNotNull(horseStyle, "The Horse Style was null!");
        registerGettersAndSetters();
    }

    public SpongeHorseData() {
        this(HorseColors.WHITE, HorseStyles.NONE);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.HORSE_COLOR, SpongeHorseData.this::getHorseColor);
        registerFieldSetter(Keys.HORSE_COLOR, SpongeHorseData.this::setHorseColor);
        registerKeyValue(Keys.HORSE_COLOR, SpongeHorseData.this::color);

        registerFieldGetter(Keys.HORSE_STYLE, SpongeHorseData.this::getHorseStyle);
        registerFieldSetter(Keys.HORSE_STYLE, this::setHorseStyle);
        registerKeyValue(Keys.HORSE_STYLE, SpongeHorseData.this::style);

    }

    @Override
    public Value<HorseColor> color() {
        return new SpongeValue<>(Keys.HORSE_COLOR, Constants.Entity.Horse.DEFAULT_COLOR, this.horseColor);
    }

    @Override
    public Value<HorseStyle> style() {
        return new SpongeValue<>(Keys.HORSE_STYLE, Constants.Entity.Horse.DEFAULT_STYLE, this.horseStyle);
    }

    @Override
    public HorseData copy() {
        return new SpongeHorseData(this.horseColor, this.horseStyle);
    }

    @Override
    public ImmutableHorseData asImmutable() {
        return new ImmutableSpongeHorseData(this.horseColor, this.horseStyle);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.HORSE_COLOR.getQuery(), this.horseColor.getId())
                .set(Keys.HORSE_STYLE.getQuery(), this.horseStyle.getId());

    }

    private HorseColor getHorseColor() {
        return this.horseColor;
    }

    private void setHorseColor(HorseColor horseColor) {
        this.horseColor = horseColor;
    }

    private HorseStyle getHorseStyle() {
        return this.horseStyle;
    }

    private void setHorseStyle(HorseStyle horseStyle) {
        this.horseStyle = horseStyle;
    }

}
