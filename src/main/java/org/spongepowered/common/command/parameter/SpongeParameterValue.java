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

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandException;
import net.minecraft.util.text.StringTextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.brigadier.argument.ArgumentParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SpongeParameterValue<T> implements Parameter.Value<T> {

   // private final static Text NEW_LINE = Text.of("\n");
   // private final static Text GENERIC_EXCEPTION_ERROR = t("Could not parse element");

    private final ImmutableList<ValueParser<? extends T>> parsers;
    @Nullable private final SpongeDefaultValueParser<? extends T> defaultParser;
    private final ValueCompleter completer;
    private final Predicate<CommandCause> requirement;
    @Nullable private final ValueUsage usage;
    private final Key<T> key;
    private final boolean isOptional;
    private final boolean consumeAll;
    private final boolean terminal;

    public SpongeParameterValue(
            final ImmutableList<ValueParser<? extends T>> parsers,
            @Nullable final Function<CommandCause, ? extends T> defaultParser,
            final ValueCompleter completer,
            @Nullable final ValueUsage usage,
            final Predicate<CommandCause> requirement,
            final Key<T> key,
            final boolean isOptional,
            final boolean consumeAll,
            final boolean terminal) {
        this.parsers = parsers;
        this.defaultParser = defaultParser == null ? null : new SpongeDefaultValueParser<>(defaultParser);
        this.completer = completer;
        this.requirement = requirement;
        this.usage = usage;
        this.key = key;
        this.isOptional = isOptional;
        this.consumeAll = consumeAll;
        this.terminal = consumeAll || terminal;
    }

    @Override
    public void parse(final ArgumentReader.@NonNull Mutable args, final CommandContext.@NonNull Builder context) throws ArgumentParseException {
        final ArgumentReader.Immutable readerState = args.getImmutable();
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
        final ArgumentReader.Immutable state = args.getImmutable();
        final CommandContext.Builder.Transaction transaction = context.startTransaction();
        for (final ValueParser<? extends T> parser : this.parsers) {
            try {
                parser.getValue(this.key, args, context).ifPresent(t -> context.putEntry(this.key, t));
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

        try {
            if (this.defaultParser != null) {
                this.defaultParser.getValue(this.key, args, context);
            }
        } catch (final ArgumentParseException ex) {
            if (currentExceptions == null) {
                currentExceptions = new ArrayList<>();
            }
            currentExceptions.add(ex);
        }

        // If we get this far, we failed to parse, return the exceptions
        if (currentExceptions == null) {
            throw new CommandException(new StringTextComponent("Could not parse element"));
            // throw new ArgumentParseException(t("Could not parse element"), args.getInput(), args.getCursor());
        } else if (currentExceptions.size() == 1) {
            throw currentExceptions.get(0);
        } else {
            final List<Text> errors = currentExceptions.stream().map(ArgumentParseException::getSuperText).collect(Collectors.toList());
            throw new ArgumentParseException(Text.joinWith(Text.newLine(), errors), args.getInput(), args.getCursor());
        }

    }

    @Override
    @NonNull
    public Key<T> getKey() {
        return this.key;
    }

    @Override
    @NonNull
    public List<String> complete(final ArgumentReader.@NonNull Immutable reader, @NonNull final CommandContext context) {
        return this.completer.complete(context);
    }

    @Override
    @NonNull
    public Text getUsage(@NonNull final CommandCause cause) {
        if (this.usage != null) {
            return this.usage.getUsage(Text.of(this.key.key()));
        }

        final Text usage = Text.of(this.key.key());
        if (this.isOptional) {
            return Text.of("[", usage, "]");
        }

        return usage;
    }

    @Override
    @NonNull
    public Collection<ValueParser<? extends T>> getParsers() {
        return this.parsers;
    }

    @Override
    @NonNull
    public ValueCompleter getCompleter() {
        return this.completer;
    }

    @Override
    @NonNull
    public Predicate<CommandCause> getRequirement() {
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

    public SpongeDefaultValueParser<? extends T> getDefaultParser() {
        return this.defaultParser;
    }

    @Override
    public boolean willConsumeAllRemaining() {
        return this.consumeAll;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public ArgumentParser<? extends T> getArgumentTypeIfStandard() {
        if (this.parsers.size() == 1) {
            final ValueParser<? extends T> parser = this.parsers.get(0);
            if (parser instanceof ArgumentParser<?>) {
                return (ArgumentParser<T>) parser;
            }
        }
        return null;
    }

}
