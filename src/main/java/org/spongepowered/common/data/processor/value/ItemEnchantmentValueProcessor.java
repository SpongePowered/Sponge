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

import com.google.common.collect.ImmutableList;
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
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemEnchantmentValueProcessor extends AbstractSpongeValueProcessor<ItemStack, List<ItemEnchantment>, ListValue<ItemEnchantment>> {

    public ItemEnchantmentValueProcessor() {
        super(ItemStack.class, Keys.ITEM_ENCHANTMENTS);
    }

    @Override
    protected ListValue<ItemEnchantment> constructValue(List<ItemEnchantment> defaultValue) {
        return new SpongeListValue<>(Keys.ITEM_ENCHANTMENTS, defaultValue);
    }

    @Override
    protected boolean set(ItemStack container, List<ItemEnchantment> value) {
        NbtDataUtil.setItemEnchantments(container, value);
        return false;
    }

    @Override
    protected Optional<List<ItemEnchantment>> getVal(ItemStack container) {
        return Optional.of(NbtDataUtil.getItemEnchantments(container));
    }

    @Override
    protected ImmutableValue<List<ItemEnchantment>> constructImmutableValue(List<ItemEnchantment> value) {
        return new ImmutableSpongeListValue<>(Keys.ITEM_ENCHANTMENTS, ImmutableList.copyOf(value));
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
