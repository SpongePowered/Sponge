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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionTypeData;
import org.spongepowered.api.data.manipulator.mutable.PotionTypeData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.potion.PotionType;
import org.spongepowered.common.data.manipulator.mutable.SpongePotionTypeData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class ItemPotionTypeDataProcessor extends AbstractItemSingleDataProcessor<PotionType, Value<PotionType>, PotionTypeData, ImmutablePotionTypeData> {

    public ItemPotionTypeDataProcessor() {
        super(itemStack -> itemStack.func_77973_b() == Items.field_151068_bn || itemStack.func_77973_b() == Items.field_185155_bH ||
                itemStack.func_77973_b() == Items.field_185156_bI || itemStack.func_77973_b() == Items.field_185167_i, Keys.POTION_TYPE);
    }

    @Override
    protected boolean set(ItemStack dataHolder, PotionType value) {
        if (!dataHolder.func_77942_o()) {
            dataHolder.func_77982_d(new CompoundNBT());
        }

        PotionUtils.func_185188_a(dataHolder, ((net.minecraft.potion.Potion) value));
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<PotionType> getVal(ItemStack dataHolder) {
        return Optional.of((PotionType) PotionUtils.func_185191_c(dataHolder));
    }

    @Override
    protected ImmutableValue<PotionType> constructImmutableValue(PotionType value) {
        return new ImmutableSpongeValue<>(Keys.POTION_TYPE, value);
    }

    @Override
    protected Value<PotionType> constructValue(PotionType actualValue) {
        return new SpongeValue<>(Keys.POTION_TYPE, actualValue);
    }

    @Override
    protected PotionTypeData createManipulator() {
        return new SpongePotionTypeData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof ItemStack)) {
            return DataTransactionResult.failNoData();
        }

        ItemStack itemStack = (ItemStack) container;
        Item item = itemStack.func_77973_b();
        // TODO check if this is correct - also for the PotionEffect Processors
        if (item != Items.field_151068_bn) {
            return DataTransactionResult.failNoData();
        }

        Optional<PotionType> val = getVal(itemStack);
        if (val.isPresent()) {
            PotionUtils.func_185188_a(itemStack, Potions.field_185229_a);
            return DataTransactionResult.successRemove(constructImmutableValue(val.get()));
        }
        return DataTransactionResult.successNoData();
    }
}
