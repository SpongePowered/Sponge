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
import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class ItemSignLinesValueProcessor extends AbstractSpongeValueProcessor<ItemStack, List<Text>, ListValue<Text>> {

    public ItemSignLinesValueProcessor() {
        super(ItemStack.class, Keys.SIGN_LINES);
    }

    @Override
    protected ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<>(Keys.SIGN_LINES, defaultValue);
    }

    @Override
    protected boolean set(ItemStack container, List<Text> value) {
        final NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(container);
        final NBTTagCompound tileCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.BLOCK_ENTITY_TAG);
        tileCompound.setString(NbtDataUtil.BLOCK_ENTITY_ID, NbtDataUtil.SIGN);
        tileCompound.setString("Text1", TextSerializers.JSON.serialize(value.get(1)));
        tileCompound.setString("Text2", TextSerializers.JSON.serialize(value.get(2)));
        tileCompound.setString("Text3", TextSerializers.JSON.serialize(value.get(3)));
        tileCompound.setString("Text4", TextSerializers.JSON.serialize(value.get(4)));
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Optional<List<Text>> getVal(ItemStack container) {
        if (!container.hasTagCompound()) {
            return Optional.empty();
        } else {
            final NBTTagCompound mainCompound = container.getTagCompound();
            if (!mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG, NbtDataUtil.TAG_COMPOUND) || !mainCompound
                .getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG).hasKey(NbtDataUtil.BLOCK_ENTITY_ID)) {
                return Optional.empty();
            }
            final NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
            final String id = tileCompound.getString(NbtDataUtil.BLOCK_ENTITY_ID);
            if (!id.equalsIgnoreCase(NbtDataUtil.SIGN)) {
                return Optional.empty();
            }
            final List<Text> texts = Lists.newArrayListWithCapacity(4);
            texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text1")));
            texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text2")));
            texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text3")));
            texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text4")));
            return Optional.of(texts);
        }
    }

    @Override
    protected ImmutableValue<List<Text>> constructImmutableValue(List<Text> value) {
        return new ImmutableSpongeListValue<>(Keys.SIGN_LINES, ImmutableList.copyOf(value));
    }

    @Override
    protected boolean supports(ItemStack container) {
        return container.getItem().equals(Items.sign);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!((ItemStack) container).getItem().equals(Items.sign)) {
            return DataTransactionResult.failNoData();
        } else {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> immutableTexts =
                    new ImmutableSpongeListValue<>(Keys.SIGN_LINES, ImmutableList.copyOf(oldData.get()));
                builder.replace(immutableTexts);
            }
            if (!((ItemStack) container).hasTagCompound()) {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
            try {
                final Optional<NBTTagCompound> mainCompound = NbtDataUtil.getItemCompound(((ItemStack) container));
                if (mainCompound.isPresent()) {
                    mainCompound.get().removeTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                }
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        }
    }
}
