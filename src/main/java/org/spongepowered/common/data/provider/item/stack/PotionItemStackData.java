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
package org.spongepowered.common.data.provider.item.stack;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.item.potion.PotionType;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class PotionItemStackData {

    private PotionItemStackData() {
    }

    // @formatter:off
    // TODO check support
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.COLOR)
                        .get(h -> Color.ofRgb(h.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor()))
                        .set((h, v) -> h.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, contents -> new PotionContents(contents.potion(), Optional.of(v.rgb()), contents.customEffects())))
                        .delete(h -> h.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, contents -> new PotionContents(contents.potion(), Optional.empty(), contents.customEffects())))
                        .supports(h -> h.getItem() == Items.POTION || h.getItem() == Items.SPLASH_POTION || h.getItem() == Items.LINGERING_POTION || h.getItem() == Items.TIPPED_ARROW)
                    .create(Keys.CUSTOM_POTION_EFFECTS)
                        .get(h -> {
                            final List<MobEffectInstance> effects = h.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).customEffects();
                            return effects.isEmpty() ? null : ImmutableList.copyOf((List<PotionEffect>) (Object) effects);
                        })
                        .set((h, v) -> {
                            final var mcList = v.stream().map(MobEffectInstance.class::cast).toList();
                            h.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, contents -> new PotionContents(contents.potion(), contents.customColor(), mcList));
                        })
                        .delete(h -> h.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, contents -> new PotionContents(contents.potion(), contents.customColor(), Collections.emptyList())))
                        .supports(h -> h.getItem() == Items.POTION || h.getItem() == Items.SPLASH_POTION || h.getItem() == Items.LINGERING_POTION || h.getItem() == Items.TIPPED_ARROW)
                    .create(Keys.POTION_TYPE)
                        .get(h -> (PotionType) h.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().map(Holder::value).orElse(null)) // TODO empty POTION gone?
                        .set((h, v) -> {
                            final var potion = Optional.ofNullable(v).map(Potion.class::cast).map(BuiltInRegistries.POTION::wrapAsHolder); // TODO set empty POTION? same as delete?
                            h.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, contents -> new PotionContents(potion, contents.customColor(), contents.customEffects()));
                        })
                        .delete(h -> h.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, contents -> new PotionContents(Optional.empty(), contents.customColor(), contents.customEffects())))
                        .supports(h -> h.getItem() == Items.POTION || h.getItem() == Items.SPLASH_POTION ||
                                h.getItem() == Items.LINGERING_POTION || h.getItem() == Items.TIPPED_ARROW)
                    .create(Keys.POTION_EFFECTS)
                        .get(h -> {
                            final Iterable<MobEffectInstance> effects = h.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects();
                            return ImmutableList.copyOf((List<PotionEffect>) (Object) effects);
                        })
                        .supports(h -> h.getItem() == Items.POTION || h.getItem() == Items.SPLASH_POTION || h.getItem() == Items.LINGERING_POTION || h.getItem() == Items.TIPPED_ARROW)
        ;
    }
    // @formatter:on
}
