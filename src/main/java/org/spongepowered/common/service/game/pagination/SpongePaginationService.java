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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.MapMaker;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.common.command.SpongeCommandCompletion;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public final class SpongePaginationService implements PaginationService {

    static final class SourcePaginations {

        private final Map<UUID, ActivePagination> paginations = new ConcurrentHashMap<>();
        private @Nullable volatile UUID lastUuid;

        @Nullable public ActivePagination get(final UUID uuid) {
            return this.paginations.get(uuid);
        }

        public void put(final ActivePagination pagination) {
            synchronized (this.paginations) {
                this.paginations.put(pagination.getId(), pagination);
                this.lastUuid = pagination.getId();
            }
        }

        public Set<UUID> keys() {
            return this.paginations.keySet();
        }

        public @Nullable UUID getLastUuid() {
            return this.lastUuid;
        }
    }

    private final ConcurrentMap<Audience, SourcePaginations> activePaginations = new MapMaker().weakKeys().makeMap();

    // We have a second active pagination system because of the way Players are handled by the server.
    // As Players are recreated every time they die in game, just storing the player in a weak map will
    // cause the player to be removed form the map upon death. Thus, player paginations get redirected
    // through to this cache instead, which last for 10 minutes from last access.
    private final Cache<UUID, SourcePaginations> playerActivePaginations = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @Override
    public PaginationList.Builder builder() {
        return new SpongePaginationBuilder(this);
    }

    @Nullable SourcePaginations getPaginationState(final Audience source, final boolean create) {
        if (source instanceof Player) {
            return this.getPaginationStateForPlayer((Player) source, create);
        }

        return this.getPaginationStateForNonPlayer(source, create);
    }

    private @Nullable SourcePaginations getPaginationStateForNonPlayer(final Audience source, final boolean create) {
        SourcePaginations ret = this.activePaginations.get(source);
        if (ret == null && create) {
            ret = new SourcePaginations();
            final SourcePaginations existing = this.activePaginations.putIfAbsent(source, ret);
            if (existing != null) {
                ret = existing;
            }
        }
        return ret;
    }

    private @Nullable SourcePaginations getPaginationStateForPlayer(final Player source, final boolean create) {
        return this.playerActivePaginations.get(source.uniqueId(), k -> create ? new SourcePaginations() : null);
    }

    public Command.Parameterized createPaginationCommand() {
        final Parameter.Value<ActivePagination> paginationIdParameter = Parameter.builder(ActivePagination.class)
                .addParser(new ActivePaginationParameter())
                .key("pagination-id")
                .build();

        final Command.Parameterized next = Command.builder()
                .shortDescription(Component.text("Go to the next page"))
                .executor((context) -> {
                    context.requireOne(paginationIdParameter).nextPage();
                    return CommandResult.success();
                }).build();

        final Command.Parameterized prev = Command.builder()
                .shortDescription(Component.text("Go to the previous page"))
                .executor((context) -> {
                    context.requireOne(paginationIdParameter).previousPage();
                    return CommandResult.success();
                }).build();

        final Parameter.Value<Integer> pageParameter = Parameter.integerNumber().key("page").build();

        final CommandExecutor pageExecutor = (context) -> {
            context.requireOne(paginationIdParameter).specificPage(context.requireOne(pageParameter));
            return CommandResult.success();
        };

        final Command.Parameterized page = Command.builder()
                .shortDescription(Component.text("Go to a specific page"))
                .addParameter(pageParameter)
                .executor(pageExecutor)
                .build();

        return Command.builder()
                .addParameters(paginationIdParameter, Parameter.firstOf(pageParameter,
                        Parameter.subcommand(next, "next", "n"),
                        Parameter.subcommand(prev, "prev", "p", "previous"),
                        Parameter.subcommand(page, "page")))
                .addChild(page, "page")
                .shortDescription(Component.text("Helper command for paginations occurring"))
                .executor(pageExecutor)
                .build();
    }

    private final class ActivePaginationParameter implements ValueParameter<ActivePagination>, ValueCompleter {

        @Override
        public Optional<? extends ActivePagination> parseValue(final Parameter.Key<? super ActivePagination> parameterKey,
                final ArgumentReader.Mutable reader,
                final CommandContext.Builder context) throws ArgumentParseException {
            final Audience source = context.cause().audience();

            final SourcePaginations paginations = SpongePaginationService.this.getPaginationState(source, false);
            if (paginations == null) {
                final String name = source instanceof Nameable ? ((Nameable) source).name() : source.toString();
                throw reader.createException(Component.text(String.format("Source %s has no paginations!", name)));
            }

            final UUID id;
            final ArgumentReader.Immutable state = reader.immutable();
            try {
                id = UUID.fromString(reader.parseString());
            } catch (final IllegalArgumentException ex) {
                if (paginations.getLastUuid() != null) {
                    reader.setState(state);
                    return Optional.ofNullable(paginations.get(paginations.getLastUuid()));
                }
                throw reader.createException(Component.text("Input was not a valid UUID!"));
            }
            final ActivePagination pagination = paginations.get(id);
            if (pagination == null) {
                throw reader.createException(Component.text("No pagination registered for id " + id));
            }
            return Optional.ofNullable(paginations.get(id));
        }

        @Override
        public List<CommandCompletion> complete(final CommandContext context, final String input) {
            final Audience audience = context.cause().audience();
            final SourcePaginations paginations = SpongePaginationService.this.getPaginationState(audience, false);
            if (paginations != null) {
                return paginations.keys().stream().map(Object::toString)
                        .filter(x -> x.startsWith(input))
                        .map(SpongeCommandCompletion::new)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
    }
}
