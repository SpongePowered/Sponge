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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.SignData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeSignData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.SpongeImmutableListValue;
import org.spongepowered.common.data.value.SpongeMutableListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class ItemSignDataProcessor extends AbstractItemSingleDataProcessor<List<Text>, SignData, ImmutableSignData> {

    public ItemSignDataProcessor() {
        super(stack -> stack.getItem().equals(Items.SIGN), Keys.SIGN_LINES);
    }

    @Override
    protected Optional<List<Text>> getVal(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            return Optional.empty();
        }
        final NBTTagCompound mainCompound = itemStack.getTagCompound();
        if (!mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG, NbtDataUtil.TAG_COMPOUND)
                || !mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG).hasKey(NbtDataUtil.BLOCK_ENTITY_ID)) {
            return Optional.empty();
        }
        final NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
        final String id = tileCompound.getString(NbtDataUtil.BLOCK_ENTITY_ID);
        if (!id.equalsIgnoreCase(NbtDataUtil.SIGN)) {
            return Optional.empty();
        }
        final List<Text> texts = Lists.newArrayListWithCapacity(4);
        for (int i = 0; i < 4; i++) {
            texts.add(SpongeTexts.fromLegacy(tileCompound.getString("Text" + (i + 1))));
        }
        return Optional.of(texts);
    }

    @Override
    public Optional<SignData> fill(DataContainer container, SignData signData) {
        if (!container.contains(Keys.SIGN_LINES.getQuery())) {
            return Optional.empty();
        }
        checkNotNull(signData);
        final List<String> lines = container.getStringList(Keys.SIGN_LINES.getQuery()).get();
        final List<Text> textLines = Lists.newArrayListWithCapacity(4);
        try {
            if (lines.isEmpty()) {
                for (int i = 0; i < 4; i++) {
                    textLines.set(i, Text.of());
                }

            } else {
                int lineNum = 0;
                for (String line : lines) {
                    if (lineNum >= 4) {
                        break;
                    }
                    lineNum++;
                    textLines.add(TextSerializers.JSON.deserialize(line));
                }
            }
        } catch (Exception e) {
            throw new InvalidDataException("Could not translate text json lines", e);
        }
        return Optional.of(signData.set(Keys.SIGN_LINES, textLines));
    }

    @Override
    protected boolean set(ItemStack itemStack, List<Text> lines) {
        final NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(itemStack);
        final NBTTagCompound tileCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.BLOCK_ENTITY_TAG);
        tileCompound.setString(NbtDataUtil.BLOCK_ENTITY_ID, NbtDataUtil.SIGN);
        for (int i = 0; i < 4; i++) {
            Text line = lines.size() > i ? lines.get(i) : Text.empty();
            if (line == null) {
                throw new IllegalArgumentException("A null line was given at index " + i);
            }
            tileCompound.setString("Text" + (i + 1), TextSerializers.JSON.serialize(line));
        }
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!supports(container)) {
            return DataTransactionResult.failNoData();
        }
        ItemStack itemStack = (ItemStack) container;
        Optional<List<Text>> old = getVal(itemStack);
        if (!old.isPresent()) {
            return DataTransactionResult.successNoData();
        }
        try {
            NbtDataUtil.getItemCompound(itemStack).get().removeTag(NbtDataUtil.BLOCK_ENTITY_TAG);
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        } catch (Exception e) {
            return DataTransactionResult.builder().result(DataTransactionResult.Type.ERROR).build();
        }
    }

    @Override
    protected Value.Mutable<List<Text>> constructMutableValue(List<Text> defaultValue) {
        return new SpongeMutableListValue<>(Keys.SIGN_LINES, defaultValue);
    }

    @Override
    protected Value.Immutable<List<Text>> constructImmutableValue(List<Text> value) {
        return new SpongeImmutableListValue<>(Keys.SIGN_LINES, ImmutableList.copyOf(value));
    }

    @Override
    protected SignData createManipulator() {
        return new SpongeSignData();
    }

}
