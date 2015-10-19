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

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkData;
import org.spongepowered.api.data.manipulator.mutable.FireworkData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeFireworkData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueBuilder;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;

public class SpongeFireworkData extends AbstractData<FireworkData, ImmutableFireworkData> implements FireworkData {

    private List<FireworkEffect> fireworkEffects;
    private int flightModifier = 0;

    public SpongeFireworkData(List<FireworkEffect> effects, int modifier) {
        super(FireworkData.class);
        this.fireworkEffects = Lists.newArrayList(effects);
        this.flightModifier = modifier >= 0 ? modifier : 0;
        registerGettersAndSetters();
    }

    public SpongeFireworkData(List<FireworkEffect> effects) {
        this(effects, 0);
    }

    public SpongeFireworkData() {
        this(Lists.<FireworkEffect>newArrayListWithCapacity(0), 0);
    }

    @Override
    public ListValue<FireworkEffect> effects() {
        return new SpongeListValue<>(Keys.FIREWORK_EFFECTS, Lists.newArrayList(this.fireworkEffects));
    }

    @Override
    public MutableBoundedValue<Integer> flightModifier() {
        return SpongeValueBuilder.boundedBuilder(Keys.FIREWORK_FLIGHT_MODIFIER)
            .defaultValue(0)
            .minimum(0)
            .maximum(Integer.MAX_VALUE)
            .actualValue(this.flightModifier)
            .build();
    }

    @Override
    public FireworkData copy() {
        return new SpongeFireworkData(this.fireworkEffects, this.flightModifier);
    }

    @Override
    public ImmutableFireworkData asImmutable() {
        return new ImmutableSpongeFireworkData(this.fireworkEffects, this.flightModifier);
    }

    @Override
    public int compareTo(FireworkData o) {
        return ComparisonChain.start()
                .compare(o.effects().containsAll(this.fireworkEffects), this.fireworkEffects.containsAll(o.effects().get()))
                .compare(o.flightModifier().get().intValue(), this.flightModifier)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.FIREWORK_EFFECTS.getQuery(), this.fireworkEffects)
                .set(Keys.FIREWORK_FLIGHT_MODIFIER.getQuery(), this.flightModifier);
    }

    @Override
    protected void registerGettersAndSetters() {
        // TODO
    }
}
