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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SpongePaginationList implements PaginationList {

    private final SpongePaginationService service;
    private final Iterable<Component> contents;
    private final Optional<Component> title;
    private final Optional<Component> header;
    private final Optional<Component> footer;
    private final Component paginationSpacer;
    private final int linesPerPage;

    public SpongePaginationList(SpongePaginationService service, Iterable<Component> contents, @Nullable Component title, @Nullable Component header,
            @Nullable Component footer, Component paginationSpacer, int linesPerPage) {
        this.service = service;
        this.contents = contents;
        this.title = Optional.ofNullable(title);
        this.header = Optional.ofNullable(header);
        this.footer = Optional.ofNullable(footer);
        this.paginationSpacer = paginationSpacer;
        this.linesPerPage = linesPerPage;
    }

    @Override
    public Iterable<Component> getContents() {
        return this.contents;
    }

    @Override
    public Optional<Component> getTitle() {
        return this.title;
    }

    @Override
    public Optional<Component> getHeader() {
        return this.header;
    }

    @Override
    public Optional<Component> getFooter() {
        return this.footer;
    }

    @Override
    public Component getPadding() {
        return this.paginationSpacer;
    }

    @Override
    public int getLinesPerPage() {
        return this.linesPerPage;
    }

    @Override
    public void sendTo(final Audience receiver, int page) {
        checkNotNull(receiver, "The message receiver cannot be null!");

        final PaginationCalculator calculator = new PaginationCalculator(this.linesPerPage);
        Iterable<Map.Entry<Component, Integer>> counts = StreamSupport.stream(this.contents.spliterator(), false).map(input -> {
            int lines = calculator.getLines(input);
            return Maps.immutableEntry(input, lines);
        }).collect(Collectors.toList());

        Component title = this.title.orElse(null);
        if (title != null) {
            title = calculator.center(title, this.paginationSpacer);
        }

        // If the Audience is a Player, then upon death, they will become a different Audience object.
        // Thus, we use a supplier to supply the player from the server, if required.
        Supplier<Optional<? extends Audience>> audienceSupplier;
        if (receiver instanceof Player) {
            final UUID playerUuid = ((Player) receiver).getUniqueId();
            audienceSupplier = () -> Sponge.getServer().getPlayer(playerUuid);
        } else {
            WeakReference<Audience> srcReference = new WeakReference<>(receiver);
            audienceSupplier = () -> Optional.ofNullable(srcReference.get());
        }

        ActivePagination pagination;
        if (this.contents instanceof List) { // If it started out as a list, it's probably reasonable to copy it to another list
            pagination = new ListPagination(audienceSupplier, calculator, ImmutableList.copyOf(counts), title, this.header.orElse(null),
                    this.footer.orElse(null), this.paginationSpacer);
        } else {
            pagination = new IterablePagination(audienceSupplier, calculator, counts, title, this.header.orElse(null),
                    this.footer.orElse(null), this.paginationSpacer);
        }

        this.service.getPaginationState(receiver, true).put(pagination);
        try {
            pagination.specificPage(page);
        } catch (CommandException e) {
            Component text = e.getText();
            if (text != null) {
                receiver.sendMessage(text.color(NamedTextColor.RED));
            }
        }
    }
}
