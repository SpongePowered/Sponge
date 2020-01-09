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
package org.spongepowered.common.data.provider.entity.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.data.util.PotionEffectHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class LivingEntityPotionEffectsProvider extends GenericMutableDataProvider<LivingEntity, List<PotionEffect>> {

    public LivingEntityPotionEffectsProvider() {
        super(Keys.POTION_EFFECTS.get());
    }

    @Override
    protected Optional<List<PotionEffect>> getFrom(LivingEntity dataHolder) {
        final Collection<EffectInstance> effects = dataHolder.getActivePotionEffects();
        if (effects.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(PotionEffectHelper.copyAsPotionEffects(effects));
    }

    @Override
    protected boolean set(LivingEntity dataHolder, List<PotionEffect> value) {
        dataHolder.clearActivePotions();
        for (PotionEffect effect : value) {
            dataHolder.addPotionEffect(PotionEffectHelper.copyAsEffectInstance(effect));
        }
        return true;
    }

    @Override
    protected boolean removeFrom(LivingEntity dataHolder) {
        dataHolder.clearActivePotions();
        return true;
    }
}
