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

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkData;
import org.spongepowered.api.data.manipulator.mutable.FireworkData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeFireworkData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;

import java.util.List;

public class ImmutableSpongeFireworkData extends AbstractImmutableData<ImmutableFireworkData, FireworkData> implements ImmutableFireworkData {

    private final ImmutableList<FireworkEffect> fireworkEffects;
    private final int modifier;

    public ImmutableSpongeFireworkData(List<FireworkEffect> effects, int flightModifier) {
        super(ImmutableFireworkData.class);
        this.fireworkEffects = ImmutableList.copyOf(effects);
        this.modifier = flightModifier;
        registerGetters();
    }

    @Override
    public ImmutableListValue<FireworkEffect> effects() {
        return new ImmutableSpongeListValue<>(Keys.FIREWORK_EFFECTS, this.fireworkEffects);
    }

    @Override
    public ImmutableBoundedValue<Integer> flightModifier() {
        return new ImmutableSpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, 0, 0, intComparator(), Integer.MAX_VALUE, this.modifier);
    }

    @Override
    public FireworkData asMutable() {
        return new SpongeFireworkData(this.fireworkEffects, this.modifier);
    }

    @Override
    public int compareTo(ImmutableFireworkData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.FIREWORK_EFFECTS, this.fireworkEffects)
            .set(Keys.FIREWORK_FLIGHT_MODIFIER, this.modifier);
    }

    @Override
    protected void registerGetters() {
        // TODO
    }
}
