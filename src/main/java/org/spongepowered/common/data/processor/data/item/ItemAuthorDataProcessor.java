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

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.Type;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableAuthorData;
import org.spongepowered.api.data.manipulator.mutable.item.AuthorData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeAuthorData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.interfaces.text.IMixinText;

import java.util.Locale;
import java.util.Optional;

public class ItemAuthorDataProcessor extends AbstractItemSingleDataProcessor<Text, Value<Text>, AuthorData, ImmutableAuthorData> {

    public ItemAuthorDataProcessor() {
        super(input -> {
            return input.getItem() == Items.writable_book || input.getItem() == Items.written_book;
        }, Keys.BOOK_AUTHOR);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<AuthorData> data = from(dataHolder);
            try {
                if (((ItemStack) dataHolder).getTagCompound() == null) {
                    ((ItemStack) dataHolder).setTagCompound(new NBTTagCompound());
                }
                ((ItemStack) dataHolder).getTagCompound().setString(NbtDataUtil.ITEM_BOOK_AUTHOR, NbtDataUtil.INVALID_TITLE);

                return builder.replace(data.get().getValues()).result(Type.SUCCESS).build();
            } catch (Exception e) {
                Sponge.getLogger().error("There was an issue removing the author of an itemstack!", e);
                return builder.result(Type.ERROR).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected AuthorData createManipulator() {
        return new SpongeAuthorData();
    }

    @Override
    protected boolean set(ItemStack itemStack, Text value) {
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setString(NbtDataUtil.ITEM_BOOK_AUTHOR, ((IMixinText) value).toLegacy('\247', Locale.ENGLISH));
        return true;
    }

    @Override
    protected Optional<Text> getVal(ItemStack itemStack) {
        if (!itemStack.hasTagCompound() || !itemStack.getTagCompound().hasKey(NbtDataUtil.ITEM_BOOK_AUTHOR)) {
            return Optional.empty();
        }
        final String json = itemStack.getTagCompound().getString(NbtDataUtil.ITEM_BOOK_AUTHOR);
        final Text author = Texts.json().fromUnchecked(json);
        return Optional.<Text>of(author);
    }

    @Override
    protected ImmutableValue<Text> constructImmutableValue(Text value) {
        return new ImmutableSpongeValue<Text>(Keys.BOOK_AUTHOR, value);
    }

}
