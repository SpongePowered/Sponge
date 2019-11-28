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

import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeEnchantmentData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemEnchantmentDataProcessor
        extends AbstractItemSingleDataProcessor<List<Enchantment>, ListValue<Enchantment>, EnchantmentData, ImmutableEnchantmentData> {

    public ItemEnchantmentDataProcessor() {
        super(input -> true, Keys.ITEM_ENCHANTMENTS);
    }

    @Override
    protected EnchantmentData createManipulator() {
        return new SpongeEnchantmentData();
    }

    @Override
    protected boolean set(final ItemStack itemStack, final List<Enchantment> value) {
        final CompoundNBT compound;
        if (itemStack.func_77978_p() == null) {
            compound = new CompoundNBT();
            itemStack.func_77982_d(compound);
        } else {
            compound = itemStack.func_77978_p();
        }

        if (value.isEmpty()) { // if there's no enchantments, remove the tag that says there's enchantments
            compound.func_82580_o(Constants.Item.ITEM_ENCHANTMENT_LIST);
        } else {
            final Map<EnchantmentType, Integer> valueMap = Maps.newLinkedHashMap();
            for (final Enchantment enchantment : value) { // convert ItemEnchantment to map
                valueMap.put(enchantment.getType(), enchantment.getLevel());
            }
            final ListNBT newList = new ListNBT(); // construct the enchantment list
            for (final Map.Entry<EnchantmentType, Integer> entry : valueMap.entrySet()) {
                final CompoundNBT enchantmentCompound = new CompoundNBT();
                enchantmentCompound.func_74777_a(Constants.Item.ITEM_ENCHANTMENT_ID,
                    (short) net.minecraft.enchantment.Enchantment.func_185258_b((net.minecraft.enchantment.Enchantment) entry.getKey()));
                enchantmentCompound.func_74777_a(Constants.Item.ITEM_ENCHANTMENT_LEVEL, entry.getValue().shortValue());
                newList.func_74742_a(enchantmentCompound);
            }
            compound.func_74782_a(Constants.Item.ITEM_ENCHANTMENT_LIST, newList);
        }

        return true;
    }

    @Override
    protected Optional<List<Enchantment>> getVal(final ItemStack itemStack) {
        if (itemStack.func_77948_v()) {
            return Optional.of(Constants.NBT.getItemEnchantments(itemStack));
        }
        return Optional.empty();
    }

    @Override
    protected ListValue<Enchantment> constructValue(final List<Enchantment> actualValue) {
        return new SpongeListValue<>(Keys.ITEM_ENCHANTMENTS, actualValue);
    }

    @Override
    protected ImmutableValue<List<Enchantment>> constructImmutableValue(final List<Enchantment> value) {
        return new ImmutableSpongeListValue<>(Keys.ITEM_ENCHANTMENTS, ImmutableList.copyOf(value));
    }

    @Override
    public Optional<EnchantmentData> fill(final DataContainer container, final EnchantmentData enchantmentData) {
        checkDataExists(container, Keys.ITEM_ENCHANTMENTS.getQuery());
        final List<Enchantment> enchantments = container.getSerializableList(Keys.ITEM_ENCHANTMENTS.getQuery(), Enchantment.class).get();
        final ListValue<Enchantment> existing = enchantmentData.enchantments();
        existing.addAll(enchantments);
        enchantmentData.set(existing);
        return Optional.of(enchantmentData);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            final ItemStack stack = (ItemStack) container;
            final Optional<List<Enchantment>> old = getVal(stack);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            stack.func_77978_p().func_82580_o(Constants.Item.ITEM_ENCHANTMENT_LIST);
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }

}
