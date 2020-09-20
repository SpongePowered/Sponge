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
package org.spongepowered.common.adventure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.util.TypeTokens;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SpongeCallback {

    private final Cache<UUID, Consumer<CommandCause>> executors = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    private final Parameter.Key<Consumer<CommandCause>> executorKey = Parameter.key("key", TypeTokens.COMMAND_CAUSE_CONSUMER);

    public Command.Parameterized createCommand() {
        this.executors.invalidateAll();
        return Command.builder()
                .setShortDescription(TextComponent.of("Execute a callback registered as part of a TextComponent. Primarily for internal use"))
                .parameter(Parameter.builder(TypeTokens.COMMAND_CAUSE_CONSUMER).setKey(this.executorKey).parser(new CallbackValueParameter()).build())
                .setExecutor(this::commandCallback)
                .build();
    }

    public UUID registerCallback(final Consumer<CommandCause> causeConsumer) {
        final UUID newKey = UUID.randomUUID();
        this.executors.put(newKey, causeConsumer);
        return newKey;
    }

    public CommandResult commandCallback(@NonNull final CommandContext context) throws CommandException {
        context.requireOne(this.executorKey).accept(context.getCause());
        return CommandResult.success();
    }

    private final class CallbackValueParameter implements ValueParameter<Consumer<CommandCause>> {

        @Override
        @NonNull
        public List<String> complete(@NonNull final CommandContext context, final String currentInput) {
            return SpongeCallback.this.executors.asMap().keySet().stream().map(UUID::toString).filter(x -> x.startsWith(currentInput))
                    .collect(Collectors.toList());
        }

        @Override
        @NonNull
        public Optional<? extends Consumer<CommandCause>> getValue(
                final Parameter.@NonNull Key<? super Consumer<CommandCause>> parameterKey,
                final ArgumentReader.@NonNull Mutable reader,
                final CommandContext.@NonNull Builder context) throws ArgumentParseException {
            final String next = reader.parseString();
            try {
                final UUID id = UUID.fromString(next);
                final Consumer<CommandCause> ret = SpongeCallback.this.executors.getIfPresent(id);
                if (ret == null) {
                    throw reader.createException(TextComponent.of(
                            "The callback you provided was not valid. Keep in mind that callbacks will expire after 10 " +
                            "minutes, so you might want to consider clicking faster next time!"));
                }
                return Optional.of(ret);
            } catch (final IllegalArgumentException ex) {
                throw reader.createException(TextComponent.of("Input " + next + " was not a valid UUID"));
            }
        }

    }

}
