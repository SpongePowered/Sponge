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
package org.spongepowered.common.service.game.pagination;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public final class SpongePaginationList implements PaginationList {

    private final SpongePaginationService service;
    private final Iterable<Component> contents;
    private final Component title;
    private final Component header;
    private final Component footer;
    private final Component paginationSpacer;
    private final int linesPerPage;

    public SpongePaginationList(
            final SpongePaginationService service, final Iterable<Component> contents, final @Nullable Component title,
            final @Nullable Component header,
            final @Nullable Component footer, final Component paginationSpacer, final int linesPerPage) {
        this.service = service;
        this.contents = contents;
        this.title = title;
        this.header = header;
        this.footer = footer;
        this.paginationSpacer = paginationSpacer;
        this.linesPerPage = linesPerPage;
    }

    @Override
    public Iterable<Component> contents() {
        return this.contents;
    }

    @Override
    public Optional<Component> title() {
        return Optional.ofNullable(this.title);
    }

    @Override
    public Optional<Component> header() {
        return Optional.ofNullable(this.header);
    }

    @Override
    public Optional<Component> footer() {
        return Optional.ofNullable(this.footer);
    }

    @Override
    public Component padding() {
        return this.paginationSpacer;
    }

    @Override
    public int linesPerPage() {
        return this.linesPerPage;
    }

    @Override
    public void sendTo(final Audience receiver, final int page) {
        Objects.requireNonNull(receiver, "The message receiver cannot be null!");

        final PaginationCalculator calculator = new PaginationCalculator(this.linesPerPage);
        final Iterable<Map.Entry<Component, Integer>> counts = StreamSupport.stream(this.contents.spliterator(), false).map(input -> {
            final int lines = calculator.getLines(input);
            return Maps.immutableEntry(input, lines);
        }).collect(Collectors.toList());

        Component title = this.title;
        if (title != null) {
            title = calculator.center(title, this.paginationSpacer);
        }

        // If the Audience is a Player, then upon death, they will become a different Audience object.
        // Thus, we use a supplier to supply the player from the server, if required.
        final Supplier<Optional<? extends Audience>> audienceSupplier;
        if (receiver instanceof Player) {
            final UUID playerUuid = ((Player) receiver).uniqueId();
            audienceSupplier = () -> Sponge.server().player(playerUuid);
        } else {
            final WeakReference<Audience> srcReference = new WeakReference<>(receiver);
            audienceSupplier = () -> Optional.ofNullable(srcReference.get());
        }

        final ActivePagination pagination;
        if (this.contents instanceof List) { // If it started out as a list, it's probably reasonable to copy it to another list
            pagination = new ListPagination(audienceSupplier, calculator, ImmutableList.copyOf(counts), title, this.header,
                    this.footer, this.paginationSpacer);
        } else {
            pagination = new IterablePagination(audienceSupplier, calculator, counts, title, this.header,
                    this.footer, this.paginationSpacer);
        }

        this.service.getPaginationState(receiver, true).put(pagination);
        try {
            pagination.specificPage(page);
        } catch (final CommandException e) {
            final Component text = e.componentMessage();
            if (text != null) {
                receiver.sendMessage(Identity.nil(), text.color(NamedTextColor.RED));
            }
        }
    }
}
