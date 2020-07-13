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
package org.spongepowered.common.command.parameter.managed.standard;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.brigadier.argument.AbstractArgumentParser;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class SpongeChoicesValueParameter<T> extends AbstractArgumentParser<T> {

    private final Supplier<? extends Collection<String>> choices;
    private final Function<String, ? extends T> results;
    private final boolean showInUsage;

    public SpongeChoicesValueParameter(
            final Map<String, Supplier<? extends T>> choices,
            final boolean showInUsage) {
        this(
                choices::keySet,
                x -> choices.get(x).get(),
                showInUsage
        );
    }

    public SpongeChoicesValueParameter(
            final Supplier<? extends Collection<String>> choices,
            final Function<String, ? extends T> results,
            final boolean showInUsage) {
        this.choices = choices;
        this.results = results;
        this.showInUsage = showInUsage;
    }

    @Override
    @NonNull
    public List<String> complete(@NonNull final CommandContext context) {
        return ImmutableList.copyOf(this.choices.get());
    }

    @Override
    @NonNull
    public Optional<? extends T> getValue(
            final Parameter.@NonNull Key<? super T> parameterKey,
            final ArgumentReader.@NonNull Mutable reader,
            final CommandContext.@NonNull Builder context) throws ArgumentParseException {
        final String entry = reader.parseString();
        if (this.choices.get().contains(entry)) {
            final T result = this.results.apply(entry);
            if (result != null) {
                return Optional.of(result);
            }
        }

        if (this.showInUsage) {
            throw reader.createException(Text.of(entry, " is not a valid choice!",
                    Text.newLine(),
                    Text.newLine(),
                    "Valid choices include: ", this.choices.get().stream()
                            .filter(x -> !x.equals(entry))
                            .limit(5).collect(Collectors.joining(", "))));
        }
        throw reader.createException(Text.of(entry, " is not a valid choice!"));
    }

}
