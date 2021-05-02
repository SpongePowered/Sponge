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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * For use with ArgumentTypes in the base game
 */
public class StandardArgumentParser<S, T> implements ArgumentParser<T>, ValueParameter.Simple<T> {

    @SuppressWarnings("unchecked")
    public static <T> StandardArgumentParser<T, T> createIdentity(final ArgumentType<T> type) {
        return new StandardArgumentParser<>(type, (StandardArgumentParser.Converter<T, T>) Converter.IDENTITY);
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
    public final T parse(
            final Parameter.Key<? super T> key,
            final SpongeCommandContextBuilder contextBuilder,
            final SpongeStringReader reader,
            final @Nullable ValueParameterModifier<T> modifier) throws CommandSyntaxException {
        final ArgumentReader.Immutable state = reader.immutable();
        final CommandContext.Builder.Transaction transaction = contextBuilder.startTransaction();
        try {
            final T value = this.modifyResult(key, contextBuilder, reader, modifier,
                    this.converter.convert(reader, contextBuilder.cause(), this.type.parse(reader)));
            contextBuilder.commit(transaction);
            return value;
        } catch (final ArgumentParseException e) {
            // reset the state as it did not go through.
            final ArgumentParseException e2 = this.modifyExceptionMessage(reader, e, modifier);
            reader.setState(state);
            contextBuilder.rollback(transaction);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                    .createWithContext(reader, e2);
        }
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
        return Collections.singletonList(this.type);
    }

    @Override
    public boolean doesNotRead() {
        return false;
    }

    @Override
    public List<CommandCompletion> complete(final @NonNull CommandCause context, final @NonNull String currentInput) {
        final SuggestionsBuilder suggestionsBuilder = new SuggestionsBuilder(currentInput, 0);
        this.listSuggestions(
                new SpongeCommandContextBuilder(null, (CommandSourceStack) context, new RootCommandNode<>(), 0).build(currentInput), suggestionsBuilder);
        return suggestionsBuilder.build().getList().stream().map(SpongeCommandCompletion::from).collect(Collectors.toList());
    }

    @Override
    public @NonNull Optional<? extends T> parseValue(final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable reader)
            throws ArgumentParseException {
        try {
            return Optional.of(this.converter.convert((StringReader) reader, cause, this.type.parse((StringReader) reader)));
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(SpongeAdventure.asAdventure(e.getRawMessage()), e, e.getInput(), e.getCursor());
        }
    }

    @FunctionalInterface
    public interface Converter<S, T> {
        Converter<Object, Object> IDENTITY = (reader, cause, input) -> input;

        T convert(StringReader reader, CommandCause cause, S input) throws CommandSyntaxException;

    }

}
