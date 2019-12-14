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
package org.spongepowered.common.data.processor.data.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.value.ListValue.Mutable;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.manipulator.mutable.SpongePotionEffectData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ItemPotionEffectDataProcessor extends AbstractItemSingleDataProcessor<List<PotionEffect>, Mutable<PotionEffect>, PotionEffectData, ImmutablePotionEffectData> {

    public ItemPotionEffectDataProcessor() {
        super(itemStack -> itemStack.getItem() == Items.POTION || itemStack.getItem() == Items.SPLASH_POTION ||
                itemStack.getItem() == Items.LINGERING_POTION || itemStack.getItem() == Items.TIPPED_ARROW, Keys.POTION_EFFECTS);
    }

    @Override
    protected boolean set(ItemStack dataHolder, List<PotionEffect> value) {
        if (!dataHolder.hasTag()) {
            dataHolder.put(new CompoundNBT());
        }
        final CompoundNBT mainCompound = dataHolder.getTag();
        final ListNBT potionList = new ListNBT();
        for (PotionEffect effect : value) {
            final CompoundNBT potionCompound = new CompoundNBT();
            ((net.minecraft.potion.EffectInstance) effect).write(potionCompound);
            potionList.add(potionCompound);
        }
        mainCompound.put(Constants.Item.CUSTOM_POTION_EFFECTS, potionList);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<List<PotionEffect>> getVal(ItemStack dataHolder) {
        final List<net.minecraft.potion.EffectInstance> effects = PotionUtils.getEffectsFromStack(dataHolder);
        if (effects.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((List<PotionEffect>) (List<?>) effects);
    }

    @Override
    protected Immutable<List<PotionEffect>> constructImmutableValue(List<PotionEffect> value) {
        return new ImmutableSpongeListValue<>(Keys.POTION_EFFECTS, ImmutableList.copyOf(value));
    }

    @Override
    protected Mutable<PotionEffect> constructValue(List<PotionEffect> actualValue) {
        return new SpongeListValue<>(Keys.POTION_EFFECTS, actualValue);
    }

    @Override
    protected PotionEffectData createManipulator() {
        return new SpongePotionEffectData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof ItemStack)) {
            return DataTransactionResult.failNoData();
        }
        ItemStack itemStack = (ItemStack) container;
        Item item = itemStack.getItem();
        if (item != Items.POTION) {
            return DataTransactionResult.failNoData();
        }

        Optional<List<PotionEffect>> currentEffects = this.getVal(itemStack);
        if (!itemStack.hasTag()) {
            itemStack.put(new CompoundNBT());
        }

        final CompoundNBT tagCompound = itemStack.getTag();
        tagCompound.put(Constants.Item.CUSTOM_POTION_EFFECTS, new ListNBT());
        if (currentEffects.isPresent()) {
            return DataTransactionResult.successRemove(this.constructImmutableValue(currentEffects.get()));
        }
        return DataTransactionResult.successNoData();
    }
}
