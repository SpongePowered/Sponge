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
package org.spongepowered.common.data.processor.item;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;

import com.google.common.base.Optional;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.item.AuthorComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.item.SpongeAuthorComponent;

public class SpongeAuthorProcessor implements SpongeDataProcessor<AuthorComponent> {

    @Override
    public Optional<AuthorComponent> getFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack)) {
            return Optional.absent();
        }
        if (((ItemStack) dataHolder).getItem() != Items.written_book) {
            return Optional.absent();
        }
        if (!((ItemStack) dataHolder).hasTagCompound()) {
            return Optional.absent();
        }
        if (!((ItemStack) dataHolder).getTagCompound().hasKey("author")) {
            return Optional.absent();
        }
        final String rawAuthor = ((ItemStack) dataHolder).getTagCompound().getString("author");
        return Optional.of(create().setValue(Texts.of(rawAuthor)));
    }

    @Override
    public Optional<AuthorComponent> fillData(DataHolder dataHolder, AuthorComponent manipulator, DataPriority priority) {
        if (dataHolder instanceof org.spongepowered.api.item.inventory.ItemStack) {
            if (((ItemStack) dataHolder).getItem() != Items.written_book) {
                return Optional.absent();
            }
            switch (checkNotNull(priority)) {
                case PRE_MERGE:
                case COMPONENT:
                    return Optional.of(manipulator);
                default:
                    manipulator.setValue(Texts.of(((ItemStack) dataHolder).getTagCompound().getString("author")));
                    return Optional.of(manipulator);
            }
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, AuthorComponent manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() == Items.written_book) {
            ((ItemStack) dataHolder).setTagInfo("author", new NBTTagString(Texts.toPlain(manipulator.getValue())));
            return successNoData();
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() == Items.written_book) {
            ((ItemStack) dataHolder).setTagInfo("author", new NBTTagString("invalid"));
            return true;
        }
        return false;
    }

    @Override
    public Optional<AuthorComponent> build(DataView container) throws InvalidDataException {
        if (!checkNotNull(container).contains(Tokens.BOOK_AUTHOR.getQuery())) {
            throw new InvalidDataException("Missing author to construct an AuthorData.");
        }
        final String rawAuthor = container.getString(Tokens.BOOK_AUTHOR.getQuery()).get();
        final AuthorComponent data = create();
        data.setValue(Texts.of(rawAuthor));
        return Optional.of(data);
    }

    @Override
    public AuthorComponent create() {
        return new SpongeAuthorComponent();
    }

    @Override
    public Optional<AuthorComponent> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack)) {
            return Optional.absent();
        }
        if (((ItemStack) dataHolder).getItem() != Items.written_book) {
            return Optional.absent();
        }
        if (!((ItemStack) dataHolder).hasTagCompound()) {
            return Optional.absent();
        }
        final String rawAuthor = ((ItemStack) dataHolder).getTagCompound().getString("author");
        return Optional.of(create().setValue(Texts.of(rawAuthor)));
    }
}