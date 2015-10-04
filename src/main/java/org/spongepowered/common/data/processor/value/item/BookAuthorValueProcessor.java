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

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.text.IMixinText;

import java.util.Locale;
import java.util.Optional;

public class BookAuthorValueProcessor extends AbstractSpongeValueProcessor<Text, Value<Text>> {

    public BookAuthorValueProcessor() {
        super(Keys.BOOK_AUTHOR);
    }

    @Override
    public Optional<Text> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            final ItemStack itemStack = (ItemStack) container;
            if (!itemStack.hasTagCompound() || !itemStack.getTagCompound().hasKey(NbtDataUtil.ITEM_BOOK_AUTHOR)) {
                return Optional.empty();
            }
            final String json = itemStack.getTagCompound().getString(NbtDataUtil.ITEM_BOOK_AUTHOR);
            final Text author = Texts.json().fromUnchecked(json);
            return Optional.<Text>of(author);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            return ((ItemStack) container).getItem() == Items.writable_book || ((ItemStack) container).getItem() == Items.written_book;
        } else {
            return false;
        }
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Text value) {
        final ImmutableValue<Text> author = new ImmutableSpongeValue<Text>(Keys.BOOK_AUTHOR, value);
        if (this.supports(container)) {
            final Optional<Text> oldData = getValueFromContainer(container);
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            if (oldData.isPresent()) {
                final ImmutableValue<Text> oldAuthor =
                        new ImmutableSpongeValue<Text>(Keys.BOOK_AUTHOR, oldData.get());
                builder.replace(oldAuthor);
            }
            if (((ItemStack) container).getTagCompound() == null) {
                ((ItemStack) container).setTagCompound(new NBTTagCompound());
            }
            ((ItemStack) container).getTagCompound().setString(NbtDataUtil.ITEM_BOOK_AUTHOR, ((IMixinText) value).toLegacy('\247', Locale.ENGLISH));
            builder.success(author);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionBuilder.failResult(author);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<Text> oldData = getValueFromContainer(container);
            if (oldData.isPresent()) {
                final ImmutableValue<Text> author = new ImmutableSpongeValue<Text>(Keys.BOOK_AUTHOR, oldData.get());
                builder.replace(author);
            }
            if (((ItemStack) container).getTagCompound() != null) {
                ((ItemStack) container).setTagCompound(new NBTTagCompound());
            }
            ((ItemStack) container).getTagCompound().setString(NbtDataUtil.ITEM_BOOK_AUTHOR, NbtDataUtil.INVALID_TITLE);
            return builder.result(DataTransactionResult.Type.SUCCESS).build();

        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<Text> constructValue(Text defaultValue) {
        return new SpongeValue<Text>(Keys.BOOK_AUTHOR, defaultValue);
    }

}
