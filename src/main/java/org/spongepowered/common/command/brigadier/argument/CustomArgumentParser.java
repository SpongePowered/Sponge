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

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContext;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;
import org.spongepowered.common.command.parameter.managed.clientcompletion.SpongeClientCompletionType;
import org.spongepowered.common.util.CommandUtil;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * For use with other argument types
 */
public final class CustomArgumentParser<T> implements ArgumentParser<T>, SuggestionProvider<CommandSourceStack> {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");

    private final List<ArgumentType<?>> types;

    private final Collection<ValueParser<? extends T>> parsers;
    private final ValueCompleter completer;
    private final boolean doesNotRead;

    public CustomArgumentParser(final Collection<ValueParser<? extends T>> parsers, final ValueCompleter completer, final boolean doesNotRead) {
        this.parsers = parsers;
        this.completer = completer;
        this.doesNotRead =
                doesNotRead || parsers.stream().allMatch(x -> x.clientCompletionType().contains(ClientCompletionTypes.NONE.get()));
        // indicates that we should try to parse this even if there is nothing else to parse.
        if (this.parsers.size() == 1) {
            final ValueParser<? extends T> parser = this.parsers.iterator().next();
            if (parser instanceof StandardArgumentParser) {
                this.types = Collections.unmodifiableList(((StandardArgumentParser<?, ?>) parser).getClientCompletionArgumentType());
            } else if (this.doesNotRead) {
                this.types = Collections.singletonList(Constants.Command.STANDARD_STRING_ARGUMENT_TYPE);
            } else {
                this.types = parser.clientCompletionType().stream()
                        .map(x -> ((SpongeClientCompletionType) x).getType())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } else {
            this.types = Collections.singletonList(Constants.Command.STANDARD_STRING_ARGUMENT_TYPE);
        }
    }

    @Override
    public final T parse(final Parameter.Key<? super T> key,
                   final SpongeCommandContextBuilder contextBuilder,
                   final SpongeStringReader reader,
                   final ValueParameterModifier<T> modifier)
            throws CommandSyntaxException {
        List<Exception> exceptions = null;
        final ArgumentReader.Immutable state = reader.immutable();
        T value;
        for (final ValueParser<? extends T> parser : this.parsers) {
            final org.spongepowered.api.command.parameter.CommandContext.Builder.Transaction transaction = contextBuilder.startTransaction();
            try {
                value = this.modifyResult(key, contextBuilder, reader, modifier,
                        parser.parseValue(key, reader, contextBuilder).orElse(null));
                if (modifier != null) {
                    value = modifier.modifyResult(key, reader.immutable(), contextBuilder, value).orElse(null);
                }
                contextBuilder.commit(transaction);
                return value;
            } catch (final Exception e) {
                if (exceptions == null) {
                    exceptions = new ArrayList<>();
                }
                if (e instanceof ArgumentParseException) {
                    exceptions.add(this.modifyExceptionMessage(reader, (ArgumentParseException) e, modifier));
                } else {
                    exceptions.add(e);
                }
            }

            // reset the state as it did not go through.
            reader.setState(state);
            contextBuilder.rollback(transaction);
        }

        if (exceptions != null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                    .createWithContext(reader, exceptions);
        }

        // TODO: Check this - don't want Brig to blow up. If that happens, mandate everything returns an object.
        return null;
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(
            final com.mojang.brigadier.context.CommandContext<?> context,
            final SuggestionsBuilder builder) {

        final List<CommandCompletion> completions = this.completer.complete((SpongeCommandContext) context, builder.getRemaining());
        return CommandUtil.buildSuggestionsFromCompletions(completions, builder);
    }

    @Override
    public boolean doesNotRead() {
        return this.doesNotRead;
    }

    @Override
    public Collection<String> getExamples() {
        return Collections.emptyList();
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return this.types;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return this.listSuggestions(context, builder);
    }
}
