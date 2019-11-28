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

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionColorData;
import org.spongepowered.api.data.manipulator.mutable.PotionColorData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.manipulator.mutable.SpongePotionColorData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemPotionColorDataProcessor extends AbstractItemSingleDataProcessor<Color, Value<Color>, PotionColorData, ImmutablePotionColorData> {

    public ItemPotionColorDataProcessor() {
        super(itemStack -> itemStack.func_77973_b() == Items.field_151068_bn || itemStack.func_77973_b() == Items.field_185155_bH ||
                itemStack.func_77973_b() == Items.field_185156_bI, Keys.POTION_COLOR);
    }

    @Override
    protected boolean set(ItemStack dataHolder, Color value) {
        if (!dataHolder.func_77942_o()) {
            dataHolder.func_77982_d(new CompoundNBT());
        }

        final CompoundNBT mainCompound = dataHolder.func_77978_p();
        mainCompound.func_74768_a(Constants.Item.CUSTOM_POTION_COLOR, value.getRgb());
        return true;
    }

    @Override
    protected Optional<Color> getVal(ItemStack dataHolder) {
        return Optional.of(Color.ofRgb(PotionUtils.func_190932_c(dataHolder)));
    }

    @Override
    protected ImmutableValue<Color> constructImmutableValue(Color value) {
        return new ImmutableSpongeValue<>(Keys.POTION_COLOR, value);
    }

    @Override
    protected Value<Color> constructValue(Color actualValue) {
        return new SpongeValue<>(Keys.POTION_COLOR, actualValue);
    }

    @Override
    protected PotionColorData createManipulator() {
        return new SpongePotionColorData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!supports(container)) {
            return DataTransactionResult.failNoData();
        }

        ItemStack itemStack = (ItemStack) container;
        if (!itemStack.func_77942_o()) {
            return DataTransactionResult.successNoData();
        }

        CompoundNBT mainCompound = itemStack.func_77978_p();
        if (!mainCompound.func_150297_b(Constants.Item.CUSTOM_POTION_COLOR, Constants.NBT.TAG_INT)) {
            return DataTransactionResult.successNoData();
        }

        Color removedColor = Color.ofRgb(mainCompound.func_74762_e(Constants.Item.CUSTOM_POTION_COLOR));
        mainCompound.func_82580_o(Constants.Item.CUSTOM_POTION_COLOR);
        return DataTransactionResult.successRemove(constructImmutableValue(removedColor));
    }
}
