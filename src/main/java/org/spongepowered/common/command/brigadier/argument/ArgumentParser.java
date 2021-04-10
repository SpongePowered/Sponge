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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ArgumentParser<T> {

    T parse(
            Parameter.Key<? super T> key,
            SpongeCommandContextBuilder contextBuilder,
            SpongeStringReader reader,
            @Nullable ValueParameterModifier<T> modifier) throws CommandSyntaxException;

    CompletableFuture<Suggestions> listSuggestions(
            com.mojang.brigadier.context.CommandContext<?> context,
            SuggestionsBuilder builder);

    Collection<String> getExamples();

    default List<ArgumentType<?>> getClientCompletionArgumentType() {
        return Collections.singletonList(Constants.Command.STANDARD_STRING_ARGUMENT_TYPE);
    }

    default T modifyResult(
            final Parameter.Key<? super T> key,
            final SpongeCommandContextBuilder contextBuilder,
            final SpongeStringReader reader,
            final @Nullable ValueParameterModifier<T> modifier,
            final @Nullable T value) throws ArgumentParseException {
        if (modifier != null) {
            return modifier.modifyResult(key, reader.immutable(), contextBuilder, value).orElse(null);
        }
        return value;
    }

    default ArgumentParseException modifyExceptionMessage(final SpongeStringReader reader,
                                                          final ArgumentParseException thrownException,
                                                          final @Nullable ValueParameterModifier<T> modifier) {
        if (modifier != null) {
            final Component replacementMessage = modifier.modifyExceptionMessage(thrownException.superText());
            if (replacementMessage != thrownException.superText()) {
                return reader.createException(replacementMessage);
            }
        }
        return thrownException;
    }

    boolean doesNotRead();

    default boolean hasClientNativeCompletions() {
        return false;
    }

}
