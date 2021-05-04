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
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SpongeParameterValueBuilder<T> implements Parameter.Value.Builder<T> {

    private static final ValueCompleter EMPTY_COMPLETER = (context, currentInput) -> Collections.emptyList();

    private final Type typeToken;
    private final List<ValueParser<? extends T>> parsers = new ArrayList<>();
    private Parameter.@Nullable Key<T> key;
    private @Nullable ValueCompleter completer;
    private @Nullable ValueUsage usage;
    private @Nullable Predicate<CommandCause> executionRequirements;
    private boolean consumesAll;
    private boolean isOptional;
    private boolean terminal;
    private @Nullable ValueParameterModifier<T> modifier;

    public SpongeParameterValueBuilder(final @NonNull Type token) {
        this.typeToken = token;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> key(final @NonNull String key) {
        return this.key(new SpongeParameterKey<>(key, this.typeToken));
    }

    @Override public Parameter.Value.@NonNull Builder<T> key(final Parameter.@NonNull Key<T> key) {
        Objects.requireNonNull(key, "The key cannot be null");
        this.key = key;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> addParser(final @NonNull ValueParser<? extends T> parser) {
        this.parsers.add(Objects.requireNonNull(parser, "The ValueParser may not be null"));
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> completer(final @Nullable ValueCompleter completer) {
        this.completer = completer;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> modifier(final @Nullable ValueParameterModifier<T> modifier) {
        this.modifier = modifier;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> usage(final @Nullable ValueUsage usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> requiredPermission(final @Nullable String permission) {
        if (permission == null) {
            return this.requirements(null);
        } else {
            return this.requirements(commandCause -> commandCause.subject().hasPermission(permission));
        }
    }

    @Override
    public Parameter.Value.@NonNull Builder<T> requirements(final @Nullable Predicate<CommandCause> executionRequirements) {
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
    public @NonNull SpongeParameterValue<T> build() throws IllegalStateException {
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
                    final ImmutableList.Builder<CommandCompletion> builder = ImmutableList.builder();
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
                this.terminal,
                this.modifier);
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
