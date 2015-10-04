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
package org.spongepowered.common.data.processor.value;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemEnchantmentValueProcessor extends AbstractSpongeValueProcessor<List<ItemEnchantment>, ListValue<ItemEnchantment>> {

    public ItemEnchantmentValueProcessor() {
        super(Keys.ITEM_ENCHANTMENTS);
    }

    @Override
    protected ListValue<ItemEnchantment> constructValue(List<ItemEnchantment> defaultValue) {
        return new SpongeListValue<ItemEnchantment>(Keys.ITEM_ENCHANTMENTS, defaultValue);
    }

    @Override
    public Optional<List<ItemEnchantment>> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            if (!((ItemStack) container).isItemEnchanted()) {
                return Optional.empty();
            } else {
                final List<ItemEnchantment> enchantments = Lists.newArrayList();
                final NBTTagList list = ((ItemStack) container).getEnchantmentTagList();
                for (int i = 0; i < list.tagCount(); i++) {
                    final NBTTagCompound compound = list.getCompoundTagAt(i);
                    final short enchantmentId = compound.getShort(NbtDataUtil.ITEM_ENCHANTMENT_ID);
                    final short level = compound.getShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL);

                    final Enchantment enchantment = (Enchantment) net.minecraft.enchantment.Enchantment.getEnchantmentById(enchantmentId);
                    enchantments.add(new ItemEnchantment(enchantment, level));
                }
                return Optional.of(enchantments);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof ItemStack;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, List<ItemEnchantment> value) {
        if (container instanceof ItemStack) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            if (((ItemStack) container).isItemEnchanted()) {
                builder.replace(constructValue(getValueFromContainer(container).get()).asImmutable());
            }
            final NBTTagCompound compound;
            if (((ItemStack) container).getTagCompound() == null) {
                compound = new NBTTagCompound();
                ((ItemStack) container).setTagCompound(compound);
            } else {
                compound = ((ItemStack) container).getTagCompound();
            }
            final NBTTagList enchantments = compound.getTagList(NbtDataUtil.ITEM_ENCHANTMENT_LIST, NbtDataUtil.TAG_COMPOUND);
            final Map<Enchantment, Integer> mergedMap = Maps.newLinkedHashMap(); // We need to retain insertion order.
            if (enchantments.tagCount() != 0) {
                for (int i = 0; i < enchantments.tagCount(); i++) { // we have to filter out the enchantments we're replacing...
                    final NBTTagCompound enchantmentCompound = enchantments.getCompoundTagAt(i);
                    final short enchantmentId = enchantmentCompound.getShort(NbtDataUtil.ITEM_ENCHANTMENT_ID);
                    final short level = enchantmentCompound.getShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL);
                    final Enchantment enchantment = (Enchantment) net.minecraft.enchantment.Enchantment.getEnchantmentById(enchantmentId);
                    mergedMap.put(enchantment, (int) level);
                }
            }
            for (ItemEnchantment enchantment : value) {
                mergedMap.put(enchantment.getEnchantment(), enchantment.getLevel());
            }
            final NBTTagList newList = new NBTTagList(); // Reconstruct the newly merged enchantment list
            for (Map.Entry<Enchantment, Integer> entry : mergedMap.entrySet()) {
                final NBTTagCompound enchantmentCompound = new NBTTagCompound();
                enchantmentCompound.setShort(NbtDataUtil.ITEM_ENCHANTMENT_ID, (short) ((net.minecraft.enchantment.Enchantment) entry.getKey()).effectId);
                enchantmentCompound.setShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL, entry.getValue().shortValue());
                newList.appendTag(enchantmentCompound);
            }
            compound.setTag(NbtDataUtil.ITEM_ENCHANTMENT_LIST, newList);
            builder.success(constructValue(value).asImmutable());
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            if (((ItemStack) container).isItemEnchanted()) {
                final DataTransactionBuilder builder = DataTransactionBuilder.builder();
                builder.replace(constructValue(getValueFromContainer(container).get()).asImmutable());
                ((ItemStack) container).getTagCompound().removeTag(NbtDataUtil.ITEM_ENCHANTMENT_LIST);
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }
}
