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
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * For use with ArgumentTypes in the base game
 */
public class StandardArgumentParser<S, T> extends implements ArgumentParser<T>, ValueParameter<T> {

    public static <T> StandardArgumentParser<T, T> createIdentity(final ArgumentType<T> type) {
        return new StandardArgumentParser<>(type, (reader, c, x) -> x);
    }

    public static <S, T> StandardArgumentParser<S, T> createConverter(
            final ArgumentType<S> type,
            final StandardArgumentParser.Converter<S, T> converter) {
        return new StandardArgumentParser<>(type, converter);
    }

    // ---

    private final ArgumentType<S> type;
    private final StandardArgumentParser.Converter<S, T> converter;

    protected StandardArgumentParser(
            final ArgumentType<S> type,
            final StandardArgumentParser.Converter<S, T> converter) {
        this.type = type;
        this.converter = converter;
    }

    @Override
    public T parse(
            final Parameter.Key<? super T> key,
            final SpongeCommandContextBuilder contextBuilder,
            final SpongeStringReader reader) throws CommandSyntaxException {
        return this.converter.convert(reader, contextBuilder, this.type.parse(reader));
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(
            final com.mojang.brigadier.context.CommandContext<?> context,
            final SuggestionsBuilder builder) {
        return this.type.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return this.type.getExamples();
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return ImmutableList.of(this.type);
    }

    @Override
    public boolean doesNotRead() {
        return false;
    }

    @Override
    @NonNull
    public List<String> complete(@NonNull final CommandContext context, final String currentInput) {
        final SuggestionsBuilder suggestionsBuilder = new SuggestionsBuilder(currentInput, 0);
        this.listSuggestions((com.mojang.brigadier.context.CommandContext<?>) context, suggestionsBuilder);
        return suggestionsBuilder.build().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Optional<? extends T> getValue(final Parameter.@NonNull Key<? super T> parameterKey, final ArgumentReader.@NonNull Mutable reader,
            final CommandContext.@NonNull Builder context)
            throws ArgumentParseException {
        try {
            return Optional.of(this.parse(parameterKey, (SpongeCommandContextBuilder) context, (SpongeStringReader) reader));
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(Component.text(e.getMessage()), e, e.getInput(), e.getCursor());
        }
    }

    @FunctionalInterface
    public interface Converter<S, T> {

        T convert(StringReader reader, SpongeCommandContextBuilder contextBuilder, S input) throws CommandSyntaxException;

    }

}
