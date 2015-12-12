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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkEffectData;
import org.spongepowered.api.data.manipulator.mutable.FireworkEffectData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeFireworkEffectData;
import org.spongepowered.common.data.manipulator.mutable.common.collection.AbstractSingleListData;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;

public class SpongeFireworkEffectData extends AbstractSingleListData<FireworkEffect, FireworkEffectData, ImmutableFireworkEffectData> implements FireworkEffectData {

    public SpongeFireworkEffectData() {
        this(ImmutableList.of());
    }

    public SpongeFireworkEffectData(List<FireworkEffect> effects) {
        super(FireworkEffectData.class, effects, Keys.FIREWORK_EFFECTS, ImmutableSpongeFireworkEffectData.class);
    }

    @Override
    public ListValue<FireworkEffect> effects() {
        return new SpongeListValue<>(Keys.FIREWORK_EFFECTS, Lists.newArrayList(this.getValue()));
    }

    @Override
    public int compareTo(FireworkEffectData o) {
        return Booleans.compare(o.effects().containsAll(this.getValue()), this.getValue().containsAll(o.effects().get()));
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.FIREWORK_EFFECTS.getQuery(), this.getValue());
    }
}
