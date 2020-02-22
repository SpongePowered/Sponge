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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ItemStackPotionEffectsProvider extends ItemStackDataProvider<List<PotionEffect>> {

    public ItemStackPotionEffectsProvider() {
        super(Keys.POTION_EFFECTS);
    }

    @Override
    protected boolean supports(final Item item) {
        return item == Items.POTION || item == Items.SPLASH_POTION ||
                item == Items.LINGERING_POTION || item == Items.TIPPED_ARROW;
    }

    @Override
    protected Optional<List<PotionEffect>> getFrom(final ItemStack dataHolder) {
        final List<net.minecraft.potion.EffectInstance> effects = PotionUtils.getEffectsFromStack(dataHolder);
        if (effects.isEmpty()) {
            return Optional.empty();
        }
        //noinspection unchecked
        return Optional.of((List<PotionEffect>) (List<?>) effects);
    }

    @Override
    protected boolean set(final ItemStack dataHolder, final List<PotionEffect> value) {
        final CompoundNBT tag = dataHolder.getOrCreateTag();
        final ListNBT list = value.stream()
                .map(effect -> {
                    final CompoundNBT potionTag = new CompoundNBT();
                    ((net.minecraft.potion.EffectInstance) effect).write(potionTag);
                    return potionTag;
                })
                .collect(NbtCollectors.toTagList());
        tag.put(Constants.Item.CUSTOM_POTION_EFFECTS, list);
        return true;
    }

    @Override
    protected boolean delete(final ItemStack dataHolder) {
        dataHolder.removeChildTag(Constants.Item.CUSTOM_POTION_EFFECTS);
        return true;
    }
}
