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

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeLoreData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class ItemLoreDataProcessor extends AbstractItemSingleDataProcessor<List<Text>, ListValue<Text>, LoreData, ImmutableLoreData> {

    public ItemLoreDataProcessor() {
        super(Predicates.<ItemStack>alwaysTrue(), Keys.ITEM_LORE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<LoreData> fill(DataContainer container, LoreData loreData) {
        final List<String> json = DataUtil.getData(container, Keys.ITEM_LORE, List.class);
        return Optional.of(loreData.set(Keys.ITEM_LORE, SpongeTexts.fromJson(json)));
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<LoreData> data = from(dataHolder);
            if (data.isPresent()) {
                try {
                    NbtDataUtil.removeLoreFromNBT((ItemStack) dataHolder);
                    return builder.replace(data.get().getValues()).result(Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue removing the lore from an itemstack!", e);
                    return builder.result(Type.ERROR).build();
                }
            } else {
                return builder.result(Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected LoreData createManipulator() {
      return new SpongeLoreData();
    }

    @Override
    protected boolean set(ItemStack itemStack, List<Text> value) {
        NbtDataUtil.setLoreToNBT(itemStack, value);
        return true;
    }

    @Override
    protected Optional<List<Text>> getVal(ItemStack itemStack) {
        final NBTTagCompound subCompound = itemStack.getSubCompound(NbtDataUtil.ITEM_DISPLAY, false);
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
       return new ImmutableSpongeListValue<Text>(Keys.ITEM_LORE, ImmutableList.copyOf(value));
    }

}
