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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeLoreData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ItemLoreDataProcessor extends AbstractItemSingleDataProcessor<List<Text>, ListValue<Text>, LoreData, ImmutableLoreData> {

    public ItemLoreDataProcessor() {
        super(input -> true, Keys.ITEM_LORE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<LoreData> fill(DataContainer container, LoreData loreData) {
        final List<String> json = DataUtil.getData(container, Keys.ITEM_LORE, List.class);
        return Optional.of(loreData.set(Keys.ITEM_LORE, SpongeTexts.fromJson(json)));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (supports(container)) {
            ItemStack stack = (ItemStack) container;
            Optional<List<Text>> old = getVal(stack);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            if(stack.func_179543_a(Constants.Item.ITEM_DISPLAY) != null) {
                stack.func_179543_a(Constants.Item.ITEM_DISPLAY).func_82580_o(Constants.Item.ITEM_LORE);
            }
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected LoreData createManipulator() {
        return new SpongeLoreData();
    }

    @Override
    protected boolean set(ItemStack itemStack, List<Text> value) {
        final NBTTagList list =  SpongeTexts.asLegacy(value);
        itemStack.func_190925_c(Constants.Item.ITEM_DISPLAY).func_74782_a(Constants.Item.ITEM_LORE, list); // setSubCompound
        return true;
    }

    @Override
    protected Optional<List<Text>> getVal(ItemStack itemStack) {
        final NBTTagCompound subCompound = itemStack.func_179543_a(Constants.Item.ITEM_DISPLAY);
        if (subCompound == null) {
            return Optional.empty();
        }
        if (!subCompound.func_150297_b(Constants.Item.ITEM_LORE, Constants.NBT.TAG_LIST)) {
            return Optional.empty();
        }
        final NBTTagList list = subCompound.func_150295_c(Constants.Item.ITEM_LORE, Constants.NBT.TAG_STRING);
        return Optional.of(SpongeTexts.fromNbtLegacy(list));
    }

    @Override
    protected ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<>(Keys.ITEM_LORE, defaultValue);
    }

    @Override
    protected ImmutableValue<List<Text>> constructImmutableValue(List<Text> value) {
        return new ImmutableSpongeListValue<>(Keys.ITEM_LORE, ImmutableList.copyOf(value));
    }

}
