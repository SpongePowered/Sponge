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

import org.spongepowered.api.service.pagination.PaginationCalculator;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Holds logic for an active pagination that is occurring.
 */
class ActivePagination {

    private static final Text SLASH_TEXT = Texts.of("/");
    private static final Text DIVIDER_TEXT = Texts.of(" ");
    private static final Text NEXT_PAGE_TEXT = Texts.builder(">>").color(TextColors.BLUE).style(TextStyles.UNDERLINE).build();
    private static final Text PREVIOUS_PAGE_TEXT = Texts.builder("<<").color(TextColors.BLUE).style(TextStyles.UNDERLINE).build();
    private final WeakReference<CommandSource> src;
    private final UUID id = UUID.randomUUID();
    private final Text title;
    private final Text header;
    private final Text footer;
    private final int maxContentLinesPerPage;
    protected final PaginationCalculator<CommandSource> calc;
    private final String padding;
    private final List<List<Text>> pages;

    public ActivePagination(CommandSource src, PaginationCalculator<CommandSource> calc, Iterable<Map.Entry<Text, Integer>> lines,
            Text title, Text header, Text footer, String padding) {
        this.src = new WeakReference<CommandSource>(src);
        this.calc = calc;
        this.title = title;
        this.header = header;
        this.footer = footer;
        this.padding = padding;
        int maxContentLinesPerPage = calc.getLinesPerPage(src) - 1;
        if (title != null) {
            maxContentLinesPerPage -= calc.getLines(src, title);
        }
        if (header != null) {
            maxContentLinesPerPage -= calc.getLines(src, header);
        }
        if (footer != null) {
            maxContentLinesPerPage -= calc.getLines(src, footer);
        }
        this.maxContentLinesPerPage = maxContentLinesPerPage;

        List<List<Text>> pages = new ArrayList<List<Text>>();
        List<Text> currentPage = new ArrayList<Text>();
        int currentPageLines = 0;

        for (Map.Entry<Text, Integer> ent : lines) {
            if (getMaxContentLinesPerPage() > 0 && ent.getValue() + currentPageLines > getMaxContentLinesPerPage() && currentPageLines != 0) {
                while (currentPageLines < getMaxContentLinesPerPage()) {
                    currentPage.add(Texts.of());
                    currentPageLines++;
                }
                currentPageLines = 0;
                pages.add(currentPage);
                currentPage = new ArrayList<Text>();
            }
            currentPageLines += ent.getValue();
            currentPage.add(ent.getKey());
        }
        if (currentPageLines > 0) {
            while (currentPageLines < getMaxContentLinesPerPage()) {
                currentPage.add(Texts.of());
                currentPageLines++;
            }
            pages.add(currentPage);
        }
        this.pages = pages;

    }

    public UUID getId() {
        return this.id;
    }

    protected List<Text> getLines(int page) throws CommandException {
        if (page < 1) {
            throw new CommandException(t("Page %s does not exist!", page));
        } else if (page > this.pages.size()) {
            throw new CommandException(t("Page %s is too high", page));
        }
        return this.pages.get(page - 1);
    }

    protected boolean hasPrevious(int page) {
        return page > 1;
    }

    protected boolean hasNext(int page) {
        return page < this.pages.size();
    }

    protected int getTotalPages() {
        return this.pages.size();
    }

    protected int getMaxContentLinesPerPage() {
        return this.maxContentLinesPerPage;
    }

    public void sendPage(int page) throws CommandException {
        CommandSource src = this.src.get();
        if (src == null) {
            throw new CommandException(t("Source for pagination %s is no longer active!", getId()));
        }
        List<Text> toSend = new ArrayList<Text>();
        Text title = this.title;
        if (title != null) {
            toSend.add(title);
        }
        Text header = this.header;
        if (header != null) {
            toSend.add(header);
        }

        for (Text line : getLines(page)) {
            toSend.add(line);
        }

        Text footer = calculateFooter(page);
        if (footer != null) {
            toSend.add(this.calc.center(src, footer, this.padding));
        }
        if (this.footer != null) {
            toSend.add(this.footer);
        }
        src.sendMessage(toSend);
    }

    protected Text calculateFooter(int currentPage) {
        boolean hasPrevious = hasPrevious(currentPage);
        boolean hasNext = hasNext(currentPage);

        TextBuilder ret = Texts.builder();
        if (hasPrevious) {
            ClickAction<?> backAction = TextActions.runCommand("/pagination " + this.getId().toString() + " " + (currentPage - 1));
            ret.append(PREVIOUS_PAGE_TEXT.builder().onClick(backAction).build()).append(DIVIDER_TEXT);
        }
        boolean needsDiv = false;
        int totalPages = getTotalPages();
        if (totalPages > 1) {
            ret.append(Texts.of(currentPage)).append(SLASH_TEXT).append(Texts.of(totalPages));
            needsDiv = true;
        }
        if (hasNext) {
            if (needsDiv) {
                ret.append(DIVIDER_TEXT);
            }
            ClickAction<?> backAction = TextActions.runCommand("/pagination " + this.getId().toString() + " " + (currentPage + 1));
            ret.append(NEXT_PAGE_TEXT.builder().onClick(backAction).build());
        }
        if (this.title != null) {
            ret.color(this.title.getColor());
            ret.style(this.title.getStyle());
        }
        return ret.build();
    }
}
