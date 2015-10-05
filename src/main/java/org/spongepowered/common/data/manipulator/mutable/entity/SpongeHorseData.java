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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.type.HorseVariants;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHorseData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

public class SpongeHorseData extends AbstractData<HorseData, ImmutableHorseData> implements HorseData {

    private HorseColor horseColor;
    private HorseStyle horseStyle;
    private HorseVariant horseVariant;

    public SpongeHorseData(HorseColor horseColor, HorseStyle horseStyle, HorseVariant horseVariant) {
        super(HorseData.class);
        this.horseColor = horseColor;
        this.horseStyle = horseStyle;
        this.horseVariant = horseVariant;
        registerGettersAndSetters();
    }

    public SpongeHorseData() {
        this(HorseColors.WHITE, HorseStyles.NONE, HorseVariants.HORSE);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.HORSE_COLOR, SpongeHorseData.this::getHorseColor);
        registerFieldSetter(Keys.HORSE_COLOR, SpongeHorseData.this::setHorseColor);
        registerKeyValue(Keys.HORSE_COLOR, SpongeHorseData.this::color);

        registerFieldGetter(Keys.HORSE_STYLE, SpongeHorseData.this::getHorseStyle);
        registerFieldSetter(Keys.HORSE_STYLE, this::setHorseStyle);
        registerKeyValue(Keys.HORSE_STYLE, SpongeHorseData.this::style);

        registerFieldGetter(Keys.HORSE_VARIANT, SpongeHorseData.this::getHorseVariant);
        registerFieldSetter(Keys.HORSE_VARIANT, SpongeHorseData.this::setHorseVariant);
        registerKeyValue(Keys.HORSE_VARIANT, SpongeHorseData.this::variant);
    }

    @Override
    public Value<HorseColor> color() {
        return new SpongeValue<>(Keys.HORSE_COLOR, HorseColors.WHITE, this.horseColor);
    }

    @Override
    public Value<HorseStyle> style() {
        return new SpongeValue<>(Keys.HORSE_STYLE, HorseStyles.NONE, this.horseStyle);
    }

    @Override
    public Value<HorseVariant> variant() {
        return new SpongeValue<>(Keys.HORSE_VARIANT, HorseVariants.HORSE, this.horseVariant);
    }

    @Override
    public HorseData copy() {
        return new SpongeHorseData(this.horseColor, this.horseStyle, this.horseVariant);
    }

    @Override
    public ImmutableHorseData asImmutable() {
        return new ImmutableSpongeHorseData(this.horseColor, this.horseStyle, this.horseVariant);
    }

    @Override
    public int compareTo(HorseData other) {
        return ComparisonChain.start()
                .compare(this.horseColor.getId(), other.color().get().getId())
                .compare(this.horseStyle.getId(), other.style().get().getId())
                .compare(this.horseVariant.getId(), other.variant().get().getId())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.HORSE_COLOR.getQuery(), this.horseColor.getId())
                .set(Keys.HORSE_STYLE.getQuery(), this.horseStyle.getId())
                .set(Keys.HORSE_VARIANT.getQuery(), this.horseVariant.getId());

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

    private HorseVariant getHorseVariant() {
        return this.horseVariant;
    }

    private void setHorseVariant(HorseVariant horseVariant) {
        this.horseVariant = horseVariant;
    }
}
