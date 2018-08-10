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
package org.spongepowered.common.text.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class BookViewImpl implements BookView {

    final Text title;
    final Text author;
    final ImmutableList<Text> pages;

    BookViewImpl(final Text title, final Text author, final ImmutableList<Text> pages) {
        this.title = title;
        this.pages = pages;
        this.author = author;
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    @Override
    public Text getAuthor() {
        return this.author;
    }

    @Override
    public ImmutableList<Text> getPages() {
        return this.pages;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final List<DataContainer> pages = this.pages.stream().map(Text::toContainer).collect(Collectors.toList());
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.getContentVersion())
                .set(Queries.TEXT_TITLE, this.title.toContainer())
                .set(Queries.TEXT_AUTHOR, this.author.toContainer())
                .set(Queries.TEXT_PAGE_LIST, pages);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("title", this.title)
                .add("author", this.author)
                .add("pages", this.pages)
                .toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BookViewImpl)) {
            return false;
        }
        final BookViewImpl that = (BookViewImpl) other;
        return this.title.equals(that.title) && this.author.equals(that.author) && this.pages.equals(that.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.title, this.author, this.pages);
    }

    public static final class Builder implements BookView.Builder {

        Text title = Text.empty();
        Text author = Text.empty();
        List<Text> pages = new ArrayList<>();

        @Override
        public BookView.Builder title(final Text title) {
            this.title = checkNotNull(title, "title");
            return this;
        }

        @Override
        public BookView.Builder author(final Text author) {
            this.author = checkNotNull(author, "author");
            return this;
        }

        @Override
        public BookView.Builder addPage(final Text page) {
            this.pages.add(checkNotNull(page, "page"));
            return this;
        }

        @Override
        public BookView.Builder addPages(final Collection<Text> pages) {
            this.pages.addAll(checkNotNull(pages, "pages"));
            return this;
        }

        @Override
        public BookView.Builder addPages(final Text... pages) {
            this.addPages(Arrays.asList(checkNotNull(pages, "pages")));
            return this;
        }

        @Override
        public BookView.Builder insertPage(final int i, final Text page) {
            this.pages.add(i, checkNotNull(page, "page"));
            return this;
        }

        @Override
        public BookView.Builder insertPages(final int i, final Collection<Text> pages) {
            this.pages.addAll(i, checkNotNull(pages, "pages"));
            return this;
        }

        @Override
        public BookView.Builder insertPages(final int i, final Text... pages) {
            this.insertPages(i, Arrays.asList(checkNotNull(pages, "pages")));
            return this;
        }

        @Override
        public BookView.Builder removePage(final Text page) {
            this.pages.remove(checkNotNull(page, "page"));
            return this;
        }

        @Override
        public BookView.Builder removePage(final int i) {
            this.pages.remove(i);
            return this;
        }

        @Override
        public BookView.Builder removePages(final Collection<Text> pages) {
            this.pages.removeAll(checkNotNull(pages, "pages"));
            return this;
        }

        @Override
        public BookView.Builder removePages(final Text... pages) {
            this.removePages(Arrays.asList(checkNotNull(pages, "pages")));
            return this;
        }

        @Override
        public BookView.Builder clearPages() {
            this.pages.clear();
            return this;
        }

        @Override
        public BookView build() {
            return new BookViewImpl(this.title, this.author, ImmutableList.copyOf(this.pages));
        }

        @Override
        public BookView.Builder from(final BookView value) {
            this.title = value.getTitle();
            this.author = value.getAuthor();
            this.pages = value.getPages();
            return this;
        }

        @Override
        public BookView.Builder reset() {
            this.title = Text.empty();
            this.author = Text.empty();
            this.pages = new ArrayList<>();
            return this;
        }
    }
}
