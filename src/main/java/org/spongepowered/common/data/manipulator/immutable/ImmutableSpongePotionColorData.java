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
package org.spongepowered.common.data.manipulator.immutable;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionColorData;
import org.spongepowered.api.data.manipulator.mutable.PotionColorData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.SpongePotionColorData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongePotionColorData extends AbstractImmutableSingleData<Color, ImmutablePotionColorData, PotionColorData> implements ImmutablePotionColorData {

    private final ImmutableValue<Color> immutableValue = new ImmutableSpongeValue<>(Keys.POTION_COLOR, Color.RED, this.value);

    public ImmutableSpongePotionColorData(Color type) {
        super(ImmutablePotionColorData.class, type, Keys.POTION_COLOR);
    }

    @Override
    public ImmutableValue<Color> color() {
        return this.immutableValue;
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return this.color();
    }

    @Override
    public SpongePotionColorData asMutable() {
        return new SpongePotionColorData(this.value);
    }
}
