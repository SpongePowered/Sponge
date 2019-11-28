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
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeSignData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class ItemSignDataProcessor extends AbstractItemSingleDataProcessor<List<Text>, ListValue<Text>, SignData, ImmutableSignData> {

    public ItemSignDataProcessor() {
        super(stack -> stack.func_77973_b().equals(Items.field_151155_ap), Keys.SIGN_LINES);
    }

    @Override
    protected Optional<List<Text>> getVal(final ItemStack itemStack) {
        final NBTTagCompound mainCompound = itemStack.func_77978_p();
        if (mainCompound == null) {
            return Optional.empty();
        }
        if (!mainCompound.func_150297_b(Constants.Item.BLOCK_ENTITY_TAG, Constants.NBT.TAG_COMPOUND)
                || !mainCompound.func_74775_l(Constants.Item.BLOCK_ENTITY_TAG).func_74764_b(Constants.Item.BLOCK_ENTITY_ID)) {
            return Optional.empty();
        }
        final NBTTagCompound tileCompound = mainCompound.func_74775_l(Constants.Item.BLOCK_ENTITY_TAG);
        final String id = tileCompound.func_74779_i(Constants.Item.BLOCK_ENTITY_ID);
        if (!id.equalsIgnoreCase(Constants.TileEntity.SIGN)) {
            return Optional.empty();
        }
        final List<Text> texts = Lists.newArrayListWithCapacity(4);
        for (int i = 0; i < 4; i++) {
            texts.add(SpongeTexts.fromLegacy(tileCompound.func_74779_i("Text" + (i + 1))));
        }
        return Optional.of(texts);
    }

    @Override
    public Optional<SignData> fill(final DataContainer container, final SignData signData) {
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
                for (final String line : lines) {
                    if (lineNum >= 4) {
                        break;
                    }
                    lineNum++;
                    textLines.add(TextSerializers.JSON.deserialize(line));
                }
            }
        } catch (final Exception e) {
            throw new InvalidDataException("Could not translate text json lines", e);
        }
        return Optional.of(signData.set(Keys.SIGN_LINES, textLines));
    }

    @Override
    protected boolean set(final ItemStack itemStack, final List<Text> lines) {
        final NBTTagCompound signCompound = itemStack.func_190925_c(Constants.Item.BLOCK_ENTITY_TAG);
        signCompound.func_74778_a(Constants.Item.BLOCK_ENTITY_ID, Constants.TileEntity.SIGN);
        for (int i = 0; i < 4; i++) {
            final Text line = lines.size() > i ? lines.get(i) : Text.EMPTY;
            if (line == null) {
                throw new IllegalArgumentException("A null line was given at index " + i);
            }
            signCompound.func_74778_a("Text" + (i + 1), TextSerializers.JSON.serialize(line));
        }
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (!supports(container)) {
            return DataTransactionResult.failNoData();
        }
        final ItemStack itemStack = (ItemStack) container;
        final Optional<List<Text>> old = getVal(itemStack);
        if (!old.isPresent()) {
            return DataTransactionResult.successNoData();
        }
        try {
            if (itemStack.func_77942_o()) {
                itemStack.func_77978_p().func_82580_o(Constants.Item.BLOCK_ENTITY_TAG);
            }
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        } catch (final Exception e) {
            return DataTransactionResult.builder().result(DataTransactionResult.Type.ERROR).build();
        }
    }

    @Override
    protected ListValue<Text> constructValue(final List<Text> defaultValue) {
        return new SpongeListValue<>(Keys.SIGN_LINES, defaultValue);
    }

    @Override
    protected ImmutableValue<List<Text>> constructImmutableValue(final List<Text> value) {
        return new ImmutableSpongeListValue<>(Keys.SIGN_LINES, ImmutableList.copyOf(value));
    }

    @Override
    protected SignData createManipulator() {
        return new SpongeSignData();
    }

}
