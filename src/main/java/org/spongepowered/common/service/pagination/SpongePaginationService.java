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

import static org.spongepowered.api.command.parameter.Parameter.firstOf;
import static org.spongepowered.api.command.parameter.Parameter.integerNumber;
import static org.spongepowered.api.command.parameter.Parameter.subcommand;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.common.command.registrar.SpongeParameterizedCommandRegistrar;
import org.spongepowered.common.launch.Launcher;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

@Singleton
public final class SpongePaginationService implements PaginationService {

    static class SourcePaginations {

        private final Map<UUID, ActivePagination> paginations = new ConcurrentHashMap<>();
        @Nullable private volatile UUID lastUuid;

        @Nullable public ActivePagination get(UUID uuid) {
            return this.paginations.get(uuid);
        }

        public void put(ActivePagination pagination) {
            synchronized (this.paginations) {
                this.paginations.put(pagination.getId(), pagination);
                this.lastUuid = pagination.getId();
            }
        }

        public Set<UUID> keys() {
            return this.paginations.keySet();
        }

        @Nullable
        public UUID getLastUuid() {
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

    private final AtomicBoolean commandRegistered = new AtomicBoolean();

    void registerCommandOnce() {
        if (this.commandRegistered.compareAndSet(false, true)) {
            SpongeParameterizedCommandRegistrar.INSTANCE.register(
                    Launcher.getInstance().getCommonPlugin(),
                    this.buildPaginationCommand(),
                    "pagination", "page"
            );
        }

    }

    @Override
    public PaginationList.Builder builder() {
        return new SpongePaginationBuilder(this);
    }

    @Nullable
    SourcePaginations getPaginationState(Audience source, boolean create) {
        if (source instanceof Player) {
            return this.getPaginationStateForPlayer((Player) source, create);
        }

        return this.getPaginationStateForNonPlayer(source, create);
    }

    @Nullable
    private SourcePaginations getPaginationStateForNonPlayer(Audience source, boolean create) {
        SourcePaginations ret = this.activePaginations.get(source);
        if (ret == null && create) {
            ret = new SourcePaginations();
            SourcePaginations existing = this.activePaginations.putIfAbsent(source, ret);
            if (existing != null) {
                ret = existing;
            }
        }
        return ret;
    }

    @Nullable
    private SourcePaginations getPaginationStateForPlayer(Player source, boolean create) {
        return this.playerActivePaginations.get(source.getUniqueId(), k -> create ? new SourcePaginations() : null);
    }

    private Command.Parameterized buildPaginationCommand() {
        Parameter.Value<ActivePagination> paginationIdParameter = Parameter.builder(ActivePagination.class)
                .parser(new ActivePaginationParser())
                .setSuggestions(new ActivePaginationCompleter())
                .setKey("pagination-id")
                .build();

        Command.Parameterized next = Command.builder()
                .setShortDescription(TextComponent.of("Go to the next page"))
                .setExecutor((context) -> {
                    context.requireOne(paginationIdParameter).nextPage();
                    return CommandResult.success();
                }).build();

        Command.Parameterized prev = Command.builder()
                .setShortDescription(TextComponent.of("Go to the previous page"))
                .setExecutor((context) -> {
                    context.requireOne(paginationIdParameter).previousPage();
                    return CommandResult.success();
                }).build();

        Parameter.Value<Integer> pageParameter = integerNumber().setKey("page").build();

        CommandExecutor pageExecutor = (context) -> {
            context.requireOne(paginationIdParameter).specificPage(context.requireOne(pageParameter));
            return CommandResult.success();
        };

        Command.Parameterized page = Command.builder()
                .setShortDescription(TextComponent.of("Go to a specific page"))
                .parameter(pageParameter)
                .setExecutor(pageExecutor)
                .build();

        //We create the child manually in order to force that paginationElement is required for all children + fallback
        //https://github.com/SpongePowered/SpongeAPI/issues/1272
        return Command.builder()
                .parameters(paginationIdParameter, firstOf(pageParameter,
                        subcommand(next, "next", "n"),
                        subcommand(prev, "prev", "p", "previous"),
                        subcommand(page, "page")))
                .child(page, "page")
                .setExecutor(page)
                .setShortDescription(TextComponent.of("Helper command for paginations occurring"))
                .build();
    }

    private class ActivePaginationParser implements ValueParser<ActivePagination> {

        @Override
        public Optional<? extends ActivePagination> getValue(Parameter.Key<? super ActivePagination> parameterKey, ArgumentReader.Mutable reader,
                CommandContext.Builder context) throws ArgumentParseException {
            Audience source = context.getCause().first(Audience.class)
                    .orElseThrow(() -> reader.createException(TextComponent.of("No usable source found")));

            SourcePaginations paginations = SpongePaginationService.this.getPaginationState(source, false);
            if (paginations == null) {
                String name = source instanceof Nameable ? ((Nameable) source).getName() : source.toString();
                throw reader.createException(TextComponent.of(String.format("Source %s has no paginations!", name)));
            }

            UUID id;
            ArgumentReader.Immutable state = reader.getImmutable();
            try {
                id = UUID.fromString(reader.parseString());
            } catch (IllegalArgumentException ex) {
                if (paginations.getLastUuid() != null) {
                    reader.setState(state);
                    return Optional.ofNullable(paginations.get(paginations.getLastUuid()));
                }
                throw reader.createException(TextComponent.of("Input was not a valid UUID!"));
            }
            ActivePagination pagination = paginations.get(id);
            if (pagination == null) {
                throw reader.createException(TextComponent.of("No pagination registered for id " + id));
            }
            return Optional.ofNullable(paginations.get(id));
        }
    }

    private class ActivePaginationCompleter implements ValueCompleter {

        @Override
        public List<String> complete(CommandContext context) {
            return context.getCause().first(Audience.class)
                    .map(src -> SpongePaginationService.this.getPaginationState(src, false))
                    .map(paginations -> ImmutableList.copyOf(Iterables.transform(paginations.keys(), Object::toString)))
                    .orElseGet(ImmutableList::of);
        }
    }
}
