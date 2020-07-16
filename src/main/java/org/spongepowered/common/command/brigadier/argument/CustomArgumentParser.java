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
package org.spongepowered.common.command.brigadier.argument;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContext;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;
import org.spongepowered.common.command.parameter.managed.clientcompletion.SpongeClientCompletionType;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * For use with other argument types
 */
public class CustomArgumentParser<T> implements ArgumentParser<T>, SuggestionProvider<CommandSource> {

    public static final ClientCompletionType NONE_CLIENT_COMPLETION_TYPE = new SpongeClientCompletionType("none", null);

    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");

    private final ImmutableList<ArgumentType<?>> types;

    private final Collection<ValueParser<? extends T>> parsers;
    private final ValueCompleter completer;
    private final boolean isHidden;
    private final boolean containsDefault;

    public CustomArgumentParser(final Collection<ValueParser<? extends T>> parsers, final ValueCompleter completer, final boolean containsDefault) {
        this.parsers = parsers;
        this.completer = completer;
        this.isHidden = parsers.stream().allMatch(x -> x.getClientCompletionType().contains(CustomArgumentParser.NONE_CLIENT_COMPLETION_TYPE));
        this.containsDefault = containsDefault || this.isHidden; // indicates that we should try to parse this even if there is nothing else to parse.
        if (this.containsDefault && this.parsers.size() == 2) {
            final ValueParser<? extends T> parser = this.parsers.iterator().next();
            if (parser instanceof StandardArgumentParser) {
                this.types = ImmutableList.copyOf(((StandardArgumentParser<?, ?>) parser).getClientCompletionArgumentType());
            } else {
                this.types = ImmutableList.of(Constants.Command.STANDARD_STRING_ARGUMENT_TYPE);
            }
        } else {
            this.types = ImmutableList.of(Constants.Command.STANDARD_STRING_ARGUMENT_TYPE);
        }
    }

    @Override
    public T parse(final Parameter.Key<? super T> key, final SpongeCommandContextBuilder contextBuilder, final SpongeStringReader reader)
            throws CommandSyntaxException {
        List<Exception> exceptions = null;
        final ArgumentReader.Immutable state = reader.getImmutable();
        Optional<? extends T> value;
        for (final ValueParser<? extends T> parser : this.parsers) {
            final org.spongepowered.api.command.parameter.CommandContext.Builder.Transaction transaction = contextBuilder.startTransaction();
            try {
                value = parser.getValue(key, (ArgumentReader.Mutable) reader, contextBuilder);
                contextBuilder.commit(transaction);
                return value.orElse(null);
            } catch (final Exception e) {
                if (exceptions == null) {
                    exceptions = new ArrayList<>();
                }
                exceptions.add(e);
            }

            // reset the state as it did not go through.
            reader.setState(state);
            contextBuilder.rollback(transaction);
        }

        if (exceptions != null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                    .createWithContext(reader, exceptions);
            /* throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                    .createWithContext(reader,
                            Text.joinWith(Text.newLine(), exceptions.stream()
                                    .map(ArgumentParseException::getSuperText)
                                    .collect(Collectors.toList()))); */
        }

        // TODO: Check this - don't want Brig to blow up. If that happens, mandate everything returns an object.
        return null;
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(
            final com.mojang.brigadier.context.CommandContext<?> context,
            final SuggestionsBuilder builder) {
        for (final String s : this.completer.complete((SpongeCommandContext) context)) {
            if (CustomArgumentParser.INTEGER_PATTERN.matcher(s).matches()) {
                try {
                    builder.suggest(Integer.parseInt(s));
                } catch (final NumberFormatException ex) {
                    builder.suggest(s);
                }
            } else {
                builder.suggest(s);
            }
        }
        return builder.buildFuture();
    }

    @Override
    public boolean isHiddenFromClient() {
        return this.isHidden;
    }

    @Override
    public boolean canParseEmpty() {
        return this.containsDefault;
    }

    @Override
    public Collection<String> getExamples() {
        return ImmutableList.of();
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return this.types;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSource> context, final SuggestionsBuilder builder)
            throws CommandSyntaxException {
        return this.listSuggestions(context, builder);
    }
}
