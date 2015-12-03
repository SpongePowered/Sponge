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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Optional;

public class ItemLoreValueProcessor extends AbstractSpongeValueProcessor<ItemStack, List<Text>, ListValue<Text>> {

    public ItemLoreValueProcessor() {
        super(ItemStack.class, Keys.ITEM_LORE);
    }

    @Override
    protected ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<>(Keys.ITEM_LORE, defaultValue);
    }

    @Override
    protected boolean set(ItemStack container, List<Text> value) {
        NbtDataUtil.setLoreToNBT(container, value);
        return true;
    }

    @Override
    protected Optional<List<Text>> getVal(ItemStack container) {
        final NBTTagCompound subCompound = container.getSubCompound(NbtDataUtil.ITEM_DISPLAY, false);
        if (subCompound == null) {
            return Optional.empty();
        }
        if (!subCompound.hasKey(NbtDataUtil.ITEM_LORE, NbtDataUtil.TAG_LIST)) {
            return Optional.empty();
        }
        return Optional.of(NbtDataUtil.getLoreFromNBT(subCompound));
    }

    @Override
    protected ImmutableValue<List<Text>> constructImmutableValue(List<Text> value) {
        return new ImmutableSpongeListValue<>(Keys.ITEM_LORE, ImmutableList.copyOf(value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> lore = new ImmutableSpongeListValue<>(Keys.ITEM_LORE, ImmutableList.copyOf(oldData.get()));
                builder.replace(lore);
            }
            NbtDataUtil.removeLoreFromNBT((ItemStack) container);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionResult.failNoData();
    }
}
