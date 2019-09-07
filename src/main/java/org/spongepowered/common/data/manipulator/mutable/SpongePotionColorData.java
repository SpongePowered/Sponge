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
package org.spongepowered.common.data.manipulator.mutable;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionColorData;
import org.spongepowered.api.data.manipulator.mutable.PotionColorData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongePotionColorData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongePotionColorData extends AbstractSingleData<Color, PotionColorData, ImmutablePotionColorData> implements PotionColorData {

    public SpongePotionColorData() {
        this(Color.RED);
    }

    public SpongePotionColorData(Color color) {
        super(PotionColorData.class, color, Keys.POTION_COLOR);
    }

    @Override
    public Value<Color> color() {
        return new SpongeValue<>(Keys.POTION_COLOR, Color.RED, this.getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return this.color();
    }

    @Override
    public PotionColorData copy() {
        return new SpongePotionColorData(this.getValue());
    }

    @Override
    public ImmutablePotionColorData asImmutable() {
        return new ImmutableSpongePotionColorData(this.getValue());
    }
}
