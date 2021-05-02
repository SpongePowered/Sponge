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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.stream.Collectors;

public final class PotionData {

    private PotionData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ThrownPotion.class)
                    .create(Keys.ITEM_STACK_SNAPSHOT)
                        .get(h -> ItemStackUtil.snapshotOf(h.getItem()))
                        .setAnd((h, v) -> {
                            final ItemStack itemStack = ItemStackUtil.fromSnapshotToNative(v);
                            if (itemStack.getItem() != Items.SPLASH_POTION && itemStack.getItem() != Items.LINGERING_POTION) {
                                // Minecraft will throw a hissy fit if we do allow any other type of potion
                                // so, we have to return false because the item stack is invalid.
                                return false;
                            }
                            h.setItem(itemStack);
                            return true;
                        })
                    .create(Keys.POTION_EFFECTS)
                        .get(h -> PotionUtils.getMobEffects(h.getItem()).stream().map(PotionEffect.class::cast).collect(Collectors.toList()))
                        .set((h, v) -> {
                            h.getItem().removeTagKey(Constants.Item.CUSTOM_POTION_EFFECTS);
                            PotionUtils.setCustomEffects(h.getItem(), v.stream()
                                    .map(MobEffectInstance.class::cast)
                                    .collect(Collectors.toList()));
                        });
    }
    // @formatter:on
}
