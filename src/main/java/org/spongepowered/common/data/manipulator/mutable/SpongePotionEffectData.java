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

import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongePotionEffectData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;

public class SpongePotionEffectData extends AbstractData<PotionEffectData, ImmutablePotionEffectData> implements PotionEffectData {

    private List<PotionEffect> effects;

    public SpongePotionEffectData(List<PotionEffect> effects) {
        super(PotionEffectData.class);
        this.effects = Lists.newArrayList(effects);
        registerGettersAndSetters();
    }

    @Override
    public ListValue<PotionEffect> effects() {
        return new SpongeListValue<PotionEffect>(Keys.POTION_EFFECTS, this.effects);
    }

    @Override
    public PotionEffectData copy() {
        return new SpongePotionEffectData(this.effects);
    }

    @Override
    public ImmutablePotionEffectData asImmutable() {
        return new ImmutableSpongePotionEffectData(this.effects);
    }

    @Override
    public int compareTo(PotionEffectData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.POTION_EFFECTS.getQuery(), this.effects);
    }

    @Override
    protected void registerGettersAndSetters() {
        // TODO
    }
}
