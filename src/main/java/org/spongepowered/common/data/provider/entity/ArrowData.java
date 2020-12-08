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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.accessor.entity.projectile.ArrowEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.Set;
import java.util.stream.Collectors;

public final class ArrowData {

    private ArrowData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ArrowEntity.class)
                    .create(Keys.POTION_EFFECTS)
                        .get(h -> {
                            final Set<EffectInstance> effects = ((ArrowEntityAccessor) h).accessor$effects();
                            if (effects.isEmpty()) {
                                return null;
                            }
                            return effects.stream()
                                    .map(effect -> (PotionEffect) new EffectInstance(effect.getPotion(), effect.getDuration(),
                                            effect.getAmplifier(), effect.isAmbient(), effect.doesShowParticles()))
                                    .collect(Collectors.toList());
                        })
                        .set((h, v) -> {
                            ((ArrowEntityAccessor) h).accessor$effects().clear();
                            for (final PotionEffect effect : v) {
                                final EffectInstance mcEffect = new EffectInstance(((EffectInstance) effect).getPotion(), effect.getDuration(),
                                        effect.getAmplifier(), effect.isAmbient(), effect.showsParticles());
                                h.addEffect(mcEffect);
                            }
                        });
    }
    // @formatter:on
}
