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

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableStoredEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.StoredEnchantmentData;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeStoredEnchantmentData;
import org.spongepowered.common.item.enchantment.SpongeEnchantment;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class StoredEnchantmentDataProcessor extends
        AbstractItemSingleDataProcessor<List<Enchantment>, ListValue<Enchantment>, StoredEnchantmentData, ImmutableStoredEnchantmentData> {

    public StoredEnchantmentDataProcessor() {
        super(stack -> stack.getItem().equals(Items.ENCHANTED_BOOK), Keys.STORED_ENCHANTMENTS);
    }

    @Override
    protected ListValue<Enchantment> constructValue(List<Enchantment> actualValue) {
        return SpongeValueFactory.getInstance().createListValue(Keys.STORED_ENCHANTMENTS, actualValue, Lists.newArrayList());
    }

    @Override
    protected boolean set(ItemStack entity, List<Enchantment> value) {
        if (!entity.hasTag()) {
            entity.setTag(new CompoundNBT());
        }
        ListNBT list = new ListNBT();
        for (Enchantment enchantment : value) {
            CompoundNBT tag = new CompoundNBT();
            tag.putShort(Constants.Item.ITEM_ENCHANTMENT_ID, (short) net.minecraft.enchantment.Enchantment.func_185258_b((net.minecraft.enchantment.Enchantment) enchantment.getType()));
            tag.putShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL, (short) enchantment.getLevel());
            list.func_74742_a(tag);
        }
        entity.getTag().func_74782_a(Constants.Item.ITEM_STORED_ENCHANTMENTS_LIST, list);
        return true;
    }

    @Override
    protected Optional<List<Enchantment>> getVal(ItemStack entity) {
        if (!entity.hasTag() || !entity.getTag().contains(Constants.Item.ITEM_STORED_ENCHANTMENTS_LIST, Constants.NBT.TAG_LIST)) {
            return Optional.empty();
        }
        List<Enchantment> list = Lists.newArrayList();
        ListNBT tags = entity.getTag().getList(Constants.Item.ITEM_STORED_ENCHANTMENTS_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tags.func_74745_c(); i++) {
            CompoundNBT tag = tags.getCompound(i);
            list.add(new SpongeEnchantment(
                    (EnchantmentType) net.minecraft.enchantment.Enchantment.getEnchantmentByID(tag.getShort(Constants.Item.ITEM_ENCHANTMENT_ID)),
                    tag.getShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL)));
        }
        return Optional.of(list);
    }

    @Override
    protected ImmutableValue<List<Enchantment>> constructImmutableValue(List<Enchantment> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected StoredEnchantmentData createManipulator() {
        return new SpongeStoredEnchantmentData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (supports(container)) {
            ItemStack stack = (ItemStack) container;
            if (stack.hasTag() && stack.getTag().contains(Constants.Item.ITEM_STORED_ENCHANTMENTS_LIST, Constants.NBT.TAG_COMPOUND)) {
                stack.getTag().remove(Constants.Item.ITEM_STORED_ENCHANTMENTS_LIST);
            }
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }
}
