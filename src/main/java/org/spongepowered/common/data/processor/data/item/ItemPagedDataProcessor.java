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

import net.minecraft.item.Items;
import net.minecraft.nbt.StringNBT;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.text.SpongeTexts;

public class ItemPagedDataProcessor extends AbstractItemBookPagesProcessor<Text, PagedData, ImmutablePagedData> {

    public ItemPagedDataProcessor() {
        super(input -> input.getItem() == Items.WRITABLE_BOOK || input.getItem() == Items.WRITTEN_BOOK, Keys.BOOK_PAGES);
    }

    @Override
    StringNBT translateTo(final Text type) {
        return new StringNBT(TextSerializers.JSON.serialize(type));
    }

    @Override
    Text translateFrom(final String page) {
        try {
            // Book text should be in JSON, but there is a chance it might be in
            // Legacy format.
            return TextSerializers.JSON.deserialize(page);
        } catch (final TextParseException ignored) {
            return SpongeTexts.fromLegacy(page);
        }
    }


    @Override
    protected PagedData createManipulator() {
        return new SpongePagedData();
    }

}
