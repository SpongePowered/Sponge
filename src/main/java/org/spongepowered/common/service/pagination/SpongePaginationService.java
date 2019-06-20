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

import static org.spongepowered.api.command.args.GenericArguments.firstParsing;
import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.common.SpongeImpl;

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

public class SpongePaginationService implements PaginationService {

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
    private final ConcurrentMap<MessageReceiver, SourcePaginations> activePaginations = new MapMaker().weakKeys().makeMap();

    // We have a second active pagination system because of the way Players are handled by the server.
    // As Players are recreated every time they die in game, just storing the player in a weak map will
    // cause the player to be removed form the map upon death. Thus, player paginations get redirected
    // through to this cache instead, which last for 10 minutes from last access.
    private final Cache<UUID, SourcePaginations> playerActivePaginations = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    private final AtomicBoolean commandRegistered = new AtomicBoolean();

    void registerCommandOnce() {
        if (this.commandRegistered.compareAndSet(false, true)) {
            SpongeImpl.getGame().getCommandManager().register(
                    SpongeImpl.getPlugin(),
                    buildPaginationCommand(),
                    "pagination", "page"
            );
        }

    }

    @Override
    public PaginationList.Builder builder() {
        return new SpongePaginationBuilder(this);
    }

    @Nullable
    SourcePaginations getPaginationState(MessageReceiver source, boolean create) {
        if (source instanceof Player) {
            return getPaginationStateForPlayer((Player) source, create);
        }

        return getPaginationStateForNonPlayer(source, create);
    }

    @Nullable
    private SourcePaginations getPaginationStateForNonPlayer(MessageReceiver source, boolean create) {
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

    @SuppressWarnings("deprecation")
    private CommandSpec buildPaginationCommand() {

        final ActivePaginationCommandElement paginationElement = new ActivePaginationCommandElement(t("pagination-id"));

        CommandSpec next = CommandSpec.builder()
                .description(t("Go to the next page"))
                .executor((src, args) -> {
                    args.<ActivePagination>getOne("pagination-id").get().nextPage();
                    return CommandResult.success();
                }).build();

        CommandSpec prev = CommandSpec.builder()
                .description(t("Go to the previous page"))
                .executor((src, args) -> {
                    args.<ActivePagination>getOne("pagination-id").get().previousPage();
                    return CommandResult.success();
                }).build();

        CommandElement pageArgs = integer(t("page"));

        CommandExecutor pageExecutor = (src, args) -> {
            args.<ActivePagination>getOne("pagination-id").get().specificPage(args.<Integer>getOne("page").get());
            return CommandResult.success();
        };

        CommandSpec page = CommandSpec.builder()
                        .description(t("Go to a specific page"))
                        .arguments(pageArgs)
                        .executor(pageExecutor).build();

        //Fallback to page arguments
        ChildCommandElementExecutor childDispatcher = new ChildCommandElementExecutor(pageExecutor);
        childDispatcher.register(next, "next", "n");
        childDispatcher.register(prev, "prev", "p", "previous");
        childDispatcher.register(page, "page");

        //We create the child manually in order to force that paginationElement is required for all children + fallback
        //https://github.com/SpongePowered/SpongeAPI/issues/1272
        return CommandSpec.builder().arguments(paginationElement, firstParsing(childDispatcher, pageArgs))
                .executor(childDispatcher)
                .description(t("Helper command for paginations occurring"))
                .build();
    }

    private class ActivePaginationCommandElement extends CommandElement {

        protected ActivePaginationCommandElement(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        @SuppressWarnings("deprecation")
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            UUID id;

            SourcePaginations paginations = getPaginationState(source, false);
            if (paginations == null) {
                throw args.createError(t("Source %s has no paginations!", source.getName()));
            }

            Object state = args.getState();
            try {
                id = UUID.fromString(args.next());
            } catch (IllegalArgumentException ex) {
                if (paginations.getLastUuid() != null) {
                    args.setState(state);
                    return paginations.get(paginations.getLastUuid());
                }
                throw args.createError(t("Input was not a valid UUID!"));
            }
            ActivePagination pagination = paginations.get(id);
            if (pagination == null) {
                throw args.createError(t("No pagination registered for id %s", id.toString()));
            }
            return paginations.get(id);
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            SourcePaginations paginations = getPaginationState(src, false);
            if (paginations == null) {
                return ImmutableList.of();
            }

            final Optional<String> optNext = args.nextIfPresent();
            if (optNext.isPresent()) {
                return paginations.keys().stream()
                    .map(Object::toString)
                    .filter(new StartsWithPredicate(optNext.get()))
                    .collect(ImmutableList.toImmutableList());
            }
            return ImmutableList.copyOf(Iterables.transform(paginations.keys(), Object::toString));
        }

        @Override
        public Text getUsage(CommandSource src) {
            return getKey() == null ? Text.of() : Text.of("[", getKey(), "]");
        }
    }
}
