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

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.PotionContents;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.accessor.world.entity.projectile.ArrowAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.List;
import java.util.stream.Collectors;

public final class ArrowData {

    private ArrowData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Arrow.class)
                    .create(Keys.POTION_EFFECTS)
                        .get(h -> {
                            final Iterable<MobEffectInstance> effects = ((ArrowAccessor) h).invoker$getPotionContents().getAllEffects();
                            return Streams.of(effects)
                                    .map(effect -> (PotionEffect) ArrowData.clone(effect))
                                    .collect(Collectors.toList());
                        })
                        .set((h, v) -> {
                            final PotionContents previousContents = ((ArrowAccessor) h).invoker$getPotionContents();
                            final List<MobEffectInstance> list = v.stream().map(effect -> ArrowData.clone((MobEffectInstance) effect)).toList();
                            ((ArrowAccessor) h).invoker$setPotionContents(new PotionContents(previousContents.potion(), previousContents.customColor(), list));
                        });
    }
    // @formatter:on
    @NotNull
    private static MobEffectInstance clone(final MobEffectInstance effect) {
        return new MobEffectInstance(effect.getEffect(),
                                    effect.getDuration(),
                                    effect.getAmplifier(),
                                    effect.isAmbient(),
                                    effect.isVisible(),
                                    effect.showIcon());
        // TODO API showIcon?
        // TODO hiddenEffect?
    }
}
