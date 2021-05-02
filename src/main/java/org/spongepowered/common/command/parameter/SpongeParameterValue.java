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
package org.spongepowered.common.command.parameter;

import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.network.chat.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;
import org.spongepowered.common.command.brigadier.argument.ArgumentParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SpongeParameterValue<T> implements Parameter.Value<T> {

    private final List<ValueParser<? extends T>> parsers;
    private final ValueCompleter completer;
    private final Predicate<CommandCause> requirement;
    private final @Nullable ValueUsage usage;
    private final Key<T> key;
    private final boolean isOptional;
    private final boolean consumeAll;
    private final boolean terminal;
    private final @Nullable ValueParameterModifier<T> modifier;

    public SpongeParameterValue(
            final List<ValueParser<? extends T>> parsers,
            final ValueCompleter completer,
            final @Nullable ValueUsage usage,
            final Predicate<CommandCause> requirement,
            final Key<T> key,
            final boolean isOptional,
            final boolean consumeAll,
            final boolean terminal,
            final @Nullable ValueParameterModifier<T> modifier) {
        this.parsers = parsers;
        this.completer = completer;
        this.requirement = requirement;
        this.usage = usage;
        this.key = key;
        this.isOptional = isOptional;
        this.consumeAll = consumeAll;
        this.modifier = modifier;
        this.terminal = consumeAll || terminal;
    }

    @Override
    public void parse(final ArgumentReader.@NonNull Mutable args, final CommandContext.@NonNull Builder context) throws ArgumentParseException {
        final ArgumentReader.Immutable readerState = args.immutable();
        final CommandContext.Builder.Transaction transaction = context.startTransaction();

        try {
            do {
                args.skipWhitespace();
                this.parseInternal(args, context);
            } while (this.consumeAll && args.canRead()); // executes more than once for "consumeAll"
        } catch (final ArgumentParseException apex) {
            args.setState(readerState);
            context.rollback(transaction);
            if (!this.isOptional) {
                return; // Optional
            }

            throw apex;
        }
        context.commit(transaction);

    }

    private void parseInternal(
            final ArgumentReader.Mutable args,
            final CommandContext.Builder context) throws ArgumentParseException {

        List<ArgumentParseException> currentExceptions = null;
        final ArgumentReader.Immutable state = args.immutable();
        final CommandContext.Builder.Transaction transaction = context.startTransaction();
        for (final ValueParser<? extends T> parser : this.parsers) {
            try {
                T value = parser.parseValue(this.key, args, context).orElse(null);
                if (this.modifier != null) {
                    value = this.modifier.modifyResult(this.key, args.immutable(), context, value).orElse(null);
                }
                if (value != null) {
                    context.putEntry(this.key, value);
                }
                context.commit(transaction);
                return; // something parsed, so we exit.
            } catch (final ArgumentParseException ex) {
                if (currentExceptions == null) {
                    currentExceptions = new ArrayList<>();
                }

                currentExceptions.add(ex);
                args.setState(state);
                context.rollback(transaction);
            }
        }

        // If we get this far, we failed to parse, return the exceptions
        if (currentExceptions == null) {
            throw new CommandRuntimeException(new TextComponent("Could not parse element"));
            // throw new ArgumentParseException(t("Could not parse element"), args.getInput(), args.cursor());
        } else if (currentExceptions.size() == 1) {
            throw currentExceptions.get(0);
        } else {
            final List<Component> errors = currentExceptions.stream().map(ArgumentParseException::superText).collect(Collectors.toList());
            throw new ArgumentParseException(Component.join(Component.newline(), errors), args.input(), args.cursor());
        }

    }

    @Override
    public @NonNull Key<T> key() {
        return this.key;
    }

    @Override
    public List<CommandCompletion> complete(final ArgumentReader.@NonNull Immutable reader, final @NonNull CommandContext context) {
        final String currentInput = reader.remaining();
        final List<CommandCompletion> result = this.completer.complete(context, currentInput);
        if (this.modifier != null) {
            return this.modifier.modifyCompletion(context, currentInput, result);
        }
        return result;
    }

    @Override
    public @NonNull String usage(final @NonNull CommandCause cause) {
        if (this.usage != null) {
            return this.usage.usage(this.key.key());
        }

        final String usage = this.key.key();
        if (this.isOptional) {
            return "[" + usage + "]";
        }

        return usage;
    }

    @Override
    public @NonNull Collection<ValueParser<? extends T>> parsers() {
        return this.parsers;
    }

    @Override
    public @NonNull ValueCompleter completer() {
        return this.completer;
    }

    @Override
    public @NonNull Optional<ValueParameterModifier<T>> modifier() {
        return Optional.ofNullable(this.modifier);
    }

    @Override
    public @NonNull Optional<ValueUsage> valueUsage() {
        return Optional.ofNullable(this.usage);
    }

    @Override
    public @NonNull Predicate<CommandCause> requirement() {
        return this.requirement;
    }

    @Override
    public boolean isTerminal() {
        return this.terminal;
    }

    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    @Override
    public boolean willConsumeAllRemaining() {
        return this.consumeAll;
    }

    @SuppressWarnings("unchecked")
    public @Nullable ArgumentParser<T> argumentTypeIfStandard() {
        if (this.parsers.size() == 1) {
            final ValueParser<? extends T> parser = this.parsers.get(0);
            if (parser instanceof ArgumentParser<?>) {
                return (ArgumentParser<T>) parser;
            }
        }
        return null;
    }

}
