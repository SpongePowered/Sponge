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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.manipulator.mutable.SpongePotionEffectData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ItemPotionEffectDataProcessor extends AbstractItemSingleDataProcessor<List<PotionEffect>, ListValue<PotionEffect>, PotionEffectData, ImmutablePotionEffectData> {

    public ItemPotionEffectDataProcessor() {
        super(itemStack -> itemStack.getItem() == Items.POTIONITEM || itemStack.getItem() == Items.SPLASH_POTION ||
                itemStack.getItem() == Items.LINGERING_POTION || itemStack.getItem() == Items.TIPPED_ARROW, Keys.POTION_EFFECTS);
    }

    @Override
    protected boolean set(ItemStack dataHolder, List<PotionEffect> value) {
        if (!dataHolder.hasTagCompound()) {
            dataHolder.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound mainCompound = dataHolder.getTagCompound();
        final NBTTagList potionList = new NBTTagList();
        for (PotionEffect effect : value) {
            final NBTTagCompound potionCompound = new NBTTagCompound();
            ((net.minecraft.potion.PotionEffect) effect).writeCustomPotionEffectToNBT(potionCompound);
            potionList.appendTag(potionCompound);
        }
        mainCompound.setTag(Constants.Item.CUSTOM_POTION_EFFECTS, potionList);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<List<PotionEffect>> getVal(ItemStack dataHolder) {
        final List<net.minecraft.potion.PotionEffect> effects = PotionUtils.getEffectsFromStack(dataHolder);
        if (effects.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((List<PotionEffect>) (List<?>) effects);
    }

    @Override
    protected ImmutableValue<List<PotionEffect>> constructImmutableValue(List<PotionEffect> value) {
        return new ImmutableSpongeListValue<>(Keys.POTION_EFFECTS, ImmutableList.copyOf(value));
    }

    @Override
    protected ListValue<PotionEffect> constructValue(List<PotionEffect> actualValue) {
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
        if (item != Items.POTIONITEM) {
            return DataTransactionResult.failNoData();
        }

        Optional<List<PotionEffect>> currentEffects = getVal(itemStack);
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        final NBTTagCompound tagCompound = itemStack.getTagCompound();
        tagCompound.setTag(Constants.Item.CUSTOM_POTION_EFFECTS, new NBTTagList());
        if (currentEffects.isPresent()) {
            return DataTransactionResult.successRemove(constructImmutableValue(currentEffects.get()));
        }
        return DataTransactionResult.successNoData();
    }
}
