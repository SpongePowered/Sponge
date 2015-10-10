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
package org.spongepowered.common.data.builder.manipulator.mutable.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableAuthorData;
import org.spongepowered.api.data.manipulator.mutable.item.AuthorData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeAuthorData;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Optional;

public class ItemAuthorDataBuilder implements DataManipulatorBuilder<AuthorData, ImmutableAuthorData> {

    @Override
    public Optional<AuthorData> build(DataView container) {
        if (container.contains(Keys.BOOK_AUTHOR.getQuery())) {
            final String json = DataUtil.getData(container, Keys.BOOK_AUTHOR, String.class);
            final Text author = Texts.json().fromUnchecked(json);
            return Optional.of(new SpongeAuthorData(author));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public AuthorData create() {
        return new SpongeAuthorData();
    }

    @Override
    public Optional<AuthorData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() != Items.writable_book && ((ItemStack) dataHolder).getItem() != Items.written_book) {
                return Optional.empty();
            }
            if (!((ItemStack) dataHolder).hasTagCompound() || !((ItemStack) dataHolder).getTagCompound().hasKey(NbtDataUtil.ITEM_BOOK_AUTHOR)) {
                return Optional.of(new SpongeAuthorData());
            }
            final String json = ((ItemStack) dataHolder).getTagCompound().getString(NbtDataUtil.ITEM_BOOK_AUTHOR);
            final Text author = Texts.json().fromUnchecked(json);
            return Optional.of(new SpongeAuthorData(author));
        } else {
            return Optional.empty();
        }
    }

}
