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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeLoreData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeLoreData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.List;

public class LoreDataProcessor extends AbstractSpongeDataProcessor<LoreData, ImmutableLoreData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof ItemStack;
    }

    @Override
    public Optional<LoreData> from(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            if (dataHolder.get(Keys.ITEM_LORE).isPresent()) {
                final ItemStack itemStack = (ItemStack) dataHolder;
                final NBTTagCompound subCompound = itemStack.getSubCompound(NbtDataUtil.DISPLAY, false);
                if (subCompound == null) {
                    return Optional.absent();
                }
                if (!subCompound.hasKey(NbtDataUtil.LORE, NbtDataUtil.TAG_LIST)) {
                    return Optional.absent();
                }
                return Optional.<LoreData>of(new SpongeLoreData(NbtDataUtil.getLoreFromNBT(subCompound)));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<LoreData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            final ItemStack itemStack = (ItemStack) dataHolder;
            final NBTTagCompound subCompound = itemStack.getSubCompound(NbtDataUtil.DISPLAY, false);
            if (subCompound == null) {
                return Optional.absent();
            }
            if (!subCompound.hasKey(NbtDataUtil.LORE, NbtDataUtil.TAG_LIST)) {
                return Optional.absent();
            }
            return Optional.<LoreData>of(new SpongeLoreData(NbtDataUtil.getLoreFromNBT(subCompound)));
        }
        return Optional.absent();
    }

    @Override
    public Optional<LoreData> fill(DataHolder dataHolder, LoreData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final LoreData data = from(dataHolder).orNull();
            final LoreData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final List<Text> lore = newData.get(Keys.ITEM_LORE).get();
            return Optional.of(manipulator.set(Keys.ITEM_LORE, lore));
        }
        return Optional.absent();
    }

    @Override
    public Optional<LoreData> fill(DataContainer container, LoreData loreData) {
        final List<String> json = DataUtil.getData(container, Keys.ITEM_LORE, List.class);
        final List<Text> lore = Lists.newArrayList();
        for (String str : json) {
            lore.add(Texts.json().fromUnchecked(str));
        }
        return Optional.of(loreData.set(Keys.ITEM_LORE, lore));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, LoreData manipulator, MergeFunction function) {
        if (!supports(dataHolder)) {
            return DataTransactionBuilder.failResult(manipulator.getValues());
        }
        try {
            Optional<LoreData> data = from(dataHolder);
            final LoreData newData = checkNotNull(function).merge(data.orNull(), manipulator);
            NbtDataUtil.setLoreToNBT((ItemStack) dataHolder, newData.lore().get());
            if (data.isPresent()) {
                return DataTransactionBuilder.successReplaceResult(newData.getValues(), data.get().getValues());
            } else {
                return DataTransactionBuilder.builder().success(newData.getValues()).build();
            }
        } catch (Exception e) {
            return DataTransactionBuilder.builder().reject(manipulator.getValues()).result(Type.ERROR).build();
        }
    }

    @Override
    public Optional<ImmutableLoreData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableLoreData immutable) {
        if (key == Keys.ITEM_LORE) {
            return Optional.<ImmutableLoreData>of(new ImmutableSpongeLoreData(immutable.lore().get()));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<LoreData> data = from(dataHolder);
            if (data.isPresent()) {
                try {
                    NbtDataUtil.removeLoreFromNBT((ItemStack) dataHolder);
                    return builder.replace(data.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue removing the lore from an itemstack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

}
