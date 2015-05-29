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
package org.spongepowered.common.data.component.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.HorseComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.type.HorseVariants;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

public class SpongeHorseComponent extends SpongeAbstractComponent<HorseComponent> implements HorseComponent {

    private HorseColor color = HorseColors.BLACK;
    private HorseStyle style = HorseStyles.BLACK_DOTS;
    private HorseVariant variant = HorseVariants.HORSE;

    public SpongeHorseComponent() {
        super(HorseComponent.class);
    }

    @Override
    public HorseStyle getStyle() {
        return this.style;
    }

    @Override
    public HorseComponent setStyle(HorseStyle style) {
        this.style = checkNotNull(style, "Style is null!");
        return this;
    }

    @Override
    public HorseColor getColor() {
        return this.color;
    }

    @Override
    public HorseComponent setColor(HorseColor color) {
        this.color = checkNotNull(color, "Color is null!");
        return this;
    }

    @Override
    public HorseVariant getVariant() {
        return this.variant;
    }

    @Override
    public HorseComponent setVariant(HorseVariant variant) {
        this.variant = checkNotNull(variant, "Variant is null!");
        return this;
    }

    @Override
    public HorseComponent copy() {
        return new SpongeHorseComponent()
                .setColor(this.color)
                .setStyle(this.style)
                .setVariant(this.variant);
    }

    @Override
    public HorseComponent reset() {
        return setColor(HorseColors.BLACK)
                .setStyle(HorseStyles.BLACK_DOTS)
                .setVariant(HorseVariants.HORSE);
    }

    @Override
    public int compareTo(HorseComponent o) {
        return o.getColor().getId().compareTo(this.getColor().getId()) - o.getStyle().getId().compareTo(this.getStyle().getId())
                - o.getVariant().getId().compareTo(this.getVariant().getId());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.HORSE_COLOR.getQuery(), this.getColor().getId())
                .set(Tokens.HORSE_STYLE.getQuery(), this.getStyle().getId())
                .set(Tokens.HORSE_VARIANT.getQuery(), this.getVariant().getId());
    }
}
