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

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemDisplayNameValueProcessor extends AbstractSpongeValueProcessor<ItemStack, Text, Value<Text>> {

    public ItemDisplayNameValueProcessor() {
        super(ItemStack.class, Keys.DISPLAY_NAME);
    }

    @Override
    protected Value<Text> constructValue(final Text defaultValue) {
        return new SpongeValue<>(Keys.DISPLAY_NAME, Text.of(), defaultValue);
    }

    @Override
    protected boolean set(final ItemStack container, final Text value) {
        final String legacy = SpongeTexts.toLegacy(value);
        if (container.getItem() == Items.WRITTEN_BOOK) {
            container.setTagInfo(Constants.Item.Book.ITEM_BOOK_TITLE, new StringNBT(legacy));
        } else {
            container.setStackDisplayName(legacy);
        }
        return true;
    }

    @Override
    protected Optional<Text> getVal(final ItemStack container) {
        if (container.getItem() == Items.WRITTEN_BOOK) {
            final CompoundNBT mainCompound = container.getTag();
            if (mainCompound == null) {
                return Optional.empty(); // Basically, this book wasn't initialized properly.
            }
            final String titleString = mainCompound.getString(Constants.Item.Book.ITEM_BOOK_TITLE);
            return Optional.of(SpongeTexts.fromLegacy(titleString));
        }
        final CompoundNBT mainCompound = container.getChildTag(Constants.Item.ITEM_DISPLAY);
        if (mainCompound != null && mainCompound.contains(Constants.Item.ITEM_DISPLAY_NAME, Constants.NBT.TAG_STRING)) {
            final String displayString = mainCompound.getString(Constants.Item.ITEM_DISPLAY_NAME);
            return Optional.of(SpongeTexts.fromLegacy(displayString));
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<Text> constructImmutableValue(final Text value) {
        return new ImmutableSpongeValue<>(Keys.DISPLAY_NAME, Text.of(), value);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<Text> optional = getValueFromContainer(container);
            if (optional.isPresent()) {
                try {
                    ((ItemStack) container).clearCustomName();
                    return builder.replace(new ImmutableSpongeValue<>(Keys.DISPLAY_NAME, optional.get())).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (final Exception e) {
                    SpongeImpl.getLogger().error("There was an issue removing the displayname from an itemstack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            }
            return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failNoData();    }
}
