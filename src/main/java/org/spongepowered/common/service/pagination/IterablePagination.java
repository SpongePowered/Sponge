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
package org.spongepowered.common.service.pagination;

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.spongepowered.api.service.pagination.PaginationCalculator;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pagination occurring for an iterable -- we don't know its size.
 */
class IterablePagination extends ActivePagination {
    private final PeekingIterator<Map.Entry<Text, Integer>> countIterator;

    public IterablePagination(CommandSource src, PaginationCalculator<CommandSource> calc, Iterable<Map.Entry<Text, Integer>> counts, Text title,
            Text header, Text footer, String padding) {
        super(src, calc, title, header, footer, padding);
        this.countIterator = Iterators.peekingIterator(counts.iterator());
    }

    @Override
    protected Iterable<Text> getLines(int page) throws CommandException {
        if (!this.countIterator.hasNext()) {
            throw new CommandException(t("Already at end of iterator"));
        }
        List<Text> ret = new ArrayList<Text>(getMaxContentLinesPerPage());
        int addedLines = 0;
        while (addedLines <= getMaxContentLinesPerPage()) {
            if (!this.countIterator.hasNext()) {
                break;
            }
            if (addedLines + this.countIterator.peek().getValue() > getMaxContentLinesPerPage()) {
                break;
            }
            Map.Entry<Text, Integer> ent = this.countIterator.next();
            ret.add(ent.getKey());
            addedLines += ent.getValue();
        }
        return ret;
    }

    @Override
    protected boolean hasPrevious(int page) {
        return false;
    }

    @Override
    protected boolean hasNext(int page) {
        return page == getCurrentPage() && this.countIterator.hasNext();
    }

    @Override
    protected int getTotalPages() {
        return -1;
    }

    @Override
    public void previousPage() throws CommandException {
        throw new CommandException(t("Cannot go backwards in a streaming pagination"));
    }
}
