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
package org.spongepowered.common.data.manipulators;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.PotionEffectData;
import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.api.potion.PotionEffectType;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SpongePotionEffectData extends AbstractListData<PotionEffect, PotionEffectData> implements PotionEffectData {

    public SpongePotionEffectData() {
        super(PotionEffectData.class);
    }

    @Override
    public PotionEffectData addPotionEffect(PotionEffect potionEffect, boolean force) {
        checkNotNull(potionEffect);
        final Iterator<PotionEffect> iterator = this.elementList.iterator();
        while (iterator.hasNext()) {
            final PotionEffect effect = iterator.next();
            if (effect.getType() == potionEffect.getType()) {
                if (effect.getAmplifier() < potionEffect.getAmplifier() || (effect.getAmplifier() == potionEffect.getAmplifier()
                        && effect.getDuration() < potionEffect.getDuration()) || force) {
                    iterator.remove();
                }
            }
        }
        return add(potionEffect);
    }

    @Override
    public PotionEffectData addPotionEffects(Collection<PotionEffect> potionEffects, boolean force) {
        for (final PotionEffect potionEffect : checkNotNull(potionEffects)) {
            addPotionEffect(potionEffect, force);
        }
        return this;
    }

    @Override
    public PotionEffectData removePotionEffect(PotionEffectType potionEffectType) {
        final Iterator<PotionEffect> iterator = this.elementList.iterator();
        while (iterator.hasNext()) {
            final PotionEffect effect = iterator.next();
            if (effect.getType() == checkNotNull(potionEffectType)) {
                iterator.remove();
            }
        }
        return this;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType potionEffectType) {
        for (final PotionEffect effect : this.elementList) {
            if (effect.getType() == checkNotNull(potionEffectType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return getAll();
    }

    @Override
    public int compareTo(PotionEffectData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(of("Potions"), this.elementList);
    }
}
