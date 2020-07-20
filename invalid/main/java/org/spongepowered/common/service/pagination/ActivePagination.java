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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.text.channel.MessageReceiver;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Holds logic for an active pagination that is occurring.
 */
abstract class ActivePagination {

    private static final Component SLASH_TEXT = TextComponent.of("/");
    private static final Component DIVIDER_TEXT = TextComponent.space();
    private static final Component CONTINUATION_TEXT = TextComponent.of("...");
    private final Supplier<Optional<MessageReceiver>> src;
    private final UUID id = UUID.randomUUID();
    private final Component nextPageText;
    private final Component prevPageText;
    @Nullable
    private final Component title;
    @Nullable
    private final Component header;
    @Nullable
    private final Component footer;
    private int currentPage;
    private final int maxContentLinesPerPage;
    protected final PaginationCalculator calc;
    private final Component padding;

    public ActivePagination(Supplier<Optional<MessageReceiver>> src, PaginationCalculator calc, @Nullable Component title,
            @Nullable Component header, @Nullable Component footer, Component padding) {
        this.src = src;
        this.calc = calc;
        this.title = title;
        this.header = header;
        this.footer = footer;
        this.padding = padding;
        this.nextPageText = TextComponent.builder("»")
                .color(NamedTextColor.BLUE)
                .decoration(TextDecoration.UNDERLINED, true)
                .clickEvent(ClickEvent.runCommand("/pagination " + this.id.toString() + " next"))
                .hoverEvent(HoverEvent.showText(TextComponent.of("/page next")))
                .insertion("/page next")
                .build();
        this.prevPageText = TextComponent.builder("«")
                .color(NamedTextColor.BLUE)
                .decoration(TextDecoration.UNDERLINED, true)
                .clickEvent(ClickEvent.runCommand("/pagination " + this.id.toString() + " prev"))
                .hoverEvent(HoverEvent.showText(TextComponent.of("/page prev")))
                .insertion("/page prev")
                .build();
        int maxContentLinesPerPage = calc.getLinesPerPage(src.get().get()) - 1;
        if (title != null) {
            maxContentLinesPerPage -= calc.getLines(title);
        }
        if (header != null) {
            maxContentLinesPerPage -= calc.getLines(header);
        }
        if (footer != null) {
            maxContentLinesPerPage -= calc.getLines(footer);
        }
        this.maxContentLinesPerPage = maxContentLinesPerPage;

    }

    public UUID getId() {
        return this.id;
    }

    protected abstract Iterable<Component> getLines(int page) throws CommandException;

    protected abstract boolean hasPrevious(int page);

    protected abstract boolean hasNext(int page);

    protected abstract int getTotalPages();

    public void nextPage() throws CommandException {
        this.specificPage(this.currentPage + 1);
    }

    public void previousPage() throws CommandException {
        this.specificPage(this.currentPage - 1);
    }

    public void currentPage() throws CommandException {
        this.specificPage(this.currentPage);
    }

    protected int getCurrentPage() {
        return this.currentPage;
    }

    protected int getMaxContentLinesPerPage() {
        return this.maxContentLinesPerPage;
    }

    public void specificPage(int page) throws CommandException {
        MessageReceiver src = this.src.get()
                .orElseThrow(() -> new CommandException(t("Source for pagination %s is no longer active!", this.getId())));
        this.currentPage = page;

        List<Text> toSend = new ArrayList<>();
        Text title = this.title;
        if (title != null) {
            toSend.add(title);
        }
        if (this.header != null) {
            toSend.add(this.header);
        }

        for (Text line : this.getLines(page)) {
            toSend.add(line);
        }

        Text footer = this.calculateFooter(page);
        toSend.add(this.calc.center(footer, this.padding));
        if (this.footer != null) {
            toSend.add(this.footer);
        }
        src.sendMessages(toSend);
    }

    protected Text calculateFooter(int currentPage) {
        boolean hasPrevious = this.hasPrevious(currentPage);
        boolean hasNext = this.hasNext(currentPage);

        TextComponent.Builder ret = TextComponent.builder();
        if (hasPrevious) {
            ret.append(this.prevPageText).append(DIVIDER_TEXT);
        } else {
            ret.append(TextComponent.of("«")).append(DIVIDER_TEXT);
        }
        boolean needsDiv = false;
        int totalPages = this.getTotalPages();
        if (totalPages > 1) {
            ret.append(TextComponent.builder()
                    .content(String.valueOf(currentPage))
                    .clickEvent(ClickEvent.runCommand("/pagination " + this.id + ' ' + currentPage))
                    .hoverEvent(HoverEvent.showText(TextComponent.of("/page " + currentPage)))
                    .insertion("/page " + currentPage)
                    .build());
            ret.append(SLASH_TEXT);
            ret.append(TextComponent.builder()
                    .content(String.valueOf(currentPage))
                    .clickEvent(ClickEvent.runCommand("/pagination " + this.id + ' ' + totalPages))
                    .hoverEvent(HoverEvent.showText(TextComponent.of("/page " + totalPages)))
                    .insertion("/page " + totalPages)
                    .build());
            needsDiv = true;
        }
        if (hasNext) {
            if (needsDiv) {
                ret.append(DIVIDER_TEXT);
            }
            ret.append(this.nextPageText);
        } else {
            if (needsDiv) {
                ret.append(DIVIDER_TEXT);
            }
            ret.append(TextComponent.of("»"));
        }

        ret.color(this.padding.getColor());
        if (this.title != null) {
            ret.style(this.title.getStyle());
        }
        return ret.build();
    }

    protected void padPage(final List<Component> currentPage, final int currentPageLines, final boolean addContinuation) {
        final int maxContentLinesPerPage = this.getMaxContentLinesPerPage();
        for (int i = currentPageLines; i < maxContentLinesPerPage; i++) {
            if (addContinuation && i == maxContentLinesPerPage - 1) {
                currentPage.add(CONTINUATION_TEXT);
            } else {
                currentPage.add(0, TextComponent.empty());
            }
        }
    }
}
