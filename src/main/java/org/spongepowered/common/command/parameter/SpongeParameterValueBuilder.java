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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SpongeParameterValueBuilder<T> implements Parameter.Value.Builder<T> {

    private static final ValueCompleter EMPTY_COMPLETER = (context, currentInput) -> ImmutableList.of();

    private final Type typeToken;
    private final List<ValueParser<? extends T>> parsers = new ArrayList<>();
    private Parameter.@Nullable Key<T> key;
    @Nullable private ValueCompleter completer;
    @Nullable private ValueUsage usage;
    @Nullable private Predicate<CommandCause> executionRequirements;
    private boolean consumesAll;
    private boolean isOptional;
    private boolean terminal;

    public SpongeParameterValueBuilder(@NonNull final Type token) {
        this.typeToken = token;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> setKey(@NonNull final String key) {
        return this.setKey(new SpongeParameterKey<>(key, this.typeToken));
    }

    @Override public Parameter.Value.@NonNull Builder<T> setKey(final Parameter.@NonNull Key<T> key) {
        Objects.requireNonNull(key, "The key cannot be null");
        this.key = key;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> parser(@NonNull final ValueParser<? extends T> parser) {
        this.parsers.add(Objects.requireNonNull(parser, "The ValueParser may not be null"));
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> setSuggestions(@Nullable final ValueCompleter completer) {
        this.completer = completer;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> setUsage(@Nullable final ValueUsage usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> setRequiredPermission(@Nullable final String permission) {
        if (permission == null) {
            return this.setUsage(null);
        } else {
            return this.setRequirements(commandCause -> commandCause.getSubject().hasPermission(permission));
        }
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> setRequirements(@Nullable final Predicate<CommandCause> executionRequirements) {
        this.executionRequirements = executionRequirements;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> consumeAllRemaining() {
        this.consumesAll = true;
        return this.terminal();
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> optional() {
        this.isOptional = true;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> terminal() {
        this.terminal = true;
        return this;
    }

    @Override
    @NonNull
    public SpongeParameterValue<T> build() throws IllegalStateException {
        Preconditions.checkState(this.key != null, "The command key may not be null");
        Preconditions.checkState(!this.parsers.isEmpty(), "There must be parsers");
        final ImmutableList.Builder<ValueParser<? extends T>> parsersBuilder = ImmutableList.builder();
        parsersBuilder.addAll(this.parsers);

        final ValueCompleter completer;
        if (this.completer != null) {
            completer = this.completer;
        } else {
            final ImmutableList.Builder<ValueCompleter> completersBuilder = ImmutableList.builder();
            for (final ValueParser<? extends T> parser : this.parsers) {
                if (parser instanceof ValueCompleter) {
                    completersBuilder.add((ValueCompleter) parser);
                }
            }

            final ImmutableList<ValueCompleter> completers = completersBuilder.build();
            if (completers.isEmpty()) {
                completer = SpongeParameterValueBuilder.EMPTY_COMPLETER;
            } else if (completers.size() == 1) {
                completer = completers.get(0);
            } else {
                completer = (context, currentInput) -> {
                    final ImmutableList.Builder<String> builder = ImmutableList.builder();
                    for (final ValueCompleter valueCompleter : completers) {
                        builder.addAll(valueCompleter.complete(context, currentInput));
                    }

                    return builder.build();
                };
            }
        }

        return new SpongeParameterValue<>(
                parsersBuilder.build(),
                completer,
                this.usage,
                this.executionRequirements == null ? commandCause -> true : this.executionRequirements,
                this.key,
                this.isOptional,
                this.consumesAll,
                this.terminal
        );
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> reset() {
        this.key = null;
        this.parsers.clear();
        this.completer = null;
        this.usage = null;
        this.isOptional = false;
        this.consumesAll = false;
        return this;
    }

}
