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
package org.spongepowered.common.text.action;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SpongeCallbackHolder {
    public static final String CALLBACK_COMMAND = "callback";
    public static final CallbackValueParameter CALLBACK_VALUE_PARAMETER = new CallbackValueParameter();
    public static final String CALLBACK_COMMAND_QUALIFIED = "/sponge:" + CALLBACK_COMMAND;
    private static final SpongeCallbackHolder INSTANCE = new SpongeCallbackHolder();

    static final ConcurrentMap<UUID, Consumer<CommandSource>> reverseMap = new ConcurrentHashMap<>();
    private static final LoadingCache<Consumer<CommandSource>, UUID> callbackCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<Consumer<CommandSource>, UUID>() {
                @Override
                public void onRemoval(RemovalNotification<Consumer<CommandSource>, UUID> notification) {
                    reverseMap.remove(notification.getValue(), notification.getKey());
                }
            })
            .build(new CacheLoader<Consumer<CommandSource>, UUID>() {
                @Override
                public UUID load(Consumer<CommandSource> key) throws Exception {
                    UUID ret = UUID.randomUUID();
                    reverseMap.putIfAbsent(ret, key);
                    return ret;
                }
            });


    public static SpongeCallbackHolder getInstance() {
        return INSTANCE;
    }


    public UUID getOrCreateIdForCallback(Consumer<CommandSource> callback) {
        return callbackCache.getUnchecked(checkNotNull(callback, "callback"));
    }

    public Optional<Consumer<CommandSource>> getCallbackForUUID(UUID id) {
        return Optional.of(reverseMap.get(id));
    }

    public Command createCommand() {
        return Command.builder()
                .setShortDescription(t("Execute a callback registered as part of a Text object. Primarily for internal use"))
                .parameters(Parameter.builder().setKey(t("callback")).setParser(CALLBACK_VALUE_PARAMETER).build())
                .setExecutor((cause, src, args) -> {
                    args.<Consumer<CommandSource>>getOneUnchecked("callback").accept(src);
                    return CommandResult.success();
                }).build();
    }

    private static class CallbackValueParameter implements ValueParameter {

        @Override
        public Optional<Object> getValue(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
            final String next = args.next();
            try {
                UUID id = UUID.fromString(next);
                Consumer<CommandSource> ret = reverseMap.get(id);
                if (ret == null) {
                    throw args.createError(t("The callback you provided was not valid. Keep in mind that callbacks will expire after 10 minutes, so"
                            + " you might want to consider clicking faster next time!"));
                }
                return Optional.of(ret);
            } catch (IllegalArgumentException ex) {
                throw args.createError(t("Input %s was not a valid UUID", next));
            }
        }

        @Override
        public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
            return ImmutableList.of();
        }

    }
}
