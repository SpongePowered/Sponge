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
        super(itemStack -> itemStack.func_77973_b() == Items.field_151068_bn || itemStack.func_77973_b() == Items.field_185155_bH ||
                itemStack.func_77973_b() == Items.field_185156_bI || itemStack.func_77973_b() == Items.field_185167_i, Keys.POTION_EFFECTS);
    }

    @Override
    protected boolean set(ItemStack dataHolder, List<PotionEffect> value) {
        if (!dataHolder.func_77942_o()) {
            dataHolder.func_77982_d(new NBTTagCompound());
        }
        final NBTTagCompound mainCompound = dataHolder.func_77978_p();
        final NBTTagList potionList = new NBTTagList();
        for (PotionEffect effect : value) {
            final NBTTagCompound potionCompound = new NBTTagCompound();
            ((net.minecraft.potion.PotionEffect) effect).func_82719_a(potionCompound);
            potionList.func_74742_a(potionCompound);
        }
        mainCompound.func_74782_a(Constants.Item.CUSTOM_POTION_EFFECTS, potionList);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<List<PotionEffect>> getVal(ItemStack dataHolder) {
        final List<net.minecraft.potion.PotionEffect> effects = PotionUtils.func_185189_a(dataHolder);
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
        Item item = itemStack.func_77973_b();
        if (item != Items.field_151068_bn) {
            return DataTransactionResult.failNoData();
        }

        Optional<List<PotionEffect>> currentEffects = getVal(itemStack);
        if (!itemStack.func_77942_o()) {
            itemStack.func_77982_d(new NBTTagCompound());
        }

        final NBTTagCompound tagCompound = itemStack.func_77978_p();
        tagCompound.func_74782_a(Constants.Item.CUSTOM_POTION_EFFECTS, new NBTTagList());
        if (currentEffects.isPresent()) {
            return DataTransactionResult.successRemove(constructImmutableValue(currentEffects.get()));
        }
        return DataTransactionResult.successNoData();
    }
}
