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
package org.spongepowered.common.data.provider.item.stack;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.Collections;

public final class BookItemStackData {

    private BookItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.AUTHOR)
                        .get(h -> {
                            final WrittenBookContent content = h.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            if (content == null) {
                                return null;
                            }
                            return LegacyComponentSerializer.legacySection().deserialize(content.author());
                        })
                        .set((h, v) -> {
                            final WrittenBookContent content = h.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            // TODO handle missing data?
                            final String author = LegacyComponentSerializer.legacySection().serialize(v);
                            h.set(DataComponents.WRITTEN_BOOK_CONTENT,
                                    new WrittenBookContent(content.title(), author, content.generation(), content.pages(), content.resolved()));
                        })
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.GENERATION)
                        .get(h -> {
                            final WrittenBookContent content = h.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            if (content == null) {
                                return null;
                            }
                            return content.generation();
                        })
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            final WrittenBookContent content = h.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            // TODO handle missing data?
                            h.set(DataComponents.WRITTEN_BOOK_CONTENT,
                                    new WrittenBookContent(content.title(), content.author(), v, content.pages(), content.resolved()));
                            return true;
                        })
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.PAGES)
                        .get(h -> {
                            final WrittenBookContent content = h.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            if (content == null) {
                                return null;
                            }
                            return content.pages().stream().map(Filterable::raw).map(SpongeAdventure::asAdventure).toList();
                        })
                        .set((h, v) -> {
                            final WrittenBookContent content = h.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            // TODO handle missing data?
                            var pages = v.stream().map(SpongeAdventure::asVanilla).map(Filterable::passThrough).toList();
                            h.set(DataComponents.WRITTEN_BOOK_CONTENT,
                                    new WrittenBookContent(content.title(), content.author(), content.generation(), pages, content.resolved()));
                        })
                        .delete(h -> {
                            final WrittenBookContent content = h.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            // TODO handle missing data?
                            h.set(DataComponents.WRITTEN_BOOK_CONTENT,
                                    new WrittenBookContent(content.title(), content.author(), content.generation(), Collections.emptyList(), content.resolved()));
                        })
                        .supports(h -> h.getItem() == Items.WRITTEN_BOOK)
                    .create(Keys.PLAIN_PAGES)
                        .get(h -> {
                            final WritableBookContent content = h.get(DataComponents.WRITABLE_BOOK_CONTENT);
                            if (content == null) {
                                return null;
                            }
                            return content.pages().stream().map(Filterable::raw).toList();
                        })
                        .set((h, v) -> h.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(v.stream().map(Filterable::passThrough).toList())))
                        .delete(h -> h.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(Collections.emptyList())))
                        .supports(h -> h.getItem() == Items.WRITABLE_BOOK);
    }
    // @formatter:on

}
