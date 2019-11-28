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
        super(itemStack -> itemStack.getItem() == Items.POTION || itemStack.getItem() == Items.SPLASH_POTION ||
                itemStack.getItem() == Items.LINGERING_POTION, Keys.POTION_COLOR);
    }

    @Override
    protected boolean set(ItemStack dataHolder, Color value) {
        if (!dataHolder.hasTag()) {
            dataHolder.setTag(new CompoundNBT());
        }

        final CompoundNBT mainCompound = dataHolder.getTag();
        mainCompound.putInt(Constants.Item.CUSTOM_POTION_COLOR, value.getRgb());
        return true;
    }

    @Override
    protected Optional<Color> getVal(ItemStack dataHolder) {
        return Optional.of(Color.ofRgb(PotionUtils.getColor(dataHolder)));
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
        if (!itemStack.hasTag()) {
            return DataTransactionResult.successNoData();
        }

        CompoundNBT mainCompound = itemStack.getTag();
        if (!mainCompound.contains(Constants.Item.CUSTOM_POTION_COLOR, Constants.NBT.TAG_INT)) {
            return DataTransactionResult.successNoData();
        }

        Color removedColor = Color.ofRgb(mainCompound.getInt(Constants.Item.CUSTOM_POTION_COLOR));
        mainCompound.remove(Constants.Item.CUSTOM_POTION_COLOR);
        return DataTransactionResult.successRemove(constructImmutableValue(removedColor));
    }
}
