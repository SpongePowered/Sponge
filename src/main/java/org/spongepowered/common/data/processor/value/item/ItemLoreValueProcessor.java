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
package org.spongepowered.common.data.processor.value.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Optional;

public class ItemLoreValueProcessor extends AbstractSpongeValueProcessor<List<Text>, ListValue<Text>> {

    public ItemLoreValueProcessor() {
        super(Keys.ITEM_LORE);
    }

    @Override
    protected ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<Text>(Keys.ITEM_LORE, defaultValue);
    }

    @Override
    public Optional<List<Text>> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            final ItemStack itemStack = (ItemStack) container;
            final NBTTagCompound subCompound = itemStack.getSubCompound(NbtDataUtil.ITEM_DISPLAY, false);
            if (subCompound == null) {
                return Optional.empty();
            }
            if (!subCompound.hasKey(NbtDataUtil.ITEM_LORE, NbtDataUtil.TAG_LIST)) {
                return Optional.empty();
            }
            return Optional.of(NbtDataUtil.getLoreFromNBT(subCompound));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof ItemStack;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, List<Text> value) {
        final ImmutableListValue<Text> lore = new ImmutableSpongeListValue<Text>(Keys.ITEM_LORE, ImmutableList.copyOf(value));
        if (this.supports(container)) {
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> oldLore =
                        new ImmutableSpongeListValue<Text>(Keys.ITEM_LORE, ImmutableList.copyOf(oldData.get()));
                builder.replace(oldLore);
            }
            NbtDataUtil.setLoreToNBT((ItemStack) container, lore.get());
            builder.success(lore);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionBuilder.failResult(lore);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> lore = new ImmutableSpongeListValue<Text>(Keys.ITEM_LORE, ImmutableList.copyOf(oldData.get()));
                builder.replace(lore);
            }
            NbtDataUtil.removeLoreFromNBT((ItemStack) container);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionBuilder.failNoData();
    }
}
