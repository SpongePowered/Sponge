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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.command.CommandMessageFormatting.error;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpongePaginationList implements PaginationList {

    private final SpongePaginationService service;
    private Iterable<Text> contents;
    private Optional<Text> title;
    private Optional<Text> header;
    private Optional<Text> footer;
    private Text paginationSpacer;
    private int linesPerPage;

    public SpongePaginationList(SpongePaginationService service, Iterable<Text> contents, Text title, Text header,
            Text footer, Text paginationSpacer, int linesPerPage) {
        this.service = service;
        this.contents = contents;
        this.title = Optional.ofNullable(title);
        this.header = Optional.ofNullable(header);
        this.footer = Optional.ofNullable(footer);
        this.paginationSpacer = paginationSpacer;
        this.linesPerPage = linesPerPage;
    }

    @Override
    public Iterable<Text> getContents() {
        return this.contents;
    }

    @Override
    public Optional<Text> getTitle() {
        return this.title;
    }

    @Override
    public Optional<Text> getHeader() {
        return this.header;
    }

    @Override
    public Optional<Text> getFooter() {
        return this.footer;
    }

    @Override
    public Text getPadding() {
        return this.paginationSpacer;
    }

    @Override
    public int getLinesPerPage() {
        return this.linesPerPage;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void sendTo(final MessageReceiver receiver) {
        checkNotNull(this.contents, "contents");
        checkNotNull(receiver, "source");
        this.service.registerCommandOnce();

        MessageReceiver realSource = receiver;
        while (realSource instanceof ProxySource) {
            realSource = ((ProxySource)realSource).getOriginalSource();
        }
        @SuppressWarnings("unchecked")
        PaginationCalculator calculator = new PaginationCalculator(this.linesPerPage);
        Iterable<Map.Entry<Text, Integer>> counts = StreamSupport.stream(this.contents.spliterator(), false).map(input -> {
            int lines = calculator.getLines(input);
            return Maps.immutableEntry(input, lines);
        }).collect(Collectors.toList());

        Text title = this.title.orElse(null);
        if (title != null) {
            title = calculator.center(title, this.paginationSpacer);
        }

        ActivePagination pagination;
        if (this.contents instanceof List) { // If it started out as a list, it's probably reasonable to copy it to another list
            pagination = new ListPagination(receiver, calculator, ImmutableList.copyOf(counts), title, this.header.orElse(null),
                    this.footer.orElse(null), this.paginationSpacer);
        } else {
            pagination = new IterablePagination(receiver, calculator, counts, title, this.header.orElse(null),
                    this.footer.orElse(null), this.paginationSpacer);
        }

        this.service.getPaginationState(receiver, true).put(pagination);
        try {
            pagination.nextPage();
        } catch (CommandException e) {
            receiver.sendMessage(error(e.getText()));
        }
    }
}
